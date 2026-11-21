package org.skriptlang.skript.registration;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.jetbrains.annotations.Nullable;

final class SyntaxRegister<I extends SyntaxInfo<?>> {
   private static final Comparator<SyntaxInfo<?>> SET_COMPARATOR = (a, b) -> {
      if (a == b) {
         return 0;
      } else {
         int priorityResult = a.priority().compareTo(b.priority());
         if (priorityResult != 0) {
            return priorityResult;
         } else if (isSkriptSyntax(a) && !isSkriptSyntax(b)) {
            return -1;
         } else if (!isSkriptSyntax(a) && isSkriptSyntax(b)) {
            return 1;
         } else {
            int scoreResult = Integer.compare(calculateComplexityScore(a), calculateComplexityScore(b));
            return scoreResult != 0 ? scoreResult : Integer.compare(a.hashCode(), b.hashCode());
         }
      }
   };
   final Set<I> syntaxes = new ConcurrentSkipListSet<>(SET_COMPARATOR);
   @Nullable
   private volatile Set<I> cache = null;

   private static boolean isSkriptSyntax(SyntaxInfo<?> info) {
      String originName = info.origin().name();
      return originName.equals("Skript") || originName.startsWith("org.skriptlang.skript");
   }

   private static int calculateComplexityScore(SyntaxInfo<?> info) {
      return info.patterns().stream().mapToInt(SyntaxRegister::calculateComplexityScore).max().orElseThrow();
   }

   private static int calculateComplexityScore(String pattern) {
      int score = 0;
      char[] chars = pattern.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         if (chars[i] == '%') {
            if ((i - 2 < 0 || chars[i - 2] != '%') && (i - 3 < 0 || chars[i - 3] != '%')) {
               score++;
            } else {
               score += 5;
            }
         }
      }

      return score;
   }

   public Collection<I> syntaxes() {
      if (this.cache == null) {
         this.cache = ImmutableSet.copyOf(this.syntaxes);
      }

      return this.cache;
   }

   public void add(I info) {
      this.syntaxes.add(info);
      this.cache = null;
   }

   public void remove(I info) {
      this.syntaxes.remove(info);
      this.cache = null;
   }
}
