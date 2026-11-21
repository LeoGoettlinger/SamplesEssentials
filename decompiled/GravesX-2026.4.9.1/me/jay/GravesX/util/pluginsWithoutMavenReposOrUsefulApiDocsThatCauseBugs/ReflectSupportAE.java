package me.jay.GravesX.util.pluginsWithoutMavenReposOrUsefulApiDocsThatCauseBugs;

import java.lang.reflect.Method;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public final class ReflectSupportAE {
   private static volatile boolean lookedUp = false;
   private static volatile boolean available = false;
   private static Class<?> AEAPI;
   private static Method M_HAS_CUSTOM;
   private static Method M_GET_ENCHANTS;
   private static Method M_HAS_WHITESCROLL;

   private ReflectSupportAE() {
   }

   public static boolean isSoulbound(ItemStack itemStack) {
      ensureLookup();
      if (available && itemStack != null) {
         try {
            if (M_HAS_CUSTOM != null) {
               Object res = M_HAS_CUSTOM.invoke(null, "Soulbound", itemStack);
               if (Boolean.TRUE.equals(res)) {
                  return true;
               }
            }

            if (M_GET_ENCHANTS != null) {
               Map<String, ?> map = (Map<String, ?>)M_GET_ENCHANTS.invoke(null, itemStack);
               return map != null && map.containsKey("Soulbound");
            }
         } catch (ReflectiveOperationException var2) {
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean hasWhitScroll(ItemStack itemStack) {
      ensureLookup();
      if (available && itemStack != null) {
         try {
            if (M_HAS_WHITESCROLL != null) {
               Object res = M_HAS_WHITESCROLL.invoke(null, itemStack);
               return Boolean.TRUE.equals(res);
            }
         } catch (ReflectiveOperationException var2) {
         }

         return false;
      } else {
         return false;
      }
   }

   private static synchronized void ensureLookup() {
      if (!lookedUp) {
         lookedUp = true;

         try {
            AEAPI = Class.forName("AEAPI");

            try {
               M_HAS_CUSTOM = AEAPI.getMethod("hasCustomEnchant", String.class, ItemStack.class);
            } catch (NoSuchMethodException var3) {
               M_HAS_CUSTOM = null;
            }

            try {
               M_GET_ENCHANTS = AEAPI.getMethod("getEnchantmentsOnItem", ItemStack.class);
            } catch (NoSuchMethodException var2) {
               M_GET_ENCHANTS = null;
            }

            try {
               M_HAS_WHITESCROLL = AEAPI.getMethod("hasWhitescroll", ItemStack.class);
            } catch (NoSuchMethodException var1) {
               M_HAS_WHITESCROLL = null;
            }

            available = true;
         } catch (ClassNotFoundException var4) {
            available = false;
         }
      }
   }
}
