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

import cn.hutool.core.util.StrUtil;
import com.github.wenzewoo.jetbrains.plugin.coderemark.form.CodeRemarkEditorForm;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class AbstractCodeRemarkIntentionAction extends BaseIntentionAction {

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

    interface OnEditorSaveListener {
        void handle(String value);
    }

    protected void showEditorPopup(String title, Editor editor, String defaultVal, OnEditorSaveListener saveListener) {
        final CodeRemarkEditorForm remarkEditor = new CodeRemarkEditorForm();
        final JEditorPane editorPane = remarkEditor.getEditorPane();

        final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
        final Balloon balloon = popupFactory.createDialogBalloonBuilder(remarkEditor.getRootPane(), title)
                .setDialogMode(true)
                .setRequestFocus(true)
                .createBalloon();

        balloon.show(popupFactory.guessBestPopupLocation(editor), Balloon.Position.below);
        editorPane.requestFocus();

        if (StrUtil.isNotEmpty(defaultVal))
            editorPane.setText(defaultVal);

        editorPane.addKeyListener(new KeyListener() {
            final long interval = 300;
            long prev = System.currentTimeMillis();

            @Override
            public void keyTyped(KeyEvent e) {
                // Skipped.
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Skipped.
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == 27) {
                    if (System.currentTimeMillis() - prev < interval)
                        balloon.hide(); // Two consecutive times
                    prev = System.currentTimeMillis();
                    if (StrUtil.isEmpty(editorPane.getText())) return; // Skipped.
                    saveListener.handle(editorPane.getText());
                    balloon.hide();
                }
            }
        });
    }
}
