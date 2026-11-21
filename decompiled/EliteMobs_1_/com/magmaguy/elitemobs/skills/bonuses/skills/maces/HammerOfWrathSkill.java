package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class HammerOfWrathSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "maces_hammer_of_wrath";
   private static final long BASE_COOLDOWN_SECONDS = 30L;
   private static final double HEALTH_THRESHOLD = 0.3;
   private static final double BASE_DAMAGE_MULTIPLIER = 3.0;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public HammerOfWrathSkill() {
      super(SkillType.MACES, 75, "Hammer of Wrath", "Deal massive damage to enemies below 30% health.", SkillBonusType.COOLDOWN, 4, "maces_hammer_of_wrath");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(15L, 30L - skillLevel / 5);
   }

   @Override
   public boolean isOnCooldown(Player player) {
      Long cooldownEnd = cooldowns.get(player.getUniqueId());
      if (cooldownEnd == null) {
         return false;
      } else if (System.currentTimeMillis() >= cooldownEnd) {
         cooldowns.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   @Override
   public void startCooldown(Player player, int skillLevel) {
      long cooldownMs = this.getCooldownSeconds(skillLevel) * 1000L;
      cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
   }

   @Override
   public long getRemainingCooldown(Player player) {
      Long cooldownEnd = cooldowns.get(player.getUniqueId());
      if (cooldownEnd == null) {
         return 0L;
      } else {
         long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
         return Math.max(0L, remaining);
      }
   }

   @Override
   public void endCooldown(Player player) {
      cooldowns.remove(player.getUniqueId());
   }

   public double checkAndApply(Player player, EliteMobDamagedByPlayerEvent event) {
      return this.activateIfEligible(player, event) ? this.getDamageMultiplier(SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES)) : 1.0;
   }

   private boolean activateIfEligible(Player player, EliteMobDamagedByPlayerEvent event) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            LivingEntity target = eliteEntity.getLivingEntity();
            double healthPercent = target.getHealth() / target.getMaxHealth();
            if (healthPercent > 0.3) {
               return false;
            } else {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
               this.activateHammer(player, target, skillLevel);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean tryActivate(Player player, Object event) {
      return event instanceof EliteMobDamagedByPlayerEvent damageEvent ? this.activateIfEligible(player, damageEvent) : false;
   }

   private void activateHammer(Player player, LivingEntity target, int skillLevel) {
      target.getWorld().spawnParticle(Particle.END_ROD, target.getLocation().add(0.0, 2.0, 0.0), 30, 0.5, 0.5, 0.5, 0.2);
      target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation().add(0.0, 1.0, 0.0), 1, 0.0, 0.0, 0.0, 0.0);
      target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.7F);
      target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0F, 0.5F);
   }

   public double getDamageMultiplier(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(3.0, 0.05, 10.0, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      cooldowns.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.removeBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of("damage", String.format("%.0f", this.getDamageMultiplier(skillLevel) * 100.0), "cooldown", String.valueOf(this.getCooldownSeconds(skillLevel)))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel) - 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("damage", String.format("%.0f", this.getDamageMultiplier(skillLevel) * 100.0), "cooldown", String.valueOf(this.getCooldownSeconds(skillLevel)))
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      cooldowns.clear();
   }
}
