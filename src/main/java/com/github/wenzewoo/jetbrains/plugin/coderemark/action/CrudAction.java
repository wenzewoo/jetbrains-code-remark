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
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public interface CrudAction {

    Editor getEditor();

    boolean isAvailable(String filePath, int lineNumber);

    void actionPerformed(String filePath, int lineNumber);

    default void showEditorPopup(
            final String title, final Editor editor, final CrudActionListener listener) {

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
                    listener.handle(filePath, lineNumber, text);
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
