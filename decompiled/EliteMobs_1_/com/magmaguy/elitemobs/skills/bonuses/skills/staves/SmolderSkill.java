package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SmolderSkill extends MagicWeaponSkill implements ProcSkill {
   public static final String SKILL_ID = "staves_smolder";
   private static final int BURN_PULSES = 4;
   private final Map<UUID, BukkitRunnable> burns = new ConcurrentHashMap<>();

   public SmolderSkill() {
      super(SkillType.STAVES, 1, "Smolder", "Fireball hits can leave the target smoldering.", SkillBonusType.PROC, "staves_smolder");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      return strike(context, MagicStrike.STAFF_FIREBALL) != null;
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.15, 0.002, 0.45, skillLevel);
   }

   private double burnFraction(int skillLevel) {
      return scaled(0.6, 0.004, skillLevel);
   }

   @Override
   public void onProc(final Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         final LivingEntity target = event.getEliteMobEntity().getLivingEntity();
         final double pulseDamage = event.getDamageWithoutCriticalStrike() * this.burnFraction(this.skillLevel(player)) / 4.0;
         BukkitRunnable previous = this.burns.remove(target.getUniqueId());
         if (previous != null) {
            previous.cancel();
         }

         BukkitRunnable burn = new BukkitRunnable() {
            private int remaining = 4;

            public void run() {
               if (this.remaining-- > 0 && target.isValid() && !target.isDead()) {
                  SmolderSkill.sideDamage(player, target, pulseDamage);
                  target.getWorld().spawnParticle(Particle.FLAME, SmolderSkill.chest(target), 8, 0.3, 0.4, 0.3, 0.01);
               } else {
                  SmolderSkill.this.burns.remove(target.getUniqueId(), this);
                  this.cancel();
               }
            }
         };
         this.burns.put(target.getUniqueId(), burn);
         burn.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
      }
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "burnPercent",
            String.format("%.0f", this.burnFraction(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.burnFraction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("burnPercent", String.format("%.0f", this.burnFraction(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      this.burns.values().forEach(BukkitRunnable::cancel);
      this.burns.clear();
      super.shutdown();
   }
}
