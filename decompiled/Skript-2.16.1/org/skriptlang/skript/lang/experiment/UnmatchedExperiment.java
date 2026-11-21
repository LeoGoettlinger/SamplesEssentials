package org.skriptlang.skript.lang.experiment;

class UnmatchedExperiment extends ConstantExperiment {
   UnmatchedExperiment(String codeName) {
      super(codeName, LifeCycle.UNKNOWN);
   }

   @Override
   public LifeCycle phase() {
      return LifeCycle.UNKNOWN;
   }

   @Override
   public boolean isKnown() {
      return false;
   }
}
