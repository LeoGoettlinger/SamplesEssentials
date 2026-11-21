package de.oliver.cloud;

import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.component.DefaultValue;
import de.oliver.cloud.description.CommandDescription;
import de.oliver.cloud.description.Description;
import de.oliver.cloud.execution.CommandExecutionHandler;
import de.oliver.cloud.key.CloudKey;
import de.oliver.cloud.meta.CommandMeta;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.parser.aggregate.AggregateParser;
import de.oliver.cloud.parser.aggregate.AggregateParserPairBuilder;
import de.oliver.cloud.parser.aggregate.AggregateParserTripletBuilder;
import de.oliver.cloud.parser.flag.CommandFlag;
import de.oliver.cloud.parser.flag.CommandFlagParser;
import de.oliver.cloud.parser.standard.LiteralParser;
import de.oliver.cloud.permission.Permission;
import de.oliver.cloud.permission.PredicatePermission;
import de.oliver.cloud.suggestion.SuggestionProvider;
import de.oliver.cloud.type.tuple.Pair;
import de.oliver.cloud.type.tuple.Triplet;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.STABLE
)
public class Command<C> {
   private final List<CommandComponent<C>> components;
   @Nullable
   private final CommandComponent<C> flagComponent;
   private final CommandExecutionHandler<C> commandExecutionHandler;
   private final Type senderType;
   private final Permission permission;
   private final CommandMeta commandMeta;
   private final CommandDescription commandDescription;

   @API(
      status = Status.INTERNAL
   )
   public Command(
      @NonNull final List<CommandComponent<C>> commandComponents,
      @NonNull final CommandExecutionHandler<C> commandExecutionHandler,
      @Nullable final Type senderType,
      @NonNull final Permission permission,
      @NonNull final CommandMeta commandMeta,
      @NonNull final CommandDescription commandDescription
   ) {
      this.components = Objects.requireNonNull(commandComponents, "Command components may not be null");
      if (this.components.isEmpty()) {
         throw new IllegalArgumentException("At least one command component is required");
      } else {
         this.flagComponent = this.components.stream().filter(ca -> ca.type() == CommandComponent.ComponentType.FLAG).findFirst().orElse(null);
         boolean foundOptional = false;

         for (CommandComponent<C> component : this.components) {
            if (component.name().isEmpty()) {
               throw new IllegalArgumentException("Component names may not be empty");
            }

            if (foundOptional && component.required()) {
               throw new IllegalArgumentException(String.format("Command component '%s' cannot be placed after an optional argument", component.name()));
            }

            if (!component.required()) {
               foundOptional = true;
            }
         }

         this.commandExecutionHandler = commandExecutionHandler;
         this.senderType = senderType;
         this.permission = permission;
         this.commandMeta = commandMeta;
         this.commandDescription = commandDescription;
      }
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public static <C> Command.Builder<C> newBuilder(
      @NonNull final String commandName, @NonNull final CommandMeta commandMeta, @NonNull final Description description, @NonNull final String... aliases
   ) {
      List<CommandComponent<C>> commands = new ArrayList<>();
      ParserDescriptor<C, String> staticParser = LiteralParser.literal(commandName, aliases);
      commands.add(CommandComponent.builder(commandName, staticParser).description(description).build());
      return new Command.Builder<>(
         null,
         commandMeta,
         null,
         commands,
         CommandExecutionHandler.noOpCommandExecutionHandler(),
         Permission.empty(),
         Collections.emptyList(),
         CommandDescription.empty()
      );
   }

   @NonNull
   public static <C> Command.Builder<C> newBuilder(@NonNull final String commandName, @NonNull final CommandMeta commandMeta, @NonNull final String... aliases) {
      List<CommandComponent<C>> commands = new ArrayList<>();
      ParserDescriptor<C, String> staticParser = LiteralParser.literal(commandName, aliases);
      commands.add(CommandComponent.<C, String>builder().name(commandName).parser(staticParser).build());
      return new Command.Builder<>(
         null,
         commandMeta,
         null,
         commands,
         CommandExecutionHandler.noOpCommandExecutionHandler(),
         Permission.empty(),
         Collections.emptyList(),
         CommandDescription.empty()
      );
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public List<CommandComponent<C>> components() {
      return new ArrayList<>(this.components);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public CommandComponent<C> rootComponent() {
      return this.components.get(0);
   }

   @API(
      status = Status.EXPERIMENTAL
   )
   @NonNull
   public List<CommandComponent<C>> nonFlagArguments() {
      List<CommandComponent<C>> components = new ArrayList<>(this.components);
      if (this.flagComponent() != null) {
         components.remove(this.flagComponent());
      }

      return components;
   }

   @API(
      status = Status.STABLE
   )
   @Nullable
   public CommandComponent<C> flagComponent() {
      return this.flagComponent;
   }

   @API(
      status = Status.STABLE
   )
   @Nullable
   public CommandFlagParser<C> flagParser() {
      CommandComponent<C> flagComponent = this.flagComponent();
      return flagComponent == null ? null : (CommandFlagParser)flagComponent.parser();
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public CommandExecutionHandler<C> commandExecutionHandler() {
      return this.commandExecutionHandler;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public Optional<TypeToken<? extends C>> senderType() {
      return this.senderType == null ? Optional.empty() : Optional.of((TypeToken<? extends C>)TypeToken.get(this.senderType));
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public Permission commandPermission() {
      return this.permission;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public CommandMeta commandMeta() {
      return this.commandMeta;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public CommandDescription commandDescription() {
      return this.commandDescription;
   }

   @Override
   public final String toString() {
      StringBuilder stringBuilder = new StringBuilder();

      for (CommandComponent<C> component : this.components()) {
         stringBuilder.append(component.name()).append(' ');
      }

      String build = stringBuilder.toString();
      return build.substring(0, build.length() - 1);
   }

   @API(
      status = Status.STABLE
   )
   public static final class Builder<C> {
      private final CommandMeta commandMeta;
      private final List<CommandComponent<C>> commandComponents;
      private final CommandExecutionHandler<C> commandExecutionHandler;
      private final Type senderType;
      private final Permission permission;
      private final CommandManager<C> commandManager;
      private final Collection<CommandFlag<?>> flags;
      private final CommandDescription commandDescription;

      private Builder(
         @Nullable final CommandManager<C> commandManager,
         @NonNull final CommandMeta commandMeta,
         @Nullable final Type senderType,
         @NonNull final List<CommandComponent<C>> commandComponents,
         @NonNull final CommandExecutionHandler<C> commandExecutionHandler,
         @NonNull final Permission permission,
         @NonNull final Collection<CommandFlag<?>> flags,
         @NonNull final CommandDescription commandDescription
      ) {
         this.commandManager = commandManager;
         this.senderType = senderType;
         this.commandComponents = Objects.requireNonNull(commandComponents, "Components may not be null");
         this.commandExecutionHandler = Objects.requireNonNull(commandExecutionHandler, "Execution handler may not be null");
         this.permission = Objects.requireNonNull(permission, "Permission may not be null");
         this.commandMeta = Objects.requireNonNull(commandMeta, "Meta may not be null");
         this.flags = Objects.requireNonNull(flags, "Flags may not be null");
         this.commandDescription = Objects.requireNonNull(commandDescription, "Command description may not be null");
      }

      @API(
         status = Status.STABLE
      )
      @Nullable
      public TypeToken<? extends C> senderType() {
         return (TypeToken<? extends C>)(this.senderType == null ? null : TypeToken.get(this.senderType));
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Permission commandPermission() {
         return this.permission;
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public CommandMeta meta() {
         return this.commandMeta;
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Command.Builder<C> apply(@NonNull final Command.Builder.Applicable<C> applicable) {
         return applicable.applyToCommandBuilder(this);
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <V> Command.Builder<C> meta(@NonNull final CloudKey<V> key, @NonNull final V value) {
         CommandMeta commandMeta = CommandMeta.builder().with(this.commandMeta).with(key, value).build();
         return new Command.Builder<>(
            this.commandManager,
            commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      @NonNull
      public Command.Builder<C> manager(@Nullable final CommandManager<C> commandManager) {
         return new Command.Builder<>(
            commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Command.Builder<C> commandDescription(@NonNull final CommandDescription commandDescription) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            commandDescription
         );
      }

      @NonNull
      public CommandDescription commandDescription() {
         return this.commandDescription;
      }

      @NonNull
      public Command.Builder<C> commandDescription(@NonNull final Description commandDescription) {
         return this.commandDescription(CommandDescription.commandDescription(commandDescription));
      }

      @NonNull
      public Command.Builder<C> commandDescription(@NonNull final Description commandDescription, @NonNull final Description verboseCommandDescription) {
         return this.commandDescription(CommandDescription.commandDescription(commandDescription, verboseCommandDescription));
      }

      @NonNull
      public Command.Builder<C> literal(@NonNull final String main, final String... aliases) {
         return this.required(main, LiteralParser.literal(main, aliases));
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Command.Builder<C> literal(@NonNull final String main, @NonNull final Description description, final String... aliases) {
         return this.required(main, LiteralParser.literal(main, aliases), description);
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(@NonNull final String name, @NonNull final CommandComponent.Builder<? super C, T> builder) {
         return this.argument(builder.name(name).required());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(@NonNull final String name, @NonNull final CommandComponent.Builder<? super C, T> builder) {
         return this.argument(builder.name(name).optional());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(@NonNull final CommandComponent.Builder<? super C, T> builder) {
         return this.argument(builder.required());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(@NonNull final CommandComponent.Builder<? super C, T> builder) {
         return this.argument(builder.optional());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(@NonNull final String name, @NonNull final ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(
         @NonNull final String name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(@NonNull final CloudKey<T> name, @NonNull final ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(
         @NonNull final CloudKey<T> name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(
         @NonNull final CloudKey<T> name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(
         @NonNull final CloudKey<T> name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final Description description,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(
         @NonNull final String name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> required(
         @NonNull final String name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final Description description,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(@NonNull final String name, @NonNull final ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).optional().build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final String name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional().suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(@NonNull final CloudKey<T> name, @NonNull final ParserDescriptor<? super C, T> parser) {
         return this.argument(CommandComponent.builder(name, parser).optional().build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final CloudKey<T> name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional().suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final String name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final String name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final Description description,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final CloudKey<T> name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final CloudKey<T> name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final Description description,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).description(description).optional().suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final String name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final DefaultValue<? super C, T> defaultValue
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final String name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final DefaultValue<? super C, T> defaultValue,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final CloudKey<T> name, @NonNull final ParserDescriptor<? super C, T> parser, @NonNull final DefaultValue<? super C, T> defaultValue
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final CloudKey<T> name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final DefaultValue<? super C, T> defaultValue,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final String name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final DefaultValue<? super C, T> defaultValue,
         @NonNull final Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final String name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final DefaultValue<? super C, T> defaultValue,
         @NonNull final Description description,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final CloudKey<T> name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final DefaultValue<? super C, T> defaultValue,
         @NonNull final Description description
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> optional(
         @NonNull final CloudKey<T> name,
         @NonNull final ParserDescriptor<? super C, T> parser,
         @NonNull final DefaultValue<? super C, T> defaultValue,
         @NonNull final Description description,
         @NonNull final SuggestionProvider<? super C> suggestions
      ) {
         return this.argument(CommandComponent.builder(name, parser).optional(defaultValue).description(description).suggestionProvider(suggestions).build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Command.Builder<C> argument(@NonNull final CommandComponent<? super C> argument) {
         List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
         commandComponents.add((CommandComponent<C>)argument);
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <T> Command.Builder<C> argument(final CommandComponent.Builder<? super C, T> builder) {
         return this.commandManager != null ? this.argument(builder.commandManager(this.commandManager).build()) : this.argument(builder.build());
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V> Command.Builder<C> requiredArgumentPair(
         @NonNull final String name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V> Command.Builder<C> requiredArgumentPair(
         @NonNull final CloudKey<Pair<U, V>> name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V> Command.Builder<C> optionalArgumentPair(
         @NonNull final String name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V> Command.Builder<C> optionalArgumentPair(
         @NonNull final CloudKey<Pair<U, V>> name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).build(), description);
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, O> Command.Builder<C> requiredArgumentPair(
         @NonNull final String name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final AggregateParserPairBuilder.Mapper<C, U, V, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, O> Command.Builder<C> requiredArgumentPair(
         @NonNull final CloudKey<O> name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final AggregateParserPairBuilder.Mapper<C, U, V, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, O> Command.Builder<C> optionalArgumentPair(
         @NonNull final String name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final AggregateParserPairBuilder.Mapper<C, U, V, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, O> Command.Builder<C> optionalArgumentPair(
         @NonNull final CloudKey<O> name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final AggregateParserPairBuilder.Mapper<C, U, V, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.pairBuilder(firstName, firstParser, secondName, secondParser).withMapper(outputType, mapper).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W> Command.Builder<C> requiredArgumentTriplet(
         @NonNull final String name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W> Command.Builder<C> requiredArgumentTriplet(
         @NonNull final CloudKey<Triplet<U, V, W>> name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W> Command.Builder<C> optionalArgumentTriplet(
         @NonNull final String name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W> Command.Builder<C> optionalArgumentTriplet(
         @NonNull final CloudKey<Triplet<U, V, W>> name,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name, AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).build(), description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W, O> Command.Builder<C> requiredArgumentTriplet(
         @NonNull final String name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final AggregateParserTripletBuilder.Mapper<C, U, V, W, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W, O> Command.Builder<C> requiredArgumentTriplet(
         @NonNull final CloudKey<O> name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final AggregateParserTripletBuilder.Mapper<C, U, V, W, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.required(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W, O> Command.Builder<C> optionalArgumentTriplet(
         @NonNull final String name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final AggregateParserTripletBuilder.Mapper<C, U, V, W, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public <U, V, W, O> Command.Builder<C> optionalArgumentTriplet(
         @NonNull final CloudKey<O> name,
         @NonNull final TypeToken<O> outputType,
         @NonNull final String firstName,
         @NonNull final ParserDescriptor<C, U> firstParser,
         @NonNull final String secondName,
         @NonNull final ParserDescriptor<C, V> secondParser,
         @NonNull final String thirdName,
         @NonNull final ParserDescriptor<C, W> thirdParser,
         @NonNull final AggregateParserTripletBuilder.Mapper<C, U, V, W, O> mapper,
         @NonNull final Description description
      ) {
         if (this.commandManager == null) {
            throw new IllegalStateException("This cannot be called from a command that has no command manager attached");
         } else {
            return this.optional(
               name,
               AggregateParser.tripletBuilder(firstName, firstParser, secondName, secondParser, thirdName, thirdParser).withMapper(outputType, mapper).build(),
               description
            );
         }
      }

      @NonNull
      public Command.Builder<C> handler(@NonNull final CommandExecutionHandler<C> commandExecutionHandler) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      @NonNull
      public Command.Builder<C> futureHandler(@NonNull final CommandExecutionHandler.FutureCommandExecutionHandler<C> commandExecutionHandler) {
         return this.handler(commandExecutionHandler);
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public CommandExecutionHandler<C> handler() {
         return this.commandExecutionHandler;
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Command.Builder<C> prependHandler(@NonNull final CommandExecutionHandler<C> handler) {
         return this.handler(CommandExecutionHandler.delegatingExecutionHandler(Arrays.asList(handler, this.handler())));
      }

      @API(
         status = Status.STABLE
      )
      @NonNull
      public Command.Builder<C> appendHandler(@NonNull final CommandExecutionHandler<C> handler) {
         return this.handler(CommandExecutionHandler.delegatingExecutionHandler(Arrays.asList(this.handler(), handler)));
      }

      @NonNull
      public <N extends C> Command.Builder<N> senderType(@NonNull final Class<? extends N> senderType) {
         return this.senderType(TypeToken.get(senderType));
      }

      @NonNull
      public <N extends C> Command.Builder<N> senderType(@NonNull final TypeToken<? extends N> senderType) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            senderType.getType(),
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            this.flags,
            this.commandDescription
         );
      }

      @NonNull
      public Command.Builder<C> permission(@NonNull final Permission permission) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            permission,
            this.flags,
            this.commandDescription
         );
      }

      @NonNull
      public Command.Builder<C> permission(@NonNull final PredicatePermission<C> permission) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            permission,
            this.flags,
            this.commandDescription
         );
      }

      @NonNull
      public Command.Builder<C> permission(@NonNull final String permission) {
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            Permission.of(permission),
            this.flags,
            this.commandDescription
         );
      }

      @NonNull
      public <N extends C> Command.Builder<N> proxies(@NonNull final Command<N> command) {
         Command.Builder<N> builder;
         if (command.senderType().isPresent()) {
            builder = this.senderType(command.senderType().get());
         } else {
            builder = this;
         }

         for (CommandComponent<N> component : command.components()) {
            if (component.type() != CommandComponent.ComponentType.LITERAL) {
               builder = builder.argument(component);
            }
         }

         if (this.permission.permissionString().isEmpty()) {
            builder = builder.permission(command.commandPermission());
         }

         return builder.handler(command.commandExecutionHandler);
      }

      @NonNull
      public <T> Command.Builder<C> flag(@NonNull final CommandFlag<T> flag) {
         List<CommandFlag<?>> flags = new ArrayList<>(this.flags);
         flags.add(flag);
         return new Command.Builder<>(
            this.commandManager,
            this.commandMeta,
            this.senderType,
            this.commandComponents,
            this.commandExecutionHandler,
            this.permission,
            Collections.unmodifiableList(flags),
            this.commandDescription
         );
      }

      @NonNull
      public <T> Command.Builder<C> flag(@NonNull final CommandFlag.Builder<C, T> builder) {
         return this.flag(builder.build());
      }

      @NonNull
      public Command<C> build() {
         List<CommandComponent<C>> commandComponents = new ArrayList<>(this.commandComponents);
         if (!this.flags.isEmpty()) {
            CommandFlagParser<C> flagParser = new CommandFlagParser<>(this.flags);
            CommandComponent<C> flagComponent = CommandComponent.<C, Object>builder()
               .name("flags")
               .parser(flagParser)
               .valueType(Object.class)
               .description(Description.of("Command flags"))
               .build();
            commandComponents.add(flagComponent);
         }

         return new Command<>(
            Collections.unmodifiableList(commandComponents),
            this.commandExecutionHandler,
            this.senderType,
            this.permission,
            this.commandMeta,
            this.commandDescription
         );
      }

      @API(
         status = Status.STABLE
      )
      @FunctionalInterface
      public interface Applicable<C> {
         @API(
            status = Status.STABLE
         )
         @NonNull
         Command.Builder<C> applyToCommandBuilder(@NonNull Command.Builder<C> builder);
      }
   }
}
