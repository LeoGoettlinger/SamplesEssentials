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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AdrenalineSurgeSkill extends SkillBonus implements CooldownSkill {
   private static final int BUFF_AMPLIFIER = 2;
   private static final int BUFF_DURATION_TICKS = 180;
   private static final double SURGE_DAMAGE_REDUCTION = 0.2;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> surgeActiveMap = new ConcurrentHashMap<>();
   private static final double HEALTH_THRESHOLD = 0.3;

   public AdrenalineSurgeSkill() {
      super(SkillType.ARMOR, 25, "Adrenaline Surge", "Gain buffs when health drops below 30%", SkillBonusType.COOLDOWN, 2, "armor_adrenaline_surge");
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
      surgeActiveMap.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of("duration", String.format("%.1fs", this.getDuration(skillLevel) / 20.0), "cooldown", String.format("%ds", this.getCooldownSeconds(skillLevel)))
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageReduction(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("cooldown", String.format("%ds", this.getCooldownSeconds(skillLevel))));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      cooldownMap.clear();
      surgeActiveMap.clear();
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : 45L;
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
      int duration = this.getDuration(skillLevel);
      int amplifier = this.configFields == null ? 2 : Math.max(0, (int)Math.floor(this.configFields.calculateValue(skillLevel)));
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, amplifier));
      player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, amplifier));
      surgeActiveMap.put(player.getUniqueId(), System.currentTimeMillis() + duration * 50L);
      player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.0);
      this.startCooldown(player, skillLevel);
   }

   public boolean isSurgeActive(Player player) {
      Long activeUntil = surgeActiveMap.get(player.getUniqueId());
      if (activeUntil == null) {
         return false;
      } else if (System.currentTimeMillis() > activeUntil) {
         surgeActiveMap.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   public double getDamageReduction(int skillLevel) {
      return clampDefensiveReduction(0.2);
   }

   public double modifyIncomingDamage(Player player, double originalDamage) {
      return !this.isSurgeActive(player) ? originalDamage : originalDamage * (1.0 - this.getDamageReduction(this.getPlayerSkillLevel(player)));
   }

   public void checkTrigger(Player player, double newHealthPercent) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         if (newHealthPercent <= 0.3) {
            this.onActivate(player, null);
            this.incrementProcCount(player);
         }
      }
   }

   private int getDuration(int skillLevel) {
      return this.configFields == null ? 180 : 100 + (int)(this.configFields.calculateValue(skillLevel) * 40.0);
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }
}
