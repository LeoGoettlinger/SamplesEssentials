package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

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

public class SteadyAimSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "crossbows_steady_aim";
   private static final long STAND_TIME_REQUIRED = 1500L;
   private static final double BASE_BONUS = 0.25;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> standingStillSince = new ConcurrentHashMap<>();

   public SteadyAimSkill() {
      super(SkillType.CROSSBOWS, 10, "Steady Aim", "Standing still increases your damage.", SkillBonusType.CONDITIONAL, 1, "crossbows_steady_aim");
   }

   public static boolean hasActiveSkill(UUID playerUUID) {
      return activePlayers.contains(playerUUID);
   }

   public static void updatePlayerMovement(UUID uuid, boolean isMoving) {
      if (isMoving) {
         standingStillSince.remove(uuid);
      } else if (!standingStillSince.containsKey(uuid)) {
         standingStillSince.put(uuid, System.currentTimeMillis());
      }
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      Long stillSince = standingStillSince.get(player.getUniqueId());
      return stillSince == null ? false : System.currentTimeMillis() - stillSince >= 1500L;
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.25, 0.003, skillLevel);
   }

   public double calculateBonus(Player player, int skillLevel) {
      return this.getConditionalBonus(player, skillLevel);
   }

   @Override
   public double getConditionalBonus(Player player, int skillLevel) {
      Long stillSince = standingStillSince.get(player.getUniqueId());
      if (stillSince == null) {
         return 0.0;
      } else {
         long standTime = System.currentTimeMillis() - stillSince;
         if (standTime < 1500L) {
            return 0.0;
         } else {
            double timeBonus = Math.min((standTime - 1500L) / 1500.0, 2.0);
            return this.getConditionalBonus(skillLevel) * (1.0 + timeBonus);
         }
      }
   }

   public static void simulateStationary(UUID uuid, long durationMs) {
      standingStillSince.put(uuid, System.currentTimeMillis() - durationMs);
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.CONDITION_SETUP;
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      standingStillSince.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      standingStillSince.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("value", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel) * 3.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      standingStillSince.clear();
   }
}
