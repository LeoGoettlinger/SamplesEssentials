package com.magmaguy.elitemobs.skills.bonuses.skills.tridents;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class UndertowSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "tridents_undertow";
   private static final double BASE_PROC_CHANCE = 0.2;
   private static final double BASE_PULL_STRENGTH = 1.0;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public UndertowSkill() {
      super(SkillType.TRIDENTS, 50, "Undertow", "Trident attacks have a chance to pull targets toward the player.", SkillBonusType.PROC, 3, "tridents_undertow");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.2, 0.002, 0.4, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            int skillLevel = this.getPlayerSkillLevel(player);
            double pullStrength = this.calculatePullStrength(skillLevel);
            Vector direction = player.getLocation().toVector().subtract(target.getLocation().toVector());
            if (!(direction.lengthSquared() < 0.001)) {
               direction.normalize().multiply(pullStrength);
               direction.setY(0.2);
               target.setVelocity(direction);
            }
         }
      }
   }

   private double calculatePullStrength(int skillLevel) {
      return this.configFields != null ? 1.0 + this.configFields.calculateValue(skillLevel) : scaled(1.0, 0.04, skillLevel);
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
      double pullStrength = this.calculatePullStrength(skillLevel);
      return this.applyLoreTemplates(Map.of("procChance", String.format("%.1f", procChance), "pullStrength", String.format("%.1f", pullStrength)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.calculatePullStrength(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "pullStrength",
            String.format("%.1f", this.calculatePullStrength(skillLevel)),
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
