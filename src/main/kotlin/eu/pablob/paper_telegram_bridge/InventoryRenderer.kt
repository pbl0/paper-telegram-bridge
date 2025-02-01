package eu.pablob.paper_telegram_bridge

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object InventoryRenderer {

    private const val SLOT_SIZE = 64 // Size of each slot in pixels
    private const val PADDING = 4 // Padding between slots in pixels
    private const val BORDER_SIZE = 2 // Border size around slots in pixels
    private const val BOTTOM_PADDING = 8 // Additional padding for the bottom row (Hotbar)

    fun renderInventoryToFile(inventory: Inventory, filePath: String): File {
        val columns = 9 // Standard inventory columns
        val rows = 5 // 5 rows for armor, inventory, and hotbar

        // Calculate image dimensions
        val width = columns * (SLOT_SIZE + PADDING) - PADDING + 2 * BORDER_SIZE
        val height = rows * (SLOT_SIZE + PADDING) - PADDING + 2 * BORDER_SIZE + BOTTOM_PADDING

        // Create the image
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        // Render background (lighter color)
        g.color = Color.decode("#C5C5C5") // Light gray for borders
        g.fillRect(0, 0, width, height)

        // Render all slots and items
        for (row in 0 until rows) { // Iterate rows in correct order
            for (col in 0 until columns) {
                if (row == 0 && col >= 5) continue // Skip slots beyond the 5th in the top row

                val index =
                    when (row) {
                        0 -> 36 + col // First row (top row): Armor and Offhand
                        1 -> 9 + col // Second row: First row of inventory *
                        2 -> 18 + col // Third row: Middle row of inventory
                        3 -> 27 + col // Fourth row: Bottom row of inventory *
                        4 -> col // Fifth row (bottom row): Hotbar
                        else -> col // Fallback (should not happen)
                    }

                var y = row * (SLOT_SIZE + PADDING) + BORDER_SIZE

                // Add extra bottom padding for the hotbar row
                if (row == 4) {
                    y += BOTTOM_PADDING
                }

                val x = col * (SLOT_SIZE + PADDING) + BORDER_SIZE

                // Draw slot background
                g.color = Color.decode("#8A8A8A") // Darker gray for slot background
                g.fillRect(x, y, SLOT_SIZE, SLOT_SIZE)

                // Draw item in slot (if present)
                val item = inventory.getItem(index)
                if (item != null && item.type != Material.AIR) {
                    drawItem(g, item, x, y)

                    // Render enchantment tint if the item is enchanted
                    if (item.enchantments.isNotEmpty()) {
                        g.color = Color(128, 0, 128, 48) // Purple with 48 alpha
                        g.fillRect(x, y, SLOT_SIZE, SLOT_SIZE)
                    }
                }
            }
        }

        g.dispose()

        // Save the image to a file
        val outputFile = File(filePath)
        ImageIO.write(image, "png", outputFile)
        return outputFile
    }

    private fun drawItem(g: Graphics2D, item: ItemStack, x: Int, y: Int) {
        val itemName = item.type.name.lowercase()
        if (itemName == "potion") {
            // Handle potions differently
            val potionTexture =
                loadPotionTexture(item, this.javaClass)
                    ?: loadAwkwardPotionTexture(this.javaClass)
            if (potionTexture != null) {
                g.drawImage(potionTexture, x + 8, y + 8, SLOT_SIZE - 16, SLOT_SIZE - 16, null)
            }
        } else if (itemName.contains("map")){
            // Handle maps
            g.drawImage(loadMapTexture(this.javaClass), x + 8, y + 8, SLOT_SIZE - 16, SLOT_SIZE - 16, null)
        }
        else {
            // Handle non-potion items
            val texture = loadItemTexture(itemName, this.javaClass)
            if (texture != null) {
                g.drawImage(texture, x + 1, y + 1, SLOT_SIZE - 1, SLOT_SIZE - 1, null)
            } else {
                // Fallback to drawing a gray box
                g.color = Color.GRAY
                g.fillRect(x + 8, y + 8, SLOT_SIZE - 16, SLOT_SIZE - 16)
            }
        }

        // Optionally, draw the item count adjusted to the bottom-right
        if (item.amount > 1) {
            g.color = Color.WHITE
            g.font = MinecraftFontLoader.getFont(26f)
            val countText = item.amount.toString()
            val textWidth = g.fontMetrics.stringWidth(countText)

            // Adjusted the item count position to bottom-right
            // g.drawString(countText, x + SLOT_SIZE - textWidth - 10, y + SLOT_SIZE - 12)
            g.drawString(countText, x + SLOT_SIZE - textWidth, y + SLOT_SIZE + 10)
        }
    }
}
