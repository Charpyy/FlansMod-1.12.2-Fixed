package com.flansmod.common.eventhandlers;

import com.flansmod.common.driveables.EntityDriveable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class DriveableDeathByHandEvent extends Event {

	private EntityDriveable driveable;

	private EntityPlayer player;

	private ItemStack driveableStack;


	public DriveableDeathByHandEvent(EntityDriveable driveable, EntityPlayer player, ItemStack driveableStack) {
		this.driveable = driveable;
		this.player = player;
		this.driveableStack = driveableStack;
	}

	public EntityDriveable getEntity() {
		return driveable;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

	public ItemStack getItemStack() {
		return driveableStack;
	}

}
