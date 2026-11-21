package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ShatterSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "maces_shatter";
   private static final long BASE_COOLDOWN_SECONDS = 15L;
   private static final double AOE_RADIUS = 5.0;
   private static final int SLOW_DURATION_TICKS = 60;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public ShatterSkill() {
      super(SkillType.MACES, 25, "Shatter", "Slam the ground, damaging and slowing nearby enemies.", SkillBonusType.COOLDOWN, 2, "maces_shatter");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(8L, 15L - skillLevel / 15);
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

   @Override
   public void onActivate(Player player, Object event) {
      this.activateShatter(player);
   }

   public void activateShatter(final Player player) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
         final double damageMultiplier = this.getDamageMultiplier(skillLevel);
         final Location center = player.getLocation();
         player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.5F);
         player.getWorld().playSound(center, Sound.BLOCK_ANVIL_LAND, 1.0F, 0.7F);
         (new BukkitRunnable() {
            double radius = 0.5;
            int ticks = 0;

            public void run() {
               if (this.ticks <= 10 && !(this.radius > 5.0)) {
                  for (int i = 0; i < 20; i++) {
                     double angle = (Math.PI / 10) * i;
                     double x = center.getX() + this.radius * Math.cos(angle);
                     double z = center.getZ() + this.radius * Math.sin(angle);
                     Location particleLoc = new Location(center.getWorld(), x, center.getY() + 0.1, z);
                     center.getWorld().spawnParticle(Particle.BLOCK, particleLoc, 3, 0.1, 0.1, 0.1, 0.0, Material.STONE.createBlockData());
                  }

                  this.radius += 0.5;
                  this.ticks++;
               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
         (new BukkitRunnable() {
            public void run() {
               for (Entity entity : center.getWorld().getNearbyEntities(center, 5.0, 3.0, 5.0)) {
                  if (entity instanceof LivingEntity living && !entity.equals(player)) {
                     EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(living);
                     if (eliteEntity != null) {
                        double baseDamage = player.getAttribute(Attribute.ATTACK_DAMAGE).getValue();
                        double damage = baseDamage * damageMultiplier;
                        CombatDamageContext.runPlayerToEliteBypass(() -> living.damage(damage, player));
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                     }
                  }
               }
            }
         }).runTask(MetadataHandler.PLUGIN);
         this.startCooldown(player, skillLevel);
      }
   }

   public double getDamageMultiplier(int skillLevel) {
      return scaled(0.7, 0.015, skillLevel);
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
         Map.of(
            "damage",
            String.format("%.0f", this.getDamageMultiplier(skillLevel) * 100.0),
            "radius",
            String.valueOf(5.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
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
