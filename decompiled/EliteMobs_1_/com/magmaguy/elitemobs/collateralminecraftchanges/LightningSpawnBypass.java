package com.magmaguy.elitemobs.collateralminecraftchanges;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class LightningSpawnBypass implements Listener {
   private static boolean bypass = false;

   public static void strikeLightningIgnoreProtections(Location location) {
      if (location != null && location.getWorld() != null) {
         bypass = true;
         location.getWorld().strikeLightningEffect(location);
      }
   }

   public static void bypass() {
      bypass = true;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onLightningSpawn(EntitySpawnEvent event) {
      if (bypass) {
         if (event.getEntity().getType().equals(EntityType.LIGHTNING_BOLT)) {
            bypass = true;
            event.setCancelled(false);
         }
      }
   }
}
