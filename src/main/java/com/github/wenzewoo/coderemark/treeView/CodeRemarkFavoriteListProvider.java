/*
 * MIT License
 *
 * Copyright (c) 2021 吴汶泽 <wenzewoo@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.wenzewoo.coderemark.treeView;

import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.listener.CodeRemarkListener;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import com.intellij.ide.favoritesTreeView.AbstractFavoritesListProvider;
import com.intellij.ide.favoritesTreeView.FavoritesListNode;
import com.intellij.ide.favoritesTreeView.FavoritesManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.github.wenzewoo.coderemark.message.CodeRemarkBundle.message;

public class CodeRemarkFavoriteListProvider extends AbstractFavoritesListProvider<CodeRemark> implements CodeRemarkListener {
    protected CodeRemarkFavoriteListProvider(@NotNull final Project project) {
        super(project, message("favorite.listName"));
        project.getMessageBus().connect().subscribe(CodeRemarkListener.TOPIC, this);
        updateChildren();
    }

    private AbstractTreeNode<CodeRemark> createFileNode(List<AbstractTreeNode<CodeRemark>> childNode, AbstractTreeNode parent, CodeRemark subRemark) {

        CodeRemark fileRemark = new CodeRemark();
        fileRemark.setFileName(subRemark.getFileName());
        fileRemark.setFileUrl(subRemark.getFileUrl());
        fileRemark.setContentHash(subRemark.getContentHash());
        fileRemark.setLineNumber(1);
        fileRemark.setText("");
        AbstractTreeNode<CodeRemark> treeNode = new AbstractTreeNode<>(myProject, fileRemark) {
            private List<AbstractTreeNode<CodeRemark>> children = new ArrayList<>();

            @Override
            protected void update(@NotNull final PresentationData presentation) {
                presentation.setIcon(CodeRemark.getIcon());
                String tip = fileRemark.getFileName();
                presentation.setTooltip(tip);
                presentation.setPresentableText(tip);
            }

            @Override
            public boolean canNavigate() {
                return false;
            }

            @Override
            public boolean canNavigateToSource() {
                return false;
            }

            @Override
            public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
                return children;
            }
        };
        treeNode.setParent(parent);
        childNode.add(treeNode);

        return treeNode;
    }

    private void updateChildren() {
        if (myProject.isDisposed()) return;
        myChildren.clear();

        final List<CodeRemark> codeRemarks = CodeRemarkRepositoryFactory.getInstance().list(myProject);
        Map<String, FileNode> nodeMap = new HashMap<>();
        for (final CodeRemark codeRemark : codeRemarks) {
            String file = codeRemark.getFileName();

            FileNode fileNode = nodeMap.get(file);
            if (fileNode == null) {
                fileNode = new FileNode(myChildren, myNode, myProject, codeRemark);
                nodeMap.put(file, fileNode);
            }
            RemarkNode node = new RemarkNode(myProject, codeRemark);
            node.setParent(fileNode);
            fileNode.addChildren(node);

        }
        FavoritesManager.getInstance(myProject).fireListeners(getListName(myProject));
    }

    @Override
    public void customizeRenderer(final ColoredTreeCellRenderer renderer, final JTree tree, @NotNull final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        if (value instanceof CodeRemark) {
            final CodeRemark codeRemark = (CodeRemark) value;
            SimpleTextAttributes attr = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, Color.BLUE);
            if (StringUtils.isNotEmpty(codeRemark.getText()))
                renderer.append(StringUtils.maxLength(codeRemark.getText(), 20) + " ", attr, true);

            renderer.append(codeRemark.getFileName(), attr, true);
            renderer.append(":", attr, true);
            renderer.append(String.valueOf(codeRemark.getLineNumber() + 1), attr, true);
        }
    }

    @Override
    public void codeRemarkChanged(@NotNull final Project project, @NotNull final VirtualFile file) {
        updateChildren();
    }

    @Override
    public int getWeight() {
        return 0;
    }
}

class RemarkNode extends AbstractTreeNode<CodeRemark> {
    private CodeRemark codeRemark;

    protected RemarkNode(Project project, @NotNull CodeRemark value) {
        super(project, value);
        this.codeRemark = value;
    }


    @Override
    protected void update(@NotNull final PresentationData presentation) {
        presentation.setIcon(CodeRemark.getIcon());
        String tip = codeRemark.getFileName() + ":" + (codeRemark.getLineNumber() + 1);
        presentation.setTooltip(tip);
        presentation.setPresentableText(codeRemark.getText());//TODO 显示行号、日期
    }

    @Override
    public boolean canNavigate() {
        return codeRemark.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return codeRemark.canNavigateToSource();
    }

    @Override
    public void navigate(final boolean requestFocus) {
        codeRemark.navigate(requestFocus);
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return Collections.emptyList();
    }
}

class FileNode extends AbstractTreeNode<CodeRemark> {
    private CodeRemark codeRemark;

    private List<AbstractTreeNode<CodeRemark>> children = new ArrayList<>();

    protected FileNode(List<AbstractTreeNode<CodeRemark>> list, FavoritesListNode root, Project project, @NotNull CodeRemark value) {
        super(project, value);
        this.codeRemark = new CodeRemark();
        codeRemark.setFileName(value.getFileName());
        codeRemark.setFileUrl(value.getFileUrl());
        codeRemark.setContentHash(value.getContentHash());
        codeRemark.setLineNumber(1);
        codeRemark.setText("");
        this.setParent(root);
        list.add(this);
    }

    @Override
    protected void update(@NotNull final PresentationData presentation) {
        presentation.setIcon(CodeRemark.getIcon());// TODO 参照BookmarkNode显示文件类型图标
        String tip = codeRemark.getFileName();
        presentation.setTooltip(tip);
        presentation.setPresentableText(tip);// TODO 参照BookmarkNode显示完整路径
    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public @NotNull Collection<? extends AbstractTreeNode<?>> getChildren() {
        return children;
    }

    public void addChildren(RemarkNode remarkNode) {
        children.add(remarkNode);// TODO 按行号排序
    }
}