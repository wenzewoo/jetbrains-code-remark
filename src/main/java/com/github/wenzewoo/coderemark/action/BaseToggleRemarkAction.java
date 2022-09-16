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

package com.github.wenzewoo.coderemark.action;

import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.action.toolbar.BasePopupToolbarAction;
import com.github.wenzewoo.coderemark.action.toolbar.RemoveRemarkPopupToolbarAction;
import com.github.wenzewoo.coderemark.action.toolbar.SaveRemarkPopupToolbarAction;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepository;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.github.wenzewoo.coderemark.toolkit.EditorUtils;
import com.github.wenzewoo.coderemark.toolkit.PopupUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

import static com.github.wenzewoo.coderemark.message.CodeRemarkBundle.message;

public interface BaseToggleRemarkAction {

    default CodeRemarkRepository getRepository() {
        return CodeRemarkRepositoryFactory.getInstance();
    }


    default void actionPerformed(final Editor editor) {
        if (null == editor) {
            return;
        }

        final Project project = editor.getProject();
        final int lineNumber = EditorUtils.getLineNumber(editor);
        final VirtualFile file = EditorUtils.getVirtualFile(editor);

        if (null != file && null != project) {
            final CodeRemark codeRemark = getRepository().get(project, file, lineNumber);

            final List<BasePopupToolbarAction> actions = new ArrayList<>();
            actions.add(new SaveRemarkPopupToolbarAction());
            if (null != codeRemark) {
                actions.add(new RemoveRemarkPopupToolbarAction());
            }

            final String fileName = file.getName();
            final String title = (null != codeRemark ? message("editRemark.title", fileName, lineNumber + 1) : message("addRemark.title", fileName, lineNumber + 1));
            final String defaultVal = (null != codeRemark ? codeRemark.getText() : null);
            PopupUtils.createCodeRemarkEditor(editor, file, title, defaultVal,
                    actions.toArray(new BasePopupToolbarAction[0])).showInBestPositionFor(editor);
        }
    }
}
