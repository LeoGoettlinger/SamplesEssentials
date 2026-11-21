package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.OfflinePlayerStub;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.BanList.Type;

public class Commandban extends EssentialsCommand {
   public Commandban() {
      super("ban");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      boolean nomatch = false;
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user;
         try {
            user = this.getPlayer(server, args, 0, true, true);
         } catch (PlayerNotFoundException var11) {
            nomatch = true;
            user = this.ess.getUser(new OfflinePlayerStub(args[0], this.ess.getServer()));
         }

         if (!user.getBase().isOnline()) {
            if (sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.ban.offline")) {
               throw new TranslatableException("banExemptOffline");
            }
         } else if (user.isAuthorized("essentials.ban.exempt") && sender.isPlayer()) {
            throw new TranslatableException("banExempt");
         }

         String senderName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : "Console";
         String senderDisplayName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : Console.displayName();
         String banReason;
         if (args.length > 1) {
            banReason = FormatUtil.replaceFormat(getFinalArg(args, 1).replace("\\n", "\n").replace("|", "\n"));
         } else {
            banReason = I18n.tlLiteral("defaultBanReason");
         }

         this.ess.getServer().getBanList(Type.NAME).addBan(user.getName(), banReason, null, senderName);
         String banDisplay = I18n.tlLiteral("banFormat", banReason, senderDisplayName);
         user.getBase().kickPlayer(this.ess.getAdventureFacet().miniToLegacy(banDisplay));
         this.ess
            .getLogger()
            .log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("playerBanned", senderDisplayName, user.getName(), banDisplay)));
         if (nomatch) {
            sender.sendTl("userUnknown", user.getName());
         }

         this.ess.broadcastTl(null, u -> !u.isAuthorized("essentials.ban.notify"), "playerBanned", new Object[]{senderDisplayName, user.getName(), banReason});
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
