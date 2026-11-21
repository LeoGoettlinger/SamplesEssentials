package com.magmaguy.elitemobs.easyminecraftgoals.v26.hitbox;

import java.lang.reflect.Field;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

public class Hitbox {
   private static Field dimensionsField = null;
   private static boolean dimensionsFieldLookupTried = false;

   private Hitbox() {
   }

   public static boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
      if (!dimensionsFieldLookupTried) {
         dimensionsFieldLookupTried = true;
         dimensionsField = findDimensionsField();
      }

      if (dimensionsField == null) {
         return false;
      } else {
         EntityDimensions entityDimensions = new EntityDimensions(width, height, height, null, fixed);

         try {
            dimensionsField.set(entity, entityDimensions);
         } catch (Exception var6) {
            var6.printStackTrace();
            return false;
         }

         entity.setBoundingBox(entityDimensions.makeBoundingBox(entity.position()));
         return true;
      }
   }

   private static Field findDimensionsField() {
      for (Field f : Entity.class.getDeclaredFields()) {
         if (f.getType() == EntityDimensions.class) {
            f.setAccessible(true);
            return f;
         }
      }

      new RuntimeException("Hitbox: no EntityDimensions field on Entity.class; custom hitboxes disabled this session").printStackTrace();
      return null;
   }
}
