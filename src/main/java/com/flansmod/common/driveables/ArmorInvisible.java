package com.flansmod.common.driveables;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.RenderPlayerEvent.Post;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber
public class ArmorInvisible {
	private static boolean invisible = false;

	private final LoadingCache<EntityPlayer, CachedInventory> cache;

	public ArmorInvisible() {
		this.cache = CacheBuilder.newBuilder().expireAfterAccess(60L, TimeUnit.SECONDS).build(new CacheLoader());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void handleCanceledEvent(Pre event) {
		if (event.isCanceled()) {
			CachedInventory cachedInv = this.cache.getUnchecked(event.getPlayer());
			NonNullList<ItemStack> cachedArmor = cachedInv.stacks;
			NonNullList<ItemStack> armor = event.getPlayer().inventory.armorInventory;
			if (armor != null && armor.size() == cachedArmor.size()) {
				if (cachedInv.state != 0) {
					for (int i = 0; i < cachedArmor.size(); ++i) {
						armor.set(i, cachedArmor.get(i));
					}
					cachedInv.state = 0;
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleEvent(Post event) {
		CachedInventory cachedInv = this.cache.getUnchecked(event.getPlayer());
		NonNullList<ItemStack> cachedArmor = cachedInv.stacks;
		NonNullList<ItemStack> armor = event.getPlayer().inventory.armorInventory;
		if (armor != null && armor.size() == cachedArmor.size()) {
			if (cachedInv.state != 0) {
				for (int i = 0; i < cachedArmor.size(); ++i) {
					armor.set(i, cachedArmor.get(i));
				}
				cachedInv.state = 0;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
	public void handleEvent(Pre event) {
		CachedInventory cachedInv = this.cache.getUnchecked(event.getPlayer());
		NonNullList<ItemStack> cachedArmor = cachedInv.stacks;
		EntityPlayerSP player = event.getPlayer();
		NonNullList<ItemStack> armor = player.inventory.armorInventory;
		if (armor != null && armor.size() == cachedArmor.size()) {
			int i;
			if (cachedInv.state != 0) {
				for (i = 0; i < cachedArmor.size(); ++i) {
					armor.set(i, cachedArmor.get(i));
				}
				cachedInv.state = 0;
			}

			for (i = 0; i < cachedArmor.size(); ++i) {
				cachedArmor.set(i, armor.get(i));
			}

			cachedInv.state = 1;
			if (invisible) {
				for (i = 0; i < cachedArmor.size(); ++i) {
					armor.set(i, ItemStack.EMPTY);
				}
			}
		}
	}

	private static class CachedInventory {
		NonNullList<ItemStack> stacks;
		int state;

		CachedInventory(NonNullList<ItemStack> stacks, int state) {
			this.stacks = stacks;
			this.state = state;
		}
	}

	private static class CacheLoader implements com.google.common.cache.CacheLoader<EntityPlayer, CachedInventory> {
		@Override
		public CachedInventory load(EntityPlayer key) {
			return new CachedInventory(key.inventory.armorInventory, 0);
		}
	}

	public static void setInvisible(boolean invisible) {
		ArmorInvisible.invisible = invisible;
	}

	// This method should be called to toggle invisibility
	public static void toggleInvisibility() {
		ArmorInvisible.invisible = !ArmorInvisible.invisible;
	}
}
