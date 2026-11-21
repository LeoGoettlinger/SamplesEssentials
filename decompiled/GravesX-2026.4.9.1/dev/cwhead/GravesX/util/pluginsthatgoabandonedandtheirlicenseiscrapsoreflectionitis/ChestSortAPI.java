package dev.cwhead.GravesX.util.pluginsthatgoabandonedandtheirlicenseiscrapsoreflectionitis;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

@Deprecated
public final class ChestSortAPI {
   private static final String PLUGIN_NAME = "ChestSort";
   private static final String API_CLASS = "de.jeff_media.chestsort.api.ChestSortAPI";
   private static volatile boolean lookedUp = false;
   private static volatile boolean classAvailable = false;
   private static MethodHandle MH_sortInventory_Inv;

   private ChestSortAPI() {
   }

   @Deprecated
   public static void sortInventory(Inventory inventory) {
      Objects.requireNonNull(inventory, "inventory");
      if (isEnabled()) {
         ensureLookup();
         invokeStaticVoid(MH_sortInventory_Inv, inventory);
      }
   }

   private static synchronized void ensureLookup() {
      if (!lookedUp) {
         lookedUp = true;

         try {
            Plugin chestSort = Bukkit.getPluginManager().getPlugin("ChestSort");
            if (chestSort == null) {
               classAvailable = false;
               return;
            }

            ClassLoader cl = chestSort.getClass().getClassLoader();
            Class<?> apiClazz = Class.forName("de.jeff_media.chestsort.api.ChestSortAPI", false, cl);
            Lookup lookup = MethodHandles.publicLookup();
            MH_sortInventory_Inv = staticHandle(lookup, apiClazz, Inventory.class);
            classAvailable = MH_sortInventory_Inv != null;
         } catch (Throwable var4) {
            classAvailable = false;
         }
      }
   }

   private static MethodHandle staticHandle(Lookup lookup, Class<?> owner, Class<?>... params) {
      try {
         Method m = owner.getMethod("sortInventory", params);
         return m.getReturnType() != void.class ? null : lookup.unreflect(m);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static void invokeStaticVoid(MethodHandle mh, Object... args) {
      if (classAvailable && mh != null) {
         try {
            mh.invokeWithArguments(args);
         } catch (Throwable var3) {
         }
      }
   }

   private static boolean isEnabled() {
      Plugin p = Bukkit.getPluginManager().getPlugin("ChestSort");
      return p != null && p.isEnabled();
   }
}
