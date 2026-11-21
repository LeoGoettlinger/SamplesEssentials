package com.artillexstudios.axtrade.listeners;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.utils.Cooldown;
import com.artillexstudios.axtrade.libs.axapi.utils.PaperUtils;
import com.artillexstudios.axtrade.request.Requests;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityInteractListener implements Listener {
   private static final Cooldown<Player> cooldown = PaperUtils.isFolia() ? Cooldown.createSynchronized() : Cooldown.create();

   @EventHandler(
      ignoreCancelled = true
   )
   public void onInteract(@NotNull PlayerInteractEntityEvent event) {
      if (AxTrade.CONFIG.getBoolean("shift-click-send-request", true)) {
         Player player = event.getPlayer();
         if (player.hasPermission("axtrade.trade")) {
            if (!cooldown.hasCooldown(player)) {
               if (player.isSneaking()) {
                  if (event.getRightClicked() instanceof Player sendTo) {
                     if (sendTo.isOnline()) {
                        cooldown.addCooldown(player, 100L);
                        Requests.addRequest(player, sendTo);
                        event.setCancelled(true);
                     }
                  }
               }
            }
         }
      }
   }
}
