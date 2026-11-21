package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.registrations.Feature;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

public interface EquippableExperimentSyntax extends SimpleExperimentalSyntax {
   ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.EQUIPPABLE_COMPONENTS);

   @Override
   default ExperimentData getExperimentData() {
      return EXPERIMENT_DATA;
   }
}
