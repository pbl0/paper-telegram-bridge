package eu.pablob.paper_telegram_bridge


import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Graphics2D
import java.io.ByteArrayOutputStream
import java.util.*

class ItemRenderer {
    private val width = 250
    private val imageScale = 48
    private val margin = 12
    private val backgroundColor = "#210939"
    private val borderColor = "#1A0B1A"
    private val enchantmentColor = "#A7A7A7"

    fun renderItemToFile(item: ItemStack): Pair<ByteArray, String> {

        // Default rendering for non-map items
        val texture = loadTexture(item)
        val height = calculateDynamicHeight(item)
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        drawBackground(g, height)
        drawTexture(g, texture)
        val itemName = drawItemName(g, item)

        // Calculate textYOffset for enchantments and durability
        var textYOffset = imageScale + margin + 50 // Start below the item name

        // Draw enchantments and update textYOffset
        textYOffset = drawEnchantments(g, item, textYOffset)

        // Draw durability below enchantments (or item name if no enchantments)
        drawDurability(g, item, textYOffset)

        // Draw stack size
        drawStackSize(g, item)

        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        val imageBytes = outputStream.toByteArray()
        outputStream.close()
        return Pair(imageBytes, itemName)
    }

    private fun loadTexture(item: ItemStack): BufferedImage? {
        return when (val itemName = item.type.name.lowercase()) {
            "potion", "splash_potion", "lingering_potion" ->
                loadPotionTexture(item, this.javaClass) ?: loadAwkwardPotionTexture(this.javaClass)

            "filled_map" -> loadMapTexture(this.javaClass)
            else -> loadItemTexture(itemName, this.javaClass)
        }
    }


    private fun calculateDynamicHeight(item: ItemStack): Int {
        var height = imageScale + margin * 2 + 30 // Base height for texture and name

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

        return height + margin // Add bottom margin
    }

    private fun drawBackground(g: Graphics2D, height: Int) {
        g.color = Color.decode(backgroundColor)
        g.fillRect(0, 0, width, height)
        g.color = Color.decode(borderColor)
        g.fillRect(4, 4, width - 8, height - 8)
    }

    private fun drawTexture(g: Graphics2D, texture: BufferedImage?) {
        if (texture == null) {
            g.color = Color.GRAY
            g.fillRect(margin, margin, imageScale, imageScale)
        } else {
            g.drawImage(texture, margin, margin, imageScale, imageScale, null)
        }
    }

    private fun drawItemName(g: Graphics2D, item: ItemStack): String {
        val displayName =
            PlainTextComponentSerializer.plainText().serialize(item.displayName()).replace("[", "").replace("]", "")
        val fullName = getItemName(item, displayName)
        val nameColor = determineNameColor(item)
        g.font = MinecraftFontLoader.getFont(16f)
        g.color = nameColor
        g.drawString(fullName, margin, imageScale + margin + 30)
        return fullName
    }

    private fun getItemName(item: ItemStack, displayName: String?): String {
        val itemTypeName = item.type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
        return if (!displayName.isNullOrEmpty()) "$displayName" else itemTypeName
    }

    private fun determineNameColor(item: ItemStack): Color {
        return when {
            item.itemMeta?.hasEnchants() == true || (item.itemMeta is EnchantmentStorageMeta && (item.itemMeta as EnchantmentStorageMeta).storedEnchants.isNotEmpty()) -> Color.CYAN
            item.type.name.contains("totem", ignoreCase = true) || item.type.name.contains(
                "book",
                ignoreCase = true
            ) -> Color.YELLOW

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
            g.color = Color.decode(enchantmentColor)
            var currentYOffset = textYOffset
            enchantments.forEach { (enchantment, level) ->
                g.drawString(
                    "${
                        enchantment.key.key.replace('_', ' ')
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    } $level", margin, currentYOffset
                )
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
            val currentDurability =
                item.type.maxDurability - (item.itemMeta as org.bukkit.inventory.meta.Damageable).damage
            g.drawString("Durability: $currentDurability/${item.type.maxDurability}", margin, textYOffset)
        }
    }

    private fun drawStackSize(g: Graphics2D, item: ItemStack) {
        if (item.amount > 1) {
            g.font = MinecraftFontLoader.getFont(20f)
            g.color = Color.WHITE
            val stackSize = "x ${item.amount}"
            // val textWidth = g.fontMetrics.stringWidth(stackSize)
            val x = margin + imageScale + 10 // Position to the right of the texture with a small margin
            val y = margin + imageScale - 5 // Slightly higher to align with the texture
            g.drawString(stackSize, x, y)
        }
    }
}