package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ExplosiveTipSkill extends SkillBonus implements ProcSkill {
   public static final String SKILL_ID = "crossbows_explosive_tip";
   private static final double BASE_PROC_CHANCE = 0.15;
   private static final double BASE_EXPLOSION_RADIUS = 4.0;
   private static final double BASE_EXPLOSION_DAMAGE = 0.55;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ExplosiveTipSkill() {
      super(SkillType.CROSSBOWS, 25, "Explosive Tip", "Bolts have a chance to explode on impact.", SkillBonusType.PROC, 2, "crossbows_explosive_tip");
   }

   @Override
   public double getProcChance(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateProcChance(skillLevel) : scaled(0.15, 0.002, 0.35, skillLevel);
   }

   public double getExplosionRadius(int skillLevel) {
      return scaled(4.0, 0.02, skillLevel);
   }

   @Override
   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            LivingEntity target = event.getEliteMobEntity().getLivingEntity();
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.CROSSBOWS);
            double explosionDamage = event.getDamage() * this.getExplosionDamageMultiplier(skillLevel);
            double radius = this.getExplosionRadius(skillLevel);
            target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation(), 1);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.2F);
            target.getNearbyEntities(radius, radius, radius)
               .stream()
               .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
               .forEach(e -> CombatDamageContext.runPlayerToEliteBypass(() -> ((LivingEntity)e).damage(explosionDamage, player)));
         }
      }
   }

   private double getExplosionDamageMultiplier(int skillLevel) {
      return scaled(0.55, 0.005, skillLevel);
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
            "procChance",
            String.format("%.1f", this.getProcChance(skillLevel) * 100.0),
            "aoeDamage",
            String.format("%.0f", this.getExplosionDamageMultiplier(skillLevel) * 100.0),
            "radius",
            String.format("%.1f", this.getExplosionRadius(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getExplosionDamageMultiplier(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("aoeDamage", String.format("%.0f", this.getExplosionDamageMultiplier(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
   }
}
