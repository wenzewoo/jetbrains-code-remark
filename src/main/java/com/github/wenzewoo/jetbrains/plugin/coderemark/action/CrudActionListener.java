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
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import org.jetbrains.annotations.NotNull;

public interface CrudActionListener {

    default void handle(final String filePath, final int lineNumber) {
        this.handle(filePath, lineNumber, null);
    }


    default void handle(final String filePath, final int lineNumber, final String inputValue) {

    }


    class Add implements CrudActionListener {

        @Override
        public void handle(final String filePath, final int lineNumber, final String inputValue) {
            CodeRemarkRepositoryFactory.getInstance().save(filePath, lineNumber, inputValue);
        }
    }

    class Edit implements CrudActionListener {

        @Override
        public void handle(final String filePath, final int lineNumber, final String inputValue) {
            CodeRemarkRepositoryFactory.getInstance().update(filePath, lineNumber, inputValue);
        }
    }

    class Detail implements CrudActionListener {

        private final Editor editor;

        public Detail(final Editor editor) {
            this.editor = editor;
        }

        @Override
        public void handle(final String filePath, final int lineNumber) {
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

    class Remove implements CrudActionListener {

        private final Editor editor;

        public Remove(final Editor editor) {
            this.editor = editor;
        }

        @Override
        public void handle(final String filePath, final int lineNumber) {
            JBPopupFactory.getInstance().createConfirmation(
                    "Remove remark with this line?", () -> {
                        CodeRemarkRepositoryFactory.getInstance().delete(filePath, lineNumber);
                    }, 1).showInBestPositionFor(editor);
        }
    }

    class RemoveAll implements CrudActionListener {

        private final Editor editor;

        public RemoveAll(final Editor editor) {
            this.editor = editor;
        }

        @Override
        public void handle(final String filePath, final int lineNumber) {
            JBPopupFactory.getInstance().createConfirmation(
                    "Remove remark with this file?", () -> {
                        CodeRemarkRepositoryFactory.getInstance().delete(filePath);
                    }, 1).showInBestPositionFor(editor);
        }
    }
}
