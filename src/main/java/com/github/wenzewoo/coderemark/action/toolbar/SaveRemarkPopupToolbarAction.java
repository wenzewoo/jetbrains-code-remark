/*
 * MIT License
 *
 * Copyright (c) 2023 吴汶泽 <wenzewoo@gmail.com>
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

import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.exception.UiIllegalArgumentException;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.github.wenzewoo.coderemark.toolkit.EditorUtils;
import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.command.WriteCommandAction;
import org.jetbrains.annotations.NotNull;

import static com.github.wenzewoo.coderemark.message.CodeRemarkBundle.message;

public class SaveRemarkPopupToolbarAction extends BasePopupToolbarAction {

    public SaveRemarkPopupToolbarAction() {
        super(message("addRemark.confirm"), Actions.CheckOut);
    }

    @Override
    Shortcut getShortcut() {
        return KeyboardShortcut.fromString("shift ENTER");
    }

    @Override
    public void actionPerformed(@NotNull final PopupActionEvent event) {
        final String text = event.getEditorPane().getText();
        if (StringUtils.isEmpty(text)) {
            throw new UiIllegalArgumentException(message("addRemark.emptyText"));
        }

        EditorUtils.addAfterLineCodeRemark(event.getEditor(), event.getLineNumber(), text);

        WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
            // Save to repository.
            CodeRemarkRepositoryFactory.getInstance(event.getProject()).save(
                    new CodeRemark(event.getProject(), event.getFile(), event.getLineNumber(), text));
            getPublisher().codeRemarkChanged(event.getProject(), event.getFile());
        });
        super.dispose();
    }
}