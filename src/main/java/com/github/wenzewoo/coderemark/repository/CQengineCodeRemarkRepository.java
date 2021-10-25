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

import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.googlecode.cqengine.query.QueryFactory.*;

public class CQengineCodeRemarkRepository implements CodeRemarkRepository {
    private final static IndexedCollection<CodeRemark> mCodeRemarks = new ConcurrentIndexedCollection<>();
    public final static Attribute<CodeRemark, String> FILE_PATH = attribute("filePath", CodeRemark::getFilePath);
    public final static Attribute<CodeRemark, Integer> LINE_NUMBER = attribute("lineNumber", CodeRemark::getLineNumber);

    static {
        try {
            Files.walk(SerializationUtils.SAVE_PATH).forEach(file -> {
                final List<CodeRemark> codeRemarks = SerializationUtils.loadFromDisk(file.toFile());
                if (codeRemarks.size() > 0)
                    mCodeRemarks.addAll(codeRemarks); // First load, loading with local file.
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
        mCodeRemarks.addIndex(HashIndex.onAttribute(FILE_PATH));
        mCodeRemarks.addIndex(NavigableIndex.onAttribute(LINE_NUMBER));
    }

    public static class SerializationUtils {
        private final static Path SAVE_PATH = Paths.get(System.getProperty("user.home"), ".code-remark");

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public static void persistToDisk(final String filePath) {
            final File saveFolder = SAVE_PATH.toFile();

            if (!saveFolder.exists())
                saveFolder.mkdirs(); // if saveFolder not exists, mkdir it.

            final List<CodeRemark> fileCodeRemarks = mCodeRemarks.retrieve(
                    equal(FILE_PATH, filePath)).stream().collect(Collectors.toList());

            final File file = Paths.get(
                    saveFolder.getAbsolutePath(), StringUtils.hashMD5(filePath) + ".bin").toFile();

            if (fileCodeRemarks.size() == 0 && file.exists())
                file.delete(); // if no more data, remove disk file.

            if (fileCodeRemarks.size() > 0) {
                if (file.exists())
                    file.delete(); // Remove old, write new.

                try (final ObjectOutputStream stream = new ObjectOutputStream(
                        new FileOutputStream(file))) {
                    stream.writeObject(fileCodeRemarks.toArray(new CodeRemark[0]));
                } catch (final Throwable ignored) {
                }
            }
        }

        public static List<CodeRemark> loadFromDisk(final File file) {
            if (!file.exists())
                return new ArrayList<>();

            try (final ObjectInputStream stream = new ObjectInputStream(
                    new FileInputStream(file))) {
                return Arrays.asList((CodeRemark[]) stream.readObject());
            } catch (final Throwable e) {
                return new ArrayList<>();
            }
        }
    }

    @Override
    public boolean exists(final String filePah) {
        return mCodeRemarks.retrieve(equal(FILE_PATH, filePah)).size() > 0;
    }

    @Override
    public boolean exists(final String filePah, final int lineNumber) {
        return mCodeRemarks.retrieve(and(equal(FILE_PATH, filePah), equal(LINE_NUMBER, lineNumber))).size() > 0;
    }

    @Override
    public CodeRemark get(final String filePath, final int lineNumber) {
        return mCodeRemarks.retrieve(and(equal(FILE_PATH, filePath), equal(LINE_NUMBER, lineNumber))).stream().findFirst().orElse(null);
    }

    @Override
    public List<CodeRemark> list(final String filePath) {
        return mCodeRemarks.retrieve(equal(FILE_PATH, filePath)).stream().collect(Collectors.toList());
    }

    @Override
    public void save(final CodeRemark remark) {
        removeIfExists(remark.getFilePath(), remark.getLineNumber());
        mCodeRemarks.add(remark);
        SerializationUtils.persistToDisk(remark.getFilePath());
    }

    void removeIfExists(final String filePath, final int lineNumber) {
        mCodeRemarks.retrieve(
                and(equal(FILE_PATH, filePath),
                        equal(LINE_NUMBER, lineNumber)))
                .stream().findFirst().ifPresent(mCodeRemarks::remove);
    }

    @Override
    public void remove(final String filePath, final int lineNumber) {
        removeIfExists(filePath, lineNumber);
        SerializationUtils.persistToDisk(filePath);
    }
}
