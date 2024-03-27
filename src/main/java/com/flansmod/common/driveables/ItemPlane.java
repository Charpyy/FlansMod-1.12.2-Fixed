package com.flansmod.common.driveables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.flansmod.common.teams.TeamsManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSponge;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import com.flansmod.common.FlansMod;
import com.flansmod.common.paintjob.IPaintableItem;
import com.flansmod.common.paintjob.PaintableType;
import com.flansmod.common.parts.PartType;
import com.flansmod.common.types.EnumType;
import com.flansmod.common.types.InfoType;

public class ItemPlane extends Item implements IPaintableItem
{
	public PlaneType type;
	
	public ItemPlane(PlaneType type1)
	{
		maxStackSize = 1;
		type = type1;
		type.item = this;
		setRegistryName(type.shortName);
		setCreativeTab(FlansMod.tabFlanDriveables);
	}
	
	@Override
	/** Make sure client and server side NBTtags update */
	public boolean getShareTag()
	{
		return true;
	}
	
	private NBTTagCompound getTagCompound(ItemStack stack, World world)
	{
		if(stack.getTagCompound() == null)
		{
			if(!world.isRemote)
				stack.setTagCompound(getOldTagCompound(stack, world));
			if(stack.getTagCompound() == null)
			{
				NBTTagCompound tags = new NBTTagCompound();
				stack.setTagCompound(tags);
				tags.setString("Type", type.shortName);
				tags.setString("Engine", PartType.defaultEngines.get(EnumType.plane).shortName);
			}
		}
		return stack.getTagCompound();
	}
	
	private NBTTagCompound getOldTagCompound(ItemStack stack, World world)
	{
		try
		{
			File file1 = world.getSaveHandler().getMapFileFromName("plane_" + stack.getItemDamage());
			if(file1 != null && file1.exists())
			{
				FileInputStream fileinputstream = new FileInputStream(file1);
				NBTTagCompound tags = CompressedStreamTools.readCompressed(fileinputstream).getCompoundTag("data");
				for(EnumDriveablePart part : EnumDriveablePart.values())
				{
					tags.setFloat(part.getShortName() + "_Health", type.health.get(part) == null ? 0 : (int) type.health.get(part).health);
					tags.setBoolean(part.getShortName() + "_Fire", false);
				}
				fileinputstream.close();
				return tags;
			}
		}
		catch(IOException e)
		{
			FlansMod.log.error("Failed to read old vehicle file");
			FlansMod.log.throwing(e);
		}
		return null;
	}
	
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag b)
	{
		String paintName = type.getPaintjob(stack.getItemDamage()).displayName;
		if (!paintName.equals("default") && !paintName.isEmpty())
			lines.add("\u00a7b\u00a7o" + paintName);

		if (!type.packName.isEmpty()) {
			lines.add("\u00a7o" + type.packName);
		}
		if (type.description != null) {
			Collections.addAll(lines, type.description.split("_"));
		}

		lines.add("");
		NBTTagCompound tags = getTagCompound(stack, world);
		PartType engine = PartType.getPart(tags.getString("Engine"));
		if (engine != null)
			lines.add("\u00a79Engine" + "\u00a77: " + engine.name);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, EnumHand hand)
	{
		ItemStack itemstack = entityplayer.getHeldItem(hand);
		if (!(TeamsManager.survivalCanPlaceVehicles || entityplayer.capabilities.isCreativeMode)) {
			// player isn't allowed to place vehicles.
			return new ActionResult<>(EnumActionResult.PASS, itemstack);
		}
		
		//Raytracing
		float cosYaw = MathHelper.cos(-entityplayer.rotationYaw * 0.01745329F - 3.141593F);
		float sinYaw = MathHelper.sin(-entityplayer.rotationYaw * 0.01745329F - 3.141593F);
		float cosPitch = -MathHelper.cos(-entityplayer.rotationPitch * 0.01745329F);
		float sinPitch = MathHelper.sin(-entityplayer.rotationPitch * 0.01745329F);
		double length = 5D;
		Vec3d posVec = new Vec3d(entityplayer.posX, entityplayer.posY + 1.62D - entityplayer.getYOffset(), entityplayer.posZ);
		Vec3d lookVec = posVec.add(sinYaw * cosPitch * length, sinPitch * length, cosYaw * cosPitch * length);
		RayTraceResult RayTraceResult = world.rayTraceBlocks(posVec, lookVec, type.placeableOnWater);
		
		//Result check
		if(RayTraceResult == null)
		{
			return new ActionResult<>(EnumActionResult.PASS, itemstack);
		}
		if(RayTraceResult.typeOfHit == Type.BLOCK)
		{
			BlockPos pos = RayTraceResult.getBlockPos();
			Block block = world.getBlockState(pos).getBlock();
			if (type.placeableOnLand || block instanceof BlockLiquid || (type.placeableOnSponge && block instanceof BlockSponge))
			{
				if(!world.isRemote)
				{
					DriveableData data = getPlaneData(itemstack, world);
					if(data != null)
					{
						EntityPlane plane = new EntityPlane(world, (double)pos.getX() + 0.5F, (double)pos.getY() + 2.5F, (double)pos.getZ() + 0.5F, entityplayer, type, data);
						if (TeamsManager.vehiclepin) {
							plane.getEntityData().setString("Owner", entityplayer.getName());
							entityplayer.sendMessage(new TextComponentString("\u00a78\u00bb \u00a77You are the \u00a7fOwner \u00a77of this plane, do: \u00a7b/vehicle add <player>"));
						}
						world.spawnEntity(plane);
						if (!world.isRemote)
						{
							//FlansMod.log.info("Player %s placed plane %s (%d) at (%d, %d, %d)", entityplayer.getDisplayName(), type.shortName, plane.getEntityId(), pos.getX(), pos.getY(), pos.getZ());
						}
					}
				}
				if(!entityplayer.capabilities.isCreativeMode)
				{
					itemstack.setCount(itemstack.getCount() - 1);
				}
				
				return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
			}
		}
		return new ActionResult<>(EnumActionResult.PASS, itemstack);
	}
	
	public Entity spawnPlane(World world, double x, double y, double z, ItemStack stack)
	{
		DriveableData data = getPlaneData(stack, world);
		if(data != null)
		{
			Entity entity = new EntityPlane(world, x, y, z, type, data, null);
			if(!world.isRemote)
			{
				world.spawnEntity(entity);
			}
			return entity;
		}
		return null;
	}
	
	public DriveableData getPlaneData(ItemStack itemstack, World world)
	{
		return new DriveableData(getTagCompound(itemstack, world), itemstack.getItemDamage());
	}
	
	/**
	 * Make sure that creatively spawned planes have nbt data
	 */
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if(tab != FlansMod.tabFlanDriveables && tab != CreativeTabs.SEARCH)
			return;
		
		ItemStack planeStack = new ItemStack(this, 1, 0);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setString("Type", type.shortName);
		if(PartType.defaultEngines.containsKey(EnumType.plane))
			tags.setString("Engine", PartType.defaultEngines.get(EnumType.plane).shortName);
		for(EnumDriveablePart part : EnumDriveablePart.values())
		{
			tags.setFloat(part.getShortName() + "_Health", type.health.get(part) == null ? 0 : (int) type.health.get(part).health);
			tags.setBoolean(part.getShortName() + "_Fire", false);
		}
		planeStack.setTagCompound(tags);
		items.add(planeStack);
	}
	
	@Override
	public InfoType getInfoType()
	{
		return type;
	}
	
	@Override
	public PaintableType GetPaintableType()
	{
		return type;
	}
}
