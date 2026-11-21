package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ArcingBoltSkill extends MagicWeaponSkill implements ProcSkill {
   public static final String SKILL_ID = "wands_arcing_bolt";
   private static final double ARC_RANGE = 5.0;

   public ArcingBoltSkill() {
      super(SkillType.WANDS, 2, "Arcing Bolt", "Missiles can arc to another nearby enemy.", SkillBonusType.PROC, "wands_arcing_bolt");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.WAND_MISSILE) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.15, 0.002, 0.45, skillLevel);
   }

   private double arcFraction(int skillLevel) {
      return scaled(0.55, 0.005, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.WAND_MISSILE);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         List<LivingEntity> nearby = enemiesNear(player, target.getLocation(), 5.0, target);
         if (!nearby.isEmpty()) {
            LivingEntity arcTarget = nearby.get(0);
            beam(chest(target), chest(arcTarget), Particle.ELECTRIC_SPARK);
            sideDamage(player, arcTarget, event.getDamage() * this.arcFraction(this.skillLevel(player)));
         }
      }
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "arcPercent",
            String.format("%.0f", this.arcFraction(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.arcFraction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("arcPercent", String.format("%.0f", this.arcFraction(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }
}
