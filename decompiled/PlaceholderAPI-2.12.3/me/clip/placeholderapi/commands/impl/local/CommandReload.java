package me.clip.placeholderapi.commands.impl.local;

import java.util.List;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.util.ExpansionSafetyCheck;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandReload extends PlaceholderCommand {
   public CommandReload() {
      super("reload");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (!new ExpansionSafetyCheck(plugin).runChecks()) {
         plugin.reloadConf(sender);
      }
   }
}
