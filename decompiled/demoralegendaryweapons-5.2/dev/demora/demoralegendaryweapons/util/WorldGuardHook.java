package dev.demora.demoralegendaryweapons.util;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WorldGuardHook {
   private final DemoraLegendaryWeapons plugin;
   private boolean available;
   private Object query;
   private Method adapt;
   private Method getApplicableRegions;
   private Method size;

   public WorldGuardHook(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.init();
   }

   private void init() {
      try {
         if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            return;
         }

         Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
         Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);
         Object platform = worldGuard.getClass().getMethod("getPlatform").invoke(worldGuard);
         Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
         this.query = container.getClass().getMethod("createQuery").invoke(container);
         Class<?> bukkitAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
         this.adapt = bukkitAdapter.getMethod("adapt", Location.class);
         Class<?> weLocation = Class.forName("com.sk89q.worldedit.util.Location");
         this.getApplicableRegions = this.query.getClass().getMethod("getApplicableRegions", weLocation);
         Class<?> regionSetClass = Class.forName("com.sk89q.worldguard.protection.ApplicableRegionSet");
         this.size = regionSetClass.getMethod("size");
         this.available = true;
         this.plugin.getLogger().info("WorldGuard hook enabled");
      } catch (Throwable var8) {
         this.available = false;
      }
   }

   public boolean isAvailable() {
      return this.available;
   }

   public boolean isProtected(Location location) {
      if (this.available && location != null && location.getWorld() != null) {
         try {
            Object weLocation = this.adapt.invoke(null, location);
            Object regionSet = this.getApplicableRegions.invoke(this.query, weLocation);
            int count = (Integer)this.size.invoke(regionSet);
            return count > 0;
         } catch (Throwable var5) {
            return false;
         }
      } else {
         return false;
      }
   }
}
