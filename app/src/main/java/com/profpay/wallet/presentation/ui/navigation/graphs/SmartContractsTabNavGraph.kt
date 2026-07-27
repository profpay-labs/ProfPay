package com.profpay.wallet.presentation.ui.navigation.graphs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.profpay.wallet.presentation.ui.navigation.Route
import com.profpay.wallet.presentation.ui.screens.SmartListScreen

@RequiresApi(Build.VERSION_CODES.S)
fun NavGraphBuilder.smartContractsTabNavGraph(navController: NavController) {
    composable<Route.SmartContractList> {
        SmartListScreen(
            goToSystemTRX = {
                navController.navigate(Route.SystemTrx)
            },
        )
    }
}
