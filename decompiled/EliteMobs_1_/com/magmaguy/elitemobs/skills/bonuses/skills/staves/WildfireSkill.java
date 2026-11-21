package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class WildfireSkill extends MagicWeaponSkill implements ConditionalSkill {
   public static final String SKILL_ID = "staves_wildfire";
   private static final double CROWD_RADIUS = 4.0;
   private static final int MAX_CROWD = 4;
   private final Map<UUID, Integer> crowd = new ConcurrentHashMap<>();

   public WildfireSkill() {
      super(SkillType.STAVES, 2, "Wildfire", "Fireballs hit harder when enemies crowd together.", SkillBonusType.CONDITIONAL, "staves_wildfire");
   }

   private double bonusPerEnemy(int skillLevel) {
      return scaled(0.1, 0.001, skillLevel);
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         int others = Math.min(4, enemiesNear(player, target.getLocation(), 4.0, target).size());
         if (others == 0) {
            return false;
         } else {
            this.crowd.put(player.getUniqueId(), others);
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return this.bonusPerEnemy(skillLevel) * 4.0;
   }

   @Override
   public double getConditionalBonus(Player player, int skillLevel) {
      return this.bonusPerEnemy(skillLevel) * this.crowd.getOrDefault(player.getUniqueId(), 0).intValue();
   }

   @Override
   protected void clearPlayer(UUID playerId) {
      this.crowd.remove(playerId);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "bonusPerEnemy",
            String.format("%.1f", this.bonusPerEnemy(skillLevel) * 100.0),
            "maxBonus",
            String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("maxBonus", String.format("%.0f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      this.crowd.clear();
      super.shutdown();
   }
}
