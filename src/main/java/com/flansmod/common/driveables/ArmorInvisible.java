package com.flansmod.common.driveables;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ArmorInvisible {
	private static boolean invisible = false;
	private static EntityPlayer targetPlayer = null;
	public static void init() {
		MinecraftForge.EVENT_BUS.register(ArmorInvisible.class);
	}
	public static void setArmor(EntityPlayer player, boolean value) {
		targetPlayer = player;
		invisible = value;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onRenderLiving(RenderLivingEvent.Pre<EntityPlayer> event) {
		if (invisible && targetPlayer != null && event.getEntity() == targetPlayer) {
			EntityLivingBase player1 = event.getEntity();
			if (player1 instanceof EntityPlayer) {
				EntityPlayer player =(EntityPlayer) event.getEntity();
				GlStateManager.pushMatrix();
				GlStateManager.colorMask(false, false, false, false);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRenderLiving(RenderLivingEvent.Post<EntityPlayer> event) {
		if (invisible && targetPlayer != null && event.getEntity() == targetPlayer) {
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.popMatrix();
		}
	}
}
