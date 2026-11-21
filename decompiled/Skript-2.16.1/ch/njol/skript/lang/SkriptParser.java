package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.lang.parser.DefaultValueData;
import ch.njol.skript.lang.parser.ExpressionParseCache;
import ch.njol.skript.lang.parser.ParseStackOverflowException;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.simplification.Simplifiable;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.patterns.TypePatternElement;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Booleans;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionReference;
import org.skriptlang.skript.common.function.FunctionReferenceParser;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.ExperimentalSyntax;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.log.runtime.RuntimeErrorCatcher;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public final class SkriptParser {
   private final String expr;
   public static final int PARSE_EXPRESSIONS = 1;
   public static final int PARSE_LITERALS = 2;
   public static final int ALL_FLAGS = 3;
   private final int flags;
   public final boolean doSimplification = SkriptConfig.simplifySyntaxesOnParse.value();
   public final ParseContext context;
   public static final String WILDCARD = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?";
   private static final Pattern VARIABLE_PATTERN = Pattern.compile("((the )?var(iable)? )?\\{.+\\}", 2);
   private static final String INVALID_LSPEC_CHARS = "[^,():/\"\\[\\]}{]";
   private static final Pattern LITERAL_SPECIFICATION_PATTERN = Pattern.compile("(?<literal>[^,():/\"\\[\\]}{]+) \\((?<classinfo>[\\w\\p{L} ]+)\\)");
   public static final Pattern LIST_SPLIT_PATTERN = Pattern.compile("\\s*,?\\s+(and|n?or)\\s+|\\s*,\\s*", 2);
   public static final Pattern OR_PATTERN = Pattern.compile("\\sor\\s", 2);
   private static final String MULTIPLE_AND_OR = "List has multiple 'and' or 'or', will default to 'and'. Use brackets if you want to define multiple lists.";
   private static final String MISSING_AND_OR = "List is missing 'and' or 'or', defaulting to 'and'";
   private final boolean suppressMissingAndOrWarnings = SkriptConfig.disableMissingAndOrWarnings.value();
   private static final Map<String, SkriptPattern> patterns = new ConcurrentHashMap<>();
   private static final Message M_QUOTES_ERROR = new Message("skript.quotes error");
   private static final Message M_BRACKETS_ERROR = new Message("skript.brackets error");

   public SkriptParser(String expr) {
      this(expr, 3);
   }

   public SkriptParser(String expr, int flags) {
      this(expr, flags, ParseContext.DEFAULT);
   }

   public SkriptParser(String expr, int flags, ParseContext context) {
      assert expr != null;

      assert (flags & 3) != 0;

      this.expr = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(expr.trim());
      this.flags = flags;
      this.context = context;
   }

   public SkriptParser(SkriptParser other, String expr) {
      this(expr, other.flags, other.context);
   }

   @Nullable
   public static <T> Literal<? extends T> parseLiteral(String expr, Class<T> expectedClass, ParseContext context) {
      expr = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(expr.trim());
      return expr.isEmpty() ? null : new UnparsedLiteral(expr).getConvertedExpression(context, expectedClass);
   }

   @Nullable
   public static <T extends SyntaxElement> T parse(String expr, Iterator<? extends SyntaxInfo<T>> source, @Nullable String defaultError) {
      expr = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(expr.trim());
      if (expr.isEmpty()) {
         Skript.error(defaultError);
         return null;
      } else {
         Object var5;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            T element = new SkriptParser(expr).parse(source);
            if (element != null) {
               log.printLog();
               return element;
            }

            log.printError(defaultError);
            var5 = null;
         }

         return (T)var5;
      }
   }

   @Nullable
   public static <T extends SyntaxElement> T parseStatic(String expr, Iterator<? extends SyntaxInfo<? extends T>> source, @Nullable String defaultError) {
      return parseStatic(expr, source, ParseContext.DEFAULT, defaultError);
   }

   @Nullable
   public static <T extends SyntaxElement> T parseStatic(
      String expr, Iterator<? extends SyntaxInfo<? extends T>> source, ParseContext parseContext, @Nullable String defaultError
   ) {
      expr = expr.trim();
      if (expr.isEmpty()) {
         Skript.error(defaultError);
         return null;
      } else {
         Object var6;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            T element = new SkriptParser(expr, 2, parseContext).parse(source);
            if (element != null) {
               log.printLog();
               return element;
            }

            log.printError(defaultError);
            var6 = null;
         }

         return (T)var6;
      }
   }

   @Nullable
   private <T extends SyntaxElement> T parse(Iterator<? extends SyntaxInfo<? extends T>> source) {
      ParsingStack parsingStack = getParser().getParsingStack();

      try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
         while (source.hasNext()) {
            SyntaxInfo<? extends T> info = (SyntaxInfo<? extends T>)source.next();
            int matchedPattern = -1;

            for (String pattern : info.patterns()) {
               matchedPattern++;
               log.clear();

               SkriptParser.ParseResult parseResult;
               try {
                  parsingStack.push(new ParsingStack.Element(info, matchedPattern));
                  parseResult = this.parse_i(pattern);
               } catch (MalformedPatternException var25) {
                  String message = "pattern compiling exception, element class: " + info.type().getName();

                  try {
                     JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(info.type());
                     message = message + " (provided by " + providingPlugin.getName() + ")";
                  } catch (IllegalStateException | IllegalArgumentException var24) {
                  }

                  throw new RuntimeException(message, var25);
               } catch (StackOverflowError var26) {
                  throw new ParseStackOverflowException(var26, new ParsingStack(parsingStack));
               } finally {
                  ParsingStack.Element stackElement = parsingStack.pop();

                  assert stackElement.syntaxElementInfo() == info && stackElement.patternIndex() == matchedPattern;
               }

               if (parseResult != null) {
                  assert parseResult.source != null;

                  List<TypePatternElement> types = null;
                  int i = 0;

                  while (true) {
                     if (i >= parseResult.exprs.length) {
                        T element = (T)info.instance();
                        if (!checkRestrictedEvents(element, parseResult) || !checkExperimentalSyntax(element)) {
                           break;
                        }

                        boolean success = element.preInit() && element.init(parseResult.exprs, matchedPattern, getParser().getHasDelayBefore(), parseResult);
                        if (!success) {
                           break;
                        }

                        Expression[] var34 = parseResult.exprs;
                        int var38 = var34.length;

                        for (int var39 = 0; var39 < var38; var39++) {
                           if (var34[var39] instanceof UnparsedLiteral unparsedLiteral && unparsedLiteral.multipleWarning()) {
                              log.printLog();
                              if (!this.doSimplification || !(element instanceof Simplifiable<?> simplifiable)) {
                                 return element;
                              }

                              T var41 = this.simplify((Simplifiable<SyntaxElement>)simplifiable);
                              if (var41 != null) {
                                 return (T)var41;
                              }
                              break;
                           }
                        }
                     }

                     if (parseResult.exprs[i] == null) {
                        if (types == null) {
                           types = parseResult.source.getElements(TypePatternElement.class);
                        }

                        SkriptParser.ExprInfo exprInfo = types.get(i).getExprInfo();
                        if (!exprInfo.isOptional) {
                           List<DefaultExpression<?>> exprs = getDefaultExpressions(exprInfo, pattern);
                           DefaultExpression<?> matchedExpr = null;

                           for (DefaultExpression<?> expr : exprs) {
                              if (expr.init()) {
                                 matchedExpr = expr;
                                 break;
                              }
                           }

                           if (matchedExpr == null) {
                              break;
                           }

                           parseResult.exprs[i] = matchedExpr;
                        }
                     }

                     i++;
                  }
               }
            }
         }

         log.printError();
         return null;
      }
   }

   @Nullable
   private <T extends SyntaxElement> T simplify(@NotNull Simplifiable<T> element) {
      SyntaxElement var5;
      try (RuntimeErrorCatcher catcher = new RuntimeErrorCatcher().start()) {
         T simplified = element.simplify();
         AtomicBoolean error = new AtomicBoolean(false);
         catcher.getCachedErrors().stream().filter(err -> err.level() == Level.SEVERE).findFirst().ifPresent(err -> {
            Skript.error(err.error());
            error.set(true);
         });
         catcher.getCachedErrors().stream().filter(err -> err.level() == Level.WARNING).findFirst().ifPresent(warning -> Skript.warning(warning.error()));
         if (error.get()) {
            return null;
         }

         var5 = simplified;
      }

      return (T)var5;
   }

   private static boolean checkRestrictedEvents(SyntaxElement element, SkriptParser.ParseResult parseResult) {
      if (element instanceof EventRestrictedSyntax eventRestrictedSyntax) {
         Class<? extends Event>[] supportedEvents = eventRestrictedSyntax.supportedEvents();
         if (!getParser().isCurrentEvent(supportedEvents)) {
            Skript.error("'" + parseResult.expr + "' can only be used in " + supportedEventsNames(supportedEvents));
            return false;
         }
      }

      return true;
   }

   @NotNull
   private static String supportedEventsNames(Class<? extends Event>[] supportedEvents) {
      List<String> names = new ArrayList<>();

      for (SkriptEventInfo<?> eventInfo : Skript.getEvents()) {
         for (Class<? extends Event> eventClass : supportedEvents) {
            for (Class<? extends Event> event : eventInfo.events) {
               if (event.isAssignableFrom(eventClass)) {
                  names.add("the %s event".formatted(eventInfo.getName().toLowerCase()));
               }
            }
         }
      }

      return StringUtils.join(names, ", ", " or ");
   }

   private static <T extends SyntaxElement> boolean checkExperimentalSyntax(T element) {
      if (element instanceof ExperimentalSyntax experimentalSyntax) {
         ExperimentSet experiments = getParser().getExperimentSet();
         return experimentalSyntax.isSatisfiedBy(experiments);
      } else {
         return true;
      }
   }

   @NotNull
   private static DefaultExpression<?> getDefaultExpression(SkriptParser.ExprInfo exprInfo, String pattern) {
      DefaultValueData data = getParser().getData(DefaultValueData.class);
      ClassInfo<?> classInfo = exprInfo.classes[0];
      DefaultExpression<?> expr = data.getDefaultValue(classInfo.getC());
      if (expr == null) {
         expr = classInfo.getDefaultExpression();
      }

      DefaultExpressionUtils.DefaultExpressionError errorType = DefaultExpressionUtils.isValid(expr, exprInfo, 0);
      if (errorType == null) {
         assert expr != null;

         return expr;
      } else {
         throw new SkriptAPIException(errorType.getError(List.of(classInfo.getCodeName()), pattern));
      }
   }

   @NotNull
   static List<DefaultExpression<?>> getDefaultExpressions(SkriptParser.ExprInfo exprInfo, String pattern) {
      if (exprInfo.classes.length == 1) {
         return new ArrayList<>(List.of(getDefaultExpression(exprInfo, pattern)));
      } else {
         DefaultValueData data = getParser().getData(DefaultValueData.class);
         EnumMap<DefaultExpressionUtils.DefaultExpressionError, List<String>> failed = new EnumMap<>(DefaultExpressionUtils.DefaultExpressionError.class);
         List<DefaultExpression<?>> passed = new ArrayList<>();

         for (int i = 0; i < exprInfo.classes.length; i++) {
            ClassInfo<?> classInfo = exprInfo.classes[i];
            DefaultExpression<?> expr = data.getDefaultValue(classInfo.getC());
            if (expr == null) {
               expr = classInfo.getDefaultExpression();
            }

            String codeName = classInfo.getCodeName();
            DefaultExpressionUtils.DefaultExpressionError errorType = DefaultExpressionUtils.isValid(expr, exprInfo, i);
            if (errorType != null) {
               failed.computeIfAbsent(errorType, list -> new ArrayList<>()).add(codeName);
            } else {
               passed.add(expr);
            }
         }

         if (!passed.isEmpty()) {
            return passed;
         } else {
            List<String> errors = new ArrayList<>();

            for (Entry<DefaultExpressionUtils.DefaultExpressionError, List<String>> entry : failed.entrySet()) {
               String error = entry.getKey().getError(entry.getValue(), pattern);
               errors.add(error);
            }

            throw new SkriptAPIException(StringUtils.join(errors, "\n"));
         }
      }
   }

   @Nullable
   private static <T> Variable<T> parseVariable(String expr, Class<? extends T>[] returnTypes) {
      if (!VARIABLE_PATTERN.matcher(expr).matches()) {
         return null;
      } else {
         String variableName = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(
            expr.substring(expr.indexOf(123) + 1, expr.lastIndexOf(125))
         );
         boolean inExpression = false;
         int variableDepth = 0;

         for (char character : variableName.toCharArray()) {
            if (character == '%' && variableDepth == 0) {
               inExpression = !inExpression;
            }

            if (inExpression) {
               if (character == '{') {
                  variableDepth++;
               } else if (character == '}') {
                  variableDepth--;
               }
            }

            if (!inExpression && (character == '{' || character == '}')) {
               return null;
            }
         }

         return Variable.newInstance(variableName, returnTypes);
      }
   }

   @Nullable
   private static Expression<?> parseExpression(Class<?>[] types, String expr) {
      if (!expr.startsWith("“") && !expr.startsWith("”") && !expr.endsWith("”") && !expr.endsWith("“")) {
         if (expr.startsWith("\"") && expr.length() != 1 && nextQuote(expr, 1) == expr.length() - 1) {
            return VariableString.newInstance(
               StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(expr.substring(1, expr.length() - 1))
            );
         } else {
            CheckedIterator<DefaultSyntaxInfos.Expression<?, ?>> iterator = new CheckedIterator<>(
               Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION).iterator(), info -> {
                  if (info != null && info.returnType() != Object.class) {
                     for (Class<?> returnType : types) {
                        assert returnType != null;

                        if (Converters.converterExists(info.returnType(), returnType)) {
                           return true;
                        }
                     }

                     return false;
                  } else {
                     return true;
                  }
               }
            );
            return parse(expr, iterator, null);
         }
      } else {
         Skript.error("Pretty quotes are not allowed, change to regular quotes (\")");
         return null;
      }
   }

   @Nullable
   private <T> Expression<? extends T> parseSingleExpr(boolean allowUnparsedLiteral, @Nullable LogEntry error, Class<? extends T>... types) {
      assert types.length > 0;

      assert types.length == 1 || !CollectionUtils.contains(types, Object.class);

      if (this.expr.isEmpty()) {
         return null;
      } else if (this.context != ParseContext.COMMAND
         && this.context != ParseContext.PARSE
         && this.expr.startsWith("(")
         && this.expr.endsWith(")")
         && next(this.expr, 0, this.context) == this.expr.length()) {
         return new SkriptParser(
               this, StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.expr.substring(1, this.expr.length() - 1))
            )
            .parseSingleExpr(allowUnparsedLiteral, error, types);
      } else {
         Expression<?> parsedExpression;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            if (this.context == ParseContext.DEFAULT || this.context == ParseContext.EVENT) {
               Variable<? extends T> parsedVariable = parseVariable(this.expr, types);
               if (parsedVariable != null) {
                  if ((this.flags & 1) == 0) {
                     Skript.error("Variables cannot be used here.");
                     log.printError();
                     return null;
                  }

                  log.printLog();
                  return parsedVariable;
               }

               if (log.hasError()) {
                  log.printError();
                  return null;
               }

               FunctionReference<T> functionReference = this.parseFunctionReference();
               if (functionReference != null) {
                  log.printLog();
                  return new ExprFunctionCall<>(functionReference, types);
               }

               if (log.hasError()) {
                  log.printError();
                  return null;
               }
            }

            log.clear();
            if ((this.flags & 1) != 0) {
               parsedExpression = parseExpression(types, this.expr);
               if (parsedExpression != null) {
                  Class<?> parsedReturnType = parsedExpression.getReturnType();

                  for (Class<? extends T> type : types) {
                     if (type.isAssignableFrom(parsedReturnType)) {
                        log.printLog();
                        return (Expression<? extends T>)parsedExpression;
                     }
                  }

                  Expression<? extends T> convertedExpression = parsedExpression.getConvertedExpression((Class<T>[])types);
                  if (convertedExpression != null) {
                     log.printLog();
                     return convertedExpression;
                  }

                  log.printError(parsedExpression.toString(null, false) + " " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
                  return null;
               }

               log.clear();
            }

            if ((this.flags & 2) == 0) {
               log.printError();
               return null;
            }

            parsedExpression = this.parseAsLiteral(allowUnparsedLiteral, log, error, types);
         }

         return (Expression<? extends T>)parsedExpression;
      }
   }

   @Nullable
   private Expression<?> parseSingleExpr(boolean allowUnparsedLiteral, @Nullable LogEntry error, SkriptParser.ExprInfo exprInfo) {
      if (this.expr.isEmpty()) {
         return null;
      } else if (this.context != ParseContext.COMMAND
         && this.context != ParseContext.PARSE
         && this.expr.startsWith("(")
         && this.expr.endsWith(")")
         && next(this.expr, 0, this.context) == this.expr.length()) {
         return new SkriptParser(
               this, StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.expr.substring(1, this.expr.length() - 1))
            )
            .parseSingleExpr(allowUnparsedLiteral, error, exprInfo);
      } else {
         Expression<?> parsedExpression;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            Class<?>[] types = new Class[exprInfo.classes.length];
            boolean hasSingular = false;
            boolean hasPlural = false;
            Class<?>[] nonNullTypes = new Class[exprInfo.classes.length];
            int nonNullIndex = 0;

            for (int i = 0; i < types.length; i++) {
               if ((this.flags & exprInfo.flagMask) != 0) {
                  if (exprInfo.isPlural[i]) {
                     hasPlural = true;
                  } else {
                     hasSingular = true;
                  }

                  types[i] = exprInfo.classes[i].getC();
                  nonNullTypes[nonNullIndex] = types[i];
                  nonNullIndex++;
               }
            }

            boolean onlyPlural = !hasSingular && hasPlural;
            boolean onlySingular = hasSingular && !hasPlural;
            if (this.context == ParseContext.DEFAULT || this.context == ParseContext.EVENT) {
               if (!onlySingular && !onlyPlural) {
                  Variable<?> parsedVariable = parseVariable(this.expr, types);
                  if (parsedVariable != null) {
                     if ((this.flags & 1) == 0) {
                        Skript.error("Variables cannot be used here.");
                        log.printError();
                        return null;
                     }

                     if ((exprInfo.classes.length == 1 && !exprInfo.isPlural[0] || Booleans.contains(exprInfo.isPlural, true)) && !parsedVariable.isSingle()) {
                        Skript.error(
                           "'"
                              + this.expr
                              + "' can only be a single "
                              + Classes.toString(Stream.of(exprInfo.classes).map(classInfo -> classInfo.getName().toString()).toArray(), false)
                              + ", not more."
                        );
                        log.printError();
                        return null;
                     }

                     log.printLog();
                     return parsedVariable;
                  }

                  if (log.hasError()) {
                     log.printError();
                     return null;
                  }
               } else {
                  Variable<?> parsedVariablex = parseVariable(this.expr, nonNullTypes);
                  if (parsedVariablex != null) {
                     if ((this.flags & 1) == 0) {
                        Skript.error("Variables cannot be used here.");
                        log.printError();
                        return null;
                     }

                     if (hasSingular && !parsedVariablex.isSingle()) {
                        Skript.error(
                           "'"
                              + this.expr
                              + "' can only be a single "
                              + Classes.toString(Stream.of(exprInfo.classes).map(classInfo -> classInfo.getName().toString()).toArray(), false)
                              + ", not more."
                        );
                        log.printError();
                        return null;
                     }

                     log.printLog();
                     return parsedVariablex;
                  }

                  if (log.hasError()) {
                     log.printError();
                     return null;
                  }
               }

               FunctionReference<?> functionReference = this.parseFunctionReference();
               if (functionReference != null) {
                  if (onlySingular && !functionReference.isSingle()) {
                     Skript.error(
                        "'"
                           + this.expr
                           + "' can only be a single "
                           + Classes.toString(Stream.of(exprInfo.classes).map(classInfo -> classInfo.getName().toString()).toArray(), false)
                           + ", not more."
                     );
                     log.printError();
                     return null;
                  }

                  log.printLog();
                  return new ExprFunctionCall<>(functionReference, types);
               }

               if (log.hasError()) {
                  log.printError();
                  return null;
               }
            }

            log.clear();
            if ((this.flags & 1) != 0) {
               parsedExpression = parseExpression(types, this.expr);
               if (parsedExpression != null) {
                  Class<?> parsedReturnType = parsedExpression.getReturnType();

                  for (int ix = 0; ix < types.length; ix++) {
                     Class<?> type = types[ix];
                     if (type != null && type.isAssignableFrom(parsedReturnType)) {
                        if (!exprInfo.isPlural[ix] && !parsedExpression.isSingle()) {
                           if (this.context == ParseContext.COMMAND) {
                              Skript.error(
                                 Commands.m_too_many_arguments
                                    .toString(exprInfo.classes[ix].getName().getIndefiniteArticle(), exprInfo.classes[ix].getName().toString()),
                                 ErrorQuality.SEMANTIC_ERROR
                              );
                           } else {
                              Skript.error(
                                 "'"
                                    + this.expr
                                    + "' can only be a single "
                                    + Classes.toString(Stream.of(exprInfo.classes).map(classInfo -> classInfo.getName().toString()).toArray(), false)
                                    + ", not more."
                              );
                           }

                           log.printError();
                           return null;
                        }

                        log.printLog();
                        return parsedExpression;
                     }
                  }

                  if (onlySingular && !parsedExpression.isSingle()) {
                     Skript.error(
                        "'"
                           + this.expr
                           + "' can only be a single "
                           + Classes.toString(Stream.of(exprInfo.classes).map(classInfo -> classInfo.getName().toString()).toArray(), false)
                           + ", not more."
                     );
                     log.printError();
                     return null;
                  }

                  Expression<?> convertedExpression = parsedExpression.getConvertedExpression(types);
                  if (convertedExpression != null) {
                     log.printLog();
                     return convertedExpression;
                  }

                  log.printError(parsedExpression.toString(null, false) + " " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
                  return null;
               }

               log.clear();
            }

            if ((this.flags & 2) == 0) {
               log.printError();
               return null;
            }

            parsedExpression = this.parseAsLiteral(allowUnparsedLiteral, log, error, nonNullTypes);
         }

         return parsedExpression;
      }
   }

   @SafeVarargs
   @Nullable
   private <T> Expression<? extends T> parseAsLiteral(boolean allowUnparsedLiteral, ParseLogHandler log, @Nullable LogEntry error, Class<? extends T>... types) {
      if (this.expr.endsWith(")") && this.expr.contains("(")) {
         Matcher classInfoMatcher = LITERAL_SPECIFICATION_PATTERN.matcher(this.expr);
         if (classInfoMatcher.matches()) {
            String literalString = classInfoMatcher.group("literal");
            String unparsedClassInfo = Noun.stripDefiniteArticle(classInfoMatcher.group("classinfo"));
            Expression<? extends T> result = this.parseSpecifiedLiteral(literalString, unparsedClassInfo, types);
            if (result != null) {
               log.printLog();
               return result;
            }
         }
      }

      if (types.length != 1 || types[0] != Object.class) {
         boolean containsObjectClass = false;

         for (Class<?> type : types) {
            log.clear();
            if (type == Object.class) {
               containsObjectClass = true;
            } else {
               T parsedObject = Classes.parse(this.expr, (Class<T>)type, this.context);
               if (parsedObject != null) {
                  log.printLog();
                  return new SimpleLiteral<>(parsedObject, false, new UnparsedLiteral(this.expr));
               }
            }
         }

         if (allowUnparsedLiteral && containsObjectClass) {
            return this.getUnparsedLiteral(log, error);
         } else {
            if (this.expr.startsWith("\"") && this.expr.endsWith("\"") && this.expr.length() > 1) {
               for (Class<?> typex : types) {
                  if (typex.isAssignableFrom(String.class)) {
                     VariableString string = VariableString.newInstance(this.expr.substring(1, this.expr.length() - 1));
                     if (string instanceof LiteralString) {
                        return string;
                     }
                     break;
                  }
               }
            }

            log.printError();
            return null;
         }
      } else if (!allowUnparsedLiteral) {
         log.printError();
         return null;
      } else {
         return this.getUnparsedLiteral(log, error);
      }
   }

   @Nullable
   private UnparsedLiteral getUnparsedLiteral(ParseLogHandler log, @Nullable LogEntry error) {
      if (Classes.parseSimple(this.expr, Object.class, this.context) == null) {
         log.printError();
         return null;
      } else {
         log.clear();
         LogEntry logError = log.getError();
         return new UnparsedLiteral(this.expr, logError == null || error != null && logError.quality <= error.quality ? error : logError);
      }
   }

   @SafeVarargs
   @Nullable
   private <T> Expression<? extends T> parseSpecifiedLiteral(String literalString, String unparsedClassInfo, Class<? extends T>... types) {
      ClassInfo<?> classInfo = Classes.parse(unparsedClassInfo, ClassInfo.class, this.context);
      if (classInfo == null) {
         Skript.error("A " + unparsedClassInfo + " is not a valid type.");
         return null;
      } else {
         Parser<?> classInfoParser = classInfo.getParser();
         if (classInfoParser != null && classInfoParser.canParse(this.context)) {
            if (!this.checkAcceptedType(classInfo.getC(), types)) {
               Skript.error(this.expr + " " + Language.get("is") + " " + notOfType(types));
               return null;
            } else {
               T parsedObject = (T)classInfoParser.parse(literalString, this.context);
               return parsedObject != null ? new SimpleLiteral<>(parsedObject, false, new UnparsedLiteral(literalString)) : null;
            }
         } else {
            Skript.error("A " + unparsedClassInfo + " cannot be parsed.");
            return null;
         }
      }
   }

   private boolean checkAcceptedType(Class<?> clazz, Class<?>... types) {
      for (Class<?> targetType : types) {
         if (targetType.isAssignableFrom(clazz)) {
            return true;
         }
      }

      return false;
   }

   @SafeVarargs
   @Nullable
   public final <T> Expression<? extends T> parseExpression(Class<? extends T>... types) {
      if (this.expr.isEmpty()) {
         return null;
      } else {
         assert types.length > 0;

         assert types.length == 1 || !CollectionUtils.contains(types, Object.class);

         ExpressionParseCache failedExprsCache = ParserInstance.get().getExpressionParseCache();
         failedExprsCache.push();

         Expression var5;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            Expression<? extends T> parsedExpression = this.parseSingleExpr(true, null, types);
            if (parsedExpression != null) {
               log.printLog();
               return parsedExpression;
            }

            log.clear();
            var5 = this.parseExpressionList(log, types);
         } finally {
            failedExprsCache.pop();
         }

         return var5;
      }
   }

   @Nullable
   public Expression<?> parseExpression(SkriptParser.ExprInfo exprInfo) {
      if (this.expr.isEmpty()) {
         return null;
      } else {
         ExpressionParseCache failedExprsCache = ParserInstance.get().getExpressionParseCache();
         failedExprsCache.push();

         Expression var5;
         try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            Expression<?> parsedExpression = this.parseSingleExpr(true, null, exprInfo);
            if (parsedExpression != null) {
               log.printLog();
               return parsedExpression;
            }

            log.clear();
            var5 = this.parseExpressionList(log, exprInfo);
         } finally {
            failedExprsCache.pop();
         }

         return var5;
      }
   }

   @SafeVarargs
   @Nullable
   private <T> Expression<? extends T> parseExpressionList(ParseLogHandler log, Class<? extends T>... types) {
      return (Expression<? extends T>)this.parseExpressionList_i(log, types);
   }

   @Nullable
   private Expression<?> parseExpressionList(ParseLogHandler log, SkriptParser.ExprInfo info) {
      return this.parseExpressionList_i(log, info);
   }

   @Nullable
   private Expression<?> parseExpressionList(ParseLogHandler log, SkriptParser.OrderedExprInfo info) {
      return this.parseExpressionList_i(log, info);
   }

   @Nullable
   private Expression<?> parseExpressionList_i(ParseLogHandler log, Object data) {
      SkriptParser.OrderedExprInfo orderedExprInfo = data instanceof SkriptParser.OrderedExprInfo info ? info : null;
      SkriptParser.ExprInfo exprInfo = data instanceof SkriptParser.ExprInfo infox ? infox : null;
      Class<?>[] types = orderedExprInfo == null && exprInfo == null ? (Class[])data : null;
      boolean isObject;
      if (orderedExprInfo != null) {
         isObject = orderedExprInfo.infos.length == 1 && orderedExprInfo.infos[0].classes[0].getC() == Object.class;
      } else if (exprInfo != null) {
         isObject = exprInfo.classes.length == 1 && exprInfo.classes[0].getC() == Object.class;
      } else {
         isObject = types.length == 1 && types[0] == Object.class;
      }

      List<int[]> pieces = new ArrayList<>();
      Matcher matcher = LIST_SPLIT_PATTERN.matcher(this.expr);
      int currentPosition = 0;

      for (int lastPosition = currentPosition;
         currentPosition >= 0 && currentPosition <= this.expr.length();
         currentPosition = next(this.expr, currentPosition, this.context)
      ) {
         if (currentPosition == this.expr.length() || matcher.region(currentPosition, this.expr.length()).lookingAt()) {
            pieces.add(new int[]{lastPosition, currentPosition});
            if (currentPosition == this.expr.length()) {
               break;
            }

            currentPosition = matcher.end();
            lastPosition = currentPosition;
         }
      }

      if (currentPosition != this.expr.length()) {
         assert currentPosition == -1 && this.context != ParseContext.COMMAND && this.context != ParseContext.PARSE : currentPosition + "; " + this.expr;

         log.printError("Invalid brackets/variables/text in '" + this.expr + "'", ErrorQuality.NOT_AN_EXPRESSION);
         return null;
      } else if (pieces.size() == 1) {
         if (this.expr.startsWith("(") && this.expr.endsWith(")") && next(this.expr, 0, this.context) == this.expr.length()) {
            log.clear();
            SkriptParser parser = new SkriptParser(this, this.expr.substring(1, this.expr.length() - 1));
            return exprInfo != null ? parser.parseExpression(exprInfo) : parser.parseExpression(types);
         } else if (isObject && (this.flags & 2) != 0) {
            log.clear();
            return new UnparsedLiteral(this.expr, log.getError());
         } else {
            log.printError();
            return null;
         }
      } else if (exprInfo != null && !Booleans.contains(exprInfo.isPlural, true) && !OR_PATTERN.matcher(this.expr).find()) {
         log.printError();
         return null;
      } else {
         List<Expression<?>> parsedExpressions = new ArrayList<>();
         boolean isLiteralList = true;
         Kleenean and = Kleenean.UNKNOWN;
         int first = 0;

         label293:
         while (first < pieces.size()) {
            for (int last = first; last < pieces.size(); last++) {
               if (first != 0 || last != pieces.size() - 1) {
                  int start = pieces.get(first)[0];
                  int end = pieces.get(last)[1];
                  String subExpr = this.expr.substring(start, end);
                  SkriptParser parser = new SkriptParser(this, subExpr);
                  Expression<?> parsedExpression;
                  if (subExpr.startsWith("(") && subExpr.endsWith(")") && next(subExpr, 0, this.context) == subExpr.length()) {
                     if (orderedExprInfo != null) {
                        int infoIndex = parsedExpressions.size();
                        if (infoIndex >= orderedExprInfo.infos.length) {
                           log.printError();
                           return null;
                        }

                        parsedExpression = parser.parseExpression(orderedExprInfo.infos[infoIndex]);
                     } else if (exprInfo != null) {
                        parsedExpression = parser.parseExpression(exprInfo);
                     } else {
                        parsedExpression = parser.parseExpression(types);
                     }
                  } else if (orderedExprInfo != null) {
                     int infoIndex = parsedExpressions.size();
                     if (infoIndex >= orderedExprInfo.infos.length) {
                        log.printError();
                        return null;
                     }

                     parsedExpression = parser.parseSingleExpr(last == first, log.getError(), orderedExprInfo.infos[infoIndex]);
                  } else if (exprInfo != null) {
                     parsedExpression = parser.parseSingleExpr(last == first, log.getError(), exprInfo);
                  } else {
                     parsedExpression = parser.parseSingleExpr(last == first, log.getError(), types);
                  }

                  if (parsedExpression != null) {
                     isLiteralList &= parsedExpression instanceof Literal;
                     parsedExpressions.add(parsedExpression);
                     if (first != 0) {
                        String delimiter = this.expr.substring(pieces.get(first - 1)[1], start).trim().toLowerCase(Locale.ENGLISH);
                        if (!delimiter.equals(",")) {
                           boolean or = !delimiter.endsWith("nor") && delimiter.endsWith("or");
                           if (and.isUnknown()) {
                              and = Kleenean.get(!or);
                           } else if (and != Kleenean.get(!or)) {
                              Skript.warning(
                                 "List has multiple 'and' or 'or', will default to 'and'. Use brackets if you want to define multiple lists. List: "
                                    + this.expr
                              );
                              and = Kleenean.TRUE;
                           }
                        }
                     }

                     first = last + 1;
                     continue label293;
                  }
               }
            }

            log.printError();
            return null;
         }

         Class<?>[] returnTypes;
         Class<?> superReturnType;
         if (parsedExpressions.size() == 1) {
            returnTypes = null;
            superReturnType = parsedExpressions.get(0).getReturnType();
         } else {
            returnTypes = new Class[parsedExpressions.size()];

            for (int i = 0; i < parsedExpressions.size(); i++) {
               returnTypes[i] = parsedExpressions.get(i).getReturnType();
            }

            superReturnType = Classes.getSuperClassInfo(returnTypes).getC();
         }

         if (exprInfo != null && !and.isFalse()) {
            boolean canBePlural = false;

            for (int typeIndex = 0; typeIndex < exprInfo.classes.length; typeIndex++) {
               if (exprInfo.isPlural[typeIndex] && exprInfo.classes[typeIndex].getC().isAssignableFrom(superReturnType)) {
                  canBePlural = true;
                  break;
               }
            }

            if (!canBePlural) {
               for (Expression<?> parsedExpressionx : parsedExpressions) {
                  canBePlural = false;

                  for (int typeIndexx = 0; typeIndexx < exprInfo.classes.length; typeIndexx++) {
                     if (exprInfo.isPlural[typeIndexx] && parsedExpressionx.canReturn(exprInfo.classes[typeIndexx].getC())) {
                        canBePlural = true;
                        break;
                     }
                  }

                  if (!canBePlural) {
                     break;
                  }
               }
            }

            if (!canBePlural) {
               log.printError();
               return null;
            }
         }

         if (returnTypes == null) {
            return parsedExpressions.get(0);
         } else {
            log.printLog(false);
            if (and.isUnknown() && !this.suppressMissingAndOrWarnings) {
               ParserInstance parserx = getParser();
               if (parserx.isActive() && !parserx.getCurrentScript().suppressesWarning(ScriptWarning.MISSING_CONJUNCTION)) {
                  Skript.warning("List is missing 'and' or 'or', defaulting to 'and': " + this.expr);
               }
            }

            if (isLiteralList) {
               Literal<?>[] literals = parsedExpressions.toArray(new Literal[0]);
               return new LiteralList<>(literals, superReturnType, returnTypes, !and.isFalse());
            } else {
               Expression<?>[] expressions = parsedExpressions.toArray(new Expression[0]);
               return new ExpressionList<>(expressions, superReturnType, returnTypes, !and.isFalse());
            }
         }
      }
   }

   public <T> FunctionReference<T> parseFunctionReference() {
      return this.context != ParseContext.DEFAULT && this.context != ParseContext.EVENT
         ? null
         : new FunctionReferenceParser(this.context, this.flags).parseFunctionReference(this.expr);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   @Nullable
   public <T> ch.njol.skript.lang.function.FunctionReference<T> parseFunction(@Nullable Class<? extends T>... types) {
      if (this.context != ParseContext.DEFAULT && this.context != ParseContext.EVENT) {
         return null;
      } else {
         FunctionReference<Object> newReference = new FunctionReferenceParser(this.context, this.flags).parseFunctionReference(this.expr);
         if (newReference == null) {
            return null;
         } else {
            Expression[] expressions = Arrays.stream(newReference.arguments()).map(FunctionReference.Argument::value).toArray(Expression[]::new);
            return new ch.njol.skript.lang.function.FunctionReference<>(newReference.name(), null, newReference.namespace(), types, expressions);
         }
      }
   }

   public static boolean parseArguments(String args, ScriptCommand command, ScriptCommandEvent event) {
      SkriptParser parser = new SkriptParser(args, 2, ParseContext.COMMAND);
      SkriptParser.ParseResult parseResult = parser.parse_i(command.getPattern());
      if (parseResult == null) {
         return false;
      } else {
         List<Argument<?>> arguments = command.getArguments();

         assert arguments.size() == parseResult.exprs.length;

         for (int i = 0; i < parseResult.exprs.length; i++) {
            if (parseResult.exprs[i] == null) {
               arguments.get(i).setToDefault(event);
            } else {
               arguments.get(i).set(event, parseResult.exprs[i].getArray(event));
            }
         }

         return true;
      }
   }

   @Nullable
   public static SkriptParser.ParseResult parse(String text, String pattern) {
      return new SkriptParser(text, 2, ParseContext.COMMAND).parse_i(pattern);
   }

   @Nullable
   public static SkriptParser.ParseResult parse(String text, String pattern, int parseFlags, ParseContext parseContext) {
      return new SkriptParser(text, parseFlags, parseContext).parse_i(pattern);
   }

   @Nullable
   public static SkriptParser.ParseResult parse(String text, SkriptPattern pattern, int parseFlags, ParseContext parseContext) {
      return parse(text, pattern.toString(), parseFlags, parseContext);
   }

   public static int nextBracket(String pattern, char closingBracket, char openingBracket, int start, boolean isGroup) throws MalformedPatternException {
      int index = 0;

      for (int i = start; i < pattern.length(); i++) {
         if (pattern.charAt(i) == '\\') {
            i++;
         } else if (pattern.charAt(i) == closingBracket) {
            if (index == 0) {
               if (!isGroup) {
                  throw new MalformedPatternException(pattern, "Unexpected closing bracket '" + closingBracket + "'");
               }

               return i;
            }

            index--;
         } else if (pattern.charAt(i) == openingBracket) {
            index++;
         }
      }

      if (isGroup) {
         throw new MalformedPatternException(pattern, "Missing closing bracket '" + closingBracket + "'");
      } else {
         return -1;
      }
   }

   private static int nextUnescaped(String pattern, char character, int from) {
      for (int i = from; i < pattern.length(); i++) {
         if (pattern.charAt(i) == '\\') {
            i++;
         } else if (pattern.charAt(i) == character) {
            return i;
         }
      }

      return -1;
   }

   static int countUnescaped(String haystack, char needle) {
      return countUnescaped(haystack, needle, 0, haystack.length());
   }

   static int countUnescaped(String haystack, char needle, int start, int end) {
      assert start >= 0 && start <= end && end <= haystack.length() : start + ", " + end + "; " + haystack.length();

      int count = 0;

      for (int i = start; i < end; i++) {
         char character = haystack.charAt(i);
         if (character == '\\') {
            i++;
         } else if (character == needle) {
            count++;
         }
      }

      return count;
   }

   private static int nextQuote(String string, int start) {
      boolean inExpression = false;
      int length = string.length();

      for (int i = start; i < length; i++) {
         char character = string.charAt(i);
         if (character == '"' && !inExpression) {
            if (i == length - 1 || string.charAt(i + 1) != '"') {
               return i;
            }

            i++;
         } else if (character == '%') {
            inExpression = !inExpression;
         }
      }

      return -1;
   }

   public static String notOfType(Class<?>... types) {
      if (types.length == 1) {
         Class<?> type = types[0];

         assert type != null;

         return Language.get("not") + " " + Classes.getSuperClassInfo(type).getName().withIndefiniteArticle();
      } else {
         StringBuilder message = new StringBuilder(Language.get("neither") + " ");

         for (int i = 0; i < types.length; i++) {
            if (i != 0) {
               if (i != types.length - 1) {
                  message.append(", ");
               } else {
                  message.append(" ").append(Language.get("nor")).append(" ");
               }
            }

            Class<?> c = types[i];

            assert c != null;

            ClassInfo<?> classInfo = Classes.getSuperClassInfo(c);
            if (classInfo != null) {
               message.append(classInfo.getName().withIndefiniteArticle());
            } else {
               message.append(c.getName());
            }
         }

         return message.toString();
      }
   }

   public static String notOfType(ClassInfo<?>... types) {
      if (types.length == 1) {
         return Language.get("not") + " " + types[0].getName().withIndefiniteArticle();
      } else {
         StringBuilder message = new StringBuilder(Language.get("neither") + " ");

         for (int i = 0; i < types.length; i++) {
            if (i != 0) {
               if (i != types.length - 1) {
                  message.append(", ");
               } else {
                  message.append(" ").append(Language.get("nor")).append(" ");
               }
            }

            message.append(types[i].getName().withIndefiniteArticle());
         }

         return message.toString();
      }
   }

   public static int next(String expr, int startIndex, ParseContext context) {
      if (startIndex < 0) {
         throw new StringIndexOutOfBoundsException(startIndex);
      } else {
         int exprLength = expr.length();
         if (startIndex >= exprLength) {
            return -1;
         } else if (context != ParseContext.COMMAND && context != ParseContext.PARSE) {
            return switch (expr.charAt(startIndex)) {
               case '"' -> {
                  int index = nextQuote(expr, startIndex + 1);
                  yield index < 0 ? -1 : index + 1;
               }
               case '(' -> {
                  for (int index = startIndex + 1; index >= 0 && index < exprLength; index = next(expr, index, context)) {
                     if (expr.charAt(index) == ')') {
                        yield index + 1;
                     }
                  }

                  yield -1;
               }
               case '{' -> {
                  int index = VariableString.nextVariableBracket(expr, startIndex + 1);
                  yield index < 0 ? -1 : index + 1;
               }
               default -> startIndex + 1;
            };
         } else {
            return startIndex + 1;
         }
      }
   }

   public static int nextOccurrence(String haystack, String needle, int startIndex, ParseContext parseContext, boolean caseSensitive) {
      if (startIndex < 0) {
         throw new StringIndexOutOfBoundsException(startIndex);
      } else if (parseContext != ParseContext.COMMAND && parseContext != ParseContext.PARSE) {
         int haystackLength = haystack.length();
         if (startIndex >= haystackLength) {
            return -1;
         } else {
            int needleLength = needle.length();
            char firstChar = needle.charAt(0);

            for (boolean startsWithSpecialChar = firstChar == '"' || firstChar == '{' || firstChar == '('; startIndex < haystackLength; startIndex++) {
               char character = haystack.charAt(startIndex);
               if (startsWithSpecialChar && haystack.regionMatches(!caseSensitive, startIndex, needle, 0, needleLength)) {
                  return startIndex;
               }

               switch (character) {
                  case '"':
                     startIndex = nextQuote(haystack, startIndex + 1);
                     if (startIndex < 0) {
                        return -1;
                     }
                     break;
                  case '(':
                     startIndex = next(haystack, startIndex, parseContext);
                     if (startIndex < 0) {
                        return -1;
                     }
                     break;
                  case '{':
                     startIndex = VariableString.nextVariableBracket(haystack, startIndex + 1);
                     if (startIndex < 0) {
                        return -1;
                     }
               }

               if (haystack.regionMatches(!caseSensitive, startIndex, needle, 0, needleLength)) {
                  return startIndex;
               }
            }

            return -1;
         }
      } else {
         return haystack.indexOf(needle, startIndex);
      }
   }

   @Nullable
   private SkriptParser.ParseResult parse_i(String pattern) {
      SkriptPattern skriptPattern = patterns.computeIfAbsent(pattern, PatternCompiler::compile);
      MatchResult matchResult = skriptPattern.match(this.expr, this.flags, this.context);
      return matchResult == null ? null : matchResult.toParseResult();
   }

   @Nullable
   public static NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> validatePattern(String pattern) {
      List<NonNullPair<ClassInfo<?>, Boolean>> pairs = new ArrayList<>();
      int groupLevel = 0;
      int optionalLevel = 0;
      Deque<Character> groups = new LinkedList<>();
      StringBuilder stringBuilder = new StringBuilder(pattern.length());
      int last = 0;

      for (int i = 0; i < pattern.length(); i++) {
         char character = pattern.charAt(i);
         if (character == '(') {
            groupLevel++;
            groups.addLast(character);
         } else if (character == '|') {
            if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|') {
               return error("Cannot use the pipe character '|' outside of groups. Escape it if you want to match a literal pipe: '\\|'");
            }

            groups.removeLast();
            groups.addLast(character);
         } else if (character == ')') {
            if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|') {
               return error("Unexpected closing group bracket ')'. Escape it if you want to match a literal bracket: '\\)'");
            }

            if (groups.peekLast() == '(') {
               return error(
                  "(...|...) groups have to contain at least one pipe character '|' to separate it into parts. Escape the brackets if you want to match literal brackets: \"\\(not a group\\)\""
               );
            }

            groupLevel--;
            groups.removeLast();
         } else if (character == '[') {
            optionalLevel++;
            groups.addLast(character);
         } else if (character == ']') {
            if (optionalLevel == 0 || groups.peekLast() != '[') {
               return error("Unexpected closing optional bracket ']'. Escape it if you want to match a literal bracket: '\\]'");
            }

            optionalLevel--;
            groups.removeLast();
         } else if (character == '<') {
            int j = pattern.indexOf(62, i + 1);
            if (j == -1) {
               return error("Missing closing regex bracket '>'. Escape the '<' if you want to match a literal bracket: '\\<'");
            }

            try {
               Pattern.compile(pattern.substring(i + 1, j));
            } catch (PatternSyntaxException var12) {
               return error("Invalid Regular Expression '" + pattern.substring(i + 1, j) + "': " + var12.getLocalizedMessage());
            }

            i = j;
         } else {
            if (character == '>') {
               return error("Unexpected closing regex bracket '>'. Escape it if you want to match a literal bracket: '\\>'");
            }

            if (character == '%') {
               int j = pattern.indexOf(37, i + 1);
               if (j == -1) {
                  return error("Missing end sign '%' of expression. Escape the percent sign to match a literal '%': '\\%'");
               }

               NonNullPair<String, Boolean> pair = Utils.getEnglishPlural(
                  StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(pattern.substring(i + 1, j))
               );
               ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(pair.getFirst());
               if (classInfo == null) {
                  return error(
                     "The type '"
                        + pair.getFirst()
                        + "' could not be found. Please check your spelling or escape the percent signs if you want to match literal %s: \"\\%not an expression\\%\""
                  );
               }

               pairs.add(new NonNullPair<>(classInfo, pair.getSecond()));
               stringBuilder.append(pattern, last, i + 1);
               stringBuilder.append(Utils.toEnglishPlural(classInfo.getCodeName(), pair.getSecond()));
               last = j;
               i = j;
            } else if (character == '\\') {
               if (i == pattern.length() - 1) {
                  return error("Pattern must not end in an unescaped backslash. Add another backslash to escape it, or remove it altogether.");
               }

               i++;
            }
         }
      }

      stringBuilder.append(pattern.substring(last));
      return new NonNullPair<>(stringBuilder.toString(), pairs.toArray(new NonNullPair[0]));
   }

   @Nullable
   private static NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> error(String error) {
      Skript.error("Invalid pattern: " + error);
      return null;
   }

   public static boolean validateLine(String line) {
      if (StringUtils.count(line, '"') % 2 != 0) {
         Skript.error(M_QUOTES_ERROR.toString());
         return false;
      } else {
         for (int i = 0; i < line.length(); i = next(line, i, ParseContext.DEFAULT)) {
            if (i == -1) {
               Skript.error(M_BRACKETS_ERROR.toString());
               return false;
            }
         }

         return true;
      }
   }

   private static ParserInstance getParser() {
      return ParserInstance.get();
   }

   static {
      ParserInstance.registerData(DefaultValueData.class, DefaultValueData::new);
   }

   public static class ExprInfo {
      public final ClassInfo<?>[] classes;
      public final boolean[] isPlural;
      public boolean isOptional;
      public int flagMask = -1;
      public int time = 0;

      public ExprInfo(int length) {
         this(new ClassInfo[length], new boolean[length]);
      }

      public ExprInfo(ClassInfo<?>[] classes, boolean[] isPlural) {
         Preconditions.checkState(classes.length == isPlural.length, "classes and isPlural must be the same length");
         this.classes = classes;
         this.isPlural = isPlural;
      }
   }

   private record OrderedExprInfo(SkriptParser.ExprInfo[] infos) {
   }

   public static class ParseResult {
      @Nullable
      public SkriptPattern source;
      public Expression<?>[] exprs;
      public List<java.util.regex.MatchResult> regexes = new ArrayList<>(1);
      public String expr;
      public int mark = 0;
      public List<String> tags = new ArrayList<>();

      public ParseResult(SkriptParser parser, String pattern) {
         this.expr = parser.expr;
         this.exprs = new Expression[SkriptParser.countUnescaped(pattern, '%') / 2];
      }

      public ParseResult(String expr, Expression<?>[] expressions) {
         this.expr = expr;
         this.exprs = expressions;
      }

      public boolean hasTag(String tag) {
         return this.tags.contains(tag);
      }
   }
}
