package com.flansmod.common.driveables;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ArmorInvisible {
	@Mod.EventBusSubscriber
	public static class EventHandler {

		private static boolean renderArmor = true;

		@SubscribeEvent
		public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
			if (!renderArmor) {
				return;
			}

			EntityPlayer player = event.getEntityPlayer();
			RenderPlayer renderer = event.getRenderer();
			if (player != null && renderer != null) {
				for (ItemStack stack : player.getArmorInventoryList()) {
					if (!stack.isEmpty()) {
						stack.setCount(0);
					}
				}
			}
		}
		public static void setArmor(EntityPlayer player, boolean invisible) {
			renderArmor = invisible;
		}
	}
}
