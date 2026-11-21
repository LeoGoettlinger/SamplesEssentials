package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

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

public class GritSkill extends SkillBonus implements ConditionalSkill {
   private static final double MAX_REDUCTION = 0.8;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final double HEALTH_THRESHOLD = 0.5;

   public GritSkill() {
      super(SkillType.ARMOR, 50, "Grit", "Gain increased damage reduction when below 50% health", SkillBonusType.CONDITIONAL, 3, "armor_grit");
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      this.onDeactivate(player);
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
      return this.applyLoreTemplates(Map.of("maxReduction", String.format("%.1f", this.getMaxReduction(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("maxReduction", String.format("%.1f", this.getMaxReduction(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      double damage = context instanceof Number ? ((Number)context).doubleValue() : 0.0;
      return (player.getHealth() - damage) / player.getMaxHealth() < 0.5;
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return this.getMaxReduction(skillLevel);
   }

   private double getMaxReduction(int skillLevel) {
      double configuredReduction = this.configFields == null ? 0.8 : 0.5 * this.configFields.calculateValue(skillLevel);
      return clampDefensiveReduction(configuredReduction);
   }

   public double modifyIncomingDamage(Player player, double originalDamage, Object context) {
      if (!this.conditionMet(player, originalDamage)) {
         return originalDamage;
      } else {
         int skillLevel = this.getPlayerSkillLevel(player);
         double postDamageHealth = Math.max(0.0, player.getHealth() - originalDamage);
         double healthPercent = postDamageHealth / player.getMaxHealth();
         double lowHealthMultiplier = (0.5 - healthPercent) / 0.5;
         double reduction = clampDefensiveReduction(this.getMaxReduction(skillLevel) * lowHealthMultiplier);
         return originalDamage * (1.0 - reduction);
      }
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.CONDITION_SETUP;
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }
}
