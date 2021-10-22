package com.github.wenzewoo.coderemark.expand

import com.github.wenzewoo.coderemark.renderer.CodeRemarkInlineRenderer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vfs.VirtualFile


fun FileEditor.renderCodeRemark(lineNumber: Int, description: String) {
    if (this is TextEditor) {
        this.editor.renderCodeRemark(lineNumber, description)
    }
}

fun Editor.renderCodeRemark(lineNumber: Int, description: String) {
    val lineEndOffset = this.document.getLineEndOffset(lineNumber)
    val renderer = CodeRemarkInlineRenderer(description)
    this.inlayModel.addAfterLineEndElement(lineEndOffset, true, renderer)
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