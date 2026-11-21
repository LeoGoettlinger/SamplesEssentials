package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TreeType;

public class Commandtree extends EssentialsCommand {
   public Commandtree() {
      super("tree");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      TreeType tree = null;
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         for (TreeType type : TreeType.values()) {
            if (type.name().replace("_", "").equalsIgnoreCase(args[0])) {
               tree = type;
               break;
            }
         }

         if (args[0].equalsIgnoreCase("jungle")) {
            tree = TreeType.SMALL_JUNGLE;
         }

         if (tree == null) {
            throw new NotEnoughArgumentsException();
         } else {
            Location loc = LocationUtil.getTarget(user.getBase(), this.ess.getSettings().getMaxTreeCommandRange()).add(0.0, 1.0, 0.0);
            if (loc.getBlock().getType().isSolid()) {
               throw new TranslatableException("treeFailure");
            } else if (user.getWorld().generateTree(loc, tree)) {
               user.sendTl("treeSpawned");
            } else {
               user.sendTl("treeFailure");
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length != 1) {
         return Collections.emptyList();
      } else {
         List<String> options = Lists.newArrayList();

         for (TreeType type : TreeType.values()) {
            options.add(type.name().toLowerCase(Locale.ENGLISH).replace("_", ""));
         }

         return options;
      }
   }
}
