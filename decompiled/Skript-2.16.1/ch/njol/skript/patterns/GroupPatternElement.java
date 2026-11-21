package ch.njol.skript.patterns;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class GroupPatternElement extends PatternElement {
   private final PatternElement patternElement;

   public GroupPatternElement(PatternElement patternElement) {
      this.patternElement = patternElement;
   }

   public PatternElement getPatternElement() {
      return this.patternElement;
   }

   @Override
   void setNext(@Nullable PatternElement next) {
      super.setNext(next);
      this.patternElement.setLastNext(next);
   }

   @Nullable
   @Override
   public MatchResult match(String expr, MatchResult matchResult) {
      return this.patternElement.match(expr, matchResult);
   }

   @Override
   public String toString() {
      return this.toString(SkriptPattern.StringificationProperties.DEFAULT);
   }

   @Override
   public String toString(SkriptPattern.StringificationProperties properties) {
      return "(" + this.patternElement.toFullString(properties) + ")";
   }

   @Override
   public Set<String> getCombinations(boolean clean) {
      return this.patternElement.getAllCombinations(clean);
   }
}
