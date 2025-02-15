package eu.pablob.paper_telegram_bridge

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody


interface TgApiService {
    @GET("deleteWebhook")
    suspend fun deleteWebhook(
        @Query("drop_pending_updates") dropPendingUpdates: Boolean
    ): TgResponse<Boolean>

    @GET("sendMessage?parse_mode=HTML")
    suspend fun sendMessage(
        @Query("chat_id") chatId: Long,
        @Query("text") text: String,
        @Query("reply_to_message_id") replyToMessageId: Long? = null,
        @Query("disable_notification") disableNotification: Boolean? = null,
    ): TgResponse<Message>

    @GET("getUpdates")
    suspend fun getUpdates(
        @Query("offset") offset: Long,
        @Query("limit") limit: Int = 100,
        @Query("timeout") timeout: Int = 0,
    ): TgResponse<List<Update>>

    @GET("getMe")
    suspend fun getMe(): TgResponse<User>

    @POST("setMyCommands")
    suspend fun setMyCommands(
        @Body commands: SetMyCommands,
    ): TgResponse<Boolean>

    @Multipart
    @POST("sendPhoto?parse_mode=HTML")
    suspend fun sendPhoto(
        @Part("chat_id") chatId: Long,
        @Part photo: MultipartBody.Part,
        @Part("caption") caption: RequestBody? = null,
        @Part("reply_to_message_id") replyToMessageId: Long? = null,
        @Part("disable_notification") disableNotification: Boolean? = null,
        @Part("reply_markup") replyMarkup: RequestBody? = null
    ): TgResponse<Message>

    @POST("answerCallbackQuery")
    suspend fun answerCallbackQuery(
        @Query("callback_query_id") callbackQueryId: String, // Unique identifier for the callback query
        @Query("text") text: String? = null, // Optional: Text to show to the user
        @Query("show_alert") showAlert: Boolean = false, // Optional: Whether to show an alert instead of a notification
        @Query("url") url: String? = null // Optional: URL to open (for games)
    ): TgResponse<Boolean> // Telegram API returns a boolean indicating success

    @Multipart
    @POST("editMessageMedia")
    suspend fun editMessageMedia(
        @Part("chat_id") chatId: Long,
        @Part("message_id") messageId: Long,
        @Part("media") media: RequestBody, // JSON describing the media
        @Part photo: MultipartBody.Part, // The actual image
        @Part("reply_markup") replyMarkup: RequestBody? = null // Inline keyboard
    ): TgResponse<Message>
}
