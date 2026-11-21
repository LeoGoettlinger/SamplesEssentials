package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public class ArcaneWardSkill extends MagicWeaponSkill {
   public static final String SKILL_ID = "wands_arcane_ward";

   public ArcaneWardSkill() {
      super(SkillType.WANDS, 1, "Arcane Ward", "Take less damage from projectiles while holding a wand.", SkillBonusType.PASSIVE, "wands_arcane_ward");
   }

   private double reduction(int skillLevel) {
      return SkillBonus.clampDefensiveReduction(scaled(0.1, 0.001, skillLevel));
   }

   public static double applyWard(Player player, PlayerDamagedByEliteMobEvent event, double damage) {
      if (event.getProjectile() == null) {
         return damage;
      } else if (SkillBonusRegistry.getSkillById("wands_arcane_ward") instanceof ArcaneWardSkill ward && ward.wieldedBy(player)) {
         ward.incrementProcCount(player);
         SkillBonus.sendSkillActionBar(player, ward);
         return damage * (1.0 - ward.reduction(ward.skillLevel(player)));
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
