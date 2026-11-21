package me.clip.placeholderapi.commands.impl.cloud;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandECloud extends PlaceholderCommand {
   @Unmodifiable
   private static final List<PlaceholderCommand> COMMANDS = ImmutableList.of(
      new CommandECloudClear(),
      new CommandECloudStatus(),
      new CommandECloudUpdate(),
      new CommandECloudRefresh(),
      new CommandECloudDownload(),
      new CommandECloudExpansionInfo(),
      new CommandECloudExpansionList(),
      new CommandECloudExpansionPlaceholders()
   );
   @NotNull
   @Unmodifiable
   private final Map<String, PlaceholderCommand> commands;

   public CommandECloud() {
      super("ecloud");
      Builder<String, PlaceholderCommand> commands = ImmutableMap.builder();

      for (PlaceholderCommand command : COMMANDS) {
         command.getLabels().forEach(label -> commands.put(label, command));
      }

      this.commands = commands.build();
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (params.isEmpty()) {
         Msg.msg(
            sender,
            "&b&lPlaceholderAPI &8- &7eCloud Help Menu &8- ",
            " ",
            "&b/papi &fecloud status",
            "  &7&oView status of the eCloud",
            "&b/papi &fecloud list <all/{author}/installed> {page}",
            "  &7&oList all/author specific available expansions",
            "&b/papi &fecloud info <expansion name> {version}",
            "  &7&oView information about a specific expansion available on the eCloud",
            "&b/papi &fecloud placeholders <expansion name>",
            "  &7&oView placeholders for an expansion",
            "&b/papi &fecloud download <expansion name> {version}",
            "  &7&oDownload an expansion from the eCloud",
            "&b/papi &fecloud update <expansion name/all>",
            "  &7&oUpdate a specific/all installed expansions",
            "&b/papi &fecloud refresh",
            "  &7&oFetch the most up to date list of expansions available.",
            "&b/papi &fecloud clear",
            "  &7&oClear the expansion cloud cache."
         );
      } else {
         String search = params.get(0).toLowerCase(Locale.ROOT);
         PlaceholderCommand target = this.commands.get(search);
         if (target == null) {
            Msg.msg(sender, "&cUnknown command &7ecloud " + search);
         } else {
            String permission = target.getPermission();
            if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
               Msg.msg(sender, "&cYou do not have permission to do this!");
            } else if (!plugin.getPlaceholderAPIConfig().isCloudEnabled()) {
               Msg.msg(sender, "&cThe eCloud Manager is not enabled! To enable it, set 'cloud_enabled' to true and reload the plugin.");
            } else if (!target.getLabel().equalsIgnoreCase("refresh") && plugin.getCloudExpansionManager().isEmpty()) {
               Msg.msg(
                  sender,
                  "&cThere is no available data from the eCloud. Please try running &f/papi ecloud refresh&c. If this does not resolve the issue, the eCloud may be blocked by your firewall, server host, or service provider.\n&r\n&cMore information: &fhttps://placeholderapi.com/ecloud-blocked"
               );
            } else {
               target.evaluate(plugin, sender, search, params.subList(1, params.size()));
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
         Stream<String> targets = filterByPermission(sender, this.commands.values().stream()).map(PlaceholderCommand::getLabels).flatMap(Collection::stream);
         suggestByParameter(targets, suggestions, params.isEmpty() ? null : params.get(0));
      } else {
         String search = params.get(0).toLowerCase(Locale.ROOT);
         PlaceholderCommand target = this.commands.get(search);
         if (target != null) {
            target.complete(plugin, sender, search, params.subList(1, params.size()), suggestions);
         }
      }
   }

   static {
      COMMANDS.forEach(command -> command.setPermission("placeholderapi.ecloud." + command.getLabel()));
   }
}
