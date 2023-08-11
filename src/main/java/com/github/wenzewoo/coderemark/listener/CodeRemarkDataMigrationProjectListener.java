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
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepositoryFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Data migration from 1.3.6.4-SNAPSHOT(<=) to 1.4.
 */
public class CodeRemarkDataMigrationProjectListener implements ProjectManagerListener {
    private final static Path OLD_PERSISTENT_BASE_DIR = Paths.get(System.getProperty("user.home"), ".code-remark");

    @Override
    public void projectOpened(@NotNull Project project) {
        final Path oldPersistentDir = Paths.get(//
                OLD_PERSISTENT_BASE_DIR.toFile().getAbsolutePath(), project.getName());
        if (!Files.exists(oldPersistentDir)) {
            return;
        }

        // Load from old persistent dir.
        System.out.printf("DataMigration: %s\n", oldPersistentDir.toFile().getAbsolutePath());
        try (final Stream<Path> walked = Files.walk(oldPersistentDir)) {
            // Load with local file.
            final List<CodeRemark> codeRemarks = new ArrayList<>();
            walked.forEach(e -> {
                final File localFile = e.toFile();
                if (!localFile.exists() || localFile.isDirectory()) {
                    return;
                }

                try (final ObjectInputStream stream = new ObjectInputStream(
                        new FileInputStream(localFile))) {
                    codeRemarks.addAll(Arrays.asList((CodeRemark[]) stream.readObject()));
                } catch (final Throwable e1) {
                    System.out.printf("DataMigration Deserialization Error: %s\n", e1.getMessage());
                }
            });
            if (!codeRemarks.isEmpty()) {
                CodeRemarkRepositoryFactory.getInstance(project).saveBatch(codeRemarks);
            }
            // Delete old persistent dir.
            // Files.deleteIfExists(oldPersistentDir);
            System.out.printf("DataMigration Successful: %s, %d\n", project.getName(), codeRemarks.size());
        } catch (IOException e) {
            System.out.printf("DataMigration Error: %s\n", e.getMessage());
        }
    }
}