package eu.pablob.paper_telegram_bridge

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayOutputStream
import java.util.logging.Logger

class InventoryRenderer {

    private val slotSize = 32 // Size of each slot in pixels
    private val padding = 4 // Padding between slots in pixels
    private val borderSize = 16 // Border size around slots in pixels
    private val bottomPadding = 8 // Additional padding for the bottom row (Hotbar)

    private val background: BufferedImage? = loadImage("/inventoryBackground.png", this.javaClass)
    private val logger: Logger = Logger.getLogger("InventoryRenderer")

    fun renderInventoryToFile(inventory: Inventory): ByteArray {
        val columns = 9 // Standard inventory columns
        val rows = 5 // 5 rows for armor, inventory, and hotbar

        val image = BufferedImage(
            background?.width!!,
            background.height,
            BufferedImage.TYPE_INT_ARGB
        )

        val g = image.createGraphics()

        // Draw the background first
        background.let {
            g.drawImage(it, 0, 0, null)
        }

        // Render all slots and items
        for (row in 0 until rows) { // Iterate rows in correct order
            for (col in 0 until columns) {
                if (row == 0 && col >= 5) continue // Skip slots beyond the 5th in the top row

                val index = when (row) {
                    0 -> 36 + col // First row (top row): Armor and Offhand
                    1 -> 9 + col // Second row: First row of inventory
                    2 -> 18 + col // Third row: Middle row of inventory
                    3 -> 27 + col // Fourth row: Bottom row of inventory
                    4 -> col // Fifth row (bottom row): Hotbar
                    else -> col // Fallback (should not happen)
                }

                var y = row * (slotSize + padding) + borderSize
                if (row == 4) y += bottomPadding
                val x = col * (slotSize + padding) + borderSize + 2


                // Draw item in slot (if present)
                val item = inventory.getItem(index)
                if (item != null && item.type != Material.AIR) {
                    drawItem(g, item, x, y)
                    if (item.enchantments.isNotEmpty()) {
                        g.color = Color(128, 0, 128, 48) // Purple tint for enchanted items
                        g.fillRect(x, y, slotSize, slotSize)
                    }
                }
            }
        }

        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        return outputStream.toByteArray().also { outputStream.close() }
    }

    private fun drawItem(g: Graphics2D, item: ItemStack, x: Int, y: Int) {
        val itemName = item.type.name.lowercase()
        val texture = when {
            itemName == "potion" -> loadPotionTexture(item, this.javaClass) ?: loadAwkwardPotionTexture(this.javaClass)
            itemName.contains("map") -> loadMapTexture(this.javaClass)
            else -> loadItemTexture(itemName, this.javaClass)
        }

        if (texture == null) {
            logger.warning("Missing texture for item: ${item.type}")
        }
        texture?.let {
            g.drawImage(it, x, y, slotSize, slotSize, null)
        }

        if (item.amount > 1) {
            g.color = Color.WHITE
            g.font = MinecraftFontLoader.getFont(16f)
            val countText = item.amount.toString()
            val textWidth = g.fontMetrics.stringWidth(countText)
            g.drawString(countText, x + slotSize - textWidth + 2, y + slotSize + 10)
        }
    }
}