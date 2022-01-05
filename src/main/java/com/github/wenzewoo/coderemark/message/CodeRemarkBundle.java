package com.github.wenzewoo.coderemark.message;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

public class CodeRemarkBundle {

    private final static String mBundlePath = "messages.CodeRemarkBundle";
    private final static AbstractBundle mBundle = new BundleWrapper(mBundlePath);
    public static String message(@PropertyKey(resourceBundle = mBundlePath) String key, Object... params) {
        return mBundle.getMessage(key, params);
    }
}
