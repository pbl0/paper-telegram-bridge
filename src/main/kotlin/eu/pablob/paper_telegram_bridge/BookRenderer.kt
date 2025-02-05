package eu.pablob.paper_telegram_bridge

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import java.awt.FontMetrics
import java.security.MessageDigest

class BookRenderer(private val plugin: AsyncJavaPlugin) {
    private val marginLeft = 40
    private val marginTop = 60
    private val pageWidth = 350
    private val pageHeight = 432

    fun renderBookToFile(book: ItemStack): Pair<File?, String?> {
        val meta = book.itemMeta as? BookMeta ?: return Pair(null, null)
        val serializer = PlainTextComponentSerializer.plainText()
        val pages = meta.pages().map { serializer.serialize(it) }

        val outputFolder = File(plugin.dataFolder, "inv/books/${generateBookHash(book)}")
        outputFolder.mkdirs()

        val pageFiles = mutableListOf<File>()
        var caption: String? = null
        if (meta.title != null){
            caption = "${meta.title} by ${meta.author}"
        }


        for ((index, page) in pages.withIndex()) {
            val image = BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()

            renderBackground(g, image)
            drawBookText(g, page, index + 1, pages.size)

            g.dispose()
            val outputFile = File(outputFolder, "page${index + 1}.png")
            ImageIO.write(image, "png", outputFile)
            pageFiles.add(outputFile)
        }
        return Pair(outputFolder, caption)
    }

    private fun drawBookText(g: Graphics2D, page: String, pageIndex: Int, totalPages: Int) {
        g.color = Color.BLACK
        g.font = MinecraftFontLoader.getFont(16f) // Updated font size

        val fm: FontMetrics = g.fontMetrics
        val maxLineWidth = pageWidth - marginLeft * 2  // Text width should fit within margins
        val lineHeight = fm.height  // Dynamic line height

        // Draw page number on top right
        g.drawString("Page $pageIndex of $totalPages", pageWidth - 150, marginTop)

        var y = marginTop + lineHeight  // Start below the page number
        for (line in page.split("\n")) {
            var currentLine = ""
            // TODO: Handle words that are too long and get overflown.
            for (word in line.split(" ")) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (fm.stringWidth(testLine) > maxLineWidth) {
                    // Draw the current line and move to the next one
                    g.drawString(currentLine.trim(), marginLeft, y)
                    y += lineHeight

                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }

            // Draw remaining part of the line
            if (currentLine.isNotEmpty()) {
                g.drawString(currentLine.trim(), marginLeft, y)
                y += lineHeight
            }
        }
    }



    private fun renderBackground(g: Graphics2D, image: BufferedImage) {
        val bookBackground = loadImage("/book.png", this.javaClass)
        g.color = Color.BLACK  // Set background to black
        g.fillRect(0, 0, image.width, image.height)
        g.drawImage(bookBackground, 0, 0, null)
    }

    private fun generateBookHash(book: ItemStack): String {
        val meta = book.itemMeta as? BookMeta ?: throw IllegalArgumentException("Item is not a book")

        // Extract book data
        val title = meta.title ?: "Untitled"
        val author = meta.author ?: "Unknown"
        val serializer = PlainTextComponentSerializer.plainText()
        val pages = meta.pages().map { serializer.serialize(it) }

        // Combine title, author, and pages into a single string
        val bookData = "$title:$author:$pages"

        // Generate an MD5 hash of the book data
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(bookData.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}