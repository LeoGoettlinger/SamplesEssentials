package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.config.skillbonuses.premade.MacesDivineShieldConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.powers.scripts.TimedScriptStateManager;
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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class DivineShieldSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "maces_divine_shield";
   private static final long BASE_COOLDOWN_SECONDS = 120L;
   private static final int INVULN_DURATION_TICKS = 40;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public DivineShieldSkill() {
      super(SkillType.MACES, 25, "Divine Shield", "When taking fatal damage, become invulnerable briefly.", SkillBonusType.COOLDOWN, 2, "maces_divine_shield");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(60L, 120L - skillLevel / 2);
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
   }

   public static boolean preventDeath(Player player, double incomingDamage) {
      if (!activePlayers.contains(player.getUniqueId())) {
         return false;
      } else {
         try {
            if (player.getInventory().getItemInMainHand().getType() != Material.MACE) {
               return false;
            }
         } catch (NoSuchFieldError var5) {
            return false;
         }

         if (SkillBonusRegistry.getSkillById("maces_divine_shield") instanceof DivineShieldSkill divineShield) {
            if (divineShield.isOnCooldown(player)) {
               return false;
            } else if (player.getHealth() - incomingDamage <= 0.0) {
               divineShield.activateShield(player);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public void activateShield(Player player) {
      int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
      player.setHealth(Math.min(player.getMaxHealth(), 4.0));
      TimedScriptStateManager.applyInvulnerability(this, player, true, 40);
      player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0.0, 1.0, 0.0), 50, 1.0, 1.0, 1.0, 0.3);
      player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 30, 1.0, 1.0, 1.0, 0.5);
      player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.2F);
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.5F);
      SkillBonus.sendSkillActionBar(player, this);
      if (this.configFields instanceof MacesDivineShieldConfig divineConfig) {
         player.sendMessage(ChatColorConverter.convert(divineConfig.getProcMessage()));
      }

      this.startCooldown(player, skillLevel);
      this.incrementProcCount(player);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      TimedScriptStateManager.releaseProperty(this, player.getUniqueId(), "invulnerable");
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
      return this.applyLoreTemplates(Map.of("cooldown", String.valueOf(this.getCooldownSeconds(skillLevel))));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return 1.0;
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
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("cooldown", String.valueOf(this.getCooldownSeconds(skillLevel))));
   }

   @Override
   public void shutdown() {
      TimedScriptStateManager.releaseOwner(this);
      activePlayers.clear();
      cooldowns.clear();
   }
}
