package dev.lapis256.mekanism_empowered.core.extension

import mekanism.api.math.FloatingLong


operator fun FloatingLong.plusAssign(value: FloatingLong) {
    plusEqual(value)
}
operator fun FloatingLong.plusAssign(value: Long) {
    plusEqual(value)
}
operator fun FloatingLong.plus(value: FloatingLong): FloatingLong = add(value)
operator fun FloatingLong.plus(value: Long): FloatingLong = add(value)
operator fun FloatingLong.plus(value: Double): FloatingLong = add(value)


operator fun FloatingLong.minusAssign(value: FloatingLong) {
    minusEqual(value)
}
operator fun FloatingLong.minusAssign(value: Long) {
    minusEqual(value)
}
operator fun FloatingLong.minus(value: FloatingLong): FloatingLong = subtract(value)
operator fun FloatingLong.minus(value: Long): FloatingLong = subtract(value)
operator fun FloatingLong.minus(value: Double): FloatingLong = subtract(value)


operator fun FloatingLong.timesAssign(value: FloatingLong) {
    timesEqual(value)
}
operator fun FloatingLong.timesAssign(value: Long) {
    timesEqual(value)
}
operator fun FloatingLong.times(value: FloatingLong): FloatingLong = multiply(value)
operator fun FloatingLong.times(value: Long): FloatingLong = multiply(value)
operator fun FloatingLong.times(value: Double): FloatingLong = multiply(value)


operator fun FloatingLong.divAssign(value: FloatingLong) {
    divideEquals(value)
}
operator fun FloatingLong.divAssign(value: Long) {
    divideEquals(value)
}
operator fun FloatingLong.div(value: FloatingLong): FloatingLong = divide(value)
operator fun FloatingLong.div(value: Long): FloatingLong = divide(value)
operator fun FloatingLong.div(value: Double): FloatingLong = divide(value)
