package com.github.wenzewoo.coderemark.actions

import com.github.wenzewoo.coderemark.expand.lineNumber
import com.github.wenzewoo.coderemark.expand.versionHash
import com.github.wenzewoo.coderemark.expand.virtualFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages

class RemoveAction() : AnAction("Remove") {

    override fun displayTextInToolbar(): Boolean {
        return true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = CommonDataKeys.PROJECT.getData(e.dataContext) ?: return
        CommonDataKeys.EDITOR.getData(e.dataContext)?.let { editor ->
            editor.virtualFile()?.let { file ->
                Messages.showInfoMessage(
                    "删除事件触发：project=${project.name}, line=${editor.lineNumber()}, version=${file.versionHash(project)}",
                    "CodeRemark"
                )
            }
        }
    }
}