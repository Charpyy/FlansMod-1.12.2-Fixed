package com.flansmod.common.network;

import com.flansmod.common.FlansMod;
import com.flansmod.common.driveables.EntitySeat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSeatCheck extends PacketBase 
{
	public int entityId;
	public int checkCount;
	
	public PacketSeatCheck() {}

	public PacketSeatCheck(EntitySeat seat)
	{
		entityId = seat.getEntityId();
		checkCount = 3;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) 
	{
		data.writeInt(entityId);
		data.writeInt(checkCount);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data)
	{
		entityId = data.readInt();
		checkCount = data.readInt();
	}

	@Override
	public void handleServerSide(EntityPlayerMP playerEntity) 
	{
		log("handleServerSide", playerEntity);
		
		if(checkCount <= 0)
		{
			TextComponentString cct1 = new TextComponentString("[FlansMod] "+playerEntity.getDisplayName()+" was recovering from a fall. id=" + entityId);
			cct1.getStyle().setColor(TextFormatting.YELLOW);
			TextComponentString cct2 = new TextComponentString("[FlansMod]================================================");
			cct2.getStyle().setColor(TextFormatting.RED);
			
/*
			Iterator iterator = MinecraftServer.getServer().getConfigurationManager().playerEntityList.iterator();

			while (iterator.hasNext())
			{
				EntityPlayer entityplayer = (EntityPlayer)iterator.next();
				entityplayer.addChatMessage(cct2);
				entityplayer.addChatMessage(cct1);
				entityplayer.addChatMessage(cct2);
			}
*/
		}
		else
		{
			if(playerEntity.getRidingEntity() instanceof EntitySeat)
			{
				entityId = playerEntity.getRidingEntity().getEntityId();
			}
			else
			{
				entityId = -1;
			}
			FlansMod.getPacketHandler().sendTo(this, playerEntity);
		}
	}

	private void log(String s, EntityPlayer player)
	{
		Entity re = player.getRidingEntity();
		FlansMod.log(s +" :"+player.getDisplayName()+
				" : rideEntity="+(re!=null? re.getClass().getName(): re)+
				" : seatEntityId="+entityId+
				" : check="+checkCount);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleClientSide(EntityPlayer clientPlayer) 
	{
		//log("handleClientSide", clientPlayer);
		
		if(clientPlayer.getRidingEntity()==null && entityId != -1)
		{
			if(checkCount > 1)
			{
				checkCount--;
			}
			else
			{
				checkCount--;
				Entity entity = clientPlayer.world.getEntityByID(entityId);
				if(entity instanceof EntitySeat)
				{
					//FlansMod.log("mount seat :"+clientPlayer.getDisplayName()+
							//" : seatEntityId="+entityId+
							//" : check="+checkCount);

					clientPlayer.startRiding(entity);
				}
			}
			FlansMod.getPacketHandler().sendToServer(this);
		}
	}
}
