package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class FortifySkill extends SkillBonus implements StackingSkill {
   private static final double REDUCTION_PER_STACK = 0.08;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Integer> stackMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();
   private static final long STACK_DECAY_TIME = 10000L;

   public FortifySkill() {
      super(SkillType.ARMOR, 50, "Fortify", "Build damage reduction stacks when taking damage", SkillBonusType.STACKING, 3, "armor_fortify");
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
      stackMap.remove(player.getUniqueId());
      lastDamageTime.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of("maxStacks", String.valueOf(this.getMaxStacks()), "reductionPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("reductionPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0), "maxStacks", String.valueOf(this.getMaxStacks()))
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      stackMap.clear();
      lastDamageTime.clear();
   }

   @Override
   public int getMaxStacks() {
      return this.configFields != null && this.configFields.getMaxStacks() > 0 ? this.configFields.getMaxStacks() : 5;
   }

   @Override
   public int getCurrentStacks(Player player) {
      Long lastDamage = lastDamageTime.get(player.getUniqueId());
      if (lastDamage != null && System.currentTimeMillis() - lastDamage > 10000L) {
         this.resetStacks(player);
      }

      return stackMap.getOrDefault(player.getUniqueId(), 0);
   }

   @Override
   public void addStack(Player player) {
      int current = stackMap.getOrDefault(player.getUniqueId(), 0);
      if (current < this.getMaxStacks()) {
         stackMap.put(player.getUniqueId(), current + 1);
      }

      lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
   }

   @Override
   public void resetStacks(Player player) {
      stackMap.remove(player.getUniqueId());
      lastDamageTime.remove(player.getUniqueId());
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      double configuredReduction = this.configFields == null ? 0.08 : 0.05 * this.configFields.calculateValue(skillLevel);
      return Math.min(0.8 / this.getMaxStacks(), Math.max(0.0, configuredReduction));
   }

   public double modifyIncomingDamage(Player player, double originalDamage) {
      int skillLevel = this.getPlayerSkillLevel(player);
      int currentStacks = this.getCurrentStacks(player);
      double reduction = clampDefensiveReduction(currentStacks * this.getBonusPerStack(skillLevel));
      double modifiedDamage = originalDamage * (1.0 - reduction);
      this.addStack(player);
      sendStackingSkillActionBar(player, this, this.getCurrentStacks(player), this.getMaxStacks());
      return modifiedDamage;
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }
}
