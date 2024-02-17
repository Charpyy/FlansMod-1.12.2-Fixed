package com.flansmod.common.driveables;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;


public class ArmorInvisible {
	public static class EventHandler {

		private static final Set<EntityLivingBase> invisibleEntities = new HashSet<>();

		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onRenderPlayer(RenderLivingEvent.Pre<EntityLivingBase> event) {
			EntityLivingBase entity = event.getEntity();
			if (entity != null && invisibleEntities.contains(entity)) {
				event.setCanceled(true);
			}
		}

		public static void setArmor(EntityLivingBase entity, boolean invisible) {
			if (invisible) {
				invisibleEntities.add(entity);
			} else {
				invisibleEntities.remove(entity);
			}
		}
	}
}
