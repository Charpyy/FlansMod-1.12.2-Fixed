package com.flansmod.common;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GenericMessage implements IMessage {

	public static class Handle implements IMessageHandler<GenericMessage, IMessage> {

		@Override
		public IMessage onMessage(GenericMessage message, MessageContext ctx) {
			OnReceiveMsg.receive(message.getMessage());
			return null;
		}
	}

	String s;

	public GenericMessage() {

	}

	public GenericMessage(String s){
		this.s = s;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		s = buf.toString(CharsetUtil.UTF_8);
	}

	public String getMessage() {
		return s;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf,s);
	}
}
