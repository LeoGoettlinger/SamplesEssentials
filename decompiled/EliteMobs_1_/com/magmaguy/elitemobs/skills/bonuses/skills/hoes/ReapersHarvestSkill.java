package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ReapersHarvestSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "hoes_reapers_harvest";
   private static final double HEALTH_THRESHOLD = 0.25;
   private static final double BASE_BONUS = 0.5;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ReapersHarvestSkill() {
      super(
         SkillType.HOES,
         10,
         "Reaper's Harvest",
         "Deal massive bonus damage to enemies below 25% health.",
         SkillBonusType.CONDITIONAL,
         1,
         "hoes_reapers_harvest"
      );
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            return target.getHealth() / target.getMaxHealth() < 0.25;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(0.5, 0.01, skillLevel);
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
      return this.applyLoreTemplates(Map.of("bonusPercent", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bonusPercent", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
