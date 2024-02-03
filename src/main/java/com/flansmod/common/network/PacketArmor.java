package com.flansmod.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEquipment;

public class PacketArmor {
	public static void disableEquipmentPackets(EntityPlayer player) {
		((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY));
		((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.HEAD, ItemStack.EMPTY));
		((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.CHEST, ItemStack.EMPTY));
		((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.LEGS, ItemStack.EMPTY));
		((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityEquipment(player.getEntityId(), EntityEquipmentSlot.FEET, ItemStack.EMPTY));
	}
}
