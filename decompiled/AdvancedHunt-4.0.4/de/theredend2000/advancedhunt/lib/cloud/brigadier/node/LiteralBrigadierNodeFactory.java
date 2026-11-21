package de.theredend2000.advancedhunt.lib.cloud.brigadier.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.BrigadierSetting;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.CloudBrigadierManager;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.argument.ArgumentTypeFactory;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.argument.BrigadierMapping;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.permission.BrigadierPermissionChecker;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.permission.BrigadierPermissionPredicate;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.suggestion.BrigadierSuggestionFactory;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.suggestion.CloudDelegatingSuggestionProvider;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.suggestion.SuggestionsType;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.suggestion.TooltipSuggestion;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.internal.CommandNode;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.MappedArgumentParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.aggregate.AggregateParser;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.SuggestionFactory;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public final class LiteralBrigadierNodeFactory<C, S> implements BrigadierNodeFactory<C, S, LiteralCommandNode<S>> {
   private final CloudBrigadierManager<C, S> cloudBrigadierManager;
   private final CommandManager<C> commandManager;
   private final BrigadierSuggestionFactory<C, S> brigadierSuggestionFactory;

   public LiteralBrigadierNodeFactory(
      @NonNull final CloudBrigadierManager<C, S> cloudBrigadierManager,
      @NonNull final CommandManager<C> commandManager,
      @NonNull final SuggestionFactory<C, ? extends TooltipSuggestion> suggestionFactory
   ) {
      this.cloudBrigadierManager = cloudBrigadierManager;
      this.commandManager = commandManager;
      this.brigadierSuggestionFactory = new BrigadierSuggestionFactory<>(cloudBrigadierManager, commandManager, suggestionFactory);
   }

   @NonNull
   public LiteralCommandNode<S> createNode(
      @NonNull final String label,
      @NonNull final CommandNode<C> cloudCommand,
      @NonNull final Command<S> executor,
      @NonNull final BrigadierPermissionChecker<C> permissionChecker
   ) {
      LiteralArgumentBuilder<S> literalArgumentBuilder = (LiteralArgumentBuilder<S>)LiteralArgumentBuilder.literal(label)
         .requires(this.requirement(cloudCommand, permissionChecker));
      this.updateExecutes(literalArgumentBuilder, cloudCommand, executor);
      LiteralCommandNode<S> constructedRoot = literalArgumentBuilder.build();

      for (CommandNode<C> child : cloudCommand.children()) {
         constructedRoot.addChild(this.constructCommandNode(child, permissionChecker, executor).build());
      }

      return constructedRoot;
   }

   @NonNull
   private BrigadierPermissionPredicate<C, S> requirement(
      @NonNull final CommandNode<C> cloudCommand, @NonNull final BrigadierPermissionChecker<C> permissionChecker
   ) {
      return new BrigadierPermissionPredicate<>(this.cloudBrigadierManager.senderMapper(), permissionChecker, cloudCommand);
   }

   @NonNull
   public LiteralCommandNode<S> createNode(
      @NonNull final String label,
      @NonNull final de.theredend2000.advancedhunt.lib.cloud.Command<C> cloudCommand,
      @NonNull final Command<S> executor,
      @NonNull final BrigadierPermissionChecker<C> permissionChecker
   ) {
      CommandNode<C> node = this.commandManager.commandTree().getNamedNode(cloudCommand.rootComponent().name());
      Objects.requireNonNull(node, "node");
      return this.createNode(label, node, executor, permissionChecker);
   }

   @NonNull
   public LiteralCommandNode<S> createNode(
      @NonNull final String label, @NonNull final de.theredend2000.advancedhunt.lib.cloud.Command<C> cloudCommand, @NonNull final Command<S> executor
   ) {
      return this.createNode(label, cloudCommand, executor, (sender, permission) -> this.commandManager.testPermission(sender, permission).allowed());
   }

   @NonNull
   private ArgumentBuilder<S, ?> constructCommandNode(
      @NonNull final CommandNode<C> root, @NonNull final BrigadierPermissionChecker<C> permissionChecker, @NonNull final Command<S> executor
   ) {
      if (root.component().parser() instanceof AggregateParser) {
         AggregateParser<C, ?> aggregateParser = (AggregateParser<C, ?>)root.component().parser();
         return this.constructAggregateNode(aggregateParser, root, permissionChecker, executor);
      } else {
         ArgumentBuilder<S, ?> argumentBuilder;
         if (root.component().type() == CommandComponent.ComponentType.LITERAL) {
            argumentBuilder = this.createLiteralArgumentBuilder(root.component(), root, permissionChecker);
         } else {
            argumentBuilder = this.createVariableArgumentBuilder(root.component(), root, permissionChecker);
         }

         this.updateExecutes(argumentBuilder, root, executor);

         for (CommandNode<C> node : root.children()) {
            argumentBuilder.then(this.constructCommandNode(node, permissionChecker, executor));
         }

         return argumentBuilder;
      }
   }

   @NonNull
   private ArgumentBuilder<S, ?> createLiteralArgumentBuilder(
      @NonNull final CommandComponent<C> component, @NonNull final CommandNode<C> root, @NonNull final BrigadierPermissionChecker<C> permissionChecker
   ) {
      return LiteralArgumentBuilder.literal(component.name()).requires(this.requirement(root, permissionChecker));
   }

   @NonNull
   private ArgumentBuilder<S, ?> createVariableArgumentBuilder(
      @NonNull final CommandComponent<C> component, @NonNull final CommandNode<C> root, @NonNull final BrigadierPermissionChecker<C> permissionChecker
   ) {
      ArgumentMapping<S> argumentMapping = this.getArgument(component.valueType(), component.parser());
      SuggestionProvider<S> provider;
      if (argumentMapping.suggestionsType() == SuggestionsType.CLOUD_SUGGESTIONS) {
         provider = new CloudDelegatingSuggestionProvider<>(this.brigadierSuggestionFactory, root);
      } else {
         provider = argumentMapping.suggestionProvider();
      }

      return RequiredArgumentBuilder.argument(component.name(), argumentMapping.argumentType())
         .suggests(provider)
         .requires(this.requirement(root, permissionChecker));
   }

   @NonNull
   private ArgumentBuilder<S, ?> constructAggregateNode(
      @NonNull final AggregateParser<C, ?> aggregateParser,
      @NonNull final CommandNode<C> root,
      @NonNull final BrigadierPermissionChecker<C> permissionChecker,
      @NonNull final Command<S> executor
   ) {
      Iterator<CommandComponent<C>> components = aggregateParser.components().iterator();
      List<ArgumentBuilder<S, ?>> argumentBuilders = new ArrayList<>();

      while (components.hasNext()) {
         CommandComponent<C> component = components.next();
         ArgumentBuilder<S, ?> fragmentBuilder = this.createVariableArgumentBuilder(component, root, permissionChecker);
         if (this.cloudBrigadierManager.settings().get(BrigadierSetting.FORCE_EXECUTABLE)) {
            fragmentBuilder.executes(executor);
         }

         argumentBuilders.add(fragmentBuilder);
      }

      ArgumentBuilder<S, ?> tail = argumentBuilders.get(argumentBuilders.size() - 1);

      for (CommandNode<C> node : root.children()) {
         tail.then(this.constructCommandNode(node, permissionChecker, executor));
      }

      this.updateExecutes(tail, root, executor);

      for (int i = argumentBuilders.size() - 1; i > 0; i--) {
         argumentBuilders.get(i - 1).then(argumentBuilders.get(i));
      }

      return argumentBuilders.get(0);
   }

   @NonNull
   private <K extends ArgumentParser<C, ?>> ArgumentMapping<S> getArgument(@NonNull final TypeToken<?> valueType, @NonNull final K argumentParser) {
      if (argumentParser instanceof MappedArgumentParser) {
         return this.getArgument(valueType, (K)((MappedArgumentParser)argumentParser).baseParser());
      } else {
         BrigadierMapping<C, K, S> mapping = this.cloudBrigadierManager.mappings().mapping((Class<K>)argumentParser.getClass());
         if (mapping != null && mapping.mapper() != null) {
            SuggestionProvider<S> suggestionProvider = mapping.makeSuggestionProvider(argumentParser);
            return suggestionProvider == BrigadierMapping.delegateSuggestions()
               ? ImmutableArgumentMapping.<S>builder()
                  .argumentType((ArgumentType<?>)mapping.mapper().apply(argumentParser))
                  .suggestionsType(SuggestionsType.CLOUD_SUGGESTIONS)
                  .build()
               : ImmutableArgumentMapping.<S>builder()
                  .argumentType((ArgumentType<?>)mapping.mapper().apply(argumentParser))
                  .suggestionProvider(suggestionProvider)
                  .build();
         } else {
            return this.getDefaultMapping(valueType);
         }
      }
   }

   @NonNull
   private ArgumentMapping<S> getDefaultMapping(@NonNull final TypeToken<?> type) {
      ArgumentTypeFactory<?> argumentTypeSupplier = this.cloudBrigadierManager.defaultArgumentTypeFactories().get(GenericTypeReflector.erase(type.getType()));
      if (argumentTypeSupplier != null) {
         ArgumentType<?> argumentType = argumentTypeSupplier.create();
         if (argumentType != null) {
            return ImmutableArgumentMapping.<S>builder().argumentType(argumentType).build();
         }
      }

      return ImmutableArgumentMapping.<S>builder().argumentType(StringArgumentType.word()).suggestionsType(SuggestionsType.CLOUD_SUGGESTIONS).build();
   }

   private void updateExecutes(@NonNull final ArgumentBuilder<S, ?> builder, @NonNull final CommandNode<C> node, @NonNull final Command<S> executor) {
      if (this.cloudBrigadierManager.settings().get(BrigadierSetting.FORCE_EXECUTABLE)
         || node.isLeaf()
         || node.component().optional()
         || node.command() != null
         || node.children().stream().map(CommandNode::component).filter(Objects::nonNull).anyMatch(CommandComponent::optional)) {
         builder.executes(executor);
      }
   }
}
