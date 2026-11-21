package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.BanList.Type;

public class Commandunbanip extends EssentialsCommand {
   public Commandunbanip() {
      super("unbanip");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String ipAddress;
         if (FormatUtil.validIP(args[0])) {
            ipAddress = args[0];
         } else {
            try {
               User player = this.getPlayer(server, args, 0, true, true);
               ipAddress = player.getLastLoginAddress();
            } catch (PlayerNotFoundException var7) {
               ipAddress = args[0];
            }
         }

         if (ipAddress.isEmpty()) {
            throw new PlayerNotFoundException();
         } else {
            this.ess.getServer().getBanList(Type.IP).pardon(ipAddress);
            String senderDisplayName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : Console.displayName();
            this.ess
               .getLogger()
               .log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("playerUnbanIpAddress", senderDisplayName, ipAddress)));
            this.ess.broadcastTl((IUser)null, "essentials.banip.notify", "playerUnbanIpAddress", new Object[]{senderDisplayName, ipAddress});
         }
      }
   }
}
