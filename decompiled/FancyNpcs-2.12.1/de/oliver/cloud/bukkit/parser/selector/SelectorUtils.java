package de.oliver.cloud.bukkit.parser.selector;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.oliver.cloud.brigadier.parser.WrappedBrigadierParser;
import de.oliver.cloud.bukkit.BukkitCommandContextKeys;
import de.oliver.cloud.bukkit.internal.CraftBukkitReflection;
import de.oliver.cloud.bukkit.internal.MinecraftArgumentTypes;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.suggestion.Suggestion;
import de.oliver.cloud.suggestion.SuggestionProvider;
import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
final class SelectorUtils {
   private SelectorUtils() {
   }

   @Nullable
   private static <C, T> ArgumentParser<C, T> createModernParser(final boolean single, final boolean playersOnly, final SelectorUtils.SelectorMapper<T> mapper) {
      if (CraftBukkitReflection.MAJOR_REVISION < 13) {
         return null;
      } else {
         WrappedBrigadierParser<C, Object> wrappedBrigParser = new WrappedBrigadierParser<>(
            () -> createEntityArgument(single, playersOnly), SelectorUtils.EntityArgumentParseFunction.INSTANCE
         );
         return new SelectorUtils.ModernSelectorParser<>(wrappedBrigParser, mapper);
      }
   }

   private static ArgumentType<Object> createEntityArgument(final boolean single, final boolean playersOnly) {
      Constructor<?> constructor = MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("entity")).getDeclaredConstructors()[0];
      constructor.setAccessible(true);

      try {
         return (ArgumentType<Object>)constructor.newInstance(single, playersOnly);
      } catch (ReflectiveOperationException var4) {
         throw new RuntimeException(var4);
      }
   }

   private static <X extends Throwable> RuntimeException rethrow(final Throwable t) throws X {
      throw t;
   }

   private static final class EntityArgumentParseFunction implements WrappedBrigadierParser.ParseFunction<Object> {
      static final SelectorUtils.EntityArgumentParseFunction INSTANCE = new SelectorUtils.EntityArgumentParseFunction();

      @Override
      public Object apply(final ArgumentType<Object> type, final StringReader reader) throws CommandSyntaxException {
         Method parseWithSelectorControl = CraftBukkitReflection.findMethod(type.getClass(), "parse", StringReader.class, boolean.class, boolean.class);
         if (parseWithSelectorControl != null) {
            return invokeCraftBukkitParse(parseWithSelectorControl, type, reader, true, true);
         } else {
            Method parseWithPermissionOverride = CraftBukkitReflection.findMethod(type.getClass(), "parse", StringReader.class, boolean.class);
            return parseWithPermissionOverride != null ? invokeCraftBukkitParse(parseWithPermissionOverride, type, reader, true) : type.parse(reader);
         }
      }

      private static Object invokeCraftBukkitParse(final Method method, final ArgumentType<Object> type, final Object... arguments) throws CommandSyntaxException {
         try {
            return method.invoke(type, arguments);
         } catch (InvocationTargetException var5) {
            Throwable cause = var5.getCause();
            if (cause instanceof CommandSyntaxException) {
               throw (CommandSyntaxException)cause;
            } else {
               throw new RuntimeException(var5);
            }
         } catch (ReflectiveOperationException var6) {
            throw new RuntimeException(var6);
         }
      }
   }

   abstract static class EntitySelectorParser<C, T> extends SelectorUtils.SelectorParser<C, T> {
      protected EntitySelectorParser(final boolean single) {
         super(single, false);
      }
   }

   static final class EntitySelectorWrapper {
      @MonotonicNonNull
      private static volatile SelectorUtils.EntitySelectorWrapper.Methods methods;
      private final CommandContext<?> commandContext;
      private final Object selector;

      EntitySelectorWrapper(final CommandContext<?> commandContext, final Object selector) {
         this.commandContext = commandContext;
         this.selector = selector;
      }

      private static SelectorUtils.EntitySelectorWrapper.Methods methods(final CommandContext<?> commandContext, final Object selector) {
         if (methods == null) {
            synchronized (SelectorUtils.EntitySelectorWrapper.Methods.class) {
               if (methods == null) {
                  methods = new SelectorUtils.EntitySelectorWrapper.Methods(commandContext, selector);
               }
            }
         }

         return methods;
      }

      private SelectorUtils.EntitySelectorWrapper.Methods methods() {
         return methods(this.commandContext, this.selector);
      }

      Entity singleEntity() {
         return reflectiveOperation(
            () -> (Entity)this.methods()
               .getBukkitEntity
               .invoke(this.methods().entity.invoke(this.selector, this.commandContext.get("_cloud_brigadier_native_sender")))
         );
      }

      Player singlePlayer() {
         return reflectiveOperation(
            () -> (Player)this.methods()
               .getBukkitEntity
               .invoke(this.methods().player.invoke(this.selector, this.commandContext.get("_cloud_brigadier_native_sender")))
         );
      }

      List<Entity> entities() {
         List<Object> internalEntities = reflectiveOperation(
            () -> (List<Object>)this.methods().entities.invoke(this.selector, this.commandContext.get("_cloud_brigadier_native_sender"))
         );
         return internalEntities.stream().map(o -> reflectiveOperation(() -> (Entity)this.methods().getBukkitEntity.invoke(o))).collect(Collectors.toList());
      }

      List<Player> players() {
         List<Object> serverPlayers = reflectiveOperation(
            () -> (List<Object>)this.methods().players.invoke(this.selector, this.commandContext.get("_cloud_brigadier_native_sender"))
         );
         return serverPlayers.stream().map(o -> reflectiveOperation(() -> (Player)this.methods().getBukkitEntity.invoke(o))).collect(Collectors.toList());
      }

      private static <T> T reflectiveOperation(final SelectorUtils.EntitySelectorWrapper.ReflectiveOperation<T> op) {
         try {
            return op.run();
         } catch (InvocationTargetException var2) {
            if (var2.getCause() instanceof CommandSyntaxException) {
               throw SelectorUtils.rethrow(var2.getCause());
            } else {
               throw new RuntimeException(var2);
            }
         } catch (ReflectiveOperationException var3) {
            throw new RuntimeException(var3);
         }
      }

      private static final class Methods {
         @MonotonicNonNull
         private Method getBukkitEntity;
         @MonotonicNonNull
         private Method entity;
         @MonotonicNonNull
         private Method player;
         @MonotonicNonNull
         private Method entities;
         @MonotonicNonNull
         private Method players;

         Methods(final CommandContext<?> commandContext, final Object selector) {
            Object nativeSender = commandContext.get("_cloud_brigadier_native_sender");
            Class<?> nativeSenderClass = nativeSender.getClass();

            for (Method method : selector.getClass().getDeclaredMethods()) {
               if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(nativeSenderClass) && Modifier.isPublic(method.getModifiers())) {
                  Class<?> returnType = method.getReturnType();
                  if (List.class.isAssignableFrom(returnType)) {
                     ParameterizedType stringListType = (ParameterizedType)method.getGenericReturnType();
                     Type listType = stringListType.getActualTypeArguments()[0];

                     while (listType instanceof WildcardType) {
                        listType = ((WildcardType)listType).getUpperBounds()[0];
                     }

                     Class<?> clazz = listType instanceof Class ? (Class)listType : GenericTypeReflector.erase(listType);
                     Method getBukkitEntity = findGetBukkitEntityMethod(clazz);
                     if (getBukkitEntity != null) {
                        Class<?> bukkitType = getBukkitEntity.getReturnType();
                        if (Player.class.isAssignableFrom(bukkitType)) {
                           if (this.players != null) {
                              throw new IllegalStateException();
                           }

                           this.players = method;
                        } else {
                           if (this.entities != null) {
                              throw new IllegalStateException();
                           }

                           this.entities = method;
                        }
                     }
                  } else if (returnType != void.class) {
                     Method getBukkitEntity = findGetBukkitEntityMethod(returnType);
                     if (getBukkitEntity != null) {
                        Class<?> bukkitType = getBukkitEntity.getReturnType();
                        if (Player.class.isAssignableFrom(bukkitType)) {
                           if (this.player != null) {
                              throw new IllegalStateException();
                           }

                           this.player = method;
                        } else {
                           if (this.entity != null || this.getBukkitEntity != null) {
                              throw new IllegalStateException();
                           }

                           this.entity = method;
                           this.getBukkitEntity = getBukkitEntity;
                        }
                     }
                  }
               }
            }

            Objects.requireNonNull(this.getBukkitEntity, "Failed to locate getBukkitEntity method");
            Objects.requireNonNull(this.player, "Failed to locate findPlayer method");
            Objects.requireNonNull(this.entity, "Failed to locate findEntity method");
            Objects.requireNonNull(this.players, "Failed to locate findPlayers method");
            Objects.requireNonNull(this.entities, "Failed to locate findEntities method");
         }

         private static Method findGetBukkitEntityMethod(final Class<?> returnType) {
            Method getBukkitEntity;
            try {
               getBukkitEntity = returnType.getDeclaredMethod("getBukkitEntity");
            } catch (ReflectiveOperationException var5) {
               try {
                  getBukkitEntity = returnType.getMethod("getBukkitEntity");
               } catch (ReflectiveOperationException var4) {
                  getBukkitEntity = null;
               }
            }

            return getBukkitEntity;
         }
      }

      @FunctionalInterface
      interface ReflectiveOperation<T> {
         T run() throws ReflectiveOperationException;
      }
   }

   private static class ModernSelectorParser<C, T> implements ArgumentParser.FutureArgumentParser<C, T>, SuggestionProvider<C> {
      private final WrappedBrigadierParser<C, Object> wrappedBrigadierParser;
      private final SelectorUtils.SelectorMapper<T> mapper;

      ModernSelectorParser(final WrappedBrigadierParser<C, Object> wrapperBrigParser, final SelectorUtils.SelectorMapper<T> mapper) {
         this.wrappedBrigadierParser = wrapperBrigParser;
         this.mapper = mapper;
      }

      @Override
      public CompletableFuture<ArgumentParseResult<T>> parseFuture(final CommandContext<C> commandContext, final CommandInput commandInput) {
         return CompletableFuture.supplyAsync(
            () -> {
               CommandInput originalCommandInput = commandInput.copy();
               ArgumentParseResult<Object> result = this.wrappedBrigadierParser.parse(commandContext, commandInput);
               if (result.failure().isPresent()) {
                  return (ArgumentParseResult<T>)result;
               } else {
                  String input = originalCommandInput.difference(commandInput);

                  try {
                     return ArgumentParseResult.success(
                        this.mapper.mapResult(input, new SelectorUtils.EntitySelectorWrapper(commandContext, result.parsedValue().get()))
                     );
                  } catch (CommandSyntaxException var7) {
                     return ArgumentParseResult.failure(var7);
                  } catch (Exception var8) {
                     throw SelectorUtils.rethrow(var8);
                  }
               }
            },
            commandContext.get(BukkitCommandContextKeys.SENDER_SCHEDULER_EXECUTOR)
         );
      }

      @Override
      public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(final CommandContext<C> commandContext, final CommandInput input) {
         Object commandSourceStack = commandContext.get("_cloud_brigadier_native_sender");
         Field bypassField = CraftBukkitReflection.findField(commandSourceStack.getClass(), "bypassSelectorPermissions");

         try {
            boolean prev = false;

            CompletableFuture var6;
            try {
               if (bypassField != null) {
                  prev = bypassField.getBoolean(commandSourceStack);
                  bypassField.setBoolean(commandSourceStack, true);
               }

               var6 = CompletableFuture.completedFuture(this.wrappedBrigadierParser.suggestionProvider().suggestionsFuture(commandContext, input).join());
            } finally {
               if (bypassField != null) {
                  bypassField.setBoolean(commandSourceStack, prev);
               }
            }

            return var6;
         } catch (ReflectiveOperationException var11) {
            throw new RuntimeException(var11);
         }
      }
   }

   abstract static class PlayerSelectorParser<C, T> extends SelectorUtils.SelectorParser<C, T> {
      protected PlayerSelectorParser(final boolean single) {
         super(single, true);
      }

      @NonNull
      @Override
      protected Iterable<Suggestion> legacySuggestions(final CommandContext<C> commandContext, final CommandInput input) {
         List<Suggestion> suggestions = new ArrayList<>();

         for (Player player : Bukkit.getOnlinePlayers()) {
            CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
            if (!(bukkit instanceof Player) || ((Player)bukkit).canSee(player)) {
               suggestions.add(Suggestion.suggestion(player.getName()));
            }
         }

         return suggestions;
      }
   }

   @FunctionalInterface
   interface SelectorMapper<T> {
      T mapResult(String input, SelectorUtils.EntitySelectorWrapper wrapper) throws Exception;
   }

   private abstract static class SelectorParser<C, T>
      implements ArgumentParser.FutureArgumentParser<C, T>,
      SelectorUtils.SelectorMapper<T>,
      SuggestionProvider<C> {
      static final Supplier<Object> NO_PLAYERS_EXCEPTION_TYPE = Suppliers.memoize(() -> findExceptionType("argument.entity.notfound.player"));
      static final Supplier<Object> NO_ENTITIES_EXCEPTION_TYPE = Suppliers.memoize(() -> findExceptionType("argument.entity.notfound.entity"));
      @Nullable
      private final ArgumentParser<C, T> modernParser;

      SelectorParser(final boolean single, final boolean playersOnly) {
         this.modernParser = SelectorUtils.createModernParser(single, playersOnly, this);
      }

      CompletableFuture<ArgumentParseResult<T>> legacyParse(final CommandContext<C> commandContext, final CommandInput commandInput) {
         return ArgumentParseResult.failureFuture(new SelectorUnsupportedException(commandContext, this.getClass()));
      }

      @NonNull
      Iterable<Suggestion> legacySuggestions(final CommandContext<C> commandContext, final CommandInput input) {
         return Collections.emptyList();
      }

      @Override
      public CompletableFuture<ArgumentParseResult<T>> parseFuture(final CommandContext<C> commandContext, final CommandInput commandInput) {
         return this.modernParser != null ? this.modernParser.parseFuture(commandContext, commandInput) : this.legacyParse(commandContext, commandInput);
      }

      @Override
      public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
         @NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input
      ) {
         return this.modernParser != null
            ? this.modernParser.suggestionProvider().suggestionsFuture(commandContext, input)
            : CompletableFuture.completedFuture(this.legacySuggestions(commandContext, input));
      }

      private static Object findExceptionType(final String type) {
         Field[] fields = MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("entity")).getDeclaredFields();
         return Arrays.stream(fields)
            .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == SimpleCommandExceptionType.class)
            .map(field -> {
               try {
                  Object fieldValue = field.get(null);
                  if (fieldValue == null) {
                     return null;
                  } else {
                     Field messageField = SimpleCommandExceptionType.class.getDeclaredField("message");
                     messageField.setAccessible(true);
                     return messageField.get(fieldValue).toString().contains(type) ? fieldValue : null;
                  }
               } catch (ReflectiveOperationException var4) {
                  throw new RuntimeException(var4);
               }
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Could not find exception type '" + type + "'"));
      }

      static final class Thrower {
         private final Object type;

         Thrower(final Object simpleCommandExceptionType) {
            this.type = simpleCommandExceptionType;
         }

         void throwIt() {
            throw SelectorUtils.rethrow(((SimpleCommandExceptionType)this.type).create());
         }
      }
   }
}
