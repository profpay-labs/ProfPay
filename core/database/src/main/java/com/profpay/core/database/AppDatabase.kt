package com.profpay.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.profpay.core.database.converters.BigIntegerConverter
import com.profpay.core.database.converters.DateConverter
import com.profpay.core.database.dao.*
import com.profpay.core.database.dao.wallet.*
import com.profpay.core.database.entities.*
import com.profpay.core.database.entities.wallet.*
import java.math.BigInteger

// Создание Базы Данных
@Database(
    version = 1,
    entities = [
        AddressEntity::class,
        TokenEntity::class,
        WalletProfileEntity::class,

        ProfileEntity::class,
        TransactionEntity::class,
        SettingsEntity::class,
        CentralAddressEntity::class,
        SmartContractEntity::class,
        ExchangeRatesEntity::class,
        TradingInsightsEntity::class,
        PendingTransactionEntity::class,
        PendingAmlTransactionEntity::class,
    ],
    exportSchema = true,
)
@TypeConverters(DateConverter::class, BigIntegerConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAddressDao(): AddressDao

    abstract fun getTokenDao(): TokenDao

    abstract fun getWalletProfileDao(): WalletProfileDao

    abstract fun getProfileDao(): ProfileDao

    abstract fun getSettingsDao(): SettingsDao

    abstract fun getTransactionsDao(): TransactionsDao

    abstract fun getCentralAddressDao(): CentralAddressDao

    abstract fun getSmartContractDao(): SmartContractDao

    abstract fun getExchangeRatesDao(): ExchangeRatesDao

    abstract fun getTradingInsightsDao(): TradingInsightsDao

    abstract fun getPendingTransactionDao(): PendingTransactionDao

    abstract fun getPendingAmlTransactionDao(): PendingAmlTransactionDao

    @Transaction
    open suspend fun insertWalletWithAddressesAndTokens(
        walletProfile: WalletProfileEntity,
        addresses: List<AddressEntity>,
        defaultTokenNames: List<String>,
    ): Long {
        val number = getWalletProfileDao().getCountRecords() + 1
        val entityWithName = walletProfile.copy(name = "Wallet $number")

        val walletId = getWalletProfileDao().insert(entityWithName)

        addresses.forEach { address ->
            val addressId = getAddressDao().insert(address.copy(walletId = walletId))
            val tokenEntities = defaultTokenNames.map { tokenName ->
                TokenEntity(addressId = addressId, tokenName = tokenName, balance = BigInteger.ZERO)
            }
            getTokenDao().insertAll(tokenEntities)
        }
        return walletId
    }

    companion object {
        const val DATABASE_NAME = "room_crypto_wallet.db"
    }
}
