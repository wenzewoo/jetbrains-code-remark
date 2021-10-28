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

package com.github.wenzewoo.coderemark.action.menu;

import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.action.BaseToggleRemarkAction;
import com.github.wenzewoo.coderemark.toolkit.EditorUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ComponentNotRegistered")
public class ToggleRemarkMenuAction extends AnAction implements BaseToggleRemarkAction {

    @Override
    public void update(@NotNull final AnActionEvent e) {
        super.update(e);

        final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        if (null == project) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        final Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        if (null == editor) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        final VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (null == file) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        final int lineNumber = EditorUtils.getLineNumber(editor);
        final CodeRemark codeRemark = getRepository().get(project, file, lineNumber);
        e.getPresentation().setText((null != codeRemark ? "Edit Remark" : "Add Remark"));
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        actionPerformed(CommonDataKeys.EDITOR.getData(e.getDataContext()));
    }
}
