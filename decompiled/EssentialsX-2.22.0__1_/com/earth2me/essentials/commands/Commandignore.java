package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Server;

public class Commandignore extends EssentialsCommand {
   public Commandignore() {
      super("ignore");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         StringBuilder sb = new StringBuilder();

         for (UUID uuid : user._getIgnoredPlayers()) {
            User curUser = this.ess.getUser(uuid);
            if (curUser != null && curUser.getName() != null && !curUser.getName().trim().isEmpty()) {
               sb.append(curUser.getName()).append(" ");
            }
         }

         String ignoredList = sb.toString().trim();
         user.sendTl(ignoredList.length() > 0 ? "ignoredList" : "noIgnored", ignoredList);
      } else {
         User player = this.getPlayer(server, args, 0, false, true);
         if (player.isIgnoreExempt()) {
            user.sendTl("ignoreExempt");
         } else if (user.isIgnoredPlayer(player)) {
            user.setIgnoredPlayer(player, false);
            user.sendTl("unignorePlayer", player.getName());
         } else if (user.getUUID().equals(player.getUUID())) {
            user.sendTl("ignoreYourself");
         } else {
            user.setIgnoredPlayer(player, true);
            user.sendTl("ignorePlayer", player.getName());
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(user) : Collections.emptyList();
   }
}
