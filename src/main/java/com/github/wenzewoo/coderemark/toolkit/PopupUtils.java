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

package com.github.wenzewoo.coderemark.toolkit;

import com.github.wenzewoo.coderemark.action.toolbar.BasePopupToolbarAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PopupUtils {

    public static JBPopup createComponent(final String title, final JComponent component, final JComponent focusComponent) {

        return JBPopupFactory.getInstance()
                .createComponentPopupBuilder(component, focusComponent)
                .setCancelKeyEnabled(true)
                .setTitle(title)
                .setModalContext(true)
                .setCancelButton(new IconButton("Cancel?", AllIcons.Actions.Cancel))
                .setResizable(true)
                .setRequestFocus(true)
                .setMovable(false)
                .createPopup();
    }

    public static JBPopup createMessage(@NotNull final String message, final Object... args) {
        return JBPopupFactory.getInstance().createMessage(StringUtils.format(message, args));
    }

    public static JBPopup createConfirmation(final String title, @NotNull final Runnable onYes) {
        return JBPopupFactory.getInstance().createConfirmation(title, onYes, 1);
    }

    public static void showCodeRemarkEditor(
            @NotNull final Editor editor, final String title,
            @Nullable final String defaultVal, final BasePopupToolbarAction... actions) {

        final int lineNumber = EditorUtils.getLineNumber(editor);

        // create ui
        final BorderLayoutPanel layoutPanel = JBUI.Panels.simplePanel();

        final JEditorPane editorPane = new JEditorPane();
        editorPane.setBorder(JBUI.Borders.empty(5));
        editorPane.setPreferredSize(SwingUtils.createDimension(defaultVal, editorPane.getFont(), 300, 600, 80, 120));
        editorPane.setText(defaultVal);
        final JBScrollPane scrollPane = new JBScrollPane(editorPane);
        scrollPane.setBorder(JBUI.Borders.empty());
        layoutPanel.addToCenter(scrollPane);


        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        final ActionToolbarImpl toolbar = new ActionToolbarImpl("CodeRemarkPopup.toolbar", actionGroup, true);
        toolbar.setBorder(JBUI.Borders.empty());
        toolbar.setBackground(UIUtil.getToolTipActionBackground());
        layoutPanel.addToBottom(toolbar);

        // create popup
        final JBPopup popup = PopupUtils.createComponent(title + ": " + (lineNumber + 1), layoutPanel, editorPane);
        for (final BasePopupToolbarAction action : actions) {
            action.setEvent(new BasePopupToolbarAction.PopupActionEvent(popup, editor, editorPane, lineNumber));
            action.registerCustomShortcutSet();
            actionGroup.add(action);
        }
        popup.showInBestPositionFor(editor);
    }
}
