package com.profpay.wallet.bridge.viewmodel.welcoming

import androidx.lifecycle.ViewModel
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WelcomingViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {

}
