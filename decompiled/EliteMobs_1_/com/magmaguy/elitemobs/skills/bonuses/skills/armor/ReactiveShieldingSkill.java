package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

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
import org.bukkit.entity.Player;

public class ReactiveShieldingSkill extends SkillBonus implements CooldownSkill {
   private static final double SHIELD_REDUCTION = 0.8;
   private static final long SHIELD_DURATION = 5000L;
   private static final long COOLDOWN_SECONDS = 20L;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> shieldActiveMap = new ConcurrentHashMap<>();
   private static final double BIG_HIT_THRESHOLD = 0.1;

   public ReactiveShieldingSkill() {
      super(
         SkillType.ARMOR,
         75,
         "Reactive Shielding",
         "Activate damage reduction shield when taking a big hit",
         SkillBonusType.COOLDOWN,
         4,
         "armor_reactive_shielding"
      );
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
      cooldownMap.remove(player.getUniqueId());
      shieldActiveMap.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "shieldReduction",
            String.format("%.1f", this.getShieldReduction(skillLevel) * 100.0),
            "shieldDuration",
            String.format("%.1fs", 5.0),
            "cooldown",
            String.format("%ds", this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getShieldReduction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "shieldReduction",
            String.format("%.1f", this.getShieldReduction(skillLevel) * 100.0),
            "cooldown",
            String.format("%ds", this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      cooldownMap.clear();
      shieldActiveMap.clear();
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : 20L;
   }

   @Override
   public boolean isOnCooldown(Player player) {
      Long cooldownEnd = cooldownMap.get(player.getUniqueId());
      if (cooldownEnd == null) {
         return false;
      } else if (System.currentTimeMillis() >= cooldownEnd) {
         cooldownMap.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   @Override
   public void startCooldown(Player player, int skillLevel) {
      long cooldownMs = this.getCooldownSeconds(skillLevel) * 1000L;
      cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldownMs);
   }

   @Override
   public long getRemainingCooldown(Player player) {
      Long cooldownEnd = cooldownMap.get(player.getUniqueId());
      if (cooldownEnd == null) {
         return 0L;
      } else {
         long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
         return Math.max(0L, remaining);
      }
   }

   @Override
   public void endCooldown(Player player) {
      cooldownMap.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player, Object event) {
      int skillLevel = this.getPlayerSkillLevel(player);
      shieldActiveMap.put(player.getUniqueId(), System.currentTimeMillis() + 5000L);
      player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 30, 1.0, 1.0, 1.0, 0.05);
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.5F, 1.5F);
      this.startCooldown(player, skillLevel);
   }

   public boolean isShieldActive(Player player) {
      Long activeUntil = shieldActiveMap.get(player.getUniqueId());
      if (activeUntil == null) {
         return false;
      } else if (System.currentTimeMillis() > activeUntil) {
         shieldActiveMap.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   public void checkTrigger(Player player, double damagePercent) {
      if (!this.isOnCooldown(player)) {
         if (damagePercent >= 0.1) {
            this.onActivate(player, null);
         }
      }
   }

   public double modifyIncomingDamage(Player player, double originalDamage) {
      if (!this.isShieldActive(player)) {
         return originalDamage;
      } else {
         int skillLevel = this.getPlayerSkillLevel(player);
         double reduction = this.getShieldReduction(skillLevel);
         return originalDamage * (1.0 - reduction);
      }
   }

   private double getShieldReduction(int skillLevel) {
      double configuredReduction = this.configFields == null ? 0.8 : 0.5 + this.configFields.calculateValue(skillLevel) * 0.25;
      return clampDefensiveReduction(configuredReduction);
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }
}
