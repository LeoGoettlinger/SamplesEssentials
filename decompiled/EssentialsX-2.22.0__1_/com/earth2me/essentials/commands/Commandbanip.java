package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.BanList.Type;
import org.bukkit.entity.Player;

public class Commandbanip extends EssentialsCommand {
   public Commandbanip() {
      super("banip");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String senderName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : "Console";
         String senderDisplayName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : Console.displayName();
         String ipAddress;
         if (FormatUtil.validIP(args[0])) {
            ipAddress = args[0];
         } else {
            try {
               User player = this.getPlayer(server, args, 0, true, true);
               ipAddress = player.getLastLoginAddress();
            } catch (PlayerNotFoundException var12) {
               ipAddress = args[0];
            }
         }

         if (ipAddress.isEmpty()) {
            throw new PlayerNotFoundException();
         } else {
            String banReason;
            if (args.length > 1) {
               banReason = FormatUtil.replaceFormat(getFinalArg(args, 1).replace("\\n", "\n").replace("|", "\n"));
            } else {
               banReason = I18n.tlLiteral("defaultBanReason");
            }

            String banDisplay = this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("banFormat", banReason, senderDisplayName));
            this.ess.getServer().getBanList(Type.IP).addBan(ipAddress, banReason, null, senderName);
            this.ess
               .getLogger()
               .log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("playerBanIpAddress", senderDisplayName, ipAddress, banReason)));

            for (Player player : this.ess.getServer().getOnlinePlayers()) {
               if (player.getAddress().getAddress().getHostAddress().equalsIgnoreCase(ipAddress)) {
                  player.kickPlayer(banDisplay);
               }
            }

            this.ess
               .broadcastTl(null, u -> !u.isAuthorized("essentials.banip.notify"), "playerBanIpAddress", new Object[]{senderDisplayName, ipAddress, banReason});
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
