package com.profpay.wallet.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class PushReceiver : BroadcastReceiver(), CoroutineScope {
    @Inject
    lateinit var handler: PushEventHandler
    var mapper: PushEventMapper = PushEventMapper()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onReceive(context: Context, intent: Intent) {
        val event = mapper.map(intent)

        // Если push - это наш кастомный EVENT, то обрабатываем его
        if (event != null) {
            launch {
                handler.handle(context, event, intent)
            }
            return
        }

//        // Иначе показываем локальное уведомление
//        val notificationTitle = intent.getStringExtra("title") ?: "Уведомление"
//        val notificationText = intent.getStringExtra("message") ?: "Сообщение"
//        showNotification(context, notificationTitle, notificationText)
    }
}
