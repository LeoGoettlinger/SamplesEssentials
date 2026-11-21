package ch.njol.skript.patterns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParseTagPatternElement extends PatternElement {
   @Nullable
   private String tag;
   private final int mark;

   public ParseTagPatternElement(int mark) {
      this.tag = null;
      this.mark = mark;
   }

   public ParseTagPatternElement(@NotNull String tag) {
      this.tag = tag;
      int mark = 0;

      try {
         mark = Integer.parseInt(tag);
      } catch (NumberFormatException var4) {
      }

      this.mark = mark;
   }

   @Override
   void setNext(@Nullable PatternElement next) {
      if (this.tag != null && this.tag.isEmpty()) {
         if (next instanceof LiteralPatternElement) {
            this.tag = next.toString().trim();
         } else {
            PatternElement inner = null;
            if (next instanceof GroupPatternElement) {
               inner = ((GroupPatternElement)next).getPatternElement();
            } else if (next instanceof OptionalPatternElement) {
               inner = ((OptionalPatternElement)next).getPatternElement();
            }

            if (inner instanceof ChoicePatternElement choicePatternElement) {
               List<PatternElement> patternElements = choicePatternElement.getPatternElements();

               for (int i = 0; i < patternElements.size(); i++) {
                  PatternElement patternElement = patternElements.get(i);
                  if (patternElement instanceof LiteralPatternElement && !patternElement.toString().isEmpty()) {
                     ParseTagPatternElement newTag = new ParseTagPatternElement(patternElement.toString().trim());
                     newTag.setNext(patternElement);
                     newTag.originalNext = patternElement;
                     patternElements.set(i, newTag);
                  }
               }
            }
         }
      }

      super.setNext(next);
   }

   @Nullable
   @Override
   public MatchResult match(String expr, MatchResult matchResult) {
      if (this.tag != null && !this.tag.isEmpty()) {
         matchResult.tags.add(this.tag);
      }

      matchResult.mark = matchResult.mark ^ this.mark;
      return this.matchNext(expr, matchResult);
   }

   @Override
   public String toString() {
      return this.toString(SkriptPattern.StringificationProperties.DEFAULT);
   }

   @Override
   public String toString(SkriptPattern.StringificationProperties properties) {
      if (properties.excludeParseTags()) {
         return "";
      } else if (this.tag == null) {
         return this.mark + ":";
      } else {
         return this.tag.isEmpty() ? "" : this.tag + ":";
      }
   }

   @Override
   public Set<String> getCombinations(boolean clean) {
      Set<String> combinations = new HashSet<>();
      if (!clean) {
         combinations.add(this.toString());
      }

      return combinations;
   }
}
