package eu.pablob.paper_telegram_bridge

import org.bukkit.inventory.ItemStack
import java.awt.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class AdvancementRenderer {
    private val width = 320
    private val height = 64
    private val borderColor = Color(78, 78, 78)
    private val titleColor = Color(234, 234, 2)
    private val fontSize = 16f

    fun renderAdvancement(advancementTitle: String, frameType: String, icon: ItemStack?, textColor: Color): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        drawBackground(g)
        // drawBorder(g)
        drawTitle(g, advancementTitle)
        drawSubtitle(g, frameType, textColor)
        drawIcon(g, icon)

        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }

    private fun drawBackground(g: Graphics2D) {
        val backgroundImage = loadImage("/advancement32.png", this.javaClass)
        g.drawImage(backgroundImage, 0, 0, Color.BLACK, null)
    }


    private fun drawTitle(g: Graphics2D, title: String) {
        g.font = MinecraftFontLoader.getFont(fontSize)
        g.color = Color.WHITE
        g.drawString(title, 72, 60)
    }

    private fun drawSubtitle(g: Graphics2D, frameType: String, textColor: Color) {
        val subtitle = when (frameType) {
            "goal" -> "Goal Reached!"
            "challenge" -> "Challenge Complete!"
            else -> "Advancement Made!" // Default to "task"
        }
        g.font = MinecraftFontLoader.getFont(fontSize)
        g.color = if (textColor == Color(85, 255, 85)) titleColor else textColor
        g.drawString(subtitle, 72, 35)
    }

    private fun drawIcon(g: Graphics2D, item: ItemStack?) {
        val texture = item?.type?.let { loadItemTexture(it.name.lowercase(), this.javaClass) }
        if (texture != null) {
            g.drawImage(texture, 20, 15, 32, 32, null)
        } else {
            g.color = Color.GRAY
            g.fillRect(20, 15, 32, 32)
        }
    }
}
