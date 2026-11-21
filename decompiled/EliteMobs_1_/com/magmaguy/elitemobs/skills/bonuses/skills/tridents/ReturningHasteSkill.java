package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class ReturningHasteSkill extends SkillBonus implements StackingSkill {
   public static final String SKILL_ID = "tridents_returning_haste";
   private static final int BASE_MAX_STACKS = 5;
   private static final long STACK_DECAY_TIME = 3000L;
   private static final double BASE_BONUS_PER_STACK = 0.05;
   private static final Map<UUID, Integer> playerStacks = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> lastThrowTime = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ReturningHasteSkill() {
      super(
         SkillType.TRIDENTS,
         50,
         "Returning Haste",
         "Build stacks with consecutive trident throws. Each stack increases damage.",
         SkillBonusType.STACKING,
         3,
         "tridents_returning_haste"
      );
   }

   @Override
   public int getMaxStacks() {
      return this.configFields != null && this.configFields.getMaxStacks() > 0 ? this.configFields.getMaxStacks() : 5;
   }

   @Override
   public int getCurrentStacks(Player player) {
      Long lastThrow = lastThrowTime.get(player.getUniqueId());
      if (lastThrow != null && System.currentTimeMillis() - lastThrow > 3000L) {
         this.resetStacks(player);
      }

      return playerStacks.getOrDefault(player.getUniqueId(), 0);
   }

   @Override
   public void addStack(Player player) {
      int current = this.getCurrentStacks(player);
      if (current < this.getMaxStacks()) {
         playerStacks.put(player.getUniqueId(), current + 1);
      }

      lastThrowTime.put(player.getUniqueId(), System.currentTimeMillis());
   }

   @Override
   public boolean stacksOnHit(EliteMobDamagedByPlayerEvent event) {
      return event.getEntityDamageByEntityEvent() == null ? event.isRangedAttack() : event.getEntityDamageByEntityEvent().getCause() == DamageCause.PROJECTILE;
   }

   @Override
   public void resetStacks(Player player) {
      playerStacks.remove(player.getUniqueId());
      lastThrowTime.remove(player.getUniqueId());
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return this.configFields != null ? 0.05 + this.configFields.calculateValue(skillLevel) * 0.01 : scaled(0.05, 6.0E-4, skillLevel);
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

   public static void simulateStacks(UUID uuid, int stacks) {
      playerStacks.put(uuid, stacks);
      lastThrowTime.put(uuid, System.currentTimeMillis());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double bonusPerStack = this.getBonusPerStack(skillLevel) * 100.0;
      double maxBonus = bonusPerStack * this.getMaxStacks();
      return this.applyLoreTemplates(
         Map.of(
            "maxStacks",
            String.valueOf(this.getMaxStacks()),
            "bonusPerStack",
            String.format("%.1f", bonusPerStack),
            "maxBonus",
            String.format("%.1f", maxBonus)
         )
      );
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.CONDITION_SETUP;
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      playerStacks.clear();
      lastThrowTime.clear();
      activePlayers.clear();
   }
}
