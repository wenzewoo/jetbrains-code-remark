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
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.psi.PsiFile;
import com.intellij.serviceContainer.NonInjectable;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CrudIntentionAction {

    @NonInjectable
    public CrudIntentionAction() {
    }

    @SuppressWarnings("IntentionDescriptionNotFoundInspection")
    public static class Add extends Base {

        @Override
        String getName() {
            return "[MARK] Add remark";
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
            return null != file && !CodeRemarkRepositoryFactory.getInstance().exist(
                    Utils.filePath(editor), Utils.lineNumber(editor));
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file)
                throws IncorrectOperationException {
            super.showEditorPopup("Add Remark", editor, (filePath, lineNumber, value) -> {
                CodeRemarkRepositoryFactory.getInstance().save(filePath, lineNumber, value);
            });
        }
    }

    @SuppressWarnings("IntentionDescriptionNotFoundInspection")
    public static class Edit extends Base {

        @Override
        String getName() {
            return "[MARK] Edit remark";
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
            return null != file && CodeRemarkRepositoryFactory.getInstance().exist(
                    Utils.filePath(editor), Utils.lineNumber(editor));
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file)
                throws IncorrectOperationException {
            super.showEditorPopup("Edit Remark", editor, (filePath, lineNumber, value) -> {
                CodeRemarkRepositoryFactory.getInstance().update(filePath, lineNumber, value);
            });
        }
    }

    @SuppressWarnings("IntentionDescriptionNotFoundInspection")
    public static class Remove extends Base {

        @Override
        String getName() {
            return "[MARK] Remove remark";
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
            return null != file && CodeRemarkRepositoryFactory.getInstance().exist(
                    Utils.filePath(editor), Utils.lineNumber(editor));
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file)
                throws IncorrectOperationException {
            JBPopupFactory.getInstance().createConfirmation(
                    "Remove remark with this line?", () -> {
                        CodeRemarkRepositoryFactory.getInstance().delete(
                                Utils.filePath(editor), Utils.lineNumber(editor));
                    }, 1).showInBestPositionFor(editor);
        }
    }

    @SuppressWarnings("IntentionDescriptionNotFoundInspection")
    public static class RemoveAll extends Base {

        @Override
        String getName() {
            return "[MARK] Remove remark with this file";
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
            return null != file && CodeRemarkRepositoryFactory.getInstance().exist(Utils.filePath(editor));
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file)
                throws IncorrectOperationException {
            JBPopupFactory.getInstance().createConfirmation(
                    "Remove remark with this file?", () -> {
                        CodeRemarkRepositoryFactory.getInstance().delete(Utils.filePath(editor));
                    }, 1).showInBestPositionFor(editor);
        }
    }

    @SuppressWarnings("IntentionDescriptionNotFoundInspection")
    public static class Detail extends Base {
        @Override
        String getName() {
            return "[MARK] Show detail";
        }

        @Override
        public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
            final String summary = CodeRemarkRepositoryFactory.getInstance().getSummary(
                    Utils.filePath(editor), Utils.lineNumber(editor));
            return null != file && Utils.isNotEmpty(summary) && Utils.endsWith(summary, "..");
        }

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file)
                throws IncorrectOperationException {

            final String filePath = Utils.filePath(editor);
            final Integer lineNumber = Utils.lineNumber(editor);
            final String text = CodeRemarkRepositoryFactory.getInstance().getText(filePath, lineNumber);

            if (Utils.isNotEmpty(text)) {
                final JBPopupFactory popupFactory = JBPopupFactory.getInstance();

                // new JBColor(new Color(186, 238, 186), new Color(73, 117, 73))
                final Balloon balloon = popupFactory.createHtmlTextBalloonBuilder(
                                text, null, MessageType.INFO.getPopupBackground(), null)
                        .setCloseButtonEnabled(false)
                        .createBalloon();

                CodeRemarkRepositoryFactory.getInstance().delete(filePath, lineNumber);
                balloon.addListener(new JBPopupListener() {
                    @Override
                    public void onClosed(@NotNull final LightweightWindowEvent event) {
                        CodeRemarkRepositoryFactory.getInstance().save(filePath, lineNumber, text);
                    }
                });
                balloon.show(popupFactory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        }
    }

    public abstract static class Base extends BaseIntentionAction {
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

        private interface EditorPopupSaveListener {
            void handle(String filePath, int lineNumber, String value);
        }

        protected void showEditorPopup(
                final String title, final Editor editor, final Base.EditorPopupSaveListener saveListener) {

            final String filePath = Utils.filePath(editor);
            final Integer lineNumber = Utils.lineNumber(editor);


            final JEditorPane editorPane = new JEditorPane();
            editorPane.setPreferredSize(new JBDimension(300, 80));
            editorPane.requestFocus();
            editorPane.setBorder(BorderFactory.createEmptyBorder());
            editorPane.setText(CodeRemarkRepositoryFactory.getInstance().getText(filePath, lineNumber));

            final JBScrollPane scrollPane = new JBScrollPane(editorPane);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setPreferredSize(new JBDimension(300, 80));


            final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
            final Balloon balloon = popupFactory.createDialogBalloonBuilder(
                            scrollPane, title + ":" + (lineNumber + 1))
                    .setDialogMode(false)
                    .setHideOnClickOutside(true)
                    .setCloseButtonEnabled(false)
                    .createBalloon();

            balloon.show(popupFactory.guessBestPopupLocation(editor), Balloon.Position.below);
            editorPane.requestFocus();
            editorPane.setText(CodeRemarkRepositoryFactory.getInstance().getText(filePath, lineNumber));

            balloon.addListener(new JBPopupListener() {
                @Override
                public void onClosed(@NotNull final LightweightWindowEvent event) {
                    final String text = editorPane.getText();
                    if (Utils.isNotEmpty(text))
                        saveListener.handle(filePath, lineNumber, text);
                }
            });

            editorPane.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(final KeyEvent e) {
                    if (e.getKeyCode() == 27)  // Esc
                        balloon.hide();
                }
            });
        }

    }
}