package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class LegionsDisciplineSkill extends SkillBonus implements StackingSkill {
   public static final String SKILL_ID = "spears_legions_discipline";
   private static final int BASE_MAX_STACKS = 10;
   private static final double BASE_STACK_BONUS = 0.025;
   private static final long STACK_DECAY_MS = 2500L;
   private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public LegionsDisciplineSkill() {
      super(
         SkillType.SPEARS,
         50,
         "Legion's Discipline",
         "Keep landing hits to build damage; lose the rhythm for 2.5s and the stacks fade.",
         SkillBonusType.STACKING,
         3,
         "spears_legions_discipline"
      );
   }

   @Override
   public int getMaxStacks() {
      return 10;
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      double approvedCurve = scaled(0.025, 1.5E-4, skillLevel);
      if (this.configFields == null) {
         return approvedCurve;
      } else {
         double configuredBonus = this.configFields.getStackBonus() + skillLevel * this.configFields.getScalingPerLevel();
         return !Double.isFinite(configuredBonus) ? approvedCurve : Math.max(0.0, Math.min(configuredBonus, approvedCurve));
      }
   }

   @Override
   public int getCurrentStacks(Player player) {
      this.checkStackDecay(player);
      return playerStacks.getOrDefault(player.getUniqueId(), 0);
   }

   @Override
   public void addStack(Player player) {
      int maxStacks = this.getMaxStacks();
      int current = this.getCurrentStacks(player);
      if (current < maxStacks) {
         playerStacks.put(player.getUniqueId(), current + 1);
         player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0, 1.0, 0.0), 2, 0.2, 0.2, 0.2, 0.0);
      }

      lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
   }

   @Override
   public void resetStacks(Player player) {
      int current = playerStacks.getOrDefault(player.getUniqueId(), 0);
      lastHitTime.remove(player.getUniqueId());
      if (current > 0) {
         playerStacks.remove(player.getUniqueId());
         ActionBarCompositor.show(player, ActionBarCompositor.Source.SKILL_FEEDBACK, DungeonsConfig.getLegionsDisciplineBrokenMessage());
      }
   }

   private void checkStackDecay(Player player) {
      Long last = lastHitTime.get(player.getUniqueId());
      if (last != null && System.currentTimeMillis() - last > 2500L) {
         this.resetStacks(player);
      }
   }

   public double getDamageMultiplier(Player player, int skillLevel) {
      int stacks = this.getCurrentStacks(player);
      return 1.0 + stacks * this.getBonusPerStack(skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      playerStacks.remove(player.getUniqueId());
      lastHitTime.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.removeBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "maxStacks",
            String.valueOf(this.getMaxStacks()),
            "perStack",
            String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0),
            "maxBonus",
            String.format("%.0f", this.getMaxStacks() * this.getBonusPerStack(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel) * this.getMaxStacks();
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("maxBonus", String.format("%.0f", this.getBonusValue(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      playerStacks.clear();
      lastHitTime.clear();
   }
}
