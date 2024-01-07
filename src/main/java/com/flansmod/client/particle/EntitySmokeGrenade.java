package com.flansmod.client.particle;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.util.WorldRenderer;
import com.flansmod.common.FlansMod;
import com.flansmod.common.teams.TeamsManager;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntitySmokeGrenade extends Particle
{
	public int dischargeTime;
	public EntitySmokeGrenade(World w, double px, double py, double pz, double mx, double my, double mz)
	{
		super(w, px, py, pz, mx, my, mz);
		this.particleMaxAge *= 20;
		this.particleGravity = 1;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		this.dischargeTime = 20;
	}
	
	public int getFXLayer()
	{
			 return 3;
	}

	public float getEntityBrightness(float f)
	{
			return 1.0F;
	}
	
    public void renderParticle(float par2, float par3, float par4, float par5, float par6, float par7)
    {
        //func_98187_b() = bindTexture();
    	GL11.glPushMatrix();
    	WorldRenderer worldrenderer = FlansModClient.getWorldRenderer();
    	worldrenderer.startDrawingQuads();
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.001F);
		GL11.glEnable(GL11.GL_BLEND);
		int srcBlend = GL11.glGetInteger(GL11.GL_BLEND_SRC);
		int dstBlend = GL11.glGetInteger(GL11.GL_BLEND_DST);
		GL11.glBlendFunc(1, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDepthMask(false); 
    	FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation("flansmod", "particle/case.png"));

        float scale = 0.1F * this.particleScale;
        float xPos = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) par2 - interpPosX);
        float yPos = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) par2 - interpPosY);
        float zPos = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) par2 - interpPosZ);
        float colorIntensity = 1.0F;
        worldrenderer.tessellator.getBuffer().color(this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1.0F);

        worldrenderer.addVertexWithUV((double) (xPos - par3 * scale - par6 * scale), (double) (yPos - par4 * scale), (double) (zPos - par5 * scale - par7 * scale), 0D, 1D);
        worldrenderer.addVertexWithUV((double) (xPos - par3 * scale + par6 * scale), (double) (yPos + par4 * scale), (double) (zPos - par5 * scale + par7 * scale), 1D, 1D);
        worldrenderer.addVertexWithUV((double) (xPos + par3 * scale + par6 * scale), (double) (yPos + par4 * scale), (double) (zPos + par5 * scale + par7 * scale), 1D, 0D);
        worldrenderer.addVertexWithUV((double) (xPos + par3 * scale - par6 * scale), (double) (yPos - par4 * scale), (double) (zPos + par5 * scale - par7 * scale), 0D, 0D);
        worldrenderer.draw();
		GL11.glBlendFunc(srcBlend, dstBlend);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true); 
		GL11.glPopMatrix();
    }
	
	@Override
    public AxisAlignedBB getBoundingBox()
    {
		getBoundingBox().expand(1, 1, 1);
        return getBoundingBox();
    }

	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge)
		{
			this.setExpired();
		}
		


		this.motionY -= 0.04D * (double) this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.99;
		this.motionY *= 0.99;
		this.motionZ *= 0.99;
		
		if(world.containsAnyLiquid(this.getBoundingBox()))
		{
			this.motionY = 1;
			
		}
		
		dischargeTime --;
		
		if(dischargeTime < 0)
		{
			double dx = (this.posX-this.prevPosX);
			double dy = (this.posY-this.prevPosY);
			double dz = (this.posZ-this.prevPosZ);
			FlansMod.proxy.spawnParticle("flansmod.smokeburst",
					this.posX,
					this.posY,
					this.posZ,
					0,0,0);
			
			FlansMod.proxy.spawnParticle("flansmod.bigsmoke",
					this.posX,
					this.posY,
					this.posZ,
					0,0,0);
			setExpired();	
		}
		
		int NUM = 5;
		for(int i=0; i<NUM; i++)
		{
			double dx = (this.posX-this.prevPosX) / NUM;
			double dy = (this.posY-this.prevPosY) / NUM;
			double dz = (this.posZ-this.prevPosZ) / NUM;
			FlansMod.proxy.spawnParticle("explode",
				this.prevPosX + dx*i,
				this.prevPosY + dy*i,
				this.prevPosZ + dz*i,
				0,0,0);
		}

		if(this.onGround)
		{
			setExpired();
		}
	}
}
