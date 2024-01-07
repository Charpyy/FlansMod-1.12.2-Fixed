package com.flansmod.common.driveables;

import java.util.ArrayList;
import java.util.HashMap;

import com.flansmod.common.util.Parser;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.flansmod.client.model.ModelPlane;
import com.flansmod.common.FlansMod;
import com.flansmod.common.parts.PartType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.types.InfoType.ParseFunc;
import com.flansmod.common.vector.Vector3f;

public class PlaneType extends DriveableType
{
	/**
	 * What type of flying vehicle is this?
	 */
	public EnumPlaneMode mode = EnumPlaneMode.PLANE;
	/**
	 * Pitch modifiers
	 */
	public float lookDownModifier = 1F, lookUpModifier = 1F;
	/**
	 * Roll modifiers
	 */
	public float rollLeftModifier = 1F, rollRightModifier = 1F;
	/**
	 * Yaw modifiers
	 */
	public float turnLeftModifier = 1F, turnRightModifier = 1F;

	//vehicle settings
	public boolean spinWithoutTail = false;
	public boolean heliThrottlePull = true;
	//Does this use the new flight controller?
	public boolean newFlightControl = false;

	/**
	 * Co-efficient of lift which determines how the plane flies
	 */
	public float lift = 1F;

	public float takeoffSpeed = 0.5F;
	public float maxSpeed = 2.0F;
	public boolean supersonic = false;
	public float wingArea = 1F;
	//Max thrust in 10s of kgf
	public float maxThrust = 50.0F;
	//mass in kg
	public float mass = 1000.0F;
	public float emptyDrag = 1F;
	
	/**
	 * The point at which bomb entities spawn
	 */
	public Vector3f bombPosition;
	/**
	 * The time in ticks between bullets fired by the nose / wing guns
	 */
	public int planeShootDelay;
	/**
	 * The time in ticks between bombs dropped
	 */
	public int planeBombDelay;
	
	/**
	 * The positions, parent parts and recipe items of the propellers, used to calculate forces and render the plane correctly
	 */
	public ArrayList<Propeller> propellers = new ArrayList<>();
	/**
	 * The positions, parent parts and recipe items of the helicopter propellers, used to calculate forces and render the plane correctly
	 */
	public ArrayList<Propeller> heliPropellers = new ArrayList<>(), heliTailPropellers = new ArrayList<>();
	
	/**
	 * Aesthetic features
	 */
	public boolean hasGear = false, hasDoor = false, hasWing = false;
	/**
	 * Default pitch for when parked. Will implement better system soon
	 */
	public float restingPitch = 0F;
	
	/**
	 * Whether the player can access the inventory while in the air
	 */
	public boolean invInflight = true;
	
	public static ArrayList<PlaneType> types = new ArrayList<>();

	//Aesthetic features
	//Wing Animations
	public Vector3f wingPos1 = new Vector3f(0,0,0);
	public Vector3f wingPos2 = new Vector3f(0,0,0);
	public Vector3f wingRot1 = new Vector3f(0,0,0);
	public Vector3f wingRot2 = new Vector3f(0,0,0);
	public Vector3f wingRate = new Vector3f(0,0,0);
	public Vector3f wingRotRate = new Vector3f(0,0,0);
	//Wing Wheel Animations
	public Vector3f wingWheelPos1 = new Vector3f(0,0,0);
	public Vector3f wingWheelPos2 = new Vector3f(0,0,0);
	public Vector3f wingWheelRot1 = new Vector3f(0,0,0);
	public Vector3f wingWheelRot2 = new Vector3f(0,0,0);
	public Vector3f wingWheelRate = new Vector3f(0,0,0);
	public Vector3f wingWheelRotRate = new Vector3f(0,0,0);
	//Body Wheel Animations
	public Vector3f bodyWheelPos1 = new Vector3f(0,0,0);
	public Vector3f bodyWheelPos2 = new Vector3f(0,0,0);
	public Vector3f bodyWheelRot1 = new Vector3f(0,0,0);
	public Vector3f bodyWheelRot2 = new Vector3f(0,0,0);
	public Vector3f bodyWheelRate = new Vector3f(0,0,0);
	public Vector3f bodyWheelRotRate = new Vector3f(0,0,0);
	//Tail Wheel Animations
	public Vector3f tailWheelPos1 = new Vector3f(0,0,0);
	public Vector3f tailWheelPos2 = new Vector3f(0,0,0);
	public Vector3f tailWheelRot1 = new Vector3f(0,0,0);
	public Vector3f tailWheelRot2 = new Vector3f(0,0,0);
	public Vector3f tailWheelRate = new Vector3f(0,0,0);
	public Vector3f tailWheelRotRate = new Vector3f(0,0,0);
	//Door animations
	public Vector3f doorPos1 = new Vector3f(0,0,0);
	public Vector3f doorPos2 = new Vector3f(0,0,0);
	public Vector3f doorRot1 = new Vector3f(0,0,0);
	public Vector3f doorRot2 = new Vector3f(0,0,0);
	public Vector3f doorRate = new Vector3f(0,0,0);
	public Vector3f doorRotRate = new Vector3f(0,0,0);

	//Do wings fold when Gear are deployed?
	public boolean foldWingForLand = false;
	//Can it fly with oor open?
	public boolean flyWithOpenDoor = false;
	//Do these automatically deploy when near ground?
	public boolean autoOpenDoorsNearGround = true;
	public boolean autoDeployLandingGearNearGround = true;
	//Is this a valkyrie? "very specific"
	public boolean valkyrie = false;
	
	private static HashMap<String, ParseFunc<PlaneType>> parsers = new HashMap<>();
	static
	{
		// Plane / Heli Mode
		parsers.put("Mode", (split, d) -> d.mode = EnumPlaneMode.getMode(split[1]));
		//Better flight model?
		parsers.put("NewFlightControl", (split, d) -> d.newFlightControl = Boolean.parseBoolean(split[1]));
		// Yaw modifiers
		parsers.put("TurnLeftSpeed", (split, d) -> d.turnLeftModifier = Parser.parseFloat(split[1]));
		parsers.put("TurnRightSpeed", (split, d) -> d.turnRightModifier = Parser.parseFloat(split[1]));
		// Pitch modifiers
		parsers.put("LookUpSpeed", (split, d) -> d.lookUpModifier = Parser.parseFloat(split[1]));
		parsers.put("LookDownSpeed", (split, d) -> d.lookDownModifier = Parser.parseFloat(split[1]));
		// Roll modifiers
		parsers.put("RollLeftSpeed", (split, d) -> d.rollLeftModifier = Parser.parseFloat(split[1]));
		parsers.put("RollRightSpeed", (split, d) -> d.rollRightModifier = Parser.parseFloat(split[1]));
		
		// Lift
		parsers.put("Lift", (split, d) -> d.lift = Parser.parseFloat(split[1]));
		// Armaments
		parsers.put("ShootDelay", (split, d) -> d.planeShootDelay = Parser.parseInt(split[1]));
		parsers.put("BombDelay", (split, d) -> d.planeBombDelay = Integer.parseInt(split[1]));
		parsers.put("TakeoffSpeed", (split, d) -> d.takeoffSpeed = Parser.parseFloat(split[1]));
		parsers.put("MaxSpeed", (split, d) -> d.maxSpeed = Parser.parseFloat(split[1]));
		parsers.put("Supersonic", (split, d) -> d.supersonic = Boolean.parseBoolean(split[1]));
		parsers.put("MaxThrust", (split, d) -> d.maxThrust = Float.parseFloat(split[1]));
		parsers.put("Mass", (split, d) -> d.mass = Float.parseFloat(split[1]));
		parsers.put("WingArea", (split, d) -> d.wingArea = Float.parseFloat(split[1]));
		parsers.put("HeliThrottlePull", (split, d) -> d.heliThrottlePull = Boolean.parseBoolean(split[1]));
		parsers.put("EmptyDrag", (split, d) -> d.emptyDrag = Float.parseFloat(split[1]));
		
		// Propellers
		parsers.put("Propeller", (split, d) -> 
		{
			Propeller propeller = new Propeller(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), EnumDriveablePart.getPart(split[5]), PartType.getPart(split[6]));
			d.propellers.add(propeller);
			d.driveableRecipe.add(new ItemStack(propeller.itemType.item));
		});
		parsers.put("HeliPropeller", (split, d) -> 
		{
			Propeller propeller = new Propeller(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), EnumDriveablePart.getPart(split[5]), PartType.getPart(split[6]));
			d.heliPropellers.add(propeller);
			d.driveableRecipe.add(new ItemStack(propeller.itemType.item));
		});
		parsers.put("HeliTailPropeller", (split, d) -> 
		{
			Propeller propeller = new Propeller(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]), EnumDriveablePart.getPart(split[5]), PartType.getPart(split[6]));
			d.heliTailPropellers.add(propeller);
			d.driveableRecipe.add(new ItemStack(propeller.itemType.item));
		});


		parsers.put("HasFlare", (split, d) -> d.hasFlare = Boolean.parseBoolean(split[1]));
		parsers.put("FlareDelay", (split, d) ->
		{
			d.flareDelay = Integer.parseInt(split[1]);
			if(d.flareDelay<=0)
				d.flareDelay = 1;
		});
		parsers.put("TimeFlareUsing", (split, d) ->
		{
			d.timeFlareUsing = Integer.parseInt(split[1]);
			if(d.timeFlareUsing<=0)
				d.timeFlareUsing = 1;
		});
		
		// Sound
		parsers.put("PropSoundLength", (split, d) -> d.engineSoundLength = Integer.parseInt(split[1]));
		parsers.put("PropSound", (split, d) -> 
		{
			d.engineSound = split[1];
			FlansMod.proxy.loadSound(d.contentPack, "driveables", split[1]);
		});
		parsers.put("ShootSound", (split, d) -> 
		{
			d.shootSoundPrimary = split[1];
			FlansMod.proxy.loadSound(d.contentPack, "driveables", split[1]);
		});
		parsers.put("BombSound", (split, d) -> 
		{
			d.shootSoundSecondary = split[1];
			FlansMod.proxy.loadSound(d.contentPack, "driveables", split[1]);
		});
		
		// Aesthetics
		parsers.put("HasGear", (split, d) -> d.hasGear = Boolean.parseBoolean(split[1]));
		parsers.put("HasDoor", (split, d) -> d.hasDoor = Boolean.parseBoolean(split[1]));
		parsers.put("HasWing", (split, d) -> d.hasWing = Boolean.parseBoolean(split[1]));
		parsers.put("RestingPitch", (split, d) -> d.restingPitch = Float.parseFloat(split[1]));
		parsers.put("InflightInventory", (split, d) -> d.invInflight = Boolean.parseBoolean(split[1]));

		parsers.put("FoldWingForLand", (split, d) -> d.foldWingForLand = Boolean.parseBoolean(split[1]));
		parsers.put("FlyWithOpenDoor", (split, d) -> d.flyWithOpenDoor = Boolean.parseBoolean(split[1]));
		parsers.put("AutoOpenDoorsNearGround", (split, d) -> d.autoOpenDoorsNearGround = Boolean.parseBoolean(split[1]));
		parsers.put("AutoDeployLandingGearNearGround", (split, d) -> d.autoDeployLandingGearNearGround = Boolean.parseBoolean(split[1]));
		parsers.put("SpinWithoutTail", (split, d) -> d.spinWithoutTail = Boolean.parseBoolean(split[1]));
		parsers.put("Valkyrie", (split, d) -> d.valkyrie = Boolean.parseBoolean(split[1]));

		//Animations
		//Wings
		parsers.put("WingPosition1", (split, d) -> d.wingPos1 = new Vector3f(split[1], d.shortName));
		parsers.put("WingPosition2", (split, d) -> d.wingPos2 = new Vector3f(split[1], d.shortName));
		parsers.put("WingRotation1", (split, d) -> d.wingRot1 = new Vector3f(split[1], d.shortName));
		parsers.put("WingRotation2", (split, d) -> d.wingRot2 = new Vector3f(split[1], d.shortName));
		parsers.put("WingRate", (split, d) -> d.wingRate = new Vector3f(split[1], d.shortName));
		parsers.put("WingRotRate", (split, d) -> d.wingRotRate = new Vector3f(split[1], d.shortName));

		//Wing Wheels
		parsers.put("WingWheelPosition1", (split, d) -> d.wingWheelPos1 = new Vector3f(split[1], d.shortName));
		parsers.put("WingWheelPosition2", (split, d) -> d.wingWheelPos2 = new Vector3f(split[1], d.shortName));
		parsers.put("WingWheelRotation1", (split, d) -> d.wingWheelRot1 = new Vector3f(split[1], d.shortName));
		parsers.put("WingWheelRotation2", (split, d) -> d.wingWheelRot2 = new Vector3f(split[1], d.shortName));
		parsers.put("WingWheelRate", (split, d) -> d.wingWheelRate = new Vector3f(split[1], d.shortName));
		parsers.put("WingWheelRotRate", (split, d) -> d.wingWheelRotRate = new Vector3f(split[1], d.shortName));

		//Body Wheels
		parsers.put("BodyWheelPosition1", (split, d) -> d.bodyWheelPos1 = new Vector3f(split[1], d.shortName));
		parsers.put("BodyWheelPosition2", (split, d) -> d.bodyWheelPos2 = new Vector3f(split[1], d.shortName));
		parsers.put("BodyWheelRotation1", (split, d) -> d.bodyWheelRot1 = new Vector3f(split[1], d.shortName));
		parsers.put("BodyWheelRotation2", (split, d) -> d.bodyWheelRot2 = new Vector3f(split[1], d.shortName));
		parsers.put("BodyWheelRate", (split, d) -> d.bodyWheelRate = new Vector3f(split[1], d.shortName));
		parsers.put("BodyWheelRotRate", (split, d) -> d.bodyWheelRotRate = new Vector3f(split[1], d.shortName));

		//Tail Wheels
		parsers.put("TailWheelPosition1", (split, d) -> d.tailWheelPos1 = new Vector3f(split[1], d.shortName));
		parsers.put("TailWheelPosition2", (split, d) -> d.tailWheelPos2 = new Vector3f(split[1], d.shortName));
		parsers.put("TailWheelRotation1", (split, d) -> d.tailWheelRot1 = new Vector3f(split[1], d.shortName));
		parsers.put("TailWheelRotation2", (split, d) -> d.tailWheelRot2 = new Vector3f(split[1], d.shortName));
		parsers.put("TailWheelRate", (split, d) -> d.tailWheelRate = new Vector3f(split[1], d.shortName));
		parsers.put("TailWheelRotRate", (split, d) -> d.tailWheelRotRate = new Vector3f(split[1], d.shortName));

		parsers.put("DoorPosition1", (split, d) -> d.doorPos1 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorPosition2", (split, d) -> d.doorPos2 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRotation1", (split, d) -> d.doorRot1 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRotation2", (split, d) -> d.doorRot2 = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRate", (split, d) -> d.doorRate = new Vector3f(split[1], d.shortName));
		parsers.put("DoorRotRate", (split, d) -> d.doorRotRate = new Vector3f(split[1], d.shortName));
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

	public PlaneType(TypeFile file)
	{
		super(file);
		types.add(this);
	}

	@Override
	public void preRead(TypeFile file)
	{
		super.preRead(file);
	}

	@Override
	public int numEngines()
	{
		switch(mode)
		{
			case VTOL: return Math.max(propellers.size(), heliPropellers.size());
			case PLANE: return propellers.size();
			case HELI: return heliPropellers.size();
			default: return 1;
		}
	}

	/**
	 * Find the items needed to rebuild a part. The returned array is disconnected from the template items it has looked up
	 */
	@Override
	public ArrayList<ItemStack> getItemsRequired(DriveablePart part, PartType engine)
	{
		//Get the list of items required by the driveable
		ArrayList<ItemStack> stacks = super.getItemsRequired(part, engine);
		//Add the propellers and engines
		for(Propeller propeller : propellers)
		{
			if(propeller.planePart == part.type)
			{
				if (propeller.itemType != null) {
					stacks.add(new ItemStack(propeller.itemType.item));
				} else {
					FlansMod.log.error("Couldn't drop propeller!");
				}
				if (engine.item != null) {
					stacks.add(new ItemStack(engine.item));
				} else {
					FlansMod.log.error("Couldn't drop engine!");
				}
			}
		}
		return stacks;
	}

	public static PlaneType getPlane(String find)
	{
		for(PlaneType type : types)
		{
			if(type.shortName.equals(find))
				return type;
		}
		return null;
	}

	/**
	 * To be overriden by subtypes for model reloading
	 */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelPlane.class, fileName, packName);
	}
	
	@Override
	public EntityDriveable createDriveable(World world, double x, double y, double z, DriveableData data)
	{
		return new EntityPlane(world, x, y, z, this, data, null);
	}
}
