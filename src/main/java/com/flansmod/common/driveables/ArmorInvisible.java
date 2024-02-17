package com.flansmod.common.driveables;

import com.flansmod.common.FlansMod;
import com.flansmod.common.network.PacketBase;
import com.flansmod.common.network.PacketHandler;
import com.sun.org.apache.xalan.internal.xsltc.runtime.MessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Method;
import java.nio.channels.NetworkChannel;


//public class ArmorInvisible {
//	public static class EventHandler {
//
//		private static final Set<EntityLivingBase> invisibleEntities = new HashSet<>();
//
//		@SideOnly(Side.CLIENT)
//		@SubscribeEvent
//		public static void onRenderPlayer(RenderLivingEvent.Pre<EntityLivingBase> event) {
//			EntityLivingBase entity = event.getEntity();
//			if (entity != null && invisibleEntities.contains(entity)) {
//				event.setCanceled(true);
//			}
//		}
//
//		public static void setArmor(EntityLivingBase entity, boolean invisible) {
//			if (invisible) {
//				invisibleEntities.add(entity);
//			} else {
//				invisibleEntities.remove(entity);
//			}
//		}
//	}
//}
//public class ArmorInvisible {
//	public static class PacketSetPlayerInvisibility extends PacketBase {
//		private int playerId;
//		private boolean invisible;
//
//		public PacketSetPlayerInvisibility() {}
//
//		public PacketSetPlayerInvisibility(int playerId, boolean invisible) {
//			this.playerId = playerId;
//			this.invisible = invisible;
//		}
//
//		@Override
//		public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
//			data.writeInt(playerId);
//			data.writeBoolean(invisible);
//		}
//
//		@Override
//		public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
//			playerId = data.readInt();
//			invisible = data.readBoolean();
//		}
//
//		@Override
//		public void handleServerSide(EntityPlayerMP playerEntity) {
//			EntityPlayer player = playerEntity.getServerWorld().getPlayerEntityByUUID(playerEntity.getUniqueID());
//			if (player != null) {
//				player.setInvisible(invisible);
//			}
//		}
//
//		@Override
//		@SideOnly(Side.CLIENT)
//		public void handleClientSide(EntityPlayer clientPlayer) {
//		}
//	}
//
//	public static void setArmor(EntityPlayer player, boolean invisible) {
//		int playerId = player.getEntityId();
//		PacketSetPlayerInvisibility packet = new PacketSetPlayerInvisibility(playerId, invisible);
//		FlansMod.getPacketHandler().sendToServer(packet);
//	}
//}
public class ArmorInvisible extends PacketHandler{
	public static void setArmor(EntityPlayer player, boolean invisible) {
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			PacketSetPlayerRender packet = new PacketSetPlayerRender(player.getEntityId(), invisible);
			FlansMod.getPacketHandler().sendTo(packet, playerMP);
		}
	}

	public static class PacketSetPlayerRender implements IMessage {
		private int playerId;
		private boolean shouldRender;

		public PacketSetPlayerRender() {}

		public PacketSetPlayerRender(int playerId, boolean shouldRender) {
			this.playerId = playerId;
			this.shouldRender = shouldRender;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			playerId = buf.readInt();
			shouldRender = buf.readBoolean();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(playerId);
			buf.writeBoolean(shouldRender);
		}

		public static class Handler implements IMessageHandler<PacketSetPlayerRender, IMessage> {
			@Override
			public IMessage onMessage(PacketSetPlayerRender message, MessageContext ctx) {
				EntityPlayer player = ctx.getServerHandler().player;
				Entity entity = player.world.getEntityByID(message.playerId);
				if (entity != null) {
					entity.setInvisible(message.shouldRender);
				}
				return null;
			}
		}
	}
}
