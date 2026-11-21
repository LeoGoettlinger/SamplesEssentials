package dev.cwhead.GravesX.listener.integration.itemsadder;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.integration.ItemsAdder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public final class ItemsAdderReloadGateListener implements Listener {
   private final Graves plugin;
   private final ItemsAdder integration;

   public ItemsAdderReloadGateListener(Graves plugin, ItemsAdder integration) {
      this.plugin = plugin;
      this.integration = integration;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
      String msg = event.getMessage();
      if (this.isReloadCommand(msg)) {
         this.gateNotReady("player", msg);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onConsoleCommand(ServerCommandEvent event) {
      String cmd = event.getCommand();
      if (this.isReloadCommand(cmd)) {
         this.gateNotReady("console", cmd);
      }
   }

   private boolean isReloadCommand(String raw) {
      String s = raw.trim().toLowerCase();
      if (s.startsWith("/")) {
         s = s.substring(1);
      }

      int space = s.indexOf(32);
      String base = space >= 0 ? s.substring(0, space) : s;
      String noNamespace = base.contains(":") ? base.substring(base.indexOf(58) + 1) : base;
      return noNamespace.equals("iareload") || noNamespace.equals("iazip");
   }

   private void gateNotReady(String source, String cmd) {
      this.plugin.getSchedulerManager().runTask(() -> {
         if (this.integration.isReady()) {
            this.integration.setReady(false);
            this.plugin.debugMessage("ItemsAdder reload detected (" + source + ": " + cmd + "). Integration gated until ItemsAdderLoadDataEvent.", 1);
         }
      });
   }
}
