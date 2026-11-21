package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.entity.Player;

public class PackHunterSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "bows_pack_hunter";
   private static final double ALLY_RANGE = 10.0;
   private static final double BASE_BONUS_PER_ALLY = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static boolean testOverrideNearbyPlayers = false;

   public PackHunterSkill() {
      super(SkillType.BOWS, 10, "Pack Hunter", "Deal bonus damage while an ally is nearby.", SkillBonusType.CONDITIONAL, 1, "bows_pack_hunter");
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      return testOverrideNearbyPlayers && CombatSimulator.isTestingActive(player)
         ? true
         : player.getNearbyEntities(10.0, 10.0, 10.0).stream().anyMatch(e -> e instanceof Player && !e.equals(player));
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.2, 0.004, skillLevel);
   }

   public double calculateBonus(Player player, int skillLevel) {
      long nearbyPlayers = player.getNearbyEntities(10.0, 10.0, 10.0).stream().filter(e -> e instanceof Player && !e.equals(player)).count();
      return this.getConditionalBonus(skillLevel) * Math.min(nearbyPlayers, 3L);
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
      return this.applyLoreTemplates(
         Map.of(
            "bonusPercent",
            String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0),
            "bonusPerAlly",
            String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0),
            "range",
            String.valueOf(10)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel) * 3.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "bonusPercent",
            String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0),
            "bonusPerAlly",
            String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0),
            "range",
            String.valueOf(10)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }

   @Generated
   public static boolean isTestOverrideNearbyPlayers() {
      return testOverrideNearbyPlayers;
   }

   @Generated
   public static void setTestOverrideNearbyPlayers(boolean testOverrideNearbyPlayers) {
      PackHunterSkill.testOverrideNearbyPlayers = testOverrideNearbyPlayers;
   }
}
