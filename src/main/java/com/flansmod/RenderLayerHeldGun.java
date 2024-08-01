package com.flansmod;

import com.flansmod.client.ClientProxy;
import com.flansmod.client.model.CustomItemRenderType;
import com.flansmod.client.model.RenderGun;
import com.flansmod.common.guns.ItemGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class RenderLayerHeldGun extends LayerHeldItem {
	public RenderLayerHeldGun(RenderLivingBase<?> p_i46115_1_) {
		super(p_i46115_1_);
	}

	public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		Minecraft mc = Minecraft.getMinecraft();

		boolean flag = entitylivingbaseIn.getPrimaryHand() == EnumHandSide.RIGHT;
		ItemStack itemstack = flag ? entitylivingbaseIn.getHeldItemOffhand() : entitylivingbaseIn.getHeldItemMainhand();
		ItemStack itemstack1 = flag ? entitylivingbaseIn.getHeldItemMainhand() : entitylivingbaseIn.getHeldItemOffhand();

		if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
			renderGun(entitylivingbaseIn, mc, itemstack, EnumHand.MAIN_HAND, EnumHandSide.LEFT, partialTicks);

			renderGun(entitylivingbaseIn, mc, itemstack1, EnumHand.MAIN_HAND, EnumHandSide.RIGHT, partialTicks);

		}
	}

	public void renderGun(EntityLivingBase entitylivingbaseIn, Minecraft mc, ItemStack stack, EnumHand hand, EnumHandSide side, float partialTicks){
		GlStateManager.pushMatrix();

		if (entitylivingbaseIn.isSneaking()) {
			GlStateManager.translate(0.0F, 0.2F, 0.0F);
		}

		if (stack.getItem() instanceof ItemGun){

			this.translateToHand(side);
			GlStateManager.translate(-0.06, 0.38, 0.05);
			if (side == EnumHandSide.LEFT){
				GlStateManager.translate(0.1,0,0);
			}
			ClientProxy.gunRenderer.renderItem(CustomItemRenderType.EQUIPPED, hand, stack, mc.world, entitylivingbaseIn);
		}
		GlStateManager.popMatrix();
	}
}
