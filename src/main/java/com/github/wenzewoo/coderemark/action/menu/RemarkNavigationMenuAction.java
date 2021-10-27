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

package com.github.wenzewoo.coderemark.action.menu;

import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import com.github.wenzewoo.coderemark.toolkit.VirtualFileUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ComponentNotRegistered")
public class RemarkNavigationMenuAction extends AnAction {

    @Override
    public void update(@NotNull final AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabledAndVisible(currentFileRemarks(e).size() > 0);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final List<CodeRemark> codeRemarks = currentFileRemarks(e);

        if (!codeRemarks.isEmpty()) {
            final String title = StringUtils.format("{0} remarks",
                    codeRemarks.get(0).getFileName());

            final ListPopupStep<CodeRemark> listPopupStep =
                    new CodeRemarkNavigationListPopupStep(title, e.getDataContext(), codeRemarks);
            JBPopupFactory.getInstance().createListPopup(listPopupStep).showInFocusCenter();
        }
    }

    List<CodeRemark> currentFileRemarks(final AnActionEvent e) {
        final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        final Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());

        if (null == project || null == editor || null == file || file.isWritable())
            return Collections.emptyList();

        return CodeRemarkRepositoryFactory.getInstance().list(
                project.getName(), file.getName(), VirtualFileUtils.getContentHash(file));
    }

    static class CodeRemarkNavigationListPopupStep implements ListPopupStep<CodeRemark> {

        private final String title;
        private final DataContext context;
        private final List<CodeRemark> codeRemarks;

        public CodeRemarkNavigationListPopupStep(final String title, final DataContext context, final List<CodeRemark> codeRemarks) {
            this.title = title;
            this.context = context;
            this.codeRemarks = codeRemarks;
        }

        @Override
        public @NotNull
        List<CodeRemark> getValues() {
            return codeRemarks;
        }

        @Override
        public boolean isSelectable(final CodeRemark value) {
            return true;
        }

        @Override
        public @Nullable
        Icon getIconFor(final CodeRemark value) {
            return CodeRemark.getIcon();
        }

        @Override
        public @NlsContexts.ListItem
        @NotNull
        String getTextFor(final CodeRemark value) {
            return StringUtils.format("[Line:{0}] {1}", value.getLineNumber() + 1, value.getText());
        }

        @Override
        public @Nullable
        ListSeparator getSeparatorAbove(final CodeRemark value) {
            return null;
        }

        @Override
        public int getDefaultOptionIndex() {
            return 0;
        }

        @Override
        public @NlsContexts.PopupTitle
        @Nullable
        String getTitle() {
            return title;
        }

        @Override
        public @Nullable
        PopupStep<?> onChosen(final CodeRemark selectedValue, final boolean finalChoice) {
            final Editor editor = CommonDataKeys.EDITOR.getData(context);
            final Project project = CommonDataKeys.PROJECT.getData(context);
            if (null != editor) {
                selectedValue.getTarget(project).navigateIn(editor);
            }
            return null;
        }

        @Override
        public boolean hasSubstep(final CodeRemark selectedValue) {
            return false;
        }

        @Override
        public void canceled() {

        }

        @Override
        public boolean isMnemonicsNavigationEnabled() {
            return false;
        }

        @Override
        public @Nullable
        MnemonicNavigationFilter<CodeRemark> getMnemonicNavigationFilter() {
            return null;
        }

        @Override
        public boolean isSpeedSearchEnabled() {
            return false;
        }

        @Override
        public @Nullable
        SpeedSearchFilter<CodeRemark> getSpeedSearchFilter() {
            return null;
        }

        @Override
        public boolean isAutoSelectionEnabled() {
            return false;
        }

        @Override
        public @Nullable
        Runnable getFinalRunnable() {
            return null;
        }
    }
}
