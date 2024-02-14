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

		public static List<ItemStack> retrieveArmorItems(EntityPlayer player) {
			List<ItemStack> armorItems = new ArrayList<>();
			NonNullList<ItemStack> armorInventory = player.inventory.armorInventory;

			for (int i = 0; i < armorInventory.size(); i++) {
				//armorItems.add(armorInventory.get(i));
				armorItems.add(player.inventory.armorInventory.get(i));
				armorInventory.set(i, ItemStack.EMPTY);
				player.sendMessage(new TextComponentString(armorItems.toString()));
			}

			return armorItems;
		}

		public static void restoreArmorItems(EntityPlayer player, List<ItemStack> armorItems) {
			NonNullList<ItemStack> armorInventory = player.inventory.armorInventory;
			if (armorItems != null) {
				for (int i = 0; i < armorInventory.size() && i < armorItems.size(); i++) {
					armorInventory.set(i, armorItems.get(i));
				}
			}
		}

		public static void setArmor(EntityPlayer player, boolean invisible) {
			List<ItemStack> armorItems = null;
			if (invisible) {
				armorItems = retrieveArmorItems(player);
			} else {
				restoreArmorItems(player, armorItems);
			}
		}
	}
