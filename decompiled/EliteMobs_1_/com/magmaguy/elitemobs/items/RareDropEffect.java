package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.ItemQualityColorizer;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class RareDropEffect implements Listener {
   public static void runEffect(final Item item) {
      if (ItemSettingsConfig.isEnableRareItemParticleEffects()) {
         if (ItemQualityColorizer.getItemQuality(item.getItemStack()).equals(ItemQualityColorizer.ItemQuality.LIGHT_BLUE)
            || ItemQualityColorizer.getItemQuality(item.getItemStack()).equals(ItemQualityColorizer.ItemQuality.GOLD)) {
            (new BukkitRunnable() {
               int counter = 0;

               public void run() {
                  if (item != null && item.isValid() && !item.isDead()) {
                     item.getWorld().spawnParticle(Particle.PORTAL, item.getLocation(), 5, 0.01, 0.01, 0.01, 0.5);
                     this.counter += 20;
                     if (this.counter > 2400) {
                        this.cancel();
                     }
                  } else {
                     this.cancel();
                  }
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
         }
      }
   }

   @EventHandler
   public void onItemDrop(ItemSpawnEvent event) {
      if (ItemTagger.isEliteItem(event.getEntity().getItemStack())) {
         runEffect(event.getEntity());
      }
   }
}
