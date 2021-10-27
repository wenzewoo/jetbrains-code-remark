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
import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeRemarkSerializableRepositoryWrapper implements CodeRemarkRepository {
    private final static Path mBasePath = Paths.get(System.getProperty("user.home"), ".code-remark");

    private boolean initialized = false;
    private final CodeRemarkCQengineRepository delegate;

    public CodeRemarkSerializableRepositoryWrapper(final CodeRemarkCQengineRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<CodeRemark> list(@NotNull final String projectName) {
        initialize(projectName);
        return delegate.list(projectName);
    }

    @Override
    public List<CodeRemark> list(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash) {
        initialize(projectName);
        return delegate.list(projectName, fileName, contentHash);
    }

    @Override
    public CodeRemark get(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        initialize(projectName);
        return delegate.get(projectName, fileName, contentHash, lineNumber);
    }

    @Override
    public void save(final CodeRemark codeRemark) {
        delegate.save(codeRemark);
        persistToDisk(codeRemark.getProjectName(), codeRemark.getFileName(), codeRemark.getContentHash());
    }

    @Override
    public void remove(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        delegate.remove(projectName, fileName, contentHash, lineNumber);
        persistToDisk(projectName, fileName, contentHash);
    }

    // TODO: Optimize performance.
    void initialize(@NotNull final String projectName) {
        if (!initialized) {
            try {
                // ~/.code-remark/{projectName}
                Files.walk(Paths.get(mBasePath.toFile().getAbsolutePath(), projectName)).forEach(e -> {
                    final File localFile = e.toFile();
                    if (localFile.exists()) {
                        List<CodeRemark> codeRemarks = new ArrayList<>();
                        try (final ObjectInputStream stream = new ObjectInputStream(
                                new FileInputStream(localFile))) {
                            codeRemarks = Arrays.asList((CodeRemark[]) stream.readObject());
                        } catch (final Throwable ignored) {
                        }

                        if (!codeRemarks.isEmpty())
                            CodeRemarkCQengineRepository.mCodeRemarks.addAll(codeRemarks);
                    }
                });
            } catch (final IOException ignored) {
            } finally {
                initialized = true;
            }
        }
    }


    // TODO: Optimize performance.
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void persistToDisk(final String projectName, final String fileName, final String contentHash) {
        final File baseDir = mBasePath.toFile();
        if (!baseDir.exists())
            baseDir.mkdirs(); // if baseDir not exists, mkdir it.

        final File saveDir = Paths.get(baseDir.getAbsolutePath(), projectName).toFile();
        if (!saveDir.exists())
            saveDir.mkdirs(); // if saveDir not exists, mkdir it.

        // ~/.code-remark/{projectName}/hashMD5.XXX.java
        final File localFile = Paths.get(saveDir.getAbsolutePath(),
                StringUtils.format("{0}.{1}", contentHash, fileName)).toFile();
        final List<CodeRemark> codeRemarks = list(projectName, fileName, contentHash);

        if (codeRemarks.size() == 0 && localFile.exists())
            localFile.delete(); // if not more data, remove disk file.

        if (codeRemarks.size() > 0) {
            if (localFile.exists())
                localFile.delete(); // Remove old, write new.

            try (final ObjectOutputStream stream = new ObjectOutputStream(
                    new FileOutputStream(localFile))) {
                stream.writeObject(codeRemarks.toArray(new CodeRemark[0]));
            } catch (final IOException ignored) {
            }
        }
    }
}
