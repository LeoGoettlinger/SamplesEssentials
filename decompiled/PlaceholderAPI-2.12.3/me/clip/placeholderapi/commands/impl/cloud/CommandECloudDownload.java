package me.clip.placeholderapi.commands.impl.cloud;

import java.util.Arrays;
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

public final class CommandECloudDownload extends PlaceholderCommand {
   public CommandECloudDownload() {
      super("download");
   }

   private boolean isBlockedExpansion(String name) {
      String env = System.getenv("PAPI_BLOCKED_EXPANSIONS");
      return env == null ? false : Arrays.stream(env.split(",")).anyMatch(s -> s.equalsIgnoreCase(name));
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      if (params.isEmpty()) {
         Msg.msg(sender, "&cYou must supply the name of an expansion.");
      } else if (this.isBlockedExpansion(params.get(0))) {
         Msg.msg(sender, "&cThis expansion can't be downloaded.");
      } else {
         CloudExpansion expansion = plugin.getCloudExpansionManager().findCloudExpansionByName(params.get(0)).orElse(null);
         if (expansion == null) {
            Msg.msg(sender, "&cFailed to find an expansion named: &f" + params.get(0));
         } else {
            CloudExpansion.Version version;
            if (params.size() < 2) {
               version = expansion.getVersion(expansion.getLatestVersion());
               if (version == null) {
                  Msg.msg(sender, "&cCould not find latest version for expansion.");
                  return;
               }
            } else {
               version = expansion.getVersion(params.get(1));
               if (version == null) {
                  Msg.msg(sender, "&cCould not find specified version: &f" + params.get(1), "&7Available versions: &f" + expansion.getAvailableVersions());
                  return;
               }
            }

            if (!version.isVerified()) {
               Msg.msg(
                  sender,
                  "&cThe expansion '&f" + params.get(0) + "&c' is not verified and can only be downloaded manually from &fhttps://ecloud.placeholderapi.com"
               );
            } else {
               plugin.getCloudExpansionManager()
                  .downloadExpansion(expansion, version)
                  .whenComplete(
                     (file, exception) -> {
                        if (exception != null) {
                           Msg.msg(sender, "&cFailed to download expansion: &f" + exception.getMessage());
                        } else {
                           Msg.msg(
                              sender,
                              "&aSuccessfully downloaded expansion &f" + expansion.getName() + " [" + version.getVersion() + "] &ato file: &f" + file.getName(),
                              "&aMake sure to type &f/papi reload &ato enable your new expansion!"
                           );
                           plugin.getCloudExpansionManager().load();
                        }
                     }
                  );
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
