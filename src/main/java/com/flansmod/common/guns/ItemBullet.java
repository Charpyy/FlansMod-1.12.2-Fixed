package com.flansmod.common.guns;

import java.util.Collections;
import java.util.List;

import com.flansmod.common.driveables.EnumWeaponType;
import com.flansmod.common.types.IGunboxDescriptionable;
import com.flansmod.common.vector.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import com.flansmod.common.FlansMod;
import com.flansmod.common.types.IFlanItem;
import com.flansmod.common.types.InfoType;

/**
 * Implemented from old source.
 */
public class ItemBullet extends ItemShootable implements IFlanItem, IGunboxDescriptionable
{
	public BulletType type;

	public String originGunbox = "";

	public String getOriginGunBox() { return originGunbox; }
	public void setOriginGunBox(String e) { originGunbox = e; }
	
	public ItemBullet(BulletType infoType)
	{
		super(infoType);
		type = infoType;
		setMaxStackSize(type.maxStackSize);
		setHasSubtypes(true);
		type.item = this;
		switch(type.weaponType)
		{
			case SHELL: case BOMB: case MINE: case MISSILE:
				setCreativeTab(FlansMod.tabFlanDriveables);
				break;
			default: setCreativeTab(FlansMod.tabFlanGuns);
		}
	}

	public boolean isRepairable() {
		return canRepair;
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag b)
	{
		if (type.fancyDescription && FlansMod.showItemDescriptions) {
			KeyBinding shift = Minecraft.getMinecraft().gameSettings.keyBindSneak;

			if (!type.packName.isEmpty()) {
				lines.add("\u00a7o" + type.packName);
			}
			if (type.description != null) {
				Collections.addAll(lines, type.description.split("_"));
			}
			//Reveal all the bullet stats when holding down the sneak key
			if (!GameSettings.isKeyDown(shift)) {
				lines.add("Hold \u00a7b\u00a7o" + GameSettings.getKeyDisplayString(shift.getKeyCode()) + "\u00a7r\u00a77 for details");
			} else {
				lines.add("");
				if (originGunbox != "") {
					lines.add("\u00a79Box" + "\u00a77: " + originGunbox);
				}
				lines.add("\u00a79Damage" + "\u00a77: " + roundFloat(type.damageVsLiving, 2));
				lines.add("\u00a79Penetration" + "\u00a77: " + roundFloat(type.penetratingPower, 2));
				lines.add("\u00a79Rounds" + "\u00a77: " + type.roundsPerItem);
				lines.add("\u00a79Fall Speed" + "\u00a77: " + roundFloat(type.fallSpeed, 2));

				if (type.explosionRadius > 0) {
					lines.add("\u00a79Explosion Radius" + "\u00a77: " + roundFloat(type.explosionRadius, 2));
					lines.add("\u00a79Explosion Power" + "\u00a77: " + roundFloat(type.explosionPower, 2));
				}
				if (type.numBullets > -1) {
					lines.add("\u00a79Shot" + "\u00a77: " + type.numBullets);
				}

				if (type.bulletSpread > -1) {
					lines.add("\u00a79Spread" + "\u00a77: " + type.bulletSpread);
				}
				if (type.lockOnToLivings || type.lockOnToMechas || type.lockOnToPlanes || type.lockOnToPlayers || type.lockOnToVehicles) {
					lines.add("\u00a79Guidance:" + "\u00a77: " + "LockOn");
				} else if (type.manualGuidance) {
					lines.add("\u00a79Guidance:" + "\u00a77: " + "Manual");
				} else if (type.laserGuidance) {
					lines.add("\u00a79Guidance:" + "\u00a77: " + "Laser");
				} else if (type.weaponType == EnumWeaponType.MISSILE) {
					lines.add("\u00a79Guidance:" + "\u00a77: " + "Unguided");
				}

				lines.add("");

			}
		} else {
			if (!type.packName.isEmpty()) {
				lines.add(type.packName);
			}
			if (type.description != null) {
				Collections.addAll(lines, type.description.split("_"));
			}
		}
	}

	//Can be overriden to allow new types of bullets to be created, for planes
	public EntityShootable getEntity(World worldObj, Vec3d origin, float yaw,
									 float pitch, double motionX, double motionY, double motionZ,
									 EntityLivingBase shooter, float gunDamage, int itemDamage, InfoType shotFrom)
	{
		FireableGun gun = new FireableGun(shotFrom, gunDamage, 1, (float) Math.sqrt(motionX*motionX + motionY*motionY + motionZ*motionZ), EnumSpreadPattern.circle);
		FiredShot shot = new FiredShot(gun, type, shooter);
		return new EntityBullet(worldObj, shot, origin, new Vec3d(motionX, motionY, motionZ));
	}

	//Can be overriden to allow new types of bullets to be created, vector constructor
	public EntityShootable getEntity(World worldObj, Vector3f origin, Vector3f direction,
									 EntityLivingBase shooter, float spread, float damage, float speed, int itemDamage, InfoType shotFrom)
	{
		FireableGun gun = new FireableGun(shotFrom, damage, spread, speed, EnumSpreadPattern.circle);
		FiredShot shot = new FiredShot(gun, type, shooter);
		return new EntityBullet(worldObj, shot, origin.toVec3(), direction.toVec3());
	}

	//Can be overriden to allow new types of bullets to be created, AA/MG constructor
	public EntityShootable getEntity(World worldObj, Vec3d origin, float yaw,
									 float pitch, EntityLivingBase shooter, float spread, float damage, float speed,
									 int itemDamage, InfoType shotFrom)
	{
		double motionX = -MathHelper.sin((yaw / 180F) * 3.14159265F) * MathHelper.cos((pitch/ 180F) * 3.14159265F);
		double motionZ = MathHelper.cos((yaw / 180F) * 3.14159265F) * MathHelper.cos((pitch / 180F) * 3.14159265F);
		double motionY = -MathHelper.sin((pitch / 180F) * 3.141593F);
		FireableGun gun = new FireableGun(shotFrom, damage, spread, speed, EnumSpreadPattern.circle);
		FiredShot shot = new FiredShot(gun, type, shooter);
		return new EntityBullet(worldObj, shot, origin, new Vec3d(motionX, motionY, motionZ));
	}

	//Can be overriden to allow new types of bullets to be created, Handheld constructor
	public EntityShootable getEntity(World worldObj, EntityLivingBase player,
									 float bulletSpread, float damage, float bulletSpeed, boolean b,
									 int itemDamage, InfoType shotFrom)
	{
		double motionX = -MathHelper.sin((player.rotationYaw / 180F) * 3.14159265F) * MathHelper.cos((player.rotationPitch / 180F) * 3.14159265F);
		double motionZ = MathHelper.cos((player.rotationYaw / 180F) * 3.14159265F) * MathHelper.cos((player.rotationPitch / 180F) * 3.14159265F);
		double motionY = -MathHelper.sin((player.rotationPitch / 180F) * 3.141593F);
		FireableGun gun = new FireableGun(shotFrom, damage, bulletSpread, bulletSpeed, EnumSpreadPattern.circle);
		FiredShot shot = new FiredShot(gun, type, player);
		return new EntityBullet(worldObj, shot, new Vec3d(player.posX, player.posY, player.posZ), new Vec3d(motionX, motionY, motionZ));
	}
	
	@Override
	public InfoType getInfoType()
	{
		return type;
	}

	public static float roundFloat(float value, int points) {
		int pow = 10;
		for (int i = 1; i < points; i++)
			pow *= 10;
		float result = value * pow;

		return (float) (int) ((result - (int) result) >= 0.5f ? result + 1 : result) / pow;
	}
}
