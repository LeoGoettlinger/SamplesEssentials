package com.magmaguy.elitemobs.skills;

import java.util.Objects;
import org.bukkit.NamespacedKey;

public record WeaponIdentity(NamespacedKey id, SkillType progressionSkill) {
   public WeaponIdentity(NamespacedKey id, SkillType progressionSkill) {
      id = Objects.requireNonNull(id, "id");
      progressionSkill = Objects.requireNonNull(progressionSkill, "progressionSkill");
      if (!progressionSkill.isWeaponSkill()) {
         throw new IllegalArgumentException("A weapon identity must use a weapon skill");
      } else {
         this.id = id;
         this.progressionSkill = progressionSkill;
      }
   }
}
