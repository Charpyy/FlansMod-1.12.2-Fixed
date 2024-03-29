package com.flansmod.client;

import net.minecraft.util.text.translation.I18n;

public enum FlanMouseButton {

	LEFT(0),
	RIGHT(1);

	private int buttonNumber;

	FlanMouseButton(int button)
	{
		buttonNumber = button;
	}

	public String getName()
	{
		return I18n.translateToLocal(String.format("firebutton.%s.name", name().toLowerCase()));
	}

	public int getButton()
	{
		return buttonNumber;
	}

	public static FlanMouseButton fromString(String input)
	{
		for(FlanMouseButton buttonType : values())
		{
			if(buttonType.name().equalsIgnoreCase(input))
			{
				return buttonType;
			}
		}
		return null;
	}

	@Override
	public String toString()
	{
		return name().toLowerCase();
	}

}
