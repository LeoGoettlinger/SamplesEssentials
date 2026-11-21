package com.magmaguy.elitemobs.skills.bonuses.skills.hoes;

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

public class DeathsEmbraceSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "hoes_deaths_embrace";
   private static final long BASE_COOLDOWN = 120L;
   private static final long MINIMUM_COOLDOWN = 60L;
   private static final double HEAL_PERCENT = 0.1;
   private static final double BASE_PASSIVE_BONUS = 0.05;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public DeathsEmbraceSkill() {
      super(
         SkillType.HOES, 75, "Death's Embrace", "Cheat death once per cooldown, reviving with 10% health.", SkillBonusType.COOLDOWN, 4, "hoes_deaths_embrace"
      );
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      long reduction = (long)(skillLevel * 0.4);
      return Math.max(60L, 120L - reduction);
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

   public static boolean preventDeath(Player player) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return false;
      } else {
         String mainHandName = player.getInventory().getItemInMainHand().getType().name();
         if (!mainHandName.endsWith("_HOE")) {
            return false;
         } else if (SkillBonusRegistry.getSkillById("hoes_deaths_embrace") instanceof DeathsEmbraceSkill deathsEmbrace) {
            if (deathsEmbrace.isOnCooldown(player)) {
               return false;
            } else {
               int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.HOES);
               player.setHealth(player.getMaxHealth() * 0.1);
               player.getWorld().spawnParticle(Particle.SOUL, player.getLocation(), 50, 1.0, 1.0, 1.0, 0.1);
               player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5F, 1.5F);
               deathsEmbrace.startCooldown(player, skillLevel);
               deathsEmbrace.incrementProcCount(player);
               return true;
            }
         } else {
            return false;
         }
      }
   }

   public double getPassiveDamageBonus(int skillLevel) {
      return scaled(0.05, 0.001, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      this.endCooldown(player);
      activePlayers.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.endCooldown(player);
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
            "healPercent",
            String.format("%.0f", 10.0),
            "passiveBonus",
            String.format("%.1f", this.getPassiveDamageBonus(skillLevel) * 100.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getPassiveDamageBonus(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "passiveBonus",
            String.format("%.1f", this.getPassiveDamageBonus(skillLevel) * 100.0),
            "healPercent",
            String.format("%.0f", 10.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public boolean triggersOnOffensiveHit() {
      return false;
   }

   @Override
   public void shutdown() {
      cooldowns.clear();
      activePlayers.clear();
   }
}
