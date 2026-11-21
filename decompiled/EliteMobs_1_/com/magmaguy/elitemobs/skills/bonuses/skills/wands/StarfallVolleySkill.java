package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StarfallVolleySkill extends MagicWeaponSkill implements CooldownSkill {
   public static final String SKILL_ID = "wands_starfall_volley";
   private static final long COOLDOWN_SECONDS = 20L;
   private static final int BOLTS = 8;
   private static final int MAX_TARGETS = 4;
   private static final double VOLLEY_RADIUS = 10.0;
   private static final long BOLT_INTERVAL_TICKS = 2L;
   private final Set<BukkitRunnable> volleys = ConcurrentHashMap.newKeySet();

   public StarfallVolleySkill() {
      super(
         SkillType.WANDS,
         4,
         "Starfall Volley",
         "A missile hit calls down a volley of bolts every 20 seconds.",
         SkillBonusType.COOLDOWN,
         "wands_starfall_volley"
      );
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return 20L;
   }

   private double boltFraction(int skillLevel) {
      return scaled(0.7, 0.004, skillLevel);
   }

   @Override
   public boolean tryActivate(final Player player, Object event) {
      EliteMobDamagedByPlayerEvent hit = strike(event, MagicStrike.WAND_MISSILE);
      if (hit != null && hit.getEliteMobEntity().getLivingEntity() != null) {
         LivingEntity first = hit.getEliteMobEntity().getLivingEntity();
         final List<LivingEntity> targets = new ArrayList<>();
         targets.add(first);

         for (LivingEntity enemy : enemiesNear(player, first.getLocation(), 10.0, first)) {
            if (targets.size() >= 4) {
               break;
            }

            targets.add(enemy);
         }

         int skillLevel = this.skillLevel(player);
         final double boltDamage = hit.getDamage() * this.boltFraction(skillLevel);
         BukkitRunnable volley = new BukkitRunnable() {
            private int fired = 0;

            public void run() {
               targets.removeIf(targetx -> !targetx.isValid() || targetx.isDead());
               if (this.fired < 8 && !targets.isEmpty() && player.isOnline()) {
                  LivingEntity target = targets.get(this.fired % targets.size());
                  StarfallVolleySkill.beam(player.getEyeLocation(), StarfallVolleySkill.chest(target), Particle.END_ROD);
                  StarfallVolleySkill.sideDamage(player, target, boltDamage);
                  this.fired++;
               } else {
                  StarfallVolleySkill.this.volleys.remove(this);
                  this.cancel();
               }
            }
         };
         this.volleys.add(volley);
         volley.runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
         player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0F, 1.3F);
         this.startCooldown(player, skillLevel);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of("bolts", String.valueOf(8), "boltPercent", String.format("%.0f", this.boltFraction(skillLevel) * 100.0), "cooldown", String.valueOf(20L))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.boltFraction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("bolts", String.valueOf(8), "boltPercent", String.format("%.0f", this.boltFraction(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      this.volleys.forEach(BukkitRunnable::cancel);
      this.volleys.clear();
      super.shutdown();
   }
}
