package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.BanList.Type;
import org.bukkit.entity.Player;

public class Commandtempbanip extends EssentialsCommand {
   public Commandtempbanip() {
      super("tempbanip");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
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
            } catch (PlayerNotFoundException var17) {
               ipAddress = args[0];
            }
         }

         if (ipAddress.isEmpty()) {
            throw new PlayerNotFoundException();
         } else {
            String time = getFinalArg(args, 1);
            long banTimestamp = DateUtil.parseDateDiff(time, true);
            String banReason = FormatUtil.replaceFormat(DateUtil.removeTimePattern(time));
            long maxBanLength = this.ess.getSettings().getMaxTempban() * 1000L;
            if (maxBanLength > 0L
               && banTimestamp - GregorianCalendar.getInstance().getTimeInMillis() > maxBanLength
               && sender.isPlayer()
               && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.tempban.unlimited")) {
               sender.sendTl("oversizedTempban");
            } else {
               if (banReason.length() < 2) {
                  banReason = I18n.tlLiteral("defaultBanReason");
               }

               this.ess.getServer().getBanList(Type.IP).addBan(ipAddress, banReason, new Date(banTimestamp), senderName);
               String banDisplay = this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("banFormat", banReason, senderDisplayName));

               for (Player player : this.ess.getServer().getOnlinePlayers()) {
                  if (player.getAddress().getAddress().getHostAddress().equalsIgnoreCase(ipAddress)) {
                     player.kickPlayer(banDisplay);
                  }
               }

               String tlKey = "playerTempBanIpAddress";
               Object[] objects = new Object[]{senderDisplayName, ipAddress, banReason, DateUtil.formatDateDiff(banTimestamp), banReason};
               this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("playerTempBanIpAddress", objects)));
               this.ess.broadcastTl(null, "essentials.banip.notify", "playerTempBanIpAddress", objects);
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : COMMON_DATE_DIFFS;
   }
}
