package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.magmacore.util.VersionChecker;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.entity.Player;

public final class MenuPresentation {
   private MenuPresentation() {
   }

   public static boolean supportsDialogs(Player player) {
      return PlayerData.getUseBookMenus(player.getUniqueId())
         && !BedrockChecker.isBedrock(player)
         && !DefaultConfig.isOnlyUseBedrockMenus()
         && !VersionChecker.serverVersionOlderThan(21, 6);
   }
}
