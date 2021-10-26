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

package com.github.wenzewoo.coderemark.renderer;

import com.github.wenzewoo.coderemark.action.toolbar.RemoveRemarkPopupToolbarAction;
import com.github.wenzewoo.coderemark.action.toolbar.SaveRemarkPopupToolbarAction;
import com.github.wenzewoo.coderemark.toolkit.PopupUtils;
import com.github.wenzewoo.coderemark.toolkit.StringUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.paint.EffectPainter;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


@SuppressWarnings("rawtypes")
public class CodeRemarkEditorInlineInlayRenderer
        implements EditorCustomElementRenderer {

    private final static int RENDERER_TEXT_MAX_LENGTH = 20;
    private final static Icon PREFIX_ICON = AllIcons.General.BalloonInformation;
    private final static Icon HOVERED_SUFFIX_ICON = AllIcons.General.LinkDropTriangle;


    private final String text;
    private boolean isHovered = false;
    private int textStartXCoordinate = -1;

    public CodeRemarkEditorInlineInlayRenderer(@NotNull final String text) {
        this.text = text;
    }


    @Override
    public int calcWidthInPixels(@NotNull final Inlay inlay) {
        final Editor editor = inlay.getEditor();
        final Font font = getFont(editor);
        final FontMetrics metrics = getFontMetrics(font, editor);
        return metrics.stringWidth(getPreviewText()) + PREFIX_ICON.getIconWidth() + (isHovered ? HOVERED_SUFFIX_ICON.getIconWidth() : 0);
    }


    @Override
    public void paint(@NotNull final Inlay inlay, @NotNull final Graphics graphics, @NotNull final Rectangle rectangle, @NotNull final TextAttributes textAttributes) {
        if (StringUtils.isEmpty(text)) return;

        final EditorImpl editor = (EditorImpl) inlay.getEditor();
        final TextAttributes inlineAttributes = getAttributes(editor);
        if (inlineAttributes == null || inlineAttributes.getForegroundColor() == null) return;

        final Font font = getFont(editor);
        graphics.setFont(font);
        final FontMetrics metrics = getFontMetrics(font, editor);

        int curX = rectangle.x; // int curX = rectangle.x + metrics.charWidth(' ');
        textStartXCoordinate = curX;
        final int margin = metrics.charWidth(' ') / 4;

        // draw icon
        // curX += (2 * margin);
        PREFIX_ICON.paintIcon(inlay.getEditor().getComponent(), graphics, curX, getIconY(PREFIX_ICON, rectangle));
        curX += PREFIX_ICON.getIconWidth() + margin * 2;

        // draw text
        final String previewText = getPreviewText();
        graphics.setColor(inlineAttributes.getForegroundColor());
        graphics.drawString(previewText, curX, rectangle.y + inlay.getEditor().getAscent());
        curX += metrics.stringWidth(previewText);

        // draw hovered icon
        if (isHovered) {
            final Icon icon = AllIcons.General.LinkDropTriangle;
            icon.paintIcon(inlay.getEditor().getComponent(), graphics, curX, getIconY(icon, rectangle));
        }
        paintEffects(graphics, rectangle, editor, inlineAttributes, font, metrics);
    }

    public void onMouseClicked(@NotNull final Inlay inlay, @NotNull final EditorMouseEvent event) {
        PopupUtils.createCodeRemarkEditor(event.getEditor(), "Edit remark", text,
                new SaveRemarkPopupToolbarAction(), new RemoveRemarkPopupToolbarAction())
                .showInBestPositionFor(event.getEditor());
    }

    public void onMouseMoved(@NotNull final Inlay inlay, @NotNull final EditorMouseEvent event) {
        setHovered(event.getMouseEvent().getX() >= textStartXCoordinate, inlay, event.getEditor());
    }

    public void onMouseExited(@NotNull final Inlay inlay, @NotNull final EditorMouseEvent event) {
        setHovered(false, inlay, event.getEditor());
    }


    private void setHovered(final boolean active, @NotNull final Inlay inlay, @NotNull final Editor editor) {
        if (editor instanceof EditorEx) {
            final boolean oldState = isHovered;
            final Cursor cursor = active ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null;
            ((EditorEx) editor).setCustomCursor(CodeRemarkEditorInlineInlayRenderer.class, cursor);
            isHovered = active;

            if (oldState != active)
                inlay.update();
        }
    }


    private String getPreviewText() {
        String previewText = text;
        if (previewText.length() > RENDERER_TEXT_MAX_LENGTH)
            previewText = previewText.substring(0, RENDERER_TEXT_MAX_LENGTH) + "...";

        return previewText;
    }

    private static void paintEffects(@NotNull final Graphics g,
                                     @NotNull final Rectangle r,
                                     final EditorImpl editor,
                                     final TextAttributes inlineAttributes,
                                     final Font font,
                                     final FontMetrics metrics) {
        final Color effectColor = inlineAttributes.getEffectColor();
        final EffectType effectType = inlineAttributes.getEffectType();
        if (effectColor != null) {
            g.setColor(effectColor);
            final Graphics2D g2d = (Graphics2D) g;
            final int xStart = r.x;
            final int xEnd = r.x + r.width;
            final int y = r.y + metrics.getAscent();
            if (effectType == EffectType.LINE_UNDERSCORE) {
                EffectPainter.LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            } else if (effectType == EffectType.BOLD_LINE_UNDERSCORE) {
                EffectPainter.BOLD_LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            } else if (effectType == EffectType.STRIKEOUT) {
                EffectPainter.STRIKE_THROUGH.paint(g2d, xStart, y, xEnd - xStart, editor.getCharHeight(), font);
            } else if (effectType == EffectType.WAVE_UNDERSCORE) {
                EffectPainter.WAVE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            } else if (effectType == EffectType.BOLD_DOTTED_LINE) {
                EffectPainter.BOLD_DOTTED_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.getDescent(), font);
            }
        }
    }

    private static int getIconY(final Icon icon, final Rectangle r) {
        return r.y + r.height / 2 - icon.getIconHeight() / 2;
    }

    @NotNull
    private static FontMetrics getFontMetrics(final Font font, @NotNull final Editor editor) {
        return FontInfo.getFontMetrics(font, FontInfo.getFontRenderContext(editor.getContentComponent()));
    }

    private static Font getFont(@NotNull final Editor editor) {
        final EditorColorsScheme colorsScheme = editor.getColorsScheme();
        // final TextAttributes attributes = editor.getColorsScheme().getAttributes(DebuggerColors.INLINED_VALUES);
        // final int fontStyle = attributes == null ? Font.PLAIN : attributes.getFontType();
        return UIUtil.getFontWithFallback(colorsScheme.getEditorFontName(), Font.PLAIN, colorsScheme.getEditorFontSize());
    }

    private TextAttributes getAttributes(final Editor editor) {
        if (isHovered)
            return editor.getColorsScheme().getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE);

        return editor.getColorsScheme().getAttributes(DebuggerColors.INLINED_VALUES);
    }
}
