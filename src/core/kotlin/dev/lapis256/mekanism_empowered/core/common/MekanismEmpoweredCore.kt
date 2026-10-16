package dev.lapis256.mekanism_empowered.core.common

import dev.lapis256.mekanism_empowered.core.api.MekanismEmpoweredCoreAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 已并入主模组（单一 mod id）。原先作为独立 @Mod 入口的初始化现在放在
 * [dev.lapis256.mekanism_empowered.common.MekanismEmpowered] 里，这里只留共用日志器。
 */
object MekanismEmpoweredCore {
    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger(MekanismEmpoweredCoreAPI.MOD_ID)
}
