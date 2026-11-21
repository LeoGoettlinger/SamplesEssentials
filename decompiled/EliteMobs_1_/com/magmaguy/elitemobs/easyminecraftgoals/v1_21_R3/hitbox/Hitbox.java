package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3.hitbox;

import java.lang.reflect.Field;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySize;

public class Hitbox {
   private Hitbox() {
   }

   public static boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
      EntitySize entityDimensions = new EntitySize(width, height, height, null, fixed);
      Class<?> entityClass = Entity.class;

      try {
         Field field = entityClass.getDeclaredField("bb");
         field.setAccessible(true);
         field.set(entity, entityDimensions);
      } catch (NoSuchFieldException var7) {
         var7.printStackTrace();
         return false;
      } catch (IllegalAccessException var8) {
         var8.printStackTrace();
         return false;
      }

      entity.a(entityDimensions.a(entity.dt()));
      return true;
   }
}
