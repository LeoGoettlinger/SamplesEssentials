package com.magmaguy.elitemobs.skills.bonuses.skills.spears;

import com.magmaguy.elitemobs.MetadataHandler;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ImpalerSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "spears_impaler";
   private static final long BASE_COOLDOWN_SECONDS = 45L;
   private static final int PIN_DURATION_TICKS = 60;
   private static final double BASE_DAMAGE_MULTIPLIER = 4.0;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Set<UUID> pinnedEntities = ConcurrentHashMap.newKeySet();

   public ImpalerSkill() {
      super(SkillType.SPEARS, 75, "Impaler", "Pin an enemy in place, dealing massive damage.", SkillBonusType.COOLDOWN, 4, "spears_impaler");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(25L, 45L - skillLevel / 4);
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
      return this.activateIfEligible(player, event) ? this.getDamageMultiplier(SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS)) : 1.0;
   }

   private boolean activateIfEligible(Player player, EliteMobDamagedByPlayerEvent event) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         EliteEntity eliteEntity = event.getEliteMobEntity();
         if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            if (pinnedEntities.contains(eliteEntity.getLivingEntity().getUniqueId())) {
               return false;
            } else {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
               this.activateImpale(player, eliteEntity, skillLevel);
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

   private void activateImpale(Player player, EliteEntity target, int skillLevel) {
      final LivingEntity living = target.getLivingEntity();
      if (living != null) {
         final UUID targetUUID = living.getUniqueId();
         pinnedEntities.add(targetUUID);
         living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 127));
         living.getWorld().playSound(living.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 0.7F);
         living.getWorld().playSound(living.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8F, 0.5F);
         (new BukkitRunnable() {
            int ticks = 0;

            public void run() {
               if (this.ticks < 60 && !living.isDead()) {
                  living.getWorld().spawnParticle(Particle.END_ROD, living.getLocation().add(0.0, 1.0, 0.0), 5, 0.2, 0.5, 0.2, 0.02);
                  if (this.ticks % 10 == 0) {
                     living.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, living.getLocation().add(0.0, 1.0, 0.0), 3, 0.2, 0.2, 0.2, 0.1);
                  }

                  this.ticks++;
               } else {
                  ImpalerSkill.pinnedEntities.remove(targetUUID);
                  this.cancel();
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
      }
   }

   public double getDamageMultiplier(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : scaled(4.0, 0.05, 10.0, skillLevel);
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
      pinnedEntities.clear();
   }
}
