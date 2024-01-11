package com.flansmod.common.guns;

import com.flansmod.common.eventhandlers.GunFiredEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

public class SprintShoot {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void GunFiredEvent(GunFiredEvent event) {
		if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
			boolean sprinting = Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isSprinting();
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (sprinting) {
				event.setCanceled(true);

			}
		}
	}

}
