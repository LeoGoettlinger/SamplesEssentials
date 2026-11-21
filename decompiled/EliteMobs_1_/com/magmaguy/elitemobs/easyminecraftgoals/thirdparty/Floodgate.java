package com.magmaguy.elitemobs.easyminecraftgoals.thirdparty;

import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

class Floodgate {
   private Floodgate() {
   }

   static boolean isBedrock(Player player) {
      try {
         FloodgateApi api = FloodgateApi.getInstance();
         return api != null && api.isFloodgatePlayer(player.getUniqueId());
      } catch (LinkageError | RuntimeException var2) {
         return false;
      }
   }
}
