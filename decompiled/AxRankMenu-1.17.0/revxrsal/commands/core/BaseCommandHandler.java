package revxrsal.commands.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.CommandHandlerVisitor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.dynamic.AnnotationReplacer;
import revxrsal.commands.autocomplete.AutoCompleter;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.reflect.MethodCallerFactory;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.exception.DefaultExceptionHandler;
import revxrsal.commands.exception.InvalidBooleanException;
import revxrsal.commands.exception.InvalidURLException;
import revxrsal.commands.exception.InvalidUUIDException;
import revxrsal.commands.exception.NumberNotInRangeException;
import revxrsal.commands.exception.ThrowableFromCommand;
import revxrsal.commands.help.CommandHelp;
import revxrsal.commands.help.CommandHelpWriter;
import revxrsal.commands.locales.Translator;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.OrphanRegistry;
import revxrsal.commands.orphan.Orphans;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.process.ContextResolver;
import revxrsal.commands.process.ContextResolverFactory;
import revxrsal.commands.process.ParameterNamingStrategy;
import revxrsal.commands.process.ParameterResolver;
import revxrsal.commands.process.ParameterValidator;
import revxrsal.commands.process.PermissionReader;
import revxrsal.commands.process.ResponseHandler;
import revxrsal.commands.process.SenderResolver;
import revxrsal.commands.process.ValueResolver;
import revxrsal.commands.process.ValueResolverFactory;
import revxrsal.commands.util.ClassMap;
import revxrsal.commands.util.Preconditions;
import revxrsal.commands.util.Primitives;
import revxrsal.commands.util.StackTraceSanitizer;
import revxrsal.commands.util.Strings;

@ApiStatus.Internal
public abstract class BaseCommandHandler implements CommandHandler {
   protected final Map<CommandPath, CommandExecutable> executables = new HashMap<>();
   protected final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();
   private final BaseCommandDispatcher dispatcher = new BaseCommandDispatcher(this);
   final List<ResolverFactory> factories = new ArrayList<>();
   final BaseAutoCompleter autoCompleter = new BaseAutoCompleter(this);
   final ClassMap<List<ParameterValidator<Object>>> validators = new ClassMap<>();
   final ClassMap<ResponseHandler<?>> responseHandlers = new ClassMap<>();
   final ClassMap<Supplier<?>> dependencies = new ClassMap<>();
   final List<SenderResolver> senderResolvers = new ArrayList<>();
   private final Set<PermissionReader> permissionReaders = new HashSet<>();
   final Map<Class<?>, Set<AnnotationReplacer<?>>> annotationReplacers = new ClassMap<>();
   private MethodCallerFactory methodCallerFactory = MethodCallerFactory.defaultFactory();
   private final BaseCommandHandler.WrappedExceptionHandler exceptionHandler = new BaseCommandHandler.WrappedExceptionHandler(DefaultExceptionHandler.INSTANCE);
   private StackTraceSanitizer sanitizer = StackTraceSanitizer.defaultSanitizer();
   String flagPrefix = "-";
   String switchPrefix = "-";
   String messagePrefix = "";
   CommandHelpWriter<?> helpWriter;
   ParameterNamingStrategy parameterNamingStrategy = ParameterNamingStrategy.lowerCaseWithSpace();
   boolean failOnExtra = false;
   final List<CommandCondition> conditions = new ArrayList<>();
   private final Translator translator = Translator.create();

   public BaseCommandHandler() {
      this.registerContextResolverFactory(new SenderContextResolverFactory(this.senderResolvers));
      this.registerContextResolverFactory(DependencyResolverFactory.INSTANCE);
      this.registerValueResolverFactory(EitherValueResolverFactory.INSTANCE);
      this.registerValueResolver(int.class, ValueResolver.ValueResolverContext::popInt);
      this.registerValueResolver(double.class, ValueResolver.ValueResolverContext::popDouble);
      this.registerValueResolver(short.class, ValueResolver.ValueResolverContext::popShort);
      this.registerValueResolver(byte.class, ValueResolver.ValueResolverContext::popByte);
      this.registerValueResolver(long.class, ValueResolver.ValueResolverContext::popLong);
      this.registerValueResolver(float.class, ValueResolver.ValueResolverContext::popFloat);
      this.registerValueResolver(boolean.class, this.bool());
      this.registerValueResolver(String.class, ValueResolver.ValueResolverContext::popForParameter);
      this.registerValueResolver(UUID.class, context -> {
         String value = context.pop();

         try {
            return UUID.fromString(value);
         } catch (Throwable var3) {
            throw new InvalidUUIDException(context.parameter(), value);
         }
      });
      this.registerValueResolver(URL.class, context -> {
         String value = context.pop();

         try {
            return new URL(value);
         } catch (MalformedURLException var3) {
            throw new InvalidURLException(context.parameter(), value);
         }
      });
      this.registerValueResolver(URI.class, context -> {
         String value = context.pop();

         try {
            return new URI(value);
         } catch (URISyntaxException var3) {
            throw new InvalidURLException(context.parameter(), value);
         }
      });
      this.registerContextResolver(CommandHandler.class, context -> this);
      this.registerContextResolver(ExecutableCommand.class, context -> context.parameter().getDeclaringCommand());
      this.registerContextResolver(CommandActor.class, ParameterResolver.ParameterResolverContext::actor);
      this.registerContextResolver(CommandHelp.class, new BaseCommandHelp.Resolver(this));
      this.setExceptionHandler(DefaultExceptionHandler.INSTANCE);
      this.registerCondition(CooldownCondition.INSTANCE);
      this.registerParameterValidator(Number.class, (value, parameter, actor) -> {
         Range range = parameter.getAnnotation(Range.class);
         if (range != null && (value.doubleValue() > range.max() || value.doubleValue() < range.min())) {
            throw new NumberNotInRangeException(actor, parameter, value, range.min(), range.max());
         }
      });
      this.registerCondition((actor, command, arguments) -> command.checkPermission(actor));
      this.registerAnnotationReplacer(Description.class, new LocalesAnnotationReplacer(this));
   }

   @NotNull
   @Override
   public CommandHandler setParameterNamingStrategy(@NotNull ParameterNamingStrategy strategy) {
      Preconditions.notNull(strategy, "parameter naming strategy");
      this.parameterNamingStrategy = strategy;
      return this;
   }

   @NotNull
   @Override
   public ParameterNamingStrategy getParameterNamingStrategy() {
      return this.parameterNamingStrategy;
   }

   @NotNull
   @Override
   public CommandHandler register(@NotNull Object... commands) {
      for (Object command : commands) {
         Preconditions.notNull(command, "Command");
         if (command instanceof OrphanCommand) {
            throw new IllegalArgumentException("You cannot register an OrphanCommand directly! You must wrap it using Orphans.path(...).handler(OrphanCommand)");
         }

         if (command instanceof Orphans) {
            throw new IllegalArgumentException("You forgot to call .handler(OrphanCommand) in your Orphans.path(...)!");
         }

         if (command instanceof OrphanRegistry) {
            this.setDependencies(((OrphanRegistry)command).getHandler());
            CommandParser.parse(this, (OrphanRegistry)command);
         } else {
            this.setDependencies(command);
            CommandParser.parse(this, command);
         }
      }

      for (BaseCommandCategory category : this.categories.values()) {
         CommandPath categoryPath = category.getPath().getCategoryPath();
         category.parent(categoryPath == null ? null : this.categories.get(categoryPath));
         this.findPermission(category.defaultAction);
      }

      for (CommandExecutable executable : this.executables.values()) {
         this.findPermission(executable);
      }

      return this;
   }

   @NotNull
   @Override
   public Locale getLocale() {
      return this.translator.getLocale();
   }

   @Override
   public void setLocale(@NotNull Locale locale) {
      this.translator.setLocale(locale);
   }

   @NotNull
   @Override
   public Translator getTranslator() {
      return this.translator;
   }

   private void findPermission(@Nullable CommandExecutable executable) {
      if (executable != null) {
         if (!executable.permissionSet) {
            for (PermissionReader reader : this.permissionReaders) {
               CommandPermission p = reader.getPermission(executable);
               if (p != null) {
                  executable.permissionSet = true;
                  executable.setPermission(p);
                  return;
               }
            }
         }
      }
   }

   public Set<PermissionReader> getPermissionReaders() {
      return this.permissionReaders;
   }

   @NotNull
   @Override
   public CommandHandler setMethodCallerFactory(@NotNull MethodCallerFactory factory) {
      Preconditions.notNull(factory, "method caller factory");
      this.methodCallerFactory = factory;
      return this;
   }

   @NotNull
   @Override
   public CommandHandler setExceptionHandler(@NotNull CommandExceptionHandler handler) {
      Preconditions.notNull(handler, "command exception handler");
      this.exceptionHandler.handler = handler;
      return this;
   }

   @NotNull
   @Override
   public <T extends Throwable> CommandHandler registerExceptionHandler(@NotNull Class<T> exceptionType, @NotNull BiConsumer<CommandActor, T> handler) {
      Preconditions.notNull(exceptionType, "exception type");
      Preconditions.notNull(handler, "exception handler");
      this.exceptionHandler.exceptionsHandlers.add(exceptionType, handler);
      return this;
   }

   @NotNull
   @Override
   public CommandHandler setSwitchPrefix(@NotNull String prefix) {
      Preconditions.notNull(prefix, "prefix");
      Preconditions.notEmpty(prefix, "prefix cannot be empty!");
      this.switchPrefix = prefix;
      return this;
   }

   @NotNull
   @Override
   public CommandHandler setFlagPrefix(@NotNull String prefix) {
      Preconditions.notNull(prefix, "prefix");
      Preconditions.notEmpty(prefix, "prefix cannot be empty!");
      this.flagPrefix = prefix;
      return this;
   }

   @NotNull
   @Override
   public CommandHandler setMessagePrefix(@NotNull String prefix) {
      Preconditions.notNull(prefix, "prefix");
      this.messagePrefix = prefix;
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler setHelpWriter(@NotNull CommandHelpWriter<T> helpWriter) {
      Preconditions.notNull(helpWriter, "command help writer");
      this.helpWriter = helpWriter;
      return this;
   }

   @NotNull
   @Override
   public CommandHandler disableStackTraceSanitizing() {
      this.sanitizer = StackTraceSanitizer.empty();
      return this;
   }

   @NotNull
   @Override
   public CommandHandler failOnTooManyArguments() {
      this.failOnExtra = true;
      return this;
   }

   @NotNull
   @Override
   public CommandHandler registerSenderResolver(@NotNull SenderResolver resolver) {
      Preconditions.notNull(resolver, "resolver");
      this.senderResolvers.add(resolver);
      return this;
   }

   @NotNull
   @Override
   public CommandHandler registerPermissionReader(@NotNull PermissionReader reader) {
      Preconditions.notNull(reader, "permission reader");
      this.permissionReaders.add(reader);
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerValueResolver(@NotNull Class<T> type, @NotNull ValueResolver<T> resolver) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(resolver, "resolver");
      if (type.isPrimitive()) {
         this.registerValueResolver(Primitives.wrap(type), resolver);
      }

      this.factories.add(new ResolverFactory(ValueResolverFactory.forType(type, resolver)));
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerValueResolver(int priority, @NotNull Class<T> type, @NotNull ValueResolver<T> resolver) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(resolver, "resolver");
      if (type.isPrimitive()) {
         this.registerValueResolver(priority, Primitives.wrap(type), resolver);
      }

      this.factories.add(Preconditions.coerceIn(priority, 0, this.factories.size()), new ResolverFactory(ValueResolverFactory.forType(type, resolver)));
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerContextResolver(@NotNull Class<T> type, @NotNull ContextResolver<T> resolver) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(resolver, "resolver");
      if (type.isPrimitive()) {
         this.registerContextResolver(Primitives.wrap(type), resolver);
      }

      this.factories.add(new ResolverFactory(ContextResolverFactory.forType(type, resolver)));
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerContextResolver(int priority, @NotNull Class<T> type, @NotNull ContextResolver<T> resolver) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(resolver, "resolver");
      if (type.isPrimitive()) {
         this.registerContextResolver(Primitives.wrap(type), resolver);
      }

      this.factories.add(Preconditions.coerceIn(priority, 0, this.factories.size()), new ResolverFactory(ContextResolverFactory.forType(type, resolver)));
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerContextValue(@NotNull Class<T> type, T value) {
      return this.registerContextResolver(type, ContextResolver.of(value));
   }

   @NotNull
   @Override
   public <T> CommandHandler registerContextValue(int priority, @NotNull Class<T> type, @NotNull T value) {
      return this.registerContextResolver(priority, type, ContextResolver.of(value));
   }

   @NotNull
   @Override
   public CommandHandler registerValueResolverFactory(@NotNull ValueResolverFactory factory) {
      Preconditions.notNull(factory, "value resolver factory");
      this.factories.add(new ResolverFactory(factory));
      return this;
   }

   @NotNull
   @Override
   public CommandHandler registerValueResolverFactory(int priority, @NotNull ValueResolverFactory factory) {
      Preconditions.notNull(factory, "value resolver factory");
      this.factories.add(Preconditions.coerceIn(priority, 0, this.factories.size()), new ResolverFactory(factory));
      return this;
   }

   @NotNull
   @Override
   public CommandHandler registerContextResolverFactory(@NotNull ContextResolverFactory factory) {
      Preconditions.notNull(factory, "context resolver factory");
      this.factories.add(new ResolverFactory(factory));
      return this;
   }

   @NotNull
   @Override
   public CommandHandler registerContextResolverFactory(int priority, @NotNull ContextResolverFactory factory) {
      Preconditions.notNull(factory, "context resolver factory");
      this.factories.add(Preconditions.coerceIn(priority, 0, this.factories.size()), new ResolverFactory(factory));
      return this;
   }

   @NotNull
   @Override
   public CommandHandler registerCondition(@NotNull CommandCondition condition) {
      Preconditions.notNull(condition, "condition");
      this.conditions.add(condition);
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerDependency(@NotNull Class<T> type, @NotNull Supplier<T> supplier) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(supplier, "supplier");
      this.dependencies.add(type, supplier);
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerDependency(@NotNull Class<T> type, T value) {
      Preconditions.notNull(type, "type");
      this.dependencies.add(type, () -> value);
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerParameterValidator(@NotNull Class<T> type, @NotNull ParameterValidator<T> validator) {
      Preconditions.notNull(type, "type");
      Preconditions.notNull(validator, "validator");
      this.validators.computeIfAbsent(Primitives.wrap(type), t -> new ArrayList<>()).add(validator);
      return this;
   }

   @NotNull
   @Override
   public <T> CommandHandler registerResponseHandler(@NotNull Class<T> responseType, @NotNull ResponseHandler<T> handler) {
      Preconditions.notNull(responseType, "response type");
      Preconditions.notNull(handler, "response handler");
      this.responseHandlers.add(responseType, handler);
      return this;
   }

   @NotNull
   @Override
   public <T extends Annotation> CommandHandler registerAnnotationReplacer(@NotNull Class<T> annotationType, @NotNull AnnotationReplacer<T> replacer) {
      Preconditions.notNull(annotationType, "annotation type");
      Preconditions.notNull(replacer, "annotation replacer");
      this.annotationReplacers.computeIfAbsent(annotationType, e -> new HashSet<>()).add(replacer);
      return this;
   }

   @NotNull
   @Override
   public CommandHandler accept(@NotNull CommandHandlerVisitor visitor) {
      Preconditions.notNull(visitor, "command handler visitor cannot be null!");
      visitor.visit(this);
      return this;
   }

   @Nullable
   public <T extends Annotation> List<Annotation> replaceAnnotation(AnnotatedElement element, T ann) {
      Set<AnnotationReplacer<?>> replacers = this.annotationReplacers.get(ann.annotationType());
      if (replacers != null && !replacers.isEmpty()) {
         List<Annotation> annotations = new ArrayList<>();

         for (AnnotationReplacer replacer : replacers) {
            Collection<Annotation> replaced = replacer.replaceAnnotations(element, ann);
            if (replaced != null && !replaced.isEmpty()) {
               annotations.addAll(replaced);
            }
         }

         return annotations.isEmpty() ? null : annotations;
      } else {
         return null;
      }
   }

   @NotNull
   @Override
   public AutoCompleter getAutoCompleter() {
      return this.autoCompleter;
   }

   @Override
   public ExecutableCommand getCommand(@NotNull CommandPath path) {
      return this.executables.get(path);
   }

   @Override
   public CommandCategory getCategory(@NotNull CommandPath path) {
      return this.categories.get(path);
   }

   @NotNull
   @UnmodifiableView
   @Override
   public Map<CommandPath, ExecutableCommand> getCommands() {
      return Collections.unmodifiableMap(this.executables);
   }

   @NotNull
   @UnmodifiableView
   @Override
   public Map<CommandPath, CommandCategory> getCategories() {
      return Collections.unmodifiableMap(this.categories);
   }

   public <T> ParameterResolver<T> getResolver(CommandParameter parameter) {
      for (ResolverFactory factory : this.factories) {
         Resolver resolver = factory.create(parameter);
         if (resolver != null) {
            return resolver;
         }
      }

      return parameter.getType().isEnum() ? new Resolver(null, EnumResolverFactory.INSTANCE.create(parameter)) : null;
   }

   @NotNull
   @Override
   public CommandExceptionHandler getExceptionHandler() {
      return this.exceptionHandler;
   }

   @NotNull
   @Override
   public MethodCallerFactory getMethodCallerFactory() {
      return this.methodCallerFactory;
   }

   @Override
   public <T> CommandHelpWriter<T> getHelpWriter() {
      return (CommandHelpWriter<T>)this.helpWriter;
   }

   private void unregister(CommandPath path, CommandExecutable command) {
      BaseCommandCategory parent = command.parent;
      if (parent != null) {
         parent.commands.remove(path);
         if (parent.isEmpty()) {
            this.categories.remove(parent.path);
         }
      }
   }

   private void unregister(CommandPath path, BaseCommandCategory category) {
      BaseCommandCategory parent = category.parent;
      if (parent != null) {
         parent.commands.remove(path);
         if (parent.isEmpty()) {
            this.categories.remove(parent.path);
         }
      }
   }

   @Override
   public boolean unregister(@NotNull CommandPath path) {
      boolean modified = false;
      Iterator<Entry<CommandPath, CommandExecutable>> iterator = this.executables.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<CommandPath, CommandExecutable> entry = iterator.next();
         if (entry.getKey().isChildOf(path)) {
            modified = true;
            iterator.remove();
            this.unregister(path, entry.getValue());
         }
      }

      iterator = this.categories.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<CommandPath, BaseCommandCategory> entry = iterator.next();
         if (entry.getKey().isChildOf(path)) {
            modified = true;
            iterator.remove();
            this.unregister(path, entry.getValue());
         }
      }

      return modified;
   }

   @Override
   public boolean unregister(@NotNull String commandPath) {
      return this.unregister(CommandPath.get(Strings.splitBySpace(commandPath)));
   }

   @Override
   public void unregisterAllCommands() {
      this.getRootPaths().forEach(this::unregister);
   }

   @NotNull
   @Override
   public Set<CommandPath> getRootPaths() {
      Set<CommandPath> paths = new HashSet<>();

      for (CommandPath path : this.categories.keySet()) {
         if (path.isRoot()) {
            paths.add(path);
         }
      }

      for (CommandPath pathx : this.executables.keySet()) {
         if (pathx.isRoot()) {
            paths.add(pathx);
         }
      }

      return paths;
   }

   @NotNull
   @Override
   public String getSwitchPrefix() {
      return this.switchPrefix;
   }

   @NotNull
   @Override
   public String getFlagPrefix() {
      return this.flagPrefix;
   }

   @NotNull
   @Override
   public String getMessagePrefix() {
      return this.messagePrefix;
   }

   @NotNull
   @Override
   public <T> Optional<T> dispatch(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
      return Optional.ofNullable((T)this.dispatcher.eval(actor, arguments));
   }

   @NotNull
   @Override
   public <T> Optional<T> dispatch(@NotNull CommandActor actor, @NotNull String commandInput) {
      try {
         return this.dispatch(actor, ArgumentStack.parse(commandInput));
      } catch (Throwable var4) {
         this.getExceptionHandler().handleException(var4, actor);
         return Optional.empty();
      }
   }

   @Override
   public <T> Supplier<T> getDependency(@NotNull Class<T> dependencyType) {
      return (Supplier<T>)this.dependencies.getFlexible(dependencyType);
   }

   @Override
   public <T> Supplier<T> getDependency(@NotNull Class<T> dependencyType, Supplier<T> def) {
      return (Supplier<T>)this.dependencies.getFlexibleOrDefault(dependencyType, def);
   }

   protected void setDependencies(Object ob) {
      for (Field field : Primitives.getType(ob).getDeclaredFields()) {
         if (field.isAnnotationPresent(Dependency.class)) {
            if (!field.isAccessible()) {
               field.setAccessible(true);
            }

            Supplier<?> dependency = this.dependencies.getFlexible(field.getType());
            if (dependency == null) {
               throw new IllegalStateException("Unable to find correct dependency for type " + field.getType());
            }

            try {
               field.set(ob, dependency.get());
            } catch (IllegalAccessException var8) {
               throw new IllegalStateException("Unable to inject dependency value into field " + field.getName(), var8);
            }
         }
      }
   }

   private ValueResolver<Boolean> bool() {
      return context -> {
         String v = context.pop();
         String var2 = v.toLowerCase();
         switch (var2) {
            case "true":
            case "yes":
            case "ye":
            case "y":
            case "yeah":
            case "ofcourse":
            case "mhm":
               return true;
            case "false":
            case "no":
            case "n":
               return false;
            default:
               throw new InvalidBooleanException(context.parameter(), v);
         }
      };
   }

   private class WrappedExceptionHandler implements CommandExceptionHandler {
      private final ClassMap<BiConsumer<CommandActor, Throwable>> exceptionsHandlers = new ClassMap<>();
      @NotNull
      private CommandExceptionHandler handler;

      public WrappedExceptionHandler(@NotNull CommandExceptionHandler handler) {
         this.handler = handler;
      }

      @Override
      public void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor) {
         Throwable cause = throwable.getCause();
         if (cause != null
            && (cause.getClass().isAnnotationPresent(ThrowableFromCommand.class) || this.exceptionsHandlers.getFlexible(cause.getClass()) != null)) {
            throwable = cause;
         }

         BiConsumer<CommandActor, Throwable> registered = this.exceptionsHandlers.getFlexible(throwable.getClass());
         BaseCommandHandler.this.sanitizer.sanitize(throwable);
         if (registered != null) {
            registered.accept(actor, throwable);
         } else {
            this.handler.handleException(throwable, actor);
         }
      }
   }
}
