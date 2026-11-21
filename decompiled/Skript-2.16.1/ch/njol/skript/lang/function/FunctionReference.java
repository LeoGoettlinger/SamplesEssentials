package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.util.Executable;

@Deprecated(
   forRemoval = true,
   since = "2.14"
)
public class FunctionReference<T> implements Contract, Executable<Event, T[]> {
   private static final String AMBIGUOUS_ERROR = "Skript cannot determine which function named '%s' to call. The following functions were matched: %s. Try clarifying the type of the arguments using the 'value within' expression.";
   final String functionName;
   @Nullable
   private Signature<? extends T> signature;
   @Nullable
   private Function<? extends T> function;
   private boolean singleListParam;
   private final Expression<?>[] parameters;
   private boolean single;
   @Nullable
   final Class<? extends T>[] returnTypes;
   @Nullable
   private final Node node;
   @Nullable
   public final String script;
   private Contract contract;
   private Class<?>[] parameterTypes;

   public FunctionReference(
      String functionName, @Nullable Node node, @Nullable String script, @Nullable Class<? extends T>[] returnTypes, Expression<?>[] params
   ) {
      this.functionName = functionName;
      this.node = node;
      this.script = script;
      this.returnTypes = returnTypes;
      this.parameters = params;
      this.contract = this;
   }

   public boolean validateParameterArity(boolean first) {
      if (!first && this.script == null) {
         return false;
      } else {
         Signature<?> sign = this.getRegisteredSignature();
         return sign == null ? false : this.parameters.length >= sign.getMinParameters();
      }
   }

   public boolean validateFunction(boolean first) {
      if (!first && this.script == null) {
         return false;
      } else {
         Function<? extends T> previousFunction = this.function;
         this.function = null;
         SkriptLogger.setNode(this.node);
         Skript.debug("Validating function " + this.functionName);
         Signature<?> sign = this.getRegisteredSignature();
         StringJoiner args = new StringJoiner(", ");

         for (Class<?> parameterType : this.parameterTypes) {
            args.add(Classes.getSuperClassInfo(Utils.getComponentType(parameterType)).getCodeName());
         }

         String stringified = "%s(%s)".formatted(this.functionName, args);
         if (sign == null) {
            if (first) {
               Skript.error("The function '" + stringified + "' does not exist.");
            } else {
               Skript.error(
                  "The function '"
                     + stringified
                     + "' was deleted or renamed, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
               );
               this.function = previousFunction;
            }

            return false;
         } else {
            Class<? extends T>[] expectedReturnTypes = this.returnTypes;
            if (expectedReturnTypes != null) {
               Class<?> candidateReturnType = sign.returnType();
               if (candidateReturnType == null) {
                  if (first) {
                     Skript.error("The function '" + stringified + "' doesn't return any value.");
                  } else {
                     Skript.error(
                        "The function '"
                           + stringified
                           + "' was redefined with no return value, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
                     );
                     this.function = previousFunction;
                  }

                  return false;
               }

               if (!Converters.converterExists(candidateReturnType, expectedReturnTypes)) {
                  if (first) {
                     Skript.error(
                        "The returned value of the function '"
                           + stringified
                           + "', "
                           + candidateReturnType
                           + ", is "
                           + SkriptParser.notOfType(expectedReturnTypes)
                           + "."
                     );
                  } else {
                     Skript.error(
                        "The function '"
                           + stringified
                           + "' was redefined with a different, incompatible return type, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
                     );
                     this.function = previousFunction;
                  }

                  return false;
               }

               if (first) {
                  this.single = sign.isSingle();
               } else if (this.single && !sign.isSingle()) {
                  Skript.error(
                     "The function '"
                        + this.functionName
                        + "' was redefined with a different, incompatible return type, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
                  );
                  this.function = previousFunction;
                  return false;
               }
            }

            this.singleListParam = sign.getMaxParameters() == 1 && !sign.parameters().getFirst().isSingle();
            if (!this.singleListParam && this.parameters.length > sign.getMaxParameters()) {
               if (first) {
                  if (sign.getMaxParameters() == 0) {
                     Skript.error(
                        "The function '"
                           + stringified
                           + "' has no arguments, but "
                           + this.parameters.length
                           + " are given. To call a function without parameters, just write the function name followed by '()', e.g. 'func()'."
                     );
                  } else {
                     Skript.error(
                        "The function '"
                           + stringified
                           + "' has only "
                           + sign.getMaxParameters()
                           + " argument"
                           + (sign.getMaxParameters() == 1 ? "" : "s")
                           + ", but "
                           + this.parameters.length
                           + " are given. If you want to use lists in function calls, you have to use additional parentheses, e.g. 'give(player, (iron ore and gold ore))'"
                     );
                  }
               } else {
                  Skript.error(
                     "The function '"
                        + stringified
                        + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
                  );
                  this.function = previousFunction;
               }

               return false;
            } else if (this.parameters.length < sign.getMinParameters()) {
               if (first) {
                  Skript.error(
                     "The function '"
                        + stringified
                        + "' requires at least "
                        + sign.getMinParameters()
                        + " argument"
                        + (sign.getMinParameters() == 1 ? "" : "s")
                        + ", but only "
                        + this.parameters.length
                        + " "
                        + (this.parameters.length == 1 ? "is" : "are")
                        + " given."
                  );
               } else {
                  Skript.error(
                     "The function '"
                        + stringified
                        + "' was redefined with a different, incompatible amount of arguments, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
                  );
                  this.function = previousFunction;
               }

               return false;
            } else {
               for (int i = 0; i < this.parameters.length; i++) {
                  org.skriptlang.skript.common.function.Parameter<?> signatureParam = sign.parameters().get(this.singleListParam ? 0 : i);
                  RetainingLogHandler log = SkriptLogger.startRetainingLog();

                  try {
                     Class<?> target = Utils.getComponentType(signatureParam.type());
                     Expression<?> exprParam = this.parameters[i].getConvertedExpression(target);
                     if (exprParam == null) {
                        if (first) {
                           if (LiteralUtils.hasUnparsedLiteral(this.parameters[i])) {
                              Skript.error("Can't understand this expression: " + this.parameters[i].toString());
                           } else {
                              String type = Classes.toString(Classes.getSuperClassInfo(target));
                              Skript.error(
                                 "The "
                                    + StringUtils.fancyOrderNumber(i + 1)
                                    + " argument given to the function '"
                                    + stringified
                                    + "' is not of the required type "
                                    + type
                                    + ". Check the correct order of the arguments and put lists into parentheses if appropriate (e.g. 'give(player, (iron ore and gold ore))'). Please note that storing the value in a variable and then using that variable as parameter may suppress this error, but it still won't work."
                              );
                           }
                        } else {
                           Skript.error(
                              "The function '"
                                 + stringified
                                 + "' was redefined with different, incompatible arguments, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
                           );
                           this.function = previousFunction;
                        }

                        return false;
                     }

                     if (signatureParam.isSingle() && !exprParam.isSingle()) {
                        if (first) {
                           Skript.error(
                              "The "
                                 + StringUtils.fancyOrderNumber(i + 1)
                                 + " argument given to the function '"
                                 + this.functionName
                                 + "' is plural, but a single argument was expected"
                           );
                        } else {
                           Skript.error(
                              "The function '"
                                 + stringified
                                 + "' was redefined with different, incompatible arguments, but is still used in other script(s). These will continue to use the old version of the function until Skript restarts."
                           );
                           this.function = previousFunction;
                        }

                        return false;
                     }

                     if (signatureParam.hasModifier(org.skriptlang.skript.common.function.Parameter.Modifier.RANGED)
                        && exprParam instanceof Literal<?> literalParam) {
                        org.skriptlang.skript.common.function.Parameter.Modifier.RangedModifier<?> range = signatureParam.getModifier(
                           org.skriptlang.skript.common.function.Parameter.Modifier.RangedModifier.class
                        );
                        if (!range.inRange(literalParam.getArray())) {
                           Skript.error(
                              "The argument '"
                                 + signatureParam.name()
                                 + "' only accepts values between "
                                 + Classes.toString(range.getMin())
                                 + " and "
                                 + Classes.toString(range.getMax())
                                 + ". Provided: "
                                 + literalParam.toString(null, Skript.debug())
                           );
                           return false;
                        }
                     }

                     this.parameters[i] = exprParam;
                  } finally {
                     log.printLog();
                  }
               }

               this.signature = (Signature<? extends T>)sign;
               org.skriptlang.skript.common.function.FunctionReference.Argument<Expression<?>>[] arguments = Arrays.stream(this.parameters)
                  .map(
                     it -> new org.skriptlang.skript.common.function.FunctionReference.Argument<>(
                        org.skriptlang.skript.common.function.FunctionReference.ArgumentType.UNNAMED, null, it
                     )
                  )
                  .toArray(org.skriptlang.skript.common.function.FunctionReference.Argument[]::new);
               sign.calls().add(new org.skriptlang.skript.common.function.FunctionReference<>(this.script, this.functionName, this.signature, arguments));
               Contract contract = sign.getContract();
               if (contract != null) {
                  this.contract = contract;
               }

               return true;
            }
         }
      }
   }

   private void parseParameters() {
      if (this.parameterTypes == null) {
         this.parameterTypes = new Class[this.parameters.length];

         for (int i = 0; i < this.parameters.length; i++) {
            Expression<?> parsed = LiteralUtils.defendExpression(this.parameters[i]);
            this.parameterTypes[i] = parsed.getReturnType();
         }
      }
   }

   private Signature<?> getRegisteredSignature() {
      this.parseParameters();
      if (Skript.debug()) {
         Skript.debug(
            "Getting signature for '%s' with types %s",
            this.functionName,
            Arrays.toString(Arrays.stream(this.parameterTypes).map(Class::getSimpleName).toArray())
         );
      }

      FunctionRegistry.Retrieval<Signature<?>> attempt = FunctionRegistry.getRegistry().getSignature(this.script, this.functionName, this.parameterTypes);
      if (attempt.result() == FunctionRegistry.RetrievalResult.EXACT) {
         return attempt.retrieved();
      } else {
         if (attempt.result() == FunctionRegistry.RetrievalResult.AMBIGUOUS) {
            this.ambiguousError(attempt.conflictingArgs());
         }

         return null;
      }
   }

   private Function<?> getRegisteredFunction() {
      this.parseParameters();
      if (Skript.debug()) {
         Skript.debug(
            "Getting function '%s' with types %s", this.functionName, Arrays.toString(Arrays.stream(this.parameterTypes).map(Class::getSimpleName).toArray())
         );
      }

      FunctionRegistry.Retrieval<Function<?>> attempt = FunctionRegistry.getRegistry().getFunction(this.script, this.functionName, this.parameterTypes);
      if (attempt.result() == FunctionRegistry.RetrievalResult.EXACT) {
         return attempt.retrieved();
      } else {
         if (attempt.result() == FunctionRegistry.RetrievalResult.AMBIGUOUS) {
            this.ambiguousError(attempt.conflictingArgs());
         }

         return null;
      }
   }

   @Nullable
   public Function<? extends T> getFunction() {
      return this.function;
   }

   @Nullable
   public String[] returnedKeys() {
      return this.function != null ? this.function.returnedKeys() : null;
   }

   public boolean resetReturnValue() {
      return this.function != null ? this.function.resetReturnValue() : false;
   }

   @Nullable
   protected T[] execute(Event event) {
      if (this.function == null) {
         this.function = (Function<? extends T>)this.getRegisteredFunction();
      }

      if (this.function == null) {
         Skript.error("Couldn't resolve call for '" + this.functionName + "'.");
         return null;
      } else {
         Object[][] params = new Object[this.singleListParam ? 1 : this.parameters.length][];
         if (this.singleListParam) {
            params[0] = this.evaluateSingleListParameter(this.function.signature().parameters().getFirst(), this.parameters, event);
         } else {
            for (int i = 0; i < this.parameters.length; i++) {
               params[i] = this.function.signature().parameters().get(i).evaluate(this.parameters[i], event);
            }
         }

         return (T[])this.function.execute(params);
      }
   }

   private Object[] evaluateSingleListParameter(org.skriptlang.skript.common.function.Parameter<?> parameter, Expression<?>[] arguments, Event event) {
      if (arguments.length == 0) {
         return null;
      } else if (arguments.length == 1) {
         return parameter.evaluate(arguments[0], event);
      } else if (!parameter.hasModifier(org.skriptlang.skript.common.function.Parameter.Modifier.KEYED)) {
         List<Object> list = new ArrayList<>();

         for (Expression<?> argument : arguments) {
            list.addAll(Arrays.asList(parameter.evaluate(argument, event)));
         }

         return list.toArray();
      } else {
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
   }

   public boolean isSingle() {
      return this.contract.isSingle(this.parameters);
   }

   @Override
   public boolean isSingle(Expression<?>... arguments) {
      return this.single;
   }

   @Nullable
   public Class<? extends T> getReturnType() {
      return (Class<? extends T>)this.contract.getReturnType(this.parameters);
   }

   @Nullable
   @Override
   public Class<?> getReturnType(Expression<?>... arguments) {
      if (this.signature == null) {
         throw new SkriptAPIException("Signature of function is null when return type is asked!");
      } else {
         return this.signature.returnType();
      }
   }

   public Contract getContract() {
      return this.contract;
   }

   public String toString(@Nullable Event event, boolean debug) {
      StringBuilder b = new StringBuilder(this.functionName + "(");

      for (int i = 0; i < this.parameters.length; i++) {
         if (i != 0) {
            b.append(", ");
         }

         b.append(this.parameters[i].toString(event, debug));
      }

      b.append(")");
      return b.toString();
   }

   public T[] execute(Event event, Object... arguments) {
      if (this.function == null) {
         this.function = (Function<? extends T>)this.getRegisteredFunction();
      }

      if (this.function == null) {
         Skript.error("Couldn't resolve call for '" + this.functionName + "'.");
         return null;
      } else {
         Object[][] consigned = consign(arguments);

         Object[] var4;
         try {
            var4 = this.function.execute(consigned);
         } finally {
            this.resetReturnValue();
         }

         return (T[])var4;
      }
   }

   static Object[][] consign(Object... arguments) {
      Object[][] consigned = new Object[arguments.length][];

      for (int i = 0; i < consigned.length; i++) {
         if (!(arguments[i] instanceof Object[]) && arguments[i] != null) {
            consigned[i] = new Object[]{arguments[i]};
         } else {
            consigned[i] = (Object[])arguments[i];
         }
      }

      return consigned;
   }

   private void ambiguousError(Class<?>[][] conflictingArgs) {
      List<String> parts = new ArrayList<>();

      for (Class<?>[] args : conflictingArgs) {
         String argNames = Arrays.stream(args).map(arg -> {
            String name = Classes.getExactClassName((Class<?>)arg);
            return name == null ? arg.getSimpleName() : name.toLowerCase();
         }).collect(Collectors.joining(", "));
         parts.add("%s(%s)".formatted(this.functionName, argNames));
      }

      Skript.error(
         "Skript cannot determine which function named '%s' to call. The following functions were matched: %s. Try clarifying the type of the arguments using the 'value within' expression.",
         this.functionName,
         StringUtils.join(parts, ", ", " and ")
      );
   }
}
