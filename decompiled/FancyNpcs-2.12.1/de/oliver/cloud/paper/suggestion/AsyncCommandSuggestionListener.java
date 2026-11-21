package de.oliver.cloud.paper.suggestion;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import de.oliver.cloud.bukkit.BukkitPluginRegistrationHandler;
import de.oliver.cloud.bukkit.internal.BukkitHelper;
import de.oliver.cloud.paper.LegacyPaperCommandManager;
import de.oliver.cloud.suggestion.Suggestion;
import de.oliver.cloud.suggestion.Suggestions;
import de.oliver.cloud.util.StringUtils;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

class AsyncCommandSuggestionListener<C> implements SuggestionListener<C> {
   private final LegacyPaperCommandManager<C> paperCommandManager;

   AsyncCommandSuggestionListener(@NonNull final LegacyPaperCommandManager<C> paperCommandManager) {
      this.paperCommandManager = paperCommandManager;
   }

   @EventHandler
   void onTabCompletion(@NonNull final AsyncTabCompleteEvent event) {
      String strippedBuffer = event.getBuffer().startsWith("/") ? event.getBuffer().substring(1) : event.getBuffer();
      if (!strippedBuffer.trim().isEmpty()) {
         BukkitPluginRegistrationHandler<C> bukkitPluginRegistrationHandler = (BukkitPluginRegistrationHandler<C>)this.paperCommandManager
            .commandRegistrationHandler();
         String commandLabel = strippedBuffer.split(" ")[0];
         if (bukkitPluginRegistrationHandler.isRecognized(commandLabel)) {
            String input = event.getBuffer();
            if (input.charAt(0) == '/') {
               input = input.substring(1);
            }

            this.setSuggestions(
               event, this.paperCommandManager.senderMapper().map(event.getSender()), BukkitHelper.stripNamespace(this.paperCommandManager, input)
            );
            event.setHandled(true);
         }
      }
   }

   protected Suggestions<C, ?> querySuggestions(@NonNull final C commandSender, @NonNull final String input) {
      return this.paperCommandManager.suggestionFactory().suggestImmediately(commandSender, input);
   }

   protected void setSuggestions(@NonNull final AsyncTabCompleteEvent event, @NonNull final C commandSender, @NonNull final String input) {
      Suggestions<C, ?> suggestions = this.querySuggestions(commandSender, input);
      event.setCompletions(
         suggestions.list()
            .stream()
            .map(Suggestion::suggestion)
            .map(suggestion -> StringUtils.trimBeforeLastSpace(suggestion, suggestions.commandInput()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
      );
   }
}
