package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class RangersFocusSkill extends SkillBonus implements StackingSkill {
   public static final String SKILL_ID = "bows_rangers_focus";
   private static final int MAX_STACKS = 8;
   private static final double BASE_BONUS_PER_STACK = 0.03;
   private static final long FOCUS_DECAY_TIME = 3000L;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
   private static final Map<UUID, UUID> targetedEnemy = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> lastHitTime = new ConcurrentHashMap<>();

   public RangersFocusSkill() {
      super(SkillType.BOWS, 75, "Ranger's Focus", "Focusing on a single target increases your damage.", SkillBonusType.STACKING, 4, "bows_rangers_focus");
   }

   @Override
   public int getMaxStacks() {
      return 8;
   }

   @Override
   public int getCurrentStacks(Player player) {
      Long lastHit = lastHitTime.get(player.getUniqueId());
      if (lastHit != null && System.currentTimeMillis() - lastHit > 3000L) {
         this.resetStacks(player);
      }

      return playerStacks.getOrDefault(player.getUniqueId(), 0);
   }

   @Override
   public void addStack(Player player) {
      int current = this.getCurrentStacks(player);
      if (current < 8) {
         playerStacks.put(player.getUniqueId(), current + 1);
      }

      lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
   }

   @Override
   public void resetStacks(Player player) {
      playerStacks.remove(player.getUniqueId());
      targetedEnemy.remove(player.getUniqueId());
      lastHitTime.remove(player.getUniqueId());
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return scaled(0.03, 4.0E-4, skillLevel);
   }

   public UUID getTargetedEnemy(Player player) {
      return targetedEnemy.get(player.getUniqueId());
   }

   public void setTargetedEnemy(Player player, UUID targetUUID) {
      this.getCurrentStacks(player);
      UUID currentTarget = targetedEnemy.get(player.getUniqueId());
      if (currentTarget != null && !currentTarget.equals(targetUUID)) {
         this.resetStacks(player);
      }

      targetedEnemy.put(player.getUniqueId(), targetUUID);
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
      activePlayers.remove(player.getUniqueId());
      this.resetStacks(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0), "maxStacks", String.valueOf(8)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel) * 8.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0), "maxStacks", String.valueOf(8))
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      playerStacks.clear();
      targetedEnemy.clear();
      lastHitTime.clear();
   }
}
