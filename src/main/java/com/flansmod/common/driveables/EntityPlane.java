package com.flansmod.common.driveables;

import com.flansmod.client.FlansModClient;
import com.flansmod.client.handlers.KeyInputHandler;
import com.flansmod.client.model.animation.AnimationController;
import com.flansmod.common.RotatedAxes;
import com.flansmod.common.eventhandlers.DriveableDeathByHandEvent;
import com.flansmod.common.network.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.flansmod.common.FlansMod;
import com.flansmod.common.teams.TeamsManager;
import com.flansmod.common.tools.ItemTool;
import com.flansmod.common.vector.Matrix4f;
import com.flansmod.common.vector.Vector3f;
import org.lwjgl.input.Keyboard;

public class EntityPlane extends EntityDriveable
{
	private boolean invisible;
	/**
	 * The flap positions, used for rendering and for controlling the plane rotations
	 */
	public float flapsYaw, flapsPitchLeft, flapsPitchRight;
	/**
	 * Position of looping engine sound
	 */
	public int soundPosition;
	/**
	 * The angle of the propeller for the renderer
	 */
	public float rotorAngle;
	/**
	 * Weapon delays
	 */
	public int bombDelay, gunDelay;
	/**
	 * Despawn timer
	 */
	public int ticksSinceUsed = 0;
	/**
	 * Mostly aesthetic model variables. Gear actually has a variable hitbox
	 */
	public boolean varGear = true, varDoor = false, varWing = false;
	public boolean doorsHaveShut = false;
	/**
	 * Delayer for gear, door and wing buttons
	 */
	public int toggleTimer = 0;
	/**
	 * Current plane mode
	 */
	public EnumPlaneMode mode;

	//Animation positions
	public Vector3f wingPos = new Vector3f(0, 0, 0);
	public Vector3f wingRot = new Vector3f(0, 0, 0);
	public Vector3f wingWheelPos = new Vector3f(0, 0, 0);
	public Vector3f wingWheelRot = new Vector3f(0, 0, 0);
	public Vector3f coreWheelPos = new Vector3f(0, 0, 0);
	public Vector3f coreWheelRot = new Vector3f(0, 0, 0);
	public Vector3f tailWheelPos = new Vector3f(0, 0, 0);
	public Vector3f tailWheelRot = new Vector3f(0, 0, 0);
	public Vector3f doorPos = new Vector3f(0, 0, 0);
	public Vector3f doorRot = new Vector3f(0, 0, 0);


	//Duplicate positions for smoothness
	public Vector3f prevWingPos = new Vector3f(0, 0, 0);
	public Vector3f prevWingRot = new Vector3f(0, 0, 0);
	public Vector3f prevWingWheelPos = new Vector3f(0, 0, 0);
	public Vector3f prevWingWheelRot = new Vector3f(0, 0, 0);
	public Vector3f prevCoreWheelPos = new Vector3f(0, 0, 0);
	public Vector3f prevCoreWheelRot = new Vector3f(0, 0, 0);
	public Vector3f prevTailWheelPos = new Vector3f(0, 0, 0);
	public Vector3f prevTailWheelRot = new Vector3f(0, 0, 0);
	public Vector3f prevDoorPos = new Vector3f(0, 0, 0);
	public Vector3f prevDoorRot = new Vector3f(0, 0, 0);
	public float xSpeed = 0;
	public float ySpeed = 0;
	public float zSpeed = 0;
	public float rollSpeed = 0;
	public FlightController flightController = new FlightController();
	public AnimationController anim = new AnimationController();
	public boolean initiatedAnim = false;
	
	public EntityPlane(World world)
	{
		super(world);
	}
	
	public EntityPlane(World world, double x, double y, double z, PlaneType type, DriveableData data, EntityPlayer p)
	{
		super(world, type, data, p);
		setPosition(x, y, z);
		prevPosX = x;
		prevPosY = y;
		prevPosZ = z;
		initType(type, true, false);
	}
	
	public EntityPlane(World world, double x, double y, double z, EntityPlayer placer, PlaneType type,
					   DriveableData data)
	{
		this(world, x, y + 90 / 16F, z, type, data, placer);
		rotateYaw(placer.rotationYaw + 90F);
		rotatePitch(type.restingPitch);
	}
	
	@Override
	public void initType(DriveableType type, boolean firstSpawn, boolean clientSide)
	{
		super.initType(type, firstSpawn, clientSide);
		mode = (((PlaneType)type).mode == EnumPlaneMode.HELI ? EnumPlaneMode.HELI : EnumPlaneMode.PLANE);
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound tag)
	{
		super.writeEntityToNBT(tag);
		tag.setTag("Pos", this.newDoubleNBTList(this.posX, this.posY + 1D, this.posZ));
		tag.setBoolean("VarGear", varGear);
		tag.setBoolean("VarDoor", varDoor);
		tag.setBoolean("VarWing", varWing);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound tag)
	{
		super.readEntityFromNBT(tag);
		varGear = tag.getBoolean("VarGear");
		varDoor = tag.getBoolean("VarDoor");
		varWing = tag.getBoolean("VarWing");
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
		if(!FMLCommonHandler.instance().getSide().isClient())
			return;
		if(!FlansMod.proxy.mouseControlEnabled())
			return;
		
		float sensitivity = 0.02F;
		
		flapsPitchLeft -= sensitivity * deltaY;
		flapsPitchRight -= sensitivity * deltaY;

		if (mode != EnumPlaneMode.SIXDOF) {
			flapsPitchLeft -= sensitivity * deltaX;
			flapsPitchRight += sensitivity * deltaX;
		} else {
			flapsPitchLeft -= sensitivity * deltaX;
			flapsPitchRight += sensitivity * deltaX;
		}
	}
	
	@Override
	public void setPositionRotationAndMotion(double x, double y, double z, float yaw, float pitch, float roll,
											 double motX, double motY, double motZ, float velYaw, float velPitch,
											 float velRoll, float throttle, float steeringYaw)
	{
		super.setPositionRotationAndMotion(x, y, z, yaw, pitch, roll, motX, motY, motZ, velYaw, velPitch, velRoll,
				throttle, steeringYaw);
		flapsYaw = steeringYaw;
	}

	public void setRotorPosition(float current, float previous) {
		rotorAngle = current;
		prevRotorAngle = previous;
	}

	public void setPropPosition(float current, float previous) {
		rotorAngle = current;
		prevPropAngle = previous;
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
		
		PlaneType type = this.getPlaneType();
		//Check each seat in order to see if the player can sit in it
		for(int i = 0; i <= type.numPassengers; i++)
		{
			if(getSeat(i).processInitialInteract(entityplayer, hand))
			{
				if(i == 0)
				{
					bombDelay = type.planeBombDelay;
					FlansMod.proxy.doTutorialStuff(entityplayer, this);
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean serverHandleKeyPress(int key, EntityPlayer player)
	{
		return super.serverHandleKeyPress(key, player);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean pressKey(int key, EntityPlayer player, boolean isOnEvent)
	{
		PlaneType type = this.getPlaneType();
		//Send keys which require server side updates to the server
		boolean canThrust = ((getSeat(0) != null && getSeat(0).getControllingPassenger() instanceof EntityPlayer
				&& ((EntityPlayer)getSeat(0).getControllingPassenger()).capabilities.isCreativeMode)
				|| getDriveableData().fuelInTank > 0) && hasWorkingProp() || type.fuelTankSize < 0;
		switch(key)
		{
			case 0: //Accelerate : Increase the throttle, up to 1.
			{
				if(canThrust || throttle < 0F)
				{
					throttle += 0.002F;
					if(throttle > 1F)
						throttle = 1F;
					xSpeed += 0.5F;
				}
				return true;
			}
			case 1: //Decelerate : Decrease the throttle, down to -1, or 0 if the plane cannot reverse
			{
				if(canThrust || throttle > 0F)
				{
					throttle -= 0.005F;
					if(throttle < -1F)
						throttle = -1F;
					if(throttle < 0F && type.maxNegativeThrottle == 0F)
						throttle = 0F;
					xSpeed -= 0.5F;
				}
				return true;
			}
			case 2: //Left : Yaw the flaps left
			{
				if (mode != EnumPlaneMode.SIXDOF)
					flapsYaw -= 1F;
				zSpeed -= 1F;
				return true;
			}
			case 3: //Right : Yaw the flaps right
			{
				if (mode != EnumPlaneMode.SIXDOF)
					flapsYaw += 1F;
				zSpeed += 1F;
				return true;
			}
			case 4: //Up : Pitch the flaps up
			{
				if (mode != EnumPlaneMode.SIXDOF) {
					flapsPitchLeft += 1F;
					flapsPitchRight += 1F;
				}
				ySpeed += 1F;
				return true;
			}
			case 5: //Down : Pitch the flaps down
			{
				if (mode != EnumPlaneMode.SIXDOF) {
					flapsPitchLeft -= 1F;
					flapsPitchRight -= 1F;
				}
				ySpeed -= 1F;
				return true;
			}
			case 6: //Exit : Get out
			{
				if (getSeats()[0].getControllingPassenger() != null) {
					getSeats()[0].getControllingPassenger().setInvisible(false);
					invisible = false;
				}

				return true;
			}
			case 7: //Inventory : Check to see if this plane allows in-flight inventory editing or if the plane is on the ground
			{
				if(world.isRemote && (type.invInflight || (Math.abs(throttle) < 0.1F && onGround)))
				{
					FlansMod.proxy.openDriveableMenu((EntityPlayer)getSeat(0).getControllingPassenger(), world, this);
				}
				return true;
			}
			case 10: //Change control mode
			{
				FlansMod.proxy.changeControlMode((EntityPlayer)getSeat(0).getControllingPassenger());
				getSeat(0).playerLooking = new RotatedAxes(0, 0, 0);
				return true;
			}
			case 11: //Roll left
			{
				if (mode != EnumPlaneMode.SIXDOF) {
					flapsPitchLeft += 1F;
					flapsPitchRight -= 1F;
				} else {
					flapsYaw -= 0.25F;
				}
				return true;
			}
			case 12: //Roll right
			{
				if (mode != EnumPlaneMode.SIXDOF) {
					flapsPitchLeft -= 1F;
					flapsPitchRight += 1F;
				} else {
					flapsYaw += 0.25F;
				}
				return true;
			}
			case 13: // Gear
			{
				if(toggleTimer <= 0)
				{
					if (world.isAirBlock(new BlockPos((int) posX, (int) (posY - 3), (int) posZ))) {
						varGear = !varGear;
						player.sendMessage(new TextComponentString("Landing gear " + (varGear ? "down" : "up")));
						toggleTimer = 10;
						FlansMod.getPacketHandler().sendToServer(new PacketDriveableControl(this));
					}
				}
				return true;
			}
			case 14: // Door
			{
				if(toggleTimer <= 0)
				{
					varDoor = !varDoor;
					if(type.hasDoor)
						player.sendMessage(new TextComponentString("Doors " + (varDoor ? "open" : "closed")));
					toggleTimer = 10;
					FlansMod.getPacketHandler().sendToServer(new PacketDriveableControl(this));
				}
				return true;
			}
			case 15: // Wing
			{
				if(toggleTimer <= 0)
				{
					if(type.hasWing)
					{
						varWing = !varWing;
						player.sendMessage(new TextComponentString("Switching mode"));
					}
					if(type.mode == EnumPlaneMode.VTOL)
					{
						if(mode == EnumPlaneMode.HELI)
							mode = EnumPlaneMode.PLANE;
						else mode = EnumPlaneMode.HELI;
						player.sendMessage(new TextComponentString(
								mode == EnumPlaneMode.HELI ? "Entering hover mode" : "Entering plane mode"));
					}
					anim.changeState(varWing ? 0 : 1);
					toggleTimer = 10;
					FlansMod.getPacketHandler().sendToServer(new PacketDriveableControl(this));
				}
				return true;
			}
			case 16: // Trim Button
			{
				axes.setAngles(axes.getYaw(), 0, 0);
				return true;
			}
			case 18: //Flare
			{
				if (type.hasFlare && this.ticksFlareUsing <= 0 && this.flareDelay <= 0) {
					this.ticksFlareUsing = type.timeFlareUsing * 20;
					this.flareDelay = type.flareDelay;
					if (world.isRemote) {
						FlansMod.getPacketHandler().sendToServer(new PacketDriveableKey(key));
					} else {
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
	
	@Override
	public void updateKeyHeldState(int key, boolean held)
	{
		super.updateKeyHeldState(key, held);
	}
	public int ticks;
	public EntityPlayer driver;
	//boolean sneak = KeyInputHandler.isSneak;
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		//Set previous positions
		prevWingPos = wingPos;
		prevWingRot = wingRot;
		prevWingWheelPos = wingWheelPos;
		prevWingWheelRot = wingWheelRot;
		prevCoreWheelPos = coreWheelPos;
		prevCoreWheelRot = coreWheelRot;
		prevTailWheelPos = tailWheelPos;
		prevTailWheelRot = tailWheelRot;
		prevDoorPos = doorPos;
		prevDoorRot = doorRot;
		if (getPlaneType().valkyrie) {
			if (!initiatedAnim) {
				anim.initPoses();
				anim.initAnim();
				initiatedAnim = true;
				anim.changeState(varWing ? 0 : 1);
			}

			if (initiatedAnim) {
				int i = varWing ? 0 : 1;
				anim.UpdateAnim(i);
			}
		}

		if (initiatedAnim && throttle > 0.2F) {
			Vector3f v = anim.getFullPosition(new Vector3f(151, -25, -24), anim.parts.get(5));
			v = axes.findLocalVectorGlobally(new Vector3f(-v.x, -v.y, v.z));
			Vector3f v2 = anim.getFullPosition(new Vector3f(151, -25, 24), anim.parts.get(8));

			v2 = axes.findLocalVectorGlobally(new Vector3f(-v2.x, -v2.y, v2.z));
			for (int i = 0; i < 4; i++) {
				if (!(Float.isNaN(v.x))) {
					//FlansMod.proxy.spawnParticle("flansmod.afterburn", posX+v2.x/16F, posY+(v2.y/16F), posZ+(v2.z/16F), 0, 0F, 0);
					FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.afterburn", posX + v2.x / 16F, posY + v2.y / 16F, posZ + v2.z / 16F, 0, 0, 0), posX + v2.x / 16F, posY + v2.y / 16F, posZ + v2.z / 16F, 150, dimension);
				}
				if (!(Float.isNaN(v.x))) {
					//FlansMod.proxy.spawnParticle("flansmod.afterburn", posX+v.x/16F, posY+(v.y/16F), posZ+(v.z/16F), 0, 0F, 0);
					FlansMod.getPacketHandler().sendToAllAround(new PacketParticle("flansmod.afterburn", posX + v.x / 16F, posY + v.y / 16F, posZ + v.z / 16F, 0, 0, 0), posX + v.x / 16F, posY + v.y / 16F, posZ + v.z / 16F, 150, dimension);
				}
			}

		}
		
		if(!readyForUpdates)
		{
			return;
		}
		
		//Get plane type
		PlaneType type = getPlaneType();
		DriveableData data = getDriveableData();
		if (!invisible && this.world.isRemote && getSeat(0).getControllingPassenger() != null && type.setPlayerInvisible) {
			invisible = true;
		}
		if(invisible && this.world.isRemote && getSeat(0).getControllingPassenger() != null && type.setPlayerInvisible) {
			getSeat(0).getControllingPassenger().setInvisible(true);
		}
		//if (sneak) {
		//	if(!this.world.isRemote && getSeat(0).getControllingPassenger() != null && type.setPlayerInvisible) {
		//		if (driver != null) {
		//			driver.setInvisible(false);
		//		}
		//	}
		//}

		if(type == null)
		{
			FlansMod.log.warn("Plane type null. Not ticking plane");
			return;
		}
		
		//Work out if this is the client side and the player is driving
		boolean thePlayerIsDrivingThis =
				world.isRemote && getSeat(0) != null && getSeat(0).getControllingPassenger() instanceof EntityPlayer
						&& FlansMod.proxy.isThePlayer((EntityPlayer)getSeat(0).getControllingPassenger());
		//Despawning
		ticksSinceUsed++;
		if(!world.isRemote && getSeat(0).getControllingPassenger() != null)
			ticksSinceUsed = 0;
		if(!world.isRemote && TeamsManager.planeLife > 0 && ticksSinceUsed > TeamsManager.planeLife * 20)
		{
			setDead();
		}

		if (this.world.isRemote && (this.varFlare || this.ticksFlareUsing > 0)) {
			if (this.ticksExisted % 5 == 0) {
				Vector3f dir = axes.findLocalVectorGlobally(new Vector3f(0, -0.5F, 0));
				FlansMod.proxy.spawnParticle("flansmod.flare", this.posX, this.posY, this.posZ,
						dir.x,
						dir.y,
						dir.z);
			}
		}
		if (this.ticksFlareUsing > 0)
			this.ticksFlareUsing--;
		if (this.flareDelay > 0)
			this.flareDelay--;
		
		//Shooting, inventories, etc.
		//Decrement bomb and gun timers
		if(bombDelay > 0)
			bombDelay--;
		if(gunDelay > 0)
			gunDelay--;
		if(toggleTimer > 0)
			toggleTimer--;
		
		//Aesthetics
		//Rotate the propellers
		/*if(hasEnoughFuel())
		{
			propAngle += (Math.pow(throttle, 0.4)) * 1.5;
		}*/

		if (!varWing) {
			wingPos = transformPart(wingPos, type.wingPos1, type.wingRate);
			wingRot = transformPart(wingRot, type.wingRot1, type.wingRotRate);
		} else {
			wingPos = transformPart(wingPos, type.wingPos2, type.wingRate);
			wingRot = transformPart(wingRot, type.wingRot2, type.wingRotRate);
		}

		if (varGear) {
			wingWheelPos = transformPart(wingWheelPos, type.wingWheelPos1, type.wingWheelRate);
			wingWheelRot = transformPart(wingWheelRot, type.wingWheelRot1, type.wingWheelRotRate);
			coreWheelPos = transformPart(coreWheelPos, type.bodyWheelPos1, type.bodyWheelRate);
			coreWheelRot = transformPart(coreWheelRot, type.bodyWheelRot1, type.bodyWheelRotRate);
			tailWheelPos = transformPart(tailWheelPos, type.tailWheelPos1, type.tailWheelRate);
			tailWheelRot = transformPart(tailWheelRot, type.tailWheelRot1, type.tailWheelRotRate);

		} else {
			wingWheelPos = transformPart(wingWheelPos, type.wingWheelPos2, type.wingWheelRate);
			wingWheelRot = transformPart(wingWheelRot, type.wingWheelRot2, type.wingWheelRotRate);
			coreWheelPos = transformPart(coreWheelPos, type.bodyWheelPos2, type.bodyWheelRate);
			coreWheelRot = transformPart(coreWheelRot, type.bodyWheelRot2, type.bodyWheelRotRate);
			tailWheelPos = transformPart(tailWheelPos, type.tailWheelPos2, type.tailWheelRate);
			tailWheelRot = transformPart(tailWheelRot, type.tailWheelRot2, type.tailWheelRotRate);
		}

		if (!varDoor) {
			doorPos = transformPart(doorPos, type.doorPos1, type.doorRate);
			doorRot = transformPart(doorRot, type.doorRot1, type.doorRotRate);
		} else {
			doorPos = transformPart(doorPos, type.doorPos2, type.doorRate);
			doorRot = transformPart(doorRot, type.doorRot2, type.doorRotRate);
		}

		if (!world.isAirBlock(new BlockPos((int) posX, (int) (posY - 10), (int) posZ)) && throttle <= 0.4) {
			if (!varGear && getSeat(0) != null && getSeat(0).getControllingPassenger() != null && type.autoDeployLandingGearNearGround) {
				((EntityPlayer) getSeat(0).getControllingPassenger()).sendMessage(new TextComponentString("Deploying landing gear"));
			}
			varGear = true;
			if (type.foldWingForLand) {
				if (varWing && getSeat(0) != null && getSeat(0).getControllingPassenger() != null) {
					((EntityPlayer) getSeat(0).getControllingPassenger()).sendMessage(new TextComponentString("Extending wings"));
				}
				varWing = false;
			}
		}

		if (!world.isAirBlock(new BlockPos((int) posX, (int) (posY - 3), (int) posZ)) && throttle <= 0.05 && type.autoOpenDoorsNearGround) {
			if (!doorsHaveShut) {
				varDoor = true;
			}
			doorsHaveShut = true;
		} else if (!type.flyWithOpenDoor) {
			varDoor = false;
			doorsHaveShut = false;
		}

		if (!isPartIntact(EnumDriveablePart.tail) && type.spinWithoutTail) flapsYaw = 15;
		
		//Return the flaps to their resting position
		flapsYaw *= 0.9F;
		flapsPitchLeft *= 0.9F;
		flapsPitchRight *= 0.9F;
		
		//Limit flap angles
		if(flapsYaw > 20)
			flapsYaw = 20;
		if(flapsYaw < -20)
			flapsYaw = -20;
		if(flapsPitchRight > 20)
			flapsPitchRight = 20;
		if(flapsPitchRight < -20)
			flapsPitchRight = -20;
		if(flapsPitchLeft > 20)
			flapsPitchLeft = 20;
		if(flapsPitchLeft < -20)
			flapsPitchLeft = -20;
		
		//Player is not driving this. Update its position from server update packets 
		if(world.isRemote && !thePlayerIsDrivingThis)
		{
			//The driveable is currently moving towards its server position. Continue doing so.
			if(serverPositionTransitionTicker > 0)
			{
				moveTowardServerPosition();
			}
			//If the driveable is at its server position and does not have the next update, it should just simulate itself as a server side plane would, so continue
		}
		
		//Movement
		
		//Throttle handling
		//Without a player, default to 0
		//With a player default to 0.5 for helicopters (hover speed)
		//And default to the range 0.25 ~ 0.5 for planes (taxi speed ~ take off speed)
		float throttlePull = 0.99F;
		if(getSeat(0) != null && getSeat(0).getControllingPassenger() != null && mode == EnumPlaneMode.HELI &&
				canThrust() && type.heliThrottlePull)
			throttle = (throttle - 0.5F) * throttlePull + 0.5F;

		if (!canThrust()) {
			throttle *= 0.99;
			if (throttle > 0.8) {
				throttle -= 0.001;
			}
			if (throttle > 0) {
				throttle -= 0.001;
			}
		}

		//Get the speed of the plane
		/*float lastTickSpeed = (float)getSpeedXYZ();
		
		//Alter angles
		//Sensitivity function
		float sensitivityAdjust = 2.00677104758f - (float)Math.exp(-2.0f * throttle) / (4.5f * (throttle + 0.1f));
		sensitivityAdjust = MathHelper.clamp(sensitivityAdjust, 0.0f, 1.0f);
		//Scalar
		sensitivityAdjust *= 0.125F;
		
		float yaw = flapsYaw * (flapsYaw > 0 ? type.turnLeftModifier : type.turnRightModifier) * sensitivityAdjust;
		
		//if(throttle < 0.2F)
		//	sensitivityAdjust = throttle * 2.5F;
		//Pitch according to the sum of flapsPitchLeft and flapsPitchRight / 2
		float flapsPitch = (flapsPitchLeft + flapsPitchRight) / 2F;
		float pitch = flapsPitch * (flapsPitch > 0 ? type.lookUpModifier : type.lookDownModifier) * sensitivityAdjust;
		
		//Roll according to the difference between flapsPitchLeft and flapsPitchRight / 2
		float flapsRoll = (flapsPitchRight - flapsPitchLeft) / 2F;
		float roll = flapsRoll * (flapsRoll > 0 ? type.rollLeftModifier : type.rollRightModifier) * sensitivityAdjust;
		
		//Damage modifiers
		if(mode == EnumPlaneMode.PLANE)
		{
			if(!isPartIntact(EnumDriveablePart.tail))
			{
				yaw = 0;
				pitch = 0;
				roll = 0;
			}
			if(!isPartIntact(EnumDriveablePart.leftWing))
				roll -= 7F * getSpeedXZ();
			if(!isPartIntact(EnumDriveablePart.rightWing))
				roll += 7F * getSpeedXZ();
		}
		
		axes.rotateLocalYaw(yaw);
		axes.rotateLocalPitch(pitch);
		axes.rotateLocalRoll(-roll);
		
		if(world.isRemote && !FlansMod.proxy.mouseControlEnabled())
		{
			//axes.rotateGlobalRoll(-axes.getRoll() * 0.1F);
		}
		
		//Some constants
		float g = 0.98F / 10F;
		float drag = 1F - (0.05F * type.drag);
		float wobbleFactor = 0F;//.005F;
		
		float throttleScaled = 0.01F * (type.maxThrottle + (data.engine == null ? 0 : data.engine.engineSpeed));
		
		if(!canThrust())
			throttleScaled = 0;
		
		int numPropsWorking = 0;
		int numProps = 0;
		
		float fuelConsumptionMultiplier = 2F;
		
		switch(mode)
		{
			case HELI:
				
				//Count the number of working propellers
				for(Propeller prop : type.heliPropellers)
					if(isPartIntact(prop.planePart))
						numPropsWorking++;
				numProps = type.heliPropellers.size();
				
				Vector3f up = axes.getYAxis();
				
				throttleScaled *= numProps == 0 ? 0 : (float)numPropsWorking / numProps * 2F;
				
				float upwardsForce = throttle * throttleScaled + (g - throttleScaled / 2F);
				if(throttle < 0.5F)
					upwardsForce = g * throttle * 2F;
				
				if(!isPartIntact(EnumDriveablePart.blades))
				{
					upwardsForce = 0F;
				}
				
				//Move up
				//Throttle - 0.5 means that the positive throttle scales from -0.5 to +0.5. Thus it accounts for gravity-ish
				motionX += upwardsForce * up.x * 0.5F;
				motionY += upwardsForce * up.y;
				motionZ += upwardsForce * up.z * 0.5F;
				//Apply gravity
				motionY -= g;
				
				//Apply wobble
				//motionX += rand.nextGaussian() * wobbleFactor;
				//motionY += rand.nextGaussian() * wobbleFactor;
				//motionZ += rand.nextGaussian() * wobbleFactor;
				
				//Apply drag
				motionX *= drag;
				motionY *= drag;
				motionZ *= drag;
				
				data.fuelInTank -= upwardsForce * fuelConsumptionMultiplier * data.engine.fuelConsumption;
				
				break;
			
			case PLANE:
				//Count the number of working propellers
				for(Propeller prop : type.propellers)
					if(isPartIntact(prop.planePart))
						numPropsWorking++;
				numProps = type.propellers.size();
				
				float throttleTemp = throttle * (numProps == 0 ? 0 : (float)numPropsWorking / numProps * 2F);
				
				//Apply forces
				Vector3f forwards = (Vector3f)axes.getXAxis().normalise();
				
				//Sanity limiter
				if(lastTickSpeed > 2F)
					lastTickSpeed = 2F;
				
				float newSpeed = lastTickSpeed + throttleScaled * 2F;
				
				//Calculate the amount to alter motion by
				float proportionOfMotionToCorrect = 2F * throttleTemp - 0.5F;
				if(proportionOfMotionToCorrect < throttle * 0.25f)
					proportionOfMotionToCorrect = throttle * 0.25f;
				if(proportionOfMotionToCorrect > 0.6F)
					proportionOfMotionToCorrect = 0.6F;
				
				//Apply gravity
				g = 0.98F / 20F;
				motionY -= g;
				
				//Apply lift
				int numWingsIntact = 0;
				if(isPartIntact(EnumDriveablePart.rightWing)) numWingsIntact++;
				if(isPartIntact(EnumDriveablePart.leftWing)) numWingsIntact++;
				
				float amountOfLift = 2F * g * throttleTemp * numWingsIntact / 2F;
				if(amountOfLift > g)
					amountOfLift = g;
				
				if(!isPartIntact(EnumDriveablePart.tail))
					amountOfLift *= 0.75F;
				
				motionY += amountOfLift;
				
				//Cut out some motion for correction
				motionX *= 1F - proportionOfMotionToCorrect;
				motionY *= 1F - proportionOfMotionToCorrect;
				motionZ *= 1F - proportionOfMotionToCorrect;
				
				//Add the corrected motion
				motionX += proportionOfMotionToCorrect * newSpeed * forwards.x;
				motionY += proportionOfMotionToCorrect * newSpeed * forwards.y;
				motionZ += proportionOfMotionToCorrect * newSpeed * forwards.z;
				
				//Apply drag
				motionX *= drag;
				motionY *= drag;
				motionZ *= drag;
				
				data.fuelInTank -= throttleScaled * fuelConsumptionMultiplier * data.engine.fuelConsumption;
				break;
			default:
				break;
		}*/
		flightController.fly(this);
		
		double motion = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
		if(motion > 10)
		{
			motionX *= 10 / motion;
			motionY *= 10 / motion;
			motionZ *= 10 / motion;
		} else if (!(getSeat(0) != null && getSeat(0).getControllingPassenger() instanceof EntityPlayer)) {
			// Slow down the plane/heli if its empty. We take 1 off emptyDrag, as FlightController applies a drag of 1 to it already.
			motionX *= 1F - (0.05F * (type.emptyDrag - 1));
			motionZ *= 1F - (0.05F * (type.emptyDrag - 1));

		}

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
			if(wheel != null && world != null)
				if(type.floatOnWater && world.containsAnyLiquid(wheel.getEntityBoundingBox()))
				{
					motionY += type.buoyancy;
				}
		}
		
		//Move the wheels first
		for(EntityWheel wheel : wheels)
		{
			if(wheel != null)
			{
				wheel.prevPosY = wheel.posY;
				wheel.move(MoverType.SELF, motionX, (onDeck) ? 0 : motionY, motionZ);
			}
		}

		correctWheelPos();
		
		//Update wheels
		for(int i = 0; i < 2; i++)
		{
			Vector3f amountToMoveCar = new Vector3f(motionX / 2F, (onDeck) ? 0 : motionY / 2F, motionZ / 2F);
			
			for(EntityWheel wheel : wheels)
			{
				if(wheel == null)
					continue;
				
				//Hacky way of forcing the car to step up blocks
				onGround = true;
				wheel.onGround = true;
				
				//Update angles
				wheel.rotationYaw = axes.getYaw();
				
				//Pull wheels towards car
				/*Vector3f targetWheelPos = axes.findLocalVectorGlobally(
						getPlaneType().wheelPositions[wheel.getExpectedWheelID()].position);*/
				Vector3f wPos = getPlaneType().wheelPositions[wheel.getExpectedWheelID()].position;
				if (type.valkyrie && varWing) wPos = new Vector3f(wPos.x, wPos.y + 90 / 16F, wPos.z);
				Vector3f targetWheelPos = axes.findLocalVectorGlobally(wPos);

				Vector3f currentWheelPos = new Vector3f(wheel.posX - posX, wheel.posY - posY, wheel.posZ - posZ);
				
				float targetWheelLength = targetWheelPos.length();
				float currentWheelLength = currentWheelPos.length();
				
				if(currentWheelLength > targetWheelLength * 3.0d)
				{
					// Make wheels break?
					//this.attackPart(EnumDriveablePart.backLeftWheel, source, damage);
				}
				
				float dLength = targetWheelLength - currentWheelLength;
				float dAngle = Vector3f.angle(targetWheelPos, currentWheelPos);
				
				{
					//Now Lerp by wheelSpringStrength and work out the new positions		
					float newLength = currentWheelLength + dLength * type.wheelSpringStrength;
					Vector3f rotateAround = Vector3f.cross(targetWheelPos, currentWheelPos, null);
					
					rotateAround.normalise();
					
					Matrix4f mat = new Matrix4f();
					mat.m00 = currentWheelPos.x;
					mat.m10 = currentWheelPos.y;
					mat.m20 = currentWheelPos.z;
					mat.rotate(dAngle * type.wheelSpringStrength, rotateAround);

					if (this.ticksExisted > 5) {
						if (!(type.valkyrie && anim.timeSinceSwitch < 10))
							axes.rotateGlobal(-dAngle * type.wheelSpringStrength, rotateAround);
					}
					
					Vector3f newWheelPos = new Vector3f(mat.m00, mat.m10, mat.m20);
					newWheelPos.normalise().scale(newLength);
					
					//The proportion of the spring adjustment that is applied to the wheel. 1 - this is applied to the plane
					float wheelProportion = 0.75F;
					
					//wheel.motionX = (newWheelPos.x - currentWheelPos.x) * wheelProportion;
					//wheel.motionY = (newWheelPos.y - currentWheelPos.y) * wheelProportion;
					//wheel.motionZ = (newWheelPos.z - currentWheelPos.z) * wheelProportion;
					
					Vector3f amountToMoveWheel = new Vector3f();
					
					amountToMoveWheel.x = (newWheelPos.x - currentWheelPos.x) * (1F - wheelProportion);
					amountToMoveWheel.y = (newWheelPos.y - currentWheelPos.y) * (1F - wheelProportion);
					amountToMoveWheel.z = (newWheelPos.z - currentWheelPos.z) * (1F - wheelProportion);
					
					amountToMoveCar.x -= (newWheelPos.x - currentWheelPos.x) * (1F - wheelProportion);
					amountToMoveCar.y -= (newWheelPos.y - currentWheelPos.y) * (1F - wheelProportion);
					amountToMoveCar.z -= (newWheelPos.z - currentWheelPos.z) * (1F - wheelProportion);
					
					//The difference between how much the wheel moved and how much it was meant to move. i.e. the reaction force from the block
					//amountToMoveCar.x += ((wheel.posX - wheel.prevPosX) - (motionX)) * 0.616F / wheels.length;
					amountToMoveCar.y += ((wheel.posY - wheel.prevPosY) - ((onDeck) ? 0 : motionY)) * 0.5F / wheels.length;
					//amountToMoveCar.z += ((wheel.posZ - wheel.prevPosZ) - (motionZ)) * 0.0616F / wheels.length;
					
					if(amountToMoveWheel.lengthSquared() >= 32f * 32f)
					{
						FlansMod.log.warn("Wheel tried to move " + amountToMoveWheel.length() + " in a single frame, capping at 32 blocks");
						amountToMoveWheel.normalise();
						amountToMoveWheel.scale(32f);
					}
					
					wheel.move(MoverType.SELF, amountToMoveWheel.x, amountToMoveWheel.y, amountToMoveWheel.z);
				}
			}
			
			move(MoverType.SELF, amountToMoveCar.x, amountToMoveCar.y, amountToMoveCar.z);
			
		}

		if (this.getControllingPassenger() != null) {
			if (this.getControllingPassenger().getClass().toString().indexOf("mcheli.aircraft.MCH_EntitySeat") > 0) {
				axes.setAngles(this.getControllingPassenger().rotationYaw + 90, 0, 0);
			}
		}


		checkForCollisions();
		
		//Sounds
		//Starting sound
		if(throttle > 0.01F && throttle < 0.2F && soundPosition == 0 && hasEnoughFuel())
		{
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, FlansMod.soundRange, dimension, type.startSound, false);
			soundPosition = type.startSoundLength;
		}
		//Flying sound
		if(throttle > 0.2F && soundPosition == 0 /*&& hasEnoughFuel()*/)
		{
			PacketPlaySound.sendSoundPacket(posX, posY, posZ, FlansMod.soundRange, dimension, type.engineSound, false);
			soundPosition = type.engineSoundLength;
		}
		
		//Sound decrementer
		if(soundPosition > 0)
			soundPosition--;
		
		for(EntitySeat seat : getSeats())
		{
			if(seat != null)
				seat.updatePosition();
		}
		
		//Calculate movement on the client and then send position, rotation etc to the server
		if(serverPosX != posX || serverPosY != posY || serverPosZ != posZ || serverYaw != axes.getYaw())
		{
			if(thePlayerIsDrivingThis)
			{
				FlansMod.getPacketHandler().sendToServer(new PacketPlaneControl(this));
				FlansMod.getPacketHandler().sendToServer(new PacketPlaneAnimator(this));
				serverPosX = posX;
				serverPosY = posY;
				serverPosZ = posZ;
				serverYaw = axes.getYaw();
			}
		}
		
		PostUpdate();
	}
	
	public boolean canThrust()
	{
		return (getSeat(0) != null && getSeat(0).getControllingPassenger() instanceof EntityPlayer
				&& ((EntityPlayer)getSeat(0).getControllingPassenger()).capabilities.isCreativeMode) ||
				driveableData.fuelInTank > 0;
	}
	
	@Override
	public void setDead()
	{
		super.setDead();
	}
	
	@Override
	public boolean gearDown()
	{
		return varGear;
	}
	
	private boolean hasWorkingProp()
	{
		PlaneType type = getPlaneType();
		if(type.mode == EnumPlaneMode.HELI || type.mode == EnumPlaneMode.VTOL)
			for(Propeller prop : type.heliPropellers)
				if(isPartIntact(prop.planePart))
					return true;
		if(type.mode == EnumPlaneMode.PLANE || type.mode == EnumPlaneMode.VTOL)
			for(Propeller prop : type.propellers)
				if(isPartIntact(prop.planePart))
					return true;
		return false;
	}
	
	public boolean attackEntityFrom(DamageSource damagesource, float i, boolean doDamage) {
		if (world.isRemote || isDead)
			return true;
		if (!TeamsManager.vehiclepin) {
			Entity trueSource = damagesource.getTrueSource();
			if (!(trueSource instanceof EntityPlayer))
				return super.attackEntityFrom(damagesource, i);

			EntityPlayer player = (EntityPlayer) trueSource;
			if (!player.onGround || (getSeat(0) != null && getSeat(0).getControllingPassenger() != null))
				return super.attackEntityFrom(damagesource, i);
			ItemStack vehicleStack = new ItemStack(getVehicleType().item, 1, driveableData.paintjobID);
			NBTTagCompound tags = new NBTTagCompound();
			vehicleStack.setTagCompound(tags);
			driveableData.writeToNBT(tags);

			DriveableDeathByHandEvent driveableDeathByHandEvent = new DriveableDeathByHandEvent(this, player, vehicleStack);
			MinecraftForge.EVENT_BUS.post(driveableDeathByHandEvent);
			if (!driveableDeathByHandEvent.isCanceled()) {
				if (!world.isRemote) {
					if (hasEmptyInventorySlot(player)) {
						player.inventory.addItemStackToInventory(vehicleStack);
					} else {
						player.sendMessage(new TextComponentString("\u00a78\u00bb \u00a7cYour inventory is full, item dropped on ground."));
						entityDropItem(vehicleStack, 0.5F);
					}
					setDead();
				}
			}

		} else {
			Entity trueSource = damagesource.getTrueSource();
			if (!(trueSource instanceof EntityPlayer))
				return super.attackEntityFrom(damagesource, i);

			EntityPlayer player = (EntityPlayer) trueSource;
			if (!player.onGround || (getSeat(0) != null && getSeat(0).getControllingPassenger() != null))
				return super.attackEntityFrom(damagesource, i);

			String playerName = player.getDisplayName().toString();
			String[] playerNameParts = playerName.split("'");
			String playerNameClean = playerNameParts[3];

			NBTTagCompound tag = getEntityData();
			String ownerName = tag.getString("Owner");

			if (!ownerName.equals(playerNameClean) && (!VehicleOwnerManager.vehicleOwners.containsKey(ownerName) ||
					!VehicleOwnerManager.vehicleOwners.get(ownerName).contains(playerNameClean))) {
				player.sendMessage(new TextComponentString("\u00a78\u00bb \u00a7cYou can't break this vehicle."));
				return super.attackEntityFrom(damagesource, i);
			}

			ItemStack vehicleStack = new ItemStack(getVehicleType().item, 1, driveableData.paintjobID);
			NBTTagCompound tags = new NBTTagCompound();
			vehicleStack.setTagCompound(tags);
			driveableData.writeToNBT(tags);

			DriveableDeathByHandEvent driveableDeathByHandEvent = new DriveableDeathByHandEvent(this, player, vehicleStack);
			MinecraftForge.EVENT_BUS.post(driveableDeathByHandEvent);
			if (!driveableDeathByHandEvent.isCanceled()) {
				if (!world.isRemote) {
					if (hasEmptyInventorySlot(player)) {
						player.inventory.addItemStackToInventory(vehicleStack);
					} else {
						player.sendMessage(new TextComponentString("\u00a78\u00bb \u00a7cYour inventory is full, item dropped on ground."));
						entityDropItem(vehicleStack, 0.5F);
					}
					setDead();
				}
			}
		}
		return true;
	}

	public VehicleType getVehicleType()
	{
		return VehicleType.getVehicle(driveableType);
	}
	public boolean hasEmptyInventorySlot(EntityPlayer player) {
		Integer size = player.inventory.getSizeInventory();
		for (int i = 0; i < player.inventory.getSizeInventory() - 5; i++) {
			ItemStack itemStack = player.inventory.getStackInSlot(i);
			if (itemStack.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean canHitPart(EnumDriveablePart part)
	{
		return varGear || (part != EnumDriveablePart.coreWheel && part != EnumDriveablePart.leftWingWheel &&
				part != EnumDriveablePart.rightWingWheel && part != EnumDriveablePart.tailWheel);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i)
	{
		return attackEntityFrom(damagesource, i, true);
	}
	
	public PlaneType getPlaneType()
	{
		return PlaneType.getPlane(driveableType);
	}
	
	@Override
	protected void dropItemsOnPartDeath(Vector3f midpoint, DriveablePart part)
	{
	}

	public Vector3f transformPart(Vector3f current, Vector3f target, Vector3f rate){
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

		return current;
	}
	
	@Override
	public String getBombInventoryName()
	{
		return "Bombs";
	}
	
	@Override
	public String getMissileInventoryName()
	{
		return "Missiles";
	}
	
	@Override
	public boolean hasMouseControlMode()
	{
		return true;
	}
}
