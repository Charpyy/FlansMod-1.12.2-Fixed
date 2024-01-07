
package com.flansmod.client.model;

import com.flansmod.client.model.animation.AnimationController;
import com.flansmod.client.model.animation.AnimationPart;
import com.flansmod.common.driveables.*;
import com.flansmod.common.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.flansmod.client.handlers.FlansModResourceHandler;
import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.Paintjob;

public class RenderPlane extends Render<EntityPlane> implements CustomItemRenderer
{
	public RenderPlane(RenderManager renderManager)
	{
		super(renderManager);
		shadowSize = 1.0F;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void render(EntityPlane entityPlane, double d, double d1, double d2, float f, float f1)
	{
		if(entityPlane.getControllingPassenger() != null)
		{
			if(entityPlane.getControllingPassenger().getClass().toString().indexOf("mcheli.aircraft.MCH_EntitySeat") > 0)
			{
				return;
			}
		}

		bindEntityTexture(entityPlane);
		PlaneType type = entityPlane.getPlaneType();
		GlStateManager.pushMatrix();
		GlStateManager.translate(d, d1, d2);
		float dYaw = (entityPlane.axes.getYaw() - entityPlane.prevRotationYaw);
		while(dYaw > 180F)
		{
			dYaw -= 360F;
		}
		while(dYaw <= -180F)
		{
			dYaw += 360F;
		}
		float dPitch = (entityPlane.axes.getPitch() - entityPlane.prevRotationPitch);
		while(dPitch > 180F)
		{
			dPitch -= 360F;
		}
		while(dPitch <= -180F)
		{
			dPitch += 360F;
		}
		float dRoll = (entityPlane.axes.getRoll() - entityPlane.prevRotationRoll);
		while(dRoll > 180F)
		{
			dRoll -= 360F;
		}
		while(dRoll <= -180F)
		{
			dRoll += 360F;
		}
		GlStateManager.rotate(180F - entityPlane.prevRotationYaw - dYaw * f1, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entityPlane.prevRotationPitch + dPitch * f1, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(entityPlane.prevRotationRoll + dRoll * f1, 1.0F, 0.0F, 0.0F);
		
		/*float modelScale = type.modelScale;
		GlStateManager.scale(modelScale, modelScale, modelScale);*/
		ModelPlane model = (ModelPlane)type.model;
		if(model != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.scale(type.modelScale, type.modelScale, type.modelScale);
			model.render(entityPlane, f1);
			float dRotorAngle = entityPlane.rotorAngle - entityPlane.prevRotorAngle;
			float rotorAngle = entityPlane.prevRotorAngle + dRotorAngle*f1;
			// Render helicopter main rotors
			for(int i = 0; i < model.heliMainRotorModels.length; i++)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(model.heliMainRotorOrigins[i].x, model.heliMainRotorOrigins[i].y, model.heliMainRotorOrigins[i].z);
				GlStateManager.rotate((entityPlane.rotorAngle + f1 * entityPlane.throttle / 7F) * model.heliRotorSpeeds[i] * 1440F /3.14159265F, 0.0F, 1.0F, 0.0F);
				GlStateManager.translate(-model.heliMainRotorOrigins[i].x, -model.heliMainRotorOrigins[i].y,-model.heliMainRotorOrigins[i].z);
				model.renderRotor(entityPlane, 0.0625F, i); //work
				GlStateManager.popMatrix();
			}
			// Render helicopter tail rotors
			for(int i = 0; i < model.heliTailRotorModels.length; i++)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(model.heliTailRotorOrigins[i].x, model.heliTailRotorOrigins[i].y, model.heliTailRotorOrigins[i].z);
				GlStateManager.rotate((entityPlane.rotorAngle + f1 * entityPlane.throttle / 7F) * 1440F / 3.14159265F, 0.0F, 0.0F, 1.0F);
				GlStateManager.translate(-model.heliTailRotorOrigins[i].x, -model.heliTailRotorOrigins[i].y, -model.heliTailRotorOrigins[i].z);
				model.renderTailRotor(entityPlane, 0.0625F, i); //work
				GlStateManager.popMatrix();
			}

			Vector3f wingPos = getRenderPosition(entityPlane.wingPos, entityPlane.prevWingPos, f1);
			Vector3f wingRot = getRenderPosition(entityPlane.wingRot, entityPlane.prevWingRot, f1);
			if(entityPlane.initiatedAnim){
				AnimationController cont = entityPlane.anim;
				AnimationPart p = cont.getCorePart();
				renderAnimPart(p, new Vector3f(0,0,0), model, entityPlane, 0.0625F, f1);
			}

			//Rotate/Render left wing
			GlStateManager.pushMatrix();
			GlStateManager.translate(model.leftWingAttach.x + wingPos.x/16, model.leftWingAttach.y + wingPos.y/16, -model.leftWingAttach.z + wingPos.z/16);
			GlStateManager.rotate(wingRot.x, 1F, 0F, 0F);
			GlStateManager.rotate(wingRot.y, 0F, 1F, 0F);
			GlStateManager.rotate(wingRot.z, 0F, 0F, 1F);
			model.renderLeftWing(entityPlane, 0.0625F);
			GlStateManager.popMatrix();


			//Rotate/Render right wing
			GlStateManager.pushMatrix();
			GlStateManager.translate(model.rightWingAttach.x + wingPos.x/16, model.rightWingAttach.y + wingPos.y/16, -model.rightWingAttach.z + wingPos.z/16);
			GlStateManager.rotate(-wingRot.x, 1F, 0F, 0F);
			GlStateManager.rotate(-wingRot.y, 0F, 1F, 0F);
			GlStateManager.rotate(wingRot.z, 0F, 0F, 1F);
			model.renderRightWing(entityPlane, 0.0625F);
			GlStateManager.popMatrix();

			//Rotate/Render left wing wheel
			GlStateManager.pushMatrix();
			GlStateManager.translate(model.leftWingWheelAttach.x + entityPlane.wingWheelPos.x/16, model.leftWingWheelAttach.y+ entityPlane.wingWheelPos.y/16, -model.leftWingWheelAttach.z + entityPlane.wingWheelPos.z/16);
			GlStateManager.rotate(entityPlane.wingWheelRot.x, 1F, 0F, 0F);
			GlStateManager.rotate(entityPlane.wingWheelRot.y, 0F, 1F, 0F);
			GlStateManager.rotate(entityPlane.wingWheelRot.z, 0F, 0F, 1F);
			model.renderLeftWingWheel(entityPlane, 0.0625F);
			GlStateManager.popMatrix();

			//Rotate/Render right wing wheel
			GlStateManager.pushMatrix();
			GlStateManager.translate(model.rightWingWheelAttach.x + entityPlane.wingWheelPos.x/16, model.rightWingWheelAttach.y + entityPlane.wingWheelPos.y/16, -model.rightWingWheelAttach.z + entityPlane.wingWheelPos.z/16);
			GlStateManager.rotate(-entityPlane.wingWheelRot.x, 1F, 0F, 0F);
			GlStateManager.rotate(-entityPlane.wingWheelRot.y, 0F, 1F, 0F);
			GlStateManager.rotate(entityPlane.wingWheelRot.z, 0F, 0F, 1F);
			model.renderRightWingWheel(entityPlane, 0.0625F);
			GlStateManager.popMatrix();

			//Rotate/Render core wheel
			GlStateManager.pushMatrix();
			GlStateManager.translate(model.bodyWheelAttach.x + entityPlane.coreWheelPos.x/16, model.bodyWheelAttach.y + entityPlane.coreWheelPos.y/16, model.bodyWheelAttach.z + entityPlane.coreWheelPos.z/16);
			GlStateManager.rotate(entityPlane.coreWheelRot.x, 1F, 0F, 0F);
			GlStateManager.rotate(entityPlane.coreWheelRot.y, 0F, 1F, 0F);
			GlStateManager.rotate(entityPlane.coreWheelRot.z, 0F, 0F, 1F);
			model.renderCoreWheel(entityPlane, 0.0625F);
			GlStateManager.popMatrix();

			//Rotate/Render tail wheel
			GlStateManager.pushMatrix();
			GlStateManager.translate(model.tailWheelAttach.x + entityPlane.tailWheelPos.x/16, model.tailWheelAttach.y + entityPlane.tailWheelPos.y/16, model.tailWheelAttach.z + entityPlane.tailWheelPos.z/16);
			GlStateManager.rotate(entityPlane.tailWheelRot.x, 1F, 0F, 0F);
			GlStateManager.rotate(entityPlane.tailWheelRot.y, 0F, 1F, 0F);
			GlStateManager.rotate(entityPlane.tailWheelRot.z, 0F, 0F, 1F);
			model.renderTailWheel(entityPlane, 0.0625F);
			GlStateManager.popMatrix();

			Vector3f doorPos = getRenderPosition(entityPlane.doorPos, entityPlane.prevDoorPos, f1);
			Vector3f doorRot = getRenderPosition(entityPlane.doorRot, entityPlane.prevDoorRot, f1);


			//Rotate/Render door
			GlStateManager.pushMatrix();
			GlStateManager.translate(model.doorAttach.x + doorPos.x/16, model.doorAttach.y + doorPos.y/16, model.doorAttach.z + doorPos.z/16);
			GlStateManager.rotate(doorRot.x, 1F, 0F, 0F);
			GlStateManager.rotate(doorRot.y, 0F, 1F, 0F);
			GlStateManager.rotate(doorRot.z, 0F, 0F, 1F);
			model.renderDoor(entityPlane, 0.0625F);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}
		
		if(FlansMod.DEBUG)
		{
			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.disableDepth();
			GlStateManager.disableLighting();
			GlStateManager.color(1F, 0F, 0F, 0.3F);
			GlStateManager.scale(-1F, 1F, -1F);
			for(DriveablePart part : entityPlane.getDriveableData().parts.values())
			{
				if(part.box == null)
					continue;
				
				GlStateManager.color(1F, entityPlane.isPartIntact(part.type) ? 1F : 0F, 0F, 0.3F);
				
				ModelDriveable.renderOffsetAABB(new AxisAlignedBB(part.box.x, part.box.y, part.box.z, (part.box.x + part.box.w),
						(part.box.y + part.box.h), (part.box.z + part.box.d)), 0, 0, 0);
			}
			GlStateManager.color(1F, 1F, 0F, 0.3F);
			for(Propeller prop : type.propellers)
			{
				ModelDriveable.renderOffsetAABB(new AxisAlignedBB(prop.x / 16F - 0.25F, prop.y / 16F - 0.25F, prop.z / 16F - 0.25F,
						prop.x / 16F + 0.25F, prop.y / 16F + 0.25F, prop.z / 16F + 0.25F), 0, 0, 0);
			}
			
			// Render shoot points
			GlStateManager.color(1F, 0F, 1F, 0.3F);
			for(ShootPoint point : type.shootPointsPrimary)
			{
				DriveablePosition driveablePosition = point.rootPos;
				ModelDriveable.renderOffsetAABB(new AxisAlignedBB(
						driveablePosition.position.x - 0.25F,
						driveablePosition.position.y - 0.25F,
						driveablePosition.position.z - 0.25F,
						driveablePosition.position.x + 0.25F,
						driveablePosition.position.y + 0.25F,
						driveablePosition.position.z + 0.25F),
					0, 0, 0);
			}
			
			GlStateManager.color(0F, 1F, 0F, 0.3F);
			for(ShootPoint point : type.shootPointsSecondary)
			{
				DriveablePosition driveablePosition = point.rootPos;
				ModelDriveable.renderOffsetAABB(new AxisAlignedBB(
						driveablePosition.position.x - 0.25F,
						driveablePosition.position.y - 0.25F,
						driveablePosition.position.z - 0.25F,
						driveablePosition.position.x + 0.25F,
						driveablePosition.position.y + 0.25F,
						driveablePosition.position.z + 0.25F),
					0, 0, 0);
			}
			
			
			GlStateManager.enableTexture2D();
			GlStateManager.enableDepth();
			GlStateManager.disableBlend();
			GlStateManager.color(1F, 1F, 1F, 1F);
		}
		GlStateManager.popMatrix();
	}

	public Vector3f getRenderPosition(Vector3f current, Vector3f previous, float f)
	{
		Vector3f diff = new Vector3f(current.x - previous.x, current.y - previous.y, current.z - previous.z);

		Vector3f corrected = new Vector3f(previous.x + (diff.x*f),previous.y + (diff.y*f), previous.z + (diff.z*f));
		return corrected;
	}
	
	@Override
	public boolean shouldRender(EntityPlane entity, ICamera camera, double camX, double camY, double camZ)
	{
		return true;
	}
	
	@Override
	public void doRender(EntityPlane entity, double d, double d1, double d2, float f, float f1)
	{
		//The plane is rendered by the renderWorld Method
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPlane entity)
	{
		DriveableType type = entity.getDriveableType();
		Paintjob paintjob = type.getPaintjob(entity.getDriveableData().paintjobID);
		return FlansModResourceHandler.getPaintjobTexture(paintjob);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void renderWorld(RenderWorldLastEvent event)
	{
		//Get the world
		World world = Minecraft.getMinecraft().world;
		if(world == null)
			return;
		
		//Get the camera frustrum for clipping
		Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
		double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.getPartialTicks();
		double y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.getPartialTicks();
		double z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.getPartialTicks();
		
		//Frustum frustrum = new Frustum();
		//frustrum.setPosition(x, y, z);
		
		//Push
		GlStateManager.pushMatrix();
		//Setup lighting
		Minecraft.getMinecraft().entityRenderer.enableLightmap();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		
		RenderHelper.enableStandardItemLighting();
		
		//GlStateManager.translate(-x, -y, -z);
		for(Object entity : world.loadedEntityList)
		{
			if(entity instanceof EntityPlane)
			{
				EntityPlane plane = (EntityPlane)entity;
				int i = plane.getBrightnessForRender();
				
				if(plane.isBurning())
				{
					i = 15728880;
				}
				
				int j = i % 65536;
				int k = i / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				render(plane,
						(plane.prevPosX - x) + (plane.posX - plane.prevPosX) * event.getPartialTicks(),
						(plane.prevPosY - y) + (plane.posY - plane.prevPosY) * event.getPartialTicks(),
						(plane.prevPosZ - z) + (plane.posZ - plane.prevPosZ) * event.getPartialTicks(),
						0F,
						event.getPartialTicks());
			}
		}
		
		//Reset Lighting
		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		//Pop
		GlStateManager.popMatrix();
	}
	
	@Override
	public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data)
	{
		GlStateManager.pushMatrix();
		if(item != null && item.getItem() instanceof ItemPlane)
		{
			PlaneType planeType = ((ItemPlane)item.getItem()).type;
			if(planeType.model != null)
			{
				float scale = 0.5F;
				switch(type)
				{
					case INVENTORY:
					{
						GlStateManager.rotate(180F, 0F, 1F, 0F);
						scale = 1.0F;
						break;
					}
					case ENTITY:
					{
						scale = 1.5F;
						break;
					}
					case EQUIPPED:
					{
						GlStateManager.rotate(0F, 0F, 0F, 1F);
						GlStateManager.rotate(270F, 1F, 0F, 0F);
						GlStateManager.rotate(270F, 0F, 1F, 0F);
						GlStateManager.translate(0F, 0.25F, 0F);
						scale = 0.5F;
						break;
					}
					case EQUIPPED_FIRST_PERSON:
					{
						if(hand == EnumHand.MAIN_HAND)
						{
							GlStateManager.rotate(45F, 0F, 1F, 0F);
							GlStateManager.translate(-0.5F, 0.5F, -0.5F);
							GlStateManager.rotate(180F, 0F, 1F, 0F);
						}
						else
						{
							GlStateManager.rotate(45F, 0F, 1F, 0F);
							GlStateManager.translate(-0.5F, 0.5F, -2.3F);
							GlStateManager.rotate(180F, 0F, 1F, 0F);
						}
						scale = 1F;
						break;
					}
					default:
						break;
				}
				
				GlStateManager.scale(scale / planeType.cameraDistance, scale / planeType.cameraDistance,
						scale / planeType.cameraDistance);
				Minecraft.getMinecraft().renderEngine.bindTexture(FlansModResourceHandler.getTexture(planeType));
				ModelDriveable model = planeType.model;
				model.render(planeType);
			}
		}
		GlStateManager.popMatrix();
	}
	
	public static class Factory implements IRenderFactory<EntityPlane>
	{
		@Override
		public Render<EntityPlane> createRenderFor(RenderManager manager)
		{
			return new RenderPlane(manager);
		}
	}

	public int getPartId(int i)
	{
		/**
		 int id = 0;
		 if(i == 2) id = 0;
		 else if(i == 1) id = 1;
		 else if(i == 0) id = 2;
		 else id = i;
		 */
		int id = i;
		return id;
	}

	public void renderAnimPart(AnimationPart p, Vector3f parent, ModelPlane mod, EntityPlane plane, float f5, float f1)
	{
		Vector3f pos = Vector3f.sub(p.position, parent, null);
		Vector3f offset = Interpolate(p.offset, p.prevOff, f1);
		Vector3f rotation = Interpolate(p.rotation, p.prevRot, f1);
		GlStateManager.pushMatrix();
		GlStateManager.translate(pos.x/16F, -pos.y/16F, -pos.z/16F);
		GlStateManager.rotate(rotation.x, 1, 0, 0);
		GlStateManager.rotate(rotation.y, 0, 1, 0);
		GlStateManager.rotate(rotation.z, 0, 0, 1);
		GlStateManager.translate(offset.x/16F, offset.y/16F, offset.z/16F);
		int i = getPartId(p.type);
		mod.renderValk(plane, f5, i);
		if(p.hasChildren){
			for(AnimationPart p2:p.children)
			{
				renderAnimPart(p2, p.position, mod, plane, f5, f1);
			}
		}
		GlStateManager.popMatrix();

	}

	public Vector3f Interpolate(Vector3f current, Vector3f prev, float f1)
	{
		Vector3f result;
		result = new Vector3f(prev.x + (current.x-prev.x)*f1,prev.y + (current.y-prev.y)*f1, prev.z + (current.z-prev.z)*f1);
		return result;
	}
}
