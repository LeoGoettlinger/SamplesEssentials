package de.oliver.cloud.annotations;

import de.oliver.cloud.CommandManager;
import de.oliver.cloud.annotations.assembler.ArgumentAssembler;
import de.oliver.cloud.annotations.assembler.ArgumentAssemblerImpl;
import de.oliver.cloud.annotations.assembler.FlagAssembler;
import de.oliver.cloud.annotations.assembler.FlagAssemblerImpl;
import de.oliver.cloud.annotations.descriptor.ArgumentDescriptor;
import de.oliver.cloud.annotations.descriptor.CommandDescriptor;
import de.oliver.cloud.annotations.descriptor.FlagDescriptor;
import de.oliver.cloud.annotations.exception.ExceptionHandler;
import de.oliver.cloud.annotations.exception.ExceptionHandlerFactory;
import de.oliver.cloud.annotations.extractor.ArgumentExtractor;
import de.oliver.cloud.annotations.extractor.CommandExtractor;
import de.oliver.cloud.annotations.extractor.CommandExtractorImpl;
import de.oliver.cloud.annotations.extractor.FlagExtractor;
import de.oliver.cloud.annotations.extractor.FlagExtractorImpl;
import de.oliver.cloud.annotations.extractor.StandardArgumentExtractor;
import de.oliver.cloud.annotations.injection.RawArgs;
import de.oliver.cloud.annotations.parser.MethodArgumentParserFactory;
import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.processing.CommandContainer;
import de.oliver.cloud.annotations.string.StringProcessor;
import de.oliver.cloud.annotations.suggestion.SuggestionProviderFactory;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.caption.Caption;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.component.preprocessor.RegexPreprocessor;
import de.oliver.cloud.description.Description;
import de.oliver.cloud.execution.CommandExecutionHandler;
import de.oliver.cloud.internal.CommandInputTokenizer;
import de.oliver.cloud.meta.CommandMeta;
import de.oliver.cloud.meta.CommandMetaBuilder;
import de.oliver.cloud.parser.ParserDescriptor;
import de.oliver.cloud.parser.ParserParameters;
import de.oliver.cloud.parser.flag.CommandFlag;
import de.oliver.cloud.suggestion.SuggestionProvider;
import de.oliver.cloud.type.tuple.Pair;
import de.oliver.cloud.util.annotation.AnnotationAccessor;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class AnnotationParser<C> {
   private static final Comparator<Class<?>> COMMAND_CONTAINER_COMPARATOR = Comparator.<Class<?>>comparingInt(clazz -> {
      CommandContainer commandContainer = clazz.getAnnotation(CommandContainer.class);
      return commandContainer == null ? 1 : commandContainer.priority();
   }).reversed();
   public static final String INFERRED_ARGUMENT_NAME = "__INFERRED_ARGUMENT_NAME__";
   private final CommandManager<C> manager;
   private final Map<Class<? extends Annotation>, AnnotationMapper<?>> annotationMappers;
   private final Map<Class<? extends Annotation>, PreprocessorMapper<?, C>> preprocessorMappers;
   private final Map<Class<? extends Annotation>, BuilderModifier<?, C>> builderModifiers;
   private final List<BuilderDecorator<C>> builderDecorators;
   private final Map<Predicate<Method>, CommandMethodExecutionHandlerFactory<C>> commandMethodFactories;
   private final TypeToken<C> commandSenderType;
   private final MetaFactory metaFactory;
   private StringProcessor stringProcessor;
   private SyntaxParser syntaxParser;
   private ArgumentExtractor argumentExtractor;
   private ArgumentAssembler<C> argumentAssembler;
   private FlagExtractor flagExtractor;
   private FlagAssembler flagAssembler;
   private CommandExtractor commandExtractor;
   private SuggestionProviderFactory<C> suggestionProviderFactory;
   private MethodArgumentParserFactory<C> methodArgumentParserFactory;
   private ExceptionHandlerFactory<C> exceptionHandlerFactory;
   private DescriptionMapper descriptionMapper;
   private DefaultValueRegistry<C> defaultValueRegistry;

   public AnnotationParser(
      @NonNull final CommandManager<C> manager, @NonNull final Class<C> commandSenderClass, @NonNull final Function<ParserParameters, CommandMeta> metaMapper
   ) {
      this(manager, TypeToken.get(commandSenderClass), metaMapper);
   }

   public AnnotationParser(@NonNull final CommandManager<C> manager, @NonNull final Class<C> commandSenderClass) {
      this(manager, TypeToken.get(commandSenderClass), parameters -> CommandMeta.empty());
   }

   public AnnotationParser(@NonNull final CommandManager<C> manager, @NonNull final TypeToken<C> commandSenderClass) {
      this(manager, commandSenderClass, parameters -> CommandMeta.empty());
   }

   @API(
      status = Status.STABLE
   )
   public AnnotationParser(
      @NonNull final CommandManager<C> manager,
      @NonNull final TypeToken<C> commandSenderType,
      @NonNull final Function<ParserParameters, CommandMeta> metaMapper
   ) {
      this.commandSenderType = commandSenderType;
      this.manager = manager;
      this.metaFactory = new MetaFactory(this, metaMapper);
      this.annotationMappers = new HashMap<>();
      this.preprocessorMappers = new HashMap<>();
      this.builderModifiers = new HashMap<>();
      this.commandMethodFactories = new HashMap<>();
      this.flagExtractor = new FlagExtractorImpl(this);
      this.flagAssembler = new FlagAssemblerImpl(manager);
      this.syntaxParser = new SyntaxParserImpl();
      this.descriptionMapper = DescriptionMapper.simple();
      this.argumentExtractor = StandardArgumentExtractor.create(this);
      this.argumentAssembler = new ArgumentAssemblerImpl<>(this);
      this.commandExtractor = new CommandExtractorImpl(this);
      this.suggestionProviderFactory = SuggestionProviderFactory.defaultFactory();
      this.methodArgumentParserFactory = MethodArgumentParserFactory.defaultFactory();
      this.exceptionHandlerFactory = ExceptionHandlerFactory.defaultFactory();
      this.builderDecorators = new ArrayList<>();
      this.defaultValueRegistry = new DefaultValueRegistryImpl<>();
      this.registerBuilderModifier(
         CommandDescription.class,
         (description, builder) -> builder.commandDescription(
            de.oliver.cloud.description.CommandDescription.commandDescription(this.mapDescription(description.value()))
         )
      );
      this.registerPreprocessorMapper(
         Regex.class, annotation -> RegexPreprocessor.of(this.processString(annotation.value()), Caption.of(this.processString(annotation.failureCaption())))
      );
      this.manager
         .parameterInjectorRegistry()
         .registerInjector(
            String[].class,
            (context, annotations) -> annotations.annotation(RawArgs.class) == null
               ? null
               : new CommandInputTokenizer(context.rawInput().remainingInput()).tokenize().toArray(new String[0])
         );
      this.stringProcessor = StringProcessor.noOp();
   }

   @Nullable
   static <A extends Annotation> A getAnnotationRecursively(
      @NonNull final AnnotationAccessor annotations, @NonNull final Class<A> clazz, @NonNull final Set<Class<? extends Annotation>> checkedAnnotations
   ) {
      A innerCandidate = null;

      for (Annotation annotation : annotations.annotations()) {
         if (checkedAnnotations.add(annotation.annotationType())) {
            if (annotation.annotationType().equals(clazz)) {
               return (A)annotation;
            }

            if (!annotation.annotationType().getPackage().getName().startsWith("java.lang")) {
               A inner = getAnnotationRecursively(AnnotationAccessor.of(annotation.annotationType()), clazz, checkedAnnotations);
               if (inner != null) {
                  innerCandidate = inner;
               }
            }
         }
      }

      return innerCandidate;
   }

   @Nullable
   static <A extends Annotation> A getMethodOrClassAnnotation(@NonNull final Method method, @NonNull final Class<A> clazz) {
      A annotation = getAnnotationRecursively(AnnotationAccessor.of(method), clazz, new HashSet<>());
      if (annotation == null) {
         annotation = getAnnotationRecursively(AnnotationAccessor.of(method.getDeclaringClass()), clazz, new HashSet<>());
      }

      return annotation;
   }

   static <A extends Annotation> boolean methodOrClassHasAnnotation(@NonNull final Method method, @NonNull final Class<A> clazz) {
      return getMethodOrClassAnnotation(method, clazz) != null;
   }

   @NonNull
   public CommandManager<C> manager() {
      return this.manager;
   }

   @API(
      status = Status.STABLE
   )
   public void registerCommandExecutionMethodFactory(@NonNull final Predicate<Method> predicate, @NonNull final CommandMethodExecutionHandlerFactory<C> factory) {
      this.commandMethodFactories.put(predicate, factory);
   }

   public <A extends Annotation> void registerBuilderModifier(@NonNull final Class<A> annotation, @NonNull final BuilderModifier<A, C> builderModifier) {
      this.builderModifiers.put(annotation, builderModifier);
   }

   @API(
      status = Status.STABLE
   )
   public void registerBuilderDecorator(@NonNull final BuilderDecorator<C> decorator) {
      this.builderDecorators.add(decorator);
   }

   public <A extends Annotation> void registerAnnotationMapper(@NonNull final Class<A> annotation, @NonNull final AnnotationMapper<A> mapper) {
      this.annotationMappers.put(annotation, mapper);
   }

   @API(
      status = Status.STABLE
   )
   public <A extends Annotation> void registerPreprocessorMapper(@NonNull final Class<A> annotation, @NonNull final PreprocessorMapper<A, C> preprocessorMapper) {
      this.preprocessorMappers.put(annotation, preprocessorMapper);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public Map<Class<? extends Annotation>, PreprocessorMapper<?, C>> preprocessorMappers() {
      return Collections.unmodifiableMap(this.preprocessorMappers);
   }

   @NonNull
   public StringProcessor stringProcessor() {
      return this.stringProcessor;
   }

   public void stringProcessor(@NonNull final StringProcessor stringProcessor) {
      this.stringProcessor = stringProcessor;
   }

   @NonNull
   public String processString(@NonNull final String input) {
      return this.stringProcessor().processString(input);
   }

   public String[] processStrings(final String[] strings) {
      return Arrays.stream(strings).map(this::processString).toArray(String[]::new);
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public List<String> processStrings(@NonNull final Collection<String> strings) {
      return strings.stream().map(this::processString).collect(Collectors.toList());
   }

   @NonNull
   public SyntaxParser syntaxParser() {
      return this.syntaxParser;
   }

   @API(
      status = Status.STABLE
   )
   public void syntaxParser(@NonNull final SyntaxParser syntaxParser) {
      this.syntaxParser = syntaxParser;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public ArgumentExtractor argumentExtractor() {
      return this.argumentExtractor;
   }

   @API(
      status = Status.STABLE
   )
   public void argumentExtractor(@NonNull final ArgumentExtractor argumentExtractor) {
      this.argumentExtractor = argumentExtractor;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public ArgumentAssembler<C> argumentAssembler() {
      return this.argumentAssembler;
   }

   @API(
      status = Status.STABLE
   )
   public void argumentAssembler(@NonNull final ArgumentAssembler<C> argumentAssembler) {
      this.argumentAssembler = argumentAssembler;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public FlagExtractor flagExtractor() {
      return this.flagExtractor;
   }

   @API(
      status = Status.STABLE
   )
   public void flagExtractor(@NonNull final FlagExtractor flagExtractor) {
      this.flagExtractor = flagExtractor;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public FlagAssembler flagAssembler() {
      return this.flagAssembler;
   }

   @API(
      status = Status.STABLE
   )
   public void flagAssembler(@NonNull final FlagAssembler flagAssembler) {
      this.flagAssembler = flagAssembler;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public CommandExtractor commandExtractor() {
      return this.commandExtractor;
   }

   @API(
      status = Status.STABLE
   )
   public void commandExtractor(@NonNull final CommandExtractor commandExtractor) {
      this.commandExtractor = commandExtractor;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public SuggestionProviderFactory<C> suggestionProviderFactory() {
      return this.suggestionProviderFactory;
   }

   @API(
      status = Status.STABLE
   )
   public void suggestionProviderFactory(@NonNull final SuggestionProviderFactory<C> suggestionProviderFactory) {
      this.suggestionProviderFactory = suggestionProviderFactory;
   }

   @API(
      status = Status.EXPERIMENTAL
   )
   @NonNull
   public MethodArgumentParserFactory<C> methodArgumentParserFactory() {
      return this.methodArgumentParserFactory;
   }

   @API(
      status = Status.EXPERIMENTAL
   )
   public void methodArgumentParserFactory(@NonNull final MethodArgumentParserFactory<C> methodArgumentParserFactory) {
      this.methodArgumentParserFactory = methodArgumentParserFactory;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public ExceptionHandlerFactory<C> exceptionHandlerFactory() {
      return this.exceptionHandlerFactory;
   }

   @API(
      status = Status.STABLE
   )
   public void exceptionHandlerFactory(@NonNull final ExceptionHandlerFactory<C> exceptionHandlerFactory) {
      this.exceptionHandlerFactory = exceptionHandlerFactory;
   }

   @API(
      status = Status.STABLE
   )
   @NonNull
   public DescriptionMapper descriptionMapper() {
      return this.descriptionMapper;
   }

   @API(
      status = Status.STABLE
   )
   public void descriptionMapper(@NonNull final DescriptionMapper descriptionMapper) {
      this.descriptionMapper = descriptionMapper;
   }

   @NonNull
   public DefaultValueRegistry<C> defaultValueRegistry() {
      return this.defaultValueRegistry;
   }

   public void defaultValueRegistry(@NonNull final DefaultValueRegistry<C> defaultValueRegistry) {
      this.defaultValueRegistry = Objects.requireNonNull(defaultValueRegistry, "defaultValueRegistry");
   }

   @NonNull
   public Collection<de.oliver.cloud.Command<C>> parseContainers() throws Exception {
      return this.parseContainers(this.getClass().getClassLoader());
   }

   @API(
      status = Status.STABLE
   )
   public Collection<de.oliver.cloud.Command<C>> parseContainers(final ClassLoader classLoader) throws Exception {
      InputStream stream = classLoader.getResourceAsStream("META-INF/commands/org.incendo.cloud.annotations.processing.CommandContainer");

      List<Object> instances;
      label90: {
         List<String> classNames;
         try {
            if (stream == null) {
               instances = Collections.emptyList();
               break label90;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            try {
               classNames = reader.lines().distinct().collect(Collectors.toList());
            } catch (Throwable var14) {
               try {
                  reader.close();
               } catch (Throwable var11) {
                  var14.addSuppressed(var11);
               }

               throw var14;
            }

            reader.close();
         } catch (Throwable var15) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var10) {
                  var15.addSuppressed(var10);
               }
            }

            throw var15;
         }

         if (stream != null) {
            stream.close();
         }

         List<Class<?>> classes = new ArrayList<>();

         for (String className : classNames) {
            classes.add(Class.forName(className, true, classLoader));
         }

         classes.sort(COMMAND_CONTAINER_COMPARATOR);
         instances = new LinkedList<>();

         for (Class<?> commandContainer : classes) {
            Object instance;
            try {
               instance = commandContainer.getConstructor(AnnotationParser.class).newInstance(this);
            } catch (NoSuchMethodException var13) {
               try {
                  instance = commandContainer.getConstructor().newInstance();
               } catch (NoSuchMethodException var12) {
                  throw new IllegalStateException(String.format("Command container %s has no valid constructors", commandContainer), var12);
               }
            }

            instances.add(instance);
         }

         return this.parse(instances);
      }

      if (stream != null) {
         stream.close();
      }

      return instances;
   }

   @NonNull
   public Collection<de.oliver.cloud.Command<C>> parse(@NonNull final Object... instances) {
      return this.parse(Arrays.asList(instances));
   }

   @NonNull
   public Collection<de.oliver.cloud.Command<C>> parse(@NonNull final Collection<Object> instances) {
      for (Object instance : instances) {
         this.parseDefaultValues(instance);
      }

      for (Object instance : instances) {
         this.parseSuggestions(instance);
      }

      for (Object instance : instances) {
         this.parseParsers(instance);
      }

      for (Object instance : instances) {
         this.parseExceptionHandlers(instance);
      }

      List<de.oliver.cloud.Command<C>> result = new ArrayList<>();

      for (Object instance : instances) {
         Collection<CommandDescriptor> commandDescriptors = this.commandExtractor.extractCommands(instance);
         Collection<de.oliver.cloud.Command<C>> commands = this.construct(instance, commandDescriptors);

         for (de.oliver.cloud.Command<C> command : commands) {
            this.manager.command(command);
         }

         result.addAll(commands);
      }

      return Collections.unmodifiableList(result);
   }

   @API(
      status = Status.INTERNAL,
      consumers = {"de.oliver.cloud.annotations.*"}
   )
   @NonNull
   public Description mapDescription(@NonNull final String string) {
      return this.descriptionMapper.map(this.processString(string));
   }

   private <T> void parseSuggestions(@NonNull final T instance) {
      for (Method method : instance.getClass().getMethods()) {
         Suggestions suggestions = method.getAnnotation(Suggestions.class);
         if (suggestions != null) {
            if (!method.isAccessible()) {
               method.setAccessible(true);
            }

            boolean valid = Iterable.class.isAssignableFrom(method.getReturnType())
               || method.getReturnType().equals(Stream.class)
               || method.getReturnType().equals(CompletableFuture.class)
               || method.getReturnType().getSimpleName().equals("Sequence")
               || method.getParameterCount() == 3;
            if (!valid) {
               throw new IllegalArgumentException(
                  String.format(
                     "@Suggestions annotated method '%s' in class '%s' does not have the correct signature",
                     method.getName(),
                     instance.getClass().getCanonicalName()
                  )
               );
            }

            try {
               this.manager
                  .parserRegistry()
                  .registerSuggestionProvider(
                     this.processString(suggestions.value()),
                     this.suggestionProviderFactory.createSuggestionProvider(instance, method, this.manager.parameterInjectorRegistry())
                  );
            } catch (Exception var9) {
               throw new RuntimeException(var9);
            }
         }
      }
   }

   private <T> void parseExceptionHandlers(@NonNull final T instance) {
      for (Method method : instance.getClass().getMethods()) {
         ExceptionHandler exceptionHandler = method.getAnnotation(ExceptionHandler.class);
         if (exceptionHandler != null) {
            if (!method.isAccessible()) {
               method.setAccessible(true);
            }

            try {
               this.manager
                  .exceptionController()
                  .registerHandler(
                     exceptionHandler.value(), this.exceptionHandlerFactory.createExceptionHandler(instance, method, this.manager.parameterInjectorRegistry())
                  );
            } catch (Exception var8) {
               throw new RuntimeException(var8);
            }
         }
      }
   }

   private <T> void parseDefaultValues(@NonNull final T instance) {
      for (Method method : instance.getClass().getMethods()) {
         Default defaultValue = method.getAnnotation(Default.class);
         if (defaultValue != null) {
            String name;
            if (defaultValue.name().isEmpty()) {
               name = method.getName();
            } else {
               name = defaultValue.name();
            }

            this.defaultValueRegistry().register(name, new MethodDefaultValueFactory<>(method, instance));
         }
      }
   }

   private <T> void parseParsers(@NonNull final T instance) {
      for (Method method : instance.getClass().getMethods()) {
         Parser parser = method.getAnnotation(Parser.class);
         if (parser != null) {
            try {
               String suggestions = this.processString(parser.suggestions());
               SuggestionProvider<C> suggestionProvider;
               if (suggestions.isEmpty()) {
                  suggestionProvider = SuggestionProvider.noSuggestions();
               } else {
                  suggestionProvider = this.manager
                     .parserRegistry()
                     .getSuggestionProvider(suggestions)
                     .orElseThrow(() -> new NullPointerException(String.format("Cannot find the suggestion provider with name '%s'", suggestions)));
               }

               ParserDescriptor<C, ?> parserDescriptor = this.methodArgumentParserFactory
                  .createArgumentParser(suggestionProvider, instance, method, this.manager.parameterInjectorRegistry());
               String name = this.processString(parser.name());
               if (name.isEmpty()) {
                  this.manager.parserRegistry().registerParser(parserDescriptor);
               } else {
                  this.manager.parserRegistry().registerNamedParser(name, parserDescriptor);
               }
            } catch (Exception var11) {
               throw new RuntimeException(var11);
            }
         }
      }
   }

   @NonNull
   private Collection<de.oliver.cloud.Command<C>> construct(@NonNull final Object instance, @NonNull final Collection<CommandDescriptor> commandDescriptors) {
      return commandDescriptors.stream().flatMap(descriptor -> this.constructCommands(instance, descriptor).stream()).collect(Collectors.toList());
   }

   @NonNull
   private Collection<de.oliver.cloud.Command<C>> constructCommands(@NonNull final Object instance, @NonNull final CommandDescriptor commandDescriptor) {
      AnnotationAccessor classAnnotations = AnnotationAccessor.of(instance.getClass());
      List<de.oliver.cloud.Command<C>> commands = new ArrayList<>();
      Method method = commandDescriptor.method();
      CommandManager<C> manager = this.manager;
      CommandMetaBuilder metaBuilder = CommandMeta.builder().with(this.metaFactory.apply(method));
      de.oliver.cloud.Command.Builder<C> builder = manager.commandBuilder(
         commandDescriptor.commandToken(), commandDescriptor.syntax().get(0).minor(), metaBuilder.build()
      );

      for (BuilderDecorator<C> decorator : this.builderDecorators) {
         builder = decorator.decorate(builder);
      }

      Collection<ArgumentDescriptor> arguments = this.argumentExtractor.extractArguments(commandDescriptor.syntax(), method);
      Collection<FlagDescriptor> flagDescriptors = this.flagExtractor.extractFlags(method);
      Collection<CommandFlag<?>> flags = flagDescriptors.stream().map(this.flagAssembler()::assembleFlag).collect(Collectors.toList());
      Map<String, CommandComponent<C>> commandComponents = this.constructComponents(arguments, commandDescriptor);
      boolean commandNameFound = false;

      for (SyntaxFragment token : commandDescriptor.syntax()) {
         if (!commandNameFound) {
            commandNameFound = true;
         } else if (token.argumentMode() == ArgumentMode.LITERAL) {
            builder = builder.literal(token.major(), token.minor().toArray(new String[0]));
         } else {
            CommandComponent<C> component = commandComponents.get(token.major());
            if (component == null) {
               throw new IllegalArgumentException(String.format("Found no mapping for argument '%s' in method '%s'", token.major(), method.getName()));
            }

            builder = builder.argument(component);
         }
      }

      Class<? extends C> senderType = null;

      for (Parameter parameter : method.getParameters()) {
         if (!parameter.isAnnotationPresent(Argument.class) && GenericTypeReflector.isSuperType(this.commandSenderType.getType(), parameter.getType())) {
            senderType = (Class<? extends C>)parameter.getType();
            break;
         }
      }

      Permission permission = getMethodOrClassAnnotation(method, Permission.class);
      if (permission != null) {
         String[] permissions = permission.value();
         if (permissions.length == 1) {
            builder = builder.permission(this.processString(permissions[0]));
         } else if (permissions.length > 1) {
            builder = builder.permission(
               permission.mode().combine(Arrays.stream(permissions).map(this::processString).map(de.oliver.cloud.permission.Permission::permission))
            );
         }
      }

      if (commandDescriptor.requiredSender() != Object.class) {
         if (!GenericTypeReflector.isSuperType(this.commandSenderType.getType(), commandDescriptor.requiredSender())) {
            throw new IllegalArgumentException(
               String.format(
                  "Command method %s#%s(%s) has invalid sender type requirement: %s does not inherit from %s",
                  method.getDeclaringClass().getName(),
                  method.getName(),
                  Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", ")),
                  commandDescriptor.requiredSender().getName(),
                  this.commandSenderType.getType().getTypeName()
               )
            );
         }

         builder = builder.senderType((Class<? extends C>)commandDescriptor.requiredSender());
      } else if (senderType != null) {
         builder = builder.senderType(senderType);
      }

      try {
         MethodCommandExecutionHandler.CommandMethodContext<C> context = new MethodCommandExecutionHandler.CommandMethodContext<>(
            instance, commandComponents, arguments, flagDescriptors, method, this
         );
         CommandExecutionHandler<C> commandExecutionHandler = new MethodCommandExecutionHandler<>(context);

         for (Entry<Predicate<Method>, CommandMethodExecutionHandlerFactory<C>> entry : this.commandMethodFactories.entrySet()) {
            if (entry.getKey().test(method)) {
               commandExecutionHandler = entry.getValue().createExecutionHandler(context);
               break;
            }
         }

         builder = builder.handler(commandExecutionHandler);
      } catch (Exception var20) {
         throw new RuntimeException("Failed to construct command execution handler", var20);
      }

      for (CommandFlag<?> flag : flags) {
         builder = builder.flag(flag);
      }

      for (Annotation annotation : AnnotationAccessor.of(classAnnotations, AnnotationAccessor.of(method)).annotations()) {
         BuilderModifier builderModifier = this.builderModifiers.get(annotation.annotationType());
         if (builderModifier != null) {
            builder = (de.oliver.cloud.Command.Builder<C>)builderModifier.modifyBuilder(annotation, builder);
         }
      }

      de.oliver.cloud.Command<C> builtCommand = builder.build();
      commands.add(builtCommand);
      if (method.isAnnotationPresent(ProxiedBy.class)) {
         manager.command(this.constructProxy(method.getAnnotation(ProxiedBy.class), builtCommand));
      }

      return commands;
   }

   @NonNull
   private Map<String, CommandComponent<C>> constructComponents(
      @NonNull final Collection<ArgumentDescriptor> arguments, @NonNull final CommandDescriptor commandDescriptor
   ) {
      return arguments.stream()
         .map(
            argumentDescriptor -> this.argumentAssembler
               .assembleArgument(this.findSyntaxFragment(commandDescriptor.syntax(), this.processString(argumentDescriptor.name())), argumentDescriptor)
         )
         .map(component -> Pair.of(component.name(), component))
         .collect(Collectors.toMap(Pair::first, Pair::second));
   }

   @NonNull
   private de.oliver.cloud.Command<C> constructProxy(@NonNull final ProxiedBy proxyAnnotation, @NonNull final de.oliver.cloud.Command<C> command) {
      String proxy = this.processString(proxyAnnotation.value());
      if (proxy.contains(" ")) {
         throw new IllegalArgumentException("@ProxiedBy proxies may only contain single literals");
      } else {
         return this.manager.commandBuilder(proxy, command.commandMeta(), new String[0]).proxies(command).build();
      }
   }

   @NonNull
   private SyntaxFragment findSyntaxFragment(@NonNull final List<SyntaxFragment> fragments, @NonNull final String argumentName) {
      return fragments.stream()
         .filter(fragment -> fragment.argumentMode() != ArgumentMode.LITERAL)
         .filter(fragment -> fragment.major().equals(argumentName))
         .findFirst()
         .orElseThrow(() -> new IllegalArgumentException("Argument is not declared in syntax: " + argumentName));
   }

   @NonNull
   Map<Class<? extends Annotation>, AnnotationMapper<?>> annotationMappers() {
      return this.annotationMappers;
   }
}
