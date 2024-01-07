package com.flansmod.common.eventhandlers;

import com.flansmod.common.guns.EntityBullet;
import com.flansmod.common.guns.raytracing.FlansModRaytracer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class BulletHitEvent extends Event {

	private EntityBullet bullet;

	private FlansModRaytracer.BulletHit hit;

	private Entity entity;

	private Block block;


	public BulletHitEvent(EntityBullet bullet, FlansModRaytracer.BulletHit hit) {
		this.bullet = bullet;
		this.hit = hit;

		//Try to set the entity field
		if(hit instanceof FlansModRaytracer.DriveableHit) {
			entity = ((FlansModRaytracer.DriveableHit)hit).driveable;
		} else if(hit instanceof FlansModRaytracer.PlayerBulletHit) {
			entity = ((FlansModRaytracer.PlayerBulletHit)hit).hitbox.player;
		} else if(hit instanceof FlansModRaytracer.EntityHit) {
			entity = ((FlansModRaytracer.EntityHit)hit).entity;
		}

		//Try to set the block field
		if(hit instanceof FlansModRaytracer.BlockHit) {
			RayTraceResult raytraceResult = ((FlansModRaytracer.BlockHit)hit).getRayTraceResult();
			block = bullet.world.getBlockState(raytraceResult.getBlockPos()).getBlock();
		}

	}

	public EntityBullet getBullet() {
		return bullet;
	}

	public FlansModRaytracer.BulletHit getHit() {
		return hit;
	}

	public boolean hitEntity() {
		return entity != null;
	}

	/**
	 * Returns null if the the bullet didn't hit an entity
	 *
	 */
	public Entity getHitEntity() {
		return entity;
	}

	/**
	 * Returns null if the the bullet didn't hit a block
	 *
	 */
	public Block getHitBlock() {
		return block;
	}

}
