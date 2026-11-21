package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.Script;

public class ExperimentRegistry implements Experimented {
   private final Skript skript;
   private final Set<Experiment> experiments;

   public ExperimentRegistry(Skript skript) {
      this.skript = skript;
      this.experiments = new LinkedHashSet<>();
   }

   @NotNull
   public Experiment find(String text) {
      if (this.experiments.isEmpty()) {
         return Experiment.unknown(text);
      } else {
         for (Experiment experiment : this.experiments) {
            if (experiment.matches(text)) {
               return experiment;
            }
         }

         return Experiment.unknown(text);
      }
   }

   public Experiment[] registered() {
      return this.experiments.toArray(new Experiment[0]);
   }

   public void register(SkriptAddon addon, Experiment experiment) {
      this.experiments.add(experiment);
   }

   public void registerAll(SkriptAddon addon, Experiment... experiments) {
      for (Experiment experiment : experiments) {
         this.register(addon, experiment);
      }
   }

   public void unregister(SkriptAddon addon, Experiment experiment) {
      this.experiments.remove(experiment);
   }

   public Experiment register(SkriptAddon addon, String codeName, LifeCycle phase, String... patterns) {
      Experiment experiment = Experiment.constant(codeName, phase, patterns);
      this.register(addon, experiment);
      return experiment;
   }

   @Override
   public boolean hasExperiment(Experiment experiment) {
      return this.experiments.contains(experiment);
   }

   @Override
   public boolean hasExperiment(String featureName) {
      return this.find(featureName).isKnown();
   }

   public boolean isUsing(Script script, Experiment experiment) {
      if (script == null) {
         return false;
      } else {
         ExperimentSet set = script.getData(ExperimentSet.class);
         return set == null ? false : set.hasExperiment(experiment);
      }
   }

   public boolean isUsing(Script script, String featureName) {
      if (script == null) {
         return false;
      } else {
         ExperimentSet set = script.getData(ExperimentSet.class);
         return set == null ? false : set.hasExperiment(featureName);
      }
   }
}
