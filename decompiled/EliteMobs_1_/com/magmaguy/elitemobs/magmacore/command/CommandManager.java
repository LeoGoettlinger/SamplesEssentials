package com.magmaguy.elitemobs.magmacore.command;

import com.magmaguy.elitemobs.magmacore.command.arguments.ICommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.Generated;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager implements CommandExecutor, TabCompleter {
   private static final HashSet<CommandManager> commandManagers = new HashSet<>();
   public final List<AdvancedCommand> commands = new ArrayList<>();
   private final String commandExtension;

   public CommandManager(JavaPlugin javaPlugin, String commandExtension) {
      javaPlugin.getCommand(commandExtension).setExecutor(this);
      this.commandExtension = commandExtension;
      commandManagers.add(this);
   }

   CommandManager(String commandExtension) {
      this.commandExtension = commandExtension;
   }

   public static void shutdown() {
      commandManagers.forEach(CommandManager::clearAllCommands);
      commandManagers.clear();
   }

   public void clearAllCommands() {
      this.commands.clear();
   }

   public void registerCommand(AdvancedCommand command) {
      this.commands.add(command);
   }

   public void unregisterCommand(Command command) {
      this.commands.remove(command);
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (args.length == 0) {
         for (AdvancedCommand command : this.commands) {
            if (command.getAliases().isEmpty()) {
               if (command.getSenderType() == SenderType.PLAYER && !(sender instanceof Player)) {
                  Logger.sendMessage(sender, "This command must be run as a player!");
                  return false;
               }

               if (!this.permissionCheck(sender, command)) {
                  Logger.sendMessage(sender, "You do not have permission to run this command!");
                  return false;
               }

               command.execute(new CommandData(sender, args, command));
               return true;
            }
         }

         this.commands.stream().filter(commandx -> this.canSuggest(sender, commandx)).forEach(commandx -> sender.sendMessage(commandx.getUsage()));
         return true;
      } else {
         AdvancedCommand commandx = findMatchingCommand(this.commands, args);
         if (commandx != null) {
            if (commandx.getSenderType() == SenderType.PLAYER && !(sender instanceof Player)) {
               Logger.sendMessage(sender, "This command must be run as a player!");
               return false;
            } else if (!this.permissionCheck(sender, commandx)) {
               Logger.sendMessage(sender, "You do not have permission to run this command!");
               return false;
            } else {
               commandx.execute(new CommandData(sender, args, commandx));
               return true;
            }
         } else {
            List<AdvancedCommand> suggestions = findAliasSuggestions(this.commands, args[0])
               .stream()
               .filter(suggestionx -> this.canSuggest(sender, suggestionx))
               .toList();
            if (!suggestions.isEmpty()) {
               Logger.sendMessage(sender, "Unknown command! Did you mean one of the following?");

               for (AdvancedCommand suggestion : suggestions) {
                  sender.sendMessage(" " + suggestion.getUsage());
               }
            } else {
               Logger.sendMessage(sender, "Unknown command!");
            }

            return false;
         }
      }
   }

   static AdvancedCommand findMatchingCommand(List<AdvancedCommand> commands, String[] args) {
      if (args.length == 0) {
         return null;
      } else {
         for (AdvancedCommand command : commands) {
            if (command.isEnabled() && command.aliasMatches(args[0]) && argumentsMatch(command, args)) {
               return command;
            }
         }

         return null;
      }
   }

   static List<AdvancedCommand> findAliasSuggestions(List<AdvancedCommand> commands, String input) {
      List<AdvancedCommand> suggestions = new ArrayList<>();

      for (AdvancedCommand command : commands) {
         if (command.isEnabled() && command.aliasStartMatches(input)) {
            suggestions.add(command);
         }
      }

      return suggestions;
   }

   private static boolean argumentsMatch(AdvancedCommand command, String[] args) {
      List<ICommandArgument> definitions = command.getArgumentsList();
      int totalDefinitions = definitions.size();
      int requiredArguments = totalDefinitions;

      for (int i = 0; i < totalDefinitions; i++) {
         if (definitions.get(i).isOptional()) {
            requiredArguments = i;
            break;
         }
      }

      int providedArguments = args.length - 1;
      boolean acceptsExtraArguments = totalDefinitions > 0 && definitions.getLast().isVarargs();
      if (providedArguments < requiredArguments) {
         return false;
      } else if (!acceptsExtraArguments && providedArguments > totalDefinitions) {
         return false;
      } else {
         for (int ix = 0; ix < providedArguments; ix++) {
            ICommandArgument definition = definitionAt(command, ix);
            if (definition == null) {
               return false;
            }

            if (definition.isLiteral() && !definition.matchesInput(args[ix + 1])) {
               return false;
            }
         }

         return true;
      }
   }

   static ICommandArgument definitionAt(AdvancedCommand command, int inputPosition) {
      List<ICommandArgument> definitions = command.getArgumentsList();
      if (inputPosition < definitions.size()) {
         return definitions.get(inputPosition);
      } else {
         return !definitions.isEmpty() && definitions.getLast().isVarargs() ? definitions.getLast() : null;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
      return this.tabCompleteRestOfArguments(sender, args);
   }

   private List<String> tabCompleteRestOfArguments(CommandSender sender, String[] args) {
      if (args.length != 0 && args[0] != null) {
         List<String> completions = new ArrayList<>();
         if (args.length == 1) {
            for (AdvancedCommand command : this.commands) {
               if (this.canSuggest(sender, command) && command.aliasStartMatches(args[0])) {
                  completions.addAll(command.getAliases());
               }
            }

            return completions;
         } else {
            for (AdvancedCommand commandx : this.commands) {
               if (commandx.aliasMatches(args[0]) && this.canSuggest(sender, commandx)) {
                  int currentArgumentIndex = args.length - 2;
                  String currentArgument = args[args.length - 1];
                  ICommandArgument currentDefinition = definitionAt(commandx, currentArgumentIndex);
                  if (currentDefinition != null) {
                     boolean argumentsSoFarValid = true;

                     for (int i = 0; i < currentArgumentIndex; i++) {
                        ICommandArgument argDef = definitionAt(commandx, i);
                        if (argDef == null) {
                           argumentsSoFarValid = false;
                           break;
                        }

                        if (!argDef.matchesInput(args[i + 1])) {
                           argumentsSoFarValid = false;
                           break;
                        }
                     }

                     if (argumentsSoFarValid) {
                        completions.addAll(currentDefinition.getSuggestions(sender, currentArgument));
                     }
                  }
               }
            }

            return completions;
         }
      } else {
         return List.of();
      }
   }

   private boolean canSuggest(CommandSender sender, AdvancedCommand command) {
      return command.isEnabled() && (command.getSenderType() != SenderType.PLAYER || sender instanceof Player) && this.permissionCheck(sender, command);
   }

   private boolean permissionCheck(CommandSender commandSender, AdvancedCommand command) {
      return commandSender.hasPermission(command.getPermission())
         || command.getPermission().equalsIgnoreCase("")
         || command.getPermission().equalsIgnoreCase(this.commandExtension + ".");
   }

   @Generated
   public static HashSet<CommandManager> getCommandManagers() {
      return commandManagers;
   }
}
