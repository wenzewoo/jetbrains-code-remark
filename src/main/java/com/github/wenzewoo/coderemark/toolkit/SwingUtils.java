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

import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class SwingUtils {
    private final static AffineTransform AFFINE_TRANSFORM = new AffineTransform();
    private final static FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(AFFINE_TRANSFORM, true, true);


    @Nullable
    public static Rectangle2D getTextRectangle(@Nullable final String text, @NotNull final Font font) {
        if (StringUtils.isEmpty(text)) return null;

        return font.getStringBounds(text, FONT_RENDER_CONTEXT);
    }

    public static JBDimension createDimension(@Nullable final String text, @NotNull final Font font, final int minW, final int maxW, final int minH, final int maxH) {
        final Rectangle2D rectangle = getTextRectangle(text, font);
        if (null == rectangle)
            return new JBDimension(minW, minH);

        int width = minW;
        if (!(rectangle.getWidth() < minW)) {
            if (rectangle.getWidth() > maxW)
                width = maxW;
            else width = (int) rectangle.getWidth();
        }

        int height = minH;
        if (!(rectangle.getHeight() < minH)) {
            if (rectangle.getHeight() > maxH)
                height = maxH;
            else height = (int) rectangle.getHeight();
        }

        return new JBDimension(width, height);
    }
}
