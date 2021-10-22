package com.github.wenzewoo.coderemark.listener

import com.github.wenzewoo.coderemark.expand.renderCodeRemark
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile

class EditorManagerListener : FileEditorManagerListener {

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        source.getSelectedEditor(file)?.let {
            it.renderCodeRemark(10, "你好世界")
            it.renderCodeRemark(12, "Hello World")
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        println("fileClosed(), file=${file.canonicalPath}")
    }
}