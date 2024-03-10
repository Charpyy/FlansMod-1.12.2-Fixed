package com.flansmod.common.network;
import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.EntityDriveable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class PacketCameraShake extends PacketBase {

	public PacketCameraShake() {
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
	}

	@Override
	public void handleServerSide(EntityPlayerMP playerEntity) {
		FlansMod.log("Received server side packet Camera Shake revoie ton code bozo");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(EntityPlayer clientPlayer) {
		FlansMod.log("Camera shake");
		FlansModClient.cameraShake = 5;
	}
}
