package eu.pablob.paper_telegram_bridge

import org.bukkit.inventory.ItemStack
import java.awt.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class AdvancementRenderer {
    private val width = 250
    private val height = 80
    private val backgroundColor = Color(32, 32, 32)
    private val borderColor = Color(78, 78, 78)
    private val titleColor = Color(234, 234, 2)

    fun renderAdvancement(advancementTitle: String, frameType: String, icon: ItemStack?, textColor: Color): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        drawBackground(g)
        drawBorder(g)
        drawTitle(g, advancementTitle)
        drawSubtitle(g, frameType, textColor)
        drawIcon(g, icon)

        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray()
    }

    private fun drawBackground(g: Graphics2D) {
        g.color = backgroundColor
        g.fillRect(0, 0, width, height)
    }

    private fun drawBorder(g: Graphics2D) {
        g.color = borderColor

        // Thickness of the border (increase for a bolder outline)
        val thickness = 3

        // Draw a filled border
        g.fillRect(0, 0, width, thickness) // Top border
        g.fillRect(0, 0, thickness, height) // Left border
        g.fillRect(width - thickness, 0, thickness, height) // Right border
        g.fillRect(0, height - thickness, width, thickness) // Bottom border
    }


    private fun drawTitle(g: Graphics2D, title: String) {
        g.font = MinecraftFontLoader.getFont(16f)
        g.color = Color.WHITE
        g.drawString(title, 50, 60)
    }

    private fun drawSubtitle(g: Graphics2D, frameType: String, textColor: Color) {
        val subtitle = when (frameType) {
            "goal" -> "Goal Reached!"
            "challenge" -> "Challenge Complete!"
            else -> "Advancement Made!" // Default to "task"
        }
        g.font = MinecraftFontLoader.getFont(14f)
        g.color = if (textColor == Color(85, 255, 85)) titleColor else textColor
        g.drawString(subtitle, 50, 40)
    }

    private fun drawIcon(g: Graphics2D, item: ItemStack?) {
        val texture = item?.type?.let { loadItemTexture(it.name.lowercase(), this.javaClass) }
        if (texture != null) {
            g.drawImage(texture, 10, 15, 32, 32, null)
        } else {
            g.color = Color.GRAY
            g.fillRect(10, 15, 32, 32)
        }
    }
}
