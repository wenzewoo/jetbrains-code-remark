package com.github.wenzewoo.coderemark.renderer

import com.github.wenzewoo.coderemark.actions.RemoveAction
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.FontPreferences
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.ComplementaryFontsRegistry
import com.intellij.openapi.editor.impl.FontInfo
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.ui.DebuggerColors
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JEditorPane


class CodeRemarkInlineRenderer(private var description: String) : EditorCustomElementRenderer {

    private var isHovered = false
    private var textStartXCoordinate: Int = -1
    private val descriptionMaxLength = 20

    private fun getFontInfo(editor: Editor): FontInfo {
        val colorsScheme: EditorColorsScheme = editor.colorsScheme
        val fontPreferences: FontPreferences = colorsScheme.fontPreferences
        val attributes: TextAttributes = editor.colorsScheme.getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE)

        return ComplementaryFontsRegistry.getFontAbleToDisplay(
            'a'.toInt(), attributes.fontType, fontPreferences,
            FontInfo.getFontRenderContext(editor.contentComponent)
        )
    }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return this.getFontInfo(inlay.editor).fontMetrics().stringWidth(this.description)
    }


    override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttributes: TextAttributes) {
        val editor = inlay.editor
        val attributes = editor.colorsScheme.getAttributes(
            if (isHovered) DebuggerColors.INLINED_VALUES_MODIFIED
            else DebuggerColors.INLINED_VALUES_EXECUTION_LINE
        ) ?: return

        val fgColor = attributes.foregroundColor ?: return

        g.color = fgColor
        val fontInfo = getFontInfo(editor)
        g.font = fontInfo.font
        val metrics = fontInfo.fontMetrics()

        var previewDescription = description
        if (description.length > descriptionMaxLength)
            previewDescription = "${description.substring(0, descriptionMaxLength - 1)}..."

        g.drawString("// $previewDescription", r.x, r.y + metrics.ascent)

        val margin = metrics.charWidth(' ') / 4
        val curX = r.x + metrics.charWidth(' ')
        textStartXCoordinate = curX + 2 * margin
    }

    fun onClick(inlay: Inlay<*>, event: EditorMouseEvent) {
        val contentPanel = JBUI.Panels.simplePanel()

        val editPane = JEditorPane()
        editPane.preferredSize = JBDimension(300, 80)
        editPane.requestFocus()
        editPane.border = BorderFactory.createEmptyBorder()
        editPane.text = this.description

        val scrollPane = JBScrollPane(editPane)
        scrollPane.border = BorderFactory.createEmptyBorder()

        contentPanel.addToCenter(scrollPane)

        val actionGroup = DefaultActionGroup()
        actionGroup.add(RemoveAction(), Constraints.FIRST)

        val toolbar = ActionToolbarImpl("CodeRemarkInlineRenderer.Toolbar", actionGroup, true)
        toolbar.border = BorderFactory.createEmptyBorder()
        toolbar.background = UIUtil.getToolTipActionBackground()
        contentPanel.addToBottom(toolbar)

        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(contentPanel, editPane)
            .setResizable(false)
            .setRequestFocus(true)
            .setMovable(false)
            .createPopup()
            .showInBestPositionFor(event.editor)
    }

    fun onMouseExit(inlay: Inlay<*>, event: EditorMouseEvent) {
        setHovered(false, inlay, event.editor as EditorEx)
    }

    fun onMouseMove(inlay: Inlay<*>, event: EditorMouseEvent) {
        val editorEx: EditorEx = event.editor as EditorEx
        setHovered(event.mouseEvent.x >= textStartXCoordinate, inlay, editorEx)
    }

    private fun setHovered(active: Boolean, inlay: Inlay<*>, editorEx: EditorEx) {
        val oldState = isHovered
        isHovered = active
        val cursor: Cursor? = if (active) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else null
        editorEx.setCustomCursor(CodeRemarkInlineRenderer::class.java, cursor)
        if (oldState != active)
            inlay.repaint()
    }
}