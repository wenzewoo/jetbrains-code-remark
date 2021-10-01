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
import com.github.wenzewoo.jetbrains.plugin.coderemark.Utils;
import com.github.wenzewoo.jetbrains.plugin.coderemark.repository.CodeRemarkRepositoryFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ShowCodeRemarkIntentionAction extends AbstractCodeRemarkIntentionAction {
    @Override
    String getName() {
        return "[MARK] Show this code mark detail";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        final String summary = CodeRemarkRepositoryFactory.getInstance().getSummary(
                Utils.filePath(editor), Utils.lineNumber(editor));
        return StrUtil.isNotEmpty(summary) && StrUtil.endWith(summary, "..");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file)
            throws IncorrectOperationException {
        final String text = CodeRemarkRepositoryFactory.getInstance().getText(
                Utils.filePath(editor), Utils.lineNumber(editor));

        if (StrUtil.isNotEmpty(text)) {
            final JBPopupFactory popupFactory = JBPopupFactory.getInstance();
            popupFactory.createHtmlTextBalloonBuilder(
                            text, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                    .setFadeoutTime(5000)
                    .createBalloon()
                    .show(popupFactory.guessBestPopupLocation(editor), Balloon.Position.below);
        }
    }
}
