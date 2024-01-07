package com.flansmod.common.sync;

import com.flansmod.common.network.PacketHashSend;
import com.flansmod.common.FlansMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.server.MinecraftServer;

public class SyncEventHandler {
    @SubscribeEvent
    public void playerJoined(PlayerLoggedInEvent event) {
        MinecraftServer mc = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (mc.isDedicatedServer() && event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            FlansMod.getPacketHandler().sendTo(new PacketHashSend(Sync.cachedHash), player);
        }
    }
}
