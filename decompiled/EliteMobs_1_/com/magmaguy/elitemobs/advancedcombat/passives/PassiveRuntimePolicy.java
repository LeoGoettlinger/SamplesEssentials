package com.magmaguy.elitemobs.advancedcombat.passives;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.Collection;
import java.util.Objects;

final class PassiveRuntimePolicy {
   private PassiveRuntimePolicy() {
   }

   static int controlDurationTicks(int originalTicks, PassiveMechanics mechanics) {
      Objects.requireNonNull(mechanics, "mechanics");
      return originalTicks != -1 && originalTicks > 0 ? Math.max(1, (int)Math.round(originalTicks * knockbackMultiplier(mechanics))) : originalTicks;
   }

   static double knockbackMultiplier(PassiveMechanics mechanics) {
      Objects.requireNonNull(mechanics, "mechanics");
      return 1.0 - mechanics.controlResistanceFraction();
   }

   static boolean isMagicWeaponSkill(SkillType progressionSkill) {
      return progressionSkill == SkillType.WANDS || progressionSkill == SkillType.STAVES;
   }

   static double partyMovementAdjustment(Collection<PassiveMechanics> nearbyPartyMechanics, boolean grouped) {
      Objects.requireNonNull(nearbyPartyMechanics, "nearbyPartyMechanics");
      return !grouped
         ? 0.0
         : nearbyPartyMechanics.stream().filter(Objects::nonNull).mapToDouble(PassiveMechanics::partyMovementSpeedAdjustment).max().orElse(0.0);
   }
}
