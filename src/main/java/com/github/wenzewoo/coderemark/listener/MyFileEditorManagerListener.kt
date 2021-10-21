package com.github.wenzewoo.coderemark.listener

import com.github.wenzewoo.coderemark.expand.getRelativePath
import com.github.wenzewoo.coderemark.expand.versionHash
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile

class MyFileEditorManagerListener : FileEditorManagerListener {

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        source.getSelectedEditor(file)?.let {

            println("fileOpened(), path=${file.getRelativePath(source.project)}, versionHash=${file.versionHash(source.project)} ")
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        println("fileClosed(), file=${file.canonicalPath}")
    }
}