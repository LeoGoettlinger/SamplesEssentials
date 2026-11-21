package com.artillexstudios.axrankmenu;

import com.artillexstudios.axrankmenu.libs.axapi.utils.NumberUtils;

public class ConfigMigrator {
   public ConfigMigrator() {
      AxRankMenu.RANKS.getBackingDocument().remove("VIP");
      AxRankMenu.RANKS.getBackingDocument().remove("MVP");

      for (String route : AxRankMenu.CONFIG.getSection("menu").getRoutesAsStrings(true)) {
         if (!AxRankMenu.CONFIG.getStringList("menu." + route).isEmpty()) {
            AxRankMenu.RANKS.set(route, AxRankMenu.CONFIG.getStringList("menu." + route));
         } else if (NumberUtils.isInt(AxRankMenu.CONFIG.getString("menu." + route))) {
            AxRankMenu.RANKS.set(route, AxRankMenu.CONFIG.getLong("menu." + route));
         } else if (NumberUtils.isDouble(AxRankMenu.CONFIG.getString("menu." + route))) {
            AxRankMenu.RANKS.set(route, AxRankMenu.CONFIG.getDouble("menu." + route));
         } else {
            AxRankMenu.RANKS.set(route, AxRankMenu.CONFIG.getString("menu." + route));
         }
      }

      AxRankMenu.CONFIG.getBackingDocument().remove("menu");
      AxRankMenu.CONFIG.save();
      AxRankMenu.RANKS.save();
   }
}
