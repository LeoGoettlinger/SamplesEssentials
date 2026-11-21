package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.ess3.api.TranslatableException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.generator.WorldInfo;

public class HomesCommand extends EssentialsTreeNode {
   private static final String HOMES_USAGE = "/<command> homes (fix | delete [world])";

   public HomesCommand() {
      super("homes");
   }

   @Override
   protected void run(final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         sender.sendMessage("This sub-command provides a utility to mass-delete homes based on user options:");
         sender.sendMessage("Use \"fix\" to delete all homes inside non-existent or unloaded worlds.");
         sender.sendMessage("Use \"delete\" to delete all existing homes.");
         sender.sendMessage("Use \"delete <worldname>\" to delete all homes inside a specific world.");
         throw new Exception("/<command> homes (fix | delete [world])");
      } else {
         String var4 = args[0];
         switch (var4) {
            case "fix":
               sender.sendTl("fixingHomes");
               this.ess.runTaskAsynchronously(() -> {
                  for (UUID u : this.ess.getUsers().getAllUserUUIDs()) {
                     User user = this.ess.getUsers().loadUncachedUser(u);
                     if (user != null) {
                        for (String homeName : user.getHomes()) {
                           try {
                              if (user.getHome(homeName) == null) {
                                 user.delHome(homeName);
                              }
                           } catch (Exception var8) {
                              this.ess.getLogger().info("Unable to delete home " + homeName + " for " + user.getName());
                           }
                        }
                     }
                  }

                  sender.sendTl("fixedHomes");
               });
               break;
            case "delete":
               boolean filterByWorld = args.length >= 2;
               if (filterByWorld && Bukkit.getWorld(args[1]) == null) {
                  throw new TranslatableException("invalidWorld");
               }

               if (filterByWorld) {
                  sender.sendTl("deletingHomesWorld", args[1]);
               } else {
                  sender.sendTl("deletingHomes");
               }

               this.ess.runTaskAsynchronously(() -> {
                  for (UUID u : this.ess.getUsers().getAllUserUUIDs()) {
                     User user = this.ess.getUsers().loadUncachedUser(u);
                     if (user != null) {
                        for (String homeName : user.getHomes()) {
                           try {
                              Location home = user.getHome(homeName);
                              if (!filterByWorld || home != null && home.getWorld() != null && home.getWorld().getName().equals(args[1])) {
                                 user.delHome(homeName);
                              }
                           } catch (Exception var10) {
                              this.ess.getLogger().info("Unable to delete home " + homeName + " for " + user.getName());
                           }
                        }
                     }
                  }

                  if (filterByWorld) {
                     sender.sendTl("deletedHomesWorld", args[1]);
                  } else {
                     sender.sendTl("deletedHomes");
                  }
               });
               break;
            default:
               throw new Exception("/<command> homes (fix | delete [world])");
         }
      }
   }

   @Override
   protected List<String> tabComplete(CommandSource sender, String commandLabel, String[] args) {
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"fix", "delete"});
      } else {
         return args.length == 2 && args[0].equalsIgnoreCase("delete")
            ? Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).collect(Collectors.toList())
            : Collections.emptyList();
      }
   }
}
