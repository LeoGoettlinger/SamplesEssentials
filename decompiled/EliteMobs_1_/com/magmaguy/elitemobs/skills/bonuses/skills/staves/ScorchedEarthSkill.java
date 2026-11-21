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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ScorchedEarthSkill extends MagicWeaponSkill implements ProcSkill {
   public static final String SKILL_ID = "staves_scorched_earth";
   private static final double RADIUS = 2.5;
   private static final int PULSES = 4;
   private final Map<UUID, UUID> lastPatchAttack = new ConcurrentHashMap<>();
   private final Set<BukkitRunnable> patches = ConcurrentHashMap.newKeySet();

   public ScorchedEarthSkill() {
      super(SkillType.STAVES, 3, "Scorched Earth", "Fireballs can leave the ground burning.", SkillBonusType.PROC, "staves_scorched_earth");
   }

   @Override
   public boolean canProc(Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
      return event != null && !event.getMagicHit().attackId().equals(this.lastPatchAttack.get(player.getUniqueId()));
   }

   @Override
   public double getProcChance(int skillLevel) {
      return scaled(0.1, 0.002, 0.4, skillLevel);
   }

   private double pulseFraction(int skillLevel) {
      return scaled(0.15, 0.002, skillLevel);
   }

   @Override
   public void onProc(final Player player, Object context) {
      EliteMobDamagedByPlayerEvent event = strike(context, MagicStrike.STAFF_FIREBALL);
      if (event != null && event.getEliteMobEntity().getLivingEntity() != null) {
         this.lastPatchAttack.put(player.getUniqueId(), event.getMagicHit().attackId());
         final Location center = event.getEliteMobEntity().getLivingEntity().getLocation();
         final double pulseDamage = event.getDamageWithoutCriticalStrike() * this.pulseFraction(this.skillLevel(player));
         BukkitRunnable patch = new BukkitRunnable() {
            private int remaining = 4;

            public void run() {
               if (this.remaining-- > 0 && player.isOnline()) {
                  ScorchedEarthSkill.burnGround(center);

                  for (LivingEntity enemy : ScorchedEarthSkill.enemiesNear(player, center, 2.5, null)) {
                     ScorchedEarthSkill.sideDamage(player, enemy, pulseDamage);
                  }
               } else {
                  ScorchedEarthSkill.this.patches.remove(this);
                  this.cancel();
               }
            }
         };
         this.patches.add(patch);
         burnGround(center);
         patch.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
      }
   }

   private static void burnGround(Location center) {
      World world = center.getWorld();
      if (world != null) {
         for (int point = 0; point < 16; point++) {
            double angle = (Math.PI * 2) * point / 16.0;
            world.spawnParticle(Particle.FLAME, center.clone().add(Math.cos(angle) * 2.5, 0.1, Math.sin(angle) * 2.5), 1, 0.0, 0.0, 0.0, 0.0);
         }

         world.spawnParticle(Particle.LAVA, center.clone().add(0.0, 0.1, 0.0), 3, 1.25, 0.0, 1.25, 0.0);
      }
   }

   @Override
   protected void clearPlayer(UUID playerId) {
      this.lastPatchAttack.remove(playerId);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "pulsePercent",
            String.format("%.0f", this.pulseFraction(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.pulseFraction(skillLevel) * 4.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("pulsePercent", String.format("%.0f", this.pulseFraction(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      this.patches.forEach(BukkitRunnable::cancel);
      this.patches.clear();
      this.lastPatchAttack.clear();
      super.shutdown();
   }
}
