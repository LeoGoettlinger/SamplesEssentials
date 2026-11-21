package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class DeadEyeSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "bows_dead_eye";
   private static final long BASE_COOLDOWN = 30L;
   private static final double BASE_DAMAGE_MULTIPLIER = 3.3;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> cooldownEndTimes = new ConcurrentHashMap<>();

   public DeadEyeSkill() {
      super(SkillType.BOWS, 75, "Dead Eye", "Critical shots deal massive bonus damage.", SkillBonusType.COOLDOWN, 4, "bows_dead_eye");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(15L, 30L - skillLevel / 5);
   }

   @Override
   public boolean isOnCooldown(Player player) {
      return this.getRemainingCooldown(player) > 0L;
   }

   @Override
   public void startCooldown(Player player, int skillLevel) {
      UUID uuid = player.getUniqueId();
      cooldownEndTimes.put(uuid, GameClock.getCurrentTick() + this.getCooldownSeconds(skillLevel) * 20L);
   }

   @Override
   public long getRemainingCooldown(Player player) {
      Long endTime = cooldownEndTimes.get(player.getUniqueId());
      if (endTime == null) {
         return 0L;
      } else {
         long remaining = endTime - GameClock.getCurrentTick();
         if (remaining <= 0L) {
            cooldownEndTimes.remove(player.getUniqueId(), endTime);
         }

         return Math.max(0L, (remaining + 19L) / 20L);
      }
   }

   @Override
   public void endCooldown(Player player) {
      cooldownEndTimes.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player, Object event) {
      if (event instanceof EliteMobDamagedByPlayerEvent damageEvent) {
         if (damageEvent.getEliteMobEntity().getLivingEntity() != null) {
            damageEvent.getEliteMobEntity()
               .getLivingEntity()
               .getWorld()
               .spawnParticle(
                  Particle.ENCHANTED_HIT, damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.2
               );
         }
      }
   }

   @Override
   public boolean tryActivate(Player player, Object event) {
      if (event instanceof EliteMobDamagedByPlayerEvent damageEvent) {
         if (!damageEvent.isCriticalStrike()) {
            return false;
         } else {
            this.onActivate(player, event);
            return true;
         }
      } else {
         return false;
      }
   }

   private double getDamageMultiplier(int skillLevel) {
      return scaled(3.3, 0.05, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      this.endCooldown(player);
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      this.endCooldown(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "damageMultiplier", String.format("%.1f", this.getDamageMultiplier(skillLevel)), "cooldown", String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel) - 1.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("damageMultiplier", String.format("%.1f", this.getDamageMultiplier(skillLevel))));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      cooldownEndTimes.clear();
   }
}
