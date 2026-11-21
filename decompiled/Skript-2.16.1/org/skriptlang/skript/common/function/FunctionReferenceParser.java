package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.FunctionRegistry;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FunctionReferenceParser(ParseContext context, int flags) {
   private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile("(?<name>[\\p{IsAlphabetic}_][\\p{IsAlphabetic}\\d_]*)\\((?<args>.*)\\)");
   private static final ArgsMessage UNEXPECTED_ARGUMENT = new ArgsMessage("functions.unexpected argument");
   private static final ArgsMessage INVALID_ARGUMENT = new ArgsMessage("functions.invalid argument");
   private static final ArgsMessage UNKNOWN_FUNCTION = new ArgsMessage("functions.unknown function");
   private static final ArgsMessage POTENTIAL_SIGNATURE = new ArgsMessage("functions.potential signature");

   public <T> FunctionReference<T> parseFunctionReference(String expr) {
      FunctionReference var14;
      try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
         if (!expr.endsWith(")")) {
            log.printLog();
            return null;
         }

         Matcher matcher = FUNCTION_CALL_PATTERN.matcher(expr);
         if (!matcher.matches()) {
            log.printLog();
            return null;
         }

         String functionName = matcher.group("name");
         String args = matcher.group("args");
         int i = 0;

         while (i < args.length()) {
            if (i == -1) {
               log.printLog();
               return null;
            }

            i = SkriptParser.next(args, i, this.context);
         }

         if ((this.flags & 1) == 0) {
            Skript.error("Functions cannot be used here (or there is a problem with your arguments).");
            log.printError();
            return null;
         }

         FunctionReference.Argument<String>[] arguments = new FunctionArgumentParser(args).getArguments();
         var14 = this.parseFunctionReference(functionName, arguments, log);
      }

      return var14;
   }

   public <T> FunctionReference<T> parseFunctionReference(String name, FunctionReference.Argument<String>[] arguments, ParseLogHandler log) {
      Set<String> named = new HashSet<>();

      for (FunctionReference.Argument<String> argument : arguments) {
         if (argument.type() == FunctionReference.ArgumentType.NAMED) {
            boolean added = named.add(argument.name());
            if (!added) {
               Skript.error(Language.get("functions.already assigned value to parameter"), argument.name());
               log.printError();
               return null;
            }
         }
      }

      ParserInstance parser = ParserInstance.get();
      String namespace;
      if (parser.isActive()) {
         namespace = parser.getCurrentScript().getConfig().getFileName();
      } else {
         namespace = null;
      }

      Set<ch.njol.skript.lang.function.Signature<?>> options = FunctionRegistry.getRegistry().getSignatures(namespace, name);
      if (options.isEmpty()) {
         this.doesNotExist(name, arguments, options);
         log.printError();
         return null;
      } else {
         Set<ch.njol.skript.lang.function.Signature<?>> exacts = new HashSet<>();
         Set<ch.njol.skript.lang.function.Signature<?>> lists = new HashSet<>();

         for (ch.njol.skript.lang.function.Signature<?> option : options) {
            if (option.parameters().size() == 1 && !option.parameters().getFirst().isSingle()) {
               lists.add(option);
            } else {
               exacts.add(option);
            }
         }

         Set<FunctionReference<T>> exactReferences = this.getExactReferences(namespace, name, exacts, arguments);
         if (exactReferences == null) {
            log.printError();
            return null;
         } else if (exactReferences.size() == 1) {
            return exactReferences.stream().findAny().orElse(null);
         } else {
            Set<FunctionReference<T>> listReferences = this.getListReferences(namespace, name, lists, arguments);
            if (listReferences == null) {
               log.printError();
               return null;
            } else {
               exactReferences.addAll(listReferences);
               if (exactReferences.isEmpty()) {
                  this.doesNotExist(name, arguments, Sets.union(exacts, lists));
                  log.printError();
                  return null;
               } else if (exactReferences.size() == 1) {
                  return exactReferences.stream().findAny().orElse(null);
               } else {
                  this.ambiguousError(name, exactReferences);
                  log.printError();
                  return null;
               }
            }
         }
      }
   }

   private <T> Set<FunctionReference<T>> getExactReferences(
      String namespace, String name, Set<ch.njol.skript.lang.function.Signature<?>> signatures, FunctionReference.Argument<String>[] arguments
   ) {
      Set<FunctionReference<T>> exactReferences = new HashSet<>();
      boolean hasNames = Arrays.stream(arguments).anyMatch(argument -> argument.type() == FunctionReference.ArgumentType.NAMED);
      FunctionReference.Argument<String>[] originalArguments = arguments;
      Iterator var8 = signatures.iterator();

      FunctionReference.Argument<String> argument;
      label150:
      while (true) {
         if (!var8.hasNext()) {
            return exactReferences;
         }

         ch.njol.skript.lang.function.Signature<?> signature = (ch.njol.skript.lang.function.Signature<?>)var8.next();
         if (arguments.length <= signature.getMaxParameters() && arguments.length >= signature.getMinParameters()) {
            arguments = originalArguments;
            SequencedMap<String, Parameter<?>> parameters = signature.parameters().sequencedMap();
            FunctionReference.Argument<String>[] parseArguments = new FunctionReference.Argument[parameters.size()];
            FunctionReferenceParser.ArgumentParseTarget[] parseTargets = new FunctionReferenceParser.ArgumentParseTarget[parameters.size()];
            int index = 0;
            boolean hasPlaceholders = false;

            for (Entry<String, Parameter<?>> entry : parameters.entrySet()) {
               Parameter<?> parameter = entry.getValue();
               FunctionReference.Argument<String> argumentx = null;
               if (hasNames) {
                  for (FunctionReference.Argument<String> candidate : arguments) {
                     if (entry.getKey().equals(candidate.name())) {
                        argumentx = candidate;
                        break;
                     }
                  }
               } else if (index < arguments.length) {
                  argumentx = new FunctionReference.Argument<>(FunctionReference.ArgumentType.UNNAMED, entry.getKey(), arguments[index].value());
               }

               if (argumentx == null) {
                  argumentx = new FunctionReference.Argument<>(FunctionReference.ArgumentType.UNNAMED, entry.getKey(), null);
                  hasPlaceholders = true;
               }

               parseArguments[index] = argumentx;
               Class<?> targetType = Utils.getComponentType(parameter.type());
               Expression<?> fallback;
               if (parameter instanceof ScriptParameter<?> sp) {
                  fallback = sp.defaultValue();
               } else if (parameter.hasModifier(Parameter.Modifier.OPTIONAL)) {
                  fallback = new FunctionReferenceParser.EmptyExpression();
               } else {
                  fallback = null;
               }

               parseTargets[index] = new FunctionReferenceParser.ArgumentParseTarget(targetType, fallback);
               index++;
            }

            if (hasPlaceholders) {
               boolean copied = false;

               for (int i = 0; i < arguments.length; i++) {
                  FunctionReference.Argument<String> argumentxx = arguments[i];
                  if (argumentxx.type() == FunctionReference.ArgumentType.NAMED && !parameters.containsKey(argumentxx.name())) {
                     if (!copied) {
                        arguments = Arrays.copyOf(arguments, arguments.length);
                        copied = true;
                     }

                     arguments[i] = new FunctionReference.Argument<>(FunctionReference.ArgumentType.UNNAMED, argumentxx.name(), argumentxx.raw());
                  }
               }

               List<String> priorNames = new ArrayList<>();
               int lastCheck = -1;
               int lastFilled = -1;

               for (int ix = 0; ix < arguments.length; ix++) {
                  argument = arguments[ix];
                  if (argument.type() == FunctionReference.ArgumentType.NAMED) {
                     priorNames.add(argument.name());
                     if (ix > 0 && arguments[ix - 1].type() == FunctionReference.ArgumentType.NAMED) {
                        lastCheck = -1;
                     }
                  } else {
                     String nextName = null;

                     for (int j = ix + 1; j < arguments.length; j++) {
                        FunctionReference.Argument<String> nextArgument = arguments[j];
                        if (nextArgument.type() == FunctionReference.ArgumentType.NAMED) {
                           nextName = nextArgument.name();
                           break;
                        }
                     }

                     int jx = lastCheck + 1;

                     while (true) {
                        if (jx >= parseArguments.length) {
                           break label150;
                        }

                        FunctionReference.Argument<String> parseArgument = parseArguments[jx];
                        if (parseArgument.type() == FunctionReference.ArgumentType.NAMED) {
                           if (parseArgument.name().equals(nextName)) {
                              break label150;
                           }

                           priorNames.remove(parseArgument.name());
                        } else if (jx > lastFilled && priorNames.isEmpty()) {
                           parseArguments[jx] = new FunctionReference.Argument<>(FunctionReference.ArgumentType.UNNAMED, parseArgument.name(), argument.value());
                           lastCheck = jx;
                           lastFilled = jx;
                           break;
                        }

                        jx++;
                     }
                  }
               }
            }

            FunctionReferenceParser.ArgumentParseResult result = this.parseFunctionArguments(parseArguments, parseTargets);
            switch (result.type()) {
               case OK:
                  FunctionReference<T> reference = new FunctionReference<>(namespace, name, (Signature<T>)signature, result.parsed());
                  if (reference.validate()) {
                     exactReferences.add(reference);
                  }
                  break;
               case LIST_ERROR:
                  return null;
            }
         }
      }

      if (argument.name() != null) {
         Skript.error(UNEXPECTED_ARGUMENT.toString(argument.name()));
      } else {
         Skript.error(Language.get("functions.mixing named and unnamed arguments"));
      }

      return null;
   }

   private <T> Set<FunctionReference<T>> getListReferences(
      String namespace, String name, Set<ch.njol.skript.lang.function.Signature<?>> signatures, FunctionReference.Argument<String>[] arguments
   ) {
      if (arguments.length > 1) {
         for (FunctionReference.Argument<String> argument : arguments) {
            if (argument.type() == FunctionReference.ArgumentType.NAMED) {
               this.doesNotExist(name, arguments, signatures);
               return null;
            }
         }
      }

      Set<FunctionReference<T>> references = new HashSet<>();

      label71:
      for (ch.njol.skript.lang.function.Signature<?> signature : signatures) {
         Parameter<?> parameter = signature.parameters().getFirst();
         Class<?> targetType = parameter.type().componentType();
         Expression<?> fallback;
         if (parameter instanceof ScriptParameter<?> sp) {
            fallback = sp.defaultValue();
         } else if (parameter.hasModifier(Parameter.Modifier.OPTIONAL)) {
            fallback = new FunctionReferenceParser.EmptyExpression();
         } else {
            fallback = null;
         }

         FunctionReferenceParser.ArgumentParseTarget[] parseTargets = new FunctionReferenceParser.ArgumentParseTarget[]{
            new FunctionReferenceParser.ArgumentParseTarget(targetType, fallback)
         };
         if (arguments.length == 1 && arguments[0].type() == FunctionReference.ArgumentType.NAMED && !arguments[0].name().equals(parameter.name())) {
            this.doesNotExist(name, arguments, signatures);
         } else {
            String joined = arguments.length == 0 ? null : Arrays.stream(arguments).map(FunctionReference.Argument::value).collect(Collectors.joining(", "));
            FunctionReference.Argument<String> argumentx = new FunctionReference.Argument<>(FunctionReference.ArgumentType.NAMED, parameter.name(), joined);
            FunctionReference.Argument<String>[] array = CollectionUtils.array(argumentx);
            FunctionReferenceParser.ArgumentParseResult result = this.parseFunctionArguments(array, parseTargets);
            if (result.type() == FunctionReferenceParser.ArgumentParseResultType.LIST_ERROR) {
               return null;
            }

            if (result.type() == FunctionReferenceParser.ArgumentParseResultType.OK) {
               if (result.parsed().length == 1 && result.parsed()[0].value() instanceof ExpressionList<?> list) {
                  for (Expression<?> expression : list.getExpressions()) {
                     if (expression instanceof ExpressionList) {
                        this.doesNotExist(name, arguments, signatures);
                        continue label71;
                     }
                  }
               }

               FunctionReference<T> reference = new FunctionReference<>(namespace, name, (Signature<T>)signature, result.parsed());
               if (reference.validate()) {
                  references.add(reference);
               }
            }
         }
      }

      return references;
   }

   private <T> void ambiguousError(String name, Set<FunctionReference<T>> references) {
      List<String> parts = new ArrayList<>();

      for (FunctionReference<T> reference : references) {
         String builder = reference.name()
            + "("
            + Arrays.stream(reference.signature().parameters().all())
               .map(
                  it -> it.type().isArray()
                     ? Classes.getSuperClassInfo(it.type().componentType()).getName().getPlural()
                     : Classes.getSuperClassInfo(it.type()).getName().getSingular()
               )
               .collect(Collectors.joining(", "))
            + ")";
         parts.add(builder);
      }

      Skript.error(Language.get("functions.ambiguous function call"), name, StringUtils.join(parts, ", ", " or "));
   }

   private void doesNotExist(String name, FunctionReference.Argument<String>[] arguments, Set<ch.njol.skript.lang.function.Signature<?>> possibleSignatures) {
      StringJoiner joiner = new StringJoiner(", ");
      List<Class<?>> argumentTypes = new ArrayList<>();

      for (FunctionReference.Argument<String> argument : arguments) {
         SkriptParser parser = new SkriptParser(argument.value(), this.flags | 2, this.context);
         Expression<?> expression = LiteralUtils.defendExpression(parser.parseExpression(Object.class));
         String argumentName;
         if (argument.type() == FunctionReference.ArgumentType.NAMED) {
            argumentName = argument.name() + ": ";
         } else {
            argumentName = "";
         }

         if (!LiteralUtils.canInitSafely(expression)) {
            joiner.add(argumentName + "?");
         } else {
            Class<?> returnType = expression.getReturnType();
            argumentTypes.add(returnType);
            ClassInfo<?> classInfo = Classes.getSuperClassInfo(returnType);
            if (expression.isSingle()) {
               joiner.add(argumentName + classInfo.getName().getSingular());
            } else {
               joiner.add(argumentName + classInfo.getName().getPlural());
            }
         }
      }

      String possibleMatch = "";
      Optional<ch.njol.skript.lang.function.Signature<?>> intended = possibleSignatures.stream().filter(signature -> {
         List<Class<?>> currentArgumentTypes = new ArrayList<>(argumentTypes);
         Arrays.stream(signature.parameters().all()).map(parameter -> Utils.getComponentType(parameter.type())).forEach(type -> {
            Iterator<Class<?>> iterator = currentArgumentTypes.iterator();

            while (iterator.hasNext()) {
               if (type.isAssignableFrom(iterator.next())) {
                  iterator.remove();
                  break;
               }
            }
         });
         return currentArgumentTypes.isEmpty();
      }).min(Comparator.comparingInt(signature -> Math.abs(arguments.length - signature.getMaxParameters())));
      if (intended.isPresent()) {
         possibleMatch = " " + POTENTIAL_SIGNATURE.toString(intended.get().toString(false, false));
      }

      Skript.error(UNKNOWN_FUNCTION.toString(name, joiner) + possibleMatch);
   }

   private FunctionReferenceParser.ArgumentParseResult parseFunctionArguments(
      FunctionReference.Argument<String>[] arguments, FunctionReferenceParser.ArgumentParseTarget[] targets
   ) {
      assert arguments.length == targets.length;

      assert Arrays.stream(targets).map(FunctionReferenceParser.ArgumentParseTarget::type).noneMatch(Class::isArray);

      FunctionReference.Argument<Expression<?>>[] parsed = new FunctionReference.Argument[arguments.length];

      for (int i = 0; i < arguments.length; i++) {
         FunctionReference.Argument<String> argument = arguments[i];
         FunctionReferenceParser.ArgumentParseTarget targetData = targets[i];
         Expression<?> expression = this.parseExpression(argument, targetData);
         if (expression == null) {
            return new FunctionReferenceParser.ArgumentParseResult(FunctionReferenceParser.ArgumentParseResultType.PARSE_FAIL, null);
         }

         if (expression instanceof ExpressionList<?> list && !list.getAnd()) {
            Skript.error(Language.get("functions.or in arguments"));
            return new FunctionReferenceParser.ArgumentParseResult(FunctionReferenceParser.ArgumentParseResultType.LIST_ERROR, null);
         }

         parsed[i] = new FunctionReference.Argument<>(argument.type(), argument.name(), expression);
      }

      return new FunctionReferenceParser.ArgumentParseResult(FunctionReferenceParser.ArgumentParseResultType.OK, parsed);
   }

   private Expression<?> parseExpression(FunctionReference.Argument<String> argument, FunctionReferenceParser.ArgumentParseTarget targetData) {
      if (argument.value() == null) {
         return targetData.fallback;
      } else {
         SkriptParser parser = new SkriptParser(argument.value(), this.flags | 2, this.context);

         Expression var6;
         try (ParseLogHandler logHandler = new ParseLogHandler().start()) {
            Expression<?> expression = parser.parseExpression(targetData.type());
            if (expression == null) {
               logHandler.printError(
                  INVALID_ARGUMENT.toString(argument.name(), Classes.getSuperClassInfo(targetData.type()).getName().getSingular(), argument.value())
               );
            }

            var6 = expression;
         }

         return var6;
      }
   }

   private record ArgumentParseResult(@NotNull FunctionReferenceParser.ArgumentParseResultType type, FunctionReference.Argument<Expression<?>>[] parsed) {
   }

   private static enum ArgumentParseResultType {
      OK,
      PARSE_FAIL,
      LIST_ERROR;
   }

   private record ArgumentParseTarget(@NotNull Class<?> type, @Nullable Expression<?> fallback) {
   }

   public static class EmptyExpression extends SimpleLiteral<Integer> {
      public EmptyExpression() {
         super(1, true);
      }
   }
}
