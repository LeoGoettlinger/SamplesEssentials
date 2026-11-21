package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public class DuelistsFocusSkill extends MagicWeaponSkill implements ConditionalSkill {
   public static final String SKILL_ID = "wands_duelists_focus";
   private static final double DUEL_RANGE = 12.0;

   public DuelistsFocusSkill() {
      super(SkillType.WANDS, 2, "Duelist's Focus", "Missiles hit harder when you face one enemy alone.", SkillBonusType.CONDITIONAL, "wands_duelists_focus");
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
      return event != null && event.getEliteMobEntity().getLivingEntity() != null
         ? enemiesNear(player, player.getLocation(), 12.0, event.getEliteMobEntity().getLivingEntity()).isEmpty()
         : false;
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.25, 0.003, skillLevel);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("bonus", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0), "range", String.format("%.0f", 12.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bonus", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
   }
}
