package com.flansmod.common.driveables;

import java.util.ArrayList;
import java.util.HashMap;

import com.flansmod.common.util.Parser;
import com.flansmod.common.vector.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.flansmod.client.model.ModelVehicle;
import com.flansmod.common.FlansMod;
import com.flansmod.common.parts.PartType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.types.InfoType.ParseFunc;

public class VehicleType extends DriveableType
{
	/**
	 * Movement modifiers
	 */
	public float turnLeftModifier = 1F, turnRightModifier = 1F;
	/**
	 * If true, this will crush any living entity under the wheels
	 */
	public boolean squashMobs = false;
	/**
	 * If this is true, the vehicle will drive from all wheels
	 */
	public boolean fourWheelDrive = false;
	/**
	 * If true, then wheels will rotate as the vehicle drives
	 */
	public boolean rotateWheels = false;
	/**
	 * Tank movement system. Uses track collision box for thrust, rather than the wheels
	 */
	public boolean tank = false;

	/**
	 * Amount to decrease throttle by each tick.
	 */
	public float throttleDecay = 0.0035F;

	/**
	 * Aesthetic door variable
	 */
	public boolean hasDoor = false;
	
	public int trackLinkFix = 5;
	public boolean flipLinkFix = false;

	/**
	 * Mass of the vehicle, for use in realistic acceleration calculation
	 */
	public float mass = 1000F;

	public boolean useRealisticAcceleration = false;

	// Braking modifier.
	public float brakingModifier = 1;

	public float maxFallSpeed = 0.85F;
	public float gravity = 0.175F;

	//Door animations
	public Vector3f doorPos1 = new Vector3f(0, 0, 0);
	public Vector3f doorPos2 = new Vector3f(0, 0, 0);
	public Vector3f doorRot1 = new Vector3f(0, 0, 0);
	public Vector3f doorRot2 = new Vector3f(0, 0, 0);
	public Vector3f doorRate = new Vector3f(0, 0, 0);
	public Vector3f doorRotRate = new Vector3f(0, 0, 0);
	public Vector3f door2Pos1 = new Vector3f(0, 0, 0);
	public Vector3f door2Pos2 = new Vector3f(0, 0, 0);
	public Vector3f door2Rot1 = new Vector3f(0, 0, 0);
	public Vector3f door2Rot2 = new Vector3f(0, 0, 0);
	public Vector3f door2Rate = new Vector3f(0, 0, 0);
	public Vector3f door2RotRate = new Vector3f(0, 0, 0);
	public boolean shootWithOpenDoor = false;

	public String driftSound = "";
	public int driftSoundLength;

	public ArrayList<SmokePoint> smokers = new ArrayList<SmokePoint>();
	
	public static ArrayList<VehicleType> types = new ArrayList<>();
	private static HashMap<String, ParseFunc<VehicleType>> parsers = new HashMap<>();
	
	static
	{
		parsers.put("SquashMobs", (split, d) -> d.squashMobs = Boolean.parseBoolean(split[1]));
		parsers.put("FourWheelDrive", (split, d) -> d.fourWheelDrive = Boolean.parseBoolean(split[1]));
		parsers.put("Tank", (split, d) -> d.tank = Boolean.parseBoolean(split[1]));
		parsers.put("TankMode", (split, d) -> d.tank = Boolean.parseBoolean(split[1]));
		parsers.put("HasDoor", (split, d) -> d.hasDoor = Boolean.parseBoolean(split[1]));
		parsers.put("RotateWheels", (split, d) -> d.rotateWheels = Boolean.parseBoolean(split[1]));
		parsers.put("ThrottleDecay", (split, d) -> d.throttleDecay = Parser.parseFloat(split[1]));
		parsers.put("Mass", (split, d) -> d.mass = Parser.parseFloat(split[1]));
		parsers.put("UseRealisticAcceleration", (split, d) -> d.useRealisticAcceleration = Boolean.parseBoolean(split[1]));
		parsers.put("Gravity", (split, d) -> d.gravity = Parser.parseFloat(split[1]));
		parsers.put("MaxFallSpeed", (split, d) -> d.maxFallSpeed = Parser.parseFloat(split[1]));
		parsers.put("BrakingModifier", (split, d) -> d.brakingModifier = Parser.parseFloat(split[1]));
		parsers.put("ShootWithOpenDoor", (split, d) -> d.shootWithOpenDoor = Boolean.parseBoolean(split[1]));
		parsers.put("FixTrackLink", (split, d) -> d.trackLinkFix = Parser.parseInt(split[1]));
		parsers.put("FlipLinkFix", (split, d) -> d.flipLinkFix = Boolean.parseBoolean(split[1]));
			
		parsers.put("TurnLeftSpeed", (split, d) -> d.turnLeftModifier = Parser.parseFloat(split[1]));
		parsers.put("TurnRightSpeed", (split, d) -> d.turnRightModifier = Parser.parseFloat(split[1]));
		parsers.put("ShootDelay", (split, d) -> d.shootDelaySecondary = Parser.parseInt(split[1]));
		parsers.put("ShellDelay", (split, d) -> d.shootDelayPrimary = Parser.parseInt(split[1]));
		
		parsers.put("ShellSound", (split, d) -> 
		{
			d.shootSoundSecondary = split[1];
			FlansMod.proxy.loadSound(d.contentPack, "driveables", split[1]);
		});
		parsers.put("ShootSound", (split, d) -> 
		{
			d.shootSoundPrimary = split[1];
			FlansMod.proxy.loadSound(d.contentPack, "driveables", split[1]);
		});

		//Animations
		parsers.put("DoorPosition1", (split, d) -> d.doorPos1 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorPosition2", (split, d) -> d.doorPos2 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRotation1", (split, d) -> d.doorRot1 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRotation2", (split, d) -> d.doorRot2 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRate", (split, d) -> d.doorRate = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRotRate", (split, d) -> d.doorRotRate = new Vector3f(split[1], d.shortName));

		parsers.put("Door2Position1", (split, d) -> d.door2Pos1 = new Vector3f(split[1], d.shortName));
		parsers.put("Door2Position2", (split, d) -> d.door2Pos2 = new Vector3f(split[1], d.shortName));
		parsers.put("Door2Rotation1", (split, d) -> d.door2Rot1 = new Vector3f(split[1], d.shortName));
		parsers.put("Door2Rotation2", (split, d) -> d.door2Rot2 = new Vector3f(split[1], d.shortName));
		parsers.put("Door2Rate", (split, d) -> d.door2Rate = new Vector3f(split[1], d.shortName));
		parsers.put("Door2RotRate", (split, d) -> d.door2RotRate = new Vector3f(split[1], d.shortName));

		parsers.put("DriftSoundLength", (split, d) -> d.driftSoundLength = Parser.parseInt(split[1]));
		parsers.put("DriftSound", (split, d) -> {
				d.driftSound = split[1];
				FlansMod.proxy.loadSound(d.contentPack, "driveables", split[1]);
		});
		parsers.put("AddSmokePoint", (split, d) -> {
			SmokePoint smoke = new SmokePoint();
			smoke.position = new Vector3f(split[1], d.shortName);
			smoke.direction = new Vector3f(split[2], d.shortName);
			smoke.detTime = Parser.parseInt(split[3]);
			smoke.part = split[4];
			d.smokers.add(smoke);
		});
		parsers.put("AddSmokeDispenser", (split, d) -> {
			SmokePoint smoke = new SmokePoint();
			smoke.position = new Vector3f(split[1], d.shortName);
			smoke.direction = new Vector3f(split[2], d.shortName);
			smoke.detTime = Parser.parseInt(split[3]);
			smoke.part = split[4];
			d.smokers.add(smoke);
		});
	}
	
	public VehicleType(TypeFile file)
	{
		super(file);
		types.add(this);
	}
	
	@Override
	public void preRead(TypeFile file)
	{
		super.preRead(file);
		wheelPositions = new DriveablePosition[4];
	}
	
	@Override
	protected void read(String[] split, TypeFile file)
	{
		try
		{			
			ParseFunc parser = parsers.get(split[0]);
			if(parser != null)
			{
				parser.Parse(split, this);
			}
			else
			{
				super.read(split, file);
			}
		}
		catch(Exception ignored)
		{
		}
	}
	
	/**
	 * Find the items needed to rebuild a part. The returned array is disconnected from the template items it has looked
	 * up
	 */
	@Override
	public ArrayList<ItemStack> getItemsRequired(DriveablePart part, PartType engine)
	{
		//Get the list of items required by the driveable
		ArrayList<ItemStack> stacks = super.getItemsRequired(part, engine);
		//Add the propellers and engines
		if(EnumDriveablePart.core == part.type)
		{
			stacks.add(new ItemStack(engine.item));
		}
		return stacks;
	}
	
	public static VehicleType getVehicle(String find)
	{
		for(VehicleType type : types)
		{
			if(type.shortName.equals(find))
				return type;
		}
		return null;
	}

	public static class SmokePoint {
		public Vector3f position;
		public Vector3f direction;
		public int detTime;
		public String part;
	}


	/**
	 * To be overriden by subtypes for model reloading
	 */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelVehicle.class, fileName, packName);
	}
	
	@Override
	public EntityDriveable createDriveable(World world, double x, double y, double z, DriveableData data)
	{
		return new EntityVehicle(world, x, y, z, this, data);
	}
}
