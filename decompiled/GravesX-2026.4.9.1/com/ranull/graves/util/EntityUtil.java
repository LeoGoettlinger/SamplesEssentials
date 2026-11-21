package com.ranull.graves.util;

import org.bukkit.entity.Entity;

public class EntityUtil {
   private EntityUtil() {
   }

   public static boolean hasPermission(Entity entity, String permission) {
      try {
         return entity.hasPermission(permission);
      } catch (NoSuchMethodError var3) {
         return true;
      }
   }
}
