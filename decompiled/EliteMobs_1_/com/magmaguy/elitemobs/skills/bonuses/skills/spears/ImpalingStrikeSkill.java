package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ImpalingStrikeSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "spears_impaling_strike";
   private static final double BASE_PROC_CHANCE = 0.17;
   private static final int BLEED_DURATION_TICKS = 100;
   private static final int BLEED_TICK_INTERVAL = 20;
   private static final double BASE_BLEED_DAMAGE = 0.08;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, BukkitRunnable> activeBleedTasks = new ConcurrentHashMap<>();

   public ImpalingStrikeSkill() {
      super(SkillType.SPEARS, 25, "Impaling Strike", "Chance to cause bleed damage over time.", SkillBonusType.PROC, 2, "spears_impaling_strike");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.17, 0.002, 0.35, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
            double baseDamage = event.getDamageWithoutCriticalStrike();
            double bleedDamage = baseDamage * this.getBleedDamagePercent(skillLevel);
            this.applyBleed(player, eliteEntity, bleedDamage);
         }
      }
   }

   private void applyBleed(final Player player, final EliteEntity target, final double damagePerTick) {
      final LivingEntity livingTarget = target.getLivingEntity();
      if (livingTarget != null) {
         final UUID targetUUID = livingTarget.getUniqueId();
         BukkitRunnable existingTask = activeBleedTasks.remove(targetUUID);
         if (existingTask != null) {
            existingTask.cancel();
         }

         BukkitRunnable bleedTask = new BukkitRunnable() {
            int ticksRemaining = 100;

            public void run() {
               if (ImpalingStrikeSkill.activeBleedTasks.get(targetUUID) == this
                  && this.ticksRemaining > 0
                  && target.getLivingEntity() == livingTarget
                  && livingTarget.isValid()
                  && !livingTarget.isDead()) {
                  if (this.ticksRemaining % 20 == 0) {
                     CombatDamageContext.runPlayerToEliteBypass(() -> livingTarget.damage(damagePerTick, player));
                     LivingEntity living = target.getLivingEntity();
                     if (living != null) {
                        living.getWorld()
                           .spawnParticle(
                              Particle.BLOCK, living.getLocation().add(0.0, 1.0, 0.0), 5, 0.2, 0.2, 0.2, 0.0, Material.REDSTONE_BLOCK.createBlockData()
                           );
                     }
                  }

                  this.ticksRemaining -= 20;
               } else {
                  ImpalingStrikeSkill.activeBleedTasks.remove(targetUUID, this);
                  this.cancel();
               }
            }
         };
         bleedTask.runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
         activeBleedTasks.put(targetUUID, bleedTask);
      }
   }

   public double getBleedDamagePercent(int skillLevel) {
      return scaled(0.08, 0.0014, 0.15, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "chance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "bleedDamage",
            String.format("%.0f", this.getBleedDamagePercent(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBleedDamagePercent(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("totalBleed", String.format("%.0f", this.getBleedDamagePercent(skillLevel) * 100.0 * 5.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();

      for (BukkitRunnable task : activeBleedTasks.values()) {
         task.cancel();
      }

      activeBleedTasks.clear();
   }
}
