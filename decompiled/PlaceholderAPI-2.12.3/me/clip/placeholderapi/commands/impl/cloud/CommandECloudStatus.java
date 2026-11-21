package me.clip.placeholderapi.commands.impl.cloud;

import java.util.List;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.manager.CloudExpansionManager;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandECloudStatus extends PlaceholderCommand {
   public CommandECloudStatus() {
      super("status");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      CloudExpansionManager manager = plugin.getCloudExpansionManager();
      int updateCount = manager.getCloudUpdateCount();
      int authorCount = manager.getCloudExpansionAuthorCount();
      int expansionCount = manager.getCloudExpansions().size();
      StringBuilder builder = new StringBuilder();
      builder.append("&bThere are &a").append(expansionCount).append("&b expansions available on the eCloud.").append('\n');
      builder.append("&7A total of &f").append(authorCount).append("&7 authors have contributed to the eCloud.").append('\n');
      if (updateCount > 0) {
         builder.append("&eYou have &f")
            .append(updateCount)
            .append(updateCount > 1 ? "&e expansions" : "&e expansion")
            .append(" installed that ")
            .append(updateCount > 1 ? "have an" : "has an")
            .append(" update available.");
      }

      Msg.msg(sender, builder.toString());
   }
}
