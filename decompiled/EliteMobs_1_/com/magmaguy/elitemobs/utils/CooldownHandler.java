package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import java.util.Collection;
import org.bukkit.scheduler.BukkitRunnable;

public class CooldownHandler {
   private CooldownHandler() {
   }

   public static <T> void initialize(final Collection<T> collection, final T object, int cooldownInTicks) {
      collection.add(object);
      (new BukkitRunnable() {
         public void run() {
            collection.remove(object);
         }
      }).runTaskLater(MetadataHandler.PLUGIN, cooldownInTicks);
   }
}
