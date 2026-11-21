package me.clip.placeholderapi.commands.impl.local;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandList extends PlaceholderCommand {
   public CommandList() {
      super("list");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      Set<String> identifiers = PlaceholderAPI.getRegisteredIdentifiers();
      if (identifiers.isEmpty()) {
         Msg.msg(sender, "&cThere are no placeholder hooks active!");
      } else {
         List<List<String>> partitions = Lists.partition(identifiers.stream().sorted().collect(Collectors.toList()), 10);
         Msg.msg(
            sender,
            "&7A total of &f" + identifiers.size() + "&7 placeholder hook(s) are active: &a",
            partitions.stream().map(partition -> String.join("&7, &a", partition)).collect(Collectors.joining("\n"))
         );
      }
   }
}
