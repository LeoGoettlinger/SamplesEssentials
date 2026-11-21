package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.util.Contract;
import com.google.common.base.Preconditions;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;

final class DefaultFunctionImpl<T> extends ch.njol.skript.lang.function.Function<T> implements DefaultFunction<T> {
   private final SkriptAddon source;
   private final SequencedMap<String, Parameter<?>> parameters;
   private final java.util.function.Function<FunctionArguments, T> execute;
   private final List<String> description;
   private final List<String> since;
   private final List<String> examples;
   private final List<String> keywords;
   private final List<String> requires;

   DefaultFunctionImpl(
      SkriptAddon source,
      String name,
      SequencedMap<String, Parameter<?>> parameters,
      Class<T> returnType,
      boolean single,
      @Nullable Contract contract,
      java.util.function.Function<FunctionArguments, T> execute,
      String[] description,
      String[] since,
      String[] examples,
      String[] keywords,
      String[] requires
   ) {
      super(new ch.njol.skript.lang.function.Signature<>(null, name, parameters.values().toArray(new Parameter[0]), returnType, single, contract));
      Preconditions.checkNotNull(source, "source cannot be null");
      Preconditions.checkNotNull(name, "name cannot be null");
      Preconditions.checkNotNull(parameters, "parameters cannot be null");
      Preconditions.checkNotNull(returnType, "return type cannot be null");
      Preconditions.checkNotNull(execute, "execute cannot be null");
      this.source = source;
      this.parameters = parameters;
      this.execute = execute;
      this.description = description != null ? List.of(description) : Collections.emptyList();
      this.since = since != null ? List.of(since) : Collections.emptyList();
      this.examples = examples != null ? List.of(examples) : Collections.emptyList();
      this.keywords = keywords != null ? List.of(keywords) : Collections.emptyList();
      this.requires = requires != null ? List.of(requires) : Collections.emptyList();
   }

   @Nullable
   @Override
   public T[] execute(FunctionEvent<?> event, Object[][] params) {
      Map<String, Object> args = new LinkedHashMap<>();
      int length = Math.min(this.parameters.size(), params.length);
      Parameter<?>[] arrayParams = this.parameters.values().toArray(new Parameter[0]);

      for (int i = 0; i < length; i++) {
         Object[] arg = params[i];
         Parameter<?> parameter = arrayParams[i];
         if (arg != null && arg.length != 0) {
            if (parameter.hasModifier(Parameter.Modifier.RANGED)) {
               Parameter.Modifier.RangedModifier<?> range = parameter.getModifier(Parameter.Modifier.RangedModifier.class);
               if (!range.inRange(arg)) {
                  return null;
               }
            }

            if (parameter.isSingle()) {
               if (arg.length != 1) {
                  return null;
               }

               assert parameter.type().isAssignableFrom(arg[0].getClass()) : "argument type %s does not match parameter type %s"
                  .formatted(parameter.type().getSimpleName(), arg[0].getClass().getSimpleName());

               args.put(parameter.name(), arg[0]);
            } else {
               assert parameter.type().isAssignableFrom(arg.getClass()) : "argument type %s does not match parameter type %s"
                  .formatted(parameter.type().getSimpleName(), arg.getClass().getSimpleName());

               args.put(parameter.name(), arg);
            }
         } else if (!parameter.hasModifier(Parameter.Modifier.OPTIONAL)) {
            return null;
         }
      }

      FunctionArgumentsImpl arguments = new FunctionArgumentsImpl(args);
      T result = this.execute.apply(arguments);
      if (result == null) {
         return null;
      } else if (result.getClass().isArray()) {
         return (T[])result;
      } else {
         T[] array = (T[])((Object[])Array.newInstance(result.getClass(), 1));
         array[0] = result;
         return array;
      }
   }

   @Override
   public T execute(@NotNull FunctionEvent<?> event, @NotNull FunctionArguments arguments) {
      for (String name : arguments.names()) {
         Parameter<?> parameter = this.parameters.get(name);
         Object value = arguments.get(name);
         if (value == null && !parameter.hasModifier(Parameter.Modifier.OPTIONAL)) {
            return null;
         }

         if (parameter.hasModifier(Parameter.Modifier.RANGED)) {
            Parameter.Modifier.RangedModifier<?> range = parameter.getModifier(Parameter.Modifier.RangedModifier.class);
            if (!range.inRange(value)) {
               return null;
            }
         }
      }

      return this.execute.apply(arguments);
   }

   @Override
   public boolean resetReturnValue() {
      return true;
   }

   @NotNull
   @Override
   public String name() {
      return this.getName();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> description() {
      return this.description;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> since() {
      return this.since;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> examples() {
      return this.examples;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> keywords() {
      return this.keywords;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> requires() {
      return this.requires;
   }

   @NotNull
   @Override
   public SkriptAddon source() {
      return this.source;
   }

   @NotNull
   @Override
   public Signature<T> signature() {
      return this.getSignature();
   }

   static class BuilderImpl<T> implements DefaultFunction.Builder<T> {
      private final SkriptAddon source;
      private final String name;
      private final Class<T> returnType;
      private final SequencedMap<String, Parameter<?>> parameters = new LinkedHashMap<>();
      private Contract contract = null;
      private String[] description;
      private String[] since;
      private String[] examples;
      private String[] keywords;
      private String[] requires;

      BuilderImpl(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
         Preconditions.checkNotNull(source, "source cannot be null");
         Preconditions.checkNotNull(name, "name cannot be null");
         Preconditions.checkNotNull(returnType, "return type cannot be null");
         this.source = source;
         this.name = name;
         this.returnType = returnType;
      }

      @Override
      public DefaultFunction.Builder<T> contract(@NotNull Contract contract) {
         Preconditions.checkNotNull(contract, "contract cannot be null");
         this.contract = contract;
         return this;
      }

      @Override
      public DefaultFunction.Builder<T> description(@NotNull String... description) {
         Preconditions.checkNotNull(description, "description cannot be null");
         checkNotNull(description, "description contents cannot be null");
         this.description = description;
         return this;
      }

      @Override
      public DefaultFunction.Builder<T> since(@NotNull String... since) {
         Preconditions.checkNotNull(since, "since cannot be null");
         checkNotNull(since, "since contents cannot be null");
         this.since = since;
         return this;
      }

      @Override
      public DefaultFunction.Builder<T> examples(@NotNull String... examples) {
         Preconditions.checkNotNull(examples, "examples cannot be null");
         checkNotNull(examples, "examples contents cannot be null");
         this.examples = examples;
         return this;
      }

      @Override
      public DefaultFunction.Builder<T> keywords(@NotNull String... keywords) {
         Preconditions.checkNotNull(keywords, "keywords cannot be null");
         checkNotNull(keywords, "keywords contents cannot be null");
         this.keywords = keywords;
         return this;
      }

      @Override
      public DefaultFunction.Builder<T> requires(@NotNull String... requires) {
         Preconditions.checkNotNull(this.keywords, "requires cannot be null");
         checkNotNull(this.keywords, "requires contents cannot be null");
         this.requires = requires;
         return this;
      }

      @Override
      public DefaultFunction.Builder<T> parameter(@NotNull String name, @NotNull Class<?> type, @NotNull Parameter.Modifier... modifiers) {
         Preconditions.checkNotNull(name, "name cannot be null");
         Preconditions.checkNotNull(type, "type cannot be null");
         this.parameters.put(name, new DefaultFunctionImpl.DefaultParameter<>(name, type, modifiers));
         return this;
      }

      @Override
      public DefaultFunction<T> build(@NotNull java.util.function.Function<FunctionArguments, T> execute) {
         Preconditions.checkNotNull(execute, "execute cannot be null");
         return new DefaultFunctionImpl<>(
            this.source,
            this.name,
            this.parameters,
            this.returnType,
            !this.returnType.isArray(),
            this.contract,
            execute,
            this.description,
            this.since,
            this.examples,
            this.keywords,
            this.requires
         );
      }

      private static void checkNotNull(@NotNull String[] strings, @NotNull String message) {
         for (String string : strings) {
            Preconditions.checkNotNull(string, message);
         }
      }
   }

   record DefaultParameter<T>(String name, Class<T> type, Set<Parameter.Modifier> modifiers) implements Parameter<T> {
      DefaultParameter(String name, Class<T> type, Parameter.Modifier... modifiers) {
         this(name, type, Set.of(modifiers));
      }

      @NotNull
      @Override
      public String toString() {
         return this.toFormattedString();
      }
   }
}
