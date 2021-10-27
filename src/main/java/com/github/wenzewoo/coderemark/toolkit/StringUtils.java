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

package com.github.wenzewoo.coderemark.toolkit;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

public class StringUtils {

    public static boolean isEmpty(final String text) {
        return null == text || "".equals(text.trim());
    }

    public static boolean isNotEmpty(final String text) {
        return !isEmpty(text);
    }

    public static String format(final String text, final Object... args) {
        return MessageFormat.format(text, args);
    }

    public static boolean equals(@Nullable final String val1, @Nullable final String val2) {
        return StringUtil.equals(val1, val2);
    }

    public static String maxLength(@Nullable final String val, final int maxLength) {
        if (isEmpty(val)) return val;

        if (val.length() > maxLength)
            return val.substring(0, maxLength - 1) + "...";
        return val;
    }
}
