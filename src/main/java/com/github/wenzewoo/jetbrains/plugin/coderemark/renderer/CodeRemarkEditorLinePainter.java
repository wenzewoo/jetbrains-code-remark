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
import com.github.wenzewoo.jetbrains.plugin.coderemark.config.CodeRemarkConfig;
import com.github.wenzewoo.jetbrains.plugin.coderemark.config.CodeRemarkConfigService;
import com.github.wenzewoo.jetbrains.plugin.coderemark.repository.CodeRemarkRepositoryFactory;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CodeRemarkEditorLinePainter extends EditorLinePainter {
    private final Map<String, CodeRemarkInlineRenderer> rendererSet = new ConcurrentHashMap<>();

    private static int getIdentifierEndOffset(@NotNull CharSequence text, int startOffset) {
        while (startOffset < text.length() && Character.isJavaIdentifierPart(text.charAt(startOffset))) startOffset++;
        return startOffset;
    }

    @Nullable
    @Override
    public Collection<LineExtensionInfo> getLineExtensions(
            @NotNull final Project project, @NotNull final VirtualFile file, final int lineNumber) {
        CodeRemarkInlayListener.getInstance(project).startListening();
        final CodeRemarkRendererState rendererState = CodeRemarkRendererState.getInstance();
        if (!rendererSet.containsKey(file.getCanonicalPath())) {
            if (lineNumber == 10) {

                FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor(file);
                if (editor instanceof TextEditor) {
                    Editor e = ((TextEditor) editor).getEditor();
                    Document document = e.getDocument();
                    CharSequence text = document.getImmutableCharSequence();
                    int offset = document.getLineEndOffset(lineNumber);

                    CodeRemarkInlineRenderer renderer = new CodeRemarkInlineRenderer("Supalle");
                    rendererSet.put(file.getCanonicalPath(), renderer);
//                    CodeRemarkInlineRenderer renderer = new CodeRemarkInlineRenderer("Supalle" + text);
                    Inlay<CodeRemarkInlineRenderer> inlay = e.getInlayModel().addAfterLineEndElement(offset, false, renderer);
                    Disposer.register(inlay, () -> {
                    });
//                int insertOffset = getIdentifierEndOffset(text, offset);
//                e.getInlayModel().getInlineElementsInRange(insertOffset, insertOffset, XDebuggerInlayUtil.MyRenderer.class).forEach(Disposer::dispose);
//                e.getInlayModel().addInlineElement(insertOffset, new XDebuggerInlayUtil.MyRenderer(inlayText));
                }

//                ApplicationManager.getApplication().invokeLater(() -> {
//                    FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor(file);
//                    if (editor instanceof TextEditor) {
//                        Editor e = ((TextEditor) editor).getEditor();
//                        Document document = e.getDocument();
//                        CharSequence text = document.getImmutableCharSequence();
//                        int offset = document.getLineEndOffset(lineNumber);
//
//                        CodeRemarkInlineRenderer renderer = new CodeRemarkInlineRenderer("Supalle");
//                        rendererSet.put(file.getCanonicalPath(), renderer);
////                    CodeRemarkInlineRenderer renderer = new CodeRemarkInlineRenderer("Supalle" + text);
//                        Inlay<CodeRemarkInlineRenderer> inlay = e.getInlayModel().addAfterLineEndElement(offset, false, renderer);
//                        Disposer.register(inlay, () -> {
//                        });
////                int insertOffset = getIdentifierEndOffset(text, offset);
////                e.getInlayModel().getInlineElementsInRange(insertOffset, insertOffset, XDebuggerInlayUtil.MyRenderer.class).forEach(Disposer::dispose);
////                e.getInlayModel().addInlineElement(insertOffset, new XDebuggerInlayUtil.MyRenderer(inlayText));
//                    }
//                });

            }
            //
//            UIUtil.invokeLaterIfNeeded(() -> {
//                FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor(file);
//                if (editor instanceof TextEditor) {
//                    Editor e = ((TextEditor) editor).getEditor();
//                    Document document = e.getDocument();
//                    CharSequence text = document.getImmutableCharSequence();
//                    int offset = document.getLineEndOffset(lineNumber);
//                    CodeRemarkInlineRenderer renderer = new CodeRemarkInlineRenderer("Supalle" + text);
//                    Inlay<CodeRemarkInlineRenderer> inlay = e.getInlayModel().addAfterLineEndElement(offset, false, renderer);
//                    Disposer.register(inlay, () -> {
//                    });
////                int insertOffset = getIdentifierEndOffset(text, offset);
////                e.getInlayModel().getInlineElementsInRange(insertOffset, insertOffset, XDebuggerInlayUtil.MyRenderer.class).forEach(Disposer::dispose);
////                e.getInlayModel().addInlineElement(insertOffset, new XDebuggerInlayUtil.MyRenderer(inlayText));
//                }
//            });
        }

        if (rendererState.get(file.getCanonicalPath())) {
            // this file is loaded, skipped, show prev extension info.
            return rendererState.getPrevExtensionInfo(file.getCanonicalPath(), lineNumber);
        }

        final List<LineExtensionInfo> result = new ArrayList<>();
        final List<Integer> lines = CodeRemarkRepositoryFactory.getInstance().lines(file.getCanonicalPath());
        if (lines.stream().anyMatch(line -> line == lineNumber)) {
            final CodeRemarkConfig config = CodeRemarkConfigService.getInstance().getState();

            final String summary = CodeRemarkRepositoryFactory.getInstance().getSummary(file.getCanonicalPath(), lineNumber);
            if (Utils.isNotEmpty(summary)) {
                if (Utils.isNotEmpty(config.getPrefix())) {
                    final Color color = CodeRemarkConfig.asColor(config.getPrefixColor());
                    result.add(new LineExtensionInfo(config.getPrefix(),
                            new TextAttributes(color, null, color, EffectType.SEARCH_MATCH, Font.BOLD | Font.ITALIC)));
                }
                final Color color = CodeRemarkConfig.asColor(config.getBodyColor());
                result.add(new LineExtensionInfo(summary,
                        new TextAttributes(color, null, color, EffectType.BOXED, Font.ITALIC)));

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
