package eu.pablob.paper_telegram_bridge

import java.io.InputStream
import org.bukkit.inventory.meta.PotionMeta
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import org.bukkit.inventory.ItemStack

fun loadItemTexture(itemName: String, javaClass: Class<out Any>): BufferedImage? {
    val texturePath = "/textures/minecraft__$itemName.png"
    val inputStream: InputStream? = javaClass.getResourceAsStream(texturePath)
    return try {
        inputStream?.let {
            ImageIO.read(it)
        }
    } catch (e: Exception) {
        null
    }
}

fun loadAwkwardPotionTexture(javaClass: Class<out Any>): BufferedImage? {
    val awkwardPotionPath = "potion__awkward"
    return loadItemTexture(awkwardPotionPath, javaClass)
}

fun loadPotionTexture(item: ItemStack, javaClass: Class<out Any>): BufferedImage? {
    val meta = item.itemMeta
    if (meta is PotionMeta) {
        val potionType = meta.basePotionType?.name?.lowercase()
        val potionTexturePath = "potion__$potionType"
        return loadItemTexture(potionTexturePath, javaClass)
    }
    return null
}

fun loadMapTexture(javaClass: Class<out Any>): BufferedImage? {
    return loadItemTexture("map", javaClass)
}

fun loadImage(path: String, javaClass: Class<out Any>): BufferedImage? {
    val inputStream: InputStream? = javaClass.getResourceAsStream(path)
    return try {
        inputStream?.let {
            ImageIO.read(it)
        }
    } catch (e: Exception) {
        null
    }
}
