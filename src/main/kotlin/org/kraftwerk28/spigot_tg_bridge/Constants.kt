package org.kraftwerk28.spigot_tg_bridge

object Constants {
    const val configFilename = "config.yml"
    const val advancementsFilename = "advancements.json"
    object WARN {
        const val noConfigWarning = "No config file found! Writing default config to config.yml."
        const val noToken = "Bot token must be defined."
        const val noUsername = "Bot username must be defined."
    }
    object INFO {
        const val reloading = "Reloading..."
        const val reloadComplete = "Reload completed."
    }
    object TIMES_OF_DAY {
        const val day = "\uD83C\uDFDE Day"
        const val sunset = "\uD83C\uDF06 Sunset"
        const val night = "\uD83C\uDF03 Night"
        const val sunrise = "\uD83C\uDF05 Sunrise"
    }
    const val USERNAME_PLACEHOLDER = "%username%"
    const val MESSAGE_TEXT_PLACEHOLDER = "%message%"
    const val CHAT_TITLE_PLACEHOLDER = "%chat%"
    object COMMANDS {
        const val PLUGIN_RELOAD = "tgbridge_reload"
    }
    object COMMAND_DESC {
        const val timeDesc = "Get time on server"
        const val onlineDesc = "Get players online"
        const val chatIDDesc = "Get current chat id"
    }
}
