package com.flansmod.common.driveables;

import com.flansmod.common.vector.Vector3f;

public class CollisionBox
{
	/**
	 * The origin in model co-ordinates
	 */
	public float x, y, z;
	/**
	 * The dimensions in model co-ordinates
	 */
	public float w, h, d;
	/**
	 * The health of the part this box is attached to
	 */
	public float health;
	/**
	 * The part this box is attached to
	 */
	public EnumDriveablePart part;

	/** The resistance of the part to penetration by bullets */
	public float penetrationResistance = 5;
	
	public CollisionBox(float health, int x, int y, int z, int w, int h, int d)
	{
		this.health = health;
		this.x = x / 16F;
		this.y = y / 16F;
		this.z = z / 16F;
		this.w = w / 16F;
		this.h = h / 16F;
		this.d = d / 16F;
	}

	public CollisionBox(float health, int x, int y, int z, int w, int h, int d, float penResistance)
	{
		this(health, x, y, z, w, h, d);
		penetrationResistance = penResistance;
	}

	
	public double Max()
	{
		double xMax = Math.max(x, x + w);
		double yMax = Math.max(y, y + h);
		double zMax = Math.max(z, z + d);
		return Math.sqrt(xMax * xMax + yMax * yMax + zMax * zMax);
	}
	
	/**
	 * @return The centre (in global co-ordinates)
	 */
	public Vector3f getCentre()
	{
		return new Vector3f(x + w / 2F, y + h / 2F, z + d / 2F);
	}
}
