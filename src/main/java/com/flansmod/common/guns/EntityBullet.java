package com.flansmod.common.guns;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.flansmod.client.particle.EntityFMTracer;
import com.flansmod.common.FlansModExplosion;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.driveables.EntitySeat;
import com.flansmod.common.eventhandlers.BulletHitEvent;
import com.flansmod.common.network.PacketHitMarker;
import com.flansmod.common.network.PacketPlaySound;
import com.flansmod.common.teams.TeamsManager;
import javafx.scene.paint.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.flansmod.client.FlansModClient;
import com.flansmod.client.debug.EntityDebugVector;
import com.flansmod.client.handlers.FlansModResourceHandler;
import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.EntityPlane;
import com.flansmod.common.driveables.EntityVehicle;
import com.flansmod.common.driveables.mechas.EntityMecha;
import com.flansmod.common.guns.raytracing.FlansModRaytracer;
import com.flansmod.common.guns.raytracing.FlansModRaytracer.BulletHit;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.vector.Vector3f;

import io.netty.buffer.ByteBuf;

public class EntityBullet extends EntityShootable implements IEntityAdditionalSpawnData {
	public Entity owner;
	public int pingOfShooter = 0;
	private static final DataParameter<String> BULLET_TYPE = EntityDataManager.createKey(EntityBullet.class, DataSerializers.STRING);

	private int bulletLife = 600; // Kill bullets after 30 seconds
	public int ticksInAir;

	private FiredShot shot;
	/**
	 * For homing missiles
	 */
	public Entity lockedOnTo;

	private float currentPenetratingPower;

	public BulletType type;

	public InfoType firedFrom;

	public ShootableType getType() {
		return type;
	}

	/**
	 * What type of weapon did this come from? For death messages
	 */

	public boolean detonated = false;
	/**
	 * For homing missiles
	 */

	public double prevDistanceToEntity = 0;
	public boolean toggleLock = false;

	public double thisSpeed = 0;
	public int closeCount = 0;
	public int soundTime = 0;
	//Used to store speed for submunitions
	public float speedA;
	public float initialSpeed;

	public int impactX;
	public int impactY;
	public int impactZ;

	public boolean isFirstPositionSetting = false;
	public boolean isPositionUpper = true;

	// Hitmarker information on the server side.
	// Moved to FiredShot
	/*public boolean lastHitHeadshot = false;
	public float lastHitPenAmount = 1F;*/

	public int submunitionDelay = 20;
	public boolean hasSetSubDelay = false;

	public boolean hasSetVLSDelay = false;
	public int VLSDelay = 0;

	public Vector3f lookVector;
	public Vector3f initialPos;
	public boolean hasSetLook = false;

	public boolean initialTick = true;

	private double getPrevDistanceToTarget;

	@SideOnly(Side.CLIENT)
	private boolean playedFlybySound;

	/**
	 * These values are used to store the UUIDs until the next entity update is performed. This prevents issues caused by the loading order
	 */
	private UUID playeruuid;
	private UUID shooteruuid;
	private boolean checkforuuids;

	public EntityBullet(World world) {
		super(world);
		setSize(0.5F, 0.5F);
	}

	public EntityBullet(World world, FiredShot shot, Vec3d origin, Vec3d direction) {
		this(world);
		ticksInAir = 0;
		this.shot = shot;
		if (shot.getShooterOptional().isPresent())
			this.owner = shot.getShooterOptional().get();
		this.dataManager.set(BULLET_TYPE, shot.getBulletType().shortName);
		firedFrom = shot.getFireableGun().getInfoType();
		setPosition(origin.x, origin.y, origin.z);
		motionX = direction.x;
		motionY = direction.y;
		motionZ = direction.z;
		setArrowHeading(motionX, motionY, motionZ, shot.getFireableGun().getGunSpread() * shot.getBulletType().bulletSpread, shot.getFireableGun().getBulletSpeed());

		currentPenetratingPower = shot.getBulletType().penetratingPower;
		setSize(shot.getBulletType().hitBoxSize, shot.getBulletType().hitBoxSize);
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(BULLET_TYPE, null);
	}

	public void setArrowHeading(double d, double d1, double d2, float spread, float speed) {
		spread /= 5F;
		float f2 = MathHelper.sqrt(d * d + d1 * d1 + d2 * d2);
		d /= f2;
		d1 /= f2;
		d2 /= f2;
		d *= speed;
		d1 *= speed;
		d2 *= speed;
		d += rand.nextGaussian() * 0.005D * spread * speed;
		d1 += rand.nextGaussian() * 0.005D * spread * speed;
		d2 += rand.nextGaussian() * 0.005D * spread * speed;
		motionX = d;
		motionY = d1;
		motionZ = d2;
		float f3 = MathHelper.sqrt(d * d + d2 * d2);
		prevRotationYaw = rotationYaw = (float) ((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
		prevRotationPitch = rotationPitch = (float) ((Math.atan2(d1, f3) * 180D) / 3.1415927410125732D);

		getLockOnTarget();
	}

	/**
	 * Find the entity nearest to the missile's trajectory, anglewise
	 */
	private void getLockOnTarget() {
		BulletType type = shot.getBulletType();

		if (type.lockOnToPlanes || type.lockOnToVehicles || type.lockOnToMechas || type.lockOnToLivings || type.lockOnToPlayers) {
			Vector3f motionVec = new Vector3f(motionX, motionY, motionZ);
			Entity closestEntity = null;
			float closestAngle = type.maxLockOnAngle * 3.14159265F / 180F;

			for (Entity entity : world.loadedEntityList) {
				String etype = entity.getEntityData().getString("EntityType");
				if ((type.lockOnToMechas && entity instanceof EntityMecha)
						|| (type.lockOnToVehicles && entity instanceof EntityVehicle)
						|| (type.lockOnToVehicles && etype.equals("Vehicle")) // for vehicle of other Mod
						|| (type.lockOnToPlanes && entity instanceof EntityPlane)
						|| (type.lockOnToPlanes && etype.equals("Plane")) // for plane of other Mod
						|| (type.lockOnToPlayers && entity instanceof EntityPlayer)
						|| (type.lockOnToLivings && entity instanceof EntityLivingBase)) {
					Vector3f relPosVec = new Vector3f(entity.posX - posX, entity.posY - posY, entity.posZ - posZ);
					float angle = Math.abs(Vector3f.angle(motionVec, relPosVec));
					if (angle < closestAngle) {
						closestEntity = entity;
						closestAngle = angle;
					}
				}
			}

			if (closestEntity != null)
				lockedOnTo = closestEntity;
		}
	}

	@Override
	public void setVelocity(double d, double d1, double d2) {
		motionX = d;
		motionY = d1;
		motionZ = d2;
		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(d * d + d2 * d2);
			prevRotationYaw = rotationYaw = (float) ((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
			prevRotationPitch = rotationPitch = (float) ((Math.atan2(d1, f) * 180D) / 3.1415927410125732D);
			setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		/**if (initialTick) {
		 initialSpeed = (float)Math.sqrt((motionX * motionX) + (motionY * motionY) + (motionZ * motionZ));
		 initialTick = false;
		 }

		 // Update the ping for hit detection
		 if (!world.isRemote && owner instanceof EntityPlayerMP) {
		 pingOfShooter = ((EntityPlayerMP)owner).ping;
		 }

		 prevPosX = posX;
		 prevPosY = posY;
		 prevPosZ = posZ;
		 if (type == null) {
		 FlansMod.log("EntityBullet.onUpdate() Error: BulletType is null (" + this + ")");
		 setDead();
		 return;
		 }


		 if (type.despawnTime > 0 && ticksExisted > type.despawnTime) {
		 detonated = true;
		 setDead();
		 return;
		 }

		 if (!hasSetSubDelay && type.hasSubmunitions) {
		 setSubmunitionDelay();
		 } else if (type.hasSubmunitions) {
		 submunitionDelay--;
		 }

		 if (!hasSetVLSDelay && type.VLS) {
		 VLSDelay = type.VLSTime;
		 hasSetVLSDelay = true;
		 }

		 if (VLSDelay > 0)
		 VLSDelay--;

		 if (!hasSetLook && owner != null) {
		 lookVector = new Vector3f((float) owner.getLookVec().x, (float) owner.getLookVec().y, (float) owner.getLookVec().z);
		 initialPos = new Vector3f(owner.posX, owner.posY, owner.posZ);
		 hasSetLook = true;
		 }


		 if (soundTime > 0)
		 soundTime--;

		 if (owner != null) {
		 double rangeX = owner.posX - this.posX;
		 double rangeY = owner.posY - this.posY;
		 double rangeZ = owner.posZ - this.posZ;
		 double range = Math.sqrt((rangeX * rangeX) + (rangeY * rangeY) + (rangeZ * rangeZ));

		 if (type.maxRange != -1 && type.maxRange < range) {
		 if (ticksExisted > type.fuse && type.fuse > 0)
		 detonate();
		 setDead();
		 }
		 } else {
		 this.setDead();
		 }**/

		try {
			//This checks if the shooter and/or player can be found. If they are loaded/online they will be included in the FiredShot data, if not this data will be deleted/ignored
			if (checkforuuids) {
				EntityPlayerMP player = null;
				Entity shooter = null;

				if (playeruuid != null) {
					for (Entity entity : world.loadedEntityList) {
						if (entity.getUniqueID().equals(playeruuid) && entity instanceof EntityPlayerMP) {
							player = (EntityPlayerMP) entity;
							break;
						}
					}
					playeruuid = null;
				}

				if (shooteruuid != null) {
					if (player != null && shooteruuid.equals(player.getUniqueID())) {
						shooter = player;
					} else {
						for (Entity entity : world.loadedEntityList) {
							if (entity.getUniqueID().equals(shooteruuid)) {
								shooter = entity;
								break;
							}
						}
					}
					shooteruuid = null;
				}

				if (shooter != null) {
					shot = new FiredShot(shot.getFireableGun(), shot.getBulletType(), shooter, player);
				}

				checkforuuids = false;
			}

			BulletType type = this.getFiredShot().getBulletType();

			// Movement dampening variables
			float drag = 0.99F;
			float gravity = 0.02F;
			// If the bullet is in water, spawn particles and increase the drag
			if (isInWater()) {
				if (world.isRemote) {
					for (int i = 0; i < 4; i++) {
						float bubbleMotion = 0.25F;
						world.spawnParticle(EnumParticleTypes.WATER_BUBBLE,
								posX - motionX * bubbleMotion,
								posY - motionY * bubbleMotion,
								posZ - motionZ * bubbleMotion, motionX, motionY, motionZ);
					}
				}
				drag = type.dragInWater;
			}
			if (!type.torpedo) {
				motionX *= drag;
				motionY *= drag;
				motionZ *= drag;
				motionY -= gravity * type.fallSpeed;
			}

			// Damp penetration too
			currentPenetratingPower *= (1 - type.penetrationDecay);


			// Apply motion
			this.setPosition(posX + motionX, posY + motionY, posZ + motionZ);

			// Recalculate the angles from the new motion
			float motionXZ = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
			rotationYaw = (float) ((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
			rotationPitch = (float) ((Math.atan2(motionY, motionXZ) * 180D) / 3.1415927410125732D);
			// Reset the range of the angles
			for (; rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) {
			}
			for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) {
			}
			for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) {
			}
			for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) {
			}
			rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
			rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;


			if (world.isRemote) {
				onUpdateClient();
				return;
			}

			if (FlansMod.DEBUG) {
				world.spawnEntity(new EntityDebugVector(world, new Vector3f(posX, posY, posZ),
						new Vector3f(motionX, motionY, motionY), 1000));

			}

			// Check the fuse to see if the bullet should explode
			ticksInAir++;
			if (ticksInAir > type.fuse && type.fuse > 0 && !isDead) {
				setDead();
			}

			if (ticksExisted > bulletLife) {
				setDead();
			}

			if (isDead)
				return;

			//Detonation conditions
			if (!world.isRemote) {
				if (ticksExisted > type.fuse && type.fuse > 0)
					detonate();
				//If this grenade has a proximity trigger, check for living entities within it's range
				if (type.livingProximityTrigger > 0 || type.driveableProximityTrigger > 0) {
					float checkRadius = Math.max(type.livingProximityTrigger, type.driveableProximityTrigger);
					List list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(checkRadius, checkRadius, checkRadius));
					for (Object obj : list) {
						if (obj == owner && ticksExisted < 10)
							continue;
						if (obj instanceof EntityLivingBase && getDistance((Entity) obj) < type.livingProximityTrigger) {
							//If we are in a gametype and both thrower and triggerer are playing, check for friendly fire
							if (TeamsManager.getInstance() != null && TeamsManager.getInstance().currentRound != null && obj instanceof EntityPlayerMP && owner instanceof EntityPlayer) {
								if (!TeamsManager.getInstance().currentRound.gametype.playerAttacked((EntityPlayerMP) obj, new EntityDamageSourceFlan(type.shortName, this, (EntityPlayer) owner, type, false, false)))
									continue;
							}
							if (type.damageToTriggerer > 0)
								((EntityLivingBase) obj).attackEntityFrom(getBulletDamage(false), type.damageToTriggerer);
							FlansMod.proxy.spawnParticle("redstone", posX, posY, posZ, 0, 0, 0);

							detonate();
							break;
						}
						if (obj instanceof EntityDriveable && getDistance((Entity) obj) < type.driveableProximityTrigger) {
							/**
							 if(TeamsManager.getInstance() != null && TeamsManager.getInstance().currentRound != null && ((EntityDriveable)obj).seats[0].riddenByEntity instanceof EntityPlayerMP && owner instanceof EntityPlayer)
							 {
							 EntityPlayerMP player = (EntityPlayerMP)((EntityDriveable)obj).seats[0].riddenByEntity;
							 if(!TeamsManager.getInstance().currentRound.gametype.playerAttacked((EntityPlayerMP)obj, new EntityDamageSourceFlans(type.shortName, this, (EntityPlayer)owner, type, false)))
							 continue;
							 }
							 */
							if (type.damageToTriggerer > 0)
								((EntityDriveable) obj).attackEntityFrom(getBulletDamage(false), type.damageToTriggerer);
							detonate();
							break;
						}
					}
				}
			}

			Vector3f origin = new Vector3f(posX, posY, posZ);
			Vector3f motion = new Vector3f(motionX, motionY, motionZ);
			float hitBoxSize = type.hitBoxSize >= 0 ? type.hitBoxSize : 0;

			speedA = motion.length();

			if (type.hasSubmunitions) {
				if (submunitionDelay < 0) {
					DeploySubmunitions();
					submunitionDelay = 9001;
				}
			}

			if (!world.isRemote) {
				Entity ignore = shot.getPlayerOptional().isPresent() ? shot.getPlayerOptional().get() : shot.getShooterOptional().orElse(null);
				int ping = 0;
				if (shot.getPlayerOptional().isPresent())
					ping = shot.getPlayerOptional().get().ping;

				List<BulletHit> hits = FlansModRaytracer.Raytrace(world, ignore, ticksInAir > 20, this, origin, motion, ping, 0f);

				// We hit something
				if (!hits.isEmpty()) {
					boolean showCrosshair = false;

					for (BulletHit bulletHit : hits) {
						BulletHitEvent bulletHitEvent = new BulletHitEvent(this, bulletHit);
						MinecraftForge.EVENT_BUS.post(bulletHitEvent);
						if (bulletHitEvent.isCanceled()) continue;

						Vector3f hitPos = new Vector3f(origin.x + motion.x * bulletHit.intersectTime,
								origin.y + motion.y * bulletHit.intersectTime,
								origin.z + motion.z * bulletHit.intersectTime);

						currentPenetratingPower = ShotHandler.OnHit(world, hitPos, motion, shot, bulletHit, currentPenetratingPower);
						if (currentPenetratingPower <= 0f) {
							ShotHandler.onDetonate(world, shot, hitPos);
							setDead();
							break;
						}

						if (bulletHit instanceof FlansModRaytracer.DriveableHit) {
							if (type.entityHitSoundEnable)
								PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.hitSoundRange, dimension, type.hitSound, true);

							boolean isFriendly = false;
							FlansModRaytracer.DriveableHit driveableHit = (FlansModRaytracer.DriveableHit) bulletHit;
							driveableHit.driveable.lastAtkEntity = owner;
							if (TeamsManager.getInstance().currentRound != null) {
								for (EntitySeat seat : driveableHit.driveable.getSeats()) {
									if (seat.getControllingPassenger() instanceof EntityPlayerMP) {
										PlayerData dataDriver = PlayerHandler.getPlayerData((EntityPlayerMP) seat.getControllingPassenger());
										PlayerData dataAttacker = PlayerHandler.getPlayerData((EntityPlayerMP) owner);
										if (dataDriver.team.shortName.equals(dataAttacker.team.shortName)) {
											isFriendly = true;
										}
									}
								}
							}
							if (isFriendly) {
								currentPenetratingPower = 0;
							} else {
								currentPenetratingPower = driveableHit.driveable.bulletHit(type, shot.getFireableGun().getDamageAgainstVehicles(), driveableHit, currentPenetratingPower);
							}

							if (!world.isRemote) {
								if (owner instanceof EntityPlayer) {
									showCrosshair = true;
								}
							}

							if (type.canSpotEntityDriveable)
								driveableHit.driveable.setEntityMarker(200);
						} else if (bulletHit instanceof FlansModRaytracer.PlayerBulletHit) {
							if (type.entityHitSoundEnable)
								PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.hitSoundRange, dimension, type.hitSound, true);

							if (!world.isRemote) {
								if (owner instanceof EntityPlayer) {
									showCrosshair = true;
								}
							}
						} else if (bulletHit instanceof FlansModRaytracer.EntityHit) {
							if (type.entityHitSoundEnable)
								PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.hitSoundRange, dimension, type.hitSound, true);

							if (!world.isRemote) {
								if (owner instanceof EntityPlayer) {
									showCrosshair = true;
									shot.lastHitPenAmount = 1F;
								}
							}
						}
					}

					if (showCrosshair && owner instanceof EntityPlayerMP) {
						FlansMod.getPacketHandler().sendTo(new PacketHitMarker(shot.lastHitHeadshot, shot.lastHitPenAmount, false), (EntityPlayerMP) owner);
					}
				}
			}
			//TODO Client homing fix
			// Apply homing action
			//if(lockedOnTo != null)
			//{
			//	double dX = lockedOnTo.posX - posX;
			//	double dY = lockedOnTo.posY - posY;
			//	double dZ = lockedOnTo.posZ - posZ;
			//	double dXYZ = dX * dX + dY * dY + dZ * dZ;
//
			//	Vector3f relPosVec = new Vector3f(dX, dY, dZ);
			//	float angle = Math.abs(Vector3f.angle(motion, relPosVec));
//
			//	double lockOnPull = (angle) * type.lockOnForce;
//
			//	lockOnPull = lockOnPull * lockOnPull;
//
			//	motionX *= 0.95f;
			//	motionY *= 0.95f;
			//	motionZ *= 0.95f;
//
			//	motionX += lockOnPull * dX / dXYZ;
			//	motionY += lockOnPull * dY / dXYZ;
			//	motionZ += lockOnPull * dZ / dXYZ;
			//}
			/***if (lockedOnTo != null) {
			 if (lockedOnTo instanceof EntityDriveable) {
			 EntityDriveable entDriveable = (EntityDriveable) lockedOnTo;
			 // entPlane.isLockedOn = true;
			 if (entDriveable.getDriveableType().lockedOnSound != null && soundTime <= 0 && !this.world.isRemote) {
			 PacketPlaySound.sendSoundPacket(lockedOnTo.posX, lockedOnTo.posY, lockedOnTo.posZ,
			 entDriveable.getDriveableType().lockedOnSoundRange, dimension, entDriveable.getDriveableType().lockedOnSound, false);
			 soundTime = entDriveable.getDriveableType().soundTime;
			 }
			 } else {
			 lockedOnTo.getEntityData().setBoolean("Tracking", true);
			 }

			 if (this.ticksExisted > type.tickStartHoming) {
			 double dX = lockedOnTo.posX - posX;
			 double dY;
			 if (type.isDoTopAttack && Math.abs(lockedOnTo.posX - this.posX) > 2 && Math.abs(lockedOnTo.posZ - this.posZ) > 2)
			 dY = lockedOnTo.posY + 30 - posY;
			 else dY = lockedOnTo.posY - posY;
			 double dZ = lockedOnTo.posZ - posZ;
			 double dXYZ;
			 if (!type.isDoTopAttack)
			 dXYZ = getDistance(lockedOnTo);
			 else dXYZ = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

			 if (owner != null && type.enableSACLOS) {
			 double dXp = lockedOnTo.posX - owner.posX;
			 double dYp = lockedOnTo.posY - owner.posY;
			 double dZp = lockedOnTo.posZ - owner.posZ;
			 Vec3d playerVec = owner.getLookVec();
			 Vector3f playerVec3f = new Vector3f(playerVec.x, playerVec.y, playerVec.z);
			 double angles = Math.abs(Vector3f.angle(playerVec3f, new Vector3f(dXp, dYp, dZp)));
			 if (angles > Math.toRadians(type.maxDegreeOfSACLOS)) {
			 lockedOnTo = null;
			 }
			 }

			 if (this.toggleLock) {
			 //prevDistanceToEntity = dXYZ;
			 if (dXYZ > type.maxRangeOfMissile)
			 lockedOnTo = null;
			 toggleLock = false;
			 }

			 // Vector3f lockedOnToVector = new Vector3f(dX,dY,dZ);

			 double dmotion = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

			 Vector3f motionVector = new Vector3f(dX * dmotion / dXYZ, dY * dmotion / dXYZ, dZ * dmotion / dXYZ);

			 double angle = Math.abs(Vector3f.angle(motion, motionVector));

			 if (angle > Math.toRadians(type.maxDegreeOfMissile)) {
			 lockedOnTo = null;
			 } else {
			 motionX = motionVector.x;
			 motionY = motionVector.y;
			 motionZ = motionVector.z;
			 }

			 if (this.ticksExisted > 4 && dXYZ > prevDistanceToEntity) {
			 closeCount++;
			 if (closeCount > 15) {
			 lockedOnTo = null;
			 }
			 } else {
			 if (closeCount > 0)
			 closeCount--;
			 }
			 prevDistanceToEntity = dXYZ;
			 }

			 if (lockedOnTo instanceof EntityDriveable) {
			 EntityDriveable plane = (EntityDriveable) lockedOnTo;

			 if (plane.varFlare || plane.ticksFlareUsing > 0)// && !type.enableSACLOS)
			 {
			 lockedOnTo = null;
			 }
			 } else if (lockedOnTo != null && lockedOnTo.getEntityData().getBoolean("FlareUsing")) {
			 lockedOnTo = null;
			 }
			 } else if (type.laserGuidance) {
			 RayTraceResult mop = getSpottedPoint((EntityLivingBase) owner, 1F, type.maxRangeOfMissile, false);
			 if (mop != null) {
			 applyLaserGuidance(new Vector3f(mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ()), motion);
			 }
			 }

			 //FlansMod.log((int)posX+","+(int)posY+","+(int)posZ);

			 if (owner != null && type.shootForSettingPos && !this.isFirstPositionSetting) {
			 if (this.owner instanceof EntityPlayer) {
			 EntityPlayer entP = (EntityPlayer) this.owner;
			 if (entP.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemGun) {
			 ItemGun itemGun = (ItemGun) entP.getHeldItem(EnumHand.MAIN_HAND).getItem();
			 this.impactX = itemGun.impactX;
			 this.impactY = itemGun.impactY;
			 this.impactZ = itemGun.impactZ;
			 }

			 }
			 this.isFirstPositionSetting = true;
			 }

			 if (type.shootForSettingPos && this.isFirstPositionSetting && this.isPositionUpper) {
			 double motionXa = this.motionX;
			 double motionYa = this.motionY;
			 double motionZa = this.motionZ;
			 double motiona = Math.sqrt((motionXa * motionXa) + (motionYa * motionYa) + (motionZa * motionZa));
			 this.motionX = 0;
			 this.motionY = motiona;
			 this.motionZ = 0;

			 if (this.posY - type.shootForSettingPosHeight > owner.posY) {
			 this.isPositionUpper = false;
			 }
			 }
			 if (type.shootForSettingPos && this.isFirstPositionSetting && !this.isPositionUpper) {
			 double rootx = this.impactX - this.posX;
			 double rootz = this.impactZ - this.posZ;
			 double roota = Math.sqrt((rootx * rootx) + (rootz * rootz));
			 double motionXa = this.motionX;
			 double motionYa = this.motionY;
			 double motionZa = this.motionZ;
			 double motiona = Math.sqrt((motionXa * motionXa) + (motionYa * motionYa) + (motionZa * motionZa));
			 this.motionX = rootx * motiona / roota;
			 this.motionZ = rootz * motiona / roota;
			 if (Math.abs(this.impactX - this.posX) < 1 && Math.abs(this.impactZ - this.posZ) < 1) {
			 double motionXab = this.motionX;
			 double motionYab = this.motionY;
			 double motionZab = this.motionZ;
			 double motionab = Math.sqrt((motionXa * motionXa) + (motionYa * motionYa) + (motionZa * motionZa));
			 this.motionX = 0;
			 this.motionY = -motionab;
			 this.motionZ = 0;
			 }
			 }***/
			/*setRenderDistanceWeight(256D);
			if (owner != null && type.manualGuidance && VLSDelay <= 0 && lockedOnTo == null) {

				setRenderDistanceWeight(256D);*/
			/**
			 boolean beamRider = true;
			 if(!beamRider)
			 {
			 this.rotationYaw = owner.rotationYaw;
			 this.rotationPitch = owner.rotationPitch;
			 double dist = MathHelper.sqrt_double( motionX*motionX + motionY*motionY + motionZ*motionZ );
			 final float PI = (float) Math.PI;
			 motionX = dist * -MathHelper.sin((rotationYaw   / 180F) * PI) * MathHelper.cos((rotationPitch / 180F) * PI)*1.02;
			 motionZ = dist *  MathHelper.cos((rotationYaw   / 180F) * PI) * MathHelper.cos((rotationPitch / 180F) * PI)*1.02;
			 motionY = dist * -MathHelper.sin((rotationPitch / 180F) * PI)*1.02;
			 } else
			 */

				/*Vector3f lookVec;
				Vector3f origin2;
				lookVec = new Vector3f((float) owner.getLookVec().x, (float) owner.getLookVec().y, (float) owner.getLookVec().z);
				origin2 = new Vector3f(owner.posX, owner.posY, owner.posZ);

				if (type.fixedDirection) {
					lookVec = lookVector;
					origin2 = initialPos;
				}
				float x = (float) (posX - origin2.x);
				float y = (float) (posY - origin2.y);
				float z = (float) (posZ - origin2.z);

				float d = (float) Math.sqrt((x * x) + (y * y) + (z * z));
				d = d + type.turnRadius;

				lookVec.normalise();

				Vector3f targetPoint = new Vector3f(origin2.x + (lookVec.x * d), origin2.y + (lookVec.y * d), origin2.z + (lookVec.z * d));
				//FlansMod.proxy.spawnParticle("explode", targetPoint.x,targetPoint.y,targetPoint.z, 0,0,0);
				//double dX = owner.posX - this.posX;
				//double dY = owner.posY - this.posY;
				//double dZ = owner.posZ - this.posZ;
				//targetPoint = new Vector3f(owner.posX, owner.posY, owner.posZ);

				Vector3f diff = Vector3f.sub(targetPoint, new Vector3f(posX, posY, posZ), null);

				float speed2 = type.trackPhaseSpeed;
				float turnSpeed = type.trackPhaseTurn;
				diff.normalise();
				turnSpeed = 0.1F;
				Vector3f targetSpeed = new Vector3f(diff.x * speed2, diff.y * speed2, diff.z * speed2);

				this.motionX += (targetSpeed.x - motionX) * turnSpeed;
				this.motionY += (targetSpeed.y - motionY) * turnSpeed;
				this.motionZ += (targetSpeed.z - motionZ) * turnSpeed;

				//this.rotationYaw = owner.rotationYaw;
				//this.rotationPitch = owner.rotationPitch;
			}*/


			if (type.torpedo) {
				if (isInWater()) {
					Vector3f motion2 = new Vector3f(motionX, motionY, motionZ);
					float length = motion.length();
					motion.normalise();
					motionY *= 0.3F;
					motionX = motion.x * 1;
					motionZ = motion.z * 1;
				} else {
					motionY -= gravity * type.fallSpeed;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			super.setDead();
		}
	}

	public DamageSource getBulletDamage(boolean headshot) {
		if (owner instanceof EntityPlayer)
			return (new EntityDamageSourceFlan(type.shortName, this, (EntityPlayer) owner, firedFrom, headshot, false)).setProjectile();
		else return (new EntityDamageSourceIndirect(type.shortName, this, owner)).setProjectile();
	}

	@SideOnly(Side.CLIENT)
	private void onUpdateClient() {
		// Particles
		if (shot.getBulletType().trailParticles) {
			spawnParticles();
		}

		if (getDistanceSq(Minecraft.getMinecraft().player) < 5 && !playedFlybySound) {
			playedFlybySound = true;
			FMLClientHandler.instance().getClient().getSoundHandler()
					.playSound(new PositionedSoundRecord(FlansModResourceHandler.getSoundEvent("bulletFlyby"), SoundCategory.HOSTILE, 10F,
							1.0F / (rand.nextFloat() * 0.4F + 0.8F), (float) posX, (float) posY, (float) posZ));
		}
	}

	private static RayTraceResult getSpottedPoint(EntityLivingBase entityBase, float fasc, double dist, boolean interact) {
		Vec3d vec3 = new Vec3d(entityBase.posX, entityBase.posY + entityBase.getEyeHeight(), entityBase.posZ);
		Vec3d vec31 = entityBase.getLook(fasc);
		Vec3d vec32 = vec3.add(vec31.x * dist, vec31.y * dist, vec31.z * dist);
		return entityBase.world.rayTraceBlocks(vec3, vec32, interact);
	}

	private void applyLaserGuidance(Vector3f targetPos, Vector3f motion) {
		if (this.ticksExisted > type.tickStartHoming) {
			double dX = targetPos.x - posX;
			double dY = targetPos.y - posY;
			double dZ = targetPos.z - posZ;
			double dXYZ;
			float f = (float) (this.posX - targetPos.x);
			float f1 = (float) (this.posY - targetPos.y);
			float f2 = (float) (this.posZ - targetPos.z);
			dXYZ = MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
			if (this.toggleLock) {
				if (dXYZ > type.maxRangeOfMissile)
					targetPos = null;
				toggleLock = false;
			}
			double dmotion = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
			Vector3f motionVector = new Vector3f(dX * dmotion / dXYZ, dY * dmotion / dXYZ, dZ * dmotion / dXYZ);
			double angle = Math.abs(Vector3f.angle(motion, motionVector));
			if (angle > Math.toRadians(type.maxDegreeOfMissile)) {
				targetPos = null;
			} else {
				motionX = motionVector.x;
				motionY = motionVector.y;
				motionZ = motionVector.z;
			}

			if (this.ticksExisted > 4 && dXYZ > getPrevDistanceToTarget) {
				closeCount++;
				if (closeCount > 15) {
					targetPos = null;
				}
			} else {
				if (closeCount > 0)
					closeCount--;
			}
			getPrevDistanceToTarget = dXYZ;
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticles() {
		//pas de gravité c'est seulement pour les véhicules, et les retomber des bullets sont dynamique et propre à leur content pack

		double dX = (posX - prevPosX) / 10;
		double dY = (posY - prevPosY) / 10;
		double dZ = (posZ - prevPosZ) / 10;
		float spread = 0.1F;
		type = shot.getBulletType();
		float fallSpeed = type.fallSpeed;
		//float drag = 0.99F;
		//float gravity = 0.02F;
		//String message = "debug message rocket " + getType();
		//Minecraft.getMinecraft().ingameGUI.setOverlayMessage(message, false);
		for (int i = 0; i < 10; i++) {
				Particle particle = FlansModClient.getParticle(shot.getBulletType().trailParticleType, world,
						prevPosX + dX * i + rand.nextGaussian() * spread,
						(prevPosY + dY * i + rand.nextGaussian() * spread) - fallSpeed,
						prevPosZ + dZ * i + rand.nextGaussian() * spread);

			World world = this.getEntityWorld();
			Particle particle1 = FlansModClient.getParticle("flansmod.rocketexhaust", world,
					prevPosX + dX * i + rand.nextGaussian() * spread,
					(prevPosY + dY * i + rand.nextGaussian() * spread) - fallSpeed,
					prevPosZ + dZ * i + rand.nextGaussian() * spread);
			if (particle1 != null && Minecraft.getMinecraft().gameSettings.fancyGraphics) {
				Minecraft.getMinecraft().effectRenderer.addEffect(particle1);
			}
		}


		if (VLSDelay > 0 && type.boostPhaseParticle != null) {
			for (int i = 0; i < 10; i++) {
				FlansMod.proxy.spawnParticle(type.boostPhaseParticle,
						prevPosX + dX * i + rand.nextGaussian() * spread,
						(prevPosY + dY * i + rand.nextGaussian() * spread) - fallSpeed,
						prevPosZ + dZ * i + rand.nextGaussian() * spread,
						0, 0, 0);
			}
		} else if (!type.VLS || (VLSDelay <= 0)) {
			for (int i = 0; i < 10; i++) {
				World world = this.getEntityWorld();
				Particle particle = FlansModClient.getParticle("flansmod.rocketexhaust", world,
						prevPosX + dX * i + rand.nextGaussian() * spread,
						(prevPosY + dY * i + rand.nextGaussian() * spread) - fallSpeed,
						prevPosZ + dZ * i + rand.nextGaussian() * spread);
				if (particle != null && Minecraft.getMinecraft().gameSettings.fancyGraphics) {
					Minecraft.getMinecraft().effectRenderer.addEffect(particle);
				}
				FlansMod.proxy.spawnParticle(type.trailParticleType,
						prevPosX + dX * i + rand.nextGaussian() * spread,
						(prevPosY + dY * i + rand.nextGaussian() * spread) - fallSpeed,
						prevPosZ + dZ * i + rand.nextGaussian() * spread,
						0, 0, 0);
			}

		}
		FlansMod.proxy.spawnParticle("explode",
				prevPosX + dX,
				prevPosY + dY,
				prevPosZ + dZ,
				motionX + (float) Math.random() * 1 - 0.5,
				motionY + (float) Math.random() * 1 - 0.5,
				motionZ + (float) Math.random() * 1 - 0.5);

	}

	@Override
	public void setDead()
	{
		if(isDead)
			return;
		super.setDead();
	}

	public float moveToTarget(float current, float target, float speed) {

		float pitchToMove = (float) ((Math.sqrt(target * target)) - Math.sqrt((current * current)));
		for (; pitchToMove > 180F; pitchToMove -= 360F) {
		}
		for (; pitchToMove <= -180F; pitchToMove += 360F) {
		}

		float signDeltaY = 0;
		if (pitchToMove > speed) {
			signDeltaY = 1;
		} else if (pitchToMove < -speed) {
			signDeltaY = -1;
		} else {
			signDeltaY = 0;
			return target;
		}


		if (current > target) {
			current = current - speed;
		} else if (current < target) {
			current = current + speed;
		}


		return current;
	}

	public void detonate() {
		//Do not detonate before grenade is primed
		if (ticksExisted < type.primeDelay)
			return;

		/*if(lockedOnTo != null)
		if(lockedOnTo instanceof EntityDriveable){
			EntityDriveable entPlane = (EntityDriveable)lockedOnTo;
			entPlane.isLockedOn = false;
		}*/

		//Stop repeat detonations
		if (detonated)
			return;
		detonated = true;

		//Play detonate sound
		PacketPlaySound.sendSoundPacket(posX, posY, posZ, FlansMod.soundRange, dimension, type.detonateSound, true);

		//Explode
		if (!world.isRemote && type.explosionRadius > 0.1F) {
			if ((owner instanceof EntityPlayer)) {
				new FlansModExplosion(world, this, Optional.of((EntityPlayer) owner), type, posX, posY, posZ,
						type.explosionRadius, type.explosionPower, TeamsManager.explosions && type.explosionBreaksBlocks,
						type.explosionDamageVsLiving, type.explosionDamageVsPlayer, type.explosionDamageVsPlane, type.explosionDamageVsVehicle, type.smokeParticleCount, type.debrisParticleCount);
				isDead = true;
			} else {
				world.createExplosion(this, posX, posY, posZ, type.explosionRadius, TeamsManager.explosions && type.explosionBreaksBlocks);
				isDead = true;
			}
		}

		//Make fire
		if (!world.isRemote && type.fireRadius > 0.1F) {
			for (float i = -type.fireRadius; i < type.fireRadius; i++) {
				for (float j = -type.fireRadius; j < type.fireRadius; j++) {
					for (float k = -type.fireRadius; k < type.fireRadius; k++) {
						int x = MathHelper.floor(i + posX);
						int y = MathHelper.floor(j + posY);
						int z = MathHelper.floor(k + posZ);
						if (i * i + j * j + k * k <= type.fireRadius * type.fireRadius && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.AIR && rand.nextBoolean()) {
							world.setBlockState(new BlockPos(x, y, z), Blocks.FIRE.getDefaultState(), 2);
						}
					}
				}
			}
		}

		//Make explosion particles
		for (int i = 0; i < type.explodeParticles; i++) {
			EnumParticleTypes particleType = EnumParticleTypes.getByName(type.explodeParticleType);
			if (particleType != null)
				world.spawnParticle(particleType, posX, posY, posZ, rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
		}

		//Drop item upon detonation, after explosions and whatnot
		if (!world.isRemote && type.dropItemOnDetonate != null) {
			String itemName = type.dropItemOnDetonate;
			int damage = 0;
			if (itemName.contains(".")) {
				damage = Integer.parseInt(itemName.split("\\.")[1]);
				itemName = itemName.split("\\.")[0];
			}
			ItemStack dropStack = InfoType.getRecipeElement(itemName, 1, damage);
			entityDropItem(dropStack, 1.0F);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag)
	{
		if (type == null) {
			//FlansMod.log("EntityBullet.writeEntityToNBT() Error: BulletType is null (" + this + ")");
			setDead();
			return;
		}
		tag.setString("type", shot.getBulletType().shortName);
		FireableGun gun = shot.getFireableGun();
		//this data will only be present and saved on the server side
		if (gun != null)
		{
			NBTTagCompound fireablegun = new NBTTagCompound();
			fireablegun.setInteger("infotype", gun.getInfoType().shortName.hashCode());
			fireablegun.setFloat("spread", gun.getGunSpread());
			fireablegun.setFloat("speed", gun.getBulletSpeed());
			fireablegun.setFloat("damage", gun.getDamage());
			fireablegun.setFloat("vehicledamage", gun.getDamageAgainstVehicles());
			tag.setTag("fireablegun",fireablegun);

			shot.getPlayerOptional().ifPresent((EntityPlayerMP player) ->
			{

				NBTTagCompound compound = NBTUtil.createUUIDTag(player.getUniqueID());
				tag.setTag("player", compound);
			});

			shot.getShooterOptional().ifPresent((Entity shooter) ->
			{
				NBTTagCompound compound = NBTUtil.createUUIDTag(shooter.getUniqueID());
				tag.setTag("shooter", compound);
			});

		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag)
	{
		FireableGun fireablegun = null;
		String shortName = tag.getString("type");
		BulletType type = BulletType.getBullet(shortName);
		this.dataManager.set(BULLET_TYPE, shortName);

		if (tag.hasKey("fireablegun"))
		{
			NBTTagCompound gun = tag.getCompoundTag("fireablegun");
			fireablegun = new FireableGun(InfoType.getType(gun.getInteger("infotype")), gun.getFloat("damage"), gun.getFloat("vehicledamage"), gun.getFloat("spread"), gun.getFloat("speed"), EnumSpreadPattern.circle);
		}

		if (tag.hasKey("player"))
		{
			playeruuid = NBTUtil.getUUIDFromTag(tag.getCompoundTag("player"));
			checkforuuids = true;
		}

		if (tag.hasKey("shooter"))
		{
			shooteruuid = NBTUtil.getUUIDFromTag(tag.getCompoundTag("shooter"));
			checkforuuids = true;
		}

		if (type == null) {
			this.isDead = true;
			return;
		}

		if (type.despawnTime <= 0) {
			this.isDead = true;
		}

		shot = new FiredShot(fireablegun, type);
	}

	public void setSubmunitionDelay() {
		submunitionDelay = type.subMunitionTimer;
		hasSetSubDelay = true;
	}

	public void DeploySubmunitions() {
		ItemShootable itemShootable = (ItemShootable) Item.getByNameOrId(FlansMod.MODID + ":" + type.submunition);
		World worldObj = world;
		EntityLivingBase entityplayer = (EntityLivingBase) owner;
		for (int sm = 0; sm < type.numSubmunitions; sm++) {
			worldObj.spawnEntity(itemShootable.getEntity(
					worldObj,
					new Vector3f(this.posX, this.posY, this.posZ),
					new Vector3f(motionX, motionY, motionZ),
					entityplayer,
					type.submunitionSpread,
					shot.getFireableGun().getDamage(),
					speedA,
					0,
					firedFrom));
		}

		if (type.destroyOnDeploySubmunition) {
			detonate();
		}
	}

	@Override
	public void writeSpawnData(ByteBuf data)
	{
		data.writeDouble(motionX);
		data.writeDouble(motionY);
		data.writeDouble(motionZ);
		data.writeInt(impactX);
		data.writeInt(impactY);
		data.writeInt(impactZ);
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		try
		{
			motionX = data.readDouble();
			motionY = data.readDouble();
			motionZ = data.readDouble();
			impactX = data.readInt();
			impactY = data.readInt();
			impactZ = data.readInt();
		}
		catch(Exception e)
		{
			FlansMod.log.error("Failed to read bullet owner from server.");
			super.setDead();
			FlansMod.log.throwing(e);
		}
	}

	@Override
	public boolean isBurning()
	{
		return false;
	}

	@Override
	public boolean canBePushed()
	{
		return false;
	}

	public FiredShot getFiredShot()
	{
		if (shot == null)
		{
			//we dont have this object, therefore we are on the client side and need to construct it
			shot = new FiredShot(null, BulletType.getBullet(this.dataManager.get(BULLET_TYPE)));
		}
		return shot;
	}
}
