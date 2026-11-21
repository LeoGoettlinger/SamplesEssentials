package com.earth2me.essentials;

import com.earth2me.essentials.economy.EconomyLayer;
import com.earth2me.essentials.economy.EconomyLayers;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class EssentialsPluginListener implements Listener, IConf {
   private final transient net.ess3.api.IEssentials ess;

   public EssentialsPluginListener(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginEnable(final PluginEnableEvent event) {
      if (event.getPlugin().getName().equals("EssentialsChat")) {
         this.ess.getSettings().setEssentialsChatActive(true);
      }

      this.ess.getPermissionsHandler().setUseSuperperms(this.ess.getSettings().useBukkitPermissions());
      this.ess.getPermissionsHandler().checkPermissions();
      this.ess.getAlternativeCommandsHandler().addPlugin(event.getPlugin());
      if (EconomyLayers.isServerStarted()) {
         EconomyLayer layer = EconomyLayers.onPluginEnable(event.getPlugin());
         if (layer != null) {
            this.ess
               .getLogger()
               .log(Level.INFO, "Essentials found a compatible payment resolution method: " + layer.getName() + " (v" + layer.getPluginVersion() + ")!");
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPluginDisable(final PluginDisableEvent event) {
      if (event.getPlugin().getName().equals("EssentialsChat")) {
         this.ess.getSettings().setEssentialsChatActive(false);
      }

      this.ess.getPermissionsHandler().checkPermissions();
      this.ess.getAlternativeCommandsHandler().removePlugin(event.getPlugin());
      if (EconomyLayers.onPluginDisable(event.getPlugin())) {
         EconomyLayer layer = EconomyLayers.getSelectedLayer();
         if (layer != null) {
            this.ess
               .getLogger()
               .log(Level.INFO, "Essentials found a new compatible payment resolution method: " + layer.getName() + " (v" + layer.getPluginVersion() + ")!");
         } else {
            this.ess
               .getLogger()
               .log(Level.INFO, "Active payment resolution method has been disabled! Falling back to Essentials' default payment resolution system!");
         }
      }
   }

   @Override
   public void reloadConfig() {
   }
}
