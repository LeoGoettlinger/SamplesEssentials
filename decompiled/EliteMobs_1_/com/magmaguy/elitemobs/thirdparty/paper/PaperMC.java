package com.magmaguy.elitemobs.thirdparty.paper;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import lombok.Generated;
import org.bukkit.entity.Player;

public class PaperMC {
   private static boolean paperMC = false;

   public static void initialize() {
      try {
         paperMC = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
      } catch (ClassNotFoundException var1) {
         Logger.info("Not paper");
      }
   }

   public static boolean checkClient(Player player) {
      return true;
   }

   @Generated
   public static boolean isPaperMC() {
      return paperMC;
   }
}
