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

public class ConcussiveBlastSkill extends MagicWeaponSkill implements ProcSkill {
   public static final String SKILL_ID = "staves_concussive_blast";
   private static final int SLOW_TICKS = 40;

   public ConcussiveBlastSkill() {
      super(SkillType.STAVES, 1, "Concussive Blast", "Fireball hits can fling targets away and slow them.", SkillBonusType.PROC, "staves_concussive_blast");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.STAFF_FIREBALL) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.1, 0.002, 0.4, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         Vector away = target.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
         if (away.lengthSquared() > 1.0E-4) {
            target.setVelocity(away.normalize().multiply(0.7).setY(0.3));
         }

         target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
         target.getWorld().spawnParticle(Particle.CLOUD, chest(target), 12, 0.4, 0.3, 0.4, 0.05);
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
