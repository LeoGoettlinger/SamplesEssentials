package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EnderCrystalLightningRod {
   public EnderCrystalLightningRod(final EliteEntity eliteEntity, final EnderCrystal enderCrystal) {
      (new BukkitRunnable() {
         int counter = 0;

         public void run() {
            if (eliteEntity.isValid() && enderCrystal.isValid()) {
               if (this.counter % 5 == 0) {
                  Vector randomVector = new Vector(ThreadLocalRandom.current().nextInt(-15, 15), 0, ThreadLocalRandom.current().nextInt(-15, 15));
                  EnderDragonEmpoweredLightningSupport.lightningTask(enderCrystal.getLocation().clone().add(randomVector));
               }

               this.counter++;
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
   }

   public static class EnderCrystalLightningRodEvents implements Listener {
      @EventHandler(
         ignoreCancelled = true
      )
      public void damageEvent(EntityDamageEvent event) {
         if (event.getEntity().getType().equals(EntityType.END_CRYSTAL)) {
            if (event.getCause().equals(DamageCause.ENTITY_EXPLOSION)
               || event.getCause().equals(DamageCause.FALLING_BLOCK)
               || event.getCause().equals(DamageCause.LIGHTNING)) {
               if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "eliteCrystal"), PersistentDataType.STRING)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }
}
