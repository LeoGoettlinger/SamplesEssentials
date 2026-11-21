package com.sk89q.worldedit.command.util;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.util.formatting.component.CommandListBox;
import com.sk89q.worldedit.util.formatting.component.CommandUsageBox;
import com.sk89q.worldedit.util.formatting.component.InvalidComponentException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MapBackedValueStore;

public class PrintCommandHelp {
   private PrintCommandHelp() {
   }

   private static Command detectCommand(CommandManager manager, String command) {
      Optional<Command> mapping = manager.getCommand(command);
      if (mapping.isPresent()) {
         return mapping.get();
      } else if (command.startsWith("/")) {
         mapping = manager.getCommand(command.substring(1));
         return mapping.orElse(null);
      } else {
         mapping = manager.getCommand("/" + command);
         return mapping.orElse(null);
      }
   }

   public static void help(List<String> commandPath, int page, boolean listSubCommands, CommandManager manager, Actor actor, String helpRootCommand) throws InvalidComponentException {
      if (commandPath.isEmpty()) {
         printCommands(page, manager.getAllCommands(), actor, ImmutableList.of(), helpRootCommand);
      } else {
         List<Command> visited = new ArrayList<>();
         Command currentCommand = detectCommand(manager, commandPath.get(0));
         if (currentCommand == null) {
            actor.print(Caption.of("worldedit.help.command-not-found", TextComponent.of(commandPath.get(0))));
         } else {
            visited.add(currentCommand);

            for (int i = 1; i < commandPath.size(); i++) {
               String subCommand = commandPath.get(i);
               Map<String, Command> subCommands = CommandUtil.getSubCommands(currentCommand);
               if (subCommands.isEmpty()) {
                  actor.print(Caption.of("worldedit.help.no-subcommands", TextComponent.of(toCommandString(visited)), TextComponent.of(subCommand)));
                  CommandUsageBox box = new CommandUsageBox(visited, visited.stream().map(Command::getName).collect(Collectors.joining(" ")), helpRootCommand);
                  actor.print(box.create());
                  return;
               }

               if (!subCommands.containsKey(subCommand)) {
                  actor.print(Caption.of("worldedit.help.subcommand-not-found", TextComponent.of(subCommand), TextComponent.of(toCommandString(visited))));
                  printCommands(page, CommandUtil.getSubCommands((Command)Iterables.getLast(visited)).values().stream(), actor, visited, helpRootCommand);
                  return;
               }

               currentCommand = subCommands.get(subCommand);
               visited.add(currentCommand);
            }

            Map<String, Command> subCommandsx = CommandUtil.getSubCommands(currentCommand);
            if (!subCommandsx.isEmpty() && listSubCommands) {
               printCommands(page, subCommandsx.values().stream(), actor, visited, helpRootCommand);
            } else {
               CommandUsageBox box = new CommandUsageBox(visited, toCommandString(visited), helpRootCommand);
               actor.print(box.create());
            }
         }
      }
   }

   private static String toCommandString(List<Command> visited) {
      return "/" + Joiner.on(" ").join(visited.stream().map(Command::getName).iterator());
   }

   private static void printCommands(int page, Stream<Command> commandStream, Actor actor, List<Command> commandList, String helpRootCommand) throws InvalidComponentException {
      InjectedValueStore store = MapBackedValueStore.create();
      store.injectValue(Key.of(Actor.class), context -> Optional.of(actor));
      List<Command> commands = commandStream.filter(commandx -> commandx.getCondition().satisfied(store))
         .sorted(CommandUtil.byCleanName())
         .collect(Collectors.toList());
      String used = commandList.isEmpty() ? null : toCommandString(commandList);
      CommandListBox box = new CommandListBox(
         used == null ? "Help" : "Subcommands: " + used, helpRootCommand + " -s -p %page%" + (used == null ? "" : " " + used), helpRootCommand
      );
      if (!actor.isPlayer()) {
         box.formatForConsole();
      }

      for (Command mapping : commands) {
         String alias = (commandList.isEmpty() ? "/" : "") + mapping.getName();
         String command = Stream.concat(commandList.stream(), Stream.of(mapping)).map(Command::getName).collect(Collectors.joining(" ", "/", ""));
         box.appendCommand(alias, mapping.getDescription(), command);
      }

      actor.print(box.create(page));
   }
}
