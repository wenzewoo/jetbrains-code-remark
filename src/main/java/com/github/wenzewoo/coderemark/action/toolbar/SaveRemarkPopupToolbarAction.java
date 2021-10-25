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
import com.github.wenzewoo.coderemark.repository.CodeRemark;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.github.wenzewoo.coderemark.toolkit.EditorUtils;
import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import org.jetbrains.annotations.NotNull;

public class SaveRemarkPopupToolbarAction extends BasePopupToolbarAction {

    public SaveRemarkPopupToolbarAction() {
        super("Save this code remark?", AllIcons.Actions.Commit);
    }

    @Override
    Shortcut getShortcut() {
        return KeyboardShortcut.fromString("shift ENTER");
    }

    @Override
    void actionPerformed(@NotNull final PopupActionEvent event) {
        final String canonicalPath = EditorUtils.getCanonicalPath(event.editor);

        if (StringUtils.isNotEmpty(canonicalPath)) {
            final String text = event.editorPane.getText();
            if (StringUtils.isEmpty(text))
                throw new UiIllegalArgumentException("Please enter the content");

            EditorUtils.addAfterLineCodeRemark(event.editor, event.lineNumber, text);
            CodeRemarkRepositoryFactory.getInstance().save(
                    new CodeRemark(event.lineNumber, canonicalPath, text));
        }
        super.dispose();
    }
}
