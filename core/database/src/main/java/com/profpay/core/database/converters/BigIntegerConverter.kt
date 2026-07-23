package com.profpay.core.database.converters

import androidx.room.TypeConverter
import java.math.BigInteger

class BigIntegerConverter {
    @TypeConverter
    fun fromBigInteger(value: BigInteger?): String? = value?.toString()

    @TypeConverter
    fun toBigInteger(value: String?): BigInteger? = value?.let { BigInteger(it) }
}
