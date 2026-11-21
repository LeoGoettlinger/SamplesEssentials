package me.clip.placeholderapi.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.impl.cloud.CommandECloud;
import me.clip.placeholderapi.commands.impl.local.CommandDump;
import me.clip.placeholderapi.commands.impl.local.CommandExpansionRegister;
import me.clip.placeholderapi.commands.impl.local.CommandExpansionUnregister;
import me.clip.placeholderapi.commands.impl.local.CommandHelp;
import me.clip.placeholderapi.commands.impl.local.CommandInfo;
import me.clip.placeholderapi.commands.impl.local.CommandList;
import me.clip.placeholderapi.commands.impl.local.CommandParse;
import me.clip.placeholderapi.commands.impl.local.CommandReload;
import me.clip.placeholderapi.commands.impl.local.CommandVersion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class PlaceholderCommandRouter implements CommandExecutor, TabCompleter {
   @Unmodifiable
   private static final List<PlaceholderCommand> COMMANDS = ImmutableList.of(
      new CommandHelp(),
      new CommandInfo(),
      new CommandList(),
      new CommandDump(),
      new CommandECloud(),
      new CommandParse(),
      new CommandReload(),
      new CommandVersion(),
      new CommandExpansionRegister(),
      new CommandExpansionUnregister()
   );
   @NotNull
   private final PlaceholderAPIPlugin plugin;
   @NotNull
   @Unmodifiable
   private final Map<String, PlaceholderCommand> commands;

   public PlaceholderCommandRouter(@NotNull PlaceholderAPIPlugin plugin) {
      this.plugin = plugin;
      Builder<String, PlaceholderCommand> commands = ImmutableMap.builder();

      for (PlaceholderCommand command : COMMANDS) {
         command.getLabels().forEach(label -> commands.put(label, command));
      }

      this.commands = commands.build();
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (args.length == 0) {
         PlaceholderCommand fallback = this.commands.get("version");
         if (fallback != null) {
            fallback.evaluate(this.plugin, sender, "", Collections.emptyList());
         }

         return true;
      } else {
         String search = args[0].toLowerCase(Locale.ROOT);
         PlaceholderCommand target = this.commands.get(search);
         if (target == null) {
            Msg.msg(sender, "&cUnknown command &7" + search);
            return true;
         } else {
            String permission = target.getPermission();
            if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
               Msg.msg(sender, "&cYou do not have permission to do this!");
               return true;
            } else {
               target.evaluate(this.plugin, sender, search, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
               return true;
            }
         }
      }
   }

   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      List<String> suggestions = new ArrayList<>();
      if (args.length > 1) {
         PlaceholderCommand target = this.commands.get(args[0].toLowerCase(Locale.ROOT));
         if (target != null) {
            if (target.getPermission() != null && !target.getPermission().isEmpty() && !sender.hasPermission(target.getPermission())) {
               return suggestions;
            }

            target.complete(this.plugin, sender, args[0].toLowerCase(Locale.ROOT), Arrays.asList(Arrays.copyOfRange(args, 1, args.length)), suggestions);
         }

         return suggestions;
      } else {
         Stream<String> targets = PlaceholderCommand.filterByPermission(sender, this.commands.values().stream())
            .map(PlaceholderCommand::getLabels)
            .flatMap(Collection::stream);
         PlaceholderCommand.suggestByParameter(targets, suggestions, args.length == 0 ? null : args[0]);
         return suggestions;
      }
   }
}
