package ac.grim.grimac.platform.bukkit.manager;

import ac.grim.grimac.platform.api.command.PlayerSelector;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.platform.bukkit.command.BukkitPlayerSelectorParser;
import ac.grim.grimac.shaded.incendo.cloud.bukkit.BukkitCommandContextKeys;
import ac.grim.grimac.shaded.incendo.cloud.parser.ParserDescriptor;
import ac.grim.grimac.shaded.incendo.cloud.suggestion.Suggestion;
import ac.grim.grimac.shaded.incendo.cloud.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitCloudPlatformCommandArguments implements CloudPlatformCommandArguments {
   private final BukkitPlayerSelectorParser<Sender> bukkitPlayerSelectorParser = new BukkitPlayerSelectorParser<>();

   @Override
   public ParserDescriptor<Sender, PlayerSelector> singlePlayerSelectorParser() {
      return this.bukkitPlayerSelectorParser.descriptor();
   }

   @Override
   public SuggestionProvider<Sender> onlinePlayerSuggestions() {
      return (context, input) -> {
         Collection<? extends Player> players = Bukkit.getOnlinePlayers();
         List<Suggestion> suggestions = new ArrayList<>(players.size());
         Player sender = context.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER) instanceof Player player ? player : null;

         for (Player playerx : players) {
            if (sender == null || sender.canSee(playerx)) {
               suggestions.add(Suggestion.suggestion(playerx.getName()));
            }
         }

         return CompletableFuture.completedFuture(suggestions);
      };
   }
}
