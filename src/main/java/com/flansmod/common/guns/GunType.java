package com.flansmod.common.guns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.client.model.ModelBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.flansmod.client.model.EnumAnimationType;
import com.flansmod.client.model.EnumMeleeAnimation;
import com.flansmod.client.model.ModelCasing;
import com.flansmod.client.model.ModelDefaultMuzzleFlash;
import com.flansmod.client.model.ModelFlash;
import com.flansmod.client.model.ModelGun;
import com.flansmod.client.model.ModelMG;
import com.flansmod.client.model.ModelMuzzleFlash;
import com.flansmod.common.FlansMod;
import com.flansmod.common.paintjob.PaintableType;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.vector.Vector3f;

public class GunType extends PaintableType implements IScope
{
	public static final Random rand = new Random();
	
	// Gun Behaviour Variables
	
	//Recoil Variables
	/**
	 * The amount to recoil the player's view by when firing a single shot from this gun
	 */
	//public int recoil = 0;
    /**
     * Base value for Upwards cursor/view recoil
     */
	public float recoilPitch = 0.0F;
    /**
     * Base value for Left/Right cursor/view recoil
     */
    public float recoilYaw = 0.0F;
    /**
     * Modifier for setting the maximum pitch divergence when randomizing recoil (Recoil 2 + rndRecoil 0.5 == 1.5-2.5 Recoil range)
     */
    public float rndRecoilPitchRange = 0.5F;
    /**
     * Modifier for setting the maximum yaw divergence when randomizing recoil (Recoil 2 + rndRecoil 0.5 == 1.5-2.5 Recoil range)
     */
    public float rndRecoilYawRange = 0.3F;
    /**
     * Modifier for decreasing the final pitch recoil while crouching (Recoil 2 + rndRecoil 0.5 + decreaseRecoil 0.5 == 1.0-2.0 Recoil range)
     */
    public float decreaseRecoilPitch = 0.5F;
    /**
     * Modifier for decreasing the final yaw recoil while crouching (Recoil 2 + rndRecoil 0.5 + decreaseRecoil 0.5 == 1.0-2.0 Recoil range)
     */
    //This must never be set to 0, will cause massive issues
    public float decreaseRecoilYaw = 0.5F;
    /* Countering gun recoil can be modelled with angle=n^tick where n is the coefficient here. */
    /**
     * HIGHER means less force to center, meaning it takes longer to return.
     */
    public float recoilCounterCoefficient = 0.8F;
    /**
     * The above variable but for sprinting.
     */
    public float recoilCounterCoefficientSprinting = 0.9F;
    /**
     * The above variable but for sneaking.
     */
    public float recoilCounterCoefficientSneaking = 0.7F;
    
    //Ammo & Reload Variables
	/**
	 * The list of bullet types that can be used in this gun
	 */
	public List<ShootableType> ammo = new ArrayList<>(), nonExplosiveAmmo = new ArrayList<>();
	/**
	 * Whether the player can press the reload key (default R) to reload this gun
	 */
	public boolean canForceReload = true;
	/**
     * Whether the player can receive ammo for this gun from an ammo mag
     */
    public boolean allowRearm = true;
	/**
	 * The time (in ticks) it takes to reload this gun
	 */
	public int reloadTime;
	/**
     * Number of ammo items that the gun may hold. Most guns will hold one magazine.
     * Some may hold more, such as Nerf pistols, revolvers or shotguns
     */
    public int numPrimaryAmmoItems = 1;
	
    //Projectile Mechanic Variables
	/**
	 * The amount that bullets spread out when fired from this gun
	 */
	public float bulletSpread;
	
	public EnumSpreadPattern spreadPattern = EnumSpreadPattern.cube;
	public float sneakSpreadMultiplier = 0.63F;
    public float sprintSpreadMultiplier = 1.75F;
    /**
     * If true, spread determined by loaded ammo type
     */
    public boolean allowSpreadByBullet = false;
	/**
	 * Damage inflicted by this gun. Multiplied by the bullet damage.
	 */
	public float damage = 0;
	/**
	 * The damage inflicted upon punching someone with this gun
	 */
	public float meleeDamage = 1;
	// Modifier for melee damage against specifically driveable entities.
    public float meleeDamageDriveableModifier = 1;
	/**
	 * The speed of bullets upon leaving this gun. 0.0f means instant.
	 */
	public float bulletSpeed = 0.0f;
	/**
	 * The number of bullet entities created by each shot
	 */
	public int numBullets = 1;
	/**
     * Allows you to set how many bullet entities are fired per shot via the ammo used
     */
    public boolean allowNumBulletsByBulletType = false;
	/**
	 * The delay between shots in ticks (1/20ths of seconds)
	 */
	public float shootDelay = 1.0f;
	/**
     * The fire rate of the gun in RPM, 1200 = MAX
     */
    public float roundsPerMin = 0;
	/**
	 * Number of ammo items that the gun may hold. Most guns will hold one magazine.
	 * Some may hold more, such as Nerf pistols, revolvers or shotguns
	 */
	public int numAmmoItemsInGun = 1;
	/**
	 * The firing mode of the gun. One of semi-auto, full-auto, minigun or burst
	 */
	public EnumFireMode mode = EnumFireMode.FULLAUTO;
	public EnumFireMode[] submode = new EnumFireMode[]{EnumFireMode.FULLAUTO};
    public EnumFireMode defaultmode = mode;
	/**
	 * The number of bullets to fire per burst in burst mode
	 */
	public int numBurstRounds = 3;
	/**
	 * The required speed for minigun mode guns to start firing
	 */
	public float minigunStartSpeed = 15F;
	/**
	 * The maximum speed a minigun mode gun can reach
	 */
	public float minigunMaxSpeed = 30F;
	/**
	 * Whether this gun can be used underwater
	 */
	public boolean canShootUnderwater = true;
	/**
	 * The amount of knockback to impact upon the player per shot
	 */
	public float knockback = 0F;
	/**
	 * The secondary function of this gun. By default, the left mouse button triggers this
	 */
	public EnumSecondaryFunction secondaryFunction = EnumSecondaryFunction.ADS_ZOOM;
	public EnumSecondaryFunction secondaryFunctionWhenShoot = null;
	/**
	 * If true, then this gun can be dual wielded
	 */
	public boolean oneHanded = false;
	/**
	 * For one shot items like a panzerfaust
	 */
	public boolean consumeGunUponUse = false;
	/**
     * Show the crosshair when holding this weapon
     */
    public boolean showCrosshair = true;
	/**
	 * Item to drop on shooting
	 */
	public String dropItemOnShoot = null;
	//Custom Melee Stuff
	/**
	 * The time delay between custom melee attacks
	 */
	public int meleeTime = 1;
	/**
	 * The path the melee weapon takes
	 */
	public ArrayList<Vector3f> meleePath = new ArrayList<>(), meleePathAngles = new ArrayList<>();
	/**
	 * The points on the melee weapon that damage is actually done from.
	 */
	public ArrayList<Vector3f> meleeDamagePoints = new ArrayList<>();
	/**
	 * Set these to make guns only usable by a certain type of entity
	 */
	public boolean usableByPlayers = true, usableByMechas = true;
	
	/**
    * Whether Gun makes players to be EnumAction.bow
    */
    public EnumAction itemUseAction = EnumAction.BOW;
    /* Whether the gun can be hipfired while sprinting */
    /**
     * 0=use flansmod.cfg default, 1=force allow, 2=force deny
     **/
    public int hipFireWhileSprinting = 0;

    //Launcher variables
    public int canLockOnAngle = 5;
    public int lockOnSoundTime = 0;
    public String lockOnSound = "";
    public int maxRangeLockOn = 80;
    public boolean canSetPosition = false;
    /**
     * Determines what the launcher can lock on to
     */
    public boolean lockOnToPlanes = false, lockOnToVehicles = false, lockOnToMechas = false, lockOnToPlayers = false, lockOnToLivings = false;
    
	//Information
	//Show any variables into the GUI when hovering over items.
	/**
	 * If false, then attachments wil not be listed in item GUI
	 */
	public boolean showAttachments = true;
	/**
	 * Show statistics
	 */
	public boolean showDamage = false, showRecoil = false, showSpread = false;
	/**
	 * Show reload time in seconds
	 */
	public boolean showReloadTime = false;
	
	//Shields
	//A shield is actually a gun without any shoot functionality (similar to knives or binoculars)
	//and a load of shield code on top. This means that guns can have in built shields (think Nerf Stampede)
	/**
	 * Whether or not this gun has a shield piece
	 */
	public boolean shield = false;
	/**
	 * Shield collision box definition. In model co-ordinates
	 */
	public Vector3f shieldOrigin, shieldDimensions;
	/**
	 * Float between 0 and 1 denoting the proportion of damage blocked by the shield
	 */
	public float shieldDamageAbsorption = 0F;
	
	//Sounds
	/**
	 * The sound played upon shooting
	 */
	public String shootSound;
	/**
     * Bullet insert reload sound
     */
    public String bulletInsert = "defaultshellinsert";
    /**
     * Pump Sound
     */
    public String actionSound;
    /**
     * The sound to play upon shooting on last round
     */
    public String lastShootSound;

    /**
     * The sound played upon shooting with a suppressor
     */
    public String suppressedShootSound;
	/**
	 * The length of the sound for looping sounds
	 */
	public int shootSoundLength;
    /**
     * The sound to play upon reloading when empty
     */
    public String reloadSoundOnEmpty;
    /**
     * The sound to play open firing when empty(once)
     */
    public String clickSoundOnEmpty;
    /**
     * The sound to play while holding the weapon in the hand
     */
    public String idleSound;

    //Sound Modifiers
    /**
     * Whether to distort the sound or not. Generally only set to false for looping sounds
     */
	public boolean distortSound = true;
	/**
     * The length of the idle sound for looping sounds (miniguns)
     */
    public int idleSoundLength;
    /**
     * The block range for idle sounds (for miniguns etc)
     */
    public int idleSoundRange = 50;
    /**
     * The block range for melee sounds
     */
    public int meleeSoundRange = 50;
    /**
     * The block range for reload sounds
     */
    public int reloadSoundRange = 50;
    /**
     * The block range for gunshots sounds
     */
    public int gunSoundRange = 50;

    /**
     * Sound to be played outside of normal range
     */
    public String distantShootSound = "";
    /**
     * Max range for the sound to be played
     */
    public int distantSoundRange = 100;
	/**
	 * The sound to play upon reloading
	 */
	public String reloadSound;
	
	//Looping sounds
	/**
	 * Whether the looping sounds should be used. Automatically set if the player sets any one of the following sounds
	 */
	public boolean useLoopingSounds = false;
	/**
	 * Played when the player starts to hold shoot
	 */
	public String warmupSound;
	public int warmupSoundLength = 20;
	/**
	 * Played in a loop until player stops holding shoot
	 */
	public String loopedSound;
	public int loopedSoundLength = 20;
	/**
	 * Played when the player stops holding shoot
	 */
	public String cooldownSound;
	
	/**
	 * The sound to play upon weapon swing
	 */
	public String meleeSound;
	/**
	 * The sound to play while holding the weapon in the hand
	 */
	
	
	//Deployable Settings
	/**
	 * If true, then the bullet does not shoot when right clicked, but must instead be placed on the ground
	 */
	public boolean deployable = false;
	/**
	 * The deployable model
	 */
	@SideOnly(Side.CLIENT)
	public ModelMG deployableModel;
	
	public String deployableModelString;
	
	@SideOnly(Side.CLIENT)
	public ModelMuzzleFlash muzzleFlashModel;
	public String muzzleFlashModelString;
	/**
	 * The deployable model's texture
	 */
	public String deployableTexture;
	/**
	 * Various deployable settings controlling the player view limits and standing position
	 */
	public float standBackDist = 1.5F, topViewLimit = -60F, bottomViewLimit = 30F, sideViewLimit = 45F, pivotHeight = 0.375F;
	
	//Default Scope Settings. Overriden by scope attachments
	//In many cases, this will simply be iron sights
	/**
	 * Default scope overlay texture
	 */
	public String defaultScopeTexture;
	/**
	 * Whether the default scope has an overlay
	 */
	public boolean hasScopeOverlay = false;
	/**
	 * The zoom level of the default scope
	 */
	public float zoomLevel = 1.0F;
	/**
	 * The FOV zoom level of the default scope
	 */
	public float FOVFactor = 1.5F;
	/**
     * Gives night vision while scoped if true
     */
    public boolean allowNightVision = false;
    /**
     * For adding a bullet casing model to render
     */
    public ModelCasing casingModel;
    public String casingModelString;
    /**
     * For adding a muzzle flash model to render
     */
    public ModelFlash flashModel;
    public String flashModelString;
    /**
     * Set a bullet casing texture
     */
    public String casingTexture;
    /**
     * Set a muzzle flash texture
     */
    public String flashTexture;
    /**
     * Set a hit marker texture
     */
    public String hitTexture;

    public String muzzleFlashParticle = "flansmod.muzzleflash";
    public float muzzleFlashParticleSize = 1F;
    public Boolean showMuzzleFlashParticles = true;
    public Boolean showMuzzleFlashParticlesFirstPerson = false;
    public Vector3f muzzleFlashParticlesHandOffset = new Vector3f();
    public Vector3f muzzleFlashParticlesShoulderOffset = new Vector3f();
    
    //Model variables
	/**
	 * For guns with 3D models
	 */
	//TODO properly separate the data
	//@SideOnly(Side.CLIENT)
	public ModelGun model;
	
	//Attachment settings
	/**
	 * If this is true, then all attachments are allowed. Otherwise the list is checked
	 */
	public boolean allowAllAttachments = false;
	/**
	 * The list of allowed attachments for this gun
	 */
	public ArrayList<AttachmentType> allowedAttachments = new ArrayList<>();
	/**
	 * Whether each attachment slot is available
	 */
	public boolean allowBarrelAttachments = false, allowScopeAttachments = false,
			allowStockAttachments = false, allowGripAttachments = false, allowGadgetAttachments = false,
            allowSlideAttachments = false, allowPumpAttachments = false, allowAccessoryAttachments = false;
	/**
	 * The number of generic attachment slots there are on this gun
	 */
	public int numGenericAttachmentSlots = 0;
	
	/**
	 * The static hashmap of all guns by shortName
	 */
	public static HashMap<Integer, GunType> guns = new HashMap<>();
	/**
	 * The static list of all guns
	 */
	public static ArrayList<GunType> gunList = new ArrayList<>();
	
	//Modifiers
	/**
	 * Speeds up or slows down player movement when this item is held
	 */
	public float moveSpeedModifier = 1F;
	/**
	 * Gives knockback resistance to the player
	 */
	public float knockbackModifier = 0F;
	/**
     * Default spread of the gun. Do not modify.
     */
    private float defaultSpread = 0F;
    // Modifier for (usually decreasing) spread when gun is ADS. -1 uses default values from flansmod.cfg
    public float adsSpreadModifier = -1F;
    // Modifier for (usually decreasing) spread when gun is ADS. -1 uses default values from flansmod.cfg. For shotguns.
    public float adsSpreadModifierShotgun = -1F;

	public float switchDelay = 15F;
	
	public GunType(TypeFile file)
	{
		super(file);
	}
	
	@Override
    public void preRead(TypeFile file) {
        super.preRead(file);
	}
	
	@Override
	public void postRead(TypeFile file)
	{
		super.postRead(file);
		gunList.add(this);
		guns.put(shortName.hashCode(), this);
		
		if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
		{
			checkMF();
		}

	}
	
	@SideOnly(Side.CLIENT)
	private void checkMF()
	{
		if(muzzleFlashModel == null && flashModel == null)
		{
			muzzleFlashModel = new ModelDefaultMuzzleFlash();
		}
	}
	
	@Override
	protected void read(String[] split, TypeFile file)
	{
		super.read(split, file);
		try
		{
			damage = Read(split, "Damage", damage);
			canForceReload = Read(split, "CanForceReload", canForceReload);
			reloadTime = Read(split, "ReloadTime", reloadTime);
			recoilPitch = Read(split, "Recoil", recoilPitch);
			knockback = Read(split, "Knockback", knockback);
			bulletSpread = Read(split, "Accuracy", bulletSpread);
			bulletSpread = Read(split, "Spread", bulletSpread);
			numBullets = Read(split, "NumBullets", numBullets);
			consumeGunUponUse = Read(split, "ConsumeGunOnUse", consumeGunUponUse);
			dropItemOnShoot = Read(split, "DropItemOnShoot", dropItemOnShoot);
			numBurstRounds = Read(split, "NumBurstRounds", numBurstRounds);
			minigunStartSpeed = Read(split, "MinigunStartSpeed", minigunStartSpeed);
			
			//Information
			showAttachments = Read(split, "ShowAttachments", showAttachments);
			showDamage = Read(split, "ShowDamage", showDamage);
			showRecoil = Read(split, "ShowRecoil", showRecoil);
			showSpread = Read(split, "ShowAccuracy", showSpread);
			showReloadTime = Read(split, "ShowReloadTime", showReloadTime);
			
			//Sounds
			shootDelay = Read(split, "ShootDelay", shootDelay);
			shootSoundLength = Read(split, "SoundLength", shootSoundLength);
			distortSound = Read(split, "DistortSound", distortSound);
			idleSoundLength = Read(split, "IdleSoundLength", idleSoundLength);
			warmupSoundLength = Read(split, "WarmupSoundLength", warmupSoundLength);
			loopedSoundLength = Read(split, "LoopedSoundLength", loopedSoundLength);
			loopedSoundLength = Read(split, "SpinSoundLength", loopedSoundLength);
			
			if(split[0].equals("MeleeDamage"))
			{
				try{ meleeDamage = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for MeleeDamage."); }
				if(meleeDamage > 0F)
					secondaryFunction = EnumSecondaryFunction.MELEE;
			} else if (split[0].equals("MeleeDamageDriveableModifier")) {
				try{ meleeDamageDriveableModifier = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for MeleeDamageDriveableModifier."); }
            } else if (split[0].equals("CounterRecoilForce"))
            	try{ recoilCounterCoefficient = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for CounterRecoilForce."); }
            else if (split[0].equals("CounterRecoilForceSneaking"))
            	try{ recoilCounterCoefficientSneaking = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for CounterRecoilForceSneaking."); }
            else if (split[0].equals("CounterRecoilForceSprinting"))
            	try{ recoilCounterCoefficientSprinting = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for CounterRecoilForceSprinting."); }
            else if (split[0].equals("SneakSpreadModifier"))
            	try{ sneakSpreadMultiplier = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for SneakSpreadModifier."); }
            else if (split[0].equals("SprintSpreadModifier"))
            	try{ sprintSpreadMultiplier = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for SprintSpreadModifier."); }
            else if (split[0].equals("AllowRearm"))
            	try{ allowRearm = Boolean.parseBoolean(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for AllowRearm."); }
            else if (split[0].equals("Recoil"))
            	try{ recoilPitch = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for Recoil."); }
            else if (split[0].equals("RecoilYaw"))
            	try{ recoilYaw = Float.parseFloat(split[1]) / 10; } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for RecoilYaw."); }
            else if (split[0].equals("RandomRecoilRange"))
            	try{ rndRecoilPitchRange = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for RandomRecoilRange."); }
            else if (split[0].equals("RandomRecoilYawRange"))
            	try{ rndRecoilYawRange = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for RandomRecoilYawRange."); }
            else if (split[0].equals("DecreaseRecoil"))
            	try{ decreaseRecoilPitch = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for DecreaseRecoil."); }
            else if (split[0].equals("DecreaseRecoilYaw"))
            	try{ decreaseRecoilYaw = Float.parseFloat(split[1]) > 0 ? Float.parseFloat(split[1]) : 0.5F; } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for DecreaseRecoilYaw."); }
            else if (split[0].equals("ADSSpreadModifier"))
            	try{ adsSpreadModifier = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for ADSSpreadModifier."); }
            else if (split[0].equals("ADSSpreadModifierShotgun"))
            	try{ adsSpreadModifierShotgun = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for ADSSpreadModifierShotgun."); }
            else if (split[0].equals("AllowNumBulletsByBulletType"))
            	try{ allowNumBulletsByBulletType = Boolean.parseBoolean(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for AllowNumBulletsByBulletType."); }
            else if (split[0].equals("AllowSpreadByBullet"))
            	try{ allowSpreadByBullet = Boolean.parseBoolean(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for AllowSpreadByBullet."); }
            else if (split[0].equals("CanLockAngle"))
            	try{ canLockOnAngle = Integer.parseInt(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for CanLockAngle."); }
            else if (split[0].equals("LockOnSoundTime"))
            	try{ lockOnSoundTime = Integer.parseInt(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for LockOnSoundTime."); }
            else if (split[0].equals("LockOnToDriveables"))
            	try{ lockOnToPlanes = lockOnToVehicles = lockOnToMechas = Boolean.parseBoolean(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for LockOnToDriveables."); }
            else if (split[0].equals("LockOnToVehicles"))
            	try{ lockOnToVehicles = Boolean.parseBoolean(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for LockOnToVehicles."); }
            else if (split[0].equals("LockOnToPlanes"))
            	try{ lockOnToPlanes = Boolean.parseBoolean(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for LockOnToPlanes."); }
            else if (split[0].equals("LockOnToMechas"))
            	try{ lockOnToMechas = Boolean.parseBoolean(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for LockOnToMechas."); }
            else if (split[0].equals("LockOnToPlayers"))
            	try{ lockOnToPlayers = Boolean.parseBoolean(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for LockOnToPlayers."); }
            else if (split[0].equals("LockOnToLivings"))
            	try{ lockOnToLivings = Boolean.parseBoolean(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for LockOnToLivings."); }
            else if (split[0].equals("ShowCrosshair"))
            	try{ showCrosshair = Boolean.parseBoolean(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for ShowCrosshair."); }
            else if (split[0].equals("ItemUseAction"))
            	try{ itemUseAction = EnumAction.valueOf(split[1].toLowerCase()); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for ItemUseAction."); }
            else if (split[0].equals("HipFireWhileSprinting"))
            	try{ hipFireWhileSprinting = Boolean.parseBoolean(split[1].toLowerCase()) ? 1 : 2; } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for HipFireWhileSprinting."); }
            else if (split[0].equals("MaxRangeLockOn"))
            	try{ maxRangeLockOn = Integer.parseInt(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for MaxRangeLockOn."); }
            else if (split[0].equals("RoundsPerMin"))
            	try{ roundsPerMin = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for RoundsPerMin."); }
            else if (split[0].equals("IdleSoundRange"))
            	try{ idleSoundRange = Integer.parseInt(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for IdleSoundRange."); }
            else if (split[0].equals("MeleeSoundRange"))
            	try{ meleeSoundRange = Integer.parseInt(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for MeleeSoundRange."); }
            else if (split[0].equals("ReloadSoundRange"))
            	try{  reloadSoundRange = Integer.parseInt(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for ReloadSoundRange."); }
            else if (split[0].equals("GunSoundRange"))
            	try{ gunSoundRange = Integer.parseInt(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for GunSoundRange."); }
            else if(split[0].equals("ShootSound"))
			{
				shootSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			} else if (split[0].equals("BulletInsertSound")) {
	            bulletInsert = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        } else if (split[0].equals("ActionSound")) {
	            actionSound = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        } else if (split[0].equals("LastShootSound")) {
	            lastShootSound = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        } else if (split[0].equals("SuppressedShootSound")) {
	            suppressedShootSound = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        } 
	        else if(split[0].equals("ReloadSound"))
			{
				reloadSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			} else if (split[0].equals("EmptyReloadSound")) {
	            reloadSoundOnEmpty = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        } else if (split[0].equals("EmptyClickSound")) {
	            clickSoundOnEmpty = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        }
			else if(split[0].equals("IdleSound"))
			{
				idleSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("MeleeSound"))
			{
				meleeSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			
			//Looping sounds
			else if(split[0].equals("WarmupSound"))
			{
				warmupSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("LoopedSound") || split[0].equals("SpinSound"))
			{
				loopedSound = split[1];
				useLoopingSounds = true;
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("CooldownSound"))
			{
				cooldownSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			} else if (split[0].equals("LockOnSound")) {
	            lockOnSound = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        } else if (split[0].equals("DistantSound")) {
	            distantShootSound = split[1];
	            FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
	        } else if (split[0].equals("DistantSoundRange")) {
	            distantSoundRange = Integer.parseInt(split[1]);
	        }
			
			//Modes and zoom settings
			else if(split[0].equals("Mode"))
			{
				mode = EnumFireMode.getFireMode(split[1]);
				defaultmode = mode;
	            submode = new EnumFireMode[split.length - 1];
	            for (int i = 0; i < submode.length; i++) {
	                submode[i] = EnumFireMode.getFireMode(split[1 + i]);
	            }
			}
			else if(split[0].equals("Scope"))
			{
				hasScopeOverlay = true;
				if(split[1].equals("None"))
					hasScopeOverlay = false;
				else defaultScopeTexture = split[1];
			} else if (split[0].equals("AllowNightVision")) {
				try{ allowNightVision = Boolean.parseBoolean(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for allowNightVision."); }
            }
			else if(split[0].equals("ZoomLevel"))
			{
				zoomLevel = Float.parseFloat(split[1]);
				if(zoomLevel > 1F)
					secondaryFunction = EnumSecondaryFunction.ZOOM;
			}
			else if(split[0].equals("FOVZoomLevel"))
			{
				FOVFactor = Float.parseFloat(split[1]);
				if(FOVFactor > 1F)
					secondaryFunction = EnumSecondaryFunction.ADS_ZOOM;
			}
			else if(split[0].equals("Deployable"))
				deployable = split[1].equals("True");
			else if(FMLCommonHandler.instance().getSide().isClient() && deployable && split[0].equals("DeployedModel"))
			{
				deployableModel = FlansMod.proxy.loadModel(split[1], shortName, ModelMG.class, fileName, packName);
			}
			else if(FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("Model")))
			{
				model = FlansMod.proxy.loadModel(split[1], shortName, ModelGun.class, fileName, packName);
			}
			else if (FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("CasingModel"))) 
			{
                casingModel = FlansMod.proxy.loadModel(split[1], shortName, ModelCasing.class, fileName, packName);
                casingModelString = split[1];
            }
			else if (split[0].equals("CasingTexture"))
	            casingTexture = split[1];
	        else if (split[0].equals("FlashTexture"))
	            flashTexture = split[1];
	        else if (split[0].equals("MuzzleFlashParticle"))
	            muzzleFlashParticle = split[1];
	        else if (split[0].equals("MuzzleFlashParticleSize"))
	        	try{ muzzleFlashParticleSize = Float.parseFloat(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for MuzzleFlashParticleSize."); }
	        else if (split[0].equals("ShowMuzzleFlashParticle"))
	        	try{ showMuzzleFlashParticles = Boolean.parseBoolean(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for ShowMuzzleFlashParticle."); }
	        else if (split[0].equals("ShowMuzzleFlashParticleFirstPerson"))
	        	try{ showMuzzleFlashParticlesFirstPerson = Boolean.parseBoolean(split[1]); } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for ShowMuzzleFlashParticleFirstPerson."); }
	        else if (split[0].equals("MuzzleFlashParticleShoulderOffset"))
	            muzzleFlashParticlesShoulderOffset = new Vector3f(split[1], null);
	        else if (split[0].equals("MuzzleFlashParticleHandOffset"))
	            muzzleFlashParticlesHandOffset = new Vector3f(split[1], null);
	        else if (split[0].equals("Texture"))
                texture = split[1];
            else if (split[0].equals("HitTexture"))
                hitTexture = split[1];
			else if(FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("MuzzleFlashModel")))
			{
				muzzleFlashModel = FlansMod.proxy.loadModel(split[1], shortName, ModelMuzzleFlash.class, fileName, packName);
				muzzleFlashModelString = split[1];
			}
			else if (FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("FlashModel"))) 
			{
	            flashModel = FlansMod.proxy.loadModel(split[1], shortName, ModelFlash.class, fileName, packName);
	            flashModelString = split[1];
	        }
			deployableTexture = Read(split, "DeployedTexture", deployableTexture);
			standBackDist = Read(split, "StandBackDistance", standBackDist);
			topViewLimit = Read(split, "TopViewLimit", topViewLimit);
			bottomViewLimit = Read(split, "BottomViewLimit", bottomViewLimit);
			sideViewLimit = Read(split, "SideViewLimit", sideViewLimit);
			pivotHeight = Read(split, "PivotHeight", pivotHeight);
			numAmmoItemsInGun = Read(split, "NumAmmoSlots", numAmmoItemsInGun);
			numAmmoItemsInGun = Read(split, "NumAmmoItemsInGun", numAmmoItemsInGun);
			numAmmoItemsInGun = Read(split, "LoadIntoGun", numAmmoItemsInGun);
			canShootUnderwater = Read(split, "CanShootUnderwater", canShootUnderwater);
			oneHanded = Read(split, "OneHanded", oneHanded);
			usableByPlayers = Read(split, "UsableByPlayers", usableByPlayers);
			usableByMechas = Read(split, "UsableByMechas", usableByMechas);
			
			if(split[0].equals("SpreadPattern"))
				spreadPattern = EnumSpreadPattern.get(split[1]);
			
			if(split[0].equals("Ammo"))
			{
				ShootableType type = ShootableType.getShootableType(split[1]);
				if(type != null)
				{
					ammo.add(type);
					if(type.explosionRadius <= 0F)
						nonExplosiveAmmo.add(type);
				}
				// Too spammy when packs have optional dependencies
				//else FlansMod.log.warn("Could not find " + split[1] + " when reading ammo types for " + shortName);
			} else if (split[0].equals("NumAmmoSlots") || split[0].equals("NumAmmoItemsInGun") || split[0].equals("LoadIntoGun"))
				try{ numPrimaryAmmoItems = Integer.parseInt(split[1]);  } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for NumAmmoSlots or NumAmmoItemsInGun or LoadIntoGun."); }
			else if(split[0].equals("BulletSpeed"))
			{
				try{ 
					if(split[1].toLowerCase().equals("instant"))
					{
						bulletSpeed = 0.0f;
					}
					else bulletSpeed = Float.parseFloat(split[1]);
					
					if(bulletSpeed > 3.0f)
					{
						bulletSpeed = 0.0f;
					}
				} catch (Exception e) { 
					InfoType.LogError(shortName, "Incorrect format for BulletSpeed."); 
				}
			}
            else if (split[0].equals("CanSetPosition"))
            	try{ canSetPosition = Boolean.parseBoolean(split[1].toLowerCase());  } catch (Exception e) { InfoType.LogError(shortName, "Incorrect format for CanSetPosition."); }
			else if(split[0].equals("SecondaryFunction"))
				secondaryFunction = EnumSecondaryFunction.get(split[1]);
			
				//Custom Melee Stuff
			else if(split[0].equals("UseCustomMelee") && Boolean.parseBoolean(split[1]))
				secondaryFunction = EnumSecondaryFunction.CUSTOM_MELEE;
			else if (split[0].equals("UseCustomMeleeWhenShoot") && Boolean.parseBoolean(split[1]))
                secondaryFunctionWhenShoot = EnumSecondaryFunction.CUSTOM_MELEE;
			
			meleeTime = Read(split, "MeleeTime", meleeTime);
			
			if(split[0].equals("AddNode"))
			{
				meleePath.add(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F));
				meleePathAngles.add(new Vector3f(Float.parseFloat(split[4]), Float.parseFloat(split[5]), Float.parseFloat(split[6])));
			}
			else if(split[0].equals("MeleeDamagePoint") || split[0].equals("MeleeDamageOffset"))
			{
				meleeDamagePoints.add(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F));
			}
			
			//Player modifiers
			moveSpeedModifier = Read(split, "MoveSpeedModifier", moveSpeedModifier);
			moveSpeedModifier = Read(split, "Slowness", moveSpeedModifier);
			knockbackModifier = Read(split, "KnockbackReduction", knockbackModifier);
			knockbackModifier = Read(split, "KnockbackModifier", knockbackModifier);
			switchDelay = Read(split, "SwitchDelay", switchDelay);
			
			//Attachment settings
			allowAllAttachments = Read(split, "AllowAllAttachments", allowAllAttachments);
			if(split[0].equals("AllowAttachments"))
			{
				for(int i = 1; i < split.length; i++)
				{
					allowedAttachments.add(AttachmentType.getAttachment(split[i]));
				}
			}
			
			allowBarrelAttachments = Read(split, "AllowBarrelAttachments", allowBarrelAttachments);
			allowScopeAttachments = Read(split, "AllowScopeAttachments", allowScopeAttachments);
			allowStockAttachments = Read(split, "AllowStockAttachments", allowStockAttachments);
			allowGripAttachments = Read(split, "AllowGripAttachments", allowGripAttachments);
			numGenericAttachmentSlots = Read(split, "NumGenericAttachmentSlots", numGenericAttachmentSlots);
			
			allowGadgetAttachments = Read(split, "AllowGadgetAttachments", allowGadgetAttachments);
			allowSlideAttachments = Read(split, "AllowSlideAttachments", allowSlideAttachments);
			allowPumpAttachments = Read(split, "AllowPumpAttachments", allowPumpAttachments);
			allowAccessoryAttachments = Read(split, "AllowAccessoryAttachments", allowAccessoryAttachments);
			 
			
			//Shield settings
			if(split[0].toLowerCase().equals("shield"))
			{
				shield = true;
				shieldDamageAbsorption = Float.parseFloat(split[1]);
				shieldOrigin = new Vector3f(Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F, Float.parseFloat(split[4]) / 16F);
				shieldDimensions = new Vector3f(Float.parseFloat(split[5]) / 16F, Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F);
			} else if (FMLCommonHandler.instance().getSide().isClient()) {
                processAnimationConfigs(split);
            }
		}
		catch(Exception e)
		{
			if (split != null) {
                StringBuilder msg = new StringBuilder(" : ");
                for (String s : split) msg.append(" ").append(s);
				FlansMod.log.error("Reading gun file" + file.name + " failed from content pack " + file.contentPack + ": " + msg);
            } else {
				FlansMod.log.error("Reading gun file" + file.name + " failed from content pack " + file.contentPack);
            }
			if (split != null)
			{
				FlansMod.log.error("Errored reading line: " + String.join(" ", split));
			}
			FlansMod.log.throwing(e);		
		}
	}
	
	public void processAnimationConfigs(String[] split) {
        if (split[0].equals("animMinigunBarrelOrigin"))
            model.minigunBarrelOrigin = parseVector3f(split);
        else if (split[0].equals("animBarrelAttachPoint"))
            model.barrelAttachPoint = parseVector3f(split);
        else if (split[0].equals("animScopeAttachPoint"))
            model.scopeAttachPoint = parseVector3f(split);
        else if (split[0].equals("animStockAttachPoint"))
            model.stockAttachPoint = parseVector3f(split);
        else if (split[0].equals("animGripAttachPoint"))
            model.gripAttachPoint = parseVector3f(split);
        else if (split[0].equals("animGadgetAttachPoint"))
            model.gadgetAttachPoint = parseVector3f(split);
        else if (split[0].equals("animSlideAttachPoint"))
            model.slideAttachPoint = parseVector3f(split);
        else if (split[0].equals("animPumpAttachPoint"))
            model.pumpAttachPoint = parseVector3f(split);
        else if (split[0].equals("animAccessoryAttachPoint"))
            model.accessoryAttachPoint = parseVector3f(split);

        else if (split[0].equals("animDefaultBarrelFlashPoint"))
            model.defaultBarrelFlashPoint = parseVector3f(split);
        else if (split[0].equals("animMuzzleFlashPoint"))
            model.muzzleFlashPoint = parseVector3f(split);

        else if (split[0].equals("animHasFlash"))
            model.hasFlash = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animHasArms"))
            model.hasArms = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animLeftArmPos"))
            model.leftArmPos = parseVector3f(split);
        else if (split[0].equals("animLeftArmRot"))
            model.leftArmRot = parseVector3f(split);
        else if (split[0].equals("animLeftArmScale"))
            model.leftArmScale = parseVector3f(split);
        else if (split[0].equals("animRightArmPos"))
            model.rightArmPos = parseVector3f(split);
        else if (split[0].equals("animRightArmRot"))
            model.rightArmRot = parseVector3f(split);
        else if (split[0].equals("animRightArmScale"))
            model.rightArmScale = parseVector3f(split);

        else if (split[0].equals("animRightArmReloadPos"))
            model.rightArmReloadPos = parseVector3f(split);
        else if (split[0].equals("animRightArmReloadRot"))
            model.rightArmReloadRot = parseVector3f(split);
        else if (split[0].equals("animLeftArmReloadPos"))
            model.leftArmReloadPos = parseVector3f(split);
        else if (split[0].equals("animLeftArmReloadRot"))
            model.leftArmReloadRot = parseVector3f(split);

        else if (split[0].equals("animRightArmChargePos"))
            model.rightArmChargePos = parseVector3f(split);
        else if (split[0].equals("animRightArmChargeRot"))
            model.rightArmChargeRot = parseVector3f(split);
        else if (split[0].equals("animLeftArmChargePos"))
            model.leftArmChargePos = parseVector3f(split);
        else if (split[0].equals("animLeftArmChargeRot"))
            model.leftArmChargeRot = parseVector3f(split);

        else if (split[0].equals("animStagedRightArmReloadPos"))
            model.stagedrightArmReloadPos = parseVector3f(split);
        else if (split[0].equals("animStagedRightArmReloadRot"))
            model.stagedrightArmReloadRot = parseVector3f(split);
        else if (split[0].equals("animStagedLeftArmReloadPos"))
            model.stagedleftArmReloadPos = parseVector3f(split);
        else if (split[0].equals("animStagedLeftArmReloadRot"))
            model.stagedleftArmReloadRot = parseVector3f(split);

        else if (split[0].equals("animRightHandAmmo"))
            model.rightHandAmmo = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animLeftHandAmmo"))
            model.leftHandAmmo = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animGunSlideDistance"))
            model.gunSlideDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animAltGunSlideDistance"))
            model.altgunSlideDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animRecoilSlideDistance"))
            model.RecoilSlideDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animRotatedSlideDistance"))
            model.RotateSlideDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animShakeDistance"))
            model.ShakeDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animRecoilAmount"))
            model.recoilAmount = Float.parseFloat(split[1]);

        else if (split[0].equals("animCasingAnimDistance"))
            model.casingAnimDistance = parseVector3f(split);
        else if (split[0].equals("animCasingAnimSpread"))
            model.casingAnimSpread = parseVector3f(split);
        else if (split[0].equals("animCasingAnimTime"))
            model.casingAnimTime = Integer.parseInt(split[1]);
        else if (split[0].equals("animCasingRotateVector"))
            model.casingRotateVector = parseVector3f(split);
        else if (split[0].equals("animCasingAttachPoint"))
            model.casingAttachPoint = parseVector3f(split);
        else if (split[0].equals("animCasingDelay"))
            model.casingDelay = Integer.parseInt(split[1]);
        else if (split[0].equals("animCasingScale"))
            model.caseScale = Float.parseFloat(split[1]);
        else if (split[0].equals("animFlashScale"))
            model.flashScale = Float.parseFloat(split[1]);

        else if (split[0].equals("animChargeHandleDistance"))
            model.chargeHandleDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animChargeDelay"))
            model.chargeDelay = Integer.parseInt(split[1]);
        else if (split[0].equals("animChargeDelayAfterReload"))
            model.chargeDelayAfterReload = Integer.parseInt(split[1]);
        else if (split[0].equals("animChargeTime"))
            model.chargeTime = Integer.parseInt(split[1]);
        else if (split[0].equals("animCountOnRightHandSide"))
            model.countOnRightHandSide = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animIsBulletCounterActive"))
            model.isBulletCounterActive = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animIsAdvBulletCounterActive"))
            model.isAdvBulletCounterActive = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animAnimationType")) {
            if (split[1].equals("NONE"))
                model.animationType = EnumAnimationType.NONE;
            else if (split[1].equals("BOTTOM_CLIP"))
                model.animationType = EnumAnimationType.BOTTOM_CLIP;
            else if (split[1].equals("CUSTOMBOTTOM_CLIP"))
                model.animationType = EnumAnimationType.CUSTOMBOTTOM_CLIP;
            else if (split[1].equals("PISTOL_CLIP"))
                model.animationType = EnumAnimationType.PISTOL_CLIP;
            else if (split[1].equals("CUSTOMPISTOL_CLIP"))
                model.animationType = EnumAnimationType.CUSTOMPISTOL_CLIP;
            else if (split[1].equals("TOP_CLIP"))
                model.animationType = EnumAnimationType.TOP_CLIP;
            else if (split[1].equals("CUSTOMTOP_CLIP"))
                model.animationType = EnumAnimationType.CUSTOMTOP_CLIP;
            else if (split[1].equals("SIDE_CLIP"))
                model.animationType = EnumAnimationType.SIDE_CLIP;
            else if (split[1].equals("CUSTOMSIDE_CLIP"))
                model.animationType = EnumAnimationType.CUSTOMSIDE_CLIP;
            else if (split[1].equals("P90"))
                model.animationType = EnumAnimationType.P90;
            else if (split[1].equals("CUSTOMP90"))
                model.animationType = EnumAnimationType.CUSTOMP90;
            else if (split[1].equals("SHOTGUN"))
                model.animationType = EnumAnimationType.SHOTGUN;
            else if (split[1].equals("CUSTOMSHOTGUN"))
                model.animationType = EnumAnimationType.CUSTOMSHOTGUN;
            else if (split[1].equals("RIFLE"))
                model.animationType = EnumAnimationType.RIFLE;
            else if (split[1].equals("CUSTOMRIFLE"))
                model.animationType = EnumAnimationType.CUSTOMRIFLE;
            else if (split[1].equals("REVOLVER"))
                model.animationType = EnumAnimationType.REVOLVER;
            else if (split[1].equals("CUSTOMREVOLVER"))
                model.animationType = EnumAnimationType.CUSTOMREVOLVER;
            else if (split[1].equals("REVOLVER2"))
                model.animationType = EnumAnimationType.REVOLVER;
            else if (split[1].equals("CUSTOMREVOLVER2"))
                model.animationType = EnumAnimationType.CUSTOMREVOLVER;
            else if (split[1].equals("END_LOADED"))
                model.animationType = EnumAnimationType.END_LOADED;
            else if (split[1].equals("CUSTOMEND_LOADED"))
                model.animationType = EnumAnimationType.CUSTOMEND_LOADED;
            else if (split[1].equals("RIFLE_TOP"))
                model.animationType = EnumAnimationType.RIFLE_TOP;
            else if (split[1].equals("CUSTOMRIFLE_TOP"))
                model.animationType = EnumAnimationType.CUSTOMRIFLE_TOP;
            else if (split[1].equals("BULLPUP"))
                model.animationType = EnumAnimationType.BULLPUP;
            else if (split[1].equals("CUSTOMBULLPUP"))
                model.animationType = EnumAnimationType.CUSTOMBULLPUP;
            else if (split[1].equals("ALT_PISTOL_CLIP"))
                model.animationType = EnumAnimationType.ALT_PISTOL_CLIP;
            else if (split[1].equals("CUSTOMALT_PISTOL_CLIP"))
                model.animationType = EnumAnimationType.CUSTOMALT_PISTOL_CLIP;
            else if (split[1].equals("GENERIC"))
                model.animationType = EnumAnimationType.GENERIC;
            else if (split[1].equals("CUSTOMGENERIC"))
                model.animationType = EnumAnimationType.CUSTOMGENERIC;
            else if (split[1].equals("BACK_LOADED"))
                model.animationType = EnumAnimationType.BACK_LOADED;
            else if (split[1].equals("CUSTOMBACK_LOADED"))
                model.animationType = EnumAnimationType.CUSTOMBACK_LOADED;
            else if (split[1].equals("STRIKER"))
                model.animationType = EnumAnimationType.STRIKER;
            else if (split[1].equals("CUSTOMSTRIKER"))
                model.animationType = EnumAnimationType.CUSTOMSTRIKER;
            else if (split[1].equals("BREAK_ACTION"))
                model.animationType = EnumAnimationType.BREAK_ACTION;
            else if (split[1].equals("CUSTOMBREAK_ACTION"))
                model.animationType = EnumAnimationType.CUSTOMBREAK_ACTION;
            else if (split[1].equals("CUSTOM"))
                model.animationType = EnumAnimationType.CUSTOM;
        } else if (split[0].equals("animMeleeAnimation")) {
            if (split[1].equals("DEFAULT"))
                model.meleeAnimation = EnumMeleeAnimation.DEFAULT;
            else if (split[1].equals("NONE"))
                model.meleeAnimation = EnumMeleeAnimation.NONE;
            else if (split[1].equals("BLUNT_SWING"))
                model.meleeAnimation = EnumMeleeAnimation.BLUNT_SWING;
            else if (split[1].equals("BLUNT_BASH"))
                model.meleeAnimation = EnumMeleeAnimation.BLUNT_BASH;
            else if (split[1].equals("STAB_UNDERARM"))
                model.meleeAnimation = EnumMeleeAnimation.STAB_UNDERARM;
            else if (split[1].equals("STAB_OVERARM"))
                model.meleeAnimation = EnumMeleeAnimation.STAB_OVERARM;
        } else if (split[0].equals("animTiltGunTime"))
            model.tiltGunTime = Float.parseFloat(split[1]);
        else if (split[0].equals("animUnloadClipTime"))
            model.unloadClipTime = Float.parseFloat(split[1]);
        else if (split[0].equals("animLoadClipTime"))
            model.loadClipTime = Float.parseFloat(split[1]);

        else if (split[0].equals("animScopeIsOnSlide"))
            model.scopeIsOnSlide = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animScopeIsOnBreakAction"))
            model.scopeIsOnBreakAction = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animNumBulletsInReloadAnimation"))
            model.numBulletsInReloadAnimation = Float.parseFloat(split[1]);

        else if (split[0].equals("animPumpDelay"))
            model.pumpDelay = Integer.parseInt(split[1]);
        else if (split[0].equals("animPumpDelayAfterReload"))
            model.pumpDelayAfterReload = Integer.parseInt(split[1]);
        else if (split[0].equals("animPumpTime"))
            model.pumpTime = Integer.parseInt(split[1]);
        else if (split[0].equals("animHammerDelay"))
            model.hammerDelay = Integer.parseInt(split[1]);

        else if (split[0].equals("animPumpHandleDistance"))
            model.pumpHandleDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animEndLoadedAmmoDistance"))
            model.endLoadedAmmoDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animBreakActionAmmoDistance"))
            model.breakActionAmmoDistance = Float.parseFloat(split[1]);
        else if (split[0].equals("animScopeIsOnBreakAction"))
            model.scopeIsOnBreakAction = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animGripIsOnPump"))
            model.gripIsOnPump = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animGadgetsOnPump"))
            model.gripIsOnPump = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animBarrelBreakPoint"))
            model.barrelBreakPoint = parseVector3f(split);
        else if (split[0].equals("animAltBarrelBreakPoint"))
            model.altbarrelBreakPoint = parseVector3f(split);

        else if (split[0].equals("animRevolverFlipAngle"))
            model.revolverFlipAngle = Float.parseFloat(split[1]);
        else if (split[0].equals("animRevolver2FlipAngle"))
            model.revolver2FlipAngle = Float.parseFloat(split[1]);

        else if (split[0].equals("animRevolverFlipPoint"))
            model.revolverFlipPoint = parseVector3f(split);
        else if (split[0].equals("animRevolver2FlipPoint"))
            model.revolver2FlipPoint = parseVector3f(split);

        else if (split[0].equals("animBreakAngle"))
            model.breakAngle = Float.parseFloat(split[1]);
        else if (split[0].equals("animAltBreakAngle"))
            model.altbreakAngle = Float.parseFloat(split[1]);

        else if (split[0].equals("animSpinningCocking"))
            model.spinningCocking = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animSpinPoint"))
            model.spinPoint = parseVector3f(split);
        else if (split[0].equals("animHammerSpinPoint"))
            model.hammerSpinPoint = parseVector3f(split);
        else if (split[0].equals("animAltHammerSpinPoint"))
            model.althammerSpinPoint = parseVector3f(split);
        else if (split[0].equals("animHammerAngle"))
            model.hammerAngle = Float.parseFloat(split[1]);
        else if (split[0].equals("animAltHammerAngle"))
            model.althammerAngle = Float.parseFloat(split[1]);

        else if (split[0].equals("animIsSingleAction"))
            model.isSingleAction = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animSlideLockOnEmpty"))
            model.slideLockOnEmpty = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animLeftHandPump"))
            model.lefthandPump = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animRightHandPump"))
            model.righthandPump = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animLeftHandCharge"))
            model.leftHandCharge = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animRightHandCharge"))
            model.rightHandCharge = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animLeftHandBolt"))
            model.leftHandBolt = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animRightHandBolt"))
            model.rightHandBolt = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animPumpModifier"))
            model.pumpModifier = Float.parseFloat(split[1]);
        else if (split[0].equals("animChargeModifier"))
            model.chargeModifier = parseVector3f(split);
        else if (split[0].equals("animGunOffset"))
            model.gunOffset = Float.parseFloat(split[1]);
        else if (split[0].equals("animCrouchZoom"))
            model.crouchZoom = Float.parseFloat(split[1]);
        else if (split[0].equals("animFancyStance"))
            model.fancyStance = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animStanceTranslate"))
            model.stanceTranslate = parseVector3f(split);
        else if (split[0].equals("animStanceRotate"))
            model.stanceRotate = parseVector3f(split);

        else if (split[0].equals("animRotateGunVertical"))
            model.rotateGunVertical = Float.parseFloat(split[1]);
        else if (split[0].equals("animRotateGunHorizontal"))
            model.rotateGunHorizontal = Float.parseFloat(split[1]);
        else if (split[0].equals("animTiltGun"))
            model.tiltGun = Float.parseFloat(split[1]);
        else if (split[0].equals("animTranslateGun"))
            model.translateGun = parseVector3f(split);
        else if (split[0].equals("animRotateClipVertical"))
            model.rotateClipVertical = Float.parseFloat(split[1]);
        else if (split[0].equals("animStagedRotateClipVertical"))
            model.stagedrotateClipVertical = Float.parseFloat(split[1]);
        else if (split[0].equals("animRotateClipHorizontal"))
            model.rotateClipVertical = Float.parseFloat(split[1]);
        else if (split[0].equals("animStagedRotateClipHorizontal"))
            model.stagedrotateClipVertical = Float.parseFloat(split[1]);
        else if (split[0].equals("animTiltClip"))
            model.tiltClip = Float.parseFloat(split[1]);
        else if (split[0].equals("animStagedTiltClip"))
            model.stagedtiltClip = Float.parseFloat(split[1]);
        else if (split[0].equals("animTranslateClip"))
            model.translateClip = parseVector3f(split);
        else if (split[0].equals("animStagedTranslateClip"))
            model.stagedtranslateClip = parseVector3f(split);
        else if (split[0].equals("animStagedReload"))
            model.stagedReload = Boolean.parseBoolean(split[1]);

        else if (split[0].equals("animThirdPersonOffset"))
            model.thirdPersonOffset = parseVector3f(split);
        else if (split[0].equals("animItemFrameOffset"))
            model.itemFrameOffset = parseVector3f(split);
        else if (split[0].equals("animStillRenderGunWhenScopedOverlay"))
            model.stillRenderGunWhenScopedOverlay = Boolean.parseBoolean(split[1]);
        else if (split[0].equals("animAdsEffectMultiplier"))
            model.adsEffectMultiplier = Float.parseFloat(split[1]);
    }
	
	/**
     * Used only for driveables
     */
    public boolean isAmmo(ShootableType type) {
        return ammo.contains(type);
    }

    public boolean isAmmo(ShootableType type, ItemStack stack) {
        boolean result = ammo.contains(type);

        if (getGrip(stack) != null && getSecondaryFire(stack)) {
            List<ShootableType> t = new ArrayList<>();
            for (String s : getGrip(stack).secondaryAmmo) {
                ShootableType shoot = ShootableType.getShootableType(s);
                if (type != null)
                    t.add(shoot);
            }
            result = t.contains(type);
        }

        return result;
    }
    
    public Vector3f parseVector3f(String[] inp) {
        return new Vector3f(Float.parseFloat(inp[1]), Float.parseFloat(inp[2]), Float.parseFloat(inp[3]));
    }
    
    public boolean isAmmo(ItemStack stack) {
        if (stack == null)
            return false;
        else if (stack.getItem() instanceof ItemBullet) {
            return isAmmo(((ItemBullet) stack.getItem()).type, stack);
        } else if (stack.getItem() instanceof ItemGrenade) {
            return isAmmo(((ItemGrenade) stack.getItem()).type, stack);
        }
        return false;
    }
	
	public boolean isCorrectAmmo(ShootableType type)
	{
		return ammo.contains(type);
	}
	
	public boolean isCorrectAmmo(ItemStack stack)
	{
		if(stack == null || stack.isEmpty())
			return false;
		else if(stack.getItem() instanceof ItemBullet)
		{
			return isCorrectAmmo(((ItemBullet)stack.getItem()).type);
		}
		else if(stack.getItem() instanceof ItemGrenade)
		{
			return isCorrectAmmo(((ItemGrenade)stack.getItem()).type);
		}
		return false;
	}
	
	/**
	 * To be overriden by subtypes for model reloading
	 */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelGun.class);
		deployableModel = FlansMod.proxy.loadModel(deployableModelString, shortName, ModelMG.class);
        casingModel = FlansMod.proxy.loadModel(casingModelString, shortName, ModelCasing.class);
        flashModel = FlansMod.proxy.loadModel(flashModelString, shortName, ModelFlash.class);
        muzzleFlashModel = FlansMod.proxy.loadModel(muzzleFlashModelString, shortName, ModelMuzzleFlash.class);
	}
	
	@Override
	public float getZoomFactor()
	{
		return zoomLevel;
	}
	
	@Override
	public boolean hasZoomOverlay()
	{
		return hasScopeOverlay;
	}
	
	@Override
	public String getZoomOverlay()
	{
		return defaultScopeTexture;
	}
	
	@Override
	public float getFOVFactor()
	{
		return FOVFactor;
	}
	
	//ItemStack specific methods
	
	/**
	 * Return the currently active scope on this gun. Search attachments, and by default, simply give the gun
	 */
	public IScope getCurrentScope(ItemStack gunStack)
	{
		IScope attachedScope = getScope(gunStack);
		return attachedScope == null ? this : attachedScope;
	}
	
	/**
	 * Returns all attachments currently attached to the specified gun
	 */
	public ArrayList<AttachmentType> getCurrentAttachments(ItemStack gun)
	{
		checkForTags(gun);
		ArrayList<AttachmentType> attachments = new ArrayList<>();
		NBTTagCompound attachmentTags = gun.getTagCompound().getCompoundTag("attachments");
		NBTTagList genericsList = attachmentTags.getTagList("generics", (byte)10); //TODO : Check this 10 is correct
		for(int i = 0; i < numGenericAttachmentSlots; i++)
		{
			appendToList(gun, "generic_" + i, attachments);
		}
		appendToList(gun, "barrel", attachments);
		appendToList(gun, "scope", attachments);
		appendToList(gun, "stock", attachments);
		appendToList(gun, "grip", attachments);
		appendToList(gun, "gadget", attachments);
        appendToList(gun, "slide", attachments);
        appendToList(gun, "pump", attachments);
        appendToList(gun, "accessory", attachments);
		return attachments;
	}
	
	/**
	 * Private method for attaching attachments to a list of attachments with a nullcheck
	 */
	private void appendToList(ItemStack gun, String name, ArrayList<AttachmentType> attachments)
	{
		AttachmentType type = getAttachment(gun, name);
		if(type != null) attachments.add(type);
	}
	
	//Attachment getter methods
	public AttachmentType getBarrel(ItemStack gun)
	{
		return getAttachment(gun, "barrel");
	}
	
	public AttachmentType getScope(ItemStack gun)
	{
		return getAttachment(gun, "scope");
	}
	
	public AttachmentType getStock(ItemStack gun)
	{
		return getAttachment(gun, "stock");
	}
	
	public AttachmentType getGrip(ItemStack gun)
	{
		return getAttachment(gun, "grip");
	}
	
	public AttachmentType getGadget(ItemStack gun) {
        return getAttachment(gun, "gadget");
    }

    public AttachmentType getSlide(ItemStack gun) {
        return getAttachment(gun, "slide");
    }

    public AttachmentType getPump(ItemStack gun) {
        return getAttachment(gun, "pump");
    }

    public AttachmentType getAccessory(ItemStack gun) {
        return getAttachment(gun, "accessory");
    }
	
	public AttachmentType getGeneric(ItemStack gun, int i)
	{
		return getAttachment(gun, "generic_" + i);
	}
	
	//Attachment ItemStack getter methods
	public ItemStack getBarrelItemStack(ItemStack gun)
	{
		return getAttachmentItemStack(gun, "barrel");
	}
	
	public ItemStack getScopeItemStack(ItemStack gun)
	{
		return getAttachmentItemStack(gun, "scope");
	}
	
	public ItemStack getStockItemStack(ItemStack gun)
	{
		return getAttachmentItemStack(gun, "stock");
	}
	
	public ItemStack getGripItemStack(ItemStack gun)
	{
		return getAttachmentItemStack(gun, "grip");
	}
	
	public ItemStack getGadgetItemStack(ItemStack gun) {
        return getAttachmentItemStack(gun, "gadget");
    }

    public ItemStack getSlideItemStack(ItemStack gun) {
        return getAttachmentItemStack(gun, "slide");
    }

    public ItemStack getPumpItemStack(ItemStack gun) {
        return getAttachmentItemStack(gun, "pump");
    }

    public ItemStack getAccessoryItemStack(ItemStack gun) {
        return getAttachmentItemStack(gun, "accessory");
    }
	
	public ItemStack getGenericItemStack(ItemStack gun, int i)
	{
		return getAttachmentItemStack(gun, "generic_" + i);
	}
	
	/**
	 * Generalised attachment getter method
	 */
	public AttachmentType getAttachment(ItemStack gun, String name)
	{
		checkForTags(gun);
		return AttachmentType.getFromNBT(gun.getTagCompound().getCompoundTag("attachments").getCompoundTag(name));
	}
	
	/**
	 * Generalised attachment ItemStack getter method
	 */
	public ItemStack getAttachmentItemStack(ItemStack gun, String name)
	{
		checkForTags(gun);
		return new ItemStack(gun.getTagCompound().getCompoundTag("attachments").getCompoundTag(name));
	}
	
	/**
	 * Method to check for null tags and assign default empty tags in that case
	 */
	private void checkForTags(ItemStack gun)
	{
		//If the gun has no tags, give it some
		if(!gun.hasTagCompound())
		{
			gun.setTagCompound(new NBTTagCompound());
		}
		//If the gun has no attachment tags, give it some
		if(!gun.getTagCompound().hasKey("attachments"))
		{
			NBTTagCompound attachmentTags = new NBTTagCompound();
			for(int i = 0; i < numGenericAttachmentSlots; i++)
				attachmentTags.setTag("generic_" + i, new NBTTagCompound());
			attachmentTags.setTag("barrel", new NBTTagCompound());
			attachmentTags.setTag("scope", new NBTTagCompound());
			attachmentTags.setTag("stock", new NBTTagCompound());
			attachmentTags.setTag("grip", new NBTTagCompound());
			attachmentTags.setTag("gadget", new NBTTagCompound());
            attachmentTags.setTag("slide", new NBTTagCompound());
            attachmentTags.setTag("pump", new NBTTagCompound());
            attachmentTags.setTag("accessory", new NBTTagCompound());
			
			gun.getTagCompound().setTag("attachments", attachmentTags);
		}
	}
	
	/**
	 * Get the melee damage of a specific gun, taking into account attachments
	 */
	public float getMeleeDamage(ItemStack stack)
	{
		float stackMeleeDamage = meleeDamage;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackMeleeDamage *= attachment.meleeDamageMultiplier;
		}
		return stackMeleeDamage;
	}
	
	public float getMeleeDamage(ItemStack stack, boolean driveable) {
        float stackMeleeDamage = meleeDamage;
        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackMeleeDamage *= attachment.meleeDamageMultiplier;
        }
        return stackMeleeDamage * (driveable ? meleeDamageDriveableModifier : 1);
    }
	
	/**
	 * Get the damage of a specific gun, taking into account attachments
	 */
	public float getDamage(ItemStack stack)
	{
		float stackDamage = damage;
		
		if (getGrip(stack) != null && getSecondaryFire(stack))
            stackDamage = getGrip(stack).secondaryDamage;
		
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackDamage *= attachment.damageMultiplier;
		}
		return stackDamage;
	}
	
	/**
	 * Get the bullet spread of a specific gun, taking into account attachments
	 */
	public float getSpread(ItemStack stack)
	{
		float stackSpread = bulletSpread;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackSpread *= attachment.spreadMultiplier;
		}
		return stackSpread;
	}
	
	public float getSpread(ItemStack stack, boolean sneaking, boolean sprinting) {
        float stackSpread = bulletSpread;

        if (getGrip(stack) != null && getSecondaryFire(stack))
            stackSpread = getGrip(stack).secondarySpread;

        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackSpread *= attachment.spreadMultiplier;
        }
        if (sprinting) {
            stackSpread *= sprintSpreadMultiplier;
        } else if (sneaking) {
            stackSpread *= sneakSpreadMultiplier;
        }
        return stackSpread;
    }
	
	/**
     * Get the default spread of a specific gun, taking into account attachments
     */
    public float getDefaultSpread(ItemStack stack) {
        float stackSpread = defaultSpread;

        if (getGrip(stack) != null && getSecondaryFire(stack))
            stackSpread = getGrip(stack).secondaryDefaultSpread;

        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackSpread *= attachment.spreadMultiplier;
        }
        return stackSpread;
    }
    
    /**
     * Get the recoil of a specific gun, taking into account attachments
     */
    public float getRecoilPitch(ItemStack stack) {
        float stackRecoil = this.recoilPitch + (rand.nextFloat() * this.rndRecoilPitchRange);
        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackRecoil *= attachment.recoilMultiplier;
        }
        return stackRecoil;
    }
    
  //Used for displaying static recoil stats
    public float getRecoilDisplay(ItemStack stack) {
        float stackRecoil = this.recoilPitch;
        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackRecoil *= attachment.recoilMultiplier;
        }
        return stackRecoil;
    }

    public float getRecoilYaw(ItemStack stack) {
        float stackRecoilYaw = this.recoilYaw + ((rand.nextFloat() - 0.5F) * this.rndRecoilYawRange);
        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackRecoilYaw *= attachment.recoilMultiplier;
        }
        return stackRecoilYaw;
    }
	
	public EnumSpreadPattern getSpreadPattern(ItemStack stack)
	{
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			if(attachment.spreadPattern != null)
				return attachment.spreadPattern;
		}
		return spreadPattern;
	}
	
	/**
	 * Get the recoil of a specific gun, taking into account attachments
	 */
	public float getRecoil(ItemStack stack)
	{
		float stackRecoil = recoilPitch;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackRecoil *= attachment.recoilMultiplier;
		}
		return stackRecoil;
	}
	
	/**
     * Get the bullet speed of a specific gun, taking into account attachments
     */
    public float getBulletSpeed(ItemStack stack, ItemStack bulletStack) {
        float stackBulletSpeed;
        if (bulletStack != null && bulletStack.getItem() != null && bulletStack.getItem() instanceof ItemBullet) {
            stackBulletSpeed = bulletSpeed * ((ItemBullet) bulletStack.getItem()).type.speedMultiplier;
        } else {
            stackBulletSpeed = bulletSpeed;
        }

        if (getGrip(stack) != null && getSecondaryFire(stack))
            stackBulletSpeed = getGrip(stack).secondarySpeed;

        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackBulletSpeed *= attachment.bulletSpeedMultiplier;
        }
        return stackBulletSpeed;
    }
	/**
	 * Get the bullet speed of a specific gun, taking into account attachments
	 */
	public float getBulletSpeed(ItemStack stack)
	{
		float stackBulletSpeed = bulletSpeed;
		
		if (getGrip(stack) != null && getSecondaryFire(stack))
            stackBulletSpeed = getGrip(stack).secondarySpeed;
		
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackBulletSpeed *= attachment.bulletSpeedMultiplier;
		}
		return stackBulletSpeed;
	}
	
	/**
	 * Get the reload time of a specific gun, taking into account attachments
	 */
	public float getReloadTime(ItemStack stack)
	{
		float stackReloadTime = reloadTime;
		
		if (getGrip(stack) != null && getSecondaryFire(stack))
            stackReloadTime = getGrip(stack).secondaryReloadTime;
		
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackReloadTime *= attachment.reloadTimeMultiplier;
		}
		return stackReloadTime;
	}
	
	/**
     * Get the fire rate of a specific gun
     */
    public float getShootDelay(ItemStack stack) {
        //Legacy system input as direct ticks
        if (shootDelay != 0) {
            float fireRate = shootDelay;
            if (getGrip(stack) != null && getSecondaryFire(stack))
                fireRate = getGrip(stack).secondaryShootDelay;

            return fireRate;
        }
        //New system, input as RPM
        else {
            float fireRate = roundsPerMin;

            if (getGrip(stack) != null && getSecondaryFire(stack))
                fireRate = getGrip(stack).secondaryShootDelay;

            float fireTicks = 1200 / fireRate;

            return fireRate = fireTicks;
        }
    }
    
    public float getShootDelay() {
        if (shootDelay != 0) {
            return shootDelay;
        } else {
            return 1200 / roundsPerMin;
        }
    }
    
    /**
     * Get the number of bullets fired per shot of a specific gun
     */
    public int getNumBullets(ItemStack stack) {
        int amount = numBullets;

        if (getGrip(stack) != null && getSecondaryFire(stack))
            amount = getGrip(stack).secondaryNumBullets;

        return amount;
    }
    
    /**
     * Get the movement speed of a specific gun, taking into account attachments
     */
    public float getMovementSpeed(ItemStack stack) {
        float stackMovement = moveSpeedModifier;
        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            stackMovement *= attachment.moveSpeedMultiplier;
        }
        return stackMovement;
    }
    
    /**
     * Get the recoil counter coefficient of the gun, taking into account attachments
     */
    public float getRecoilControl(ItemStack stack, boolean isSprinting, boolean isSneaking) {
        float control;
        if (isSprinting) {
            control = recoilCounterCoefficientSprinting;
        } else if (isSneaking) {
            control = recoilCounterCoefficientSneaking;
        } else {
            control = recoilCounterCoefficient;
        }

        for (AttachmentType attachment : getCurrentAttachments(stack)) {
            if (isSprinting) {
                control *= attachment.recoilControlMultiplierSprinting;
            } else if (isSneaking) {
                control *= attachment.recoilControlMultiplierSneaking;
            } else {
                control *= attachment.recoilControlMultiplier;
            }
        }

        if (control > 1) {
            return 1;
        } else if (control < 0) {
            return 0;
        } else {
            return control;
        }
    }
    
    public void setFireMode(ItemStack stack, int fireMode) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        if (fireMode < EnumFireMode.values().length) {
            stack.getTagCompound().setByte("GunMode", (byte) fireMode);
        } else {
            stack.getTagCompound().setByte("GunMode", (byte) mode.ordinal());
        }
    }
	
	/**
	 * Get the firing mode of a specific gun, taking into account attachments
	 */
	public EnumFireMode getFireMode(ItemStack stack)
	{
		//Check for secondary fire mode
        if (getGrip(stack) != null && getSecondaryFire(stack))
            return getGrip(stack).secondaryFireMode;
		
        //Else check for any mode overrides from attachments
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			if(attachment.modeOverride != null)
				return attachment.modeOverride;
		}
		
		//Else set the fire mode from the gun
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("GunMode")) {
            int gm = stack.getTagCompound().getByte("GunMode");
            if (gm < EnumFireMode.values().length) {
                for (EnumFireMode enumFireMode : submode) {
                    if (gm == enumFireMode.ordinal()) {
                        return EnumFireMode.values()[gm];
                    }
                }
            }
        }
        
        setFireMode(stack, mode.ordinal());
        return mode;
	}
	
	/**
     * Set the secondary or primary fire mode
     */
    public void setSecondaryFire(ItemStack stack, boolean mode) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setBoolean("secondaryFire", mode);
    }
	
    /**
     * Get whether the gun is in secondary or primary fire mode
     */
    public boolean getSecondaryFire(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        if (!stack.getTagCompound().hasKey("secondaryFire")) {
            stack.getTagCompound().setBoolean("secondaryFire", false);
            return stack.getTagCompound().getBoolean("secondaryFire");
        }

        return stack.getTagCompound().getBoolean("secondaryFire");
    }

    /**
     * Get the max size of ammo items depending on what mode the gun is in
     */
    public int getNumAmmoItemsInGun(ItemStack stack) {
        if (getGrip(stack) != null && getSecondaryFire(stack))
            return getGrip(stack).numSecAmmoItems;
        else
            return numPrimaryAmmoItems;
    }
    
	public float GetShootDelay(ItemStack stack)
	{
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			if(attachment.modeOverride == EnumFireMode.BURST)
				return Math.max(shootDelay, 3);
		}
		
		float stackShootDelay = shootDelay;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackShootDelay *= attachment.shootDelayMultiplier;
		}
		return stackShootDelay;
	}
	
	/**
	 * Static String to GunType method
	 */
	public static GunType getGun(String s)
	{
		return guns.get(s.hashCode());
	}
	
	public static GunType getGun(int hash)
	{
		return guns.get(hash);
	}
	
	public Paintjob getPaintjob(String s) {
        for (Paintjob paintjob : paintjobs) {
            if (paintjob.iconName.equals(s))
                return paintjob;
        }
        return defaultPaintjob;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBase GetModel()
	{
		return model;
	}
	
	@Override
	public float GetRecommendedScale()
	{
		return 60.0f;
	}
	
	/**
	 * @return Returns the pumpDelayAfterReload if a model exits, otherwise 0
	 */
	public Integer getPumpDelayAfterReload()
	{
		if (model != null)
			return model.pumpDelayAfterReload;
		
		return 0;
	}
	
	/**
	 * @return Returns the pumpDelay if a model exits, otherwise 0
	 */
	public Integer getPumpDelay()
	{
		if (model != null)
			return model.pumpDelay;
		
		return 0;
	}
	
	/**
	 * @return the pump time if a model exits, otherwise 1
	 */
	public Integer getPumpTime()
	{
		if (model != null)
			return model.pumpTime;
		
		return 0;
	}
}
