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

package com.github.wenzewoo.coderemark.listener;

import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepository;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.github.wenzewoo.coderemark.toolkit.VirtualFileUtils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CodeRemarkBulkFileListener implements BulkFileListener {
    private final static CodeRemarkRepository mCodeRemarkRepository = CodeRemarkRepositoryFactory.getInstance();

    @Override
    public void after(@NotNull final List<? extends VFileEvent> events) {
        events.stream()
                .filter(this::needFilter)
                .forEach(event -> {
                    if (event instanceof VFileDeleteEvent) {
                        afterDeleteChange((VFileDeleteEvent) event);
                    } else if (event instanceof VFileMoveEvent) {
                        afterMoveChange((VFileMoveEvent) event);
                    } else if (event instanceof VFilePropertyChangeEvent) {
                        afterPropertyChange(((VFilePropertyChangeEvent) event));
                    }
                });
    }

    private Project getCurrentProject(@NotNull final VirtualFile file) {
        return ProjectLocator.getInstance().guessProjectForFile(file);
    }

    void afterDeleteChange(final VFileDeleteEvent event) {
        final VirtualFile file = event.getFile();
        final Project project = getCurrentProject(file);

        if (null == project) return; // Skipped

        if (mCodeRemarkRepository.exists(project, file)) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                mCodeRemarkRepository.remove(project, file);
            });
        }
    }

    void afterMoveChange(final VFileMoveEvent event) {
        final VirtualFile file = event.getFile();
        moveCodeRemarks(file, event.getOldPath(), file.getName());
    }

    void moveCodeRemarks(final VirtualFile newFile, final String oldPath, final String oldFileName) {
        // Find old coderemarks
        final Project project = getCurrentProject(newFile);

        if (null == project) return; // Skipped.


        final String oldContentHash = CodeRemark.createContentHash(
                VirtualFileUtils.getRelativePath(project, oldPath));
        final List<CodeRemark> codeRemarks = mCodeRemarkRepository.list(project.getName(), oldFileName, oldContentHash);

        if (codeRemarks.size() > 0) {

            WriteCommandAction.runWriteCommandAction(project, () -> {
                // Remove old coderemarks
                mCodeRemarkRepository.remove(project.getName(), oldFileName, oldContentHash);
                // Save new coderemarks
                final String newFileName = newFile.getName();
                final String newContentHash = CodeRemark.createContentHash(project, newFile);
                for (final CodeRemark codeRemark : codeRemarks) {
                    codeRemark.setFileName(newFileName);
                    codeRemark.setContentHash(newContentHash);
                }
                mCodeRemarkRepository.saveBatch(codeRemarks);
            });

        }
    }


    @SuppressWarnings("UnstableApiUsage")
    void afterPropertyChange(final VFilePropertyChangeEvent event) {
        if (event.isRename()) {
            moveCodeRemarks(event.getFile(), event.getOldPath(), (String) event.getOldValue());
        }
    }

    boolean needFilter(final VFileEvent event) {
        final VirtualFile file = event.getFile();
        if (null == file) return false;
        return !file.isDirectory();
    }
}
