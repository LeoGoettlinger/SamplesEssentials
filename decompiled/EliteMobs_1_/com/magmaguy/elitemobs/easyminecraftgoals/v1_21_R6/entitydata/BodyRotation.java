package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R6.entitydata;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;

public class BodyRotation {
   public static float getBodyRotation(Entity entity) {
      return entity instanceof EntityLiving livingEntity ? livingEntity.bB : entity.dX();
   }
}
