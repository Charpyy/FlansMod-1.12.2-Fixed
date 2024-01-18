package com.flansmod.common.guns;

import javax.annotation.Nullable;

import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.network.PacketKillMessage;
import com.flansmod.common.teams.Team;
import com.flansmod.common.types.InfoType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class EntityDamageSourceFlan extends EntityDamageSourceIndirect{
	
	private InfoType weapon;
	private EntityPlayer shooter;
	private boolean headshot;
	private boolean melee;
	/**
	 * @param s        Name of the damage source (Usually the shortName of the gun)
	 * @param entity   The Entity causing the damage (e.g. Grenade). Can be the same as 'player'
	 * @param player   The Player responsible for the damage
	 * @param wep      The InfoType of weapon used
	 */	
	public EntityDamageSourceFlan(String s, Entity entity, EntityPlayer player, InfoType wep)
	{
		this(s, entity, player, wep, false, false);
	}
	
	/**
	 * @param s        Name of the damage source (Usually the shortName of the gun)
	 * @param entity   The Entity causing the damage (e.g. Grenade). Can be the same as 'player'
	 * @param player   The Player responsible for the damage
	 * @param wep      The InfoType of weapon used
	 * @param headshot True if this was a headshot, false if not
	 */
	public EntityDamageSourceFlan(String s, Entity entity, EntityPlayer player, InfoType wep, boolean headshot, boolean melee)
	{
		super(s, entity, player);
		weapon = wep;
		shooter = player;
		this.headshot = headshot;
		this.melee = melee;
	}
	
	@Override
	public ITextComponent getDeathMessage(EntityLivingBase living)
	{
		if(!(living instanceof EntityPlayer) || shooter == null || PlayerHandler.getPlayerData(shooter) == null)
		{
			if(shooter == null)
			{
				return new TextComponentString(living.getName() + " was shot");
			}
			else return new TextComponentString(living.getName() + " was shot by " + shooter.getName());
		}

		EntityPlayer player = (EntityPlayer) living;
		Team killedTeam = PlayerHandler.getPlayerData(player).team;
		Team killerTeam = PlayerHandler.getPlayerData(shooter).team;

		float dist = player.getDistance(shooter);
		if (FlansMod.enableKillMessages) {
			FlansMod.getPacketHandler().sendToDimension(
					new PacketKillMessage(headshot, weapon, shooter.getHeldItem(EnumHand.MAIN_HAND).getItemDamage(),
							((killedTeam == null ? "f" : killedTeam.textColour) + player.getName()),
							((killerTeam == null ? "f" : killerTeam.textColour) + shooter.getName()), dist
					), living.dimension);
		}
		else{
			return null;
		}
		String killMessage =
				TextFormatting.DARK_GRAY + "[" + TextFormatting.RED + "Flansmod" + TextFormatting.DARK_GRAY + "] " +
						TextFormatting.ITALIC + TextFormatting.DARK_RED + player.getName() + TextFormatting.RESET +
						TextFormatting.GRAY + " Was killed by " + TextFormatting.ITALIC + TextFormatting.DARK_GREEN +
						shooter.getName() +
						(FlansMod.showDistanceInKillMessage ? "" + TextFormatting.RESET + TextFormatting.GRAY +
								" from " + TextFormatting.ITALIC + TextFormatting.DARK_AQUA +
								String.format("%.1f", dist) + "m" + TextFormatting.RESET + TextFormatting.GRAY +
								" away" : "");
		return new TextComponentString(FlansMod.enableKillMessages ? killMessage : "");
	}
	
	/**
	 * @return The weapon (InfoType) used to cause this damage
	 */
	public InfoType getWeapon()
	{
		return weapon;
	}
	
	/**
	 * @return The Player responsible for this damage
	 */
	public EntityPlayer getCausedPlayer()
	{
		return shooter;
	}
	
	/**
	 * @return True if this is a headshot, false if not
	 */
	public boolean isHeadshot()
	{
		return headshot;
	}

	public boolean isMelee()
	{
		return melee;
	}

	public Entity getDamageSourceEntity() {
		return this.damageSourceEntity;
	}
	
	@Override
    @Nullable
    public Vec3d getDamageLocation()
    {
		if(damageSourceEntity == null)
			return new Vec3d(0d, 0d, 0d);
        return super.getDamageLocation();
    }
}
