package com.github.wenzewoo.coderemark.listener

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame

class ApplicationListener : ApplicationActivationListener {

    override fun applicationActivated(frame: IdeFrame) {
        println("applicationActivated(), project=${frame.project}")

        frame.project?.let {
            CodeRemarkInlayListener.getInstance(it).startListening()
        }
    }
}