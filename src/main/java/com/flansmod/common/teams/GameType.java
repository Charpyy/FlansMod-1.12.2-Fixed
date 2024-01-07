package com.flansmod.common.teams;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.flansmod.common.driveables.EntityDriveable;
import com.flansmod.common.driveables.EntityPlane;
import com.flansmod.common.driveables.EntityVehicle;
import com.flansmod.common.driveables.EnumPlaneMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.flansmod.common.FlansMod;
import com.flansmod.common.PlayerData;
import com.flansmod.common.PlayerHandler;
import com.flansmod.common.network.PacketBase;
import com.flansmod.common.types.InfoType;

public abstract class GameType
{
	public static HashMap<String, GameType> gametypes = new HashMap<>();
	public static TeamsManager teamsManager = TeamsManager.getInstance();
	public static Random rand = new Random();
	
	public static GameType getGametype(String type)
	{
		return gametypes.get(type);
	}
	
	public String name;
	public String shortName;
	public int numTeamsRequired;
	
	public GameType(String name, String shortName, int numTeams)
	{
		this.name = name;
		this.shortName = shortName;
		numTeamsRequired = numTeams;
		gametypes.put(this.shortName, this);
	}
	
	/**
	 * Called when a round starts
	 */
	
	public abstract void roundStart();
	
	/**
	 * Called when a round ends. (The point at which scoreboards are displayed)
	 */
	public abstract void roundEnd();
	
	/**
	 * Called when the scoreboards and voting are finished
	 */
	public abstract void roundCleanup();
	
	public abstract boolean teamHasWon(Team team);
	
	public void tick()
	{
	}
	
	public Team[] getTeamsCanSpawnAs(TeamsRound currentRound, EntityPlayer player)
	{
		return currentRound.teams;
	}
	
	public void playerJoined(EntityPlayerMP player)
	{
	}
	
	public void playerRespawned(EntityPlayerMP player)
	{
	}
	
	public void playerQuit(EntityPlayerMP player)
	{
	}
	
	//Return true if damage should be dealt.
	public boolean playerAttacked(EntityPlayerMP player, DamageSource source)
	{
		return true;
	}
	
	public void playerKilled(EntityPlayerMP player, DamageSource source)
	{
	}
	
	public void baseAttacked(ITeamBase base, DamageSource source)
	{
	}
	
	public void objectAttacked(ITeamObject object, DamageSource source)
	{
	}
	
	public void baseClickedByPlayer(ITeamBase base, EntityPlayerMP player)
	{
	}
	
	public void objectClickedByPlayer(ITeamObject object, EntityPlayerMP player)
	{
	}
	
	public boolean playerCanLoot(ItemStack stack, InfoType infoType, EntityPlayer player, Team playerTeam)
	{
		return true;
	}
	
	public abstract Vec3d getSpawnPoint(EntityPlayerMP player);
	
	//Return whether or not the variable exists
	public boolean setVariable(String variable, String value)
	{
		return false;
	}
	
	public abstract void readFromNBT(NBTTagCompound tags);
	
	public abstract void saveToNBT(NBTTagCompound tags);
	
	public boolean sortScoreboardByTeam()
	{
		return true;
	}
	
	public boolean showZombieScore()
	{
		return false;
	}
	
	/**
	 * Whether "attacker" can attack "victim"
	 */
	public boolean playerCanAttack(EntityPlayerMP attacker, Team attackerTeam, EntityPlayerMP victim, Team victimTeam)
	{
		return true;
	}
	
	/**
	 * Called when any entity is killed. This allows one to track mob deaths too
	 */
	public void entityKilled(Entity entity, DamageSource source)
	{
	}
	
	public void playerChoseTeam(EntityPlayerMP player, Team team, Team newTeam)
	{
	}
	
	public void playerChoseNewClass(EntityPlayerMP player, IPlayerClass playerClass)
	{
	}
	
	public void playerDefected(EntityPlayerMP player, Team team, Team newTeam)
	{
	}
	
	public void playerEnteredTheGame(EntityPlayerMP player, Team team, IPlayerClass playerClass)
	{
	}
	
	//--------------------------------------
	// Helper methods - Do not override
	//--------------------------------------
	
	public EntityPlayerMP getPlayer(String username)
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(username);
	}
	
	public static PlayerData getPlayerData(EntityPlayerMP player)
	{
		return PlayerHandler.getPlayerData(player);
	}
	
	public static void sendPacketToPlayer(PacketBase packet, EntityPlayerMP player)
	{
		FlansMod.getPacketHandler().sendTo(packet, player);
	}
	
	public static String[] getPlayerNames()
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames();
	}
	
	public static List<EntityPlayerMP> getPlayers()
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
	}
	
	public static void givePoints(EntityPlayerMP player, int points)
	{
		PlayerData data = getPlayerData(player);
		data.score += points;
		if(data.team != null)
			data.team.score += points;
	}
	
	public static EntityPlayerMP getPlayerFromDamageSource(DamageSource source)
	{
		EntityPlayerMP attacker = null;
		if(source instanceof EntityDamageSource)
		{
			if(source.getTrueSource() instanceof EntityPlayerMP)
				attacker = (EntityPlayerMP)source.getTrueSource();
		}
		if(source instanceof EntityDamageSourceIndirect)
		{
			if(source.getTrueSource() instanceof EntityPlayerMP)
				attacker = (EntityPlayerMP)source.getTrueSource();
		}
		return attacker;
	}
	
	public boolean shouldAutobalance()
	{
		return true;
	}

	public void vehicleDestroyed(EntityDriveable driveable2, EntityPlayerMP attacker){
		if (driveable2!=null) {
			if (attacker != null) {
				EntityDriveable driveable = driveable2;
//                if(driveable.riddenByEntity!=null &&
//                        driveable.riddenByEntity instanceof EntityPlayer &&
//                        !getPlayerData((EntityPlayerMP) driveable.riddenByEntity).team.equals(getPlayerData(attacker).team)) {
				if(true){ //this if() need for next changes
					getPlayerInfo(attacker).vehiclesDestroyed++;
					if (driveable instanceof EntityPlane) {
						EntityPlane plane = (EntityPlane) driveable;
						if (plane.mode == EnumPlaneMode.PLANE || plane.mode == EnumPlaneMode.VTOL) {
							getPlayerInfo(attacker).addExp(100);
							getPlayerInfo(attacker).savePlayerStats();
						} else if (plane.mode == EnumPlaneMode.HELI) {
							getPlayerInfo(attacker).addExp(75);
							getPlayerInfo(attacker).savePlayerStats();
						}
					} else if (driveable instanceof EntityVehicle) {
						EntityVehicle vehicle = (EntityVehicle) driveable;
						if (vehicle.getVehicleType().tank) {
							getPlayerInfo(attacker).addExp(75);
							getPlayerInfo(attacker).savePlayerStats();
						} else {
							getPlayerInfo(attacker).addExp(50);
							getPlayerInfo(attacker).savePlayerStats();
						}
					}
				}
			}
		}
	}

	public static PlayerStats getPlayerInfo(EntityPlayerMP player) {
		return PlayerHandler.getPlayerStats(player);
	}

}
