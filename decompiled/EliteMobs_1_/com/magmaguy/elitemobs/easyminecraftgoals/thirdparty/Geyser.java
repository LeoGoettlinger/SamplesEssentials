package com.magmaguy.elitemobs.easyminecraftgoals.thirdparty;

import org.bukkit.entity.Player;
import org.geysermc.geyser.api.GeyserApi;

class Geyser {
   private Geyser() {
   }

   static boolean isBedrock(Player player) {
      try {
         GeyserApi api = GeyserApi.api();
         return api != null && api.connectionByUuid(player.getUniqueId()) != null;
      } catch (LinkageError | RuntimeException var2) {
         return false;
      }
   }
}
