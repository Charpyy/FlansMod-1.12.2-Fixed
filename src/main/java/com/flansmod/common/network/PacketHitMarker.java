package com.flansmod.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;

public class PacketHitMarker extends PacketBase
{
	public float penAmount = 1F;
    public boolean headshot = false;
    public boolean explosionHit = false;
	
	public PacketHitMarker()
	{

	}
	
	public PacketHitMarker(boolean head, float pen, boolean explosion)
    {
        headshot = head;
        penAmount = pen;
        explosionHit = explosion;
    }
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		data.writeBoolean(headshot);
        data.writeFloat(penAmount);
        data.writeBoolean(explosionHit);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		headshot = data.readBoolean();
        penAmount = data.readFloat();
        explosionHit = data.readBoolean();
	}
	
	@Override
	public void handleServerSide(EntityPlayerMP playerEntity)
	{
		FlansMod.log.warn("Received PacketHitMarker packet on server. Disregarding.");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(EntityPlayer clientPlayer)
	{
		FlansModClient.hitMarker = true;
        FlansModClient.hitMarkerPenAmount = penAmount;
        FlansModClient.hitMarkerHeadshot = headshot;
        FlansModClient.hitMarkerExplosion = explosionHit;
		FlansModClient.addHitMarker();
	}
}
