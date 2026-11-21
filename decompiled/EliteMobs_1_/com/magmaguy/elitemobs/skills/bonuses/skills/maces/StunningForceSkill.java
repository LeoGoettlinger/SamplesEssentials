package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class StunningForceSkill extends SkillBonus {
   public static final String SKILL_ID = "maces_stunning_force";
   private static final double BASE_KNOCKBACK_MULTIPLIER = 1.5;
   private static final double BASE_ROOT_CHANCE = 0.15;
   private static final int ROOT_DURATION_TICKS = 40;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public StunningForceSkill() {
      super(SkillType.MACES, 50, "Stunning Force", "Increased knockback. Chance to root enemies.", SkillBonusType.PASSIVE, 3, "maces_stunning_force");
   }

   public double getKnockbackMultiplier(int skillLevel) {
      return scaled(1.5, 0.02, skillLevel);
   }

   public double getRootChance(int skillLevel) {
      return scaled(0.15, 0.003, 0.35, skillLevel);
   }

   public void onHit(Player player, EliteMobDamagedByPlayerEvent event) {
      if (this.isActive(player)) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
            double knockbackMultiplier = this.getKnockbackMultiplier(skillLevel);
            Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector());
            if (direction.lengthSquared() < 1.0E-6) {
               double yaw = Math.toRadians(player.getLocation().getYaw());
               direction = new Vector(-Math.sin(yaw), 0.0, Math.cos(yaw));
            }

            direction.normalize();
            direction.setY(0.3);
            target.setVelocity(direction.multiply(knockbackMultiplier));
            if (Math.random() < this.getRootChance(skillLevel)) {
               this.applyRoot(player, target);
            }
         }
      }
   }

   private void applyRoot(Player player, LivingEntity target) {
      target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 127));
      target.getWorld().spawnParticle(Particle.BLOCK, target.getLocation(), 20, 0.3, 0.1, 0.3, 0.0, Material.IRON_BARS.createBlockData());
      target.getWorld().playSound(target.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0F, 0.5F);
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
            "knockback",
            String.format("%.0f", this.getKnockbackMultiplier(skillLevel) * 100.0),
            "rootChance",
            String.format("%.1f", this.getRootChance(skillLevel) * 100.0)
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getKnockbackMultiplier(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "knockback",
            String.format("%.0f", this.getKnockbackMultiplier(skillLevel) * 100.0),
            "rootChance",
            String.format("%.1f", this.getRootChance(skillLevel) * 100.0)
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
