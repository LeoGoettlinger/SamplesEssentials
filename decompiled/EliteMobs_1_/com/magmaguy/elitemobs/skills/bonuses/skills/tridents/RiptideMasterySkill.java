package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class RiptideMasterySkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "tridents_riptide_mastery";
   private static final double BASE_BONUS = 0.16;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public RiptideMasterySkill() {
      super(
         SkillType.TRIDENTS,
         50,
         "Riptide Mastery",
         "Bonus damage when in water or rain. Extra bonus if both conditions are met.",
         SkillBonusType.CONDITIONAL,
         3,
         "tridents_riptide_mastery"
      );
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      return player.isInWater() || player.getWorld().hasStorm();
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return this.calculateBonus(skillLevel);
   }

   @Override
   public double getConditionalBonus(Player player, int skillLevel) {
      return this.getTotalBonus(player, skillLevel);
   }

   private double calculateBonus(int skillLevel) {
      return this.configFields != null ? 0.16 + this.configFields.calculateValue(skillLevel) : scaled(0.16, 0.003, skillLevel);
   }

   public double getTotalBonus(Player player, int skillLevel) {
      if (!this.conditionMet(player, null)) {
         return 0.0;
      } else {
         double bonus = this.getConditionalBonus(skillLevel);
         if (player.isInWater() && player.getWorld().hasStorm()) {
            bonus *= 1.5;
         }

         return bonus;
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double bonus = this.getConditionalBonus(skillLevel) * 100.0;
      double doubleBonus = bonus * 1.5;
      return this.applyLoreTemplates(Map.of("damageBonus", String.format("%.1f", bonus), "doubleBonus", String.format("%.1f", doubleBonus)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("damageBonus", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
