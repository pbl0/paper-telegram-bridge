package eu.pablob.paper_telegram_bridge

import net.kyori.adventure.text.Component
import org.bukkit.event.HandlerList
import java.lang.Exception
import eu.pablob.paper_telegram_bridge.Constants as C

class Plugin : AsyncJavaPlugin() {
    private var tgBot: TgBot? = null
    private var eventHandler: EventHandler? = null
    private var config: Configuration? = null

    override fun onLoad() {
        // Set Java AWT to headless mode. This property applies JVM-wide.
        // It prevents java.awt.AWTError on servers without a graphical environment.
        if (System.getProperty("java.awt.headless") == null) {
            System.setProperty("java.awt.headless", "true")
        }
    }

    override suspend fun onEnableAsync() {
        try {
            launch {
                config = Configuration(this).also {
                    initializeWithConfig(it)
                }
            }
        } catch (e: Exception) {
            // Configuration file is missing or incomplete
            logger.warning(e.message)
        }
    }

    private suspend fun initializeWithConfig(config: Configuration) {
        if (!config.isEnabled) return

        tgBot?.run { stop() }
        tgBot = TgBot(this, config).also { bot ->
            bot.startPolling()
            eventHandler = EventHandler(this, config, bot).also {
                server.pluginManager.registerEvents(it, this)
            }
        }

        getCommand(C.COMMANDS.PLUGIN_RELOAD)?.run {
            setExecutor(CommandHandler(this@Plugin))
        }
        config.serverStartMessage?.let {
            tgBot?.sendMessageToTelegram(it)
        }
    }

    override suspend fun onDisableAsync() {
        config?.let fn@{ config ->
            if (!config.isEnabled)
                return@fn
            config.serverStopMessage?.let {
                tgBot?.sendMessageToTelegram(it)
            }
            eventHandler?.let { HandlerList.unregisterAll(it) }
            tgBot?.run { stop() }
            tgBot = null
        }
    }

    fun sendMessageToMinecraft(
        text: String,
        username: String? = null,
        chatTitle: String? = null,
    ) = config?.run {
        minecraftFormat
            .replace(C.MESSAGE_TEXT_PLACEHOLDER, text.escapeEmoji())
            .run {
                username?.let {
                    replace(C.USERNAME_PLACEHOLDER, it.escapeEmoji())
                } ?: this
            }
            .run {
                chatTitle?.let {
                    replace(C.CHAT_TITLE_PLACEHOLDER, it)
                } ?: this
            }
            .also { server.broadcast(Component.text(it)) }
    }

    suspend fun reload() {
        config = Configuration(this).also { config ->
            if (!config.isEnabled) return
            logger.info(C.INFO.RELOADING)
            eventHandler?.let { HandlerList.unregisterAll(it) }
            tgBot?.run { stop() }
            tgBot = TgBot(this, config).also { bot ->
                bot.startPolling()
                eventHandler = EventHandler(this, config, bot).also {
                    server.pluginManager.registerEvents(it, this)
                }
            }
            logger.info(C.INFO.RELOAD_COMPLETE)
        }
    }
}
