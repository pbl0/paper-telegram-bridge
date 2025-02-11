package eu.pablob.paper_telegram_bridge

object Constants {
    const val CONFIG_FILENAME = "config.yml"
    object WARN {
        const val NO_CONFIG_WARNING = "No config file found! Writing default config to config.yml."
        const val NO_TOKEN = "Bot token must be defined."
    }

    object INFO {
        const val RELOADING = "Reloading..."
        const val RELOAD_COMPLETE = "Reload completed."
    }

    object TimesOfDay {
        const val DAY = "\uD83C\uDFDE Day"
        const val SUNSET = "\uD83C\uDF06 Sunset"
        const val NIGHT = "\uD83C\uDF03 Night"
        const val SUNRISE = "\uD83C\uDF05 Sunrise"
    }

    const val USERNAME_PLACEHOLDER = "%username%"
    const val MESSAGE_TEXT_PLACEHOLDER = "%message%"
    const val CHAT_TITLE_PLACEHOLDER = "%chat%"

    object COMMANDS {
        const val PLUGIN_RELOAD = "tgbridge_reload"
    }

    object CommandDesc {
        const val TIME_DESC = "Get time on server"
        const val ONLINE_DESC = "Get players online"
        const val CHAT_ID_DESC = "Get current chat id"
    }
}
