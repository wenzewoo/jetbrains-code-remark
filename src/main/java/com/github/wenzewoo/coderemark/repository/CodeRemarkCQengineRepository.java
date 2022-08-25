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
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.googlecode.cqengine.query.QueryFactory.*;

public class CodeRemarkCQengineRepository implements CodeRemarkRepository {
    protected final static IndexedCollection<CodeRemark> mCodeRemarks = new ConcurrentIndexedCollection<>();
    public final static Attribute<CodeRemark, String> PROJECT_NAME = attribute(CodeRemark.class, String.class,
            "projectName", CodeRemark::getProjectName);// 规避在JDK14+出现的异常 https://github.com/npgall/cqengine/issues/269
    public final static Attribute<CodeRemark, String> CONTENT_HASH = attribute(CodeRemark.class, String.class,
            "contentHash", CodeRemark::getContentHash);
    public final static Attribute<CodeRemark, String> FILE_NAME = attribute(CodeRemark.class, String.class,
            "fileName", CodeRemark::getFileName);
    public final static Attribute<CodeRemark, Integer> LINE_NUMBER = attribute(CodeRemark.class, Integer.class,
            "lineNumber", CodeRemark::getLineNumber);

    static {
        mCodeRemarks.addIndex(HashIndex.onAttribute(PROJECT_NAME));
        mCodeRemarks.addIndex(HashIndex.onAttribute(CONTENT_HASH));
        mCodeRemarks.addIndex(HashIndex.onAttribute(FILE_NAME));
        mCodeRemarks.addIndex(NavigableIndex.onAttribute(LINE_NUMBER));
    }

    @Override
    public List<CodeRemark> list(@NotNull final String projectName) {
        return mCodeRemarks.retrieve(equal(PROJECT_NAME, projectName)).stream()
                .sorted(Comparator.comparing(CodeRemark::getFileName).thenComparing(CodeRemark::getLineNumber))
                .collect(Collectors.toList());
    }


    @Override
    public List<CodeRemark> list(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash) {
        return mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, projectName),
                        equal(FILE_NAME, fileName),
                        equal(CONTENT_HASH, contentHash)))
                .stream()
                .sorted(Comparator.comparing(CodeRemark::getLineNumber))
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash) {
        return mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, projectName),
                        equal(FILE_NAME, fileName),
                        equal(CONTENT_HASH, contentHash))).size() > 0;
    }

    @Override
    public CodeRemark get(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        return mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, projectName),
                        equal(FILE_NAME, fileName),
                        equal(CONTENT_HASH, contentHash),
                        equal(LINE_NUMBER, lineNumber)))
                .stream().findFirst().orElse(null);
    }

    @Override
    public void save(final CodeRemark codeRemark) {
        removeWith(codeRemark);
        mCodeRemarks.add(codeRemark);
    }

    @Override
    public void saveBatch(final List<CodeRemark> codeRemarks) {
        for (final CodeRemark codeRemark : codeRemarks)
            removeWith(codeRemark);
        mCodeRemarks.addAll(codeRemarks);
    }

    void removeWith(final CodeRemark codeRemark) {
        mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, codeRemark.getProjectName()),
                        equal(FILE_NAME, codeRemark.getFileName()),
                        equal(CONTENT_HASH, codeRemark.getContentHash()),
                        equal(LINE_NUMBER, codeRemark.getLineNumber())))
                .stream().forEach(mCodeRemarks::remove);
    }

    @Override
    public void remove(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, projectName),
                        equal(FILE_NAME, fileName),
                        equal(CONTENT_HASH, contentHash),
                        equal(LINE_NUMBER, lineNumber)))
                .stream().forEach(mCodeRemarks::remove);
    }

    @Override
    public void remove(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash) {
        mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, projectName),
                        equal(FILE_NAME, fileName),
                        equal(CONTENT_HASH, contentHash)))
                .stream().forEach(mCodeRemarks::remove);
    }
}
