package org.kraftwerk28.spigot_tg_bridge

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object InventoryRenderer {

    private const val SLOT_SIZE = 32 // Size of each slot in pixels
    private const val PADDING = 2    // Padding between slots in pixels

    fun renderInventoryToFile(inventory: Inventory, filePath: String): File {
        val columns = 9 // Standard inventory columns
        val rows = inventory.size / columns

        // Calculate image dimensions
        val width = columns * (SLOT_SIZE + PADDING) - PADDING
        val height = rows * (SLOT_SIZE + PADDING) - PADDING

        // Create the image
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        // Render background
        g.color = Color.DARK_GRAY
        g.fillRect(0, 0, width, height)

        // Render inventory slots and items
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val index = row * columns + col
                val x = col * (SLOT_SIZE + PADDING)
                val y = row * (SLOT_SIZE + PADDING)

                // Draw slot background
                g.color = Color.LIGHT_GRAY
                g.fillRect(x, y, SLOT_SIZE, SLOT_SIZE)

                // Draw item in slot (if present)
                val item = inventory.getItem(index)
                if (item != null && item.type != Material.AIR) {
                    drawItem(g, item, x, y)
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
        // Placeholder rendering for item icons
        g.color = Color.ORANGE
        g.fillRect(x + 4, y + 4, SLOT_SIZE - 8, SLOT_SIZE - 8)

        // Draw item count
        if (item.amount > 1) {
            g.color = Color.WHITE
            g.font = Font("Arial", Font.BOLD, 12)
            val countText = item.amount.toString()
            val textWidth = g.fontMetrics.stringWidth(countText)
            g.drawString(countText, x + SLOT_SIZE - textWidth - 4, y + SLOT_SIZE - 4)
        }
    }
}
