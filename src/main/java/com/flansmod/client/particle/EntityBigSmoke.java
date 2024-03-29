package com.flansmod.client.particle;

import com.flansmod.common.FlansMod;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.particle.ParticleExplosion;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityBigSmoke extends ParticleExplosion
{
	public int disTime;
	public EntityBigSmoke(World w, double px, double py, double pz, double mx, double my, double mz)
	{
		super(w, px, py, pz, mx, my, mz);
		this.particleMaxAge = 300;
		this.particleGravity = 1;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		this.particleScale = 0;
		this.disTime = 10;
	}
	
	public int getFXLayer()
	{
			 return 1;
	}

	public float getEntityBrightness(float f)
	{
			return 1.0F;
	}
	
	public void renderParticle(float par2, float par3, float par4, float par5, float par6, float par7)
    {
    	//super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);
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
		
		disTime--;
		
		int NUM = 1;
		if(this.disTime < 0){
		double dx = (this.posX-this.prevPosX) / NUM;
		double dy = (this.posY-this.prevPosY) / NUM;
		double dz = (this.posZ-this.prevPosZ) / NUM;
		FlansMod.proxy.spawnParticle("flansmod.rocketexhaust",
			this.prevPosX + dx*1 + 5*Math.random(),
			this.prevPosY + dy*1 + 7*Math.random(),
			this.prevPosZ + dz*1 + 5*Math.random(),
		0,0,0);
		this.disTime = 2;
		}

		/*else if(isInWater())
		{
			motionY *= 0.89;
			motionY += 0.1;
		}*/
		
    	//FMLClientHandler.instance().getClient().renderEngine.bindTexture(icon);

	}
}
