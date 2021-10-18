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

package com.github.wenzewoo.jetbrains.plugin.coderemark.action;

import com.github.wenzewoo.jetbrains.plugin.coderemark.Utils;
import com.github.wenzewoo.jetbrains.plugin.coderemark.repository.CodeRemarkRepositoryFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class CrudRightMenuAction {

    public static abstract class Base
            extends AnAction implements CrudAction {

        private Editor editor;
        private PsiFile psiFile;

        @Override
        public void update(@NotNull final AnActionEvent e) {
            super.update(e);
            this.initProperty(e);
            if (null == this.editor) {
                e.getPresentation().setEnabledAndVisible(false);
                return;
            }
            if (null == this.psiFile) {
                e.getPresentation().setEnabledAndVisible(false);
                return;
            }
            e.getPresentation().setEnabledAndVisible(
                    this.isAvailable(Utils.filePath(editor), Utils.lineNumber(editor))
            );
        }

        @Override
        public void actionPerformed(@NotNull final AnActionEvent e) {
            this.initProperty(e); // To be on the safe side, init again?

            if (null != this.editor && null != this.psiFile)
                this.actionPerformed(Utils.filePath(editor), Utils.lineNumber(editor));
        }

        void initProperty(final AnActionEvent e) {
            this.editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
            this.psiFile = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
        }

        @Override
        public Editor getEditor() {
            return editor;
        }

        public PsiFile getPsiFile() {
            return psiFile;
        }
    }


    @SuppressWarnings("ComponentNotRegistered")
    public static class Add extends Base {

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return !CodeRemarkRepositoryFactory.getInstance().exist(filePath, lineNumber);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            this.showEditorPopup("Add Remark", this.getEditor(), new CrudActionListener.Add());
        }
    }


    @SuppressWarnings("ComponentNotRegistered")
    public static class Edit extends Base {

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return CodeRemarkRepositoryFactory.getInstance().exist(filePath, lineNumber);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            this.showEditorPopup("Edit Remark", this.getEditor(), new CrudActionListener.Edit());
        }
    }


    @SuppressWarnings("ComponentNotRegistered")
    public static class Detail extends Base {

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            final String summary = CodeRemarkRepositoryFactory.getInstance().getSummary(filePath, lineNumber);
            return Utils.isNotEmpty(summary) && Utils.endsWith(summary, "..");
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            new CrudActionListener.Detail(this.getEditor()).handle(filePath, lineNumber);
        }
    }


    @SuppressWarnings("ComponentNotRegistered")
    public static class Remove extends Base {

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return CodeRemarkRepositoryFactory.getInstance().exist(filePath, lineNumber);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            new CrudActionListener.Remove(this.getEditor()).handle(filePath, lineNumber);
        }
    }


    @SuppressWarnings("ComponentNotRegistered")
    public static class RemoveAll extends Base {

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return CodeRemarkRepositoryFactory.getInstance().exist(filePath);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            new CrudActionListener.RemoveAll(this.getEditor()).handle(filePath, lineNumber);
        }
    }
}
