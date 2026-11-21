package me.clip.placeholderapi.commands.impl.local;

import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandInfo extends PlaceholderCommand {
   public CommandInfo() {
      super("info");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (params.isEmpty()) {
         Msg.msg(sender, "&cYou must specify the name of the expansion.");
      } else {
         PlaceholderExpansion expansion = plugin.getLocalExpansionManager().findExpansionByIdentifier(params.get(0)).orElse(null);
         if (expansion == null) {
            Msg.msg(sender, "&cThere is no expansion loaded with the identifier: &f" + params.get(0));
         } else {
            StringBuilder builder = new StringBuilder();
            builder.append("&7Placeholder expansion info for: &r")
               .append(expansion.getName())
               .append('\n')
               .append("&7Status: &r")
               .append(expansion.isRegistered() ? "&aRegistered" : "7cNotRegistered")
               .append('\n');
            String author = expansion.getAuthor();
            if (author != null) {
               builder.append("&7Author: &r").append(author).append('\n');
            }

            String version = expansion.getVersion();
            if (version != null) {
               builder.append("&7Version: &r").append(version).append('\n');
            }

            String requiredPlugin = expansion.getRequiredPlugin();
            if (requiredPlugin != null) {
               builder.append("&7Requires plugin: &r").append(requiredPlugin).append('\n');
            }

            List<String> placeholders = expansion.getPlaceholders();
            if (placeholders != null && !placeholders.isEmpty()) {
               builder.append("&8&m-- &7Placeholders &8&m--&r").append('\n');

               for (String placeholder : placeholders) {
                  builder.append(placeholder).append('\n');
               }
            }

            Msg.msg(sender, builder.toString());
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
         suggestByParameter(PlaceholderAPI.getRegisteredIdentifiers().stream(), suggestions, params.isEmpty() ? null : params.get(0));
      }
   }
}
