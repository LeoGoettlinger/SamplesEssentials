package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.BanList.Type;

public class Commandtempban extends EssentialsCommand {
   public Commandtempban() {
      super("tempban");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0, true, true);
         if (!user.getBase().isOnline() && sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.tempban.offline")) {
            sender.sendTl("tempbanExemptOffline");
         } else if (user.isAuthorized("essentials.tempban.exempt") && sender.isPlayer()) {
            sender.sendTl("tempbanExempt");
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

               String senderName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : "Console";
               String senderDisplayName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : Console.displayName();
               this.ess.getServer().getBanList(Type.NAME).addBan(user.getName(), banReason, new Date(banTimestamp), senderName);
               String expiry = DateUtil.formatDateDiff(banTimestamp);
               String banDisplay = user.playerTl("tempBanned", expiry, senderDisplayName, banReason);
               user.getBase().kickPlayer(this.ess.getAdventureFacet().miniToLegacy(banDisplay));
               this.ess
                  .getLogger()
                  .log(
                     Level.INFO,
                     this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("playerTempBanned", senderDisplayName, user.getName(), expiry, banReason))
                  );
               this.ess
                  .broadcastTl((IUser)null, "essentials.ban.notify", "playerTempBanned", new Object[]{senderDisplayName, user.getName(), expiry, banReason});
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : COMMON_DATE_DIFFS;
   }
}
