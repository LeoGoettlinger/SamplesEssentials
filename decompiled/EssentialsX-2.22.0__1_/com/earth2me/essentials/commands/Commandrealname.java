package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Locale;
import org.bukkit.Server;

public class Commandrealname extends EssentialsCommand {
   public Commandrealname() {
      super("realname");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         String lookup = args[0].toLowerCase(Locale.ENGLISH);
         boolean skipHidden = sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).canInteractVanished();
         boolean foundUser = false;

         for (User u : this.ess.getOnlineUsers()) {
            if (!skipHidden || !u.isHidden(sender.getPlayer()) || !u.isHiddenFrom(sender.getPlayer())) {
               u.setDisplayNick();
               if (FormatUtil.stripFormat(u.getDisplayName()).toLowerCase(Locale.ENGLISH).contains(lookup)) {
                  foundUser = true;
                  sender.sendTl("realName", u.getDisplayName(), u.getName());
               }
            }
         }

         if (!foundUser) {
            throw new PlayerNotFoundException();
         }
      }
   }
}
