package com.flansmod.client.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerArmorBase.class)
public class ArmorRender {
	@Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
	private void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo callbackInfo) {
		if (entitylivingbaseIn.isInvisible()) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
			System.out.println("INVISIBLE");
			callbackInfo.cancel();
		}
	}
}
