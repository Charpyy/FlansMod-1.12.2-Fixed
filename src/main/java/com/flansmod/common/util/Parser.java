package com.flansmod.common.util;

public class Parser
{
	private Parser() {}

	public static int parseInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException exception)
		{
			return 0;
		}
	}

	public static float parseFloat(String s)
	{
		try
		{
			return Float.parseFloat(s);
		}
		catch (NumberFormatException exception)
		{
			return 0F;
		}
	}
}
