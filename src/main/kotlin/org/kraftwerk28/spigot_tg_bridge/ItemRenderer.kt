package org.kraftwerk28.spigot_tg_bridge

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File
import java.awt.Color
import java.awt.Graphics2D

object ItemRenderer {
    private const val WIDTH = 250
    private const val IMAGE_SCALE = 48
    private const val MARGIN = 12
    private const val BACKGROUND_COLOR = "#210939"
    private const val BORDER_COLOR = "#1A0B1A"
    private const val ENCHANTMENT_COLOR = "#A7A7A7"

    fun renderItemToFile(item: ItemStack, filePath: String): Pair<File, String> {

        // Default rendering for non-map items
        val texture = loadTexture(item)
        val height = calculateDynamicHeight(item)
        val image = BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        drawBackground(g, WIDTH, height)
        drawTexture(g, texture)
        val itemName = drawItemName(g, item)

        // Calculate textYOffset for enchantments and durability
        var textYOffset = IMAGE_SCALE + MARGIN + 50 // Start below the item name

        // Draw enchantments and update textYOffset
        textYOffset = drawEnchantments(g, item, textYOffset)

        // Draw durability below enchantments (or item name if no enchantments)
        drawDurability(g, item, textYOffset)

        // Draw stack size
        drawStackSize(g, item)

        g.dispose()

        val outputFile = File(filePath)
        ImageIO.write(image, "png", outputFile)
        return Pair(outputFile, itemName)
    }

    private fun loadTexture(item: ItemStack): BufferedImage? {
        val itemName = item.type.name.lowercase()
        return when {
            itemName == "potion" || itemName == "splash_potion" || itemName == "lingering_potion" ->
                loadPotionTexture(item, this.javaClass) ?: loadAwkwardPotionTexture(this.javaClass)
            itemName == "filled_map" -> loadMapTexture(this.javaClass)
            else -> loadItemTexture(itemName, this.javaClass)
        }
    }


    private fun calculateDynamicHeight(item: ItemStack): Int {
        var height = IMAGE_SCALE + MARGIN * 2 + 30 // Base height for texture and name

        val enchantments = if (item.itemMeta is EnchantmentStorageMeta) {
            (item.itemMeta as EnchantmentStorageMeta).storedEnchants
        } else {
            item.enchantments
        }

        if (enchantments.isNotEmpty()) {
            height += 20 * enchantments.size // Add space for enchantments
        }

        if (item.itemMeta is org.bukkit.inventory.meta.Damageable && item.type.maxDurability > 0) {
            height += 20 // Add space for durability
        }

        return height + MARGIN // Add bottom margin
    }

    private fun drawBackground(g: Graphics2D, width: Int, height: Int) {
        g.color = Color.decode(BACKGROUND_COLOR)
        g.fillRect(0, 0, width, height)
        g.color = Color.decode(BORDER_COLOR)
        g.fillRect(4, 4, width - 8, height - 8)
    }

    private fun drawTexture(g: Graphics2D, texture: BufferedImage?) {
        if (texture == null) {
            g.color = Color.GRAY
            g.fillRect(MARGIN, MARGIN, IMAGE_SCALE, IMAGE_SCALE)
        } else {
            g.drawImage(texture, MARGIN, MARGIN, IMAGE_SCALE, IMAGE_SCALE, null)
        }
    }

    private fun drawItemName(g: Graphics2D, item: ItemStack): String {
        val displayName = item.itemMeta?.displayName
        val fullName = when (val itemMeta = item.itemMeta) {
            is PotionMeta -> getPotionName(item, itemMeta, displayName)
            else -> getItemName(item, displayName)
        }
        val nameColor = determineNameColor(item)
        g.font = MinecraftFontLoader.getFont(16f)
        g.color = nameColor
        g.drawString(fullName, MARGIN, IMAGE_SCALE + MARGIN + 30)
        return fullName
    }

    private fun getPotionName(item: ItemStack, potionMeta: PotionMeta, displayName: String?): String {
        val basePotionData = potionMeta.basePotionData
        return if (basePotionData != null) {
            val potionType = basePotionData.type
            val potionTypeName = potionType.toString().replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
            val potionName = when (item.type.name.lowercase()) {
                "splash_potion" -> "Splash Potion of $potionTypeName"
                "lingering_potion" -> "Lingering Potion of $potionTypeName"
                else -> "Potion of $potionTypeName"
            }
            if (!displayName.isNullOrEmpty()) "$displayName ($potionName)" else potionName
        } else {
            if (!displayName.isNullOrEmpty()) "$displayName (Potion)" else "Potion"
        }
    }

    private fun getItemName(item: ItemStack, displayName: String?): String {
        val itemTypeName = item.type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        return if (!displayName.isNullOrEmpty()) "$displayName ($itemTypeName)" else itemTypeName
    }

    private fun determineNameColor(item: ItemStack): Color {
        return when {
            item.itemMeta?.hasEnchants() == true || (item.itemMeta is EnchantmentStorageMeta && (item.itemMeta as EnchantmentStorageMeta).storedEnchants.isNotEmpty()) -> Color.CYAN
            item.type.name.contains("totem", ignoreCase = true) || item.type.name.contains("book", ignoreCase = true) -> Color.YELLOW
            else -> Color.WHITE
        }
    }

    private fun drawEnchantments(g: Graphics2D, item: ItemStack, textYOffset: Int): Int {
        val enchantments = if (item.itemMeta is EnchantmentStorageMeta) {
            (item.itemMeta as EnchantmentStorageMeta).storedEnchants
        } else {
            item.enchantments
        }
        if (enchantments.isNotEmpty()) {
            g.font = MinecraftFontLoader.getFont(14f)
            g.color = Color.decode(ENCHANTMENT_COLOR)
            var currentYOffset = textYOffset
            enchantments.forEach { (enchantment, level) ->
                g.drawString("${enchantment.key.key.replace('_', ' ').capitalize()} $level", MARGIN, currentYOffset)
                currentYOffset += 20
            }
            return currentYOffset // Return the updated Y offset
        }
        return textYOffset // If no enchantments, return the original Y offset
    }

    private fun drawDurability(g: Graphics2D, item: ItemStack, textYOffset: Int) {
        if (item.itemMeta is org.bukkit.inventory.meta.Damageable && item.type.maxDurability > 0) {
            g.font = MinecraftFontLoader.getFont(14f)
            g.color = Color.WHITE
            val currentDurability = item.type.maxDurability - (item.itemMeta as org.bukkit.inventory.meta.Damageable).damage
            g.drawString("Durability: $currentDurability/${item.type.maxDurability}", MARGIN, textYOffset)
        }
    }

    private fun drawStackSize(g: Graphics2D, item: ItemStack) {
        if (item.amount > 1) {
            g.font = MinecraftFontLoader.getFont(20f)
            g.color = Color.WHITE
            val stackSize = "x ${item.amount}"
            val textWidth = g.fontMetrics.stringWidth(stackSize)
            val x = MARGIN + IMAGE_SCALE + 10 // Position to the right of the texture with a small margin
            val y = MARGIN + IMAGE_SCALE - 5 // Slightly higher to align with the texture
            g.drawString(stackSize, x, y)
        }
    }
}