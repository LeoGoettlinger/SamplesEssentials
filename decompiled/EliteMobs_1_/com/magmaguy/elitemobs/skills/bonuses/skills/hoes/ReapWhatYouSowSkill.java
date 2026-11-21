package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class ReapWhatYouSowSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "hoes_reap_what_you_sow";
   private static final double HEALTH_THRESHOLD = 0.5;
   private static final double BASE_BONUS = 0.5;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ReapWhatYouSowSkill() {
      super(
         SkillType.HOES,
         50,
         "Reap What You Sow",
         "Deal more damage when you're at low health - risk for reward.",
         SkillBonusType.CONDITIONAL,
         3,
         "hoes_reap_what_you_sow"
      );
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      return player.getHealth() / player.getMaxHealth() < 0.5;
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(0.5, 0.01, skillLevel);
   }

   @Override
   public double getConditionalBonus(Player player, int skillLevel) {
      double healthPercent = Math.max(0.0, Math.min(1.0, player.getHealth() / player.getMaxHealth()));
      return healthPercent >= 0.5 ? 0.0 : this.getConditionalBonus(skillLevel) * (1.0 + (0.5 - healthPercent) * 2.0);
   }

   public static double calculateRiskBonus(Player player, int skillLevel) {
      return SkillBonusRegistry.getSkillById("hoes_reap_what_you_sow") instanceof ReapWhatYouSowSkill reapSkill
         ? reapSkill.getConditionalBonus(player, skillLevel)
         : 0.0;
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
      double bonusPercent = this.getConditionalBonus(skillLevel) * 100.0;
      double maxBonus = bonusPercent * 2.0;
      return this.applyLoreTemplates(Map.of("bonusPercent", String.format("%.0f", bonusPercent), "maxBonus", String.format("%.0f", maxBonus)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bonusPercent", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
