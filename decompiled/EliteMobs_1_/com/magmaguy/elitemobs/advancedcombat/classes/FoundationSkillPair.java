package com.magmaguy.elitemobs.advancedcombat.classes;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

public record FoundationSkillPair(SkillType first, SkillType second) {
   public FoundationSkillPair(SkillType first, SkillType second) {
      first = Objects.requireNonNull(first, "first");
      second = Objects.requireNonNull(second, "second");
      if (first == second) {
         throw new IllegalArgumentException("A class form requires two distinct foundation skills");
      } else {
         this.first = first;
         this.second = second;
      }
   }

   public List<SkillType> asList() {
      return List.of(this.first, this.second);
   }

   public int limitingLevel(ToIntFunction<SkillType> levelProvider) {
      Objects.requireNonNull(levelProvider, "levelProvider");
      return Math.min(requireLevel(levelProvider.applyAsInt(this.first)), requireLevel(levelProvider.applyAsInt(this.second)));
   }

   public List<SkillType> limitingSkills(ToIntFunction<SkillType> levelProvider) {
      Objects.requireNonNull(levelProvider, "levelProvider");
      int firstLevel = requireLevel(levelProvider.applyAsInt(this.first));
      int secondLevel = requireLevel(levelProvider.applyAsInt(this.second));
      if (firstLevel < secondLevel) {
         return List.of(this.first);
      } else {
         return secondLevel < firstLevel ? List.of(this.second) : List.of(this.first, this.second);
      }
   }

   private static int requireLevel(int level) {
      if (level < 0) {
         throw new IllegalArgumentException("Foundation skill levels must not be negative");
      } else {
         return level;
      }
   }
}
