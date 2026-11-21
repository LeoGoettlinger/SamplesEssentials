package com.skyblockexp.ezrtp.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class BlockCompat {
   private BlockCompat() {
   }

   public static boolean isPassable(Block block) {
      if (block == null) {
         return true;
      } else {
         try {
            Method m = block.getClass().getMethod("isPassable");
            Object res = m.invoke(block);
            if (res instanceof Boolean) {
               return (Boolean)res;
            }
         } catch (IllegalAccessException | InvocationTargetException | SecurityException | NoSuchMethodException var3) {
         }

         Material type = block.getType();
         if (type == null) {
            return true;
         } else {
            try {
               return !type.isSolid() || type == Material.AIR;
            } catch (NoSuchMethodError var4) {
               return type == Material.AIR;
            }
         }
      }
   }
}
