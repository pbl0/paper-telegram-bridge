package org.kraftwerk28.spigot_tg_bridge

import java.awt.Font
import java.awt.GraphicsEnvironment
import java.io.InputStream

object MinecraftFontLoader {
    private var minecraftFont: Font? = null

    init {
        loadMinecraftFont()
    }

    private fun loadMinecraftFont() {
        try {
            val fontStream: InputStream? = this.javaClass.getResourceAsStream("/Minecraftia-Regular.ttf")
            if (fontStream != null) {
                val font = Font.createFont(Font.TRUETYPE_FONT, fontStream)
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)
                minecraftFont = font
            } else {
                println("Minecraft font file not found. Falling back to default font.")
            }
        } catch (e: Exception) {
            println("Failed to load Minecraft font: ${e.message}. Falling back to default font.")
        }
    }

    fun getFont(size: Float): Font {
        return minecraftFont?.deriveFont(size) ?: Font("SansSerif", Font.BOLD, size.toInt())
    }
}
