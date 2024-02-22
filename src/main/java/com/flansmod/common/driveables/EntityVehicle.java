package com.flansmod.common.driveables;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.handlers.KeyInputHandler;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.eventhandlers.DriveableDeathByHandEvent;
import com.flansmod.common.network.*;
import com.flansmod.common.teams.Team;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.flansmod.api.IExplodeable;
import com.flansmod.client.model.AnimTankTrack;
import com.flansmod.client.model.AnimTrackLink;
import com.flansmod.common.FlansMod;
import com.flansmod.common.RotatedAxes;
import com.flansmod.common.teams.TeamsManager;
import com.flansmod.common.tools.ItemTool;
import com.flansmod.common.vector.Vector3f;
import org.lwjgl.input.Keyboard;

import java.util.List;



public class EntityVehicle extends EntityDriveable implements IExplodeable
{
	private boolean invisible;
	/**
	 * Weapon delays
	 */
	public int shellDelay, gunDelay;
	/**
	 * Position of looping sounds
	 */
	public int soundPosition;
	public int idlePosition;
	/**
	 * Front wheel yaw, used to control the vehicle steering
	 */
	public float wheelsYaw;
	/**
	 * Despawn time
	 */
	private int ticksSinceUsed = 0;
	/**
	 * Aesthetic door switch
	 */
	public boolean varDoor;
	/**
	 * Wheel rotation angle. Only applies to vehicles that set a rotating wheels flag
	 */
	public float wheelsAngle;
	/**
	 * Delayer for door button
	 */
	public int toggleTimer = 0;

	public AnimTankTrack rightTrack;
	public AnimTankTrack leftTrack;

	public AnimTrackLink[] trackLinksLeft = new AnimTrackLink[0];
	public AnimTrackLink[] trackLinksRight = new AnimTrackLink[0];

	public float yaw = 0;
	public float pitch = 0;
	public float roll = 0;

	public float yawSpeed = 0;

	// Used for better falling
	private float fallVelocity = 0;

	//Handling stuff
	public int keyHeld = 0;
	public boolean leftTurnHeld = false;
	public boolean rightTurnHeld = false;
	public boolean allWheelsOnGround;

	//Some nonsense
	boolean lockTurretForward = false;
	//Animation vectors
	public Vector3f doorPos = new Vector3f(0, 0, 0);
	public Vector3f doorRot = new Vector3f(0, 0, 0);
	public Vector3f door2Pos = new Vector3f(0, 0, 0);
	public Vector3f door2Rot = new Vector3f(0, 0, 0);

	public Vector3f prevDoorPos = new Vector3f(0, 0, 0);
	public Vector3f prevDoorRot = new Vector3f(0, 0, 0);
	public Vector3f prevDoor2Pos = new Vector3f(0, 0, 0);
	public Vector3f prevDoor2Rot = new Vector3f(0, 0, 0);

	public boolean deployedSmoke = false;


	//Dangerous sentry stuff
	/**
	 * Stops the sentry shooting whoever placed it or their teammates
	 */
	public EntityPlayer placer = null;
	/**
	 * For getting the placer after a reload
	 */
	public String placerName = null;
	public Entity target = null;

	public EntityVehicle(World world)
	{
		super(world);
		stepHeight = 1.0F;
	}

	//This one deals with spawning from a vehicle spawner
	public EntityVehicle(World world, double x, double y, double z, VehicleType type, DriveableData data)
	{
		super(world, type, data, null);
		stepHeight = 1.0F;
		setPosition(x, y, z);
		initType(type, true, false);
	}

	//This one allows you to deal with spawning from items
	public EntityVehicle(World world, double x, double y, double z, EntityPlayer placer, VehicleType type,
						 DriveableData data)
	{
		super(world, type, data, placer);
		stepHeight = 1.0F;
		setPosition(x, y, z);
		rotateYaw(placer.rotationYaw + 90F);
		initType(type, true, false);
	}

	public void setupTracks(DriveableType type)
	{
		rightTrack = new AnimTankTrack(type.rightTrackPoints, type.trackLinkLength);
		leftTrack = new AnimTankTrack(type.leftTrackPoints, type.trackLinkLength);
		int numLinks = Math.round(rightTrack.getTrackLength() / type.trackLinkLength);
		trackLinksLeft = new AnimTrackLink[numLinks];
		trackLinksRight = new AnimTrackLink[numLinks];
		for(int i = 0; i < numLinks; i++)
		{
			float progress = 0.01F + (type.trackLinkLength * i);
			int trackPart = leftTrack.getTrackPart(progress);
			trackLinksLeft[i] = new AnimTrackLink(progress);
			trackLinksRight[i] = new AnimTrackLink(progress);
			trackLinksLeft[i].position = leftTrack.getPositionOnTrack(progress);
			trackLinksRight[i].position = rightTrack.getPositionOnTrack(progress);
			trackLinksLeft[i].rot = new RotatedAxes(0,
				0,
				rotateTowards(leftTrack.points.get((trackPart == 0) ? leftTrack.points.size() - 1 : trackPart - 1),
					trackLinksLeft[i].position));
			trackLinksRight[i].rot = new RotatedAxes(0,
				0,
				rotateTowards(rightTrack.points.get((trackPart == 0) ? rightTrack.points.size() - 1 : trackPart - 1),
					trackLinksRight[i].position));
			trackLinksLeft[i].zRot = rotateTowards(leftTrack.points
				.get((trackPart == 0) ? leftTrack.points.size() - 1 : trackPart - 1), trackLinksLeft[i].position);
			trackLinksRight[i].zRot = rotateTowards(rightTrack.points
				.get((trackPart == 0) ? rightTrack.points.size() - 1 : trackPart - 1), trackLinksRight[i].position);
		}
	}

	@Override
	protected void initType(DriveableType type, boolean firstSpawn, boolean clientSide)
	{
		setupTracks(type);
		super.initType(type, firstSpawn, clientSide);
	}

	@Override
	public void readSpawnData(ByteBuf data)
	{
		super.readSpawnData(data);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tag)
	{
		super.writeEntityToNBT(tag);
		tag.setBoolean("VarDoor", varDoor);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag)
	{
		super.readEntityFromNBT(tag);
		varDoor = tag.getBoolean("VarDoor");
	}

	/**
	 * Called with the movement of the mouse. Used in controlling vehicles if need be.
	 *
	 * @param deltaY
	 * @param deltaX
	 */
	@Override
	public void onMouseMoved(int deltaX, int deltaY)
	{
	}

	@Override
	public void setPositionRotationAndMotion(double x, double y, double z, float yaw, float pitch, float roll,
											 double motX, double motY, double motZ, float velYaw, float velPitch,
											 float velRoll, float throttle, float steeringYaw)
	{
		super.setPositionRotationAndMotion(x, y, z, yaw, pitch, roll, motX, motY, motZ, velYaw, velPitch, velRoll,
			throttle, steeringYaw);
		wheelsYaw = steeringYaw;
	}

	@Override
	public boolean processInitialInteract(EntityPlayer entityplayer, EnumHand hand)
	{
		if(isDead)
			return false;
		if(world.isRemote)
			return false;

		//If they are using a repair tool, don't put them in
		ItemStack currentItem = entityplayer.getHeldItemMainhand();
		if(currentItem.getItem() instanceof ItemTool && ((ItemTool)currentItem.getItem()).type.healDriveables)
			return true;

		VehicleType type = getVehicleType();
		//Check each seat in order to see if the player can sit in it
		for(int i = 0; i <= type.numPassengers; i++)
		{
			if(getSeat(i).processInitialInteract(entityplayer, hand))
			{
				if(i == 0)
				{
					shellDelay = (int) type.shootDelayPrimary;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean pressKey(int key, EntityPlayer player, boolean isOnEvent)
	{
		VehicleType type = getVehicleType();
		DriveableData data = getDriveableData();

		switch(key)
		{
			case 0: // Accelerate : Increase the throttle, up to 1.
			{
				if (isEngineActive()) {
					if (type.useRealisticAcceleration) {
						throttle += (data.engine.enginePower * (throttle < 0 ? type.brakingModifier : 1))  / type.mass;
					} else {
						throttle += 0.01F * (throttle < 0 ? type.brakingModifier : 1);
					}
				}

				throttle += 0.01F;
				if(throttle > 1F)
					throttle = 1F;
				return true;
			}
			case 1: // Decelerate : Decrease the throttle, down to -1, or 0 if the vehicle cannot reverse
			{
				if (isEngineActive()) {
					if (type.useRealisticAcceleration) {
						throttle -= (data.engine.enginePower * (throttle > 0 ? type.brakingModifier : 1)) / type.mass;
					} else {
						throttle -= 0.01F * (throttle > 0 ? type.brakingModifier : 1);
					}
				}

				if(throttle < -1F)
					throttle = -1F;
				if(throttle < 0F && type.maxNegativeThrottle == 0F)
					throttle = 0F;
				return true;
			}
			case 2: // Left : Yaw the wheels left
			{
				if (throttle < 0.3F)
					throttle += type.clutchBrake;
				wheelsYaw -= 1F;
				leftTurnHeld = true;
				keyHeld = 100;
				return true;
			}
			case 3: // Right : Yaw the wheels right
			{
				if (throttle < 0.3F)
					throttle += type.clutchBrake;
				wheelsYaw += 1F;
				rightTurnHeld = true;
				keyHeld = 100;
				return true;
			}
			case 4: // Up : Brake
			{
				throttle *= 0.8F;
				if (throttle > 0.001F)
					throttle = 0F;
				if (throttle < -0.001F)
					throttle = 0F;

				target = null;

				if(onGround)
				{
					motionX *= 0.8F;
					motionZ *= 0.8F;
				}
				return true;
			}
			case 5: //Down
			{
				Minecraft mc = Minecraft.getMinecraft();
				if (toggleTimer <= 0 && TeamsManager.allowVehicleZoom) {
					toggleTimer = 10;
					if (mc.gameSettings.fovSetting != 10) {
						mc.gameSettings.fovSetting = 10;
						mc.gameSettings.mouseSensitivity = 0.2F;
					} else if (mc.gameSettings.fovSetting == 10) {
						mc.gameSettings.fovSetting = 70;
						mc.gameSettings.mouseSensitivity = 0.5F;
					}
				}
				return true;
			}
			case 7: // Inventory
			{
				if (world.isRemote)
				{
					FlansMod.proxy.openDriveableMenu((EntityPlayer) player, world, this);

				}
				return true;
			}
			case 10: //Change control mode : Do nothing
			{
				FlansMod.proxy.changeControlMode((EntityPlayer) getSeats()[0].getControllingPassenger());
				getSeats()[0].targetYaw = getSeats()[0].looking.getYaw();
				getSeats()[0].targetPitch = getSeats()[0].looking.getPitch();
				return true;
			}
			case 11: //Roll left : Do nothing
			{
				getSeats()[0].targetYaw -= getSeats()[0].seatInfo.aimingSpeed.x;
				return true;
			}
			case 12: //Roll right : Do nothing
			{
				getSeats()[0].targetYaw += getSeats()[0].seatInfo.aimingSpeed.x;
				return true;
			}
			case 13: // Gear : Do nothing
			{
				if (getSeats()[0].targetPitch < -getSeats()[0].seatInfo.minPitch)
					getSeats()[0].targetPitch += getSeats()[0].seatInfo.aimingSpeed.y;
				return true;
			}
			case 14: // Door
			{
				if(toggleTimer <= 0)
				{
					varDoor = !varDoor;
					if(type.hasDoor)
						player.sendMessage(new TextComponentString("Doors " + (varDoor ? "opened" : "closed")));
					toggleTimer = 10;
					FlansMod.getPacketHandler().sendToServer(new PacketVehicleControl(this));
				}
				return true;
			}
			case 15: // Wing : Do nothing
			{
				if (getSeats()[0].targetPitch > -getSeats()[0].seatInfo.maxPitch)
					getSeats()[0].targetPitch -= getSeats()[0].seatInfo.aimingSpeed.y;
				return true;
			}
			case 6: //Exit : Get out
			{
				if (getSeat(0) != null && getSeat(0).getControllingPassenger() != null)
				{
					invisible = false;
					getSeat(0).getControllingPassenger().setInvisible(false);
					Entity passenger = getSeat(0).getControllingPassenger();
					if (passenger instanceof EntityPlayer) {
						EntityPlayer driver = ((EntityPlayer) passenger);
						ArmorInvisible.EventHandler.setArmor(driver, false);
					}
					//}
					//resetZoom();
					//getSeat(0).getControllingPassenger().dismountRidingEntity();//Removed bcs player are not completely out of the vehicle (1.12.2 bug)
					//PacketPlaySound.sendSoundPacket(posX, posY, posZ, FlansMod.soundRange, dimension, type.exitSound, false);
				}
				return true;
			}
			case 18: //Flare
			{
				if (type.hasFlare && this.ticksFlareUsing <= 0 && this.flareDelay <= 0) {
					this.ticksFlareUsing = type.timeFlareUsing * 20;
					this.flareDelay = type.flareDelay;
					dischargeSmoke();
					if (world.isRemote) {
						FlansMod.getPacketHandler().sendToServer(new PacketDriveableKey(key));
					} else {
						dischargeSmoke();
						if (!type.flareSound.isEmpty()) {
							PacketPlaySound.sendSoundPacket(posX, posY, posZ, FlansMod.soundRange, dimension, type.flareSound, false);
						}
					}
					return true;
				}
				break;
			}
			default:
			{
				return super.pressKey(key, player, isOnEvent);
			}
		}
		return false;
	}
	@SideOnly(Side.CLIENT)
	public void resetZoom() {
		if (TeamsManager.allowVehicleZoom) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.gameSettings.fovSetting == 10) {
				mc.gameSettings.fovSetting = 70;
				mc.gameSettings.mouseSensitivity = 0.5F;
			}
		}
	}
	@Override
	public Vector3f getLookVector(ShootPoint shootPoint)
	{
		return rotate(getSeat(0).looking.getXAxis());
	}
	public int ticks;
	public static boolean exit;
	@Override
	public void onUpdate() {
		double bkPrevPosY = this.prevPosY;
		super.onUpdate();
		if (!readyForUpdates) {
			return;
		}
		VehicleType type = this.getVehicleType();
		if (type.setPlayerInvisible && exit) {
			Entity passenger = getSeat(0).getControllingPassenger();
			if (passenger instanceof EntityPlayer) {
				EntityPlayer driver = (EntityPlayer) passenger;
				ArmorInvisible.EventHandler.setArmor(driver, true);
				exit = false;
			}
		}
		//tu sors du véhicule -> invisible false parce que tu devient visible
		//sauf que quand tu rentres dans e véhicule
		//Get vehicle type
		DriveableData data = getDriveableData();
		//wheelsYaw -= 1F;
		if (!invisible && this.world.isRemote && getSeat(0).getControllingPassenger() != null && type.setPlayerInvisible) {
			invisible = true;
		}
		if(invisible && this.world.isRemote && getSeat(0).getControllingPassenger() != null && type.setPlayerInvisible) {
			getSeat(0).getControllingPassenger().setInvisible(true);
		}
		if(type == null)
		{
			FlansMod.log.warn("Vehicle type null. Not ticking vehicle");
			return;
		}

		if (type.shootWithOpenDoor) {
			canFire = varDoor;
		}

		animateFancyTracks();
		if (world.isRemote) {
			for (Entity e : findEntitiesWithinbounds()) {
				if (!isPartOfThis(e) && e instanceof EntityPlayer) {
					moveRiders(e);
				}
			}
		}

		//Work out if this is the client side and the player is driving
		boolean thePlayerIsDrivingThis =
			world.isRemote && getSeat(0) != null && getSeat(0).getControllingPassenger() instanceof EntityPlayer
				&& FlansMod.proxy.isThePlayer((EntityPlayer)getSeat(0).getControllingPassenger());

		//Despawning
		ticksSinceUsed++;
		if(!world.isRemote && getSeat(0).getControllingPassenger() != null)
			ticksSinceUsed = 0;
		if(!world.isRemote && TeamsManager.vehicleLife > 0 && ticksSinceUsed > TeamsManager.vehicleLife * 20)
		{
			setDead();
		}

		if (this.world.isRemote && (this.varFlare || this.ticksFlareUsing > (type.timeFlareUsing * 20) - 5)) {
			if (this.ticksExisted % 5 == 0) {
				deployedSmoke = true;
			}
		}



		if (this.ticksFlareUsing <= 0) deployedSmoke = false;

		if (this.ticksFlareUsing > 0)
			this.ticksFlareUsing--;
		if (this.flareDelay > 0)
			this.flareDelay--;

		//Shooting, inventories, etc.
		//Decrement shell and gun timers
		if(shellDelay > 0)
			shellDelay--;
		if(gunDelay > 0)
			gunDelay--;
		if(toggleTimer > 0)
			toggleTimer--;
		if(soundPosition > 0)
			soundPosition--;
		if (idlePosition > 0)
			idlePosition--;

		if (type.tank && isUnderWater()) {
			ticks++;
			if (ticks >= 140) {
				throttle = 0;
				ticks = 0;
			}
		}
		if (type.tank && !hasBothTracks()) throttle = 0;
		if (disabled || !hasBothTracks()) wheelsYaw = 0;


		//Aesthetics
		//Rotate the wheels
		if(hasEnoughFuel() && isEngineActive())
		{
			wheelsAngle += throttle / 3.25F;
		}

		prevDoorPos = doorPos;
		prevDoorRot = doorRot;
		prevDoor2Pos = door2Pos;
		prevDoor2Rot = door2Rot;

		if (!varDoor) {
			doorPos = transformPart(doorPos, type.doorPos1, type.doorRate);
			doorRot = transformPart(doorRot, type.doorRot1, type.doorRotRate);
			door2Pos = transformPart(door2Pos, type.door2Pos1, type.door2Rate);
			door2Rot = transformPart(door2Rot, type.door2Rot1, type.door2RotRate);
		} else {
			doorPos = transformPart(doorPos, type.doorPos2, type.doorRate);
			doorRot = transformPart(doorRot, type.doorRot2, type.doorRotRate);
			door2Pos = transformPart(door2Pos, type.door2Pos2, type.door2Rate);
			door2Rot = transformPart(door2Rot, type.door2Rot2, type.door2RotRate);
		}

		//Return the wheels to their resting position
		wheelsYaw *= 0.9F;

		//Limit wheel angles
		if(wheelsYaw > 20)
			wheelsYaw = 20;
		if(wheelsYaw < -20)
			wheelsYaw = -20;

		//Player is not driving this. Update its position from server update packets
		if(world.isRemote && !thePlayerIsDrivingThis)
		{
			//The driveable is currently moving towards its server position. Continue doing so.
			if(serverPositionTransitionTicker > 0)
			{
				moveTowardServerPosition();
			}
			//If the driveable is at its server position and does not have the next update, it should just simulate itself as a server side driveable would, so continue
		}

		//Movement

		correctWheelPos();

		Vector3f amountToMoveCar = new Vector3f();

		for(EntityWheel wheel : wheels)
		{
			if(wheel != null && world != null)
			{
				wheel.prevPosX = wheel.posX;
				wheel.prevPosY = wheel.posY;
				wheel.prevPosZ = wheel.posZ;
			}
		}

		for(EntityWheel wheel : wheels)
		{
			if(wheel == null)
				continue;

			double prevPosYWheel = wheel.posY;

			//Hacky way of forcing the car to step up blocks
			onGround = true;
			wheel.onGround = true;

			//Update angles
			wheel.rotationYaw = axes.getYaw();
			//Front wheels
			if(!type.tank && (wheel.getExpectedWheelID() == 2 || wheel.getExpectedWheelID() == 3))
			{
				wheel.rotationYaw += wheelsYaw;
			}

			wheel.motionX *= 0.9F;
			wheel.motionY *= this.posY - bkPrevPosY < 0 ? 0.999F : 0.9F;
			wheel.motionZ *= 0.9F;

			//Apply gravity
			wheel.motionY -= 0.98F / 20F;

			//Apply velocity
			EntityPlayer driver = getDriver();
			if(canThrust(data, driver) && isEngineActive())
			{
				if (!driverIsCreative() && TeamsManager.vehiclesNeedFuel)
				{
					data.fuelInTank -= Math.abs(data.engine.fuelConsumption * throttle) * 0.1;
				}

				if(getVehicleType().tank)
				{
					boolean left = wheel.getExpectedWheelID() == 0 || wheel.getExpectedWheelID() == 3;

					float turningDrag = 0.02F;
					wheel.motionX *= 1F - (Math.abs(wheelsYaw) * turningDrag);
					wheel.motionZ *= 1F - (Math.abs(wheelsYaw) * turningDrag);
					float velocityScale = 0;
					if (isUnderWater()) {
						velocityScale = 0.04F * (throttle > 0 ? type.maxThrottleInWater : type.maxNegativeThrottle)
								* data.engine.engineSpeed;
					} else {
						velocityScale = 0.04F * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle)
								* data.engine.engineSpeed;

					}
					/*float velocityScale = 0.04F * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle) *
						data.engine.engineSpeed;*/
					float steeringScale = 0.1F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier);
					float effectiveWheelSpeed =
						(throttle + (wheelsYaw * (left ? 1 : -1) * steeringScale)) * velocityScale;
					wheel.motionX += effectiveWheelSpeed * Math.cos(wheel.rotationYaw * 3.14159265F / 180F);
					wheel.motionZ += effectiveWheelSpeed * Math.sin(wheel.rotationYaw * 3.14159265F / 180F);

					yawSpeed += effectiveWheelSpeed * Math.sin(wheel.rotationYaw * 3.14159265F / 180F);
				}
				else
				{
					//if(getVehicleType().fourWheelDrive || wheel.ID == 0 || wheel.ID == 1)
					{
						/*float velocityScale =
							0.1F * throttle * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle) *
								data.engine.engineSpeed;*/
						float velocityScale = 0;
						if (isUnderWater()) {
							velocityScale = 0.1F * throttle
									* (throttle > 0 ? type.maxThrottleInWater : type.maxNegativeThrottle)
									* data.engine.engineSpeed;
						} else {
							velocityScale = 0.1F * throttle
									* (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle)
									* data.engine.engineSpeed;

						}
						wheel.motionX += Math.cos(wheel.rotationYaw * 3.14159265F / 180F) * velocityScale;
						wheel.motionZ += Math.sin(wheel.rotationYaw * 3.14159265F / 180F) * velocityScale;
					}

					//Apply steering
					if(wheel.getExpectedWheelID() == 2 || wheel.getExpectedWheelID() == 3)
					{
						float velocityScale = 0.01F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier) *
							(throttle > 0 ? 1 : -1);

						wheel.motionX -=
							wheel.getSpeedXZ() * Math.sin(wheel.rotationYaw * 3.14159265F / 180F) * velocityScale *
								wheelsYaw;
						wheel.motionZ +=
							wheel.getSpeedXZ() * Math.cos(wheel.rotationYaw * 3.14159265F / 180F) * velocityScale *
								wheelsYaw;
					}
					else
					{
						wheel.motionX *= 0.9F;
						wheel.motionZ *= 0.9F;
					}
				}
			}

			if(type.floatOnWater && world.containsAnyLiquid(wheel.getEntityBoundingBox()))
			{
				wheel.motionY += type.buoyancy;
			}

			wheel.move(MoverType.PLAYER, wheel.motionX, wheel.motionY, wheel.motionZ);

			//Pull wheels towards car
			Vector3f targetWheelPos = axes
				.findLocalVectorGlobally(getVehicleType().wheelPositions[wheel.getExpectedWheelID()].position);
			Vector3f currentWheelPos = new Vector3f(wheel.posX - posX, wheel.posY - posY, wheel.posZ - posZ);

			Vector3f dPos = ((Vector3f)Vector3f.sub(targetWheelPos, currentWheelPos, null)
				.scale(type.wheelSpringStrength));

			if(dPos.length() > 0.001F)
			{
				wheel.move(MoverType.PLAYER, dPos.x, dPos.y, dPos.z);
				dPos.scale(0.5F);
				Vector3f.sub(amountToMoveCar, dPos, amountToMoveCar);
			}

			float avgWheelHeight = 0F;

			//Secondary check whether all wheels are on ground...
			if (wheels[0] != null && wheels[1] != null && wheels[2] != null && wheels[3] != null) {
				avgWheelHeight = (float) (wheels[0].posX + wheels[1].posX + wheels[2].posX + wheels[3].posX) / 4;
				if (!wheels[0].onGround && !wheels[1].onGround && !wheels[2].onGround && !wheels[3].onGround) {
					allWheelsOnGround = false;
				} else {
					allWheelsOnGround = true;
				}
			}

			//Now we apply gravity
			if (!(type.floatOnWater && world.containsAnyLiquid(wheel.getEntityBoundingBox().offset(0, -type.floatOffset, 0))) && !wheel.onDeck) {
				float a = type.maxFallSpeed;
				float g = type.gravity;

				if (wheel.onGround) {
					a *= 1F/2F;
					g *= 3/2F;
				}

				if (wheel.motionY >= 0) {
					wheel.motionY -= g;
				}  else if (wheel.motionY >= -a) {
					wheel.motionY -= g * (wheel.motionY + a);
				}

			} else if ((type.floatOnWater && world.containsAnyLiquid(wheel.getEntityBoundingBox().offset(0, -type.floatOffset, 0))) && world.containsAnyLiquid(wheel.getEntityBoundingBox().offset(0, 1 - type.floatOffset, 0)) && !wheel.onDeck) {
				wheel.move(MoverType.PLAYER, 0F, 1F, 0F);
			} else if ((type.floatOnWater && world.containsAnyLiquid(wheel.getEntityBoundingBox().offset(0, -type.floatOffset, 0))) && !world.containsAnyLiquid(wheel.getEntityBoundingBox().offset(0, 1 - type.floatOffset, 0)) || wheel.onDeck) {
				wheel.move(MoverType.PLAYER, 0F, 0F, 0F);
				this.roll = 0;
				this.pitch = 0;
			}/* else {
                wheel.moveEntity(0F, (!onDeck) ? -1.2F : 0, 0F);
            }*/

			if ((throttle >= 1.1 || throttle <= -1.1)) {
				Vector3f motionVec = new Vector3f(0, 0, 0);
				Vector3f targetVec = type.wheelPositions[wheel.getExpectedWheelID()].position;
				targetVec = axes.findLocalVectorGlobally(targetVec);
				if (throttle > 0.1) motionVec = new Vector3f(1, 0, 0);
				if (throttle < -0.1) motionVec = new Vector3f(-1, 0, 0);
				if ((wheel.getExpectedWheelID() == 2 || wheel.getExpectedWheelID() == 3) && wheelsYaw >= 0.1) motionVec = new Vector3f(motionVec.x, 0, 1);
				if ((wheel.getExpectedWheelID() == 0 || wheel.getExpectedWheelID() == 1) && wheelsYaw <= -0.1) motionVec = new Vector3f(motionVec.x, 0, -1);
				motionVec = axes.findLocalVectorGlobally(motionVec);
				Vector3f test1Pos = new Vector3f(posX + targetVec.x + motionVec.x, posY + targetVec.y, posZ + targetVec.z + motionVec.z);
				boolean test1 = world.isAirBlock(new BlockPos(Math.round(test1Pos.x), Math.round(test1Pos.y), Math.round(test1Pos.z)));
				boolean test2 = world.isAirBlock(new BlockPos(Math.round(test1Pos.x), Math.round(test1Pos.y + type.wheelStepHeight), Math.round(test1Pos.z)));
				if (!test1 && !test2) {
					// Tests to see if we are ascending tall terrain, or stuck in the ground.
					throttle *= 0.6;
					for (EntityWheel wheel2 : wheels) {
						Vector3f wheelPos3 = axes.findLocalVectorGlobally(type.wheelPositions[wheel2.getExpectedWheelID()].position);
					}
				}
			}
		}

		if (wheels[0] != null && wheels[1] != null && wheels[2] != null && wheels[3] != null) {
			lastPos.x = (float) (wheels[0].motionX + wheels[1].motionX + wheels[2].motionX + wheels[3].motionX) / 4;
			lastPos.y = (float) (wheels[0].motionY + wheels[1].motionY + wheels[2].motionY + wheels[3].motionY) / 4;
			lastPos.z = (float) (wheels[0].motionZ + wheels[1].motionZ + wheels[2].motionZ + wheels[3].motionZ) / 4;
		}


		double bmy = this.motionY;
		this.motionY = amountToMoveCar.y;
		if (collisionHardness > 0.2F) {
			amountToMoveCar.x = -axes.getXAxis().x*(float)getSpeedXZ()*0.1F;
			amountToMoveCar.z = -axes.getXAxis().z*(float)getSpeedXZ()*0.1F;
		}
		collisionHardness = 0F;
		move(MoverType.PLAYER, amountToMoveCar.x, amountToMoveCar.y, amountToMoveCar.z);
		this.motionY = bmy;

		if(wheels[0] != null && wheels[1] != null && wheels[2] != null && wheels[3] != null)
		{
			Vector3f frontAxleCentre = new Vector3f((wheels[2].posX + wheels[3].posX) / 2F,
				(wheels[2].posY + wheels[3].posY) / 2F,
				(wheels[2].posZ + wheels[3].posZ) / 2F);
			Vector3f backAxleCentre = new Vector3f((wheels[0].posX + wheels[1].posX) / 2F,
				(wheels[0].posY + wheels[1].posY) / 2F,
				(wheels[0].posZ + wheels[1].posZ) / 2F);
			Vector3f leftSideCentre = new Vector3f((wheels[0].posX + wheels[3].posX) / 2F,
				(wheels[0].posY + wheels[3].posY) / 2F,
				(wheels[0].posZ + wheels[3].posZ) / 2F);
			Vector3f rightSideCentre = new Vector3f((wheels[1].posX + wheels[2].posX) / 2F,
				(wheels[1].posY + wheels[2].posY) / 2F,
				(wheels[1].posZ + wheels[2].posZ) / 2F);

			if (frontAxleCentre.y > backAxleCentre.y) {
				if (throttle > 0) {
					float diff = frontAxleCentre.y - backAxleCentre.y;
					float dx = frontAxleCentre.x - backAxleCentre.x;
					dx = (float) Math.sqrt(dx * dx);
					float dz = frontAxleCentre.z - backAxleCentre.z;
					dz = (float) Math.sqrt(dz * dz);
					float dist = (float) Math.sqrt(dx + dz);
					diff = diff / dist;
					throttle *= (1F - (diff / 60));
					// Slows down the vehicle when going uphill.
				}
			}

			float dx = frontAxleCentre.x - backAxleCentre.x;
			float dy = frontAxleCentre.y - backAxleCentre.y;
			float dz = frontAxleCentre.z - backAxleCentre.z;
			float drx = leftSideCentre.x - rightSideCentre.x;
			float dry = leftSideCentre.y - rightSideCentre.y;
			float drz = leftSideCentre.z - rightSideCentre.z;


			float dxz = (float)Math.sqrt(dx * dx + dz * dz);
			float drxz = (float)Math.sqrt(drx * drx + drz * drz);

			float tyaw = (float)Math.atan2(dz, dx);
			float tpitch = -(float)Math.atan2(dy, dxz);
			float troll = 0F;
			if(type.canRoll)
			{
				roll = -(float)Math.atan2(dry, drxz);
			}

			yaw = tyaw;
			pitch = Lerp(pitch, tpitch, 0.2F);
			roll = Lerp(roll, troll, 0.2F);

			/*if(type.tank)
			{
				yaw = (float)Math.atan2(wheels[3].posZ - wheels[2].posZ, wheels[3].posX - wheels[2].posX) +
					(float)Math.PI / 2F;
			}*/

			if (type.tank) {
				float effectiveWheelSpeed;
				if (isEngineActive()) {
					float velocityScale = 0.04F * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle)
							* data.engine.engineSpeed;
					float steeringScale = 0.1F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier);
					effectiveWheelSpeed = ((wheelsYaw * steeringScale)) * velocityScale;
				} else {
					effectiveWheelSpeed = 0;
				}

				yaw = axes.getYaw() / 180F * 3.14159F + effectiveWheelSpeed;
			} else {
				float velocityScale = 0.1F * throttle * (throttle > 0 ? type.maxThrottle : type.maxNegativeThrottle) * data.engine.engineSpeed;
				float steeringScale = 0.1F * (wheelsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier);
				float effectiveWheelSpeed = ((wheelsYaw * steeringScale)) * velocityScale;
				yaw = axes.getYaw() / 180F * 3.14159F + (effectiveWheelSpeed);
			}

			axes.setAngles(yaw * 180F / 3.14159F, pitch * 180F / 3.14159F, roll * 180F / 3.14159F);
		}

		checkForCollisions();

		//Sounds
		//Starting sound
		if(Math.abs(throttle) > 0.01F && Math.abs(throttle) < 0.2F && soundPosition == 0 && hasEnoughFuel())
		{
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.startSoundRange, dimension, type.startSound, false);
			soundPosition = type.startSoundLength;
		}
		//Flying sound
		if(throttle >= 0.2F && soundPosition == 0 && hasEnoughFuel() && isEngineActive())
		{
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.engineSound, false);
			soundPosition = type.engineSoundLength;
		}
		if (getSeats()[0] != null) {
			if (throttle <= 0.01F && throttle >= -0.2F && getSeats()[0].getControllingPassenger() != null && idlePosition == 0) {
				PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.idleSound, false);
				idlePosition = type.idleSoundLength;
			}
		}
		//Back sound
		if (throttle <= -0.2F && soundPosition == 0 && hasEnoughFuel() && isEngineActive()) {
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.backSoundRange, dimension, type.backSound, false);
			soundPosition = type.backSoundLength;
		}

		for(EntitySeat seat : getSeats())
		{
			if(seat != null)
				seat.updatePosition();
		}

		if(serverPosX != posX || serverPosY != posY || serverPosZ != posZ || serverYaw != axes.getYaw())
		{
			//Calculate movement on the client and then send position, rotation etc to the server
			if(thePlayerIsDrivingThis)
			{
				FlansMod.getPacketHandler().sendToServer(new PacketVehicleControl(this));
				serverPosX = posX;
				serverPosY = posY;
				serverPosZ = posZ;
				serverYaw = axes.getYaw();
			}
		}

		int animSpeed = 4;
		//Change animation speed based on our current throttle
		if((throttle > 0.05 && throttle <= 0.33) || (throttle < -0.05 && throttle >= -0.33))
		{
			animSpeed = 3;
		}
		else if((throttle > 0.33 && throttle <= 0.66) || (throttle < -0.33 && throttle >= -0.66))
		{
			animSpeed = 2;
		}
		else if((throttle > 0.66 && throttle <= 0.9) || (throttle < -0.66 && throttle >= -0.9))
		{
			animSpeed = 1;
		}
		else if((throttle > 0.9 && throttle <= 1) || (throttle < -0.9 && throttle >= -1))
		{
			animSpeed = 0;
		}

		boolean turningLeft = false;
		boolean turningRight = false;

		if(throttle > 0.05)
		{
			animCountLeft--;
			animCountRight--;
		}
		else if(throttle < -0.05)
		{
			animCountLeft++;
			animCountRight++;
		} else if (wheelsYaw < -1) {
			turningLeft = true;
			animCountLeft++;
			animCountRight--;
			animSpeed = 1;
			if (soundPosition == 0 && hasEnoughFuel() && type.tank && isEngineActive()) {
				PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.engineSound, false);
				soundPosition = type.engineSoundLength;
			}
		} else if (wheelsYaw > 1) {
			turningRight = true;
			animCountLeft--;
			animCountRight++;
			animSpeed = 1;
			if (soundPosition == 0 && hasEnoughFuel() && type.tank && isEngineActive() && isEngineActive()) {
				PacketPlaySound.sendSoundPacket(posX, posY, posZ, type.engineSoundRange, dimension, type.engineSound, false);
				soundPosition = type.engineSoundLength;
			}
		} else {
			turningLeft = false;
			turningRight = false;
		}

		if (animCountLeft <= 0) {
			animCountLeft = animSpeed;
			animFrameLeft++;
		}

		if (animCountRight <= 0) {
			animCountRight = animSpeed;
			animFrameRight++;
		}

		if (throttle < 0 || turningLeft) {
			if (animCountLeft >= animSpeed) {
				animCountLeft = 0;
				animFrameLeft--;
			}
		}

		if (throttle < 0 || turningRight) {
			if (animCountRight >= animSpeed) {
				animCountRight = 0;
				animFrameRight--;
			}
		}

		if (animFrameLeft > type.animFrames) {
			animFrameLeft = 0;
		}
		if (animFrameLeft < 0) {
			animFrameLeft = type.animFrames;
		}

		if (animFrameRight > type.animFrames) {
			animFrameRight = 0;
		}
		if (animFrameRight < 0) {
			animFrameRight = type.animFrames;
		}

		// Decrease throttle each tick.
		if (throttle > 0)
			throttle -= type.throttleDecay;
		else if (throttle < 0)
			throttle += type.throttleDecay;

		//Catch to round the throttle down to zero.
		if (throttle < type.throttleDecay && throttle > -type.throttleDecay)
			throttle = 0;

		PostUpdate();
	}

	private boolean canThrust(DriveableData data, EntityPlayer driver) {
		return !TeamsManager.vehiclesNeedFuel
				|| driverIsCreative()
				|| getDriveableType().fuelTankSize < 0
				|| (data.engine != null && data.fuelInTank > Math.abs(data.engine.fuelConsumption * throttle));
	}

	public Entity getValidTarget() {

		if (placer == null && placerName != null)
			placer = world.getPlayerEntityByName(placerName);
		float targetRange = 150F;
		Entity target = null;
		for (Object obj : world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(targetRange, targetRange, targetRange))) {
			Entity candidateEntity = (Entity) obj;
			boolean targetMobs = true;
			boolean targetPlayers = false;
			boolean targetPlanes = true;
			boolean targetVehicles = true;
			if ((targetMobs && candidateEntity instanceof EntityBat) || (targetPlayers && candidateEntity instanceof EntityPlayer) || (targetPlanes && candidateEntity instanceof EntityPlane) || (targetVehicles && candidateEntity instanceof EntityVehicle)) {
				//Check that this entity is actually in range and visible
				if (candidateEntity.getDistance(this) < targetRange) {
					targetRange = candidateEntity.getDistance(this);
					if (isPartOfThis(candidateEntity)) candidateEntity = null;
					if (candidateEntity instanceof EntityPlayer) {
						if (candidateEntity == placer || candidateEntity.getName().equals(placerName))
							candidateEntity = null;
						if (TeamsManager.enabled && TeamsManager.getInstance().currentRound != null && placer != null) {
							PlayerData placerData = PlayerHandler.getPlayerData(placer, world.isRemote ? Side.CLIENT : Side.SERVER);
							PlayerData candidateData = PlayerHandler.getPlayerData((EntityPlayer) candidateEntity, world.isRemote ? Side.CLIENT : Side.SERVER);
							if (candidateData.team == Team.spectators || candidateData.team == null)
								candidateEntity = null;
							if (!TeamsManager.getInstance().currentRound.gametype.playerCanAttack((EntityPlayerMP) placer, placerData.team, (EntityPlayerMP) candidateEntity, candidateData.team))
								candidateEntity = null;
						}
					}
					target = candidateEntity;
				}
			}
		}
		if (target != null)
			return target;
		else return null;
	}

	public void animateFancyTracks() {
		float funkypart = getVehicleType().trackLinkFix;
		boolean funk = true;
		float funk2 = 0;
		for (int i = 0; i < trackLinksLeft.length; i++) {
			trackLinksLeft[i].prevPosition = trackLinksLeft[i].position;
			trackLinksLeft[i].prevZRot = trackLinksLeft[i].zRot;
			float speed = throttle * 1.5F - (wheelsYaw / 12);
			trackLinksLeft[i].progress += speed;
			if (trackLinksLeft[i].progress > leftTrack.getTrackLength())
				trackLinksLeft[i].progress -= leftTrack.getTrackLength();
			if (trackLinksLeft[i].progress < 0) trackLinksLeft[i].progress += leftTrack.getTrackLength();
			trackLinksLeft[i].position = leftTrack.getPositionOnTrack(trackLinksLeft[i].progress);
			for (; trackLinksLeft[i].zRot > 180F; trackLinksLeft[i].zRot -= 360F) {
			}
			for (; trackLinksLeft[i].zRot <= -180F; trackLinksLeft[i].zRot += 360F) {
			}
			float newAngle = rotateTowards(leftTrack.points.get(leftTrack.getTrackPart(trackLinksLeft[i].progress)), trackLinksLeft[i].position);
			int part = leftTrack.getTrackPart(trackLinksLeft[i].progress);
			if (funk) funk2 = (speed < 0) ? 0 : 1;
			else funk2 = (speed < 0) ? -1 : 0;
			trackLinksLeft[i].zRot = Lerp(trackLinksLeft[i].zRot, newAngle, (part != (funkypart + funk2)) ? 0.5F : 1);

		}

		for (int i = 0; i < trackLinksRight.length; i++) {
			trackLinksRight[i].prevPosition = trackLinksRight[i].position;
			trackLinksRight[i].prevZRot = trackLinksRight[i].zRot;
			float speed = throttle * 1.5F + (wheelsYaw / 12);
			trackLinksRight[i].progress += speed;
			if (trackLinksRight[i].progress > rightTrack.getTrackLength())
				trackLinksRight[i].progress -= leftTrack.getTrackLength();
			if (trackLinksRight[i].progress < 0) trackLinksRight[i].progress += rightTrack.getTrackLength();
			trackLinksRight[i].position = rightTrack.getPositionOnTrack(trackLinksRight[i].progress);
			float newAngle = rotateTowards(rightTrack.points.get(rightTrack.getTrackPart(trackLinksRight[i].progress)), trackLinksRight[i].position);
			int part = rightTrack.getTrackPart(trackLinksRight[i].progress);
			if (funk) funk2 = (speed < 0) ? 0 : 1;
			else funk2 = (speed < 0) ? -1 : 0;
			trackLinksRight[i].zRot = Lerp(trackLinksRight[i].zRot, newAngle, (part != (funkypart + funk2)) ? 0.5F : 1);
		}
	}

	public float rotateTowards(Vector3f point, Vector3f original) {

		float angle = (float) Math.atan2(point.y - original.y, point.x - original.x);
		return angle;
	}

	public void dischargeSmoke() {
		VehicleType type = this.getVehicleType();
		for (int i = 0; i < type.smokers.size(); i++) {
			VehicleType.SmokePoint smoker = type.smokers.get(i);
			Vector3f dir = smoker.direction;
			Vector3f pos = smoker.position;
			int time = smoker.detTime;

			dir = axes.findLocalVectorGlobally(dir);
			pos = axes.findLocalVectorGlobally(pos);

			if (EnumDriveablePart.getPart(smoker.part) == EnumDriveablePart.turret) {
				dir = rotate(getSeats()[0].looking.findLocalVectorGlobally(smoker.direction));
				pos = getPositionOnTurret(smoker.position, false);
			}

			//FlansMod.getPacketHandler().sendToAllAround(new PacketSmokeGrenade(posX + pos.x/16, posY + pos.y/16, posZ + pos.z/16, dir.x, dir.y, dir.z, time), posX, posY, posZ, 150, dimension);

			//FlansMod.proxy.spawnSmokeGrenade("flansmod.smoker", posX + pos.x/16, posY + pos.y/16, posZ + pos.z/16, dir.x, dir.y, dir.z, time);

			FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.smoker", posX + pos.x / 16, posY + pos.y / 16, posZ + pos.z / 16, dir.x, dir.y, dir.z), posX, posY, posZ, 150, dimension);
		}
	}

	public List<Entity> findEntitiesWithinbounds() {
		VehicleType type = this.getVehicleType();
		AxisAlignedBB initialBox = this.getEntityBoundingBox();
		List<Entity> riddenEntities = world.getEntitiesWithinAABB(Entity.class, initialBox);

		Vector3f size = new Vector3f(type.harvestBoxSize.x / 8F, type.harvestBoxSize.y / 8F, type.harvestBoxSize.z / 8F);
		Vector3f pos = new Vector3f(type.harvestBoxPos.x / 8F, type.harvestBoxPos.y / 8F, type.harvestBoxPos.z / 8F);
		boolean fancy = false;
		if (!fancy) {
			for (float x = pos.x; x <= pos.x + size.x; x++) {
				for (float y = pos.y; y <= pos.y + size.y; y++) {
					for (float z = pos.z; z <= pos.z + size.z; z++) {
						Vector3f v = axes.findLocalVectorGlobally(new Vector3f(x, y, z));

						double entX = (posX + v.x);
						double entY = (posY + v.y);
						double entZ = (posZ + v.z);
						AxisAlignedBB checkBox = this.getEntityBoundingBox().offset(v.x, v.y, v.z);

						List<Entity> entityhere = world.getEntitiesWithinAABB(Entity.class, checkBox);

						for (int i = 0; i < entityhere.size(); i++) {
							if (entityhere.get(i) instanceof EntityPlayer && !isPartOfThis(entityhere.get(i)))
								riddenEntities.add(entityhere.get(i));
						}
						//Iterator<Entity> iter = entityhere.iterator();
						/**
						 while( iter.hasNext() )
						 {
						 Entity entity = iter.next();
						 if(isPartOfThis(entity)) iter.remove();
						 if(entity instanceof EntityBullet) iter.remove();
						 } */

					}
				}
			}
		} else {
			AxisAlignedBB checkBox = this.getEntityBoundingBox().expand(50, 50, 50);

			List<Entity> entityhere = world.getEntitiesWithinAABB(EntityPlayer.class, checkBox);

			for (int i = 0; i < entityhere.size(); i++) {
				if (entityhere.get(i) instanceof EntityPlayer) {
					riddenEntities.add(entityhere.get(i));
					//AxisAlignedBB checkBox2 = this.boundingBox.copy().offset(this.posX - entityhere.get(i).posX,this.posY - entityhere.get(i).posY, this.posZ - entityhere.get(i).posZ);
				}
			}

		}
		return riddenEntities;
	}

	public Vector3f transformPart(Vector3f current, Vector3f target, Vector3f rate) {
		Vector3f newPos = current;

		if (Math.sqrt((current.x - target.x) * (current.x - target.x)) > rate.x / 2) {
			if (current.x > target.x) {
				current.x = current.x - rate.x;
			} else if (current.x < target.x) {
				current.x = current.x + rate.x;
			}
		} else {
			current.x = target.x;
		}

		if (Math.sqrt((current.y - target.y) * (current.y - target.y)) > rate.y / 2) {
			if (current.y > target.y) {
				current.y = current.y - rate.y;
			} else if (current.y < target.y) {
				current.y = current.y + rate.y;
			}
		} else {
			current.y = target.y;
		}

		if (Math.sqrt((current.z - target.z) * (current.z - target.z)) > rate.z / 2) {
			if (current.z > target.z) {
				current.z = current.z - rate.z;
			} else if (current.z < target.z) {
				current.z = current.z + rate.z;
			}
		} else {
			current.z = target.z;
		}

		return newPos;
	}

	protected void fall(float k) {
		if (k <= 10) return;
		float damage = MathHelper.ceil(k) * 2;

		boolean no_damage = true;
		if (damage > 0 && invulnerableUnmountCount == 0 && this.ticksExisted > 20) {
			DriveableType type = getDriveableType();
			damage = (int) (damage * type.fallDamageFactor);
			attackPart(EnumDriveablePart.core, DamageSource.FALL, damage);
			if (type.wheelPositions.length > 0) {
				attackPart(type.wheelPositions[0].part, DamageSource.FALL, damage / 5);
			}

			no_damage = false;
		}
		//	FlansMod.log("fall%s : tick=%d damage=%.1f", no_damage? " no damage":"", this.ticksExisted, damage);
	}

	public float Lerp(float start, float end, float percent)
	{
		float result = (start + percent * (end - start));

		return result;
	}

	public static float Clamp(float val, float min, float max)
	{
		return Math.max(min, Math.min(max, val));
	}

	private float averageAngles(float a, float b)
	{
		//FlansMod.log.debug("Pre  " + a + " " + b);

		float pi = (float)Math.PI;
		for(; a > b + pi; a -= 2 * pi) ;
		for(; a < b - pi; a += 2 * pi) ;

		float avg = (a + b) / 2F;

		for(; avg > pi; avg -= 2 * pi) ;
		for(; avg < -pi; avg += 2 * pi) ;

		//FlansMod.log.debug("Post " + a + " " + b + " " + avg);

		return avg;
	}

	private Vec3d subtract(Vec3d a, Vec3d b)
	{
		return new Vec3d(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	private Vec3d crossProduct(Vec3d a, Vec3d b)
	{
		return new Vec3d(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
	}

	@Override
	public boolean landVehicle()
	{
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i)
	{
		if(world.isRemote || isDead)
			return true;

		VehicleType type = getVehicleType();

		if (damagesource.damageType.equals("player")
				&& damagesource.getTrueSource().onGround
				&& (getSeat(0) == null || getSeat(0).getControllingPassenger() == null)
				&& ((damagesource.getTrueSource() instanceof EntityPlayer && ((EntityPlayer)damagesource.getTrueSource()).capabilities.isCreativeMode) || TeamsManager.survivalCanBreakVehicles))
		{
			ItemStack vehicleStack = new ItemStack(type.item, 1, driveableData.paintjobID);
			NBTTagCompound tags = new NBTTagCompound();
			vehicleStack.setTagCompound(tags);
			driveableData.writeToNBT(tags);

			DriveableDeathByHandEvent driveableDeathByHandEvent = new DriveableDeathByHandEvent(this, (EntityPlayer)damagesource.getTrueSource(), vehicleStack);
			MinecraftForge.EVENT_BUS.post(driveableDeathByHandEvent);

			if(!driveableDeathByHandEvent.isCanceled()) {
				if (!world.isRemote && damagesource.getTrueSource() instanceof EntityPlayer) {
					//FlansMod.log("Player %s broke vehicle %s (%d) at (%f, %f, %f)", ((EntityPlayerMP) damagesource.getTrueSource()).getDisplayName(), type.shortName, getEntityId(), posX, posY, posZ);
				}
				entityDropItem(vehicleStack, 0.5F);
				setDead();
			}
		}
		return super.attackEntityFrom(damagesource, i);
	}

	public VehicleType getVehicleType()
	{
		return VehicleType.getVehicle(driveableType);
	}

	@Override
	public float getPlayerRoll()
	{
		return axes.getRoll();
	}

	public void Recoil() {

	}

	@Override
	protected void dropItemsOnPartDeath(Vector3f midpoint, DriveablePart part)
	{
	}

	@Override
	public String getBombInventoryName()
	{
		return "Mines";
	}

	@Override
	public String getMissileInventoryName()
	{
		return "Shells";
	}

	@Override
	public boolean hasMouseControlMode()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EntityLivingBase getCamera()
	{
		return null;
	}

	public boolean hasBothTracks() {
		boolean tracks = true;
		if (!isPartIntact(EnumDriveablePart.leftTrack)) {
			tracks = false;
		}

		if (!isPartIntact(EnumDriveablePart.rightTrack)) {
			tracks = false;
		}

		return tracks;
	}

	@Override
	public void setDead()
	{
		super.setDead();
	}
}
