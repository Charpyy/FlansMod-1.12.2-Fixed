package com.flansmod.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.handlers.FlansModResourceHandler;
import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.AttachmentType;
import com.flansmod.common.guns.EnumFireMode;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.IScope;
import com.flansmod.common.guns.ItemBullet;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.Paintjob;
import com.flansmod.common.paintjob.PaintableType;
import com.flansmod.common.vector.Vector3f;


public class RenderGun implements CustomItemRenderer
{
	private static TextureManager renderEngine;

	public static float smoothing;
	public static boolean bindTextures = true;

	@Override
	public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data)
	{
		//Avoid any broken cases by returning
		if(!(item.getItem() instanceof ItemGun))
			return;

		GunType gunType = ((ItemGun)item.getItem()).GetType();
		if(gunType == null)
			return;

		ModelGun model = gunType.model;
		if(model == null)
			return;

		//Render main hand gun
		GunAnimations animations =
				(type == CustomItemRenderType.ENTITY || type == CustomItemRenderType.INVENTORY)
						? new GunAnimations()
						: FlansModClient.getGunAnimations((EntityLivingBase)data[1], hand);
		renderGun(type, item, gunType, animations, hand, data);

	}

	//Render off-hand gun in 3rd person
	public void renderOffHandGun(EntityPlayer player, ItemStack offHandItemStack)
	{
		GunAnimations animations = FlansModClient.gunAnimationsLeft.get(player);
		if(animations == null)
		{
			animations = new GunAnimations();
			FlansModClient.gunAnimationsLeft.put(player, animations);
		}
		GunType offHandGunType = ((ItemGun)offHandItemStack.getItem()).GetType();
		renderGun(CustomItemRenderType.INVENTORY, offHandItemStack, offHandGunType, animations, EnumHand.OFF_HAND, player);
	}

	private void renderGun(CustomItemRenderType type, ItemStack item, GunType gunType, GunAnimations animations, EnumHand hand, Object... data)
	{
		//The model scale
		float f = 1F / 16F;
		ModelGun model = gunType.model;

		int flip = hand == EnumHand.OFF_HAND ? -1 : 1;

		GlStateManager.pushMatrix();
		{
			//Get the reload animation rotation
			float reloadRotate = 0F;

			//Setup transforms based on gun position
			switch(type)
			{
				case ENTITY:
				{
					//EntityItem entity = (EntityItem)data[1];
					//GlStateManager.rotate(entity.getAge() + (entity.getAge() == 0 ? 0 : smoothing), 0F, 1F, 0F);
					GlStateManager.translate(-0.45F + model.itemFrameOffset.x, -0.05F + model.itemFrameOffset.y, model.itemFrameOffset.z);
					break;
				}
				case INVENTORY:
				{
					GlStateManager.translate(model.itemFrameOffset.x, model.itemFrameOffset.y, model.itemFrameOffset.z);
					break;
				}
				case EQUIPPED:
				{
					if(hand == EnumHand.OFF_HAND)
					{
						GlStateManager.rotate(-70F, 1F, 0F, 0F);
						GlStateManager.rotate(48F, 0F, 0F, 1F);
						GlStateManager.rotate(105F, 0F, 1F, 0F);
						GlStateManager.translate(-0.1F, -0.22F, -0.15F);
					}
					else
					{
						GlStateManager.rotate(90F, 0F, 0F, 1F);
						GlStateManager.rotate(-90F, 1F, 0F, 0F);
						GlStateManager.translate(0.2F, 0.05F, -0F);
						GlStateManager.scale(1F, 1F, -1F);
					}
					GlStateManager.translate(model.thirdPersonOffset.x, model.thirdPersonOffset.y, model.thirdPersonOffset.z);
					/*
					if(animations.meleeAnimationProgress > 0 && animations.meleeAnimationProgress < gunType.meleePath.size())
					{
						Vector3f meleePos = gunType.meleePath.get(animations.meleeAnimationProgress);
						Vector3f nextMeleePos = animations.meleeAnimationProgress + 1 < gunType.meleePath.size() ? gunType.meleePath.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GlStateManager.translate(meleePos.x + (nextMeleePos.x - meleePos.x) * smoothing, meleePos.y + (nextMeleePos.y - meleePos.y) * smoothing, meleePos.z + (nextMeleePos.z - meleePos.z) * smoothing);
					}
					*/
					break;
				}
				case EQUIPPED_FIRST_PERSON:
				{
					IScope scope = gunType.getCurrentScope(item);
					if(FlansModClient.zoomProgress > 0.9F && scope.hasZoomOverlay() && !model.stillRenderGunWhenScopedOverlay)
					{
						GlStateManager.popMatrix();
						return;
					}
					float adsSwitch = FlansModClient.lastZoomProgress + (FlansModClient.zoomProgress - FlansModClient.lastZoomProgress) * smoothing;//0F;//((float)Math.sin((FlansMod.ticker) / 10F) + 1F) / 2F;
					adsSwitch *= model.adsEffectMultiplier;

					if(hand == EnumHand.OFF_HAND)
					{
						GlStateManager.rotate(45F, 0F, 1F, 0F);
						GlStateManager.translate(-1F, 0.675F, -1.8F);
					}
					else if (FlansModClient.zoomProgress + 0.1F > 0.9F && ItemGun.crouching && !animations.reloading)
					{
						GlStateManager.rotate(45F, 0F, 1F, 0F);
						GlStateManager.rotate(0F - 5F * adsSwitch, 0F, 0F, 1F);
						GlStateManager.translate(-1F, 0.675F + 0.180F * adsSwitch, -1F - 0.395F * adsSwitch);

						if(gunType.hasScopeOverlay && !model.stillRenderGunWhenScopedOverlay)
							GlStateManager.translate(-0.7F * adsSwitch, -0.12F * adsSwitch, -0.05F * adsSwitch);
						GlStateManager.rotate(4.5F * adsSwitch, 0F, 0F, 1F);
						// forward, up, sideways
						GlStateManager.translate(model.crouchZoom, -0.03F * adsSwitch, 0F);
					}
					else if (FlansModClient.zoomProgress + 0.1F < 0.2F && ItemGun.sprinting && !animations.reloading
							&& !ItemGun.shooting && model.fancyStance)
					{
						GlStateManager.rotate(45F + model.stanceRotate.x, 0F + model.stanceRotate.y, 1F, 0F);
						GlStateManager.rotate(0F - 5F * adsSwitch + model.stanceRotate.z, 0F, 0F, 1F);
						GlStateManager.translate(-1F, 0.675F + 0.180F * adsSwitch, -1F - 0.395F * adsSwitch);

						if (gunType.hasScopeOverlay && !model.stillRenderGunWhenScopedOverlay) {
							GlStateManager.translate(-0.7F * adsSwitch, -0.12F * adsSwitch, -0.05F * adsSwitch);
						}
						GlStateManager.rotate(4.5F * adsSwitch, 0F, 0F, 1F);
						// forward, up, sideways
						GlStateManager.translate(0.0F + model.stanceTranslate.x, -0.03F * adsSwitch + model.stanceTranslate.y, 0F + model.stanceTranslate.z);
					}
					else
					{
						GlStateManager.rotate(45F, 0F, 1F, 0F); // Angle nose down slightly -> angle nose up slightly
						GlStateManager.rotate(0F - 5F * adsSwitch, 0F, 0F, 1F); // Rotate Z nose inward
						GlStateManager.translate(-1F, 0.675F + 0.180F * adsSwitch, -1F - 0.395F * adsSwitch); // Slightly forward, slightly up -> more up, to left -> more towards middle

						if(gunType.hasScopeOverlay && !model.stillRenderGunWhenScopedOverlay)
							GlStateManager.translate(-0.7F * adsSwitch, -0.12F * adsSwitch, -0.05F * adsSwitch);
						// Rotate nose up
						GlStateManager.rotate(4.5F * adsSwitch, 0F, 0F, 1F);
						// Move gun down as ADS progresses
						GlStateManager.translate(-0.0F, -0.03F * adsSwitch, 0F);
					}

					if (animations.switchAnimationProgress > 0 && animations.switchAnimationLength > 0) {

						Vector3f pos1 = new Vector3f(0, -0.4f, 0);
						Vector3f pos2 = new Vector3f(0, 0, 0);
						Vector3f startAngles = new Vector3f(90, 30, -40);
						Vector3f endAngles = new Vector3f(0, 0, 0);
						float interp =
								(animations.switchAnimationProgress + smoothing) / animations.switchAnimationLength;

						GlStateManager.translate(pos2.x + (pos2.x - pos1.x) * interp,
								pos1.y + (pos2.y - pos1.y) * interp, pos1.z + (pos2.z - pos1.z) * interp);

						GlStateManager.rotate(startAngles.y + (endAngles.y - startAngles.y) * interp, 0f, 1f, 0f);
						GlStateManager.rotate(startAngles.z + (endAngles.z - startAngles.z) * interp, 0f, 0f, 1f);
					}

					if(animations.meleeAnimationProgress > 0 && animations.meleeAnimationProgress < gunType.meleePath.size())
					{
						Vector3f meleePos = gunType.meleePath.get(animations.meleeAnimationProgress);
						Vector3f nextMeleePos = animations.meleeAnimationProgress + 1 < gunType.meleePath.size() ? gunType.meleePath.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GlStateManager.translate(meleePos.x + (nextMeleePos.x - meleePos.x) * smoothing, meleePos.y + (nextMeleePos.y - meleePos.y) * smoothing, meleePos.z + (nextMeleePos.z - meleePos.z) * smoothing);
						Vector3f meleeAngles = gunType.meleePathAngles.get(animations.meleeAnimationProgress);
						Vector3f nextMeleeAngles = animations.meleeAnimationProgress + 1 < gunType.meleePathAngles.size() ? gunType.meleePathAngles.get(animations.meleeAnimationProgress + 1) : new Vector3f();
						GlStateManager.rotate(meleeAngles.y + (nextMeleeAngles.y - meleeAngles.y) * smoothing, 0F, 1F, 0F);
						GlStateManager.rotate(meleeAngles.z + (nextMeleeAngles.z - meleeAngles.z) * smoothing, 0F, 0F, 1F);
						GlStateManager.rotate(meleeAngles.x + (nextMeleeAngles.x - meleeAngles.x) * smoothing, 1F, 0F, 0F);
					}

					// Look at gun stuff
					float interp = animations.lookAtTimer + smoothing;
					interp /= animations.lookAtTimes[animations.lookAt.ordinal()];

					final Vector3f idlePos = new Vector3f(0.0f, 0.0f, 0.0f);
					final Vector3f look1Pos = new Vector3f(0.25f, 0.25f, 0.0f);
					final Vector3f look2Pos = new Vector3f(0.25f, 0.25f, -0.5f);
					final Vector3f idleAngles = new Vector3f(0.0f, 0.0f, 0.0f);
					final Vector3f look1Angles = new Vector3f(0.0f, 70.0f, 0.0f);
					final Vector3f look2Angles = new Vector3f(0.0f, -60.0f, 60.0f);
					Vector3f startPos, endPos, startAngles, endAngles;

					switch(animations.lookAt)
					{
						default:
						case NONE:
							startPos = endPos = idlePos;
							startAngles = endAngles = idleAngles;
							break;
						case LOOK1:
							startPos = endPos = look1Pos;
							startAngles = endAngles = look1Angles;
							break;
						case LOOK2:
							startPos = endPos = look2Pos;
							startAngles = endAngles = look2Angles;
							break;
						case TILT1:
							startPos = idlePos;
							startAngles = idleAngles;
							endPos = look1Pos;
							endAngles = look1Angles;
							break;
						case TILT2:
							startPos = look1Pos;
							startAngles = look1Angles;
							endPos = look2Pos;
							endAngles = look2Angles;
							break;
						case UNTILT:
							startPos = look2Pos;
							startAngles = look2Angles;
							endPos = idlePos;
							endAngles = idleAngles;
							break;
					}

					GlStateManager.rotate(startAngles.y + (endAngles.y - startAngles.y) * interp, 0f, 1f, 0f);
					GlStateManager.rotate(startAngles.z + (endAngles.z - startAngles.z) * interp, 0f, 0f, 1f);
					GlStateManager.translate(startPos.x + (endPos.x - startPos.x) * interp,
							startPos.y + (endPos.y - startPos.y) * interp,
							startPos.z + (endPos.z - startPos.z) * interp);


					//GlStateManager.rotate(70f, 0f, 1f, 0f);
					//GlStateManager.translate(0.25f, 0.25f, 0f);

					//GlStateManager.rotate(-60f, 0f, 1f, 0f);
					//GlStateManager.rotate(60f, 0f, 0f, 1f);
					//GlStateManager.translate(0.25f, 0.25f, -0.5f);

					GlStateManager.rotate(-animations.recoilAngle * (float)Math.sqrt(gunType.recoilPitch) * 1.5f, 0F, 0F, 1F);
					GlStateManager.translate(animations.recoilOffset.x, animations.recoilOffset.y, animations.recoilOffset.z);

					if(model.spinningCocking)
					{
						GlStateManager.translate(model.spinPoint.x, model.spinPoint.y, model.spinPoint.z);
						float pumped = (animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing);
						GlStateManager.rotate(pumped * 180F + 180F, 0F, 0F, 1F);
						GlStateManager.translate(-model.spinPoint.x, -model.spinPoint.y, -model.spinPoint.z);
					}

					if(animations.reloading)
					{
						EnumAnimationType anim = model.animationType;
						if (gunType.getGrip(item) != null && gunType.getSecondaryFire(item))
							anim = gunType.getGrip(item).model.secondaryAnimType;

						//Calculate the amount of tilt required for the reloading animation
						float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
						reloadRotate = 1F;
						if(effectiveReloadAnimationProgress < model.tiltGunTime)
							reloadRotate = effectiveReloadAnimationProgress / model.tiltGunTime;
						if(effectiveReloadAnimationProgress > model.tiltGunTime + model.unloadClipTime + model.loadClipTime)
							reloadRotate = 1F - (effectiveReloadAnimationProgress - (model.tiltGunTime + model.unloadClipTime + model.loadClipTime)) / model.untiltGunTime;

						//Rotate the gun dependent on the animation type
						switch(model.animationType)
						{
							case BOTTOM_CLIP: case PISTOL_CLIP: case SHOTGUN: case END_LOADED:
							{
								GlStateManager.rotate(60F * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(30F * reloadRotate * flip, 1F, 0F, 0F);
								GlStateManager.translate(0.25F * reloadRotate, 0F, 0F);
								break;
							}
							case CUSTOMBOTTOM_CLIP:
							case CUSTOMPISTOL_CLIP:
							case CUSTOMSHOTGUN:
							case CUSTOMEND_LOADED: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case BACK_LOADED:
							{
								GlStateManager.rotate(-75F * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(-30F * reloadRotate * flip, 1F, 0F, 0F);
								GlStateManager.translate(0.5F * reloadRotate, 0F, 0F);
								break;
							}
							case CUSTOMBACK_LOADED: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case BULLPUP:
							{
								GlStateManager.rotate(70F * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(10F * reloadRotate * flip, 1F, 0F, 0F);
								GlStateManager.translate(0.5F * reloadRotate, -0.2F * reloadRotate, 0F);
								break;
							}
							case CUSTOMBULLPUP: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case RIFLE:
							{
								GlStateManager.rotate(30F * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(-30F * reloadRotate * flip, 1F, 0F, 0F);
								GlStateManager.translate(0.5F * reloadRotate, 0F, -0.5F * reloadRotate);
								break;
							}
							// CUSTOMRIFLE allows you to customize gun tilt & rotation while maintaining the
							// specialized reload
							case CUSTOMRIFLE: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case RIFLE_TOP: case REVOLVER:
							{
								GlStateManager.rotate(30F * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(10F * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(-10F * reloadRotate * flip, 1F, 0F, 0F);
								GlStateManager.translate(0.1F * reloadRotate, -0.2F * reloadRotate, -0.1F * reloadRotate);
								break;
							}
							case CUSTOMRIFLE_TOP:
							case CUSTOMREVOLVER: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case REVOLVER2: {
								GlStateManager.rotate(20F * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(-10F * reloadRotate * flip, 1F, 0F, 0F);
								break;
							}
							case CUSTOMREVOLVER2: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case ALT_PISTOL_CLIP:
							{
								GlStateManager.rotate(60F * reloadRotate * flip, 0F, 1F, 0F);
								GlStateManager.translate(0.15F * reloadRotate, 0.25F * reloadRotate, 0F);
								break;
							}
							case CUSTOMALT_PISTOL_CLIP: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case STRIKER:
							{
								GlStateManager.rotate(-35F * reloadRotate * flip, 1F, 0F, 0F);
								GlStateManager.translate(0.2F * reloadRotate, 0F, -0.1F * reloadRotate);
								break;
							}
							case CUSTOMSTRIKER: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case GENERIC:
							{
								//Gun reloads partly or completely off-screen.
								GlStateManager.rotate(45F * reloadRotate, 0F, 0F, 1F);
								GlStateManager.translate(-0.2F * reloadRotate, -0.5F * reloadRotate, 0F);
								break;
							}
							case CUSTOMGENERIC: {
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate,
										model.translateGun.z * reloadRotate);
								break;
							}
							case CUSTOM:
							{
								GlStateManager.rotate(model.rotateGunVertical * reloadRotate, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateGunHorizontal * reloadRotate, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltGun * reloadRotate, 1F, 0F, 0F);
								GlStateManager.translate(model.translateGun.x * reloadRotate, model.translateGun.y * reloadRotate, model.translateGun.z * reloadRotate);
								break;
							}
							default: break;
						}
					}
					break;
				}
				default: break;
			}

			renderGun(item, gunType, f, model, animations, reloadRotate, type);
		}
		GlStateManager.popMatrix();
	}

	/**
	 * Gun render method, seperated from transforms so that mecha renderer may also call this
	 */
	public void renderGun(ItemStack item, GunType type, float f, ModelGun model, GunAnimations animations, float reloadRotate, CustomItemRenderType rtype)
	{
		float min = -1.5f;
        float max = 1.5f;
        float randomNum = new Random().nextFloat();
        float result = min + (randomNum * (max - min));

		//Make sure we actually have the renderEngine
		if(renderEngine == null)
			renderEngine = Minecraft.getMinecraft().renderEngine;

		//If we have no animation variables, use defaults
		if(animations == null)
			animations = GunAnimations.defaults;

		// Do we have a muzzle flash
		ModelMuzzleFlash mfModel = type.muzzleFlashModel;
		boolean renderMuzzleFlash = mfModel != null && animations.muzzleFlash > 0;

		//Get all the attachments that we may need to render
		AttachmentType scopeAttachment = type.getScope(item);
		AttachmentType barrelAttachment = type.getBarrel(item);
		AttachmentType stockAttachment = type.getStock(item);
		AttachmentType gripAttachment = type.getGrip(item);

		AttachmentType gadgetAttachment = type.getGadget(item);
		AttachmentType slideAttachment = type.getSlide(item);
		AttachmentType pumpAttachment = type.getPump(item);
		AttachmentType accessoryAttachment = type.getAccessory(item);

		ItemStack scopeItemStack = type.getScopeItemStack(item);
		ItemStack barrelItemStack = type.getBarrelItemStack(item);
		ItemStack stockItemStack = type.getStockItemStack(item);
		ItemStack gripItemStack = type.getGripItemStack(item);

		ItemStack gadgetItemStack = type.getGadgetItemStack(item);
		ItemStack slideItemStack = type.getSlideItemStack(item);
		ItemStack pumpItemStack = type.getPumpItemStack(item);
		ItemStack accessoryItemStack = type.getAccessoryItemStack(item);

		// Gun recoil
		animations.recoilAmount = model.recoilAmount;

		GlStateManager.pushMatrix();
		if (rtype == CustomItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GlStateManager.translate(0F, 0, 0);

			GlStateManager.translate(-(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * getRecoilDistance(gripAttachment, type, item), 0F, 0F);
			GlStateManager.rotate(-(animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * getRecoilAngle(gripAttachment, type, item), 0F, 0F, 1F);
			GlStateManager.rotate((float) ((-animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * result * smoothing * model.ShakeDistance), (float) 0.0f, (float) 1.0f, (float) 0.0f);
			GlStateManager.rotate((float) ((-animations.lastGunRecoil + (animations.gunRecoil - animations.lastGunRecoil) * smoothing) * result * smoothing * model.ShakeDistance), (float) 1.0f, (float) 0.0f, (float) 0.0f);

            // Do not move gun when there's a pump in the reload
			if (model.animationType == EnumAnimationType.SHOTGUN && !animations.reloading)
			{
				GlStateManager.rotate(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * -5F, 0F, 1F, 0F);
				GlStateManager.rotate(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * 5F, 1F, 0F, 0F);
			}

			if (model.isSingleAction)
			{
				GlStateManager.rotate(-(1 - Math.abs(animations.lastGunPullback + (animations.gunPullback - animations.lastGunPullback) * smoothing)) * -5F, 0F, 0F, 1F);
				GlStateManager.rotate(-(1 - Math.abs(animations.lastGunPullback + (animations.gunPullback - animations.lastGunPullback) * smoothing)) * 2.5F, 1F, 0F, 0F);
			}
		}

		ItemStack[] bulletStacks = new ItemStack[type.numAmmoItemsInGun];
		boolean empty = true;
		int numRounds = 0;
		for(int i = 0; i < type.numAmmoItemsInGun; i++)
		{
			bulletStacks[i] = ((ItemGun)item.getItem()).getBulletItemStack(item, i);
			if(bulletStacks[i] != null && bulletStacks[i].getItem() instanceof ItemBullet && bulletStacks[i].getItemDamage() < bulletStacks[i].getMaxDamage())
			{
				empty = false;
				numRounds += bulletStacks[i].getMaxDamage() - bulletStacks[i].getItemDamage();
			}
		}

		// Sanity check for empty guns
		if (model.slideLockOnEmpty)
		{
			if (empty)
				animations.onGunEmpty(true);
			else if (!empty && !animations.reloading)
				animations.onGunEmpty(false);
		}

		//Load texture
		if (rtype == CustomItemRenderType.EQUIPPED_FIRST_PERSON && model.hasArms && FlansMod.armsEnable)
		{
			Minecraft mc = Minecraft.getMinecraft();
			renderFirstPersonArm(mc.player, model, animations);
		}
		//renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(type.getPaintjob(item.getTagCompound().getString("Paint"))));
		Paintjob paintjob = type.getPaintjob(item.getItemDamage());
		if(bindTextures)
		{
			if(PaintableType.HasCustomPaintjob(item))
			{
				renderEngine.bindTexture(PaintableType.GetCustomPaintjobSkinResource(item));
			}
			else
			{
				renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(paintjob));
			}
		}

		// This allows you to offset your gun with a sight attached to properly align
		// the aiming reticle
		// Can be adjusted per scope and per gun
		if(scopeAttachment != null && model.gunOffset != 0 && FlansModClient.zoomProgress >= 0.5F)
			GlStateManager.translate(0F, -scopeAttachment.model.renderOffset + model.gunOffset / 16F, 0F);

		//Render the gun and default attachment models
		GlStateManager.pushMatrix();
		{
			GlStateManager.scale(type.modelScale, type.modelScale, type.modelScale);

			model.renderGun(f);
			model.renderCustom(f, animations);
			if(scopeAttachment == null && !model.scopeIsOnSlide && !model.scopeIsOnBreakAction)
				model.renderDefaultScope(f);
			if(barrelAttachment == null)
				model.renderDefaultBarrel(f);
			if(stockAttachment == null)
				model.renderDefaultStock(f);
			if(gripAttachment == null && !model.gripIsOnPump)
				model.renderDefaultGrip(f);
			if (gadgetAttachment == null && !model.gadgetIsOnPump)
				model.renderDefaultGadget(f);

			//Render the bullet counter
			GL11.glPushMatrix();
			{
				if(model.isBulletCounterActive)
					model.renderBulletCounter(f, numRounds);
			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{
				if(model.isAdvBulletCounterActive)
					model.renderAdvBulletCounter(f, numRounds, model.countOnRightHandSide);
			}
			GL11.glPopMatrix();

			// Option to offset flash location with a barrel attachment (location + offset =
			// new location)
			boolean isFlashEnabled = barrelAttachment == null || !barrelAttachment.disableMuzzleFlash;

			if (isFlashEnabled && animations.muzzleFlashTime > 0 && type.flashModel != null && !type.getSecondaryFire(item))
			{
				GL11.glPushMatrix();
				ModelFlash flash = type.flashModel;
				GL11.glScalef(model.flashScale, model.flashScale, model.flashScale);
				{
					Vector3f base = model.muzzleFlashPoint == null ? Vector3f.Zero : model.muzzleFlashPoint;
					if (barrelAttachment != null) {
						Vector3f barrelOffset = (barrelAttachment.model != null && barrelAttachment.model.attachmentFlashOffset != null) ? barrelAttachment.model.attachmentFlashOffset : Vector3f.Zero;
						GL11.glTranslatef(base.x + barrelOffset.x,
											base.y + barrelOffset.y,
											base.z + barrelOffset.z);
					} else {
						Vector3f defaultOffset = model.defaultBarrelFlashPoint == null ? Vector3f.Zero : model.defaultBarrelFlashPoint;

						GL11.glTranslatef(base.x + defaultOffset.x,
								base.y + defaultOffset.y,
								base.z + defaultOffset.z);
					}
					GlStateManager.disableLighting();
			        GlStateManager.enableBlend();
			        GlStateManager.disableAlpha();
			        GlStateManager.depthMask(false);
			        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
					int i = 61680;
				    int j = i % 65536;
				    int k = i / 65536;
				    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
					renderEngine.bindTexture(FlansModResourceHandler.getAuxiliaryTexture(type.flashTexture));
					//ModelGun.glowOn();
					flash.renderFlash(f, animations.flashInt);
					//ModelGun.glowOff();
					GlStateManager.enableLighting();
					GlStateManager.disableBlend();
					GlStateManager.enableAlpha();
					GlStateManager.depthMask(true);
					renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(type.getPaintjob(item.getItemDamage())));
				}
				GL11.glPopMatrix();
			}


			//Render various shoot / reload animated parts
			//Render the slide and charge action
			if (slideAttachment == null)
			{
				GlStateManager.pushMatrix();
				{
					if (!type.getSecondaryFire(item)) {
						GlStateManager.translate(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
						GL11.glTranslatef(-(1 - Math.abs(
								animations.lastCharged + (animations.charged - animations.lastCharged) * smoothing))
								* model.chargeHandleDistance, 0F, 0F);
					}
					model.renderSlide(f);
					if(scopeAttachment == null && model.scopeIsOnSlide)
						model.renderDefaultScope(f);
				}
				GlStateManager.popMatrix();
			}

			// Render the alternate slide
			if (slideAttachment == null)
			{
				GL11.glPushMatrix();
				{
					if (!type.getSecondaryFire(item)) {
						GL11.glTranslatef(
								-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing)
										* model.altgunSlideDistance,
								0F, 0F);
						model.renderaltSlide(f);
					}

					// if(scopeAttachment == null && model.scopeIsOnSlide)
					// model.renderDefaultScope(f);
				}
				GL11.glPopMatrix();
			}

			//Render the break action
			GlStateManager.pushMatrix();
			{
				GlStateManager.translate(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
				GlStateManager.rotate(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
				GlStateManager.translate(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
				model.renderBreakAction(f);
				if(scopeAttachment == null && model.scopeIsOnBreakAction)
					model.renderDefaultScope(f);
			}
			GlStateManager.popMatrix();

			// Render the alternate break action
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.altbarrelBreakPoint.x, model.altbarrelBreakPoint.y, model.altbarrelBreakPoint.z);
				GL11.glRotatef(reloadRotate * -model.altbreakAngle, 0F, 0F, 1F);
				GL11.glTranslatef(-model.altbarrelBreakPoint.x, -model.altbarrelBreakPoint.y, -model.altbarrelBreakPoint.z);
				model.renderaltBreakAction(f);
				// if(scopeAttachment == null && model.scopeIsOnBreakAction)
				// model.renderDefaultScope(f);
			}
			GL11.glPopMatrix();

			// Render the hammer
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.hammerSpinPoint.x, model.hammerSpinPoint.y, model.hammerSpinPoint.z);
				GL11.glRotatef(-animations.hammerRotation, 0F, 0F, 1F);
				GL11.glTranslatef(-model.hammerSpinPoint.x, -model.hammerSpinPoint.y, -model.hammerSpinPoint.z);
				model.renderHammer(f);
			}
			GL11.glPopMatrix();

			// Render the alternate hammer
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.althammerSpinPoint.x, model.althammerSpinPoint.y, model.althammerSpinPoint.z);
				GL11.glRotatef(-animations.althammerRotation, 0F, 0F, 1F);
				GL11.glTranslatef(-model.althammerSpinPoint.x, -model.althammerSpinPoint.y, -model.althammerSpinPoint.z);
				model.renderaltHammer(f);
			}
			GL11.glPopMatrix();

			//Render the pump-action handle
			if (pumpAttachment == null)
			{
				GlStateManager.pushMatrix();
				{
					GlStateManager.translate(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
					model.renderPump(f);
					if(gripAttachment == null && model.gripIsOnPump)
						model.renderDefaultGrip(f);
					if (gadgetAttachment == null && model.gadgetIsOnPump)
						model.renderDefaultGadget(f);
					if(FlansModClient.shotState != -1 && -(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance != -0.0)
					{
						FlansModClient.shotState = -1;
						if(type.actionSound != null)
						{
							Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(FlansModResourceHandler.getSoundEvent(type.actionSound), 1.0F));
						}
					}
				}
				GlStateManager.popMatrix();
			}

			// Render the alternate pump-action handle
			if (pumpAttachment == null)
			{
				GL11.glPushMatrix();
				{

					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
					float pumped = (animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing);
					model.renderaltPump(f);
					if (gripAttachment == null && model.gripIsOnPump)
						model.renderDefaultGrip(f);
					if (gadgetAttachment == null && model.gadgetIsOnPump)
						model.renderDefaultGadget(f);
				}
				GL11.glPopMatrix();
			}

			// Render the charge handle
			if (model.chargeHandleDistance != 0F)
			{
				GL11.glPushMatrix();
				{
					GL11.glTranslatef(-(1 - Math.abs(animations.lastCharged + (animations.charged - animations.lastCharged) * smoothing)) * model.chargeHandleDistance, 0F, 0F);
					model.renderCharge(f);
				}
				GL11.glPopMatrix();
			}

			//Render the minigun barrels
			if(type.mode == EnumFireMode.MINIGUN)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(model.minigunBarrelOrigin.x, model.minigunBarrelOrigin.y, model.minigunBarrelOrigin.z);
				GlStateManager.rotate(animations.minigunBarrelRotation, 1F, 0F, 0F);
				GlStateManager.translate(-model.minigunBarrelOrigin.x, -model.minigunBarrelOrigin.y, -model.minigunBarrelOrigin.z);
				model.renderMinigunBarrel(f);
				GlStateManager.popMatrix();
			}

			//Render the cocking handle

			//Render the revolver barrel
			GlStateManager.pushMatrix();
			{
				GlStateManager.translate(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
				GlStateManager.rotate(reloadRotate * model.revolverFlipAngle, 1F, 0F, 0F);
				GlStateManager.translate(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
				model.renderRevolverBarrel(f);
			}
			GlStateManager.popMatrix();

			// Render the revolver2 barrel
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
				GL11.glRotatef(reloadRotate * model.revolverFlipAngle, -1F, 0F, 0F);
				GL11.glTranslatef(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
				model.renderRevolver2Barrel(f);
			}
			GL11.glPopMatrix();

			//Render the clip
			GlStateManager.pushMatrix();
			{
				boolean shouldRender = true;

				EnumAnimationType anim = model.animationType;
				if (gripAttachment != null && type.getSecondaryFire(item))
					anim = gripAttachment.model.secondaryAnimType;

				float tiltGunTime = model.tiltGunTime, unloadClipTime = model.unloadClipTime, loadClipTime = model.loadClipTime;
				if (gripAttachment != null && type.getSecondaryFire(item))
				{
					tiltGunTime = gripAttachment.model.tiltGunTime;
					unloadClipTime = gripAttachment.model.unloadClipTime;
					loadClipTime = gripAttachment.model.loadClipTime;
				}

				//Check to see if the ammo should be rendered first
				switch(anim)
				{
					case END_LOADED: case BACK_LOADED:
				{
					if(empty)
						shouldRender = false;
					break;
				}
					default: break;
				}
				//If it should be rendered, do the transformations required
				if(shouldRender && animations.reloading && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
				{
					//Calculate the amount of tilt required for the reloading animation
					float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
					float clipPosition = 0F;
					if(effectiveReloadAnimationProgress > tiltGunTime && effectiveReloadAnimationProgress < tiltGunTime + unloadClipTime)
						clipPosition = (effectiveReloadAnimationProgress - tiltGunTime) / unloadClipTime;
					if(effectiveReloadAnimationProgress >= tiltGunTime + unloadClipTime && effectiveReloadAnimationProgress < tiltGunTime + unloadClipTime + loadClipTime)
						clipPosition = 1F - (effectiveReloadAnimationProgress - (tiltGunTime + unloadClipTime)) / loadClipTime;

					float loadOnlyClipPosition = Math.max(0F, Math.min(1F, 1F - ((effectiveReloadAnimationProgress - tiltGunTime) / (unloadClipTime + loadClipTime))));

					//Rotate the gun dependent on the animation type
					switch(anim)
					{
						case BREAK_ACTION: case CUSTOMBREAK_ACTION:
						{
							GlStateManager.translate(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
							GlStateManager.rotate(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
							GlStateManager.translate(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
							GlStateManager.translate(-model.breakActionAmmoDistance * clipPosition * 1 / type.modelScale, 0F, 0F);
							break;
						}
						case REVOLVER: case CUSTOMREVOLVER:
						{
							GlStateManager.translate(model.revolverFlipPoint.x, model.revolverFlipPoint.y, model.revolverFlipPoint.z);
							GlStateManager.rotate(reloadRotate * model.revolverFlipAngle, 1F, 0F, 0F);
							GlStateManager.translate(-model.revolverFlipPoint.x, -model.revolverFlipPoint.y, -model.revolverFlipPoint.z);
							GlStateManager.translate(-1F * clipPosition * 1 / type.modelScale, 0F, 0F);
							break;
						}
						case REVOLVER2: case CUSTOMREVOLVER2:
						{
							GL11.glTranslatef(model.revolver2FlipPoint.x, model.revolver2FlipPoint.y, model.revolver2FlipPoint.z);
							GL11.glRotatef(reloadRotate * model.revolver2FlipAngle, -1F, 0F, 0F);
							GL11.glTranslatef(-model.revolver2FlipPoint.x, -model.revolver2FlipPoint.y, -model.revolver2FlipPoint.z);
							GL11.glTranslatef(-1F * clipPosition * 1 / type.modelScale, 0F, 0F);
							break;
						}
						case BOTTOM_CLIP: case CUSTOMBOTTOM_CLIP:
						{
							GlStateManager.rotate(-180F * clipPosition, 0F, 0F, 1F);
							GlStateManager.rotate(60F * clipPosition, 1F, 0F, 0F);
							GlStateManager.translate(0.5F * clipPosition * 1 / type.modelScale, 0F, 0F);
							break;
						}
						case PISTOL_CLIP: case CUSTOMPISTOL_CLIP:
						{
							GlStateManager.rotate(-90F * clipPosition * clipPosition, 0F, 0F, 1F);
							GlStateManager.translate(0F, -1F * clipPosition * 1 / type.modelScale, 0F);
							break;
						}
						case ALT_PISTOL_CLIP: case CUSTOMALT_PISTOL_CLIP:
						{
							GlStateManager.rotate(5F * clipPosition, 0F, 0F, 1F);
							GlStateManager.translate(0F, -3F * clipPosition * 1 / type.modelScale, 0F);
							break;
						}
						case SIDE_CLIP: case CUSTOMSIDE_CLIP:
						{
							GlStateManager.rotate(180F * clipPosition, 0F, 1F, 0F);
							GlStateManager.rotate(60F * clipPosition, 0F, 1F, 0F);
							GlStateManager.translate(0.5F * clipPosition * 1 / type.modelScale, 0F, 0F);
							break;
						}
						case BULLPUP: case CUSTOMBULLPUP:
						{
							GlStateManager.rotate(-150F * clipPosition, 0F, 0F, 1F);
							GlStateManager.rotate(60F * clipPosition, 1F, 0F, 0F);
							GlStateManager.translate(1F * clipPosition * 1 / type.modelScale,
									-0.5F * clipPosition * 1 / type.modelScale, 0F);
							break;
						}
						case P90: case CUSTOMP90:
						{
							GlStateManager.rotate(-15F * reloadRotate * reloadRotate, 0F, 0F, 1F);
							GlStateManager.translate(0F, 0.075F * reloadRotate, 0F);
							GlStateManager.translate(-2F * clipPosition * 1 / type.modelScale, -0.3F * clipPosition * 1 / type.modelScale, 0.5F * clipPosition * 1 / type.modelScale);
							break;
						}
						case RIFLE:
						{
							float ammoPosition = clipPosition * getNumBulletsInReload(animations, gripAttachment, type, item);
							int bulletNum = MathHelper.floor(ammoPosition);
							float bulletProgress = ammoPosition - bulletNum;

							GlStateManager.rotate(bulletProgress * 15F, 0F, 1F, 0F);
							GlStateManager.rotate(bulletProgress * 15F, 0F, 0F, 1F);
							GlStateManager.translate(bulletProgress * -1F, 0F, bulletProgress * 0.5F);

							break;
						}
						case CUSTOMRIFLE:
						{
							float maxBullets = getNumBulletsInReload(animations, gripAttachment, type, item);
							float ammoPosition = clipPosition * maxBullets;
							int bulletNum = MathHelper.floor(ammoPosition);
							float bulletProgress = ammoPosition - bulletNum;

							if(type.getNumAmmoItemsInGun(item) > 1 && type.bulletInsert != null && FlansModClient.lastBulletReload != -2)
							{
								if(maxBullets == 2 && FlansModClient.lastBulletReload != -1)
								{
									int time = (int) (animations.reloadAnimationTime / maxBullets);
									Minecraft.getMinecraft().getSoundHandler().playDelayedSound(PositionedSoundRecord.getMasterRecord(FlansModResourceHandler.getSoundEvent(type.bulletInsert), 1.0F), time);
									FlansModClient.lastBulletReload = -1;
								} else if((bulletNum == (int) maxBullets || bulletNum == FlansModClient.lastBulletReload-1))
								{
									FlansModClient.lastBulletReload = bulletNum;
									Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(FlansModResourceHandler.getSoundEvent(type.bulletInsert), 1.0F));
								}

								if((ammoPosition < 0.03 && bulletProgress > 0))
								{
									FlansModClient.lastBulletReload = -2;
									Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(FlansModResourceHandler.getSoundEvent(type.bulletInsert), 1.0F));
								}
							}

							GL11.glRotatef(bulletProgress * model.rotateClipVertical, 0F, 1F, 0F);
							GL11.glRotatef(bulletProgress * model.rotateClipHorizontal, 0F, 0F, 1F);
							GL11.glRotatef(bulletProgress * model.tiltClip, 1F, 0F, 0F);
							GL11.glTranslatef(bulletProgress * model.translateClip.x / type.modelScale, bulletProgress * model.translateClip.y / type.modelScale, bulletProgress * model.translateClip.z / type.modelScale);
							break;
						}
						case RIFLE_TOP:
						{
							float ammoPosition = clipPosition * getNumBulletsInReload(animations, gripAttachment, type, item);
							int bulletNum = MathHelper.floor(ammoPosition);
							float bulletProgress = ammoPosition - bulletNum;

							GlStateManager.rotate(bulletProgress * 55F, 0F, 1F, 0F);
							GlStateManager.rotate(bulletProgress * 95F, 0F, 0F, 1F);
							GlStateManager.translate(bulletProgress * -0.1F * 1 / type.modelScale, bulletProgress * 1F, bulletProgress * 0.5F * 1 / type.modelScale);

							break;
						}
						case SHOTGUN: case STRIKER: case CUSTOMSHOTGUN: case CUSTOMSTRIKER:
						{
							/*float thing = clipPosition * model.numBulletsInReloadAnimation;
							int bulletNum = MathHelper.floor(thing);
							float bulletProgress = thing - bulletNum;*/

							float maxBullets = getNumBulletsInReload(animations, gripAttachment, type, item);
							float ammoPosition = clipPosition * maxBullets;
							int bulletNum = MathHelper.floor(ammoPosition);
							float bulletProgress = ammoPosition - bulletNum;

							if (maxBullets > 1) {
								if (type.getNumAmmoItemsInGun(item) > 1 && type.bulletInsert != null && FlansModClient.lastBulletReload != -2) {
									if (maxBullets == 2 && FlansModClient.lastBulletReload != -1) {
										int time = (int) (animations.reloadAnimationTime / maxBullets);
										Minecraft.getMinecraft().getSoundHandler() .playDelayedSound(PositionedSoundRecord.getMasterRecord(FlansModResourceHandler.getSoundEvent(type.bulletInsert), 1.0F), time);
										FlansModClient.lastBulletReload = -1;
									} else if ((bulletNum == (int) maxBullets || bulletNum == FlansModClient.lastBulletReload - 1)) {
										FlansModClient.lastBulletReload = bulletNum;
										Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord
												.getMasterRecord(FlansModResourceHandler.getSoundEvent(type.bulletInsert), 1.0F));
									}

									if ((ammoPosition < 0.03 && bulletProgress > 0)) {
										FlansModClient.lastBulletReload = -2;
										Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord
												.getMasterRecord(FlansModResourceHandler.getSoundEvent(type.bulletInsert), 1.0F));
									}
								}
							}

							GlStateManager.rotate(bulletProgress * -30F, 0F, 0F, 1F);
							GlStateManager.translate(bulletProgress * -0.5F * 1 / type.modelScale, bulletProgress * -1F * 1 / type.modelScale, 0F);

							break;
						}
						case CUSTOM:
						{
							// Staged reload allows you to change the animation route half way through
							if (effectiveReloadAnimationProgress < 0.5 && model.stagedReload)
							{
								GlStateManager.rotate(model.rotateClipVertical * clipPosition, 0F, 0F, 1F);
								GlStateManager.rotate(model.rotateClipHorizontal * clipPosition, 0F, 1F, 0F);
								GlStateManager.rotate(model.tiltClip * clipPosition, 1F, 0F, 0F);
								GlStateManager.translate(model.translateClip.x * clipPosition * 1 / type.modelScale, model.translateClip.y * clipPosition * 1 / type.modelScale, model.translateClip.z * clipPosition * 1 / type.modelScale);
								break;
							}
							else if (effectiveReloadAnimationProgress > 0.5 && model.stagedReload)
							{
								GL11.glRotatef(model.stagedrotateClipVertical * clipPosition, 0F, 0F, 1F);
								GL11.glRotatef(model.stagedrotateClipHorizontal * clipPosition, 0F, 1F, 0F);
								GL11.glRotatef(model.stagedtiltClip * clipPosition, 1F, 0F, 0F);
								GL11.glTranslatef(model.stagedtranslateClip.x * clipPosition * 1 / type.modelScale, model.stagedtranslateClip.y * clipPosition * 1 / type.modelScale, model.stagedtranslateClip.z * clipPosition * 1 / type.modelScale);
								break;
							}
							else
							{
								GL11.glRotatef(model.rotateClipVertical * clipPosition, 0F, 0F, 1F);
								GL11.glRotatef(model.rotateClipHorizontal * clipPosition, 0F, 1F, 0F);
								GL11.glRotatef(model.tiltClip * clipPosition, 1F, 0F, 0F);
								GL11.glTranslatef(model.translateClip.x * clipPosition * 1 / type.modelScale, model.translateClip.y * clipPosition * 1 / type.modelScale, model.translateClip.z * clipPosition * 1 / type.modelScale);
								break;
							}
						}
						case END_LOADED: case CUSTOMEND_LOADED:
						{
							//float bulletProgress = 1F;
							//if(effectiveReloadAnimationProgress > model.tiltGunTime)
							//	bulletProgress = 1F - Math.min((effectiveReloadAnimationProgress - model.tiltGunTime) / (model.unloadClipTime + model.loadClipTime), 1);


							float dYaw = (loadOnlyClipPosition > 0.5F ? loadOnlyClipPosition * 2F - 1F : 0F);


							GlStateManager.rotate(-45F * dYaw, 0F, 0F, 1F);
							GlStateManager.translate(-getEndLoadedDistance(gripAttachment, type, item) * dYaw, -0.5F * dYaw, 0F);

							float xDisplacement = (loadOnlyClipPosition < 0.5F ? loadOnlyClipPosition * 2F : 1F);

							GlStateManager.translate(getEndLoadedDistance(gripAttachment, type, item) * xDisplacement, 0F, 0F);

							/*
							GlStateManager.translate(1F * bulletProgress, -3F * bulletProgress, 0F);
							if(bulletProgress > 0.5F)
								GlStateManager.rotate(-90F * (bulletProgress * 2F), 0F, 0F, 1F);

							if(bulletProgress < 0.5F)
							{
								GlStateManager.translate(-3F * (bulletProgress - 0.5F), 0F, 0F);

							}
							*/


							break;
						}
						case BACK_LOADED: case CUSTOMBACK_LOADED:
						{
							float dYaw = (loadOnlyClipPosition > 0.5F ? loadOnlyClipPosition * 2F - 1F : 0F);


							//GlStateManager.rotate(-45F * dYaw, 0F, 0F, 1F);
							GlStateManager.translate(getEndLoadedDistance(gripAttachment, type, item) * dYaw, -0.5F * dYaw, 0F);

							float xDisplacement = (loadOnlyClipPosition < 0.5F ? loadOnlyClipPosition * 2F : 1F);

							GlStateManager.translate(-getEndLoadedDistance(gripAttachment, type, item) * xDisplacement, 0F, 0F);
						}

						default: break;
					}
				}

				if (rtype == CustomItemRenderType.EQUIPPED_FIRST_PERSON && model.hasArms && FlansMod.armsEnable)
				{
					Minecraft mc = Minecraft.getMinecraft();
					renderAnimArm(mc.player, model, type, animations);
				}
				renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(type.getPaintjob(item.getItemDamage())));

				if (shouldRender)
				{
					if (gripAttachment != null && type.getSecondaryFire(item))
						renderAttachmentAmmo(f, gripAttachment, model, gripAttachment.getPaintjob(gripItemStack.getItemDamage()), type.getPaintjob(item.getItemDamage()));
					else
						model.renderAmmo(f);
				}
				// Renders fullammo model for 2nd half of reload animation
				float effectiveReloadAnimationProgress = animations.lastReloadAnimationProgress + (animations.reloadAnimationProgress - animations.lastReloadAnimationProgress) * smoothing;
				reloadRotate = 1F;
				if (effectiveReloadAnimationProgress > 0.5)
					model.renderfullAmmo(f);
			}
			GlStateManager.popMatrix();

			// Render a static model of the ammo NOT being reloaded
			GL11.glPushMatrix();
			{
				if (type.getSecondaryFire(item))
					model.renderAmmo(f);
				else if (gripAttachment != null && !type.getSecondaryFire(item))
					renderAttachmentAmmo(f, gripAttachment, model, gripAttachment.getPaintjob(gripItemStack.getItemDamage()), type.getPaintjob(item.getItemDamage()));
			}
			GL11.glPopMatrix();

			//Render casing ejection
			if (rtype == CustomItemRenderType.EQUIPPED_FIRST_PERSON && FlansMod.casingEnable && type.casingModel != null && !type.getSecondaryFire(item))
			{
				ModelCasing casing = type.casingModel;
				GL11.glPushMatrix();
				{
					float casingProg = (animations.lastCasingStage + (animations.casingStage - animations.lastCasingStage) * smoothing) / model.casingAnimTime;
					if (casingProg >= 1)
						casingProg = 0;
					float moveX = model.casingAnimDistance.x + (animations.casingRandom.x * model.casingAnimSpread.x);
					float moveY = model.casingAnimDistance.y + (animations.casingRandom.y * model.casingAnimSpread.y);
					float moveZ = model.casingAnimDistance.z + (animations.casingRandom.z * model.casingAnimSpread.z);
					GL11.glScalef(model.caseScale, model.caseScale, model.caseScale);
					GL11.glTranslatef(model.casingAttachPoint.x + (casingProg * moveX), model.casingAttachPoint.y + (casingProg * moveY), model.casingAttachPoint.z + (casingProg * moveZ));
					GL11.glRotatef(casingProg * 180, model.casingRotateVector.x, model.casingRotateVector.y, model.casingRotateVector.z);
					renderEngine.bindTexture(FlansModResourceHandler.getAuxiliaryTexture(type.casingTexture));
					casing.renderCasing(f);
					renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(type.getPaintjob(item.getItemDamage())));
				}
				GL11.glPopMatrix();
			}
		}
		GlStateManager.popMatrix();

		//Render static attachments
		//Scope
		if(scopeAttachment != null)
		{
			GlStateManager.pushMatrix();
			{
				preRenderAttachment(scopeAttachment, scopeItemStack, model.scopeAttachPoint, type);
				if(model.scopeIsOnBreakAction)
				{
					GlStateManager.translate(model.barrelBreakPoint.x, model.barrelBreakPoint.y, model.barrelBreakPoint.z);
					GlStateManager.rotate(reloadRotate * -model.breakAngle, 0F, 0F, 1F);
					GlStateManager.translate(-model.barrelBreakPoint.x, -model.barrelBreakPoint.y, -model.barrelBreakPoint.z);
				}

				if(model.scopeIsOnSlide)
					GlStateManager.translate(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
				postRenderAttachment(scopeAttachment, scopeItemStack, f);
			}
			GlStateManager.popMatrix();
		}

		//Grip
		if(gripAttachment != null)
		{
			GlStateManager.pushMatrix();
			{
				preRenderAttachment(gripAttachment, gripItemStack, model.gripAttachPoint, type);
				if(model.gripIsOnPump)
					GlStateManager.translate(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
				postRenderAttachment(gripAttachment, gripItemStack, f);
			}
			GlStateManager.popMatrix();
		}

		//Barrel
		if(barrelAttachment != null)
		{
			GlStateManager.pushMatrix();
			{
				preRenderAttachment(barrelAttachment, barrelItemStack, model.barrelAttachPoint, type);
				postRenderAttachment(barrelAttachment, barrelItemStack, f);
			}
			GlStateManager.popMatrix();
		}

		//Stock
		if(stockAttachment != null)
		{
			GlStateManager.pushMatrix();
			{
				preRenderAttachment(stockAttachment, stockItemStack, model.stockAttachPoint, type);
				postRenderAttachment(stockAttachment, stockItemStack, f);
			}
			GlStateManager.popMatrix();
		}

		if(renderMuzzleFlash)
		{
			Vector3f mfPoint = model.muzzleFlashPoint2;
			if(mfPoint == ModelGun.invalid)
			{
				mfPoint = model.barrelAttachPoint;
			}
			if(barrelAttachment != null)
			{
				Vector3f.add(model.barrelAttachPoint, barrelAttachment.model.muzzleFlashPoint, mfPoint);
			}

			GlStateManager.pushMatrix();
			{

				GlStateManager.disableLighting();
		        GlStateManager.enableBlend();
		        GlStateManager.disableAlpha();
		        GlStateManager.depthMask(false);
		        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
				 int i = 61680;
			        int j = i % 65536;
			        int k = i / 65536;
			        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
				GlStateManager.color(1f, 1f, 1f);
				renderEngine.bindTexture(mfModel.GetTexture());
				GlStateManager.translate(mfPoint.x * type.modelScale, mfPoint.y * type.modelScale, mfPoint.z * type.modelScale);
				mfModel.render(null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, f);
				GlStateManager.enableLighting();
				GlStateManager.disableBlend();
				GlStateManager.enableAlpha();
				GlStateManager.depthMask(true);
			}
			GlStateManager.popMatrix();
		}

		// Slide
		if (slideAttachment != null && !type.getSecondaryFire(item))
		{
			GL11.glPushMatrix();
			{
				preRenderAttachment(slideAttachment, slideItemStack, model.slideAttachPoint, type);
				GL11.glTranslatef(-(animations.lastGunSlide + (animations.gunSlide - animations.lastGunSlide) * smoothing) * model.gunSlideDistance, 0F, 0F);
				postRenderAttachment(slideAttachment, slideItemStack, f);
			}
			GL11.glPopMatrix();
		}

		// Gadget
		if (gadgetAttachment != null)
		{
			GL11.glPushMatrix();
			{
				preRenderAttachment(gadgetAttachment, gadgetItemStack, model.gadgetAttachPoint, type);
				if (model.gadgetIsOnPump)
					GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
				postRenderAttachment(gadgetAttachment, gadgetItemStack, f);
			}
			GL11.glPopMatrix();
		}

		// Accessory
		if (accessoryAttachment != null)
		{
			GL11.glPushMatrix();
			{
				preRenderAttachment(accessoryAttachment, accessoryItemStack, model.accessoryAttachPoint, type);
				postRenderAttachment(accessoryAttachment, accessoryItemStack, f);
			}
			GL11.glPopMatrix();
		}

		// Pump
		if (pumpAttachment != null)
		{
			GL11.glPushMatrix();
			{
				preRenderAttachment(pumpAttachment, pumpItemStack, model.pumpAttachPoint, type);
				GL11.glTranslatef(-(1 - Math.abs(animations.lastPumped + (animations.pumped - animations.lastPumped) * smoothing)) * model.pumpHandleDistance, 0F, 0F);
				postRenderAttachment(pumpAttachment, pumpItemStack, f);
			}
			GL11.glPopMatrix();
		}

		// Release
		GL11.glPopMatrix();
	}

	/** Clean up some redundant code */
	private void preRenderAttachment(AttachmentType attachment, ItemStack stack, Vector3f model, GunType type) {
		Paintjob paintjob = attachment.getPaintjob(stack.getItemDamage());
		renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(paintjob));
		GL11.glTranslatef(model.x * type.modelScale, model.y * type.modelScale, model.z * type.modelScale);
		GL11.glScalef(attachment.modelScale, attachment.modelScale, attachment.modelScale);
	}

	private void postRenderAttachment(AttachmentType attachment, ItemStack stack, float f)
	{
		Paintjob paintjob = attachment.getPaintjob(stack.getItemDamage());
		ModelAttachment model = attachment.model;
		if (model != null)
			model.renderAttachment(f);
		renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(paintjob));
	}

	/** Load the attachment ammo model plus its texture */
	private void renderAttachmentAmmo(float f, AttachmentType grip, ModelGun model, Paintjob ammo, Paintjob otherAmmo)
	{
		renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(ammo));
		GL11.glTranslatef(model.gripAttachPoint.x, model.gripAttachPoint.y, model.gripAttachPoint.z);
		grip.model.renderAttachmentAmmo(f);
		renderEngine.bindTexture(FlansModResourceHandler.getPaintjobTexture(otherAmmo));
	}

	/** Load the corresponding casing model and texture */
//		private void renderCasingModel(float f, AttachmentType grip, GunType gun, ItemStack gunStack)
//		{

//		}

	/** Load the corresponding flash model and texture */
//		private void renderFlashModel()
//		{
//
//		}

	// TODO: Part of arms cleanup to rewrite into one method
	private void renderArms(EntityPlayer player, ModelGun model, GunType type, GunAnimations anim)
	{

	}

	private void renderFirstPersonArm(EntityPlayer player, ModelGun model, GunAnimations anim) {
		Minecraft mc = Minecraft.getMinecraft();
		ModelBiped modelBipedMain = new ModelBiped(0.0F, 0.0F, 64, 64);
		mc.renderEngine.bindTexture(mc.player.getLocationSkin());

		if (mc.player.getSkinType().equals("slim"))
		{
			modelBipedMain.bipedLeftArm = new ModelRenderer(modelBipedMain, 32, 48);
			modelBipedMain.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F);
			modelBipedMain.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
			modelBipedMain.bipedRightArm = new ModelRenderer(modelBipedMain, 40, 16);
			modelBipedMain.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F);
			modelBipedMain.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
		}

		float f = 1.0F;
		GL11.glColor3f(f, f, f);
		//modelBipedMain.onGround = 0.0F;

		GL11.glPushMatrix();
		{
			if (!anim.reloading && model.righthandPump) {
				RenderArms.renderArmPump(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
			}
			else if (anim.charged < 0.9 && model.leftHandAmmo && model.rightHandCharge && anim.charged != -1.0F) {
				RenderArms.renderArmCharge(model, anim, smoothing, model.rightArmChargeRot, model.rightArmChargePos);
			}
			else if (anim.pumped < 0.9 && model.rightHandBolt && model.leftHandAmmo) {
				RenderArms.renderArmBolt(model, anim, smoothing, model.rightArmChargeRot, model.rightArmChargePos);
			}
			else if (!anim.reloading && !model.righthandPump) {
				RenderArms.renderArmDefault(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
			}
			else {
				RenderArms.renderArmReload(model, anim, smoothing, model.rightArmReloadRot, model.rightArmReloadPos);
			}

			GL11.glScalef(model.rightArmScale.x, model.rightArmScale.y, model.rightArmScale.z);
			modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
			modelBipedMain.bipedRightArm.offsetY = 0F;
			if (!model.rightHandAmmo) {
				modelBipedMain.bipedRightArm.render(0.0625F);
			}
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		if (!anim.reloading && model.lefthandPump) {
			RenderArms.renderArmPump(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		}
		else if (anim.charged < 0.9 && model.rightHandCharge && model.leftHandAmmo && anim.charged != -1.0F) {
			RenderArms.renderArmCharge(model, anim, smoothing, model.leftArmChargeRot, model.leftArmChargePos);
		}
		else if (anim.pumped < 0.9 && model.rightHandBolt && model.leftHandAmmo) {
			RenderArms.renderArmBolt(model, anim, smoothing, model.leftArmChargeRot, model.leftArmChargePos);
		} else if (!anim.reloading && !model.lefthandPump) {
			RenderArms.renderArmDefault(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		} else {
			RenderArms.renderArmReload(model, anim, smoothing, model.leftArmReloadRot, model.leftArmReloadPos);
		}

		GL11.glScalef(model.leftArmScale.x, model.leftArmScale.y, model.leftArmScale.z);
		modelBipedMain.bipedLeftArm.offsetY = 0F;
		if (!model.leftHandAmmo) {
			modelBipedMain.bipedLeftArm.render(0.0625F);
		}
		GL11.glPopMatrix();
	}

	private void renderAnimArm(EntityPlayer player, ModelGun model, GunType type, GunAnimations anim) {
		Minecraft mc = Minecraft.getMinecraft();
		//ModelBiped modelBipedMain = new ModelBiped(0.0F);
		ModelBiped modelBipedMain = new ModelBiped(0.0F, 0.0F, 64, 64);
		mc.renderEngine.bindTexture(mc.player.getLocationSkin());

		if (mc.player.getSkinType().equals("slim"))
		{
			modelBipedMain.bipedLeftArm = new ModelRenderer(modelBipedMain, 32, 48);
			modelBipedMain.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F);
			modelBipedMain.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
			modelBipedMain.bipedRightArm = new ModelRenderer(modelBipedMain, 40, 16);
			modelBipedMain.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0F);
			modelBipedMain.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
		}

		GL11.glPushMatrix();
		GL11.glScalef(1 / type.modelScale, 1 / type.modelScale, 1 / type.modelScale);
		float f = 1.0F;
		GL11.glColor3f(f, f, f);
		//modelBipedMain.onGround = 0.0F;
		GL11.glPushMatrix();
		float effectiveReloadAnimationProgress = anim.lastReloadAnimationProgress
				+ (anim.reloadAnimationProgress - anim.lastReloadAnimationProgress) * smoothing;

		if (anim.charged < 0.9 && model.rightHandCharge && model.rightHandAmmo && anim.charged != -1.0F) {
			RenderArms.renderArmPump(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
		}
		else if (anim.pumped < 0.9 && model.rightHandBolt && model.rightHandAmmo) {
			RenderArms.renderArmBolt(model, anim, smoothing, model.rightArmChargeRot, model.rightArmChargePos);
		}
		else if (!anim.reloading) {
			RenderArms.renderArmDefault(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
		}
		else {
			RenderArms.renderArmReload(model, anim, smoothing, model.rightArmReloadRot, model.rightArmReloadPos);
		}

		GL11.glScalef(model.rightArmScale.x, model.rightArmScale.y, model.rightArmScale.z);
		modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
		modelBipedMain.bipedRightArm.offsetY = 0F;
		if (model.rightHandAmmo) {
			modelBipedMain.bipedRightArm.render(0.0625F);
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		if (anim.charged < 0.9 && model.leftHandCharge && model.leftHandAmmo && anim.charged != -1.0F) {
			RenderArms.renderArmCharge(model, anim, smoothing, model.leftArmChargeRot, model.leftArmChargePos);
		}

		else if (!anim.reloading && model.lefthandPump) {
			RenderArms.renderArmPump(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		}

		else if (!anim.reloading) {
			RenderArms.renderArmDefault(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		}
		else if (effectiveReloadAnimationProgress < 0.5 && model.stagedleftArmReloadPos.x != 0) {
			RenderArms.renderArmReload(model, anim, smoothing, model.leftArmReloadRot, model.leftArmReloadPos);
		} else if (effectiveReloadAnimationProgress > 0.5 && model.stagedleftArmReloadPos.x != 0) {
			RenderArms.renderArmReload(model, anim, smoothing, model.stagedleftArmReloadRot, model.stagedleftArmReloadPos);
		} else {
			RenderArms.renderArmReload(model, anim, smoothing, model.leftArmReloadRot, model.leftArmReloadPos);
		}

		GL11.glScalef(model.leftArmScale.x, model.leftArmScale.y, model.leftArmScale.z);
		modelBipedMain.bipedLeftArm.offsetY = 0F;
		if (model.leftHandAmmo) {
			modelBipedMain.bipedLeftArm.render(0.0625F);
		}
		GL11.glPopMatrix();

		GL11.glPopMatrix();
	}

	/** Get the end loaded distance, based on ammo type to reload */
	private float getEndLoadedDistance(AttachmentType grip, GunType gun, ItemStack gunStack)
	{
		if (grip != null && gun.getSecondaryFire(gunStack))
			return grip.model.endLoadedAmmoDistance;
		else
			return gun.model.endLoadedAmmoDistance;
	}

	/**
	 * Get the number of bullets to reload in animation, based on ammo type to
	 * reload
	 */
	//TODO
	private float getNumBulletsInReload(GunAnimations animations, AttachmentType grip, GunType gun, ItemStack gunStack)
	{
		// If this is a singles reload, we want to know the number of bullets already in the gun
		if (animations.singlesReload) {
			return animations.reloadAmmoCount;
		} else {
			return gun.model.numBulletsInReloadAnimation;

		}
		/*if (grip != null && gun.getSecondaryFire(gunStack))
			return grip.model.numBulletsInReloadAnimation;
		else
			return gun.model.numBulletsInReloadAnimation;*/
	}

	/** Get the recoil distance, based on ammo type to reload */
	private float getRecoilDistance(AttachmentType grip, GunType gun, ItemStack gunStack)
	{
		if (grip != null && gun.getSecondaryFire(gunStack))
			return grip.model.recoilDistance;
		else
			return gun.model.RecoilSlideDistance;
	}

	/** Get the recoil angle, based on ammo type to reload */
	private float getRecoilAngle(AttachmentType grip, GunType gun, ItemStack gunStack)
	{
		if (grip != null && gun.getSecondaryFire(gunStack))
			return grip.model.recoilAngle;
		else
			return gun.model.RotateSlideDistance;
	}
}
