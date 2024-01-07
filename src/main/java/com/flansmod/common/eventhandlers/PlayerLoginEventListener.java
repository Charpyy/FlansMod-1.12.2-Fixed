package com.flansmod.common.eventhandlers;

import com.flansmod.common.FlansMod;
import com.flansmod.common.network.PacketModConfig;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;

public class PlayerLoginEventListener {

	public PlayerLoginEventListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void OnPlayerLogin(PlayerLoggedInEvent event) {
		FlansMod.packetHandler.sendTo(new PacketModConfig(), (EntityPlayerMP) event.player);
	}
}
