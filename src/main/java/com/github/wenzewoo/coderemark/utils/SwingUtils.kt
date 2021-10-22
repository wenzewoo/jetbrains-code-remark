package com.github.wenzewoo.coderemark.utils

import com.intellij.util.ui.JBDimension
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

object SwingUtils {
    private val affineTransform: AffineTransform = AffineTransform()
    private val fontRenderContext: FontRenderContext = FontRenderContext(affineTransform, true, true)

    fun getTextHeight(str: String, font: Font): Double {
        return font.getStringBounds(str, fontRenderContext).width
    }

    fun getTextWidth(str: String, font: Font): Double {
        return font.getStringBounds(str, fontRenderContext).width
    }

    fun buildDimensionWithText(
        str: String,
        font: Font,
        minWidth: Int,
        maxWidth: Int,
        minHeight: Int,
        maxHeight: Int
    ): JBDimension {

        val w = getTextWidth(str, font)
        val width = when {
            w < minWidth -> minWidth
            w > maxWidth -> maxWidth
            else -> w.toInt()
        }
        val h = getTextHeight(str, font)
        val height = when {
            h < minHeight -> minHeight
            h > maxHeight -> maxHeight
            else -> h.toInt()
        }
        return JBDimension(width, height)
    }
}