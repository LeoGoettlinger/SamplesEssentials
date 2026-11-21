package com.artillexstudios.axtrade.libs.lamp.bukkit.brigadier;

import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;

abstract class Commodore {
   protected static Collection<String> getAliases(Command command) {
      Objects.requireNonNull(command, "command");
      Stream<String> aliasesStream = Stream.concat(Stream.of(command.getLabel()), command.getAliases().stream());
      if (command instanceof PluginCommand) {
         String fallbackPrefix = ((PluginCommand)command).getPlugin().getName().toLowerCase().trim();
         aliasesStream = aliasesStream.flatMap(alias -> Stream.of(alias, fallbackPrefix + ":" + alias));
      }

      return aliasesStream.distinct().collect(Collectors.toList());
   }

   abstract void register(Command command, LiteralCommandNode<?> node);

   abstract void register(LiteralCommandNode<?> node);
}
