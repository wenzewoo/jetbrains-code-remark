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

import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.github.wenzewoo.coderemark.toolkit.EditorUtils;
import com.github.wenzewoo.coderemark.toolkit.PopupUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.command.WriteCommandAction;
import org.jetbrains.annotations.NotNull;

import static com.github.wenzewoo.coderemark.message.CodeRemarkBundle.message;

public class RemoveRemarkPopupToolbarAction extends BasePopupToolbarAction {

    public RemoveRemarkPopupToolbarAction() {
        super(message("removeRemark.confirm"), AllIcons.Actions.Rollback);
    }

    @Override
    Shortcut getShortcut() {
        return KeyboardShortcut.fromString("shift DELETE");
    }

    @Override
    public void actionPerformed(@NotNull final PopupActionEvent event) {
        PopupUtils.createConfirmation(getTemplateText(), () -> {
            EditorUtils.clearAfterLineEndCodeRemark(event.getEditor(), event.getLineNumber());

            WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
                // Remove from repository.
                CodeRemarkRepositoryFactory.getInstance(event.getProject()).remove(
                        event.getProject(), event.getFile(), event.getLineNumber());

                getPublisher().codeRemarkChanged(event.getProject(), event.getFile());
            });

            super.dispose();
        }).showInFocusCenter();
    }
}