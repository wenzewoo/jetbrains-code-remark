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

package com.github.wenzewoo.jetbrains.plugin.coderemark.renderer;

import com.github.wenzewoo.jetbrains.plugin.coderemark.Utils;
import com.github.wenzewoo.jetbrains.plugin.coderemark.repository.CodeRemarkRepositoryFactory;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CodeRemarkEditorLinePainter extends EditorLinePainter {

    @Nullable
    @Override
    public Collection<LineExtensionInfo> getLineExtensions(
            @NotNull final Project project, @NotNull final VirtualFile file, final int lineNumber) {
        final CodeRemarkRendererState rendererState = CodeRemarkRendererState.getInstance();

        if (rendererState.get(file.getCanonicalPath())) {
            // this file is loaded, skipped, show prev extension info.
            return rendererState.getPrevExtensionInfo(file.getCanonicalPath(), lineNumber);
        }

        final List<LineExtensionInfo> result = new ArrayList<>();
        final List<Integer> lines = CodeRemarkRepositoryFactory.getInstance().lines(file.getCanonicalPath());
        if (lines.stream().anyMatch(line -> line == lineNumber)) {
            final String summary = CodeRemarkRepositoryFactory.getInstance().getSummary(file.getCanonicalPath(), lineNumber);
            if (Utils.isNotEmpty(summary)) {
                result.add(new LineExtensionInfo("// [MARK]: ",
                        new TextAttributes(JBColor.red, null, JBColor.red, EffectType.SEARCH_MATCH, Font.BOLD | Font.ITALIC)));

                result.add(new LineExtensionInfo(summary,
                        new TextAttributes(JBColor.red, null, JBColor.red, EffectType.BOXED, Font.ITALIC)));

                rendererState.incrementLine(file.getCanonicalPath())
                        .appendPrevExtensionInfo(file.getCanonicalPath(), lineNumber, result);
            }
        }
        if (rendererState.loadedLineCount(file.getCanonicalPath()) == lines.size()) {
            rendererState.resetLine(file.getCanonicalPath()).set(file.getCanonicalPath(), true); // loading completed
        }
        return result;
    }
}
