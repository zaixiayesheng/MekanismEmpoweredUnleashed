package dev.lapis256.mekanism_empowered.common.network.to_server.configuration_update

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import dev.lapis256.mekanism_empowered.extension.inserterConfig
import io.netty.buffer.ByteBuf
import mekanism.api.RelativeSide
import mekanism.api.security.IBlockSecurityUtils
import mekanism.common.network.IMekanismPacket
import mekanism.common.tile.prefab.TileEntityConfigurableMachine
import mekanism.common.util.WorldUtils
import net.minecraft.core.BlockPos
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.network.handling.IPayloadContext


data class PacketSideInserterData(private val pos: BlockPos, private val side: RelativeSide) : IMekanismPacket {
    companion object {
        val TYPE = CustomPacketPayload.Type<PacketSideInserterData>(MekanismEmpoweredAPI.rl("inserter_data"))

        val STREAM_CODEC: StreamCodec<ByteBuf, PacketSideInserterData> =
            StreamCodec.composite<ByteBuf, PacketSideInserterData, BlockPos, RelativeSide>(
                BlockPos.STREAM_CODEC, PacketSideInserterData::pos,
                RelativeSide.STREAM_CODEC, PacketSideInserterData::side
            ) { pos, side -> PacketSideInserterData(pos, side) }
    }

    override fun handle(context: IPayloadContext) {
        val player = context.player()
        val blockEntity = WorldUtils.getTileEntity(player.level(), pos) ?: return
        val tile = blockEntity as? TileEntityConfigurableMachine ?: return

        // 和 Mekanism 自己的机器包一样过所有权/安全等级检查，
        // 否则任何客户端都能翻转别人机器的灌注器侧面开关并写进存档。
        if (!IBlockSecurityUtils.INSTANCE.canAccess(player, player.level(), pos, blockEntity)) {
            return
        }

        tile.inserterConfig.toggleSideConfig(side)
    }

    override fun type() = TYPE
}
