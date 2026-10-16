package dev.lapis256.mekanism_empowered.common.network.to_server.configuration_update

import dev.lapis256.mekanism_empowered.extension.inserterConfig
import mekanism.api.RelativeSide
import mekanism.api.security.ISecurityUtils
import mekanism.common.network.IMekanismPacket
import mekanism.common.tile.prefab.TileEntityConfigurableMachine
import mekanism.common.util.WorldUtils
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent


data class PacketSideInserterData(private val pos: BlockPos, private val side: RelativeSide) : IMekanismPacket {
    override fun handle(context: NetworkEvent.Context) {
        val player = context.sender ?: return
        val blockEntity = WorldUtils.getTileEntity(player.level(), pos) ?: return
        val tile = blockEntity as? TileEntityConfigurableMachine ?: return

        // 和 Mekanism 自己的机器包一样过所有权 / 安全等级检查，
        // 否则任何客户端都能翻转别人机器的灌注器侧面开关并写进存档。
        // 1.20.1 的入口是 ISecurityUtils.INSTANCE.canAccess（1.21.1 上换成了 IBlockSecurityUtils）。
        if (!ISecurityUtils.INSTANCE.canAccess(player, blockEntity)) {
            return
        }

        tile.inserterConfig.toggleSideConfig(side)
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeBlockPos(pos)
        buffer.writeEnum(side)
    }

    companion object {
        fun decode(buffer: FriendlyByteBuf) =
            PacketSideInserterData(buffer.readBlockPos(), buffer.readEnum(RelativeSide::class.java))
    }
}
