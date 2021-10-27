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
    public final static Attribute<CodeRemark, String> PROJECT_NAME = attribute("projectName", CodeRemark::getProjectName);
    public final static Attribute<CodeRemark, String> CONTENT_HASH = attribute("contentHash", CodeRemark::getContentHash);
    public final static Attribute<CodeRemark, String> FILE_NAME = attribute("fileName", CodeRemark::getFileName);
    public final static Attribute<CodeRemark, Integer> LINE_NUMBER = attribute("lineNumber", CodeRemark::getLineNumber);

    static {
        mCodeRemarks.addIndex(HashIndex.onAttribute(PROJECT_NAME));
        mCodeRemarks.addIndex(HashIndex.onAttribute(CONTENT_HASH));
        mCodeRemarks.addIndex(HashIndex.onAttribute(FILE_NAME));
        mCodeRemarks.addIndex(NavigableIndex.onAttribute(LINE_NUMBER));
    }

    @Override
    public List<CodeRemark> list(@NotNull final String projectName) {
        return mCodeRemarks.retrieve(equal(PROJECT_NAME, projectName)).stream()
                .sorted(Comparator.comparing(CodeRemark::getFileName).thenComparing(CodeRemark::getLineNumber)).collect(Collectors.toList());
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
    public CodeRemark get(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        return mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, projectName),
                        equal(FILE_NAME, fileName),
                        equal(CONTENT_HASH, contentHash),
                        equal(LINE_NUMBER, lineNumber))).stream().findFirst().orElse(null);
    }

    @Override
    public void save(final CodeRemark codeRemark) {
        removeIfExists(codeRemark.getProjectName(), codeRemark.getFileName(), codeRemark.getContentHash(), codeRemark.getLineNumber());
        mCodeRemarks.add(codeRemark);
    }

    void removeIfExists(final String projectName, final String fileName, final String contentHash, final int lineNumber) {
        mCodeRemarks.retrieve(
                and(equal(PROJECT_NAME, projectName),
                        equal(FILE_NAME, fileName),
                        equal(CONTENT_HASH, contentHash),
                        equal(LINE_NUMBER, lineNumber)))
                .stream().findFirst().ifPresent(mCodeRemarks::remove);
    }

    @Override
    public void remove(@NotNull final String projectName, @NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        removeIfExists(projectName, fileName, contentHash, lineNumber);
    }
}
