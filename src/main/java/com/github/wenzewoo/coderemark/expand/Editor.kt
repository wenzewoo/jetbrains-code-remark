package com.github.wenzewoo.coderemark.expand

import com.github.wenzewoo.coderemark.renderer.CodeRemarkInlineRenderer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile


fun FileEditor.renderCodeRemark(lineNumber: Int, description: String) {
    if (this is TextEditor) {
        this.editor.renderCodeRemark(lineNumber, description)
    }
}

fun Editor.renderCodeRemark(lineNumber: Int, description: String) {
    var exists = false
    this.inlayModel.getAfterLineEndElementsForLogicalLine(lineNumber).forEach {
        if (it.renderer is CodeRemarkInlineRenderer) {
            exists = true
            (it.renderer as CodeRemarkInlineRenderer).setDescription(description)
        }
    }

    if (!exists) {
        val lineEndOffset = this.document.getLineEndOffset(lineNumber)
        this.inlayModel.addAfterLineEndElement(lineEndOffset, true, CodeRemarkInlineRenderer(description))
    }
}

fun Editor.clearCodeRemark(lineNumber: Int) {
    this.inlayModel.getAfterLineEndElementsForLogicalLine(lineNumber).forEach {
        if (it.renderer is CodeRemarkInlineRenderer) {
            Disposer.dispose(it)
        }
    }
}


fun Editor.virtualFile(): VirtualFile? {
    if (this is EditorEx) {
        return this.virtualFile
    }
    return null
}

fun Editor.lineNumber(): Int {
    return this.document.getLineNumber(this.caretModel.offset)
}