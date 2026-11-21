package dev.demora.demoralegendaryweapons.weapons.ravagerhorn;

import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class RideCleanupHandler {
   public static void onDismount(EntityDismountEvent event, RideStateCache state) {
      if (event.getEntity() instanceof Player player) {
         Entity var6 = event.getDismounted();
         UUID playerId = player.getUniqueId();
         Ravager ourRavager = state.getMount(playerId);
         if (ourRavager != null && var6.getUniqueId().equals(ourRavager.getUniqueId())) {
            state.startCooldown(playerId);
            state.removeRider(playerId);
            ourRavager.remove();
         }
      }
   }

   public static void onQuit(PlayerQuitEvent event, RideStateCache state) {
      UUID playerId = event.getPlayer().getUniqueId();
      Ravager ravager = state.getMount(playerId);
      if (ravager != null) {
         ravager.remove();
      }

      state.removeRider(playerId);
      state.startCooldown(playerId);
   }
}
