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

package com.github.wenzewoo.coderemark.action.toolbar;

import com.github.wenzewoo.coderemark.exception.UiIllegalArgumentException;
import com.github.wenzewoo.coderemark.toolkit.PopupUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class BasePopupToolbarAction extends AnAction {
    private PopupActionEvent event;

    public BasePopupToolbarAction(@NotNull final String text, @NotNull final Icon icon) {
        super(text, text, icon);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        try {
            if (null == event)
                throw new UiIllegalArgumentException("This PopupActionEvent is null");

            actionPerformed(event);
        } catch (final Throwable ex) {
            if (ex instanceof UiIllegalArgumentException)
                PopupUtils.createMessage(ex.getMessage()).showInFocusCenter();
            else
                ex.printStackTrace();
        }
    }

    void dispose() {
        if (null != event.popup)
            event.popup.dispose();
    }

    abstract Shortcut getShortcut();

    public void registerCustomShortcutSet() {
        final Shortcut shortcut = getShortcut();
        if (null != shortcut)
            registerCustomShortcutSet(() -> new Shortcut[]{shortcut}, event.editorPane);
    }

    abstract void actionPerformed(@NotNull PopupActionEvent event);

    public void setEvent(@NotNull final PopupActionEvent event) {
        this.event = event;
    }

    public static class PopupActionEvent {
        public JBPopup popup;
        public Editor editor;
        public JEditorPane editorPane;
        public int lineNumber;

        public PopupActionEvent(final JBPopup popup, final Editor editor, final JEditorPane editorPane, final int lineNumber) {
            this.popup = popup;
            this.editor = editor;
            this.editorPane = editorPane;
            this.lineNumber = lineNumber;
        }
    }
}
