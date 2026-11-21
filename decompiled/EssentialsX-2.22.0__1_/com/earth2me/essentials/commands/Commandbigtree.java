package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

public class Commandbigtree extends EssentialsCommand {
   public Commandbigtree() {
      super("bigtree");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      TreeType tree;
      if (args.length > 0 && args[0].equalsIgnoreCase("redwood")) {
         tree = TreeType.TALL_REDWOOD;
      } else if (args.length > 0 && args[0].equalsIgnoreCase("tree")) {
         tree = TreeType.BIG_TREE;
      } else if (args.length > 0 && args[0].equalsIgnoreCase("jungle")) {
         tree = TreeType.JUNGLE;
      } else {
         if (args.length <= 0 || !args[0].equalsIgnoreCase("darkoak")) {
            throw new NotEnoughArgumentsException();
         }

         tree = TreeType.DARK_OAK;
      }

      Location loc = LocationUtil.getTarget(user.getBase(), this.ess.getSettings().getMaxTreeCommandRange()).add(0.0, 1.0, 0.0);
      if (loc.getBlock().getType().isSolid()) {
         throw new TranslatableException("bigTreeFailure");
      } else {
         boolean success = user.getWorld().generateTree(loc, tree);
         if (success) {
            user.sendTl("bigTreeSuccess");
         } else {
            throw new TranslatableException("bigTreeFailure");
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return (List<String>)(args.length == 1 ? Lists.newArrayList(new String[]{"redwood", "tree", "jungle", "darkoak"}) : Collections.emptyList());
   }
}
