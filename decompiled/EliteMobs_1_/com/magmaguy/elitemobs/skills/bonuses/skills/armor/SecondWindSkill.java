package com.magmaguy.elitemobs.skills.bonuses.skills.armor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

public class SecondWindSkill extends SkillBonus implements CooldownSkill {
   private static final double HEAL_PERCENT = 0.4;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> cooldownMap = new ConcurrentHashMap<>();
   private final Map<UUID, SecondWindSkill.PendingHit> pendingHits = new HashMap<>();
   private static final double HEALTH_THRESHOLD = 0.25;

   public SecondWindSkill() {
      super(SkillType.ARMOR, 50, "Second Wind", "Automatically heal when health drops below 25%", SkillBonusType.COOLDOWN, 3, "armor_second_wind");
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
      this.cancelPending(player);
      activePlayers.remove(player.getUniqueId());
      cooldownMap.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "healAmount", String.format("%.1f", this.getHealPercent(skillLevel) * 100.0), "cooldown", String.format("%ds", this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getHealPercent(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "healAmount", String.format("%.1f", this.getHealPercent(skillLevel) * 100.0), "cooldown", String.format("%ds", this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public void shutdown() {
      this.pendingHits.values().forEach(pending -> pending.task.cancel());
      this.pendingHits.clear();
      activePlayers.clear();
      cooldownMap.clear();
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : 60L;
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
      if (event instanceof EntityDamageEvent hit) {
         this.afterHit(player, hit);
      }
   }

   public void afterHit(Player player, EntityDamageEvent event) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         SecondWindSkill.PendingHit existing = this.pendingHits.get(player.getUniqueId());
         if (existing != null) {
            existing.hits.add(new SecondWindSkill.Hit(event, player.getHealth()));
         } else {
            SecondWindSkill.PendingHit pending = new SecondWindSkill.PendingHit(player, event);
            this.pendingHits.put(player.getUniqueId(), pending);

            try {
               pending.task = player.getServer()
                  .getScheduler()
                  .runTask(
                     MetadataHandler.PLUGIN,
                     () -> {
                        if (this.pendingHits.remove(player.getUniqueId(), pending)) {
                           if (player.isOnline()
                              && !player.isDead()
                              && this.isActive(player)
                              && !this.isOnCooldown(player)
                              && player.getWorld().getUID().equals(pending.worldId)) {
                              double health = player.getHealth();
                              if (!(health <= 0.0) && !(health / player.getMaxHealth() > 0.25)) {
                                 boolean accepted = pending.hits
                                    .stream()
                                    .anyMatch(hit -> !hit.event.isCancelled() && hit.event.getFinalDamage() > 0.0 && health < hit.healthBefore);
                                 if (accepted) {
                                    int skillLevel = this.getPlayerSkillLevel(player);
                                    double healed = Math.min(player.getMaxHealth(), health + player.getMaxHealth() * this.getHealPercent(skillLevel));
                                    if (!(healed <= health)) {
                                       player.setHealth(healed);
                                       this.startCooldown(player, skillLevel);
                                       this.incrementProcCount(player);
                                       SkillBonus.sendSkillActionBar(player, this);
                                       player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0);
                                       player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1.5F);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  );
            } catch (RuntimeException var6) {
               this.pendingHits.remove(player.getUniqueId(), pending);
               throw var6;
            }
         }
      }
   }

   public void cancelPending(Player player) {
      SecondWindSkill.PendingHit pending = this.pendingHits.remove(player.getUniqueId());
      if (pending != null && pending.task != null) {
         pending.task.cancel();
      }
   }

   private double getHealPercent(int skillLevel) {
      return this.configFields == null ? 0.4 : Math.min(1.0, Math.max(0.0, 0.2 + this.configFields.calculateValue(skillLevel) * 0.1));
   }

   private int getPlayerSkillLevel(Player player) {
      return SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
   }

   private record Hit(EntityDamageEvent event, double healthBefore) {
   }

   private static final class PendingHit {
      final UUID worldId;
      final List<SecondWindSkill.Hit> hits = new ArrayList<>();
      BukkitTask task;

      PendingHit(Player player, EntityDamageEvent event) {
         this.worldId = player.getWorld().getUID();
         this.hits.add(new SecondWindSkill.Hit(event, player.getHealth()));
      }
   }
}
