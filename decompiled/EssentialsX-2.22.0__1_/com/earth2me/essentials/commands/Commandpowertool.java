package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.StringUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

public class Commandpowertool extends EssentialsCommand {
   public Commandpowertool() {
      super("powertool");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      String command = getFinalArg(args, 0);
      ItemStack itemStack = Inventories.getItemInHand(user.getBase());
      this.powertool(user.getSource(), user, itemStack, command);
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 3) {
         throw new Exception("When running from console, usage is: /" + commandLabel + " <player> <itemid> <command>");
      } else {
         User user = this.getPlayer(server, args, 0, true, true);
         ItemStack itemStack = this.ess.getItemDb().get(args[1]);
         String command = getFinalArg(args, 2);
         this.powertool(sender, user, itemStack, command);
      }
   }

   protected void powertool(final CommandSource sender, final User user, final ItemStack itemStack, String command) throws Exception {
      if (command != null && command.equalsIgnoreCase("d:")) {
         user.clearAllPowertools();
         sender.sendTl("powerToolClearAll");
      } else if (itemStack != null && itemStack.getType() != Material.AIR) {
         String itemName = itemStack.getType().toString().toLowerCase(Locale.ENGLISH).replaceAll("_", " ");
         List<String> powertools = (List<String>)(user.getPowertool(itemStack) != null ? user.getPowertool(itemStack) : Lists.newArrayList());
         if (command != null && !command.isEmpty()) {
            if (command.equalsIgnoreCase("l:")) {
               if (powertools.isEmpty()) {
                  throw new TranslatableException("powerToolListEmpty", itemName);
               }

               sender.sendTl("powerToolList", StringUtil.joinList(powertools), itemName);
               throw new NoChargeException();
            }

            if (command.startsWith("r:")) {
               command = command.substring(2);
               if (!powertools.contains(command)) {
                  throw new TranslatableException("powerToolNoSuchCommandAssigned", command, itemName);
               }

               powertools.remove(command);
               sender.sendTl("powerToolRemove", command, itemName);
            } else {
               if (command.startsWith("a:")) {
                  if (sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.powertool.append")) {
                     throw new TranslatableException("noPerm", "essentials.powertool.append");
                  }

                  command = command.substring(2);
                  if (powertools.contains(command)) {
                     throw new TranslatableException("powerToolAlreadySet", command, itemName);
                  }
               } else if (!powertools.isEmpty()) {
                  powertools.clear();
               }

               powertools.add(command);
               sender.sendTl("powerToolAttach", StringUtil.joinList(powertools), itemName);
            }
         } else {
            powertools.clear();
            sender.sendTl("powerToolRemoveAll", itemName);
         }

         if (!user.arePowerToolsEnabled()) {
            user.setPowerToolsEnabled(true);
            user.sendTl("powerToolsEnabled");
         }

         user.setPowertool(itemStack, powertools);
      } else {
         throw new TranslatableException("powerToolAir");
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length != 1) {
         return args[0].startsWith("a:") ? this.tabCompleteCommand(user.getSource(), server, args[0].substring(2), args, 1) : Collections.emptyList();
      } else {
         List<String> options = Lists.newArrayList(new String[]{"d:", "c:", "l:"});
         if (user.isAuthorized("essentials.powertool.append")) {
            for (String command : this.getCommands(server)) {
               options.add("a:" + command);
            }
         }

         try {
            ItemStack itemStack = Inventories.getItemInHand(user.getBase());

            for (String tool : user.getPowertool(itemStack)) {
               options.add("r:" + tool);
            }
         } catch (Exception var10) {
         }

         return options;
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else if (args.length == 2) {
         return this.getItems();
      } else if (args.length != 3) {
         return args[2].startsWith("a:") ? this.tabCompleteCommand(sender, server, args[2].substring(2), args, 3) : Collections.emptyList();
      } else {
         List<String> options = Lists.newArrayList(new String[]{"d:", "c:", "l:"});

         for (String command : this.getCommands(server)) {
            options.add("a:" + command);
         }

         try {
            User user = this.getPlayer(server, args, 0, true, true);
            ItemStack itemStack = this.ess.getItemDb().get(args[1]);

            for (String tool : user.getPowertool(itemStack)) {
               options.add("r:" + tool);
            }
         } catch (Exception var11) {
         }

         return options;
      }
   }
}
