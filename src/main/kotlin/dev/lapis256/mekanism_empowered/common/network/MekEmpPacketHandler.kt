package dev.lapis256.mekanism_empowered.common.network

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import dev.lapis256.mekanism_empowered.common.network.to_server.configuration_update.PacketSideInserterData
import mekanism.common.lib.Version
import mekanism.common.network.BasePacketHandler
import net.minecraftforge.network.simple.SimpleChannel


class MekEmpPacketHandler(versionNumber: Version) : BasePacketHandler() {
    private val netHandler = createChannel(MekanismEmpoweredAPI.rl(MekanismEmpoweredAPI.MOD_ID), versionNumber)

    override fun getChannel(): SimpleChannel = netHandler

    override fun initialize() {
        registerClientToServer(PacketSideInserterData::class.java, PacketSideInserterData::decode)
    }
}
