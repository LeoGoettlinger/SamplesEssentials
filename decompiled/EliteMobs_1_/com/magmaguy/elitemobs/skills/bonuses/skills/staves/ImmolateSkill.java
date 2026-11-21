package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public class ImmolateSkill extends MagicWeaponSkill implements ConditionalSkill {
   public static final String SKILL_ID = "staves_immolate";
   private static final double HEALTH_THRESHOLD = 0.3;

   public ImmolateSkill() {
      super(SkillType.STAVES, 3, "Immolate", "Fireballs deal bonus damage to badly wounded enemies.", SkillBonusType.CONDITIONAL, "staves_immolate");
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
      if (event == null) {
         return false;
      } else {
         EliteEntity elite = event.getEliteMobEntity();
         return elite.getMaxHealth() > 0.0 && elite.getHealth() / elite.getMaxHealth() < 0.3;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.45, 0.006, skillLevel);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of("bonus", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0), "threshold", String.format("%.0f", 30.0))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("bonus", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0), "threshold", String.format("%.0f", 30.0))
      );
   }
}
