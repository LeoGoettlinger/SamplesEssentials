package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.BanList.Type;

public class Commandunban extends EssentialsCommand {
   public Commandunban() {
      super("unban");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String name;
         try {
            User user = this.getPlayer(server, args, 0, true, true);
            name = user.getName();
            this.ess.getServer().getBanList(Type.NAME).pardon(name);
         } catch (PlayerNotFoundException var8) {
            OfflinePlayer player = server.getOfflinePlayer(args[0]);
            name = player.getName();
            if (!player.isBanned()) {
               throw new TranslatableException("playerNeverOnServer", args[0]);
            }

            this.ess.getServer().getBanList(Type.NAME).pardon(name);
         }

         String senderDisplayName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : Console.displayName();
         this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("playerUnbanned", senderDisplayName, name)));
         this.ess.broadcastTl((IUser)null, "essentials.ban.notify", "playerUnbanned", new Object[]{senderDisplayName, name});
      }
   }
}
