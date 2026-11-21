package me.clip.placeholderapi.commands.impl.local;

import java.util.List;
import java.util.Optional;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandExpansionUnregister extends PlaceholderCommand {
   public CommandExpansionUnregister() {
      super("unregister");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (params.isEmpty()) {
         Msg.msg(sender, "&cYou must specify the name of the expansion.");
      } else {
         Optional<PlaceholderExpansion> expansion = plugin.getLocalExpansionManager().findExpansionByName(params.get(0));
         if (!expansion.isPresent()) {
            Msg.msg(sender, "&cThere is no expansion loaded with the identifier: &f" + params.get(0));
         } else {
            String message = !expansion.get().unregister() ? "&cFailed to unregister expansion: &f" : "&aSuccessfully unregistered expansion: &f";
            Msg.msg(sender, message + expansion.get().getName());
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
