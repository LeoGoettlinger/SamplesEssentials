package ch.njol.skript.patterns;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class LiteralPatternElement extends PatternElement {
   private final char[] literal;

   public LiteralPatternElement(String literal) {
      this.literal = literal.toLowerCase(Locale.ENGLISH).toCharArray();
   }

   public boolean isEmpty() {
      return this.literal.length == 0;
   }

   @Nullable
   @Override
   public MatchResult match(String expr, MatchResult matchResult) {
      char[] exprChars = expr.toCharArray();
      int exprIndex = matchResult.exprOffset;

      for (char c : this.literal) {
         if (c == ' ') {
            if (exprIndex != 0 && exprIndex != exprChars.length) {
               if (exprChars[exprIndex] == ' ') {
                  exprIndex++;
               } else if (exprChars[exprIndex - 1] != ' ') {
                  return null;
               }
            }
         } else {
            if (exprIndex == exprChars.length || Character.toLowerCase(c) != Character.toLowerCase(exprChars[exprIndex])) {
               return null;
            }

            exprIndex++;
         }
      }

      matchResult.exprOffset = exprIndex;
      return this.matchNext(expr, matchResult);
   }

   @Override
   public String toString() {
      return this.toString(SkriptPattern.StringificationProperties.DEFAULT);
   }

   @Override
   public String toString(SkriptPattern.StringificationProperties properties) {
      return new String(this.literal);
   }

   @Override
   public Set<String> getCombinations(boolean clean) {
      return new HashSet<>(Set.of(this.toString()));
   }
}
