package com.flansmod.common.driveables;

import com.flansmod.common.FlansMod;
import com.flansmod.common.network.PacketBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;


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
public class ArmorInvisible {
	private static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(FlansMod.MODID);

	public static void init() {
		NETWORK_WRAPPER.registerMessage(PacketSetPlayerInvisibility.Handler.class, PacketSetPlayerInvisibility.class, 10, Side.SERVER);
	}

	public static void setArmor(EntityPlayer player, boolean invisible) {
		if (!player.world.isRemote) {
			int playerId = player.getEntityId();
			NETWORK_WRAPPER.sendToServer(new PacketSetPlayerInvisibility(playerId, invisible));
		}
	}

	public static class PacketSetPlayerInvisibility implements IMessage {
		private int playerId;
		private boolean invisible;

		public PacketSetPlayerInvisibility() {
		}

		public PacketSetPlayerInvisibility(int playerId, boolean invisible) {
			this.playerId = playerId;
			this.invisible = invisible;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(playerId);
			buf.writeBoolean(invisible);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			playerId = buf.readInt();
			invisible = buf.readBoolean();
		}

		public static class Handler implements IMessageHandler<PacketSetPlayerInvisibility, IMessage> {
			@Override
			public IMessage onMessage(PacketSetPlayerInvisibility message, MessageContext ctx) {
				FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
					MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
					PlayerList playerList = server.getPlayerList();
					EntityPlayer player = playerList.getPlayerByUUID(ctx.getServerHandler().player.getServer().getWorld(0).getEntityByID(message.playerId).getUniqueID());
					if (player != null) {
						player.setInvisible(message.invisible);
					}
				});
				return null;
			}
		}
	}
}
