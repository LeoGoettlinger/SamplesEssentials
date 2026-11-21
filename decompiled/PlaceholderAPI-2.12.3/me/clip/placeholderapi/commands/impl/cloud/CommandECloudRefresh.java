package me.clip.placeholderapi.commands.impl.cloud;

import java.util.List;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandECloudRefresh extends PlaceholderCommand {
   public CommandECloudRefresh() {
      super("refresh");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      plugin.getCloudExpansionManager().load();
      Msg.msg(sender, "&aThe eCloud manager has been refreshed!");
   }
}
