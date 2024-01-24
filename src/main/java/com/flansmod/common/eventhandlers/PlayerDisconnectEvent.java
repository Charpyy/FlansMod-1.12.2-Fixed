package com.flansmod.common.eventhandlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerDisconnectEvent {
	public PlayerDisconnectEvent() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void OnPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
		EntityPlayerMP player = (EntityPlayerMP) event.player;

	}
}
