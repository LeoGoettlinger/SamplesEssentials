package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FrozenCustomBossKnockbackHandler implements Listener {
   @EventHandler
   public void onEliteDamaged(final EliteMobDamagedEvent event) {
      if (event.getEliteEntity() instanceof CustomBossEntity customBossEntity) {
         if (customBossEntity.getCustomBossesConfigFields().isFrozen() && customBossEntity.isValid()) {
            event.getEntity().setVelocity(new Vector(0, 0, 0));
            (new BukkitRunnable() {
               public void run() {
                  if (customBossEntity.isValid()) {
                     event.getEntity().setVelocity(new Vector(0, 0, 0));
                  }
               }
            }).runTaskLater(MetadataHandler.PLUGIN, 1L);
         }
      }
   }
}
