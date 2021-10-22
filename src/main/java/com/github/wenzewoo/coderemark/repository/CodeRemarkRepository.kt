package com.github.wenzewoo.coderemark.repository

import com.github.wenzewoo.coderemark.CodeRemark

interface CodeRemarkRepository {

    fun save(codeRemark: CodeRemark)
    
}