package com.skyblockexp.ezrtp.integration;

import com.skyblockexp.ezrtp.gui.FactionClaimSelectionGuiManager;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.teamsapi.api.AbstractTeamsSubcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class RtpTeamsSubcommand extends AbstractTeamsSubcommand {
   private final FactionClaimSelectionGuiManager factionClaimGuiManager;

   RtpTeamsSubcommand(FactionClaimSelectionGuiManager factionClaimGuiManager) {
      super("rtp", "Open faction claim RTP GUI", "ezrtp.use");
      this.factionClaimGuiManager = factionClaimGuiManager;
   }

   public boolean execute(CommandSender sender, String[] args) {
      if (sender instanceof Player player) {
         return this.factionClaimGuiManager.openSelection(player);
      } else {
         MessageUtil.send(sender, "<red>This command may only be used by players.</red>");
         return true;
      }
   }

   public String getUsage() {
      return "/f rtp";
   }
}
