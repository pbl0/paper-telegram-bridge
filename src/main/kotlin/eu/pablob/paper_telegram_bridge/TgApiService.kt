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
    @POST("sendPhoto")
    suspend fun sendPhoto(
        @Part("chat_id") chatId: Long,
        @Part photo: MultipartBody.Part,
        @Part("caption") caption: RequestBody? = null,
        @Part("reply_to_message_id") replyToMessageId: Long? = null,
        @Part("disable_notification") disableNotification: Boolean? = null
    ): TgResponse<Message>
}
