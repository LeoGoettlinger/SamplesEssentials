package org.lushplugins.lushrewards.libraries.lushlib.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;

public abstract class Command extends SubCommand implements CommandExecutor, TabCompleter {
   public Command(String name) {
      super(name);
   }

   public Command addSubCommand(SubCommand subCommand) {
      super.addSubCommand(subCommand);
      return this;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
      SubCommand currSubCommand = this;
      String[] subCommandArgs = args;
      int subCommandIndex = 0;

      for (int i = 0; i < args.length; i++) {
         if (currSubCommand.getRequiredArgs(subCommandIndex).contains(args[i])) {
            subCommandIndex++;
         } else {
            subCommandIndex = 0;
            boolean found = false;

            for (SubCommand subCommand : currSubCommand.getSubCommands()) {
               if (subCommand.getName().equals(args[i])) {
                  currSubCommand = subCommand;
                  subCommandArgs = Arrays.copyOfRange(args, i + 1, args.length);
                  found = true;
                  break;
               }
            }

            if (!found) {
               break;
            }
         }
      }

      if (currSubCommand.hasRequiredPermissions(sender)) {
         return currSubCommand.execute(sender, command, label, subCommandArgs, args);
      } else {
         ChatColorHandler.sendMessage(sender, "&cInsufficient permissions");
         return true;
      }
   }

   @Nullable
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
      List<String> tabComplete = new ArrayList<>();
      SubCommand activeSubCommand = this;
      String[] subCommandArgs = new String[0];

      for (int i = 0; i < args.length; i++) {
         if (!activeSubCommand.getRequiredArgs(i).contains(args[i])) {
            for (SubCommand subCommand : activeSubCommand.getSubCommands()) {
               if (subCommand.getName().equals(args[i])) {
                  activeSubCommand = subCommand;
                  subCommandArgs = Arrays.copyOfRange(args, i + 1, args.length);
                  break;
               }
            }
         }
      }

      if (activeSubCommand.hasRequiredPermissions(sender)) {
         List<String> subCommandTabComplete = activeSubCommand.tabComplete(sender, command, label, subCommandArgs, args);
         if (subCommandTabComplete != null) {
            tabComplete.addAll(subCommandTabComplete);
         }

         if (activeSubCommand.hasRequiredArgs() && subCommandArgs.length <= activeSubCommand.getRequiredArgCount()) {
            tabComplete.addAll(activeSubCommand.getRequiredArgs(subCommandArgs.length - 1));
         } else {
            activeSubCommand.getSubCommands().forEach(subCommandx -> {
               if (subCommandx.hasRequiredPermissions(sender)) {
                  tabComplete.add(subCommandx.getName());
               }
            });
         }
      }

      List<String> wordCompletion = new ArrayList<>();
      boolean wordCompletionSuccess = false;
      int currArg = args.length - 1;

      for (String currTab : tabComplete) {
         if (currTab.startsWith(args[currArg])) {
            wordCompletion.add(currTab);
            wordCompletionSuccess = true;
         }
      }

      return wordCompletionSuccess ? wordCompletion : tabComplete;
   }
}
