package eu.pablob.paper_telegram_bridge

import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class Whitelist (private val plugin: Plugin, private val server: Server = Bukkit.getServer()) {
    /**
     * Toggles the whitelist status of a player by name.
     * Returns true if added to whitelist, false if removed.
     * This must be called from the main thread to avoid IllegalStateException.
     */
    suspend fun toggleWhitelist(name: String): Boolean {
        val result = CompletableDeferred<Boolean>()
        
        // Schedule the whitelist operation to run on the main thread
        server.scheduler.runTask(plugin, Runnable {
            try {
                val player = server.getOfflinePlayer(name)
                val newStatus = !player.isWhitelisted
                player.isWhitelisted = newStatus
                result.complete(newStatus)
            } catch (e: Exception) {
                result.completeExceptionally(e)
            }
        })
        
        return result.await()
    }

    /**
     * Returns all names on the whitelist (may include names of players who never joined).
     */
    suspend fun getWhitelistedNames(): List<String> {
        val result = CompletableDeferred<List<String>>()
        
        // Schedule the whitelist read operation to run on the main thread
        server.scheduler.runTask(plugin, Runnable {
            try {
                val names = server.whitelistedPlayers.mapNotNull(OfflinePlayer::getName)
                result.complete(names)
            } catch (e: Exception) {
                result.completeExceptionally(e)
            }
        })
        
        return result.await()
    }
}