package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class RepelSkill extends MagicWeaponSkill implements ProcSkill {
   public static final String SKILL_ID = "staves_repel";
   private static final int SLOW_TICKS = 40;

   public RepelSkill() {
      super(SkillType.STAVES, 2, "Repel", "Staff strikes can knock enemies back.", SkillBonusType.PROC, "staves_repel");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.STAFF_MELEE) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.25, 0.002, 0.55, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_MELEE);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         Vector away = target.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
         if (away.lengthSquared() < 1.0E-4) {
            away = player.getLocation().getDirection().setY(0);
         }

         if (away.lengthSquared() > 1.0E-4) {
            target.setVelocity(away.normalize().multiply(1.6).setY(0.35));
         }

         target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
         target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, chest(target), 1, 0.0, 0.0, 0.0, 0.0);
      }
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getProcChance(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("procChance", String.format("%.1f", this.getProcChance(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }
}
