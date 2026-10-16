@file:Suppress("unused")

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible


enum class Order {
    NONE, BEFORE, AFTER;
}

enum class Side {
    CLIENT, SERVER, BOTH;
}

sealed class VersionRange(val versionRange: String) {
    override fun toString(): String = versionRange
}

class Equal(version: String) : VersionRange("[$version]")
class GreaterThan(version: String) : VersionRange("($version,)")
class GreaterThanOrEqual(version: String) : VersionRange("[$version,)")
class LessThan(version: String) : VersionRange("(,$version)")
class LessThanOrEqual(version: String) : VersionRange("(,$version]")
class NotEqual(version: String) : VersionRange("(,$version),($version,)")

class RangeExclusive(min: String, max: String) : VersionRange("($min,$max)")
class RangeInclusive(min: String, max: String) : VersionRange("[$min,$max]")
class RangeInclusiveMin(min: String, max: String) : VersionRange("[$min,$max)")
class RangeInclusiveMax(min: String, max: String) : VersionRange("($min,$max]")

class Or(vararg ranges: VersionRange) : VersionRange(ranges.joinToString(",") { it.versionRange })

fun String.eq() = Equal(this)
fun String.gt() = GreaterThan(this)
fun String.gte() = GreaterThanOrEqual(this)
fun String.lt() = LessThan(this)
fun String.lte() = LessThanOrEqual(this)
fun String.neq() = NotEqual(this)

infix operator fun String.rangeTo(other: String) = RangeInclusive(this, other)
infix operator fun String.rangeUntil(other: String) = RangeInclusiveMin(this, other)
infix fun String.rangeToExclusive(other: String) = RangeExclusive(this, other)
infix fun String.rangeToMax(other: String) = RangeInclusiveMax(this, other)

fun Provider<String>.eq() = Equal(this.get())
fun Provider<String>.gt() = GreaterThan(this.get())
fun Provider<String>.gte() = GreaterThanOrEqual(this.get())
fun Provider<String>.lt() = LessThan(this.get())
fun Provider<String>.lte() = LessThanOrEqual(this.get())
fun Provider<String>.neq() = NotEqual(this.get())

infix operator fun Provider<String>.rangeTo(other: Provider<String>) = RangeInclusive(this.get(), other.get())
infix operator fun Provider<String>.rangeUntil(other: Provider<String>) = RangeInclusiveMin(this.get(), other.get())
infix fun Provider<String>.rangeToExclusive(other: Provider<String>) = RangeExclusive(this.get(), other.get())
infix fun Provider<String>.rangeToMax(other: Provider<String>) = RangeInclusiveMax(this.get(), other.get())

infix operator fun Provider<String>.rangeTo(other: String) = RangeInclusive(this.get(), other)
infix operator fun Provider<String>.rangeUntil(other: String) = RangeInclusiveMin(this.get(), other)
infix fun Provider<String>.rangeToExclusive(other: String) = RangeExclusive(this.get(), other)
infix fun Provider<String>.rangeToMax(other: String) = RangeInclusiveMax(this.get(), other)

data class ModDep(
    val id: String,
    val versionRange: VersionRange,
    val mandatory: Boolean = true,
    val ordering: Order = Order.NONE,
    val side: Side = Side.BOTH
) {
    companion object {
        fun optional(
            id: String,
            versionRange: VersionRange,
            ordering: Order = Order.NONE,
            side: Side = Side.BOTH
        ) = ModDep(id, versionRange, false, ordering, side)

        fun incompatible(id: String) =
            ModDep(id, "999.999.999-INCOMPATIBLE".eq(), false, Order.NONE, Side.BOTH)
    }
}

fun buildDeps(
    vararg deps: ModDep,
    modId: String = Constants.Mod.ID,
): String {
    return deps.joinToString(separator = "\n") { (id, versionRange, mandatory, ordering, side) ->
        """
            [[dependencies.$modId]]
            modId = "$id"
            versionRange = "$versionRange"
            mandatory = $mandatory
            ordering = "$ordering"
            side = "$side"
        """.trimIndent()
    }
}

fun extractVersionSegments(versionString: String, numberOfSegments: Int = 1) =
    versionString.split(".").take(numberOfSegments).joinToString(".")

fun extractVersionSegments(version: Provider<String>, numberOfSegments: Int = 1) =
    extractVersionSegments(version.get(), numberOfSegments)

fun DependencyHandler.variantOf(dependency: Provider<MinimalExternalModuleDependency>, classifier: String) =
    variantOf(dependency) { classifier(classifier) }

fun DependencyHandler.variantOf(dependency: ProviderConvertible<MinimalExternalModuleDependency>, classifier: String) =
    variantOf(dependency) { classifier(classifier) }
