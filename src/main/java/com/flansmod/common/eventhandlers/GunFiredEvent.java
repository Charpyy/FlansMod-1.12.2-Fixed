package com.flansmod.common.eventhandlers;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class GunFiredEvent extends Event {

	private Entity shooter;

	public GunFiredEvent(Entity shooter) {
		this.shooter = shooter;
	}

	public Entity getShooter() {
		return shooter;
	}

}
