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

package com.github.wenzewoo.coderemark.repository;

import com.github.wenzewoo.coderemark.CodeRemark;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CodeRemarkRepository {

    default List<CodeRemark> list(@NotNull final Project project) {
        return list(project.getName());
    }

    List<CodeRemark> list(@NotNull String projectName);


    default List<CodeRemark> list(@NotNull final Project project, @NotNull final VirtualFile file) {
        return list(project.getName(), file.getName(), CodeRemark.createContentHash(project, file));
    }

    List<CodeRemark> list(@NotNull String projectName, @NotNull String fileName, @NotNull String contentHash);


    default boolean exists(@NotNull final Project project, @NotNull final VirtualFile file) {
        return exists(project.getName(), file.getName(), CodeRemark.createContentHash(project, file));
    }

    boolean exists(@NotNull String projectName, @NotNull String fileName, @NotNull String contentHash);


    default CodeRemark get(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {
        return get(project.getName(), file.getName(), CodeRemark.createContentHash(project, file), lineNumber);
    }

    CodeRemark get(@NotNull String projectName, @NotNull String fileName, @NotNull String contentHash, int lineNumber);

    void save(CodeRemark codeRemark);

    void saveBatch(List<CodeRemark> codeRemarks);


    default void remove(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {
        remove(project.getName(), file.getName(), CodeRemark.createContentHash(project, file), lineNumber);
    }

    void remove(@NotNull String projectName, @NotNull String fileName, @NotNull String contentHash, int lineNumber);

    
    default void remove(@NotNull Project project, @NotNull VirtualFile file) {
        remove(project.getName(), file.getName(), CodeRemark.createContentHash(project, file));
    }

    void remove(@NotNull String projectName, @NotNull String fileName, @NotNull String contentHash);
}