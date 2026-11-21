package org.skriptlang.skript.lang.experiment;

import java.util.Collection;
import java.util.LinkedHashSet;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.ScriptData;

public class ExperimentSet extends LinkedHashSet<Experiment> implements ScriptData, Experimented {
   public ExperimentSet(@NotNull Collection<? extends Experiment> collection) {
      super(collection);
   }

   public ExperimentSet() {
   }

   @Override
   public boolean hasExperiment(Experiment experiment) {
      return this.contains(experiment);
   }

   @Override
   public boolean hasExperiment(String featureName) {
      for (Experiment experiment : this) {
         if (experiment.matches(featureName)) {
            return true;
         }
      }

      return false;
   }
}
