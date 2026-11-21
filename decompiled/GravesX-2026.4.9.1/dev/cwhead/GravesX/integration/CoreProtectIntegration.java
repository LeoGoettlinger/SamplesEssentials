package dev.cwhead.GravesX.integration;

import com.ranull.graves.Graves;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

/** @deprecated */
public class CoreProtectIntegration {
   private final Graves plugin;
   private CoreProtectAPI coreProtectAPI;

   @Deprecated
   public CoreProtectIntegration(Graves plugin) {
      this.plugin = plugin;
   }

   @Deprecated
   public CoreProtectAPI getCoreProtectAPI() {
      if (this.coreProtectAPI != null) {
         return this.coreProtectAPI;
      } else {
         if (this.plugin.getServer().getPluginManager().getPlugin("CoreProtect") instanceof CoreProtect coreProtect) {
            CoreProtectAPI api = coreProtect.getAPI();
            if (coreProtect.isEnabled() && api.APIVersion() >= 9) {
               this.coreProtectAPI = api;
            } else {
               this.plugin.getLogger().severe("CoreProtect is using API version " + api.APIVersion() + ". Graves will not be logged.");
            }
         }

         return this.coreProtectAPI;
      }
   }
}
