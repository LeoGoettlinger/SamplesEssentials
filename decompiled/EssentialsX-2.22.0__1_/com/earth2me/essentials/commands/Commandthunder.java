package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.CommonPlaceholders;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.World;

public class Commandthunder extends EssentialsCommand {
   public Commandthunder() {
      super("thunder");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         World world = user.getWorld();
         boolean setThunder = args[0].equalsIgnoreCase("true");
         if (args.length == 1) {
            world.setThundering(setThunder);
            user.sendTl("thunder", CommonPlaceholders.enableDisable(user.getSource(), setThunder));
         } else {
            world.setThundering(setThunder);
            world.setThunderDuration(Integer.parseInt(args[1]) * 20);
            user.sendTl("thunderDuration", CommonPlaceholders.enableDisable(user.getSource(), setThunder), Integer.parseInt(args[1]));
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"true", "false"});
      } else {
         return args.length == 2 ? COMMON_DATE_DIFFS : Collections.emptyList();
      }
   }
}
