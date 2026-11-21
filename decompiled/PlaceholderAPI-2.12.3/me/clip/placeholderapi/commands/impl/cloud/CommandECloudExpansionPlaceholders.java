package me.clip.placeholderapi.commands.impl.cloud;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandECloudExpansionPlaceholders extends PlaceholderCommand {
   public CommandECloudExpansionPlaceholders() {
      super("placeholders");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (params.isEmpty()) {
         Msg.msg(sender, "&cYou must specify the name of the expansion.");
      } else {
         CloudExpansion expansion = plugin.getCloudExpansionManager().findCloudExpansionByName(params.get(0)).orElse(null);
         if (expansion == null) {
            Msg.msg(sender, "&cThere is no expansion with the name: &f" + params.get(0));
         } else {
            List<String> placeholders = expansion.getPlaceholders();
            if (placeholders != null && !placeholders.isEmpty()) {
               List<List<String>> partitions = Lists.partition(placeholders.stream().sorted().collect(Collectors.toList()), 10);
               Msg.msg(
                  sender,
                  "&6" + placeholders.size() + "&7 placeholders: &a",
                  partitions.stream().map(partition -> String.join(", ", partition)).collect(Collectors.joining("\n"))
               );
            } else {
               Msg.msg(sender, "&cThe expansion specified does not have placeholders listed.");
            }
         }
      }
   }

   @Override
   public void complete(
      @NotNull PlaceholderAPIPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params,
      @NotNull List<String> suggestions
   ) {
      if (params.size() <= 1) {
         Stream<String> names = plugin.getCloudExpansionManager()
            .getCloudExpansions()
            .values()
            .stream()
            .map(CloudExpansion::getName)
            .map(name -> name.replace(' ', '_'));
         suggestByParameter(names, suggestions, params.isEmpty() ? null : params.get(0));
      }
   }
}
