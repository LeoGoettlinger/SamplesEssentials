package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.Set;
import java.util.StringJoiner;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class FunctionReference<T> implements Debuggable {
   private final String namespace;
   private final String name;
   private final FunctionReference.Argument<Expression<?>>[] arguments;
   private Signature<T> cachedSignature;
   private boolean validSignature = true;
   private boolean printedInvalidSignatureWarning;
   private Function<T> cachedFunction;
   private LinkedHashMap<String, FunctionReference.ArgInfo> cachedArguments;

   public FunctionReference(
      @Nullable String namespace, @NotNull String name, @NotNull Signature<T> signature, @NotNull FunctionReference.Argument<Expression<?>>[] arguments
   ) {
      Preconditions.checkNotNull(name, "name cannot be null");
      Preconditions.checkNotNull(signature, "signature cannot be null");
      Preconditions.checkNotNull(arguments, "arguments cannot be null");
      this.namespace = namespace;
      this.name = name;
      this.cachedSignature = signature;
      this.arguments = arguments;
   }

   public boolean validate() {
      if (!this.validSignature) {
         Class<?>[] parameters = Arrays.stream(this.cachedSignature.parameters().all()).map(Parameter::type).toArray(Class[]::new);
         FunctionRegistry.Retrieval<ch.njol.skript.lang.function.Signature<?>> result = FunctionRegistry.getRegistry()
            .getSignature(this.namespace, this.name, parameters);
         if (result.result() == FunctionRegistry.RetrievalResult.EXACT) {
            this.cachedSignature = (Signature<T>)result.retrieved();
            this.validSignature = true;
            this.printedInvalidSignatureWarning = false;
            this.cachedFunction = null;
         } else if (!this.printedInvalidSignatureWarning) {
            this.printedInvalidSignatureWarning = true;
            Skript.warning(
               String.format(
                  "The function '%s' from the script '%s' no longer exists. Skript will continue to use the old function until this function is registered again. Function call: %s",
                  this.name,
                  this.namespace,
                  this.toString(null, false)
               )
            );
         }
      }

      if (this.cachedArguments == null) {
         this.cachedArguments = new LinkedHashMap<>();
         Parameter<?>[] targets = this.cachedSignature.parameters().all();

         for (int i = 0; i < this.arguments.length; i++) {
            FunctionReference.Argument<Expression<?>> argument = this.arguments[i];
            Parameter<?> target = targets[i];
            if (!(argument.value instanceof FunctionReferenceParser.EmptyExpression)) {
               Expression<?> converted = argument.value.getConvertedExpression(Utils.getComponentType(target.type()));
               if (!this.validateArgument(target, argument.value, converted)) {
                  return false;
               }

               if (converted != null && KeyProviderExpression.areKeysRecommended(converted)) {
                  converted.returnNestedStructures(true);
               }

               this.cachedArguments.put(target.name(), new FunctionReference.ArgInfo(converted, target.type(), target.modifiers()));
            }
         }
      }

      this.cachedSignature.addCall(this);
      return true;
   }

   private boolean validateArgument(Parameter<?> target, Expression<?> original, Expression<?> converted) {
      if (converted == null) {
         if (LiteralUtils.hasUnparsedLiteral(original)) {
            Skript.error("Can't understand this expression: %s", original);
         } else {
            Skript.error(
               "Expected type %s for argument '%s', but %s is of type %s.",
               this.getName(target.type(), target.isSingle()),
               target.name(),
               original,
               this.getName(original.getReturnType(), original.isSingle())
            );
         }

         return false;
      } else if (target.isSingle() && !converted.isSingle()) {
         Skript.error(
            "Expected type %s for argument '%s', but %s is of type %s.",
            this.getName(target.type(), target.isSingle()),
            target.name(),
            converted,
            this.getName(converted.getReturnType(), converted.isSingle())
         );
         return false;
      } else {
         return true;
      }
   }

   @Internal
   public void invalidate() {
      this.validSignature = false;
   }

   private String getName(Class<?> clazz, boolean single) {
      return single ? Classes.getSuperClassInfo(clazz).getName().getSingular() : Classes.getSuperClassInfo(Utils.getComponentType(clazz)).getName().getPlural();
   }

   public T execute(Event event) {
      if (!this.validate()) {
         Skript.error("Failed to verify function %s before execution.", this.name);
         return null;
      } else {
         SequencedMap<String, Object> args = new LinkedHashMap<>();
         this.cachedArguments.forEach((k, v) -> {
            if (v.modifiers().contains(Parameter.Modifier.KEYED)) {
               args.put(k, Classes.clone(this.evaluateKeyed(v.expression(), event)));
            } else {
               if (!v.type().isArray()) {
                  args.put(k, Classes.clone(v.expression().getSingle(event)));
               } else {
                  args.put(k, Classes.clone(v.expression().getArray(event)));
               }
            }
         });
         Function<T> function = this.function();
         if (function == null) {
            Skript.error("Failed to obtain function for call: %s", this.toString(null, false));
            return null;
         } else {
            FunctionEvent<?> fnEvent = new FunctionEvent<>(function);
            if (Functions.callFunctionEvents) {
               Bukkit.getPluginManager().callEvent(fnEvent);
            }

            return function.execute(fnEvent, new FunctionArgumentsImpl(args));
         }
      }
   }

   private KeyedValue<?>[] evaluateKeyed(Expression<?> expression, Event event) {
      return expression instanceof ExpressionList<?> list
         ? this.evaluateSingleListParameter(list.getExpressions(), event)
         : this.evaluateParameter(expression, event);
   }

   private KeyedValue<?>[] evaluateSingleListParameter(Expression<?>[] arguments, Event event) {
      List<Object> values = new ArrayList<>();
      Set<String> keys = new LinkedHashSet<>();
      int keyIndex = 1;

      for (Expression<?> argument : arguments) {
         Object[] valuesArray = argument.getArray(event);
         String[] keysArray = KeyProviderExpression.areKeysRecommended(argument) ? ((KeyProviderExpression)argument).getArrayKeys(event) : null;

         for (int i = 0; i < valuesArray.length; i++) {
            if (keysArray != null) {
               if (!keys.add(keysArray[i])) {
                  continue;
               }
            } else {
               while (keys.contains(String.valueOf(keyIndex))) {
                  keyIndex++;
               }

               keys.add(String.valueOf(keyIndex++));
            }

            values.add(Classes.clone(valuesArray[i]));
         }
      }

      return KeyedValue.zip(values.toArray(), keys.toArray(new String[0]));
   }

   private KeyedValue<?>[] evaluateParameter(Expression<?> argument, Event event) {
      if (argument == null) {
         return null;
      } else {
         Object[] values = argument.getArray(event);

         for (int i = 0; i < values.length; i++) {
            values[i] = Classes.clone(values[i]);
         }

         if (argument instanceof KeyProviderExpression<?> kpe) {
            String[] keys = KeyProviderExpression.areKeysRecommended(argument) ? kpe.getArrayKeys(event) : null;
            return KeyedValue.zip(values, keys);
         } else {
            return KeyedValue.zip(values, null);
         }
      }
   }

   public Function<T> function() {
      if (this.cachedFunction == null) {
         Class<?>[] parameters = Arrays.stream(this.cachedSignature.parameters().all()).map(Parameter::type).toArray(Class[]::new);
         FunctionRegistry.Retrieval<ch.njol.skript.lang.function.Function<?>> retrieval = FunctionRegistry.getRegistry()
            .getFunction(this.namespace, this.name, parameters);
         if (retrieval.result() == FunctionRegistry.RetrievalResult.EXACT) {
            this.cachedFunction = (Function<T>)retrieval.retrieved();
         }
      }

      return this.cachedFunction;
   }

   public Signature<T> signature() {
      return this.cachedSignature;
   }

   public String namespace() {
      return this.namespace;
   }

   @NotNull
   public String name() {
      return this.name;
   }

   @NotNull
   public FunctionReference.Argument<Expression<?>>[] arguments() {
      return this.arguments;
   }

   public boolean isSingle() {
      if (this.cachedSignature.contract() != null) {
         Expression<?>[] args = Arrays.stream(this.arguments).map(it -> it.value).toArray(Expression[]::new);
         return this.cachedSignature.contract().isSingle(args);
      } else {
         return this.cachedSignature.isSingle();
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      StringBuilder builder = new StringBuilder();
      builder.append(this.name);
      builder.append("(");
      StringJoiner args = new StringJoiner(", ");

      for (FunctionReference.Argument<Expression<?>> argument : this.arguments) {
         args.add("%s: %s".formatted(argument.name, argument.value.toString(event, debug)));
      }

      builder.append(args);
      builder.append(")");
      return builder.toString();
   }

   private record ArgInfo(Expression<?> expression, Class<?> type, Set<Parameter.Modifier> modifiers) {
   }

   public record Argument<T>(FunctionReference.ArgumentType type, String name, T value, @Nullable String raw) {
      public Argument(FunctionReference.ArgumentType type, String name, T value) {
         this(type, name, value, null);
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof FunctionReference.Argument<?> argument)
            ? false
            : Objects.equals(this.value, argument.value) && Objects.equals(this.name, argument.name) && this.type == argument.type;
      }

      @Override
      public int hashCode() {
         int result = Objects.hashCode(this.type);
         result = 31 * result + Objects.hashCode(this.name);
         return 31 * result + Objects.hashCode(this.value);
      }
   }

   public static enum ArgumentType {
      NAMED,
      UNNAMED;
   }
}
