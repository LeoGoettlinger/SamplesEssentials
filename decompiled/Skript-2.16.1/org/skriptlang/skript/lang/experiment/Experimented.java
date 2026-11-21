package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.Skript;

@FunctionalInterface
public interface Experimented {
   boolean hasExperiment(Experiment var1);

   default boolean hasExperiment(String featureName) {
      return Skript.experiments().find(featureName).isKnown();
   }
}
