package com.skyblockexp.ezrtp.integration;

import com.skyblockexp.ezrtp.gui.FactionClaimSelectionGuiManager;
import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsSubcommand;
import org.bukkit.plugin.Plugin;

final class TeamsApiSubcommandOps {
   private TeamsApiSubcommandOps() {
   }

   static Object registerSubcommand(Plugin plugin, FactionClaimSelectionGuiManager gui) {
      RtpTeamsSubcommand subcommand = new RtpTeamsSubcommand(gui);
      TeamsAPI.registerSubcommand(plugin, subcommand);
      return subcommand;
   }

   static void unregisterSubcommand(Object subcommand) {
      TeamsAPI.unregisterSubcommand((TeamsSubcommand)subcommand);
   }
}
