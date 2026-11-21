package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StormCallerSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "tridents_storm_caller";
   private static final double BASE_PROC_CHANCE = 0.15;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public StormCallerSkill() {
      super(
         SkillType.TRIDENTS,
         25,
         "Storm Caller",
         "Trident attacks have a chance to strike lightning at the target.",
         SkillBonusType.PROC,
         2,
         "tridents_storm_caller"
      );
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.15, 0.0015, 0.35, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            int skillLevel = this.getPlayerSkillLevel(player);
            if (!CombatSimulator.isTestingActive(player)) {
               target.getWorld().strikeLightningEffect(target.getLocation());
            }

            double lightningDamage = this.calculateLightningDamage(skillLevel);
            CombatDamageContext.runPlayerToEliteBypass(() -> target.damage(event.getDamage() * lightningDamage, player));
         }
      }
   }

   private double calculateLightningDamage(int skillLevel) {
      return scaled(0.5, 0.01, skillLevel);
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.TRIDENTS);
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
      double damage = this.calculateLightningDamage(skillLevel);
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", procChance), "lightningDamage", String.format("%.0f", damage * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculateLightningDamage(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "lightningDamage",
            String.format("%.0f", this.calculateLightningDamage(skillLevel) * 100.0),
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
