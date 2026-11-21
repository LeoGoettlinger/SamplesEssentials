package me.clip.placeholderapi.commands.impl.local;

import java.util.List;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandVersion extends PlaceholderCommand {
   public CommandVersion() {
      super("version");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      PluginDescriptionFile description = plugin.getDescription();
      Msg.msg(
         sender,
         "&b&lPlaceholderAPI &7(&f" + description.getVersion() + "&7)",
         "&7Author: &f" + description.getAuthors(),
         "&7PAPI Commands: &b/papi &fhelp",
         "&7eCloud Commands&8: &b/papi &fecloud"
      );
   }
}
