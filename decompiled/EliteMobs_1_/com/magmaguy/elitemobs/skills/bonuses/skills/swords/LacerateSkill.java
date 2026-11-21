package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

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

public class LacerateSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "swords_lacerate";
   private static final double BASE_PROC_CHANCE = 0.15;
   private static final int BLEED_DURATION_TICKS = 100;
   private static final int BLEED_TICK_INTERVAL = 20;
   private static final Map<UUID, BukkitRunnable> activeBleedEffects = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public LacerateSkill() {
      super(SkillType.SWORDS, 10, "Lacerate", "Attacks have a chance to cause bleeding, dealing damage over time.", SkillBonusType.PROC, 1, "swords_lacerate");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.15, 0.002, 0.5, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            int skillLevel = this.getPlayerSkillLevel(player);
            double bleedDamagePerTick = event.getDamageWithoutCriticalStrike() * this.getBleedPercent(skillLevel);
            this.applyBleedEffect(player, eliteEntity, bleedDamagePerTick);
         }
      }
   }

   private void applyBleedEffect(final Player player, final EliteEntity target, final double damagePerTick) {
      final UUID entityUUID = target.getLivingEntity().getUniqueId();
      if (activeBleedEffects.containsKey(entityUUID)) {
         activeBleedEffects.get(entityUUID).cancel();
      }

      BukkitRunnable bleedTask = new BukkitRunnable() {
         int ticksRemaining = 100;

         public void run() {
            if (this.ticksRemaining > 0 && target.getLivingEntity() != null && !target.getLivingEntity().isDead()) {
               LivingEntity entity = target.getLivingEntity();
               if (entity != null && !entity.isDead()) {
                  CombatDamageContext.runPlayerToEliteBypass(() -> entity.damage(damagePerTick, player));
                  entity.getWorld()
                     .spawnParticle(Particle.BLOCK, entity.getLocation().add(0.0, 1.0, 0.0), 5, 0.3, 0.3, 0.3, 0.0, Material.REDSTONE_BLOCK.createBlockData());
               }

               this.ticksRemaining -= 20;
            } else {
               LacerateSkill.activeBleedEffects.remove(entityUUID);
               this.cancel();
            }
         }
      };
      bleedTask.runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
      activeBleedEffects.put(entityUUID, bleedTask);
   }

   private double getBleedPercent(int skillLevel) {
      return scaled(0.08, 0.001, skillLevel);
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
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
      double procChance = this.getProcChance(skillLevel) * 100.0;
      double bleedPercent = this.getBleedPercent(skillLevel) * 100.0;
      double totalPercent = bleedPercent * 5.0;
      return this.applyLoreTemplates(
         Map.of(
            "procChance",
            String.format("%.1f", procChance),
            "bleedPercent",
            String.format("%.0f", bleedPercent),
            "totalPercent",
            String.format("%.0f", totalPercent)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBleedPercent(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("totalPercent", String.format("%.0f", this.getBleedPercent(skillLevel) * 500.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      for (BukkitRunnable task : activeBleedEffects.values()) {
         task.cancel();
      }

      activeBleedEffects.clear();
      activePlayers.clear();
   }
}
