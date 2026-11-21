package com.oheers.fish.update;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.messages.ConfigMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotify implements Listener {
   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      if (EvenMoreFish.getInstance().isUpdateAvailable()) {
         if (event.getPlayer().hasPermission("emf.admin.update.notify")) {
            ConfigMessage.ADMIN_UPDATE_AVAILABLE.getMessage().send(event.getPlayer());
         }
      }
   }
}
