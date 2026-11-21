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

public class IronStanceSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "armor_iron_stance";
   private static final double DAMAGE_REDUCTION = 0.4;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> lastMoveTime = new ConcurrentHashMap<>();
   private static final long STAND_TIME_REQUIRED = 1000L;

   public IronStanceSkill() {
      super(SkillType.ARMOR, 10, "Iron Stance", "Gain damage reduction when standing still", SkillBonusType.CONDITIONAL, 1, "armor_iron_stance");
   }

   public static void simulateStationary(UUID uuid) {
      lastMoveTime.remove(uuid);
   }

   @Override
   public void removeBonus(Player player) {
      this.onDeactivate(player);
   }

   @Override
   public void onActivate(Player player) {
      if (activePlayers.add(player.getUniqueId())) {
         lastMoveTime.put(player.getUniqueId(), System.currentTimeMillis());
      }
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      lastMoveTime.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      if (activePlayers.add(player.getUniqueId())) {
         lastMoveTime.put(player.getUniqueId(), System.currentTimeMillis());
      }
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("reduction", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      lastMoveTime.clear();
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (!this.isActive(player)) {
         return false;
      } else {
         Long lastMove = lastMoveTime.get(player.getUniqueId());
         return lastMove == null ? true : System.currentTimeMillis() - lastMove >= 1000L;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return clampDefensiveReduction(this.configFields == null ? 0.4 : this.configFields.calculateValue(skillLevel));
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("reduction", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.CONDITION_SETUP;
   }

   public static void updatePlayerMovement(UUID uuid, boolean isMoving) {
      if (isMoving && activePlayers.contains(uuid)) {
         lastMoveTime.put(uuid, System.currentTimeMillis());
      }
   }

   public double modifyIncomingDamage(Player player, double originalDamage, Object context) {
      if (this.conditionMet(player, context)) {
         int skillLevel = this.getPlayerSkillLevel(player);
         double reduction = clampDefensiveReduction(this.getConditionalBonus(skillLevel));
         return originalDamage * (1.0 - reduction);
      } else {
         return originalDamage;
      }
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }
}
