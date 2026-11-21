package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class SwiftStrikesSkill extends SkillBonus {
   public static final String SKILL_ID = "swords_swift_strikes";
   private static final double BASE_SPEED_BONUS = 0.05;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, SwiftStrikesSkill.SpeedLease> appliedSpeeds = new ConcurrentHashMap<>();

   public SwiftStrikesSkill() {
      super(SkillType.SWORDS, 10, "Swift Strikes", "Move faster while wielding a sword.", SkillBonusType.PASSIVE, 1, "swords_swift_strikes");
   }

   public static double getSpeedBonus(int skillLevel) {
      return scaled(0.05, 0.001, 0.15, skillLevel);
   }

   public static boolean hasActiveSkill(UUID playerUUID) {
      return activePlayers.contains(playerUUID);
   }

   public static void applySpeedBonus(Player player, int skillLevel) {
      if (hasActiveSkill(player.getUniqueId())) {
         SwiftStrikesSkill.SpeedLease current = appliedSpeeds.get(player.getUniqueId());
         float actual = player.getWalkSpeed();
         float baseline = current != null && current.player() == player && Float.compare(actual, current.applied()) == 0 ? current.baseline() : actual;
         float applied = Math.min(1.0F, baseline + (float)getSpeedBonus(skillLevel));
         if (Float.compare(actual, applied) != 0) {
            player.setWalkSpeed(applied);
         }

         appliedSpeeds.put(player.getUniqueId(), new SwiftStrikesSkill.SpeedLease(player, baseline, applied));
      }
   }

   public static void removeSpeedBonus(Player player) {
      SwiftStrikesSkill.SpeedLease lease = appliedSpeeds.get(player.getUniqueId());
      if (lease != null && lease.player() == player) {
         appliedSpeeds.remove(player.getUniqueId(), lease);
         if (Float.compare(player.getWalkSpeed(), lease.applied()) == 0) {
            player.setWalkSpeed(lease.baseline());
         }
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
      if (WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand()) == SkillType.SWORDS) {
         applySpeedBonus(player, skillLevel);
      } else {
         removeSpeedBonus(player);
      }
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeSpeedBonus(player);
   }

   @Override
   public void onActivate(Player player) {
      this.applyBonus(player, SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS));
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      removeSpeedBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double speed = getSpeedBonus(skillLevel) * 100.0;
      return this.applyLoreTemplates(Map.of("value", String.format("%.1f", speed)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return getSpeedBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", getSpeedBonus(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.ATTRIBUTE_CHECK;
   }

   @Override
   public void shutdown() {
      for (SwiftStrikesSkill.SpeedLease lease : List.copyOf(appliedSpeeds.values())) {
         removeSpeedBonus(lease.player());
      }

      activePlayers.clear();
   }

   private record SpeedLease(Player player, float baseline, float applied) {
   }
}
