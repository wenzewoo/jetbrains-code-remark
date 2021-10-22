package com.github.wenzewoo.coderemark.action

import com.github.wenzewoo.coderemark.expand.lineNumber
import com.github.wenzewoo.coderemark.expand.renderCodeRemark
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Constraints
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

class AddIntentionAction : BaseIntentionAction() {
    override fun getFamilyName(): String {
        return "[MARK] Add remark"
    }

    override fun getText(): String {
        return this.familyName
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?): Boolean {
        return true
    }

    override fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
        if (null == editor) return

        val lineNumber = editor.lineNumber()

        val contentPanel = JBUI.Panels.simplePanel()

        val headPanel = JBUI.Panels.simplePanel()
        val lblTitle = JLabel("Add remark: ${lineNumber + 1}", SwingConstants.CENTER)
        lblTitle.font = Font(lblTitle.font.name, Font.BOLD, lblTitle.font.size)
        lblTitle.border = CompoundBorder(lblTitle.border, EmptyBorder(5, 5, 5, 5))
        headPanel.addToCenter(lblTitle)
        headPanel.background = UIUtil.getToolTipActionBackground();
        contentPanel.addToTop(headPanel);

        val editPane = JEditorPane()

        editPane.preferredSize = JBDimension(300, 80)
        editPane.requestFocus()
        editPane.border = CompoundBorder(BorderFactory.createEmptyBorder(), EmptyBorder(5, 5, 5, 5))

        val scrollPane = JBScrollPane(editPane)
        scrollPane.border = BorderFactory.createEmptyBorder()

        contentPanel.addToCenter(scrollPane)


        val actionGroup = DefaultActionGroup()
        val toolbar = ActionToolbarImpl("AddIntentionAction.Toolbar", actionGroup, true)
        toolbar.border = BorderFactory.createEmptyBorder()
        toolbar.background = UIUtil.getToolTipActionBackground()
        contentPanel.addToBottom(toolbar)

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(contentPanel, editPane)
            .setResizable(true)
            .setRequestFocus(true)
            .setMovable(false)
            .createPopup()

        actionGroup.add(object : AnAction("Save this remark?", null, AllIcons.Actions.Commit) {

            override fun actionPerformed(e: AnActionEvent) {
                editor.renderCodeRemark(lineNumber, editPane.text)
                popup.dispose()
            }
        }, Constraints.FIRST)

        actionGroup.add(object : AnAction("Cancel?", null, AllIcons.Actions.Cancel) {

            override fun actionPerformed(e: AnActionEvent) {
                popup.dispose()
            }
        }, Constraints.LAST)

        contentPanel.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                if (e?.keyCode == 27) {
                    popup.dispose()
                }
            }
        })

        popup.showInBestPositionFor(editor)
    }
}