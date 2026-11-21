package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
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
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VorpalStrikeSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "swords_vorpal_strike";
   private static final double BASE_COOLDOWN = 8.0;
   private static final double BASE_DAMAGE_MULTIPLIER = 1.45;
   private static final Map<UUID, BukkitRunnable> cooldownTasks = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public VorpalStrikeSkill() {
      super(SkillType.SWORDS, 75, "Vorpal Strike", "Critical hits can deal devastating bonus damage.", SkillBonusType.COOLDOWN, 4, "swords_vorpal_strike");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      if (this.configFields != null && this.configFields.getCooldownSeconds() > 0.0) {
         return Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)));
      } else {
         double reduction = 1.0 - skillLevel * 0.003;
         return (long)Math.max(4.0, 8.0 * reduction);
      }
   }

   @Override
   public boolean isOnCooldown(Player player) {
      return cooldownTasks.containsKey(player.getUniqueId());
   }

   @Override
   public void startCooldown(final Player player, int skillLevel) {
      final UUID uuid = player.getUniqueId();
      this.endCooldown(player);
      long seconds = this.getCooldownSeconds(skillLevel);
      BukkitRunnable expiry = new BukkitRunnable() {
         public void run() {
            if (VorpalStrikeSkill.cooldownTasks.remove(uuid, this)) {
               Player p = Bukkit.getPlayer(uuid);
               if (p == player && player.isOnline() && VorpalStrikeSkill.this.isActive(player)) {
                  p.sendMessage(DungeonsConfig.getVorpalStrikeReadyMessage());
               }
            }
         }
      };
      expiry.runTaskLater(MetadataHandler.PLUGIN, seconds * 20L);
      cooldownTasks.put(uuid, expiry);
   }

   @Override
   public long getRemainingCooldown(Player player) {
      return 0L;
   }

   @Override
   public void endCooldown(Player player) {
      BukkitRunnable task = cooldownTasks.remove(player.getUniqueId());
      if (task != null) {
         task.cancel();
      }
   }

   @Override
   public void onActivate(Player player, Object event) {
      if (event instanceof EliteMobDamagedByPlayerEvent damageEvent) {
         if (damageEvent.isCriticalStrike()) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
            double multiplier = this.getDamageMultiplier(skillLevel);
            damageEvent.setDamage(damageEvent.getDamage() * multiplier);
            if (damageEvent.getEliteMobEntity().getLivingEntity() != null) {
               damageEvent.getEliteMobEntity()
                  .getLivingEntity()
                  .getWorld()
                  .spawnParticle(Particle.CRIT, damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0.0, 1.0, 0.0), 30, 0.5, 0.5, 0.5, 0.3);
            }

            this.startCooldown(player, skillLevel);
            this.incrementProcCount(player);
            SkillBonus.sendSkillActionBar(player, this);
         }
      }
   }

   private double getDamageMultiplier(int skillLevel) {
      return scaled(1.45, 0.006, 2.5, skillLevel);
   }

   @Override
   public boolean tryActivate(Player player, Object event) {
      if (event instanceof EliteMobDamagedByPlayerEvent damageEvent) {
         if (!damageEvent.isCriticalStrike()) {
            return false;
         } else if (this.isOnCooldown(player)) {
            return false;
         } else {
            this.onActivate(player, damageEvent);
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      UUID uuid = player.getUniqueId();
      activePlayers.remove(uuid);
      this.endCooldown(player);
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
      double multiplier = this.getDamageMultiplier(skillLevel);
      double cooldown = this.getCooldownSeconds(skillLevel);
      return this.applyLoreTemplates(Map.of("multiplier", String.format("%.1f", multiplier), "cooldown", String.format("%.1f", cooldown)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("multiplier", String.format("%.1f", this.getDamageMultiplier(skillLevel)), "cooldown", String.format("%d", this.getCooldownSeconds(skillLevel)))
      );
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      cooldownTasks.values().forEach(BukkitRunnable::cancel);
      cooldownTasks.clear();
      activePlayers.clear();
   }
}
