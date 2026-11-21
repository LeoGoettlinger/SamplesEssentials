package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.patterns.SkriptPattern;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface Experiment {
   @Internal
   static Experiment unknown(String text) {
      return new UnmatchedExperiment(text);
   }

   static Experiment constant(String codeName, LifeCycle phase, String... patterns) {
      return new ConstantExperiment(codeName, phase, patterns);
   }

   String codeName();

   LifeCycle phase();

   default boolean isKnown() {
      return this.phase() != LifeCycle.UNKNOWN;
   }

   SkriptPattern pattern();

   default boolean matches(String text) {
      return this.pattern().match(text) != null;
   }
}
