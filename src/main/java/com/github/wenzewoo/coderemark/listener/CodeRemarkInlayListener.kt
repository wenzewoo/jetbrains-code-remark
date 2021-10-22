package com.github.wenzewoo.coderemark.listener

import com.github.wenzewoo.coderemark.renderer.CodeRemarkInlineRenderer
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayModel
import com.intellij.openapi.editor.event.*
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull
import java.awt.Point
import java.awt.event.MouseEvent

@Service
class CodeRemarkInlayListener(private val project: Project) : EditorMouseMotionListener, EditorMouseListener,
    Disposable {

    private var isListening = false
    private var lastHoveredInlay: Inlay<*>? = null

    fun startListening() {
        if (!isListening) {
            isListening = true
            val multicaster: EditorEventMulticaster = EditorFactory.getInstance().eventMulticaster
            multicaster.addEditorMouseMotionListener(this, this)
            multicaster.addEditorMouseListener(this, this)
        }
    }

    override fun mouseMoved(@NotNull event: EditorMouseEvent) {
        val inlay = getInlay(event)
        if (lastHoveredInlay != null) {
            val renderer: CodeRemarkInlineRenderer = lastHoveredInlay!!.renderer as CodeRemarkInlineRenderer
            if (lastHoveredInlay !== inlay) {
                renderer.onMouseExit(lastHoveredInlay!!, event)
            }
            lastHoveredInlay = null
        }
        if (inlay != null) {
            lastHoveredInlay = if (inlay.renderer is CodeRemarkInlineRenderer) {
                (inlay.renderer as CodeRemarkInlineRenderer).onMouseMove(inlay, event)
                inlay
            } else {
                null
            }
        }
    }

    override fun mouseClicked(@NotNull event: EditorMouseEvent) {
        if (!event.isConsumed) {
            val inlay = getInlay(event)
            if (inlay != null && inlay.renderer is CodeRemarkInlineRenderer) {
                (inlay.renderer as CodeRemarkInlineRenderer).onClick(inlay, event)
                event.consume()
            }
        }
    }

    private fun getInlay(@NotNull event: EditorMouseEvent): Inlay<*>? {
        val editor: Editor = event.editor
        val inlayModel: InlayModel = editor.inlayModel
        val e: MouseEvent = event.mouseEvent
        val point: Point = e.point
        val area: EditorMouseEventArea = event.area
        val inEditingArea = area === EditorMouseEventArea.EDITING_AREA
        val inlayCandidate: Inlay<*>? = if (inEditingArea) inlayModel.getElementAt(
            point, CodeRemarkInlineRenderer::class.java
        ) else null
        return if (inlayCandidate == null ||
            (inlayCandidate.placement == Inlay.Placement.BELOW_LINE ||
                    inlayCandidate.placement == Inlay.Placement.ABOVE_LINE) &&
            inlayCandidate.widthInPixels <= point.getX()
        ) null else inlayCandidate
    }

    companion object {
        fun getInstance(project: Project): CodeRemarkInlayListener {
            return project.getService(CodeRemarkInlayListener::class.java)
        }
    }

    override fun dispose() {
        // TODO
    }
}