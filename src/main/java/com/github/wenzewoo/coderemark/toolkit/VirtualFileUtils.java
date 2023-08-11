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

package com.github.wenzewoo.coderemark.toolkit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class VirtualFileUtils {

    public static String getAbstractPath(@NotNull final Project project) {
        return project.getBasePath();
    }

    public static String getRelativePath(@NotNull final Project project, final String canonicalPath) {
        if (StringUtils.isEmpty(canonicalPath)) return canonicalPath;
        final String projectBasePath = project.getBasePath();
        if (StringUtils.isEmpty(projectBasePath) || !canonicalPath.startsWith(projectBasePath)) return canonicalPath;
        return canonicalPath.substring(projectBasePath.length());
    }

    public static String getRelativePath(@NotNull final Project project, @NotNull final VirtualFile file) {
        return getRelativePath(project, file.getCanonicalPath());
    }

    public static byte[] getContentBytes(@NotNull final VirtualFile file) {
        try {
            return file.contentsToByteArray(true);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Get file contentsToByteArray error, file is " + file.getCanonicalPath() + ", " + e.getMessage());
        }
    }
}