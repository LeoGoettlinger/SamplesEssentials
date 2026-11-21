package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PhoenixMantleSkill extends MagicWeaponSkill implements CooldownSkill {
   public static final String SKILL_ID = "staves_phoenix_mantle";
   private static final long COOLDOWN_SECONDS = 120L;
   private static final double SURVIVING_HEALTH = 2.0;
   private static final double BURST_RADIUS = 4.0;
   private static final int BURN_TICKS = 60;

   public PhoenixMantleSkill() {
      super(SkillType.STAVES, 4, "Phoenix Mantle", "Survive a fatal blow and burst into flame.", SkillBonusType.COOLDOWN, "staves_phoenix_mantle");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return 120L;
   }

   @Override
   public boolean triggersOnOffensiveHit() {
      return false;
   }

   public static boolean preventDeath(Player player) {
      if (SkillBonusRegistry.getSkillById("staves_phoenix_mantle") instanceof PhoenixMantleSkill mantle
         && mantle.wieldedBy(player)
         && !mantle.isOnCooldown(player)) {
         player.setHealth(2.0);

         for (LivingEntity enemy : enemiesNear(player, player.getLocation(), 4.0, null)) {
            Vector away = enemy.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);
            if (away.lengthSquared() > 1.0E-4) {
               enemy.setVelocity(away.normalize().multiply(1.2).setY(0.4));
            }

            enemy.setFireTicks(Math.max(enemy.getFireTicks(), 60));
         }

         player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0.0, 1.0, 0.0), 80, 1.5, 1.0, 1.5, 0.08);
         player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation().add(0.0, 1.0, 0.0), 1, 0.0, 0.0, 0.0, 0.0);
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1.0F, 1.4F);
         mantle.startCooldown(player, mantle.skillLevel(player));
         mantle.incrementProcCount(player);
         SkillBonus.sendSkillActionBar(player, mantle);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("cooldown", String.valueOf(120L)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return 0.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("cooldown", String.valueOf(120L)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }
}
