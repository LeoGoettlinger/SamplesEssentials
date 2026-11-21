package vrx.economy.vRXClans.libs.cloud;

import io.leangen.geantyref.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import vrx.economy.vRXClans.libs.cloud.caption.Caption;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionFormatter;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionRegistry;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.caption.StandardCaptionsProvider;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandContextFactory;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.context.StandardCommandContextFactory;
import vrx.economy.vRXClans.libs.cloud.exception.handling.ExceptionController;
import vrx.economy.vRXClans.libs.cloud.execution.CommandExecutor;
import vrx.economy.vRXClans.libs.cloud.execution.ExecutionCoordinator;
import vrx.economy.vRXClans.libs.cloud.execution.postprocessor.AcceptingCommandPostprocessor;
import vrx.economy.vRXClans.libs.cloud.execution.postprocessor.CommandPostprocessingContext;
import vrx.economy.vRXClans.libs.cloud.execution.postprocessor.CommandPostprocessor;
import vrx.economy.vRXClans.libs.cloud.execution.preprocessor.AcceptingCommandPreprocessor;
import vrx.economy.vRXClans.libs.cloud.execution.preprocessor.CommandPreprocessingContext;
import vrx.economy.vRXClans.libs.cloud.execution.preprocessor.CommandPreprocessor;
import vrx.economy.vRXClans.libs.cloud.help.CommandPredicate;
import vrx.economy.vRXClans.libs.cloud.help.HelpHandler;
import vrx.economy.vRXClans.libs.cloud.help.HelpHandlerFactory;
import vrx.economy.vRXClans.libs.cloud.injection.ParameterInjectorRegistry;
import vrx.economy.vRXClans.libs.cloud.internal.CommandNode;
import vrx.economy.vRXClans.libs.cloud.internal.CommandRegistrationHandler;
import vrx.economy.vRXClans.libs.cloud.meta.CommandMeta;
import vrx.economy.vRXClans.libs.cloud.parser.ParserRegistry;
import vrx.economy.vRXClans.libs.cloud.parser.StandardParserRegistry;
import vrx.economy.vRXClans.libs.cloud.parser.flag.CommandFlag;
import vrx.economy.vRXClans.libs.cloud.permission.AndPermission;
import vrx.economy.vRXClans.libs.cloud.permission.OrPermission;
import vrx.economy.vRXClans.libs.cloud.permission.Permission;
import vrx.economy.vRXClans.libs.cloud.permission.PermissionResult;
import vrx.economy.vRXClans.libs.cloud.permission.PredicatePermission;
import vrx.economy.vRXClans.libs.cloud.services.ServicePipeline;
import vrx.economy.vRXClans.libs.cloud.services.State;
import vrx.economy.vRXClans.libs.cloud.setting.Configurable;
import vrx.economy.vRXClans.libs.cloud.setting.ManagerSetting;
import vrx.economy.vRXClans.libs.cloud.state.RegistrationState;
import vrx.economy.vRXClans.libs.cloud.state.Stateful;
import vrx.economy.vRXClans.libs.cloud.suggestion.DelegatingSuggestionFactory;
import vrx.economy.vRXClans.libs.cloud.suggestion.FilteringSuggestionProcessor;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestion;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionFactory;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionMapper;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProcessor;
import vrx.economy.vRXClans.libs.cloud.syntax.CommandSyntaxFormatter;
import vrx.economy.vRXClans.libs.cloud.syntax.StandardCommandSyntaxFormatter;
import vrx.economy.vRXClans.libs.cloud.type.tuple.Pair;
import vrx.economy.vRXClans.libs.cloud.type.tuple.Triplet;

@API(
   status = API.Status.STABLE
)
public abstract class CommandManager<C> implements Stateful<RegistrationState>, CommandBuilderSource<C> {
   private final Configurable<ManagerSetting> settings = Configurable.enumConfigurable(ManagerSetting.class);
   private final ServicePipeline servicePipeline = ServicePipeline.builder().build();
   private final ParserRegistry<C> parserRegistry = new StandardParserRegistry<>();
   private final Collection<Command<C>> commands = new LinkedList<>();
   private final ParameterInjectorRegistry<C> parameterInjectorRegistry = new ParameterInjectorRegistry<>();
   private final CommandTree<C> commandTree;
   private final SuggestionFactory<C, ? extends Suggestion> suggestionFactory;
   private final Set<CloudCapability> capabilities = new HashSet<>();
   private final ExceptionController<C> exceptionController = new ExceptionController<>();
   private final CommandExecutor<C> commandExecutor;
   private CaptionFormatter<C, String> captionVariableReplacementHandler = CaptionFormatter.placeholderReplacing();
   private CommandSyntaxFormatter<C> commandSyntaxFormatter = new StandardCommandSyntaxFormatter<>(this);
   private SuggestionProcessor<C> suggestionProcessor = new FilteringSuggestionProcessor<>();
   private CommandRegistrationHandler<C> commandRegistrationHandler;
   private CaptionRegistry<C> captionRegistry;
   private HelpHandlerFactory<C> helpHandlerFactory = HelpHandlerFactory.standard(this);
   private SuggestionMapper<? extends Suggestion> mapper = SuggestionMapper.identity();
   private final AtomicReference<RegistrationState> state = new AtomicReference<>(RegistrationState.BEFORE_REGISTRATION);

   protected CommandManager(
      @NonNull final ExecutionCoordinator<C> executionCoordinator, @NonNull final CommandRegistrationHandler<C> commandRegistrationHandler
   ) {
      CommandContextFactory<C> commandContextFactory = new StandardCommandContextFactory<>(this);
      this.commandTree = CommandTree.newTree(this);
      this.commandRegistrationHandler = commandRegistrationHandler;
      this.suggestionFactory = new DelegatingSuggestionFactory<>(
         this, this.commandTree, commandContextFactory, executionCoordinator, suggestion -> this.mapper.map(suggestion)
      );
      this.commandExecutor = new StandardCommandExecutor<>(this, executionCoordinator, commandContextFactory);
      this.servicePipeline.registerServiceType(new TypeToken<CommandPreprocessor<C>>() {}, new AcceptingCommandPreprocessor<>());
      this.servicePipeline.registerServiceType(new TypeToken<CommandPostprocessor<C>>() {}, new AcceptingCommandPostprocessor<>());
      this.captionRegistry = CaptionRegistry.captionRegistry();
      this.captionRegistry.registerProvider(new StandardCaptionsProvider<>());
      this.parameterInjectorRegistry().registerInjector(CommandContext.class, (context, annotationAccessor) -> context);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public CommandExecutor<C> commandExecutor() {
      return this.commandExecutor;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public SuggestionFactory<C, ? extends Suggestion> suggestionFactory() {
      return this.suggestionFactory;
   }

   @NonNull
   public SuggestionMapper<? extends Suggestion> suggestionMapper() {
      return this.mapper;
   }

   public void appendSuggestionMapper(@NonNull final SuggestionMapper<? extends Suggestion> mapper) {
      this.suggestionMapper(this.suggestionMapper().then(mapper));
   }

   public void suggestionMapper(@NonNull final SuggestionMapper<? extends Suggestion> mapper) {
      this.mapper = Objects.requireNonNull(mapper, "mapper");
   }

   @This
   @NonNull
   public CommandManager<C> command(@NonNull final Command<? extends C> command) {
      if (!this.transitionIfPossible(RegistrationState.BEFORE_REGISTRATION, RegistrationState.REGISTERING) && !this.isCommandRegistrationAllowed()) {
         throw new IllegalStateException(
            "Unable to register commands because the manager is no longer in a registration state. Your platform may allow unsafe registrations by enabling the appropriate manager setting."
         );
      } else {
         this.commandTree.insertCommand((Command<C>)command);
         this.commands.add((Command<C>)command);
         return this;
      }
   }

   @API(
      status = API.Status.STABLE
   )
   @This
   @NonNull
   public CommandManager<C> command(@NonNull final CommandFactory<C> commandFactory) {
      commandFactory.createCommands(this).forEach(this::command);
      return this;
   }

   @NonNull
   public CommandManager<C> command(@NonNull final Command.Builder<? extends C> command) {
      return this.command(command.manager(this).build());
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public CaptionFormatter<C, String> captionFormatter() {
      return this.captionVariableReplacementHandler;
   }

   @API(
      status = API.Status.STABLE
   )
   public void captionFormatter(@NonNull final CaptionFormatter<C, String> captionFormatter) {
      this.captionVariableReplacementHandler = captionFormatter;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public CommandSyntaxFormatter<C> commandSyntaxFormatter() {
      return this.commandSyntaxFormatter;
   }

   @API(
      status = API.Status.STABLE
   )
   public void commandSyntaxFormatter(@NonNull final CommandSyntaxFormatter<C> commandSyntaxFormatter) {
      this.commandSyntaxFormatter = commandSyntaxFormatter;
   }

   @NonNull
   public CommandRegistrationHandler<C> commandRegistrationHandler() {
      return this.commandRegistrationHandler;
   }

   @API(
      status = API.Status.STABLE
   )
   protected final void commandRegistrationHandler(@NonNull final CommandRegistrationHandler<C> commandRegistrationHandler) {
      this.requireState(RegistrationState.BEFORE_REGISTRATION);
      this.commandRegistrationHandler = commandRegistrationHandler;
   }

   @API(
      status = API.Status.STABLE
   )
   protected final void registerCapability(@NonNull final CloudCapability capability) {
      this.capabilities.add(capability);
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean hasCapability(@NonNull final CloudCapability capability) {
      return this.capabilities.contains(capability);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public Collection<CloudCapability> capabilities() {
      return Collections.unmodifiableSet(new HashSet<>(this.capabilities));
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public PermissionResult testPermission(@NonNull final C sender, @NonNull final Permission permission) {
      if (permission instanceof PredicatePermission) {
         return ((PredicatePermission)permission).testPermission(sender);
      } else if (permission instanceof OrPermission) {
         for (Permission innerPermission : permission.permissions()) {
            PermissionResult result = this.testPermission(sender, innerPermission);
            if (result.allowed()) {
               return result;
            }
         }

         return PermissionResult.denied(permission);
      } else if (permission instanceof AndPermission) {
         for (Permission innerPermissionx : permission.permissions()) {
            PermissionResult result = this.testPermission(sender, innerPermissionx);
            if (!result.allowed()) {
               return result;
            }
         }

         return PermissionResult.allowed(permission);
      } else {
         return PermissionResult.of(permission.isEmpty() || this.hasPermission(sender, permission.permissionString()), permission);
      }
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public final CaptionRegistry<C> captionRegistry() {
      return this.captionRegistry;
   }

   @API(
      status = API.Status.STABLE
   )
   public final void captionRegistry(@NonNull final CaptionRegistry<C> captionRegistry) {
      this.captionRegistry = captionRegistry;
   }

   public abstract boolean hasPermission(@NonNull C sender, @NonNull String permission);

   @API(
      status = API.Status.EXPERIMENTAL
   )
   public void deleteRootCommand(@NonNull final String rootCommand) throws CloudCapability.CloudCapabilityMissingException {
      if (!this.hasCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION)) {
         throw new CloudCapability.CloudCapabilityMissingException(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
      } else {
         CommandNode<C> node = this.commandTree.getNamedNode(rootCommand);
         if (node != null && node.component() != null) {
            this.commandRegistrationHandler.unregisterRootCommand(node.component());
            this.commandTree.deleteRecursively(node, true, this.commands::remove);
         }
      }
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public Collection<String> rootCommands() {
      return this.commandTree
         .rootNodes()
         .stream()
         .map(CommandNode::component)
         .filter(Objects::nonNull)
         .filter(component -> component.type() == CommandComponent.ComponentType.LITERAL)
         .map(CommandComponent::name)
         .collect(Collectors.toList());
   }

   @NonNull
   @Override
   public final Command.Builder<C> decorateBuilder(@NonNull final Command.Builder<C> builder) {
      return builder.manager(this);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public <T> CommandComponent.Builder<C, T> componentBuilder(@NonNull final Class<T> type, @NonNull final String name) {
      return CommandComponent.<C, T>ofType(type, name).commandManager(this);
   }

   @NonNull
   public CommandFlag.Builder<C, Void> flagBuilder(@NonNull final String name) {
      return CommandFlag.builder(name);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public CommandTree<C> commandTree() {
      return this.commandTree;
   }

   @NonNull
   @Override
   public CommandMeta createDefaultCommandMeta() {
      return CommandMeta.empty();
   }

   public void registerCommandPreProcessor(@NonNull final CommandPreprocessor<C> processor) {
      this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPreprocessor<C>>() {}, processor, Collections.emptyList());
   }

   public void registerCommandPostProcessor(@NonNull final CommandPostprocessor<C> processor) {
      this.servicePipeline.registerServiceImplementation(new TypeToken<CommandPostprocessor<C>>() {}, processor, Collections.emptyList());
   }

   @API(
      status = API.Status.STABLE
   )
   public State preprocessContext(@NonNull final CommandContext<C> context, @NonNull final CommandInput commandInput) {
      this.servicePipeline.pump(CommandPreprocessingContext.of(context, commandInput)).through(new TypeToken<CommandPreprocessor<C>>() {}).complete();
      return context.<String>optional("__COMMAND_PRE_PROCESSED__").orElse("").isEmpty() ? State.REJECTED : State.ACCEPTED;
   }

   public State postprocessContext(@NonNull final CommandContext<C> context, @NonNull final Command<C> command) {
      this.servicePipeline.pump(CommandPostprocessingContext.of(context, command)).through(new TypeToken<CommandPostprocessor<C>>() {}).complete();
      return context.<String>optional("__COMMAND_POST_PROCESSED__").orElse("").isEmpty() ? State.REJECTED : State.ACCEPTED;
   }

   @NonNull
   public SuggestionProcessor<C> suggestionProcessor() {
      return this.suggestionProcessor;
   }

   public void suggestionProcessor(@NonNull final SuggestionProcessor<C> suggestionProcessor) {
      this.suggestionProcessor = suggestionProcessor;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public ParserRegistry<C> parserRegistry() {
      return this.parserRegistry;
   }

   @NonNull
   public final ParameterInjectorRegistry<C> parameterInjectorRegistry() {
      return this.parameterInjectorRegistry;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public final ExceptionController<C> exceptionController() {
      return this.exceptionController;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public final Collection<Command<C>> commands() {
      return Collections.unmodifiableCollection(this.commands);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public final HelpHandler<C> createHelpHandler() {
      return this.helpHandlerFactory.createHelpHandler(cmd -> true);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public final HelpHandler<C> createHelpHandler(@NonNull final CommandPredicate<C> filter) {
      return this.helpHandlerFactory.createHelpHandler(filter);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public final HelpHandlerFactory<C> helpHandlerFactory() {
      return this.helpHandlerFactory;
   }

   @API(
      status = API.Status.STABLE
   )
   public final void helpHandlerFactory(@NonNull final HelpHandlerFactory<C> helpHandlerFactory) {
      this.helpHandlerFactory = helpHandlerFactory;
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public Configurable<ManagerSetting> settings() {
      return this.settings;
   }

   @NonNull
   public final RegistrationState state() {
      return this.state.get();
   }

   public final boolean transitionIfPossible(@NonNull final RegistrationState in, @NonNull final RegistrationState out) {
      return this.state.compareAndSet(in, out) || this.state.get() == out;
   }

   @API(
      status = API.Status.STABLE
   )
   protected final void lockRegistration() {
      if (this.state() == RegistrationState.BEFORE_REGISTRATION) {
         this.transitionOrThrow(RegistrationState.BEFORE_REGISTRATION, RegistrationState.AFTER_REGISTRATION);
      } else {
         this.transitionOrThrow(RegistrationState.REGISTERING, RegistrationState.AFTER_REGISTRATION);
      }
   }

   @API(
      status = API.Status.STABLE
   )
   public boolean isCommandRegistrationAllowed() {
      return this.settings().get(ManagerSetting.ALLOW_UNSAFE_REGISTRATION) || this.state.get() != RegistrationState.AFTER_REGISTRATION;
   }

   protected void registerDefaultExceptionHandlers(
      @NonNull final Consumer<Triplet<CommandContext<C>, Caption, List<CaptionVariable>>> messageSender,
      @NonNull final Consumer<Pair<String, Throwable>> logger
   ) {
      DefaultExceptionHandlers<C> defaultExceptionHandlers = new DefaultExceptionHandlers<>(messageSender, logger, this.exceptionController);
      defaultExceptionHandlers.register();
   }
}
