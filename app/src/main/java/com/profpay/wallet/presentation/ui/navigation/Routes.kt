// Обновить файл: app/src/main/java/com/profpay/wallet/ui/navigation/Routes.kt

package com.profpay.wallet.presentation.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 */
sealed interface Route {

    @Serializable
    data object Splash : Route

    @Serializable
    data object Welcome : Route

    @Serializable
    data object PinCreate : Route

    @Serializable
    data object PinUnlock : Route

    @Serializable
    data object BlockedApp : Route

    @Serializable
    data object NoNetwork : Route

    @Serializable
    data object OnboardingGraph : Route

    @Serializable
    data object OnboardingChoice : Route

    @Serializable
    data object CreateWallet : Route

    @Serializable
    data object RecoverWallet : Route

    @Serializable
    data object SeedPhraseConfirmation : Route

    @Serializable
    data object WalletCreated : Route

    @Serializable
    data object WalletRecoveryProgress : Route

    @Serializable
    data object MainGraph : Route

    @Serializable
    data object Wallet : Route  // Wallet tab start

    @Serializable
    data object SmartContractList : Route  // Smart contracts tab start

    @Serializable
    data object SettingsGraph : Route  // Settings tab navigation graph

    @Serializable
    data object Settings : Route  // Settings tab start

    @Serializable
    data object WalletDetails : Route  // Main wallet screen

    @Serializable
    data object WalletSots : Route

    @Serializable
    data object WalletArchivalSots : Route

    @Serializable
    data class WalletAddress(
        val address: String,
        val tokenName: String,
    ) : Route

    @Serializable
    data class WalletSend(
        val addressId: Long,
        val tokenName: String,
    ) : Route

    @Serializable
    data class WalletReceive(
        val address: String,
    ) : Route

    @Serializable
    data object WalletSystem : Route

    @Serializable
    data object SystemTrx : Route

    @Serializable
    data object CentralAddressTransactionHistory : Route

    @Serializable
    data object TransactionDetails : Route

    @Serializable
    data object WalletSystemOnboardingGraph : Route

    @Serializable
    data object WalletSystemOnboardingChoice : Route

    @Serializable
    data object CreateWalletFromWalletSystem : Route

    @Serializable
    data object RecoverWalletFromWalletSystem : Route

    @Serializable
    data object SeedPhraseConfirmationFromWalletSystem : Route

    @Serializable
    data object WalletAddedFromWalletSystem : Route

    @Serializable
    data object RecoveringWalletAddingFromWalletSystem : Route

    @Serializable
    data object SettingsNotifications : Route

    @Serializable
    data object SettingsSecurity : Route

    @Serializable
    data object SettingsAccount : Route

    @Serializable
    data object SettingsAml : Route

    @Serializable
    data object SettingsPinChange : Route

    @Serializable
    data object SettingsPinCreate : Route
}
