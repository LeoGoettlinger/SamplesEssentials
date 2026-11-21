package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ExposeWeaknessSkill extends SkillBonus implements ProcSkill, TargetDebuffBonus {
   public static final String SKILL_ID = "swords_expose_weakness";
   private static final double BASE_PROC_CHANCE = 0.2;
   private static final double BASE_DEFENSE_REDUCTION = 0.1;
   private static final int DEBUFF_DURATION_TICKS = 100;
   private static final Map<UUID, ExposeWeaknessSkill.DebuffData> debuffedEntities = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ExposeWeaknessSkill() {
      super(SkillType.SWORDS, 25, "Expose Weakness", "Attacks have a chance to reduce the target's defense.", SkillBonusType.PROC, 2, "swords_expose_weakness");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.2, 0.003, 0.5, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity target = event.getEliteMobEntity();
         if (target != null && target.getLivingEntity() != null) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
            this.applyDebuff(target, skillLevel);
         }
      }
   }

   private void applyDebuff(EliteEntity target, int skillLevel) {
      final UUID entityUUID = target.getLivingEntity().getUniqueId();
      ExposeWeaknessSkill.DebuffData existing = debuffedEntities.get(entityUUID);
      if (existing != null) {
         existing.cancel();
      }

      double reduction = this.getDefenseReduction(skillLevel);
      BukkitRunnable expiryTask = new BukkitRunnable() {
         public void run() {
            ExposeWeaknessSkill.debuffedEntities.remove(entityUUID);
         }
      };
      expiryTask.runTaskLater(MetadataHandler.PLUGIN, 100L);
      debuffedEntities.put(entityUUID, new ExposeWeaknessSkill.DebuffData(reduction, expiryTask));
      if (target.getLivingEntity() != null) {
         target.getLivingEntity().getWorld().spawnParticle(Particle.ENCHANT, target.getLivingEntity().getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0);
      }
   }

   public static double getDamageMultiplier(UUID entityUUID) {
      ExposeWeaknessSkill.DebuffData data = debuffedEntities.get(entityUUID);
      return data == null ? 1.0 : 1.0 + data.defenseReduction;
   }

   public static boolean isDebuffed(UUID entityUUID) {
      return debuffedEntities.containsKey(entityUUID);
   }

   private double getDefenseReduction(int skillLevel) {
      return scaled(0.1, 0.002, skillLevel);
   }

   @Override
   public boolean appliesTo(LivingEntity target, Player attacker) {
      return isDebuffed(target.getUniqueId());
   }

   @Override
   public SkillType levelSource() {
      return SkillType.SWORDS;
   }

   @Override
   public double bonusFor(Player attacker, LivingEntity target, int level) {
      return getDamageMultiplier(target.getUniqueId()) - 1.0;
   }

   @Override
   public String debugLabel() {
      return "ExposeWeakness=";
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
      double proc = this.getProcChance(skillLevel) * 100.0;
      double reduction = this.getDefenseReduction(skillLevel) * 100.0;
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", proc), "reduction", String.format("%.1f", reduction)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDefenseReduction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("reduction", String.format("%.1f", this.getDefenseReduction(skillLevel) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      for (ExposeWeaknessSkill.DebuffData data : debuffedEntities.values()) {
         data.cancel();
      }

      debuffedEntities.clear();
      activePlayers.clear();
   }

   private static class DebuffData {
      final double defenseReduction;
      final BukkitRunnable expiryTask;

      DebuffData(double defenseReduction, BukkitRunnable expiryTask) {
         this.defenseReduction = defenseReduction;
         this.expiryTask = expiryTask;
      }

      void cancel() {
         if (this.expiryTask != null) {
            this.expiryTask.cancel();
         }
      }
   }
}
