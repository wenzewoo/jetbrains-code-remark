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

package com.github.wenzewoo.coderemark.action.intention;

import com.github.wenzewoo.coderemark.action.BaseToggleRemarkAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import static com.github.wenzewoo.coderemark.message.CodeRemarkBundle.message;

@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class ToggleRemarkIntentionAction extends BaseIntentionAction implements BaseToggleRemarkAction {
    @Override
    public @NotNull
    @IntentionFamilyName String getFamilyName() {
        return ToggleRemarkIntentionAction.class.getName();
    }

    @Override
    public @IntentionName
    @NotNull
    String getText() {
        return message("intentionAction.text");
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
        return null != file && null != editor;
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        actionPerformed(editor);
    }
}
