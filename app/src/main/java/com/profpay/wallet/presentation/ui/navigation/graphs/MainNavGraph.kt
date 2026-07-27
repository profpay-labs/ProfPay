package com.profpay.wallet.presentation.ui.navigation.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.profpay.core.network.monitor.NetworkMonitor
import com.profpay.wallet.presentation.ui.navigation.MainScreen
import com.profpay.wallet.presentation.ui.navigation.Route

//import com.profpay.wallet.ui.app.navigation.HomeScreen

@RequiresApi(Build.VERSION_CODES.S)
fun NavGraphBuilder.mainNavGraph(
    rootNavController: NavController,
    networkMonitor: NetworkMonitor,
) {
    composable<Route.MainGraph> {
        MainScreen()
    }
}
