package dev.lapis256.mekanism_empowered.core.common.util

import java.util.function.IntSupplier
import java.util.function.LongSupplier


class IntLongSupplier(private val value: IntSupplier) : IntSupplier, LongSupplier {
    override fun getAsInt(): Int = value.asInt
    override fun getAsLong(): Long = value.asInt.toLong()
}
