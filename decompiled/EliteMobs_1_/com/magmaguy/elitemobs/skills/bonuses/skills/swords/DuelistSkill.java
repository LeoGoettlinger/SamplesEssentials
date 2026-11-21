package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
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
import org.bukkit.entity.Player;

public class DuelistSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "swords_duelist";
   private static final double BASE_DAMAGE_BONUS = 0.45;
   private static final double DETECTION_RADIUS = 10.0;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public DuelistSkill() {
      super(SkillType.SWORDS, 50, "Duelist", "Deal bonus damage when fighting a single enemy.", SkillBonusType.CONDITIONAL, 3, "swords_duelist");
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity target = event.getEliteMobEntity();
         return target != null && target.getLivingEntity() != null
            ? target.getLivingEntity()
               .getNearbyEntities(10.0, 10.0, 10.0)
               .stream()
               .filter(e -> !e.getUniqueId().equals(target.getLivingEntity().getUniqueId()))
               .noneMatch(EntityTracker::isEliteMob)
            : false;
      } else {
         return false;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.45, 0.006, skillLevel);
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
      double bonus = this.getConditionalBonus(skillLevel) * 100.0;
      return this.applyLoreTemplates(Map.of("value", String.format("%.1f", bonus)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("value", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
