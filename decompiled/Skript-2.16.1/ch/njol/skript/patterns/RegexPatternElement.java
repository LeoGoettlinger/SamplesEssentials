package ch.njol.skript.patterns;

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class RegexPatternElement extends PatternElement {
   private final Pattern pattern;

   public RegexPatternElement(Pattern pattern) {
      this.pattern = pattern;
   }

   @Nullable
   @Override
   public MatchResult match(String expr, MatchResult matchResult) {
      int exprIndex = matchResult.exprOffset;

      Object var12;
      try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
         Matcher matcher = this.pattern.matcher(expr);

         for (int nextExprOffset = SkriptParser.next(expr, exprIndex, matchResult.parseContext);
            nextExprOffset != -1;
            nextExprOffset = SkriptParser.next(expr, nextExprOffset, matchResult.parseContext)
         ) {
            log.clear();
            matcher.region(exprIndex, nextExprOffset);
            if (matcher.matches()) {
               MatchResult matchResultCopy = matchResult.copy();
               matchResultCopy.exprOffset = nextExprOffset;
               MatchResult newMatchResult = this.matchNext(expr, matchResultCopy);
               if (newMatchResult != null) {
                  newMatchResult.regexResults.addFirst(matcher.toMatchResult());
                  log.printLog();
                  return newMatchResult;
               }
            }
         }

         log.printError(null);
         var12 = null;
      }

      return (MatchResult)var12;
   }

   @Override
   public String toString() {
      return this.toString(SkriptPattern.StringificationProperties.DEFAULT);
   }

   @Override
   public String toString(SkriptPattern.StringificationProperties properties) {
      return "<" + this.pattern + ">";
   }

   @Override
   public Set<String> getCombinations(boolean clean) {
      return new HashSet<>(Set.of(this.toString()));
   }
}
