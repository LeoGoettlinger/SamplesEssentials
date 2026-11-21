package vrx.economy.vRXClans.libs.cloud.paper.suggestion;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.brigadier.suggestion.TooltipSuggestion;
import vrx.economy.vRXClans.libs.cloud.paper.LegacyPaperCommandManager;
import vrx.economy.vRXClans.libs.cloud.paper.suggestion.tooltips.CompletionMapper;
import vrx.economy.vRXClans.libs.cloud.paper.suggestion.tooltips.CompletionMapperFactory;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionFactory;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestions;
import vrx.economy.vRXClans.libs.cloud.util.StringUtils;

class BrigadierAsyncCommandSuggestionListener<C> extends AsyncCommandSuggestionListener<C> {
   private final CompletionMapperFactory completionMapperFactory = CompletionMapperFactory.detectingRelocation();
   private final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory;

   BrigadierAsyncCommandSuggestionListener(@NonNull final LegacyPaperCommandManager<C> paperCommandManager) {
      super(paperCommandManager);
      this.suggestionFactory = paperCommandManager.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion);
   }

   @EventHandler
   @Override
   void onTabCompletion(@NonNull final AsyncTabCompleteEvent event) {
      super.onTabCompletion(event);
   }

   @Override
   protected Suggestions<C, ? extends TooltipSuggestion> querySuggestions(@NonNull final C commandSender, @NonNull final String input) {
      return this.suggestionFactory.suggestImmediately(commandSender, input);
   }

   @Override
   protected void setSuggestions(@NonNull final AsyncTabCompleteEvent event, @NonNull final C commandSender, @NonNull final String input) {
      CompletionMapper completionMapper = this.completionMapperFactory.createMapper();
      Suggestions<C, ? extends TooltipSuggestion> suggestions = this.querySuggestions(commandSender, input);
      event.completions(suggestions.list().stream().map(suggestion -> {
         String trim = StringUtils.trimBeforeLastSpace(suggestion.suggestion(), suggestions.commandInput());
         return trim == null ? null : suggestion.withSuggestion(trim);
      }).filter(Objects::nonNull).map(completionMapper::map).collect(Collectors.toList()));
   }
}
