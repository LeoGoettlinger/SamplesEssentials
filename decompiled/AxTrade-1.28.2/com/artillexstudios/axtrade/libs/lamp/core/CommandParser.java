package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.annotation.Command;
import com.artillexstudios.axtrade.libs.lamp.annotation.Default;
import com.artillexstudios.axtrade.libs.lamp.annotation.DefaultFor;
import com.artillexstudios.axtrade.libs.lamp.annotation.Description;
import com.artillexstudios.axtrade.libs.lamp.annotation.Flag;
import com.artillexstudios.axtrade.libs.lamp.annotation.SecretCommand;
import com.artillexstudios.axtrade.libs.lamp.annotation.Single;
import com.artillexstudios.axtrade.libs.lamp.annotation.Subcommand;
import com.artillexstudios.axtrade.libs.lamp.annotation.Switch;
import com.artillexstudios.axtrade.libs.lamp.annotation.Usage;
import com.artillexstudios.axtrade.libs.lamp.command.ArgumentStack;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.command.CommandPermission;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.core.reflect.MethodCaller;
import com.artillexstudios.axtrade.libs.lamp.orphan.OrphanCommand;
import com.artillexstudios.axtrade.libs.lamp.orphan.OrphanRegistry;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterValidator;
import com.artillexstudios.axtrade.libs.lamp.process.PermissionReader;
import com.artillexstudios.axtrade.libs.lamp.process.ResponseHandler;
import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import com.artillexstudios.axtrade.libs.lamp.util.Primitives;
import com.artillexstudios.axtrade.libs.lamp.util.Strings;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

final class CommandParser {
   private static final char INHERIT_PARENT_PATH = '~';
   private static final String S_INHERIT_PARENT_PATH = "~";
   static final ResponseHandler<?> VOID_HANDLER = (response, actor, command) -> {};
   private static final AtomicInteger COMMAND_ID = new AtomicInteger();

   private CommandParser() {
   }

   public static void parse(@NotNull BaseCommandHandler handler, @NotNull OrphanRegistry orphan) {
      OrphanCommand instance = orphan.getHandler();
      Class<?> type = instance.getClass();
      parse(handler, type, orphan);
   }

   public static void parse(@NotNull BaseCommandHandler handler, @NotNull Object boundTarget) {
      Class<?> type = boundTarget instanceof Class ? (Class)boundTarget : boundTarget.getClass();
      parse(handler, type, boundTarget);
   }

   public static void parse(@NotNull BaseCommandHandler handler, @NotNull Class<?> container, @NotNull Object boundTarget) {
      try {
         Map<CommandPath, BaseCommandCategory> categories = handler.categories;

         for (Method method : getAllMethods(container)) {
            AnnotationReader reader = AnnotationReader.create(handler, method);
            Object invokeTarget = boundTarget;
            if (!reader.shouldDismiss()) {
               if (boundTarget instanceof OrphanRegistry) {
                  insertCommandPath((OrphanRegistry)boundTarget, reader);
                  invokeTarget = ((OrphanRegistry)boundTarget).getHandler();
               }

               reader.distributeAnnotations();
               reader.replaceAnnotations(handler);
               List<CommandPath> paths = getCommandPath(container, method, reader);
               MethodCaller.BoundMethodCaller caller = handler.getMethodCallerFactory().createFor(method).bindTo(invokeTarget);
               int id = COMMAND_ID.getAndIncrement();
               String[] defPaths = reader.get(DefaultFor.class, DefaultFor::value);
               List<CommandPath> defaultPaths = defPaths == null ? Collections.emptyList() : parseDefaultPaths(defPaths, container, method, reader);
               boolean isDefault = !defaultPaths.isEmpty();

               for (CommandPath defaultPath : defaultPaths) {
                  for (BaseCommandCategory category : generateCategoriesForPath(handler, true, defaultPath)) {
                     categories.putIfAbsent(category.path, category);
                  }
               }

               paths.forEach(
                  path -> {
                     for (BaseCommandCategory categoryx : generateCategoriesForPath(handler, isDefault, path)) {
                        categories.putIfAbsent(categoryx.path, categoryx);
                     }

                     Set<CommandPath> defaultPathsAndNormalPath = new HashSet<>();
                     defaultPathsAndNormalPath.add(path);
                     defaultPathsAndNormalPath.addAll(defaultPaths);

                     for (CommandPath p : defaultPathsAndNormalPath) {
                        boolean registerAsDefault = defaultPaths.contains(p);
                        CommandExecutable executable = new CommandExecutable();
                        if (!registerAsDefault) {
                           categories.remove(p);
                        }

                        executable.name = p.getLast();
                        executable.id = id;
                        executable.handler = handler;
                        executable.description = reader.get(Description.class, Description::value);
                        executable.path = p;
                        executable.method = method;
                        executable.reader = reader;
                        executable.secret = reader.contains(SecretCommand.class);
                        executable.methodCaller = caller;
                        if (registerAsDefault) {
                           executable.parent(categories.get(p), true);
                        } else {
                           executable.parent(categories.get(p.getCategoryPath()), false);
                        }

                        executable.responseHandler = getResponseHandler(handler, method.getGenericReturnType());
                        executable.parameters = getParameters(handler, method, executable);
                        executable.resolveableParameters = executable.parameters
                           .stream()
                           .filter(c -> c.getCommandIndex() != -1)
                           .collect(Collectors.toMap(CommandParameter::getCommandIndex, c -> (CommandParameter)c));
                        executable.usage = reader.get(Usage.class, Usage::value, () -> generateUsage(executable));
                        if (!registerAsDefault) {
                           putOrError(handler.executables, p, executable, "A command with path '" + p.toRealString() + "' already exists!");
                        }
                     }
                  }
               );
            }
         }
      } catch (Throwable var18) {
         throw var18;
      }
   }

   private static void insertCommandPath(OrphanRegistry boundTarget, AnnotationReader reader) {
      List<CommandPath> paths = boundTarget.getParentPaths();
      final String[] pathsArray = paths.stream().map(CommandPath::toRealString).toArray(String[]::new);
      reader.add(new Command() {
         @Override
         public Class<? extends Annotation> annotationType() {
            return Command.class;
         }

         @Override
         public String[] value() {
            return pathsArray;
         }
      });
   }

   private static Set<Method> getAllMethods(Class<?> c) {
      Set<Method> methods = new HashSet<>();

      for (Class<?> current = c; current != null && current != Object.class; current = current.getSuperclass()) {
         Collections.addAll(methods, current.getDeclaredMethods());
      }

      return methods;
   }

   private static String generateUsage(@NotNull ExecutableCommand command) {
      StringJoiner joiner = new StringJoiner(" ");
      CommandHandler handler = command.getCommandHandler();

      for (CommandParameter parameter : command.getValueParameters().values()) {
         if (parameter.getResolver().mutatesArguments()) {
            if (parameter.isSwitch()) {
               joiner.add("[" + handler.getSwitchPrefix() + parameter.getSwitchName() + "]");
            } else if (parameter.isFlag()) {
               joiner.add("[" + handler.getFlagPrefix() + parameter.getFlagName() + " <value>]");
            } else if (parameter.isOptional()) {
               joiner.add("[" + parameter.getName() + "]");
            } else {
               joiner.add("<" + parameter.getName() + ">");
            }
         }
      }

      return joiner.toString();
   }

   @NotNull
   private static ResponseHandler<?> getResponseHandler(BaseCommandHandler handler, Type genericType) {
      Class<?> rawType = Primitives.getRawType(genericType);
      if (CompletionStage.class.isAssignableFrom(rawType)) {
         ResponseHandler delegateHandler = getResponseHandler(handler, getInsideGeneric(genericType));
         return new CompletionStageResponseHandler(handler, delegateHandler);
      } else if (Optional.class.isAssignableFrom(rawType)) {
         ResponseHandler delegateHandler = getResponseHandler(handler, getInsideGeneric(genericType));
         return new OptionalResponseHandler(delegateHandler);
      } else if (Supplier.class.isAssignableFrom(rawType)) {
         ResponseHandler delegateHandler = getResponseHandler(handler, getInsideGeneric(genericType));
         return new SupplierResponseHandler(delegateHandler);
      } else {
         return handler.responseHandlers.getFlexibleOrDefault(rawType, VOID_HANDLER);
      }
   }

   private static Type getInsideGeneric(Type genericType) {
      try {
         return ((ParameterizedType)genericType).getActualTypeArguments()[0];
      } catch (ClassCastException var2) {
         return Object.class;
      }
   }

   private static Set<BaseCommandCategory> generateCategoriesForPath(CommandHandler handler, boolean isDefault, @NotNull CommandPath path) {
      if (path.size() == 1 && !isDefault) {
         return Collections.emptySet();
      } else {
         String parent = path.getParent();
         Set<BaseCommandCategory> categories = new HashSet<>();
         BaseCommandCategory root = new BaseCommandCategory();
         root.handler = handler;
         root.path = CommandPath.get(parent);
         root.name = parent;
         categories.add(root);
         List<String> pathList = new ArrayList<>();
         pathList.add(parent);

         for (String subcommand : path.getSubcommandPath()) {
            pathList.add(subcommand);
            BaseCommandCategory cat = new BaseCommandCategory();
            cat.handler = handler;
            cat.path = CommandPath.get(pathList);
            cat.name = cat.path.getName();
            categories.add(cat);
         }

         return categories;
      }
   }

   private static List<CommandParameter> getParameters(@NotNull BaseCommandHandler handler, @NotNull Method method, @NotNull CommandExecutable command) {
      List<CommandParameter> parameters = new ArrayList<>();
      Parameter[] methodParameters = method.getParameters();
      int cIndex = 0;

      for (int i = 0; i < methodParameters.length; i++) {
         Parameter javaParameter = methodParameters[i];
         AnnotationReader paramAnns = AnnotationReader.create(handler, javaParameter);
         List<ParameterValidator<Object>> validators = new ArrayList<>(
            handler.validators.getFlexibleOrDefault(javaParameter.getType(), Collections.emptyList())
         );
         String[] defaultValue = paramAnns.get(Default.class, Default::value);
         if (defaultValue == null || defaultValue.length == 0) {
            defaultValue = null;
         }

         BaseCommandParameter param = new BaseCommandParameter(
            paramAnns.get(Description.class, Description::value),
            i,
            defaultValue == null ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(defaultValue)),
            i == methodParameters.length - 1 && !paramAnns.contains(Single.class),
            paramAnns.contains(com.artillexstudios.axtrade.libs.lamp.annotation.Optional.class) || paramAnns.contains(Default.class),
            command,
            javaParameter,
            paramAnns.get(Switch.class),
            paramAnns.get(Flag.class),
            Collections.unmodifiableList(validators)
         );
         String overriddenName = Strings.getOverriddenName(javaParameter);
         param.name = overriddenName == null ? javaParameter.getName() : overriddenName;
         if (overriddenName == null) {
            overriddenName = handler.parameterNamingStrategy.getName(param);
            Objects.requireNonNull(
               overriddenName, "ParameterNamingStrategy.getName() returned null for parameter '" + javaParameter.getName() + "' in '" + method + "'!"
            );
         }

         param.name = overriddenName;

         for (PermissionReader reader : handler.getPermissionReaders()) {
            CommandPermission permission = reader.getPermission(param);
            if (permission != null) {
               param.permission = permission;
               break;
            }
         }

         if (param.isSwitch() && Primitives.wrap(param.getType()) != Boolean.class) {
            throw new IllegalStateException("Switch parameter " + javaParameter + " at " + method + " must be of boolean type!");
         }

         ParameterResolver<?> resolver;
         if (param.getType() == ArgumentStack.class) {
            resolver = new Resolver(context -> ArgumentStack.copyExact(context.input()), null);
         } else {
            resolver = handler.getResolver(param);
         }

         if (resolver == null) {
            throw new IllegalStateException("Unable to find a resolver for parameter type " + javaParameter.getType());
         }

         param.resolver = resolver;
         if (resolver.mutatesArguments()) {
            param.cindex = cIndex++;
         }

         param.suggestionProvider = handler.autoCompleter.getProvider(param);
         parameters.add(param);
      }

      return Collections.unmodifiableList(parameters);
   }

   private static List<CommandPath> getCommandPath(@NotNull Class<?> container, @NotNull Method method, @NotNull AnnotationReader reader) {
      DefaultFor defaultFor = reader.get(DefaultFor.class);
      return defaultFor != null ? parseDefaultPaths(defaultFor.value(), container, method, reader) : parseCommandAnnotations(container, method, reader);
   }

   @NotNull
   private static List<CommandPath> parseDefaultPaths(String[] path, @NotNull Class<?> container, @NotNull Method method, @NotNull AnnotationReader reader) {
      return Arrays.stream(path)
         .flatMap(
            defaultPath -> defaultPath.indexOf(126) != -1
               ? parseCommandAnnotations(container, method, reader)
                  .stream()
                  .map(CommandPath::toRealString)
                  .map(v -> defaultPath.replace("~", v))
                  .map(CommandPath::parse)
               : Stream.of(CommandPath.parse(defaultPath))
         )
         .collect(Collectors.toList());
   }

   private static List<CommandPath> parseCommandAnnotations(@NotNull Class<?> container, @NotNull Method method, @NotNull AnnotationReader reader) {
      List<CommandPath> paths = new ArrayList<>();
      List<String> commands = new ArrayList<>();
      List<String> subcommands = new ArrayList<>();
      Command commandAnnotation = reader.get(
         Command.class,
         "Method "
            + method.getName()
            + " does not have a parent command! You might have forgotten one of the following:\n- @Command on the method or class\n- implement OrphanCommand"
      );
      Preconditions.notEmpty(commandAnnotation.value(), "@Command#value() cannot be an empty array!");
      Collections.addAll(commands, commandAnnotation.value());
      List<String> parentSubcommandAliases = new ArrayList<>();

      for (Class<?> topClass : getTopClasses(container)) {
         Subcommand ps = topClass.getAnnotation(Subcommand.class);
         if (ps != null) {
            Collections.addAll(parentSubcommandAliases, ps.value());
         }
      }

      Subcommand subcommandAnnotation = reader.get(Subcommand.class);
      if (subcommandAnnotation != null) {
         Collections.addAll(subcommands, subcommandAnnotation.value());
      }

      for (String command : commands) {
         if (!subcommands.isEmpty()) {
            for (String subcommand : subcommands) {
               List<String> path = new ArrayList<>(Strings.splitBySpace(command));
               parentSubcommandAliases.forEach(subcommandAlias -> path.addAll(Strings.splitBySpace(subcommandAlias)));
               path.addAll(Strings.splitBySpace(subcommand));
               paths.add(CommandPath.get(path));
            }
         } else {
            paths.add(CommandPath.parse(command));
         }
      }

      return paths;
   }

   private static List<Class<?>> getTopClasses(Class<?> c) {
      List<Class<?>> classes = com.artillexstudios.axtrade.libs.lamp.util.Collections.listOf(c);
      Class<?> enclosingClass = c.getEnclosingClass();

      while (c.getEnclosingClass() != null) {
         c = enclosingClass;
         classes.add(enclosingClass);
      }

      Collections.reverse(classes);
      return classes;
   }

   private static <K, V> void putOrError(Map<K, V> map, K key, V value, String err) {
      if (map.containsKey(key)) {
         throw new IllegalStateException(err);
      } else {
         map.put(key, value);
      }
   }
}
