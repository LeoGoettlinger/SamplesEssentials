package com.magmaguy.elitemobs.magmacore.match;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

final class MatchMarker {
   private MatchMarker() {
   }

   static void set(Player player, Match match) {
      set(player, MatchCore.plugin().getName() + ":" + match.getRuntimeId());
   }

   static void setPending(Player player) {
      set(player, MatchCore.plugin().getName() + ":pending");
   }

   private static void set(Player player, String value) {
      player.setMetadata("magmacore_match", new FixedMetadataValue(MatchCore.plugin(), value));
   }

   static void clear(Player player) {
      player.removeMetadata("magmacore_match", MatchCore.plugin());
   }

   static boolean occupied(Player player) {
      return player.hasMetadata("magmacore_match");
   }
}
