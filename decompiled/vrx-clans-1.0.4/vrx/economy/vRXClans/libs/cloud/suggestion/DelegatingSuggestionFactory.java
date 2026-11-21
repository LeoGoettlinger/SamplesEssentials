package vrx.economy.vRXClans.libs.cloud.suggestion;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.CommandManager;
import vrx.economy.vRXClans.libs.cloud.CommandTree;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandContextFactory;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.execution.ExecutionCoordinator;
import vrx.economy.vRXClans.libs.cloud.services.State;
import vrx.economy.vRXClans.libs.cloud.setting.ManagerSetting;

@API(
   status = API.Status.INTERNAL,
   consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
)
public final class DelegatingSuggestionFactory<C, S extends Suggestion> implements SuggestionFactory<C, S> {
   private final List<S> singleEmptySuggestion;
   private final CommandManager<C> commandManager;
   private final CommandTree<C> commandTree;
   private final CommandContextFactory<C> contextFactory;
   private final ExecutionCoordinator<C> executionCoordinator;
   private final SuggestionMapper<S> mapper;

   public DelegatingSuggestionFactory(
      @NonNull final CommandManager<C> commandManager,
      @NonNull final CommandTree<C> commandTree,
      @NonNull final CommandContextFactory<C> contextFactory,
      @NonNull final ExecutionCoordinator<C> executionCoordinator,
      @NonNull final SuggestionMapper<S> mapper
   ) {
      this.commandManager = commandManager;
      this.commandTree = commandTree;
      this.contextFactory = contextFactory;
      this.executionCoordinator = executionCoordinator;
      this.mapper = mapper;
      this.singleEmptySuggestion = Collections.singletonList(mapper.map(Suggestion.suggestion("")));
   }

   @NonNull
   @Override
   public CompletableFuture<Suggestions<C, S>> suggest(@NonNull final CommandContext<C> context, @NonNull final String input) {
      return this.suggestFromTree(context, input);
   }

   @NonNull
   @Override
   public CompletableFuture<Suggestions<C, S>> suggest(@NonNull final C sender, @NonNull final String input) {
      return this.suggest(this.contextFactory.create(true, sender), input);
   }

   @NonNull
   @Override
   public <S2 extends Suggestion> SuggestionFactory<C, S2> mapped(@NonNull final SuggestionMapper<S2> mapper) {
      return new DelegatingSuggestionFactory<>(this.commandManager, this.commandTree, this.contextFactory, this.executionCoordinator, this.mapper.then(mapper));
   }

   private CompletableFuture<Suggestions<C, S>> suggestFromTree(final CommandContext<C> context, final String input) {
      CommandInput commandInput = CommandInput.of(input);
      context.store("__raw_input__", commandInput.copy());
      if (this.commandManager.preprocessContext(context, commandInput) != State.ACCEPTED) {
         return this.commandManager.settings().get(ManagerSetting.FORCE_SUGGESTION)
            ? CompletableFuture.completedFuture(Suggestions.create(context, this.singleEmptySuggestion, commandInput))
            : CompletableFuture.completedFuture(Suggestions.create(context, Collections.emptyList(), commandInput));
      } else {
         return this.executionCoordinator
            .coordinateSuggestions(this.commandTree, context, commandInput, this.mapper)
            .thenApply(
               suggestions -> this.commandManager.settings().get(ManagerSetting.FORCE_SUGGESTION) && suggestions.list().isEmpty()
                  ? Suggestions.create(suggestions.commandContext(), this.singleEmptySuggestion, commandInput)
                  : suggestions
            );
      }
   }
}
