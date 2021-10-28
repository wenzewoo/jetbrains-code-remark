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
import com.intellij.ide.favoritesTreeView.FavoritesManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CodeRemarkFavoriteListProvider extends AbstractFavoritesListProvider<CodeRemark> implements CodeRemarkListener {
    protected CodeRemarkFavoriteListProvider(@NotNull final Project project) {
        super(project, "Remarks");
        project.getMessageBus().connect().subscribe(CodeRemarkListener.TOPIC, this);
        updateChildren();
    }

    void updateChildren() {
        if (myProject.isDisposed()) return;
        myChildren.clear();

        final List<CodeRemark> codeRemarks = CodeRemarkRepositoryFactory.getInstance().list(myProject);
        for (final CodeRemark codeRemark : codeRemarks) {

            final AbstractTreeNode<CodeRemark> treeNode = new AbstractTreeNode<>(myProject, codeRemark) {
                @Override
                protected void update(@NotNull final PresentationData presentation) {
                    presentation.setIcon(CodeRemark.getIcon());
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
                public @NotNull
                Collection<? extends AbstractTreeNode<?>> getChildren() {
                    return Collections.emptyList();
                }
            };
            treeNode.setParent(myNode);
            myChildren.add(treeNode);
        }
        FavoritesManager.getInstance(myProject).fireListeners(getListName(myProject));
    }

    @Override
    public void customizeRenderer(final ColoredTreeCellRenderer renderer, final JTree tree,
                                  @NotNull final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        if (value instanceof CodeRemark) {
            final CodeRemark codeRemark = (CodeRemark) value;
            if (StringUtils.isNotEmpty(codeRemark.getText()))
                renderer.append(StringUtils.maxLength(codeRemark.getText(), 20) + " ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES, true);

            renderer.append(codeRemark.getFileName(), SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
            renderer.append(":", SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
            renderer.append(String.valueOf(codeRemark.getLineNumber() + 1), SimpleTextAttributes.GRAYED_ATTRIBUTES, true);
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
