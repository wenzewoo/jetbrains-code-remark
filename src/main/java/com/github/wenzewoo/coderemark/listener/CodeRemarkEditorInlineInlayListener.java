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

package com.github.wenzewoo.coderemark.listener;

import com.github.wenzewoo.coderemark.renderer.CodeRemarkEditorInlineInlayRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;

public class CodeRemarkEditorInlineInlayListener
        implements EditorMouseMotionListener, EditorMouseListener, Disposable {

    private boolean isListening = false;
    private Inlay<?> lastHoveredInlay = null;

    public static CodeRemarkEditorInlineInlayListener getInstance(@NotNull final Project project) {
        return ServiceManager.getService(project, CodeRemarkEditorInlineInlayListener.class);
    }

    public void startListening() {
        if (!isListening) {
            final EditorEventMulticaster editorEventMulticaster = EditorFactory.getInstance().getEventMulticaster();
            editorEventMulticaster.addEditorMouseListener(this, this);
            editorEventMulticaster.addEditorMouseMotionListener(this, this);
            isListening = true;
        }
    }

    @Override
    public void mouseMoved(@NotNull final EditorMouseEvent event) {
        final Inlay<?> inlay = getInlay(event);

        if (lastHoveredInlay != null) {
            final EditorCustomElementRenderer renderer = lastHoveredInlay.getRenderer();
            if (renderer instanceof CodeRemarkEditorInlineInlayRenderer) {
                final CodeRemarkEditorInlineInlayRenderer codeRemarkRender = (CodeRemarkEditorInlineInlayRenderer) renderer;
                if (lastHoveredInlay != inlay)
                    codeRemarkRender.onMouseExited(lastHoveredInlay, event);
                lastHoveredInlay = null;
            }
        }

        if (inlay != null) {
            final EditorCustomElementRenderer renderer = inlay.getRenderer();
            if (renderer instanceof CodeRemarkEditorInlineInlayRenderer) {
                final CodeRemarkEditorInlineInlayRenderer codeRemarkRender = (CodeRemarkEditorInlineInlayRenderer) renderer;
                codeRemarkRender.onMouseMoved(inlay, event);
                lastHoveredInlay = inlay;
            } else lastHoveredInlay = null;
        }
    }

    @Override
    public void mouseClicked(@NotNull final EditorMouseEvent event) {
        if (!event.isConsumed()) {
            final Inlay<?> inlay = getInlay(event);

            if (null != inlay) {
                final EditorCustomElementRenderer renderer = inlay.getRenderer();
                if (renderer instanceof CodeRemarkEditorInlineInlayRenderer)
                    ((CodeRemarkEditorInlineInlayRenderer) renderer).onMouseClicked(inlay, event);
                event.consume();
            }
        }
    }

    @Override
    public void dispose() {
        // Do nothing
    }


    private Inlay<?> getInlay(@NotNull final EditorMouseEvent event) {
        final Editor editor = event.getEditor();
        final InlayModel inlayModel = editor.getInlayModel();
        final MouseEvent mouseEvent = event.getMouseEvent();
        final Point point = mouseEvent.getPoint();
        final EditorMouseEventArea area = event.getArea();

        Inlay<? extends CodeRemarkEditorInlineInlayRenderer> inlay = null;
        if (EditorMouseEventArea.EDITING_AREA == area)
            inlay = inlayModel.getElementAt(point, CodeRemarkEditorInlineInlayRenderer.class);

        if (inlay == null
                || ((inlay.getPlacement() == Inlay.Placement.BELOW_LINE
                || inlay.getPlacement() == Inlay.Placement.ABOVE_LINE)
                && inlay.getWidthInPixels() <= point.getX())) {
            return null;
        }
        return inlay;
    }
}
