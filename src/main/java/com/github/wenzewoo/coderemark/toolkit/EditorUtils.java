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

import com.github.wenzewoo.coderemark.renderer.CodeRemarkEditorInlineInlayRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorUtils {

    @Nullable
    public static String getCanonicalPath(@NotNull final Editor editor) {
        if (editor instanceof EditorEx) {

            final VirtualFile virtualFile = ((EditorEx) editor).getVirtualFile();
            if (null == virtualFile) return null;

            return virtualFile.getCanonicalPath();
        }
        return null;
    }

    public static int getLineNumber(@NotNull final Editor editor) {
        return editor.getDocument().getLineNumber(
                editor.getCaretModel().getOffset());
    }

    public static void addAfterLineCodeRemark(@NotNull final Editor editor, final int lineNumber, @NotNull final String text) {
        addAfterLineEndElement(editor, lineNumber, new CodeRemarkEditorInlineInlayRenderer(text));
    }

    public static void addAfterLineEndElement(
            @NotNull final Editor editor, final int lineNumber, @NotNull final EditorCustomElementRenderer renderer) {

        // if exists, clear it.
        clearAfterLineEndElement(editor, lineNumber, renderer.getClass());

        final int endOffset = editor.getDocument().getLineEndOffset(lineNumber);
        editor.getInlayModel().addAfterLineEndElement(endOffset, true, renderer);
    }

    public static void clearAfterLineEndCodeRemark(@NotNull final Editor editor, final int lineNumber) {
        clearAfterLineEndElement(editor, lineNumber, CodeRemarkEditorInlineInlayRenderer.class);
    }

    public static void clearAfterLineEndElement(
            @NotNull final Editor editor, final int lineNumber, @NotNull final Class<? extends EditorCustomElementRenderer> rendererClass) {

        editor.getInlayModel().getAfterLineEndElementsForLogicalLine(lineNumber).forEach(inlay -> {
            if (inlay.getRenderer().getClass().getName().equals(rendererClass.getName())) {
                Disposer.dispose(inlay); // Destroy Inlay
            }
        });
    }
}
