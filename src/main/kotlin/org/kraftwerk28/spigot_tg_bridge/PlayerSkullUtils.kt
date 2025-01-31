package org.kraftwerk28.spigot_tg_bridge

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class PlayerSkullUtils {

    companion object {
        /**
         * Creates an ItemStack of the player's skull.
         *
         * @param player The player whose skull will be created.
         * @return The player's skull as an ItemStack.
         */
        fun getPlayerSkull(player: Player): ItemStack {
            val skull = ItemStack(Material.PLAYER_HEAD)
            val meta = skull.itemMeta as SkullMeta
            meta.owningPlayer = player
            skull.itemMeta = meta
            return skull
        }

        /**
         * Gives the player their own skull.
         *
         * @param player The player to give the skull to.
         */
        fun givePlayerSkull(player: Player) {
            val skull = getPlayerSkull(player)
            player.inventory.addItem(skull)
            player.sendMessage("You received your skull!")
        }
    }
}