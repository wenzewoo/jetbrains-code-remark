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
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CrudIntentionAction {

    public abstract static class Base
            extends BaseIntentionAction implements CrudAction {

        private Editor editor;

        abstract String getName();

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getText() {
            return this.getName();
        }

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        @Override
        public String getFamilyName() {
            return this.getName();
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
            this.editor = editor;
            return null != file && this.isAvailable(Utils.filePath(editor), Utils.lineNumber(editor));
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file)
                throws IncorrectOperationException {
            this.editor = editor;
            this.actionPerformed(Utils.filePath(editor), Utils.lineNumber(editor));
        }

        @Override
        public Editor getEditor() {
            return editor;
        }
    }

    public static class Add extends Base {

        @Override
        String getName() {
            return "[MARK] Add remark";
        }

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return !CodeRemarkRepositoryFactory.getInstance().exist(filePath, lineNumber);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            super.showEditorPopup("Add Remark", this.getEditor(), new CrudActionListener.Add());
        }
    }

    public static class Edit extends Base {

        @Override
        String getName() {
            return "[MARK] Edit remark";
        }

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return CodeRemarkRepositoryFactory.getInstance().exist(filePath, lineNumber);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            super.showEditorPopup("Edit Remark", this.getEditor(), new CrudActionListener.Edit());
        }
    }

    public static class Remove extends Base {

        @Override
        String getName() {
            return "[MARK] Remove remark";
        }

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return CodeRemarkRepositoryFactory.getInstance().exist(filePath, lineNumber);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            new CrudActionListener.Remove(this.getEditor()).handle(filePath, lineNumber);
        }
    }

    public static class RemoveAll extends Base {

        @Override
        String getName() {
            return "[MARK] Remove remark with this file";
        }

        @Override
        public boolean isAvailable(final String filePath, final int lineNumber) {
            return CodeRemarkRepositoryFactory.getInstance().exist(filePath);
        }

        @Override
        public void actionPerformed(final String filePath, final int lineNumber) {
            new CrudActionListener.RemoveAll(this.getEditor()).handle(filePath, lineNumber);
        }
    }

    public static class Detail extends Base {
        @Override
        String getName() {
            return "[MARK] Show remark detail";
        }

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
}
