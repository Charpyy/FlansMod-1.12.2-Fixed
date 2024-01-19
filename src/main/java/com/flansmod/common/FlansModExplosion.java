package com.flansmod.common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.flansmod.common.network.PacketParticle;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;

import com.flansmod.common.guns.EntityDamageSourceFlan;
import com.flansmod.common.teams.TeamsManager;
import com.flansmod.common.types.InfoType;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import static com.flansmod.common.CSVWriter.writeDataToCSV;


public class  FlansModExplosion extends Explosion
{
	
	private boolean causesFire;
	private boolean breaksBlocks;
	private final Random random;
	private final World world;
	private final double x, y, z;
	private final Optional<? extends EntityPlayer> player;
	private final Entity explosive;
	private final float radius;
	private final List<BlockPos> affectedBlockPositions;
	private final Map<EntityPlayer, Vec3d> playerKnockbackMap;
	private final Vec3d position;
	private final InfoType type; // type of Flan's Mod weapon causing explosion

	private final static int boomRadius = 16;
	private static final Random explosionRNG = new Random();
	private float power;
	private final float damageVsLiving;
	private final float damageVsPlayer;
	private final float damageVsPlane;
	private final float damageVsVehicle;
	public boolean breakBlocks;
	public boolean canceled = false;

	public boolean isSmoking = true;

	public FlansModExplosion(World world, Entity entity, Optional<? extends EntityPlayer> player, InfoType type, double x, double y, double z, float size, boolean causesFire, boolean smoking, boolean breaksBlocks)
	{
		this(world, entity, player, type, x, y, z, size, 1, breaksBlocks, 20, 20, 20, 20, smoking ? 1 : 0, 1);
		this.causesFire = causesFire;
		this.breaksBlocks = breaksBlocks && TeamsManager.explosions;
	}




	public FlansModExplosion(World world, Entity entity, Optional<? extends EntityPlayer> player, InfoType type,
		double x, double y, double z, float explosionRadius, float explosionPower, boolean breakBlocks,
		float damageLiving, float damagePlayer, float damagePlane, float damageVehicle, int smokeCount, int debrisCount)
	{
		super(world, entity, x, y, z, explosionRadius, false, smokeCount > 0);
		this.radius = explosionRadius;
		this.power = explosionPower;
		this.random = new Random();
		this.affectedBlockPositions = Lists.newArrayList();
		this.playerKnockbackMap = Maps.newHashMap();
		this.world = world;
		this.player = player;
		this.x = x;
		this.y = y;
		this.z = z;
		this.breaksBlocks = TeamsManager.explosions;
		this.position = new Vec3d(this.x, this.y, this.z);
		this.type = type;
		//String data = type.toString();
		//writeDataToCSV(data);
		this.explosive = entity;
		this.causesFire = false;
		this.isSmoking = (smokeCount > 0);
		this.breakBlocks = breakBlocks;
		damageVsPlayer = damagePlayer;
		damageVsLiving  = damageLiving;
		damageVsPlane   = damagePlane;
		damageVsVehicle = damageVehicle;
		
		if(!ForgeEventFactory.onExplosionStart(world, this))
		{
			this.doExplosionA();
			this.doExplosionB(isSmoking);
			spawnParticle(smokeCount, debrisCount);

			canceled = net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, this);
			
			for(EntityPlayer obj : world.playerEntities)
			{
				FlansMod.getPacketHandler().sendTo(new SPacketExplosion(x, y, z, radius, affectedBlockPositions, getPlayerKnockbackMap().get(obj)), (EntityPlayerMP)obj);
			}
		}
	}

	/**
	 * Does the first part of the explosion (destroy blocks)
	 */
	@Override
	public void doExplosionA()
	{
		Set<BlockPos> set = Sets.newHashSet();
		
		if(breaksBlocks)
		{
			for(int j = 0; j < 16; ++j)
			{
				for(int k = 0; k < 16; ++k)
				{
					for(int l = 0; l < 16; ++l)
					{
						if(j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15)
						{
							double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
							double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
							double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
							double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
							d0 /= d3;
							d1 /= d3;
							d2 /= d3;
							float f = this.radius * (0.7F + this.world.rand.nextFloat() * 0.6F);
							double d4 = this.x;
							double d6 = this.y;
							double d8 = this.z;
							
							for(; f > 0.0F; f -= 0.22500001F)
							{
								BlockPos blockpos = new BlockPos(d4, d6, d8);
								IBlockState iblockstate = this.world.getBlockState(blockpos);
								
								if(iblockstate.getMaterial() != Material.AIR)
								{
									float f2 = this.explosive != null ? this.explosive.getExplosionResistance(this, this.world, blockpos, iblockstate) : iblockstate.getBlock().getExplosionResistance(world, blockpos, null, this);
									f -= (f2 + 0.3F) * 0.3F;
								}
								
								if(f > 0.0F && (this.explosive == null || this.explosive.canExplosionDestroyBlock(this, this.world, blockpos, iblockstate, f)))
								{
									set.add(blockpos);
								}
								
								d4 += d0 * 0.30000001192092896D;
								d6 += d1 * 0.30000001192092896D;
								d8 += d2 * 0.30000001192092896D;
							}
						}
					}
				}
			}
		}
		
		this.affectedBlockPositions.addAll(set);
		float f3 = this.radius * 2.0F;
		int k1 = MathHelper.floor(this.x - (double)f3 - 1.0D);
		int l1 = MathHelper.floor(this.x + (double)f3 + 1.0D);
		int i2 = MathHelper.floor(this.y - (double)f3 - 1.0D);
		int i1 = MathHelper.floor(this.y + (double)f3 + 1.0D);
		int j2 = MathHelper.floor(this.z - (double)f3 - 1.0D);
		int j1 = MathHelper.floor(this.z + (double)f3 + 1.0D);
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.explosive, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, f3);
		Vec3d vec3d = new Vec3d(this.x, this.y, this.z);
		
		for(Entity entity : list)
		{
			if(!entity.isImmuneToExplosions())
			{
				double d12 = entity.getDistance(this.x, this.y, this.z) / (double)f3;
				
				if(d12 <= 1.0D)
				{
					double d5 = entity.posX - this.x;
					double d7 = entity.posY + (double)entity.getEyeHeight() - this.y;
					double d9 = entity.posZ - this.z;
					double d13 = (double)MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
					
					if(d13 != 0.0D)
					{
						d5 /= d13;
						d7 /= d13;
						d9 /= d13;
						double d14 = (double)this.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
						double d10 = (1.0D - d12) * d14;
						//if(player.isPresent())
						//{
						//entity.attackEntityFrom(new EntityDamageSourceFlan(type.shortName, explosive, player.get(), type).setExplosion(),
						//(float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f3 + 1.0D)));
						//} else {
						entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f3 + 1.0D)));
						//}
						double d11 = d10;
						
						if(entity instanceof EntityLivingBase)
						{
							d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d10);
						}
						
						entity.motionX += d5 * d11;
						entity.motionY += d7 * d11;
						entity.motionZ += d9 * d11;
						
						if(entity instanceof EntityPlayer)
						{
							EntityPlayer entityplayer = (EntityPlayer)entity;
							
							if(!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying))
							{
								this.playerKnockbackMap.put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Does the second part of the explosion (sound, particles, drop spawn)
	 */
	public void doExplosionB(boolean spawnParticles)
	{
		this.world.playSound(null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
		
		if(this.radius >= 2.0F && this.breaksBlocks)
		{
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
		}
		else
		{
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
		}
		
		if(this.breaksBlocks)
		{
			for(BlockPos blockpos : this.affectedBlockPositions)
			{
				IBlockState iblockstate = this.world.getBlockState(blockpos);
				Block block = iblockstate.getBlock();
				
				if(spawnParticles)
				{
					double d0 = (double)((float)blockpos.getX() + this.world.rand.nextFloat());
					double d1 = (double)((float)blockpos.getY() + this.world.rand.nextFloat());
					double d2 = (double)((float)blockpos.getZ() + this.world.rand.nextFloat());
					double d3 = d0 - this.x;
					double d4 = d1 - this.y;
					double d5 = d2 - this.z;
					double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
					d3 /= d6;
					d4 /= d6;
					d5 /= d6;
					double d7 = 0.5D / (d6 / (double)this.radius + 0.1D);
					d7 *= (double)(this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
					d3 *= d7;
					d4 *= d7;
					d5 *= d7;
					this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.x) / 2.0D, (d1 + this.y) / 2.0D, (d2 + this.z) / 2.0D, d3, d4, d5);
					this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
				}
				
				if(iblockstate.getMaterial() != Material.AIR)
				{
					if(block.canDropFromExplosion(this))
					{
						block.dropBlockAsItemWithChance(this.world, blockpos, this.world.getBlockState(blockpos), 1.0F / this.radius, 0);
					}
					
					block.onBlockExploded(this.world, blockpos, this);
				}
			}
		}
		
		if(this.causesFire)
		{
			for(BlockPos blockpos1 : this.affectedBlockPositions)
			{
				if(this.world.getBlockState(blockpos1).getMaterial() == Material.AIR && this.world.getBlockState(blockpos1.down()).isFullBlock() && this.random.nextInt(3) == 0)
				{
					this.world.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
				}
			}
		}
	}
	
	@Override
	public Map<EntityPlayer, Vec3d> getPlayerKnockbackMap()
	{
		return this.playerKnockbackMap;
	}
	
	@Override
	public void clearAffectedBlockPositions()
	{
		this.affectedBlockPositions.clear();
	}
	
	@Override
	public List<BlockPos> getAffectedBlockPositions()
	{
		return this.affectedBlockPositions;
	}
	
	@Override
	public Vec3d getPosition()
	{
		return this.position;
	}

	public void spawnParticle(int numSmoke, int numDebris)
	{
		float mod = radius * 0.1F;

		for(int smoke = 0; smoke < numSmoke; smoke++)
		{
			float smokeRand = (float)Math.random();

			if(smokeRand < 0.25)
			{
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.flare", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.flare", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod);
			} else if (smokeRand > 0.25 && smokeRand < 0.5){
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.flare", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.flare", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod);
			} else if (smokeRand > 0.5 && smokeRand < 0.75){
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.flare", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.flare", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod);
			} else if (smokeRand > 0.75){
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.flare", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.flare", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod);
			}

		}

		for(int debris = 0; debris < numDebris; debris++)
		{

			float smokeRand = (float)Math.random();

			if(smokeRand < 0.25)
			{
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.debris1", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.debris1", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod);
			} else if (smokeRand > 0.25 && smokeRand < 0.5){
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.debris1", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.debris1", x, y, z, (float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod);
			} else if (smokeRand > 0.5 && smokeRand < 0.75){
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.debris1", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.debris1", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, (float)Math.random()*mod);
			} else if (smokeRand > 0.75){
				FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.debris1", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod), x, y, z, 150, world.provider.getDimension());
				//FlansMod.proxy.spawnParticle("flansmod.debris1", x, y, z, -(float)Math.random()*mod, (float)Math.random()*mod, -(float)Math.random()*mod);
			}
		}
	}

	public float getBlockDensity(Vec3d p_72842_1_, AxisAlignedBB p_72842_2_)
	{
		double d0 = 1.0D / ((p_72842_2_.maxX - p_72842_2_.minX) * 2.0D + 1.0D);
		double d1 = 1.0D / ((p_72842_2_.maxY - p_72842_2_.minY) * 2.0D + 1.0D);
		double d2 = 1.0D / ((p_72842_2_.maxZ - p_72842_2_.minZ) * 2.0D + 1.0D);

		if(d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D)
		{
			int i = 0;
			int j = 0;

			for (float f = 0.0F; f <= 1.0F; f = (float) ((double) f + d0))
			{
				for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) ((double) f1 + d1))
				{
					for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) ((double) f2 + d2))
					{
						double d3 = p_72842_2_.minX + (p_72842_2_.maxX - p_72842_2_.minX) * (double) f;
						double d4 = p_72842_2_.minY + (p_72842_2_.maxY - p_72842_2_.minY) * (double) f1;
						double d5 = p_72842_2_.minZ + (p_72842_2_.maxZ - p_72842_2_.minZ) * (double) f2;

						if(this.world.rayTraceBlocks(new Vec3d(d3, d4, d5), p_72842_1_, false, true, false) == null)
						{
							++i;
						}

						++j;
					}
				}
			}

			return (float) i / (float) j;
		}
		else
		{
			return 0.0F;
		}
	}

	public static void clientExplosion(World worldObj, float explosionSize,
									   double explosionX, double explosionY, double explosionZ)
	{
		List affectedBlockPositions = new ArrayList();
		Entity exploder = null;

		Explosion explosion = new Explosion(worldObj, exploder, explosionX, explosionY, explosionZ, explosionSize, false, false);

		if(explosionSize < 2)
		{
			explosionX += explosionRNG.nextFloat() - 0.5F;
			explosionZ += explosionRNG.nextFloat() - 0.5F;
		}

		boolean isSmoking = true;

		//	doExplosionA
		{
			final float f = explosionSize;
			HashSet hashset = new HashSet();
			int i;
			int j;
			int k;
			double d5;
			double d6;
			double d7;

			for (i = 0; i < boomRadius; ++i)
			{
				for (j = 0; j < boomRadius; ++j)
				{
					for (k = 0; k < boomRadius; ++k)
					{
						if (i == 0 || i == boomRadius - 1 || j == 0 || j == boomRadius - 1 || k == 0 || k == boomRadius - 1)
						{
							double d0 = (double)((float)i / ((float)boomRadius - 1.0F) * 2.0F - 1.0F);
							double d1 = (double)((float)j / ((float)boomRadius - 1.0F) * 2.0F - 1.0F);
							double d2 = (double)((float)k / ((float)boomRadius - 1.0F) * 2.0F - 1.0F);
							double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
							d0 /= d3;
							d1 /= d3;
							d2 /= d3;
							float f1 = explosionSize * (0.7F + worldObj.rand.nextFloat() * 0.6F);
							d5 = explosionX;
							d6 = explosionY;
							d7 = explosionZ;

							for (float f2 = 0.3F; f1 > 0.0F; f1 -= f2 * 0.75F)
							{
								int j1 = MathHelper.floor(d5);
								int k1 = MathHelper.floor(d6);
								int l1 = MathHelper.floor(d7);
								Block block = worldObj.getBlockState(new BlockPos(j1, k1, l1)).getBlock();

//								if (block.getMaterial() != Material.air)
								{
									float f3 = 0;//exploder != null ? exploder.func_145772_a(explosion, worldObj, j1, k1, l1, block) : block.getExplosionResistance(exploder, worldObj, j1, k1, l1, explosionX, explosionY, explosionZ);
									f1 -= (f3 + 0.3F) * f2;
								}

								if (f1 > 0.0F && (exploder == null || exploder.canExplosionDestroyBlock(explosion, worldObj, new BlockPos(j1, k1, l1), block.getDefaultState(), f1)))
								{
									hashset.add(new ChunkPos(new BlockPos(j1, k1, l1)));
								}

								d5 += d0 * (double)f2;
								d6 += d1 * (double)f2;
								d7 += d2 * (double)f2;
							}
						}
					}
				}
			}

			affectedBlockPositions.addAll(hashset);
			explosionSize *= 2.0F;
			i = MathHelper.floor(explosionX - (double)explosionSize - 1.0D);
			j = MathHelper.floor(explosionX + (double)explosionSize + 1.0D);
			k = MathHelper.floor(explosionY - (double)explosionSize - 1.0D);
			int i2 = MathHelper.floor(explosionY + (double)explosionSize + 1.0D);
			int l = MathHelper.floor(explosionZ - (double)explosionSize - 1.0D);
			int j2 = MathHelper.floor(explosionZ + (double)explosionSize + 1.0D);
			List list = worldObj.getEntitiesWithinAABBExcludingEntity(exploder, new AxisAlignedBB((double)i, (double)k, (double)l, (double)j, (double)i2, (double)j2));
			Vec3d vec3 = new Vec3d(explosionX, explosionY, explosionZ);

			for (int i1 = 0; i1 < list.size(); ++i1)
			{
				Entity entity = (Entity)list.get(i1);
				double d4 = entity.getDistance(explosionX, explosionY, explosionZ) / (double)explosionSize;

				if (d4 <= 1.0D)
				{
					d5 = entity.posX - explosionX;
					d6 = entity.posY + (double)entity.getEyeHeight() - explosionY;
					d7 = entity.posZ - explosionZ;
					double d9 = (double)MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);

					if (d9 != 0.0D)
					{
						d5 /= d9;
						d6 /= d9;
						d7 /= d9;
						double d10 = (double)worldObj.getBlockDensity(vec3, entity.getEntityBoundingBox());
						double d11 = (1.0D - d4) * d10;
//						entity.attackEntityFrom(DamageSource.setExplosionSource(explosion), (float)((int)((d11 * d11 + d11) / 2.0D * 8.0D * (double)explosionSize + 1.0D)));
						double d8 = 1.0D;
						if (entity instanceof EntityLivingBase)
							d8 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, d11);
						entity.motionX += d5 * d8;
						entity.motionY += d6 * d8;
						entity.motionZ += d7 * d8;
					}
				}
			}

			explosionSize = f;
		}
		//	doExplosionB
		{
//			worldObj.playSoundEffect(explosionX, explosionY, explosionZ, "random.explode", 4.0F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

			if (explosionSize >= 2.0F && isSmoking)
			{
				worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, explosionX, explosionY, explosionZ, 1.0D, 0.0D, 0.0D);
			}
			else
			{
				worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, explosionX, explosionY, explosionZ, 1.0D, 0.0D, 0.0D);
			}

			Iterator iterator = affectedBlockPositions.iterator();

			int cnt = 0;
			while (iterator.hasNext())
			{
				cnt++;

				ChunkPos chunkposition = (ChunkPos)iterator.next();
				int i = getChunk(worldObj, chunkposition.x, chunkposition.z).getX();
				int j = getChunk(worldObj, chunkposition.x, chunkposition.z).getY();
				int k = getChunk(worldObj, chunkposition.x, chunkposition.z).getZ();
				Block block = worldObj.getBlockState(new BlockPos(i, j, k)).getBlock();

				double d0 = (double)((float)i + worldObj.rand.nextFloat());
				double d1 = (double)((float)j + worldObj.rand.nextFloat());
				double d2 = (double)((float)k + worldObj.rand.nextFloat());
				double d3 = d0 - explosionX;
				double d4 = d1 - explosionY;
				double d5 = d2 - explosionZ;
				double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
				d3 /= d6;
				d4 /= d6;
				d5 /= d6;
				double d7 = 0.5D / (d6 / (double)explosionSize + 0.1D);
				d7 *= (double)(worldObj.rand.nextFloat() * worldObj.rand.nextFloat() + 0.3F);
				d3 *= d7;
				d4 *= d7;
				d5 *= d7;

				/*
				if(false)
				{
					FlansMod.proxy.spawnParticle("explode", (d0 + explosionX * 1.0D) / 2.0D, (d1 + explosionY * 1.0D) / 2.0D, (d2 + explosionZ * 1.0D) / 2.0D, d3, d4, d5);
					FlansMod.proxy.spawnParticle("smoke", d0, d1, d2, d3, d4, d5);
				}
				else
				*/
				{
					if((explosionSize<=1 && cnt % 4==0) || explosionSize>1)
					{
						FlansMod.proxy.spawnParticle("explode", (d0 + explosionX * 1.0D) / 2.0D, (d1 + explosionY * 1.0D) / 2.0D, (d2 + explosionZ * 1.0D) / 2.0D, d3, d4, d5);
					}
					//				FlansMod.proxy.spawnParticle("smoke", d0, d1, d2, d3, d4, d5);


					block = Blocks.AIR;
					if(explosionSize <= 2)
					{
						if(cnt % 8==0)
						{
							block = getNearBlock(worldObj, i, j, k);
						}
					}
					else
					{
						block = getNearBlock(worldObj, i, j, k);
					}

					if(block != Blocks.AIR)
					{
						float m = explosionSize;
						if(m <= 1)
						{
							m *= 2;
						}
						else
						{
							m *= 0.5F;
						}
						final String pname = "blockdust_" + Block.getIdFromBlock(block) + "_" + worldObj.getBlockState(new BlockPos(i, j, k)).getBlock().getMetaFromState(worldObj.getBlockState(new BlockPos(i, j, k)));
						FlansMod.proxy.spawnParticle(pname,
								(d0 + explosionX * 1.0D) / 2.0D,
								(d1 + explosionY * 1.0D) / 2.0D,
								(d2 + explosionZ * 1.0D) / 2.0D,
								d3 * m,
								d4 * m,
								d5 * m);
					}
				}
			}
		}
	}

	protected static BlockPos getChunk(World world, int x, int z){
		Chunk chunk = world.getChunk(x, z);
		int k = x * 16 + world.rand.nextInt(16);
		int l = z * 16 + world.rand.nextInt(16);
		int i1 = MathHelper.roundUp(chunk.getHeight(new BlockPos(k, 0, l)) + 1, 16);
		int j1 = world.rand.nextInt(i1 > 0 ? i1 : chunk.getTopFilledSegment() + 16 - 1);
		return new BlockPos(k, j1, l);
	}

	public static Block getNearBlock(World w, int x, int y, int z)
	{
		final int[][] offset = new int[][]
				{
						{ 0,-1, 0},
						{ 1, 0, 0},
						{-1, 0, 0},
						{ 0, 0, 1},
						{ 0, 0,-1},
				};
		for(int i=0; i<offset.length; i++)
		{
			if(y > 1 || offset[i][1]==0)
			{
				Block block = w.getBlockState(new BlockPos(x + offset[i][0], y + offset[i][1], z + offset[i][2])).getBlock();
				if(block != Blocks.AIR)
				{
					return block;
				}
			}
		}
		return Blocks.AIR;
	}
}
