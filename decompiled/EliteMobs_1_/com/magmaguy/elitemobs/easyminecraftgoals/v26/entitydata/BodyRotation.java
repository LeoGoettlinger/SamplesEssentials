package com.magmaguy.elitemobs.easyminecraftgoals.v26.entitydata;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BodyRotation {
   public static float getBodyRotation(Entity entity) {
      return entity instanceof LivingEntity livingEntity ? livingEntity.yBodyRot : entity.getYRot();
   }
}
