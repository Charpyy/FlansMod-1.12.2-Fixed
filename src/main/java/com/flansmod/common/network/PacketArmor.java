package com.flansmod.common.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;

import net.minecraft.entity.Entity;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.inventory.EntityEquipmentSlot;

public class PacketArmor {

	public static void disableEquipmentPackets(Entity player) {
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			playerMP.connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY));
			playerMP.connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.HEAD, ItemStack.EMPTY));
			playerMP.connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.CHEST, ItemStack.EMPTY));
			playerMP.connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.LEGS, ItemStack.EMPTY));
			playerMP.connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.FEET, ItemStack.EMPTY));
		} else if (player instanceof EntityPlayerSP) {
			EntityPlayerSP playerSP = (EntityPlayerSP) player;
			playerSP.inventory.offHandInventory.set(0, ItemStack.EMPTY);
		}
	}
}
