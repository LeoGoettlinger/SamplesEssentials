package com.lishid.openinv.listener;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.util.setting.PlayerToggles;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class ToggleListener implements Listener {
   @Keep
   @EventHandler
   private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      PlayerToggles.any().set(playerId, false);
      PlayerToggles.silent().set(playerId, false);
   }
}
