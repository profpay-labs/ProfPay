package com.profpay.wallet

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import com.bugfender.sdk.Bugfender
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import me.pushy.sdk.Pushy
import androidx.work.Configuration
import com.profpay.wallet.workers.PendingTransactionCleanupWorker
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        initializePushNotifications()
        initializeWorkers()
        initializeSentry()
        initializeBugfender()
    }

    private fun initializeSentry() {
        SentryAndroid.init(this) { options ->
            options.tracesSampleRate = 1.0
            options.isEnableAutoSessionTracking = true
            options.isEnableUserInteractionTracing = true
            options.isEnableUserInteractionBreadcrumbs = true
            options.isAttachScreenshot = false
        }
    }

    private fun initializePushNotifications() {
        Pushy.listen(this)
        Pushy.toggleForegroundService(true, this)
    }

    private fun initializeWorkers() {
        PendingTransactionCleanupWorker.schedule(this)
    }

    private fun initializeBugfender() {
        Bugfender.init(this, BuildConfig.BUGFENDER_API_KEY, BuildConfig.DEBUG, true)
        Bugfender.enableCrashReporting()
        Bugfender.enableUIEventLogging(this)
        Bugfender.enableLogcatLogging()
    }
}
