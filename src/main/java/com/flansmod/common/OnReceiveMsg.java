package com.flansmod.common;

public class OnReceiveMsg {

	public static void receive(String msg) {
		String[] data = msg.split(";");
		for (String text : data) {
			System.out.println(text);
		}
	}
}
