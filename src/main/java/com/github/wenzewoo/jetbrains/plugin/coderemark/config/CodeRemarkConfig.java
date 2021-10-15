package com.github.wenzewoo.jetbrains.plugin.coderemark.config;

import java.awt.*;

@SuppressWarnings("UseJBColor")
public class CodeRemarkConfig {

    private String prefix = "//[MARK]: ";
    private String prefixColor = "255,153,0,255";
    private String bodyColor = "255,255,141,218";


    public String getPrefix() {
        return prefix;
    }

    public CodeRemarkConfig setPrefix(final String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getPrefixColor() {
        return prefixColor;
    }

    public CodeRemarkConfig setPrefixColor(final String prefixColor) {
        this.prefixColor = prefixColor;
        return this;
    }

    public String getBodyColor() {
        return bodyColor;
    }

    public CodeRemarkConfig setBodyColor(final String bodyColor) {
        this.bodyColor = bodyColor;
        return this;
    }

    public static Color asColor(final String color) {
        final String[] rgb = color.split(",");
        return new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]), Integer.parseInt(rgb[3]));
    }

    public static String byColor(final Color color) {
        return String.format("%d,%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}
