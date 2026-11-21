package com.magmaguy.elitemobs.advancedcombat.classes;

import java.util.Arrays;
import java.util.Optional;

public enum ClassBand {
   ROOT(0, 1, 30, 10),
   LEVEL_31(1, 31, 60, 30),
   LEVEL_61(2, 61, 90, 60),
   LEVEL_91(3, 91, Integer.MAX_VALUE, 90),
   STARTER(-1, 1, 10, 1);

   private final int depth;
   private final int effectiveStart;
   private final int effectiveEnd;
   private final int skillUnlockLevel;

   private ClassBand(int depth, int effectiveStart, int effectiveEnd, int skillUnlockLevel) {
      this.depth = depth;
      this.effectiveStart = effectiveStart;
      this.effectiveEnd = effectiveEnd;
      this.skillUnlockLevel = skillUnlockLevel;
   }

   public int depth() {
      return this.depth;
   }

   public int effectiveStart() {
      return this.effectiveStart;
   }

   public int effectiveEnd() {
      return this.effectiveEnd;
   }

   public int skillUnlockLevel() {
      return this.skillUnlockLevel;
   }

   public boolean isRoot() {
      return this == ROOT || this == STARTER;
   }

   public boolean isTerminal() {
      return this == LEVEL_91;
   }

   public boolean containsEffectiveLevel(int effectiveLevel) {
      return effectiveLevel >= this.effectiveStart && effectiveLevel <= this.effectiveEnd;
   }

   public int toEffectiveLevel(int localLevel) {
      if (localLevel < 1) {
         throw new IllegalArgumentException("Local class level must be positive");
      } else {
         long effectiveLevel = (long)this.effectiveStart + localLevel - 1L;
         if (effectiveLevel > this.effectiveEnd) {
            throw new IllegalArgumentException("Local level " + localLevel + " exceeds the " + this + " band");
         } else {
            return Math.toIntExact(effectiveLevel);
         }
      }
   }

   public int toLocalLevel(int effectiveLevel) {
      if (!this.containsEffectiveLevel(effectiveLevel)) {
         throw new IllegalArgumentException("Effective level " + effectiveLevel + " is outside the " + this + " band");
      } else {
         return effectiveLevel - this.effectiveStart + 1;
      }
   }

   public Optional<ClassBand> next() {
      return this.isTerminal() ? Optional.empty() : Arrays.stream(values()).filter(candidate -> candidate.depth == this.depth + 1).findFirst();
   }

   public static ClassBand atDepth(int depth) {
      return Arrays.stream(values())
         .filter(candidate -> candidate.depth == depth)
         .findFirst()
         .orElseThrow(() -> new IllegalArgumentException("Unsupported class-tree depth: " + depth));
   }
}
