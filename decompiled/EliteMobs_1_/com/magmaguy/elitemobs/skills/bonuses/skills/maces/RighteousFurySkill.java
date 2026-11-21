package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

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

public class RighteousFurySkill extends SkillBonus implements StackingSkill {
   public static final String SKILL_ID = "maces_righteous_fury";
   private static final int MAX_STACKS = 5;
   private static final double BASE_STACK_BONUS = 0.03;
   private static final long STACK_DECAY_MS = 2500L;
   private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public RighteousFurySkill() {
      super(SkillType.MACES, 10, "Righteous Fury", "Build fury stacks on hit, increasing your damage.", SkillBonusType.STACKING, 1, "maces_righteous_fury");
   }

   @Override
   public int getMaxStacks() {
      return 5;
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return scaled(0.03, 0.001, skillLevel);
   }

   @Override
   public int getCurrentStacks(Player player) {
      this.checkStackDecay(player);
      return playerStacks.getOrDefault(player.getUniqueId(), 0);
   }

   @Override
   public void addStack(Player player) {
      int current = this.getCurrentStacks(player);
      if (current < this.getMaxStacks()) {
         playerStacks.put(player.getUniqueId(), current + 1);
         player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0.0, 1.0, 0.0), 5, 0.3, 0.3, 0.3, 0.02);
      }

      lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
   }

   @Override
   public void resetStacks(Player player) {
      playerStacks.remove(player.getUniqueId());
      lastHitTime.remove(player.getUniqueId());
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
      this.resetStacks(player);
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
         Map.of("maxStacks", String.valueOf(this.getMaxStacks()), "damagePerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel) * this.getMaxStacks();
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("maxDamage", String.format("%.0f", this.getBonusValue(skillLevel) * 100.0), "maxStacks", String.valueOf(this.getMaxStacks()))
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      playerStacks.clear();
      lastHitTime.clear();
   }
}
