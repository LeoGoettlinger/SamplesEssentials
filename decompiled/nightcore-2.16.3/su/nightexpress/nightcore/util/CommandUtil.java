package su.nightexpress.nightcore.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.command.api.NightPluginCommand;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.impl.WrappedCommand;
import su.nightexpress.nightcore.util.bridge.Software;

public class CommandUtil {
   @NonNull
   private static SimpleCommandMap getCommandMap() {
      return Software.instance().getCommandMap();
   }

   @Deprecated
   public static void register(@NonNull Plugin plugin, @NonNull NightPluginCommand command) {
      WrappedCommand wrappedCommand = new WrappedCommand(plugin, command);
      if (getCommandMap().register(plugin.getName(), wrappedCommand)) {
         command.setBackend(wrappedCommand);
      }
   }

   public static boolean register(@NonNull Plugin plugin, @NonNull WrappedCommand wrappedCommand) {
      return getCommandMap().register(plugin.getName(), wrappedCommand);
   }

   public static boolean register(@NonNull Command command, @NonNull String fallbackPrefix) {
      return getCommandMap().register(fallbackPrefix, command);
   }

   public static boolean unregister(@NonNull String name) {
      Command command = getCommand(name).orElse(null);
      return command == null ? false : unregister(command);
   }

   public static boolean unregister(@NonNull Command command) {
      SimpleCommandMap commandMap = getCommandMap();
      Map<String, Command> knownCommands = Software.instance().getKnownCommands(commandMap);
      return !command.unregister(commandMap)
         ? false
         : knownCommands.keySet().removeIf(key -> key.equalsIgnoreCase(command.getName()) || command.getAliases().contains(key));
   }

   @NonNull
   public static Set<String> getAliases(@NonNull String name) {
      return getAliases(name, false);
   }

   @NonNull
   public static Set<String> getAliases(@NonNull String name, boolean inclusive) {
      Command command = getCommand(name).orElse(null);
      if (command == null) {
         return Collections.emptySet();
      } else {
         Set<String> aliases = new HashSet<>(command.getAliases());
         if (inclusive) {
            aliases.add(command.getName());
         }

         return aliases;
      }
   }

   @NonNull
   public static Optional<Command> getCommand(@NonNull String name) {
      return getCommandMap()
         .getCommands()
         .stream()
         .filter(command -> command.getName().equalsIgnoreCase(name) || command.getLabel().equalsIgnoreCase(name) || command.getAliases().contains(name))
         .findFirst();
   }

   @NonNull
   public static String getCommandName(@NonNull String string) {
      String name = string.split(" ")[0].substring(1);
      String[] pluginPrefix = name.split(":");
      if (pluginPrefix.length == 2) {
         name = pluginPrefix[1];
      }

      return name;
   }

   @Nullable
   public static Player getPlayerOrSender(@NonNull CommandContext context, @NonNull ParsedArguments arguments, @NonNull String name) {
      Player player;
      if (arguments.hasArgument(name)) {
         player = arguments.getPlayerArgument(name);
      } else {
         if (context.getExecutor() == null) {
            context.errorPlayerOnly();
            return null;
         }

         player = context.getExecutor();
      }

      return player;
   }
}
