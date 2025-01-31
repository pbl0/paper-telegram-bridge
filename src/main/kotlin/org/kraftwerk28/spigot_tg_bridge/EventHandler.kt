package org.kraftwerk28.spigot_tg_bridge

import java.io.InputStream

import org.json.JSONArray
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent

import org.kraftwerk28.spigot_tg_bridge.Constants as C
import org.kraftwerk28.spigot_tg_bridge.InventoryRenderer


class EventHandler(
    private val plugin: Plugin,
    private val config: Configuration,
    private val tgBot: TgBot,
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (!config.logFromMCtoTG || event.isCancelled) return
        val player = event.player
        val message = event.message
    
        if (message.contains("[inv]")) {
            plugin.launch {
                val inventoryImage = InventoryRenderer.renderInventoryToFile(player.inventory, "inventory.png")
                val caption = "${player.displayName}: [${player.displayName}’s Inventory]"
                tgBot.sendPhotoToTelegram(inventoryImage, caption)
            }
        } else if(message.contains("[item]")){
            plugin.launch {
                val item = player.inventory.itemInMainHand
                val (itemImage, itemName) = ItemRenderer.renderItemToFile(item, "item.png")
                val name = itemName.substringBefore('(').trim()
                var amountSuffix = ""
                if (item.amount > 1){
                    amountSuffix = " x ${item.amount}"
                }
                tgBot.sendPhotoToTelegram(itemImage, "${player.displayName}: [$name$amountSuffix]")
            }
        } else {
            sendMessage(message, player.displayName)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!config.logJoinLeave) return
        val player = event.player
        val hasPermission = player.hasPermission("tg-bridge.silentjoinleave")
        if (hasPermission) return
        val username = player.displayName.fullEscape()
        val text = config.joinString.replace("%username%", username)
        sendMessage(text)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (!config.logJoinLeave) return
        val player = event.player
        val hasPermission = player.hasPermission("tg-bridge.silentjoinleave")
        if (hasPermission) return
        val username = player.displayName.fullEscape()
        val text = config.leaveString.replace("%username%", username)
        sendMessage(text)
    }

    @EventHandler
    fun onPlayerDied(event: PlayerDeathEvent) {
        if (!config.logDeath) return
        val hasPermission = event.entity.hasPermission("tg-bridge.silentjoinleave")
        if (hasPermission) return
        event.deathMessage?.let {
            val username = event.entity.displayName.fullEscape()
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
        if (event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK)
            return
        val text = "<i>${player.displayName}</i> fell asleep."
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

        // ! Surely there is a better way to do this...
        val allAdvancements = loadAchievementsFromResource("/${C.advancementsFilename}")
        val displayTitle = getDisplayTitleByKey(advancementKey.key, allAdvancements) as String
        val username = event.player.displayName.fullEscape()

        val message = config.advancementString.replace("%username%", username).replace("%advancement%", displayTitle)
        sendMessage(message)
    }


    private fun loadAchievementsFromResource(resourcePath: String): List<Advancement> {
        val inputStream: InputStream = this::class.java.getResourceAsStream(resourcePath)
        val json = inputStream.bufferedReader().use { it.readText() }
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

    private fun getDisplayTitleByKey(key: String, advancementes: List<Advancement>): String? {
        return advancementes.find { it.key == key }?.displayTitle
    }
}
