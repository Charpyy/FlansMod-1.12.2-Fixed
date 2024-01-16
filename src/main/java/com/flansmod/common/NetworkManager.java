package com.flansmod.common;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkManager {

	private static SimpleNetworkWrapper networkChannel;

	public static SimpleNetworkWrapper getNetworkChannel() {
		if (networkChannel == null) {
			networkChannel = NetworkRegistry.INSTANCE.newSimpleChannel("typeww2");
			networkChannel.registerMessage(GenericMessage.Handle.class, GenericMessage.class, 80, Side.CLIENT);
		}
		return networkChannel;
	}
}
