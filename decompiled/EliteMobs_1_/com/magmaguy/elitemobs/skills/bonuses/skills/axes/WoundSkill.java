package com.magmaguy.elitemobs.skills.bonuses.skills.axes;

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

public class WoundSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "axes_wound";
   private static final double BASE_PROC_CHANCE = 0.15;
   private static final int BLEED_DURATION = 80;
   private static final Map<UUID, BukkitRunnable> activeWounds = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public WoundSkill() {
      super(SkillType.AXES, 10, "Wound", "Attacks can inflict deep wounds that bleed heavily.", SkillBonusType.PROC, 1, "axes_wound");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.15, 0.003, 0.4, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity target = event.getEliteMobEntity();
         if (target != null && target.getLivingEntity() != null) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.AXES);
            double bleedDamagePerTick = event.getDamageWithoutCriticalStrike() * this.getBleedPercent(skillLevel);
            this.applyWound(player, target, bleedDamagePerTick);
         }
      }
   }

   private void applyWound(final Player player, final EliteEntity target, final double damagePerTick) {
      final UUID entityUUID = target.getLivingEntity().getUniqueId();
      if (activeWounds.containsKey(entityUUID)) {
         activeWounds.get(entityUUID).cancel();
      }

      BukkitRunnable woundTask = new BukkitRunnable() {
         int ticks = 0;

         public void run() {
            if (this.ticks < 80 && target.getLivingEntity() != null && !target.getLivingEntity().isDead()) {
               LivingEntity entity = target.getLivingEntity();
               CombatDamageContext.runPlayerToEliteBypass(() -> entity.damage(damagePerTick, player));
               entity.getWorld()
                  .spawnParticle(Particle.BLOCK, entity.getLocation().add(0.0, 1.0, 0.0), 8, 0.3, 0.3, 0.3, 0.0, Material.REDSTONE_BLOCK.createBlockData());
               this.ticks += 20;
            } else {
               WoundSkill.activeWounds.remove(entityUUID);
               this.cancel();
            }
         }
      };
      woundTask.runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
      activeWounds.put(entityUUID, woundTask);
   }

   private double getBleedPercent(int skillLevel) {
      return scaled(0.1, 0.0015, skillLevel);
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
      double bleedPercent = this.getBleedPercent(skillLevel) * 100.0;
      double totalPercent = bleedPercent * 4.0;
      return this.applyLoreTemplates(
         Map.of(
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
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
      return this.applyFormattedBonusTemplate(Map.of("totalPercent", String.format("%.0f", this.getBleedPercent(skillLevel) * 400.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      for (BukkitRunnable task : activeWounds.values()) {
         task.cancel();
      }

      activeWounds.clear();
      activePlayers.clear();
   }
}
