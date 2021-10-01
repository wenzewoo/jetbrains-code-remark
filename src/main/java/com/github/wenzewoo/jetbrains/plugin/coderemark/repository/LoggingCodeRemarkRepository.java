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

import java.util.List;

public class LoggingCodeRemarkRepository implements CodeRemarkRepository {
    private final CodeRemarkRepository delegate;

    public LoggingCodeRemarkRepository(CodeRemarkRepository delegate) {
        this.delegate = delegate;
    }

    static class Cost implements AutoCloseable {
        private final long start;
        private final String method;
        private final Object[] params;

        public Cost(String method, Object... params) {
            this.method = method;
            this.params = params;
            this.start = System.currentTimeMillis();
        }

        @Override
        public void close() {
//            final String prefix = String.format("Call %s(%s) consume ", method, Arrays.toString(params));
//            System.out.printf("%s: %d ms\n", prefix, (System.currentTimeMillis() - start));
        }
    }

    @Override
    public List<Integer> lines(String filePath) {
        try (final Cost ignored = new Cost("lines", filePath)) {
            return this.delegate.lines(filePath);
        }
    }

    @Override
    public Boolean exist(String filePath) {
        try (final Cost ignored = new Cost("exist", filePath)) {
            return this.delegate.exist(filePath);
        }
    }

    @Override
    public Boolean exist(String filePath, int lineNumber) {
        try (final Cost ignored = new Cost("exist", filePath, lineNumber)) {
            return this.delegate.exist(filePath, lineNumber);
        }
    }

    @Override
    public String getSummary(String filePath, int lineNumber) {
        try (final Cost ignored = new Cost("getSummary", filePath, lineNumber)) {
            return this.delegate.getSummary(filePath, lineNumber);
        }
    }

    @Override
    public String getText(String filePath, int lineNumber) {
        try (final Cost ignored = new Cost("getText", filePath, lineNumber)) {
            return this.delegate.getText(filePath, lineNumber);
        }
    }

    @Override
    public void save(String filePath, int lineNumber, String text) {
        try (final Cost ignored = new Cost("save", filePath, lineNumber, text)) {
            this.delegate.save(filePath, lineNumber, text);
        }
    }

    @Override
    public void update(String filePath, int lineNumber, String text) {
        try (final Cost ignored = new Cost("update", filePath, lineNumber, text)) {
            this.delegate.update(filePath, lineNumber, text);
        }
    }

    @Override
    public void delete(String filePath, int lineNumber) {
        try (final Cost ignored = new Cost("delete", filePath, lineNumber)) {
            this.delegate.delete(filePath, lineNumber);
        }
    }

    @Override
    public void delete(String filePath) {
        try (final Cost ignored = new Cost("delete", filePath)) {
            this.delegate.delete(filePath);
        }
    }
}
