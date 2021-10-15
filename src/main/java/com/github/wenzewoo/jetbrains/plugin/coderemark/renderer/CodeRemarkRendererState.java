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

package com.github.wenzewoo.jetbrains.plugin.coderemark.renderer;

import com.github.wenzewoo.jetbrains.plugin.coderemark.Utils;
import com.intellij.openapi.editor.LineExtensionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnusedReturnValue")
public class CodeRemarkRendererState {
    public final static Map<String, Boolean> mStateMap = new ConcurrentHashMap<>();
    public final static Map<String, Integer> mLoadedLineCountMap = new ConcurrentHashMap<>();
    public final static Map<String, List<LineExtensionInfo>> mPrevExtensionInfoMap = new ConcurrentHashMap<>();

    public synchronized CodeRemarkRendererState appendPrevExtensionInfo(
            final String filePath, final Integer lineNumber, final List<LineExtensionInfo> extensionInfo) {
        final String key = filePath + "#" + lineNumber;
        final List<LineExtensionInfo> extensionInfoList = mPrevExtensionInfoMap
                .getOrDefault(key, new ArrayList<>());
        if (extensionInfoList.size() > 0 && extensionInfo.size() > 0)
            extensionInfoList.clear(); // There is only one piece of data per row
        extensionInfoList.addAll(extensionInfo);
        mPrevExtensionInfoMap.put(key, extensionInfoList);
        return this;
    }

    public synchronized List<LineExtensionInfo> getPrevExtensionInfo(final String filePath, final Integer lineNumber) {
        return mPrevExtensionInfoMap.get(filePath + "#" + lineNumber);
    }

    public synchronized CodeRemarkRendererState resetPrevExtensionInfo(final String filePath) {
        for (final String key : mPrevExtensionInfoMap.keySet()) {
            if (Utils.startsWith(key, filePath))
                mPrevExtensionInfoMap.remove(key);
        }
        return this;
    }

    public synchronized CodeRemarkRendererState incrementLine(final String filePath) {
        mLoadedLineCountMap.put(filePath, mLoadedLineCountMap.getOrDefault(filePath, 0) + 1);
        return this;
    }

    public synchronized CodeRemarkRendererState resetLine(final String filePath) {
        mLoadedLineCountMap.remove(filePath);
        return this;
    }

    public synchronized int loadedLineCount(final String filePath) {
        return mLoadedLineCountMap.getOrDefault(filePath, 0);
    }


    public synchronized CodeRemarkRendererState set(final String filePath, final Boolean state) {
        mStateMap.put(filePath, state);
        return this;
    }

    public synchronized Boolean get(final String filePath) {
        return mStateMap.getOrDefault(filePath, false);
    }

    public static CodeRemarkRendererState getInstance() {
        return Singleton.INSTANCE.getInstance();
    }

    private enum Singleton {
        INSTANCE;
        private final CodeRemarkRendererState instance;

        Singleton() {
            instance = new CodeRemarkRendererState();
        }

        public CodeRemarkRendererState getInstance() {
            return instance;
        }
    }
}
