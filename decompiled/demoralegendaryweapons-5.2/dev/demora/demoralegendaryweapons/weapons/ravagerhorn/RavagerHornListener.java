package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RavagerHornListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final RavagerHorn ravagerHorn;
   private final RideStateCache state;

   public RavagerHornListener(DemoraLegendaryWeapons plugin, RavagerHorn ravagerHorn) {
      this.plugin = plugin;
      this.ravagerHorn = ravagerHorn;
      this.state = new RideStateCache(plugin);
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      HornUseHandler.handle(event, this.state, this.plugin, this.ravagerHorn);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDismount(EntityDismountEvent event) {
      RideCleanupHandler.onDismount(event, this.state);
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      RideCleanupHandler.onQuit(event, this.state);
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      MountDamageHandler.handle(event, this.state, this.plugin);
   }

   public void applyThrowChargeDecay() {
      ChargeDecayRunner.run(this.state, this.plugin);
   }

   public long getRemainingCooldownSeconds(UUID playerId) {
      return this.state.getRemainingCooldownSeconds(playerId);
   }
}
