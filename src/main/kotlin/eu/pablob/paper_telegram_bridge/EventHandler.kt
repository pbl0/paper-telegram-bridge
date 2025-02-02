package eu.pablob.paper_telegram_bridge

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import java.io.InputStream

import org.json.JSONArray
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

import eu.pablob.paper_telegram_bridge.Constants as C


class EventHandler(
    private val plugin: Plugin,
    private val config: Configuration,
    private val tgBot: TgBot,
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        if (!config.logFromMCtoTG || event.isCancelled) return

        val player = event.player
        val message = PlainTextComponentSerializer.plainText().serialize(event.message())
        // Inventory
        if (message.contains("[")) {
            getLogInventory(message, player)
        } else {
            sendMessage(message, player.name)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!config.logJoinLeave) return
        val player = event.player
        val hasPermission = player.hasPermission("tg-bridge.silentjoinleave")
        if (hasPermission) return
        val username = player.playerProfile.name.toString()
        val text = config.joinString.replace("%username%", username)
        sendMessage(text)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (!config.logJoinLeave) return
        val player = event.player
        val hasPermission = player.hasPermission("tg-bridge.silentjoinleave")
        if (hasPermission) return
        val username = player.playerProfile.name.toString().fullEscape()
        val text = config.leaveString.replace("%username%", username)
        sendMessage(text)
    }

    @EventHandler
    fun onPlayerDied(event: PlayerDeathEvent) {
        if (!config.logDeath) return
        val hasPermission = event.entity.hasPermission("tg-bridge.silentjoinleave")
        if (hasPermission) return
        event.deathMessage().toString().let {
            val username = event.entity.playerProfile.name.toString().fullEscape()
            val text = it.replace(username, "<i>$username</i>")
            sendMessage(text)
        }
    }

    @EventHandler
    fun onPlayerAsleep(event: PlayerBedEnterEvent) {
        if (!config.logPlayerAsleep) return
        val player = event.player
        val hasPermission = player.hasPermission("tg-bridge.silentjoinleave")
        if (hasPermission) return
        if (event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK) return
        val text = "<i>${player.playerProfile.name.toString()}</i> fell asleep."
        sendMessage(text)
    }

    private fun sendMessage(text: String, username: String? = null) = plugin.launch {
        tgBot.sendMessageToTelegram(text, username)
    }


    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (!config.logPlayerAdvancement) return

        val advancementKey = event.advancement.key
        // Filter out recipes advancements
        if (advancementKey.toString().startsWith("minecraft:recipes")) return

        // TODO: Surely there is a better way to do this...
        val allAdvancements = loadAchievementsFromResource()
        val displayTitle = getDisplayTitleByKey(advancementKey.key, allAdvancements) as String
        val username = event.player.playerProfile.name.toString().fullEscape()

        val message = config.advancementString.replace("%username%", username).replace("%advancement%", displayTitle)
        sendMessage(message)
    }


    private fun loadAchievementsFromResource(): List<Advancement> {
        val resourcePath = "/${C.ADVANCEMENTS_FILENAME}"
        val inputStream: InputStream? = this::class.java.getResourceAsStream(resourcePath)
        val json = inputStream!!.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)
        val advancements = mutableListOf<Advancement>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val displayTitle = jsonObject.getString("displayTitle")
            val key = jsonObject.getString("key")
            advancements.add(Advancement(displayTitle, key))
        }
        return advancements
    }

    private fun getDisplayTitleByKey(key: String, advancements: List<Advancement>): String? {
        return advancements.find { it.key == key }?.displayTitle
    }

    private fun getLogInventory(message: String, player: Player) {
        if (!config.logInventory) return

        val lowerMessage = message.lowercase()

        val playerName = player.playerProfile.name

        fun formatCaption(label: String, msgBefore: String, msgAfter: String) =
            "$playerName: $msgBefore[$playerName’s $label]$msgAfter"

        when {
            "[inv]" in lowerMessage -> plugin.launch {
                val (userMessageBefore, userMessageAfter) = userMessageBeforeAfter(message, "[inv]")
                val image = InventoryRenderer.renderInventoryToFile(player.inventory, "inventory.png")
                tgBot.sendPhotoToTelegram(image, formatCaption("Inventory", userMessageBefore, userMessageAfter))
            }

            "[ender]" in lowerMessage -> plugin.launch {
                val (userMessageBefore, userMessageAfter) = userMessageBeforeAfter(message, "[inv]")
                val image = EnderChestRenderer.renderEnderChestToFile(player.enderChest, "ender.png")
                tgBot.sendPhotoToTelegram(image, formatCaption("Ender Chest", userMessageBefore, userMessageAfter))
            }

            "[item]" in lowerMessage -> plugin.launch {
                val item = player.inventory.itemInMainHand
                val (image, itemName) = ItemRenderer.renderItemToFile(item, "item.png")
                val formattedName = itemName.substringBefore('(').trim()
                val amountSuffix = if (item.amount > 1) " x ${item.amount}" else ""
                val (userMessageBefore, userMessageAfter) = userMessageBeforeAfter(message, "[item]")
                tgBot.sendPhotoToTelegram(
                    image, "$playerName: $userMessageBefore[$formattedName$amountSuffix]$userMessageAfter"
                )
            }

            else -> {
                sendMessage(message, playerName)

            } // Default case if no tags are found
        }
    }

    private fun userMessageBeforeAfter(message: String, tag: String): Pair<String, String> {
        var userMessageBefore = message.substringBefore(tag)
        var userMessageAfter = message.substringAfter(tag)

        // Handle InteractiveChat plugin formatting.
        if ("<" in message && ">" in message) {
            val newTag = message.substringAfter("<").substringBefore(">")
            userMessageBefore = message.substringBefore("<$newTag>")
            userMessageAfter = message.substringAfter("<$newTag>")
        }

        return Pair(userMessageBefore, userMessageAfter)
    }

}
