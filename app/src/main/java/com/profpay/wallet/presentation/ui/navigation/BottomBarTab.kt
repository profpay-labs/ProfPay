package com.profpay.wallet.presentation.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.profpay.wallet.R

enum class BottomBarTab(
    val route: Route,
    val title: String,
    @DrawableRes val iconRes: Int,
) {
    SmartContracts(
        route = Route.SmartContractList,
        title = "Smart",
        iconRes = R.drawable.icon_smart,
    ),
    Profile(
        route = Route.Wallet,
        title = "Wallet",
        iconRes = R.drawable.icon_wallet,
    ),
    Settings(
        route = Route.SettingsGraph,
        title = "Settings",
        iconRes = R.drawable.icon_settings,
    );

    @Composable
    fun icon(): ImageVector = ImageVector.vectorResource(id = iconRes)
}
