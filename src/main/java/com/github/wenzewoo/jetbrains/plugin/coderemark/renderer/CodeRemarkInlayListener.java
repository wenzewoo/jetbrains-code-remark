package com.github.wenzewoo.jetbrains.plugin.coderemark.renderer;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseEvent;

@Service
public final class CodeRemarkInlayListener implements EditorMouseMotionListener, EditorMouseListener {
    private final Project myProject;
    private Inlay lastHoveredInlay;
    private boolean myListening;

    public CodeRemarkInlayListener(@NotNull Project project) {
        super();
        this.lastHoveredInlay = null;
        this.myProject = project;
    }

    public void startListening() {
        if (!this.myListening) {
            this.myListening = true;
            EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
            multicaster.addEditorMouseMotionListener(this, this.myProject);
            multicaster.addEditorMouseListener(this, this.myProject);
        }

    }

    public void mouseMoved(@NotNull EditorMouseEvent event) {
        if (event == null) {
            return;
        }
        Inlay<?> inlay = getInlay(event);
        if (this.lastHoveredInlay != null) {
            CodeRemarkInlineRenderer renderer = (CodeRemarkInlineRenderer) this.lastHoveredInlay.getRenderer();
            if (this.lastHoveredInlay != inlay) {
                renderer.onMouseExit(this.lastHoveredInlay, event);
            }

            this.lastHoveredInlay = null;
        }

        if (inlay != null) {
            if (inlay.getRenderer() instanceof CodeRemarkInlineRenderer) {
                ((CodeRemarkInlineRenderer) inlay.getRenderer()).onMouseMove(inlay, event);
                this.lastHoveredInlay = inlay;
            } else {
                this.lastHoveredInlay = null;
            }
        }

    }

    public void mouseClicked(@NotNull EditorMouseEvent event) {
        if (event == null) {
            return;
        }

        if (!event.isConsumed()) {
            Inlay inlay = getInlay(event);
            if (inlay != null && inlay.getRenderer() instanceof CodeRemarkInlineRenderer) {
                ((CodeRemarkInlineRenderer) inlay.getRenderer()).onClick(inlay, event);
                event.consume();
            }

        }
    }

    public static CodeRemarkInlayListener getInstance(Project project) {
        return (CodeRemarkInlayListener) project.getService(CodeRemarkInlayListener.class);
    }

    private Inlay<?> getInlay(@NotNull EditorMouseEvent event) {
        Editor editor = event.getEditor();
        InlayModel inlayModel = editor.getInlayModel();
        MouseEvent e = event.getMouseEvent();
        Point point = e.getPoint();
        EditorMouseEventArea area = event.getArea();
        boolean inEditingArea = area == EditorMouseEventArea.EDITING_AREA;
        Inlay<?> inlayCandidate = inEditingArea ? inlayModel.getElementAt(point, CodeRemarkInlineRenderer.class) : null;
        Inlay<?> inlay = inlayCandidate == null ||
                (inlayCandidate.getPlacement() == Inlay.Placement.BELOW_LINE ||
                        inlayCandidate.getPlacement() == Inlay.Placement.ABOVE_LINE) &&
                        inlayCandidate.getWidthInPixels() <= point.getX() ? null : inlayCandidate;

        return inlay;
    }
}