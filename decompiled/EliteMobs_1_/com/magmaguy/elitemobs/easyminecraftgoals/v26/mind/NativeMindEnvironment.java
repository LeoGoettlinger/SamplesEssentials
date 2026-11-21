package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import net.minecraft.world.entity.Mob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.plugin.java.JavaPlugin;

final class NativeMindEnvironment implements Listener, AutoCloseable {
   private final String hostIdentity;

   NativeMindEnvironment(String hostIdentity) {
      this.hostIdentity = hostIdentity;
      JavaPlugin plugin = JavaPlugin.getProvidingPlugin(NativeMindHost.class);
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onCombust(EntityCombustEvent event) {
      if (event.getClass() == EntityCombustEvent.class) {
         if (event.getEntity() instanceof LivingEntity living) {
            if (MindCarrierState.isMarked(living, this.hostIdentity)) {
               if (NativeMindVersion.getNMSLivingEntity(living) instanceof Mob mob && NativeMindVersion.sunlightExposure(mob)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @Override
   public void close() {
      HandlerList.unregisterAll(this);
   }
}
