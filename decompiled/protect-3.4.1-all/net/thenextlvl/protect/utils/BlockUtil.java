package net.thenextlvl.protect.utils;

import org.bukkit.Material;

public final class BlockUtil {
   public static boolean isInteractable(Material type, Material item) {
      switch (item) {
         case BRUSH:
            switch (type) {
               case SUSPICIOUS_GRAVEL:
               case SUSPICIOUS_SAND:
                  return true;
               default:
                  return type.isInteractable();
            }
         case SHEARS:
         case GLASS_BOTTLE:
            switch (type) {
               case BEEHIVE:
               case BEE_NEST:
                  return true;
            }
      }

      return type.isInteractable();
   }
}
