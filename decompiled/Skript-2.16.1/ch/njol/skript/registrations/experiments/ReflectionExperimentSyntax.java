package ch.njol.skript.registrations.experiments;

import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

public interface ReflectionExperimentSyntax extends SimpleExperimentalSyntax {
   ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.SCRIPT_REFLECTION);

   @Override
   default ExperimentData getExperimentData() {
      return EXPERIMENT_DATA;
   }
}
