package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BattlemagesGuardSkill extends MagicWeaponSkill {
   public static final String SKILL_ID = "staves_battlemages_guard";
   private static final double GUARD_RANGE = 4.0;

   public BattlemagesGuardSkill() {
      super(
         SkillType.STAVES,
         1,
         "Battlemage's Guard",
         "Take less damage from nearby enemies while holding a staff.",
         SkillBonusType.PASSIVE,
         "staves_battlemages_guard"
      );
   }

   private double reduction(int skillLevel) {
      return SkillBonus.clampDefensiveReduction(scaled(0.1, 0.001, skillLevel));
   }

   public static double applyGuard(Player player, PlayerDamagedByEliteMobEvent event, double damage) {
      if (SkillBonusRegistry.getSkillById("staves_battlemages_guard") instanceof BattlemagesGuardSkill guard && guard.wieldedBy(player)) {
         LivingEntity attacker = event.getAttacker();
         if (attacker != null && attacker.getWorld().equals(player.getWorld()) && !(attacker.getLocation().distanceSquared(player.getLocation()) > 16.0)) {
            guard.incrementProcCount(player);
            SkillBonus.sendSkillActionBar(player, guard);
            return damage * (1.0 - guard.reduction(guard.skillLevel(player)));
         } else {
            return damage;
         }
      } else {
         return damage;
      }
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("reduction", String.format("%.1f", this.reduction(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.reduction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("reduction", String.format("%.1f", this.reduction(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }
}
