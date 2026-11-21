package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class HarvesterSkill extends SkillBonus {
   public static final String SKILL_ID = "hoes_harvester";
   private static final double BASE_DAMAGE_BONUS = 0.1;
   private static final double MAX_REWARD_BONUS = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public HarvesterSkill() {
      super(SkillType.HOES, 25, "Harvester", "Deal more damage and reap up to 20% more Elite Coins.", SkillBonusType.PASSIVE, 2, "hoes_harvester");
   }

   public double getDamageBonus(int skillLevel) {
      double approvedCurve = scaled(0.1, 0.001, skillLevel);
      if (this.configFields == null) {
         return approvedCurve;
      } else {
         double configuredBonus = this.configFields.calculateValue(skillLevel);
         return !Double.isFinite(configuredBonus) ? approvedCurve : Math.max(0.0, Math.min(configuredBonus, approvedCurve));
      }
   }

   public double getLootBonus(int skillLevel) {
      return Math.max(0.0, Math.min(this.getDamageBonus(skillLevel), 0.2));
   }

   public static double getLootMultiplier(Player player) {
      if (player != null && activePlayers.contains(player.getUniqueId())) {
         if (SkillBonusRegistry.getSkillById("hoes_harvester") instanceof HarvesterSkill harvester) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.HOES);
            return 1.0 + harvester.getLootBonus(skillLevel);
         } else {
            return 1.0;
         }
      } else {
         return 1.0;
      }
   }

   public static int applyEliteCoinBonus(Player player, int baseAmount) {
      return baseAmount <= 0 ? Math.max(0, baseAmount) : (int)Math.floor(baseAmount * getLootMultiplier(player));
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
            "damagePercent",
            String.format("%.1f", this.getDamageBonus(skillLevel) * 100.0),
            "lootPercent",
            String.format("%.1f", this.getLootBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "damagePercent",
            String.format("%.1f", this.getDamageBonus(skillLevel) * 100.0),
            "lootPercent",
            String.format("%.1f", this.getLootBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
