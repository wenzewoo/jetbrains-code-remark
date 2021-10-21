package com.github.wenzewoo.coderemark

data class CodeRemark(
    val projectName: String,
    val isWritable: Boolean,
    val filePath: String, // file.absolutePath() or file.relativePath()
    val versionHash: String, // fileBody.hash() or relativePath.hash()
    val lineNumber: Int,
    val description: String
)
