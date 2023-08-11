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
package com.github.wenzewoo.coderemark.repository.persistent;


import com.github.wenzewoo.coderemark.CodeRemark;
import com.github.wenzewoo.coderemark.repository.CodeRemarkRepository;
import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@State(
        name = "CodeRemarkPersistentStateRepository",
        storages = {@Storage("./code-remark.xml")}
)
public class CodeRemarkPersistentStateRepository
        implements CodeRemarkRepository, PersistentStateComponent<CodeRemarkPersistentState> {
    private final CodeRemarkPersistentState state = new CodeRemarkPersistentState("1.4.0");

    @Override
    public @Nullable CodeRemarkPersistentState getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull CodeRemarkPersistentState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    @Override
    public List<CodeRemark> list() {
        return this.stateStream().sorted(this.stateComparator()).collect(Collectors.toList());
    }

    private Stream<CodeRemark> stateStream() {
        if (this.state.getProjectCodeRemarks().size() > 512) {
            return this.state.getProjectCodeRemarks().parallelStream();
        }
        return this.state.getProjectCodeRemarks().stream();
    }

    private Comparator<CodeRemark> stateComparator() {
        return Comparator.comparing(CodeRemark::getFileName).thenComparing(CodeRemark::getLineNumber);
    }

    private Predicate<CodeRemark> stateFilter(@NotNull CodeRemark codeRemark) {
        return this.stateFilter(codeRemark.getFileName(), codeRemark.getContentHash(), codeRemark.getLineNumber());
    }

    private Predicate<CodeRemark> stateFilter(String fileName, String contentHash, Integer lineNumber) {
        return (codeRemark) -> {
            final boolean fileNameMatch = StringUtils.isEmpty(fileName) || StringUtils.equals(fileName, codeRemark.getFileName());
            final boolean contentHashMatch = StringUtils.isEmpty(contentHash) || StringUtils.equals(contentHash, codeRemark.getContentHash());
            final boolean lineNumberMatch = null == lineNumber || Objects.equals(lineNumber, codeRemark.getLineNumber());
            return fileNameMatch && contentHashMatch && lineNumberMatch;
        };
    }


    @Override
    public List<CodeRemark> list(@NotNull final String fileName, @NotNull final String contentHash) {
        final Predicate<CodeRemark> stateFilter = this.stateFilter(fileName, contentHash, null);
        return this.stateStream().filter(stateFilter).sorted(this.stateComparator()).collect(Collectors.toList());
    }

    @Override
    public boolean exists(@NotNull final String fileName, @NotNull final String contentHash) {
        final Predicate<CodeRemark> stateFilter = this.stateFilter(fileName, contentHash, null);
        return this.stateStream().anyMatch(stateFilter);
    }

    @Override
    public CodeRemark get(@NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        final Predicate<CodeRemark> stateFilter = this.stateFilter(fileName, contentHash, lineNumber);
        return this.stateStream().filter(stateFilter).findFirst().orElse(null);
    }

    @Override
    public void save(final CodeRemark codeRemark) {
        this.removeWith(codeRemark);
        this.state.getProjectCodeRemarks().add(codeRemark);
    }

    @Override
    public void saveBatch(final List<CodeRemark> codeRemarks) {
        for (final CodeRemark codeRemark : codeRemarks) {
            this.removeWith(codeRemark);
        }
        this.state.getProjectCodeRemarks().addAll(codeRemarks);
    }

    void removeWith(final CodeRemark codeRemark) {
        final Predicate<CodeRemark> stateFilter = this.stateFilter(codeRemark);
        this.state.getProjectCodeRemarks().removeIf(stateFilter);
    }

    @Override
    public void remove(@NotNull final String fileName, @NotNull final String contentHash, final int lineNumber) {
        final Predicate<CodeRemark> stateFilter = this.stateFilter(fileName, contentHash, lineNumber);
        this.state.getProjectCodeRemarks().removeIf(stateFilter);
    }

    @Override
    public void remove(@NotNull final String fileName, @NotNull final String contentHash) {
        final Predicate<CodeRemark> stateFilter = this.stateFilter(fileName, contentHash, null);
        this.state.getProjectCodeRemarks().removeIf(stateFilter);
    }
}