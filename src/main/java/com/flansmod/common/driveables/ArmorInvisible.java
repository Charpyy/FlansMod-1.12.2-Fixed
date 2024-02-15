package com.flansmod.common.driveables;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
public class ArmorInvisible {
	public static void setArmorVisibility(EntityPlayer player, boolean invisible) {
		for (ItemStack armorPiece : player.inventory.armorInventory) {
			if (armorPiece.isEmpty()) continue;
			NBTTagCompound tagCompound = armorPiece.getTagCompound();
			if (tagCompound == null) {
				tagCompound = new NBTTagCompound();
				armorPiece.setTagCompound(tagCompound);
			}
			tagCompound.setBoolean("HideFlags", invisible);
		}
	}

	public static void setArmor(EntityPlayer player, boolean invisible) {
		if (invisible) {
			setArmorVisibility(player, true);
			player.sendMessage(new TextComponentString("Armure rendue invisible"));
		} else {
			setArmorVisibility(player, false);
			player.sendMessage(new TextComponentString("Armure rendue visible"));
		}
	}
}
