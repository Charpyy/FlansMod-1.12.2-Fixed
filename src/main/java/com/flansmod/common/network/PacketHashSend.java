package com.flansmod.common.network;

import com.flansmod.common.FlansMod;
import com.flansmod.common.sync.Sync;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public class PacketHashSend extends PacketBase {
    String hash;

    @SuppressWarnings("unused")
	public PacketHashSend() {
    }

    public PacketHashSend(String typeHash) {
        hash = typeHash;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        writeUTF(data, hash);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        hash = readUTF(data);
    }

    @Override
    public void handleServerSide(EntityPlayerMP player) {
        FlansMod.log("Received pack hash from %s (%s)", player.getName(), hash);
        if (!hash.equals(Sync.cachedHash) && FlansMod.kickNonMatchingHashes) {
            player.connection.disconnect(new TextComponentString("[Sync] Client-server mismatch."));
        }
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        if (FlansMod.printDebugLog) {
            FlansMod.log.info("Received packet %s", hash);
        }
        if (!hash.equals(Sync.cachedHash) && FlansMod.kickNonMatchingHashes) {
            clientPlayer.sendMessage(new TextComponentString("[Sync] Client-Server mismatch detected."));
            FlansMod.log.info("Kicked from server, invalid hash. Make sure your packs are the same as the server's.");
            FlansMod.log.info("S: " + hash);
            FlansMod.log.info("C: " + Sync.cachedHash);
        }
        FlansMod.getPacketHandler().sendToServer(new PacketHashSend(Sync.getUnifiedHash()));
    }
}
