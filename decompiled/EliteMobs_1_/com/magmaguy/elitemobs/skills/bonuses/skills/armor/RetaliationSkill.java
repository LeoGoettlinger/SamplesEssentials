package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
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
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RetaliationSkill extends SkillBonus implements ProcSkill {
   private static final double REFLECT_PERCENT = 0.5;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public RetaliationSkill() {
      super(SkillType.ARMOR, 25, "Retaliation", "Chance to reflect damage back to attackers", SkillBonusType.PROC, 2, "armor_retaliation");
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      this.onDeactivate(player);
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
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "reflectDamage",
            String.format("%.1f", this.getReflectPercent(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getProcChance(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "reflectDamage",
            String.format("%.1f", this.getReflectPercent(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : 0.25;
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof Object[] params && params.length >= 2 && params[0] instanceof LivingEntity attacker && params[1] instanceof Double damage) {
         int skillLevel = this.getPlayerSkillLevel(player);
         double reflectDamage = damage * this.getReflectPercent(skillLevel);
         CombatDamageContext.runPlayerToEliteBypass(() -> attacker.damage(reflectDamage, player));
         player.getWorld().spawnParticle(Particle.CRIT, attacker.getLocation().add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.0);
      }
   }

   private double getReflectPercent(int skillLevel) {
      return 0.5;
   }

   public void onDamageTaken(Player player, LivingEntity attacker, double damage) {
      if (this.isActive(player)) {
         int skillLevel = this.getPlayerSkillLevel(player);
         if (ThreadLocalRandom.current().nextDouble() < this.getProcChance(skillLevel)) {
            this.onProc(player, new Object[]{attacker, damage});
            this.incrementProcCount(player);
         }
      }
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }
}
