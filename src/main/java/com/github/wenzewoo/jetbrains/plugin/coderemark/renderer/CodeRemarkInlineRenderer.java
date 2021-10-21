package com.github.wenzewoo.jetbrains.plugin.coderemark.renderer;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.FontPreferences;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.ComplementaryFontsRegistry;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CodeRemarkInlineRenderer implements EditorCustomElementRenderer {

    private final String myText;
    private boolean isHovered = false;
    private int myTextStartXCoordinate;

    public CodeRemarkInlineRenderer(String text) {
        myText = "(" + text + ")";
    }

    private static FontInfo getFontInfo(@NotNull Editor editor) {
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        FontPreferences fontPreferences = colorsScheme.getFontPreferences();
        TextAttributes attributes = editor.getColorsScheme().getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE);
        int fontStyle = attributes == null ? Font.PLAIN : attributes.getFontType();
        return ComplementaryFontsRegistry.getFontAbleToDisplay('a', fontStyle, fontPreferences,
                FontInfo.getFontRenderContext(editor.getContentComponent()));
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        FontInfo fontInfo = getFontInfo(inlay.getEditor());
        return fontInfo.fontMetrics().stringWidth(myText);
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        Editor editor = inlay.getEditor();
        TextAttributes attributes = editor.getColorsScheme().getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE);
        if (attributes == null) return;
        Color fgColor = attributes.getForegroundColor();
        if (fgColor == null) return;
        g.setColor(fgColor);
        FontInfo fontInfo = getFontInfo(editor);
        g.setFont(fontInfo.getFont());
        FontMetrics metrics = fontInfo.fontMetrics();
        g.drawString(myText, r.x, r.y + metrics.getAscent());

        int margin = metrics.charWidth(' ') / 4;
        int curX = r.x + metrics.charWidth(' ');
        myTextStartXCoordinate = curX += (2 * margin);
    }

    @NotNull
    private static FontMetrics getFontMetrics(Font font, @NotNull Editor editor) {
        return FontInfo.getFontMetrics(font, FontInfo.getFontRenderContext(editor.getContentComponent()));
    }


    public void onClick(Inlay inlay, EditorMouseEvent event) {


    }

    public void onMouseExit(Inlay inlay, @NotNull EditorMouseEvent event) {
        setHovered(false, inlay, (EditorEx) event.getEditor());
    }

    public void onMouseMove(Inlay inlay, @NotNull EditorMouseEvent event) {
        EditorEx editorEx = (EditorEx) event.getEditor();
        if (event.getMouseEvent().getX() >= myTextStartXCoordinate) {
            setHovered(true, inlay, editorEx);
        } else {
            setHovered(false, inlay, editorEx);
        }
    }

    private void setHovered(boolean active, Inlay inlay, EditorEx editorEx) {
        boolean oldState = isHovered;
        isHovered = active;
        Cursor cursor = active ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null;
        editorEx.setCustomCursor(CodeRemarkInlineRenderer.class, cursor);
        if (oldState != active) {
//            inlay.update();
            inlay.repaint();
        }
    }
}
