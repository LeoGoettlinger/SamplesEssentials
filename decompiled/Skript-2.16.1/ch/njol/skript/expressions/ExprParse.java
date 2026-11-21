package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.patterns.MalformedPatternException;
import ch.njol.skript.patterns.MatchResult;
import ch.njol.skript.patterns.PatternCompiler;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Parse")
@Description({"Parses text as a given type, or as a given pattern.", "This expression can be used in two different ways: One which parses the entire text as a single instance of a type, e.g. as a number, and one that parses the text according to a pattern.", "If the given text could not be parsed, this expression will return nothing and the <a href='#ExprParseError'>parse error</a> will be set if some information is available.", "Some notes about parsing with a pattern:", "- The pattern must be a <a href='./patterns/'>Skript pattern</a>, e.g. percent signs are used to define where to parse which types, e.g. put a %number% or %items% in the pattern if you expect a number or some items there.", "- You <i>have to</i> save the expression's value in a list variable, e.g. <code>set {parsed::*} to message parsed as \"...\"</code>.", "- The list variable will contain the parsed values from all %types% in the pattern in order. If a type was plural, e.g. %items%, the variable's value at the respective index will be a list variable, e.g. the values will be stored in {parsed::1::*}, not {parsed::1}."})
@Example.Examples({@Example("set {var} to line 1 parsed as number"), @Example("on chat:\n\tset {var::*} to message parsed as \"buying %items% for %money%\"\n\tif parse error is set:\n\t\tmessage \"%parse error%\"\n\telse if {var::*} is set:\n\t\tcancel event\n\t\tremove {var::2} from the player's balance\n\t\tgive {var::1::*} to the player\n")})
@Since({"2.0"})
public class ExprParse extends SimpleExpression<Object> {
   @Nullable
   static String lastError = null;
   private Expression<String> text;
   @Nullable
   private SkriptPattern pattern;
   @Nullable
   private NonNullPair<ClassInfo<?>, Boolean>[] patternExpressions;
   private boolean single = true;
   public boolean flatten = true;
   @Nullable
   private ClassInfo<?> classInfo;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.text = (Expression<String>)exprs[0];
      if (exprs[1] == null) {
         String pattern = ChatColor.translateAlternateColorCodes('&', parseResult.regexes.get(0).group());
         if (!VariableString.isQuotedCorrectly(pattern, false)) {
            Skript.error("Invalid amount and/or placement of double quotes in '" + pattern + "'");
            return false;
         } else {
            NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> p = SkriptParser.validatePattern(pattern);
            if (p == null) {
               return false;
            } else {
               pattern = p.getFirst();
               this.patternExpressions = p.getSecond();

               for (NonNullPair<ClassInfo<?>, Boolean> patternExpression : this.patternExpressions) {
                  if (!canParse(patternExpression.getFirst())) {
                     return false;
                  }

                  if (patternExpression.getSecond()) {
                     this.single = false;
                  }
               }

               pattern = escapeParseTags(pattern);

               try {
                  this.pattern = PatternCompiler.compile(pattern);
               } catch (MalformedPatternException var11) {
                  Skript.error("Malformed pattern: " + var11.getMessage());
                  return false;
               }

               if (this.single) {
                  this.single = this.pattern.countNonNullTypes() <= 1;
               }

               return true;
            }
         }
      } else {
         this.classInfo = (ClassInfo<?>)((Literal)exprs[1]).getSingle();
         if (this.classInfo.getC() == String.class) {
            Skript.error("Parsing as text is useless as only things that are already text may be parsed");
            return false;
         } else {
            return canParse(this.classInfo);
         }
      }
   }

   @Nullable
   @Override
   protected Object[] get(Event event) {
      String text = this.text.getSingle(event);
      if (text == null) {
         return null;
      } else {
         ParseLogHandler parseLogHandler = SkriptLogger.startParseLogHandler();

         try {
            lastError = null;
            if (this.classInfo != null) {
               Parser<?> parser = this.classInfo.getParser();

               assert parser != null;

               Object value = parser.parse(text, ParseContext.PARSE);
               if (value != null) {
                  Object[] valueArray = (Object[])Array.newInstance(this.classInfo.getC(), 1);
                  valueArray[0] = value;
                  return valueArray;
               }
            } else {
               assert this.pattern != null && this.patternExpressions != null;

               MatchResult matchResult = this.pattern.match(text, 2, ParseContext.PARSE);
               if (matchResult != null) {
                  Expression<?>[] exprs = matchResult.getExpressions();

                  assert this.patternExpressions.length == exprs.length;

                  if (!this.flatten) {
                     int nonNullExprCount = 0;

                     for (Expression<?> expr : exprs) {
                        if (expr != null) {
                           nonNullExprCount++;
                        }
                     }

                     Object[] values = new Object[nonNullExprCount];
                     int valueIndex = 0;

                     for (int i = 0; i < exprs.length; i++) {
                        if (exprs[i] != null) {
                           values[valueIndex] = this.patternExpressions[i].getSecond() ? exprs[i].getArray(null) : exprs[i].getSingle(null);
                           valueIndex++;
                        }
                     }

                     return values;
                  }

                  List<Object> values = new ArrayList<>();

                  for (int ix = 0; ix < exprs.length; ix++) {
                     if (exprs[ix] != null) {
                        if (this.patternExpressions[ix].getSecond()) {
                           values.addAll(Arrays.asList(exprs[ix].getArray(null)));
                        } else {
                           values.add(exprs[ix].getSingle(null));
                        }
                     }
                  }

                  return values.toArray();
               }
            }

            LogEntry error = parseLogHandler.getError();
            if (error != null) {
               lastError = error.toString();
            } else if (this.classInfo != null) {
               lastError = text + " could not be parsed as " + this.classInfo.getName().withIndefiniteArticle();
            } else {
               lastError = text + " could not be parsed as \"" + this.pattern + "\"";
            }

            return null;
         } finally {
            parseLogHandler.clear();
            parseLogHandler.printLog();
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.single;
   }

   @Override
   public Class<?> getReturnType() {
      if (this.classInfo != null) {
         return this.classInfo.getC();
      } else {
         return this.patternExpressions.length == 1 ? this.patternExpressions[0].getFirst().getC() : Object.class;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.text.toString(event, debug) + " parsed as " + (this.classInfo != null ? this.classInfo.toString(4) : this.pattern);
   }

   private static boolean canParse(ClassInfo<?> classInfo) {
      Parser<?> parser = classInfo.getParser();
      if (parser != null && parser.canParse(ParseContext.PARSE)) {
         return true;
      } else {
         Skript.error("Text cannot be parsed as " + classInfo.getName().withIndefiniteArticle());
         return false;
      }
   }

   private static String escapeParseTags(String pattern) {
      StringBuilder b = new StringBuilder(pattern.length());

      for (int i = 0; i < pattern.length(); i++) {
         char c = pattern.charAt(i);
         if (c == '\\') {
            b.append(c);
            b.append(pattern.charAt(i + 1));
            i++;
         } else if (c != 166 && c != ':') {
            b.append(c);
         } else {
            b.append("\\");
            b.append(c);
         }
      }

      return b.toString();
   }

   static {
      Skript.registerExpression(ExprParse.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "%string% parsed as (%-*classinfo%|\"<.*>\")");
   }
}
