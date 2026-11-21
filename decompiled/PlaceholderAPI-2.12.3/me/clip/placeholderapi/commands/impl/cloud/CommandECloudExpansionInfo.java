package me.clip.placeholderapi.commands.impl.cloud;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandECloudExpansionInfo extends PlaceholderCommand {
   public CommandECloudExpansionInfo() {
      super("info");
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
            StringBuilder builder = new StringBuilder();
            builder.append("&bExpansion: &f")
               .append(expansion.shouldUpdate() ? "&e" : "&a")
               .append(expansion.getName())
               .append('\n')
               .append("&bAuthor: &f")
               .append(expansion.getAuthor())
               .append('\n');
            if (params.size() < 2) {
               builder.append("&bLatest Version: &f")
                  .append(expansion.getLatestVersion())
                  .append('\n')
                  .append("&bReleased: &f")
                  .append(expansion.getTimeSinceLastUpdate())
                  .append(" ago")
                  .append('\n')
                  .append("&bVerified: ")
                  .append(expansion.getVersion().isVerified() ? "&a&l✔" : "&c&l❌")
                  .append('\n')
                  .append("&bRelease Notes: &f")
                  .append(expansion.getVersion().getReleaseNotes())
                  .append('\n');
            } else {
               CloudExpansion.Version version = expansion.getVersion(params.get(1));
               if (version == null) {
                  Msg.msg(sender, "&cCould not find specified version: &f" + params.get(1), "&aVersions: &f" + expansion.getAvailableVersions());
                  return;
               }

               builder.append("&bVersion: &f")
                  .append(version.getVersion())
                  .append('\n')
                  .append("&bVerified: ")
                  .append(version.isVerified() ? "&a&l✔" : "&c&l❌")
                  .append('\n')
                  .append("&bRelease Notes: &f")
                  .append(version.getReleaseNotes())
                  .append('\n')
                  .append("&bDownload URL: &f")
                  .append(version.getUrl())
                  .append('\n');
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
      if (params.size() <= 2) {
         if (params.size() <= 1) {
            Stream<String> names = plugin.getCloudExpansionManager()
               .getCloudExpansions()
               .values()
               .stream()
               .map(CloudExpansion::getName)
               .map(name -> name.replace(' ', '_'));
            suggestByParameter(names, suggestions, params.isEmpty() ? null : params.get(0));
         } else {
            Optional<CloudExpansion> expansion = plugin.getCloudExpansionManager().findCloudExpansionByName(params.get(0));
            if (expansion.isPresent()) {
               suggestByParameter(expansion.get().getAvailableVersions().stream(), suggestions, params.get(1));
            }
         }
      }
   }
}
