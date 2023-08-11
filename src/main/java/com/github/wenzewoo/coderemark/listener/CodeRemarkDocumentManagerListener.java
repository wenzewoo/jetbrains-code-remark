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

package com.github.wenzewoo.coderemark.listener;

import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.renderer.CodeRemarkEditorInlineInlayRenderer;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepository;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class CodeRemarkDocumentManagerListener implements FileDocumentManagerListener {


    @NotNull
    public CodeRemarkListener getPublisher(final Project project) {
        return project.getMessageBus().syncPublisher(CodeRemarkListener.TOPIC);
    }

    @Override
    public void beforeDocumentSaving(@NotNull final Document document) {
        final VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (null == file || !file.isWritable()) return; // If it is an editable file (!file.is Writable())

        final Project project = ProjectLocator.getInstance().guessProjectForFile(file);
        if (null == project) return;
        if (document.getLineCount() == 0) return; // No content, skipped.

        final Editor[] editors = EditorFactory.getInstance().getEditors(document, project);
        if (editors.length > 0) {
            // The offset of inlay may have been changed, and it needs to be retrieved and saved again
            final int endOffset = document.getLineEndOffset(document.getLineCount() - 1);
            final List<Inlay<? extends CodeRemarkEditorInlineInlayRenderer>> inlays = editors[0].getInlayModel()
                    .getAfterLineEndElementsInRange(0, endOffset, CodeRemarkEditorInlineInlayRenderer.class);

            if (!inlays.isEmpty()) {
                final List<CodeRemark> codeRemarks = inlays.stream().map(inlay -> {
                    final int newLineNumber = document.getLineNumber(inlay.getOffset());
                    return new CodeRemark(project, file, newLineNumber, inlay.getRenderer().getText());
                }).collect(Collectors.toList());

                final CodeRemarkRepository codeRemarkRepository = CodeRemarkRepositoryFactory.getInstance(project);
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    codeRemarkRepository.remove(project, file);
                    codeRemarkRepository.saveBatch(codeRemarks);
                    getPublisher(project).codeRemarkChanged(project, file);
                });
            }
        }
    }
}