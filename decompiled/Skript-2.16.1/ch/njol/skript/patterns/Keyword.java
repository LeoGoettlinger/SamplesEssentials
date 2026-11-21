package ch.njol.skript.patterns;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;

abstract class Keyword {
   abstract boolean isPresent(String var1);

   public static int computeMinLength(PatternElement first) {
      int length = 0;

      for (PatternElement next = first; next != null; next = next.originalNext) {
         switch (next) {
            case LiteralPatternElement ignored:
               String literal = next.toString();

               for (int i = 0; i < literal.length(); i++) {
                  if (literal.charAt(i) != ' ') {
                     length++;
                  }
               }
               break;
            case ChoicePatternElement choicePatternElement:
               int min = Integer.MAX_VALUE;

               for (PatternElement choice : choicePatternElement.getPatternElements()) {
                  int choiceLen = computeMinLength(choice);
                  if (choiceLen < min) {
                     min = choiceLen;
                  }
               }

               if (min != Integer.MAX_VALUE) {
                  length += min;
               }
               break;
            case GroupPatternElement groupPatternElement:
               length += computeMinLength(groupPatternElement.getPatternElement());
               break;
            default:
         }
      }

      return length;
   }

   @Contract("_ -> new")
   public static Keyword[] buildKeywords(PatternElement first) {
      return buildKeywords(first, true, 0);
   }

   @Contract("_, _, _ -> new")
   private static Keyword[] buildKeywords(PatternElement first, boolean starting, int depth) {
      List<Keyword> keywords = new ArrayList<>();

      for (PatternElement next = first; next != null; next = next.originalNext) {
         Objects.requireNonNull(next);
         switch (next) {
            case LiteralPatternElement ignored:
               String literal = next.toString().trim();

               while (literal.contains("  ")) {
                  literal = literal.replace("  ", " ");
               }

               if (!literal.isEmpty()) {
                  keywords.add(new Keyword.SimpleKeyword(literal, starting, next.next == null));
               }
               break;
            case ChoicePatternElement choicePatternElement when depth <= 1:
               boolean finalStarting = starting;
               Set<Set<Keyword>> choices = choicePatternElement.getPatternElements()
                  .stream()
                  .map(element -> buildKeywords(element, finalStarting, depth))
                  .<Set<Keyword>>map(ImmutableSet::copyOf)
                  .collect(Collectors.toSet());
               if (choices.stream().noneMatch(Collection::isEmpty)) {
                  keywords.add(new Keyword.ChoiceKeyword(choices));
               }
               break;
            case GroupPatternElement groupPatternElement:
               Collections.addAll(keywords, buildKeywords(groupPatternElement.getPatternElement(), starting, depth + 1));
               break;
            default:
         }

         if (!(next instanceof ParseTagPatternElement)) {
            starting = false;
         }
      }

      return keywords.toArray(new Keyword[0]);
   }

   private static final class ChoiceKeyword extends Keyword {
      private final Set<Set<Keyword>> choices;

      ChoiceKeyword(Set<Set<Keyword>> choices) {
         this.choices = choices;
      }

      @Override
      public boolean isPresent(String expr) {
         return this.choices.stream().anyMatch(keywords -> keywords.stream().allMatch(keyword -> keyword.isPresent(expr)));
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.choices.toArray());
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return obj instanceof Keyword.ChoiceKeyword choiceKeyword ? this.choices.equals(choiceKeyword.choices) : false;
         }
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this).add("choices", this.choices.stream().map(Object::toString).collect(Collectors.joining(", "))).toString();
      }
   }

   private static final class SimpleKeyword extends Keyword {
      private final String keyword;
      private final boolean starting;
      private final boolean ending;

      SimpleKeyword(String keyword, boolean starting, boolean ending) {
         this.keyword = keyword;
         this.starting = starting;
         this.ending = ending;
      }

      @Override
      public boolean isPresent(String expr) {
         if (this.starting) {
            return expr.startsWith(this.keyword);
         } else {
            return this.ending ? expr.endsWith(this.keyword) : expr.contains(this.keyword);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.keyword, this.starting, this.ending);
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return !(obj instanceof Keyword.SimpleKeyword simpleKeyword)
               ? false
               : this.keyword.equals(simpleKeyword.keyword) && this.starting == simpleKeyword.starting && this.ending == simpleKeyword.ending;
         }
      }

      @Override
      public String toString() {
         return MoreObjects.toStringHelper(this).add("keyword", this.keyword).add("starting", this.starting).add("ending", this.ending).toString();
      }
   }
}
