package com.flansmod.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.ItemGun;

public class PacketGunFire extends PacketBase
{
	private EnumHand hand;
	public float yaw;
    public float pitch;
	
	public PacketGunFire() {
	}
	
	public PacketGunFire(EnumHand hand, float yaw, float pitch)
	{
		this.hand = hand;
		this.yaw = yaw;
        this.pitch = pitch;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		//TODO Proper packet enum encoding
		data.writeInt(EnumHand.MAIN_HAND.equals(hand)?0:1);
		data.writeFloat(yaw);
        data.writeFloat(pitch);
	}
	
	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		//TODO Proper packet enum encoding
		hand = data.readInt()==0?EnumHand.MAIN_HAND:EnumHand.OFF_HAND;
		yaw = data.readFloat();
        pitch = data.readFloat();
	}
	
	@Override
	public void handleServerSide(EntityPlayerMP playerEntity)
	{
		ItemStack itemstack = playerEntity.getHeldItem(hand);
		//TODO can itemstack be null?
		Item item = itemstack.getItem();
		if (item instanceof ItemGun) {
			float bkYaw = playerEntity.rotationYaw;
            float bkPitch = playerEntity.rotationPitch;
            playerEntity.rotationYaw = yaw;
            playerEntity.rotationPitch = pitch;
			ItemGun gun = (ItemGun) item;
			gun.shootServer(hand, playerEntity, itemstack);
			playerEntity.rotationYaw = bkYaw;
            playerEntity.rotationPitch = bkPitch;
			
		} else {
			FlansMod.log.warn("Received invalid PacketGunFire. Item in hand is not an instance of ItemGun");
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(EntityPlayer clientPlayer)
	{
		FlansMod.log.warn("Received gun button packet on client. Skipping.");
	}
}
