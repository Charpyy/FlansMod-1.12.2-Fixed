package com.flansmod.common.driveables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
	public class ArmorInvisible {
		public static List<ItemStack> armorItems = new ArrayList<>();

		public static List<ItemStack> retrieveArmorItems(EntityPlayer player) {
			//if (armorItems == null) {
			//	armorItems = null;
			//}
			armorItems = new ArrayList<>();
			NonNullList<ItemStack> armorInventory = player.inventory.armorInventory;
			armorItems.add(player.inventory.armorInventory.get(0));
			armorItems.add(player.inventory.armorInventory.get(1));
			armorItems.add(player.inventory.armorInventory.get(2));
			armorItems.add(player.inventory.armorInventory.get(3));
			armorInventory.set(0, ItemStack.EMPTY);
			player.sendMessage(new TextComponentString(armorItems.get(0).toString()));
			armorInventory.set(1, ItemStack.EMPTY);
			player.sendMessage(new TextComponentString(armorItems.get(1).toString()));
			armorInventory.set(2, ItemStack.EMPTY);
			player.sendMessage(new TextComponentString(armorItems.get(2).toString()));
			armorInventory.set(3, ItemStack.EMPTY);
			player.sendMessage(new TextComponentString(armorItems.get(3).toString()));
			player.sendMessage(new TextComponentString("remove armor"));
			player.sendMessage(new TextComponentString(armorItems.toString()));
			return armorItems;
		}

		public static void restoreArmorItems(EntityPlayer player, List<ItemStack> armorItems) {
			if (player == null || player.inventory == null || player.inventory.armorInventory == null || armorItems == null) {
				return;
			}
			NonNullList<ItemStack> armorInventory = player.inventory.armorInventory;
			for (int i = 0; i < 4; i++) {
				if (i < armorItems.size() && armorItems.get(i) != null) {
					armorInventory.set(i, armorItems.get(i));
				}
			}
			player.sendMessage(new TextComponentString("set armor"));
			player.sendMessage(new TextComponentString(armorItems.toString()));
			armorItems = null;
		}

		public static void setArmor(EntityPlayer player, boolean invisible) {
			if (invisible) {
				armorItems = retrieveArmorItems(player);
			} else {
				restoreArmorItems(player, armorItems);
			}
		}
	}
