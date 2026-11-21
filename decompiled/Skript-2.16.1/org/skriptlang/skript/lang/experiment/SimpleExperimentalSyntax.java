package org.skriptlang.skript.lang.experiment;

public interface SimpleExperimentalSyntax extends ExperimentalSyntax {
   @Override
   default boolean isSatisfiedBy(ExperimentSet experimentSet) {
      return this.getExperimentData().checkRequirementsAndError(experimentSet);
   }

   ExperimentData getExperimentData();
}
