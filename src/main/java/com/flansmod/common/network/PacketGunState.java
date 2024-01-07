package com.flansmod.common.network;

import com.flansmod.client.FlansModClient;
import com.flansmod.common.FlansMod;
import com.flansmod.common.eventhandlers.ServerTickEvent;
import com.flansmod.common.guns.AttachmentType;
import com.flansmod.common.guns.ItemGun;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;


//Packets must be registered in .network.PacketHandler!
public class PacketGunState extends PacketBase 
{
	
	boolean isScoped;
	public PacketGunState()
	{

	}
	
	public PacketGunState(boolean isScoped)
	{
		this.isScoped = isScoped;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		data.writeBoolean(isScoped);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		isScoped = data.readBoolean();
	}

	@Override
	public void handleServerSide(EntityPlayerMP player) 
	{
		//FlansMod.log("Gun state packet received + isScoped; " + isScoped);
		//player.getHeldItem();
		if(player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemGun)
		{
			ItemGun itemGun = (ItemGun)player.getHeldItem(EnumHand.MAIN_HAND).getItem();
			ItemStack itemstack = player.getHeldItem(EnumHand.MAIN_HAND);
			AttachmentType scope = itemGun.type.getScope(itemstack);
			
			//Apply night vision while scoped if gun.allowNightVision = True
			if(itemGun.type.allowNightVision && isScoped)
			{
				player.addPotionEffect(new PotionEffect(Potion.getPotionById(16), 1200, 0));
				ServerTickEvent.nightVisionPlayers.add(player);
			}
			else if(itemGun.type.allowNightVision && !isScoped)
			{
				player.removePotionEffect(Potion.getPotionById(16));
				ServerTickEvent.nightVisionPlayers.remove(player);
			}
			//Apply night vision while scoped if attachment.hasNightVision = True
			if(scope != null && scope.hasNightVision && isScoped)
			{
				player.addPotionEffect(new PotionEffect(Potion.getPotionById(16), 1200, 0));
				ServerTickEvent.nightVisionPlayers.add(player);
			}
			else if(scope != null && scope.hasNightVision && !isScoped)
			{
				player.removePotionEffect(Potion.getPotionById(16));
				ServerTickEvent.nightVisionPlayers.remove(player);
			}
			itemGun.isScoped = isScoped;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(EntityPlayer clientPlayer) 
	{

	}
}
