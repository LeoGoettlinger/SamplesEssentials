package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;

public class Commandburn extends EssentialsCommand {
   public Commandburn() {
      super("burn");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, sender, args, 0);
         int seconds = Integer.parseInt(args[1]);
         int fireTicks = (int)Math.max(0L, Math.min(seconds * 20L, 2147483647L));
         user.getBase().setFireTicks(fireTicks);
         sender.sendTl("burnMsg", user.getDisplayName(), Math.max(0, seconds));
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else {
         return args.length == 2 ? COMMON_DURATIONS : Collections.emptyList();
      }
   }
}
