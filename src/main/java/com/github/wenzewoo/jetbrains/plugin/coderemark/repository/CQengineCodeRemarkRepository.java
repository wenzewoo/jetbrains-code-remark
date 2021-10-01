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

package com.github.wenzewoo.jetbrains.plugin.coderemark.repository;

import cn.hutool.core.util.StrUtil;
import com.github.wenzewoo.jetbrains.plugin.coderemark.Utils;
import com.github.wenzewoo.jetbrains.plugin.coderemark.state.CodeRemarkLoadState;
import com.google.common.collect.Lists;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.googlecode.cqengine.query.QueryFactory.*;

public class CQengineCodeRemarkRepository implements CodeRemarkRepository {
    private final static IndexedCollection<CodeRemark> mCodeRemarks = new ConcurrentIndexedCollection<>();

    static {
        try {
            Files.walk(SerializationUtils.SAVE_PATH).forEach(file -> {
                final List<CodeRemark> codeRemarks = SerializationUtils.loadFromDisk(file.toFile());
                if (null != codeRemarks && codeRemarks.size() > 0) {
                    mCodeRemarks.addAll(codeRemarks); // First load, loading with local file.
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCodeRemarks.addIndex(HashIndex.onAttribute(CodeRemark.FILE_PATH));
        mCodeRemarks.addIndex(NavigableIndex.onAttribute(CodeRemark.LINE_NUMBER));
    }

    public static class SerializationUtils {
        private final static Path SAVE_PATH = Paths.get(System.getProperty("user.home"), ".code-remark");

        @SuppressWarnings("ResultOfMethodCallIgnored")
        public static void persistToDisk(String filePath) {
            final List<CodeRemark> fileCodeRemarks = mCodeRemarks.retrieve(
                    equal(CodeRemark.FILE_PATH, filePath)).stream().collect(Collectors.toList());

            final File file = Paths.get(
                    SAVE_PATH.toFile().getAbsolutePath(),
                    Utils.hashMD5(filePath) + ".bin").toFile();

            if (fileCodeRemarks.size() == 0 && file.exists())
                file.delete(); // if no more data, remove disk file.

            if (fileCodeRemarks.size() > 0) {
                if (file.exists())
                    file.delete(); // Remove old, write new.

                try (final ObjectOutputStream stream = new ObjectOutputStream(
                        new FileOutputStream(file))) {
                    stream.writeObject(fileCodeRemarks.toArray(new CodeRemark[0]));
                } catch (Throwable ignored) {
                }
            }
        }

        public static List<CodeRemark> loadFromDisk(File file) {
            if (!file.exists())
                return Lists.newArrayList();

            try (final ObjectInputStream stream = new ObjectInputStream(
                    new FileInputStream(file))) {
                return Arrays.asList((CodeRemark[]) stream.readObject());
            } catch (Throwable e) {
                return Lists.newArrayList();
            }
        }
    }


    @Override
    public List<Integer> lines(String filePath) {
        return mCodeRemarks.retrieve(equal(CodeRemark.FILE_PATH, filePath))
                .stream().map(CodeRemark::getLineNumber).collect(Collectors.toList());
    }

    @Override
    public Boolean exist(String filePath) {
        return mCodeRemarks.retrieve(equal(CodeRemark.FILE_PATH, filePath)).size() > 0;
    }

    @Override
    public Boolean exist(String filePath, int lineNumber) {
        return mCodeRemarks.retrieve(
                and(equal(CodeRemark.FILE_PATH, filePath),
                        equal(CodeRemark.LINE_NUMBER, lineNumber))).size() > 0;
    }

    @Override
    public String getSummary(String filePath, int lineNumber) {
        return mCodeRemarks.retrieve(
                        and(equal(CodeRemark.FILE_PATH, filePath),
                                equal(CodeRemark.LINE_NUMBER, lineNumber)))
                .stream().map(CodeRemark::getSummary).findFirst().orElse(null);
    }

    @Override
    public String getText(String filePath, int lineNumber) {
        return mCodeRemarks.retrieve(
                        and(equal(CodeRemark.FILE_PATH, filePath),
                                equal(CodeRemark.LINE_NUMBER, lineNumber)))
                .stream().map(CodeRemark::getText).findFirst().orElse(null);
    }

    @Override
    public void save(String filePath, int lineNumber, String text) {
        mCodeRemarks.add(new CodeRemark(filePath, lineNumber, text));
        SerializationUtils.persistToDisk(filePath); // To disk
        CodeRemarkLoadState.getInstance().resetLine(filePath).set(filePath, false);
    }

    @Override
    public void update(String filePath, int lineNumber, String text) {
        final CodeRemark codeRemark = mCodeRemarks.retrieve(
                        and(equal(CodeRemark.FILE_PATH, filePath),
                                equal(CodeRemark.LINE_NUMBER, lineNumber)))
                .stream().findFirst().orElse(null);

        if (null != codeRemark) {
            mCodeRemarks.remove(codeRemark);
            codeRemark.setText(text);
            mCodeRemarks.add(codeRemark);
            SerializationUtils.persistToDisk(filePath); // To disk
            CodeRemarkLoadState.getInstance().resetLine(filePath).set(filePath, false);
        }
    }

    @Override
    public void delete(String filePath, int lineNumber) {
        mCodeRemarks.retrieve(
                        and(equal(CodeRemark.FILE_PATH, filePath),
                                equal(CodeRemark.LINE_NUMBER, lineNumber)))
                .stream().findFirst().ifPresent(mCodeRemarks::remove);
        SerializationUtils.persistToDisk(filePath); // To disk
        CodeRemarkLoadState.getInstance().resetLine(filePath).resetPrevExtensionInfo(filePath).set(filePath, false);
    }

    @Override
    public void delete(String filePath) {
        mCodeRemarks.retrieve(equal(CodeRemark.FILE_PATH, filePath))
                .stream().filter(Objects::nonNull).forEach(mCodeRemarks::remove);
        SerializationUtils.persistToDisk(filePath); // To disk
        CodeRemarkLoadState.getInstance().resetLine(filePath).resetPrevExtensionInfo(filePath).set(filePath, false);
    }

    static class CodeRemark implements Serializable {
        private static final long serialVersionUID = 3586412079041650189L;
        private String filePath;
        private Integer lineNumber;
        private String text;

        public final static Attribute<CodeRemark, String> FILE_PATH = attribute("filePath", CodeRemark::getFilePath);
        public final static Attribute<CodeRemark, Integer> LINE_NUMBER = attribute("lineNumber", CodeRemark::getLineNumber);

        public CodeRemark() {
        }

        public CodeRemark(String filePath, Integer lineNumber, String text) {
            this.filePath = filePath;
            this.lineNumber = lineNumber;
            this.text = text;
        }

        public String getFilePath() {
            return filePath;
        }

        public CodeRemark setFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Integer getLineNumber() {
            return lineNumber;
        }

        public CodeRemark setLineNumber(Integer lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public String getSummary() {
            return StrUtil.maxLength(this.text, 17);
        }

        public String getText() {
            return text;
        }

        public CodeRemark setText(String text) {
            this.text = text;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CodeRemark.class.getSimpleName() + "{", "}")
                    .add("filePath='" + filePath + "'")
                    .add("lineNumber=" + lineNumber)
                    .add("summery='" + this.getSummary() + "'")
                    .add("text='" + text + "'")
                    .toString();
        }
    }
}
