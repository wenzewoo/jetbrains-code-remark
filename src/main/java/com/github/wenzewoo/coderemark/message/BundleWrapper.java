package com.github.wenzewoo.coderemark.message;


import com.intellij.AbstractBundle;
import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public class BundleWrapper extends AbstractBundle {

    private Locale locale;
    private final String bundle;
    private final ClassLoader classLoader;
    private final ResourceBundle.Control control;

    public BundleWrapper(@NonNls @NotNull String pathToBundle) {
        super(pathToBundle);
        try {
            //noinspection MissingRecentApi
            locale = DynamicBundle.getLocale();
        } catch (Throwable e) {
            locale = Locale.ENGLISH;
        }
        this.bundle = pathToBundle;
        this.classLoader = this.getClass().getClassLoader();
        this.control = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
    }

    @Override
    protected ResourceBundle findBundle(
            @NotNull String pathToBundle, @NotNull ClassLoader loader, @NotNull ResourceBundle.Control control) {
        if (null != locale)
            return ResourceBundle.getBundle(pathToBundle, locale, loader, control);
        return super.findBundle(pathToBundle, loader, control);
    }

    @Override
    public @NotNull
    @Nls String getMessage(@NotNull @NonNls String key, @NotNull Object... params) {
        if (null != locale)
            return message(ResourceBundle.getBundle(bundle, locale, classLoader, control), key, params);
        return super.getMessage(key, params);
    }
}
