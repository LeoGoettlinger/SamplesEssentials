package ch.njol.skript.patterns;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class OptionalPatternElement extends PatternElement {
   private final PatternElement patternElement;

   public OptionalPatternElement(PatternElement patternElement) {
      if (patternElement instanceof GroupPatternElement groupPatternElement && groupPatternElement.next == null) {
         patternElement = groupPatternElement.getPatternElement();
      }

      this.patternElement = patternElement;
   }

   @Override
   void setNext(@Nullable PatternElement next) {
      super.setNext(next);
      this.patternElement.setLastNext(next);
   }

   @Nullable
   @Override
   public MatchResult match(String expr, MatchResult matchResult) {
      MatchResult newMatchResult = this.patternElement.match(expr, matchResult.copy());
      return newMatchResult != null ? newMatchResult : this.matchNext(expr, matchResult);
   }

   public PatternElement getPatternElement() {
      return this.patternElement;
   }

   @Override
   public String toString() {
      return this.toString(SkriptPattern.StringificationProperties.DEFAULT);
   }

   @Override
   public String toString(SkriptPattern.StringificationProperties properties) {
      return "[" + this.patternElement.toFullString(properties) + "]";
   }

   @Override
   public Set<String> getCombinations(boolean clean) {
      Set<String> combinations = this.patternElement.getAllCombinations(clean);
      combinations.add("");
      return combinations;
   }
}
