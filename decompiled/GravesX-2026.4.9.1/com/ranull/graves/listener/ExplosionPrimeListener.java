package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.GravePreExplodeEvent;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class ExplosionPrimeListener implements Listener {
   private final Graves plugin;

   public ExplosionPrimeListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onExplosionPrime(ExplosionPrimeEvent event) {
      Entity sourceEntity = event.getEntity();
      Location explosionLocation = sourceEntity.getLocation().clone();
      List<Grave> allGraves = this.plugin.getGraveManager().getAllGraves();
      if (allGraves != null && !allGraves.isEmpty()) {
         float blastRadius = event.getRadius();
         float blastRadiusSquared = blastRadius * blastRadius;
         boolean cancelExplosion = false;

         for (Grave grave : allGraves) {
            Location graveLocation = grave.getLocationDeath();
            if (graveLocation != null
               && Objects.equals(graveLocation.getWorld(), explosionLocation.getWorld())
               && explosionLocation.distanceSquared(graveLocation) <= blastRadiusSquared) {
               GravePreExplodeEvent preEvent = new GravePreExplodeEvent(grave, explosionLocation, sourceEntity, blastRadius);
               this.plugin.getServer().getPluginManager().callEvent(preEvent);
               if (preEvent.isCancelled()) {
                  cancelExplosion = true;
                  break;
               }

               float newRadius = preEvent.getRadius();
               if (newRadius != blastRadius) {
                  blastRadius = newRadius;
                  blastRadiusSquared = newRadius * newRadius;
                  event.setRadius(newRadius);
               }

               Location newExplosionLocation = preEvent.getExplosionLocation();
               if (!newExplosionLocation.equals(explosionLocation)) {
                  explosionLocation = newExplosionLocation.clone();
                  if (!sourceEntity.getLocation().equals(newExplosionLocation)) {
                     sourceEntity.teleport(newExplosionLocation);
                  }
               }
            }
         }

         if (cancelExplosion) {
            event.setCancelled(true);
         }
      }
   }
}
