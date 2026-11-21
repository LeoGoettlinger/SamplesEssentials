package ch.njol.skript.patterns;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.lang.parser.ExpressionParseCache;
import ch.njol.skript.lang.parser.LiteralParseCache;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;

public class TypePatternElement extends PatternElement {
   private final ClassInfo<?>[] classes;
   private final boolean[] isPlural;
   private final boolean isNullable;
   private final int flagMask;
   private final int time;
   private final int expressionIndex;

   public TypePatternElement(ClassInfo<?>[] classes, boolean[] isPlural, boolean isNullable, int flagMask, int time, int expressionIndex) {
      this.classes = classes;
      this.isPlural = isPlural;
      this.isNullable = isNullable;
      this.flagMask = flagMask;
      this.time = time;
      this.expressionIndex = expressionIndex;
   }

   public static TypePatternElement fromString(String string, int expressionIndex) {
      int caret = 0;
      int flagMask = -1;
      boolean isNullable = false;

      while (true) {
         switch (string.charAt(caret)) {
            case '*':
               flagMask &= -2;
               break;
            case '-':
               isNullable = true;
               break;
            case '~':
               flagMask &= -3;
               break;
            default:
               int time = 0;
               int timeStart = string.indexOf(64, caret);
               if (timeStart != -1) {
                  time = Integer.parseInt(string.substring(timeStart + 1));
                  string = string.substring(0, timeStart);
               } else {
                  string = string.substring(caret);
               }

               String[] classes = string.split("/");
               ClassInfo<?>[] classInfos = new ClassInfo[classes.length];
               boolean[] isPlural = new boolean[classes.length];

               for (int i = 0; i < classes.length; i++) {
                  Utils.PluralResult p = Utils.isPlural(classes[i]);
                  classInfos[i] = Classes.getClassInfo(p.updated());
                  isPlural[i] = p.plural();
               }

               return new TypePatternElement(classInfos, isPlural, isNullable, flagMask, time, expressionIndex);
         }

         caret++;
      }
   }

   @Nullable
   @Override
   public MatchResult match(String expr, MatchResult matchResult) {
      TypePatternElement.OffsetState state = new TypePatternElement.OffsetState();
      int exprOffset = this.initOffset(expr, matchResult, state);
      if (exprOffset == -1) {
         return null;
      } else {
         SkriptParser.ExprInfo exprInfo = this.getExprInfo();
         ExpressionParseCache parseCache = ParserInstance.get().getExpressionParseCache();
         MatchResult matchBackup = null;
         ParseLogHandler loopLogBackup = null;
         ParseLogHandler exprLogBackup = null;
         ParseLogHandler loopLog = SkriptLogger.startParseLogHandler();

         try {
            while (exprOffset != -1) {
               loopLog.clear();
               String substring = expr.substring(matchResult.exprOffset, exprOffset);
               int effectiveFlags = matchResult.flags & this.flagMask;
               ExpressionParseCache.Failure cacheKey = new ExpressionParseCache.Failure(
                  substring, effectiveFlags, this.classes, this.isPlural, this.isNullable, this.time
               );
               if (parseCache.contains(cacheKey)) {
                  exprOffset = this.advanceOffset(expr, exprOffset, matchResult.parseContext, state);
               } else {
                  MatchResult copy = matchResult.copy();
                  copy.exprOffset = exprOffset;
                  MatchResult tailMatch = this.matchNext(expr, copy);
                  if (tailMatch == null) {
                     exprOffset = this.advanceOffset(expr, exprOffset, matchResult.parseContext, state);
                  } else {
                     ParseLogHandler exprLog = SkriptLogger.startParseLogHandler();

                     try {
                        Expression<?> expression = new SkriptParser(substring, effectiveFlags, matchResult.parseContext).parseExpression(exprInfo);
                        if (expression == null) {
                           parseCache.add(cacheKey);
                           exprOffset = this.advanceOffset(expr, exprOffset, matchResult.parseContext, state);
                           continue;
                        }

                        if (!this.applyTimeState(expression)) {
                           return null;
                        }

                        tailMatch.expressions[this.expressionIndex] = expression;
                        if (!this.hasUnparsedLiterals(tailMatch)) {
                           exprLog.printLog();
                           loopLog.printLog();
                           return tailMatch;
                        }

                        if (matchBackup == null) {
                           matchBackup = tailMatch;
                           loopLogBackup = loopLog.backup();
                           exprLogBackup = exprLog.backup();
                        }
                     } finally {
                        if (!exprLog.isStopped()) {
                           exprLog.printError();
                        }
                     }

                     exprOffset = this.advanceOffset(expr, exprOffset, matchResult.parseContext, state);
                  }
               }
            }

            return matchBackup;
         } finally {
            if (loopLogBackup != null) {
               loopLog.restore(loopLogBackup);

               assert exprLogBackup != null;

               exprLogBackup.printLog();
            }

            if (!loopLog.isStopped()) {
               loopLog.printError();
            }
         }
      }
   }

   private boolean applyTimeState(Expression<?> expression) {
      if (this.time == 0) {
         return true;
      } else if (expression instanceof Literal) {
         return false;
      } else if (ParserInstance.get().getHasDelayBefore() == Kleenean.TRUE) {
         Skript.error("Cannot use time states after the event has already passed");
         return false;
      } else if (!expression.setTime(this.time)) {
         Skript.error(expression + " does not have a " + (this.time == EventValue.Time.PAST.value() ? "past" : "future") + " state");
         return false;
      } else {
         return true;
      }
   }

   private boolean hasUnparsedLiterals(@NotNull MatchResult matchResult) {
      LiteralParseCache literalCache = ParserInstance.get().getLiteralParseCache();

      for (int i = this.expressionIndex + 1; i < matchResult.expressions.length; i++) {
         if (matchResult.expressions[i] instanceof UnparsedLiteral unparsed) {
            LiteralParseCache.Failure key = new LiteralParseCache.Failure(unparsed.getData(), matchResult.parseContext);
            if (literalCache.contains(key)) {
               return true;
            }

            if (Classes.parse(unparsed.getData(), Object.class, matchResult.parseContext) == null) {
               literalCache.add(key);
               return true;
            }
         }
      }

      return false;
   }

   private int initOffset(String expr, MatchResult matchResult, TypePatternElement.OffsetState state) {
      if (this.next == null) {
         return expr.length();
      } else if (!(this.next instanceof LiteralPatternElement)) {
         state.nextLiteral = null;
         return SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
      } else {
         state.nextLiteral = this.next.toString();
         state.nextLiteralIsWhitespace = state.nextLiteral.trim().isEmpty();
         if (!state.nextLiteralIsWhitespace) {
            state.nextLiteral = state.nextLiteral.stripTrailing();
         }

         int offset = SkriptParser.nextOccurrence(expr, state.nextLiteral, matchResult.exprOffset, matchResult.parseContext, false);
         if (offset == -1 && state.nextLiteralIsWhitespace) {
            state.nextLiteral = null;
            offset = SkriptParser.next(expr, matchResult.exprOffset, matchResult.parseContext);
         }

         return offset;
      }
   }

   private int advanceOffset(String expr, int currentOffset, ParseContext parseContext, TypePatternElement.OffsetState state) {
      if (state.nextLiteral == null) {
         return SkriptParser.next(expr, currentOffset, parseContext);
      } else {
         int newOffset = SkriptParser.nextOccurrence(expr, state.nextLiteral, currentOffset + 1, parseContext, false);
         if (newOffset == -1 && state.nextLiteralIsWhitespace) {
            state.nextLiteral = null;
            return SkriptParser.next(expr, currentOffset, parseContext);
         } else {
            return newOffset;
         }
      }
   }

   @Override
   public String toString() {
      return this.toString(SkriptPattern.StringificationProperties.DEFAULT);
   }

   @Override
   public String toString(SkriptPattern.StringificationProperties properties) {
      StringBuilder stringBuilder = new StringBuilder().append("%");
      if (!properties.excludeTypeFlags()) {
         if (this.isNullable) {
            stringBuilder.append("-");
         }

         if (this.flagMask != -1) {
            if ((this.flagMask & 2) == 0) {
               stringBuilder.append("~");
            } else if ((this.flagMask & 1) == 0) {
               stringBuilder.append("*");
            }
         }
      }

      for (int i = 0; i < this.classes.length; i++) {
         String codeName = this.classes[i].getCodeName();
         if (this.isPlural[i]) {
            stringBuilder.append(Utils.toEnglishPlural(codeName));
         } else {
            stringBuilder.append(codeName);
         }

         if (i != this.classes.length - 1) {
            stringBuilder.append("/");
         }
      }

      if (!properties.excludeTypeFlags() && this.time != 0) {
         stringBuilder.append("@").append(this.time);
      }

      return stringBuilder.append("%").toString();
   }

   public SkriptParser.ExprInfo getExprInfo() {
      SkriptParser.ExprInfo exprInfo = new SkriptParser.ExprInfo(this.classes.length);

      for (int i = 0; i < this.classes.length; i++) {
         exprInfo.classes[i] = this.classes[i];
         exprInfo.isPlural[i] = this.isPlural[i];
      }

      exprInfo.isOptional = this.isNullable;
      exprInfo.flagMask = this.flagMask;
      exprInfo.time = this.time;
      return exprInfo;
   }

   @Override
   public Set<String> getCombinations(boolean clean) {
      Set<String> combinations = new HashSet<>();
      if (clean && this.flagMask != 2) {
         combinations.add("%*%");
      } else {
         combinations.add(this.toString());
      }

      return combinations;
   }

   private static final class OffsetState {
      @Nullable
      String nextLiteral;
      boolean nextLiteralIsWhitespace;
   }
}
