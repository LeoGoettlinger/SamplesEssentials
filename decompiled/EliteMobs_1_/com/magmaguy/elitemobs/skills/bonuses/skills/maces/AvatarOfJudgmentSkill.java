package com.magmaguy.elitemobs.skills.bonuses.skills.maces;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class AvatarOfJudgmentSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "maces_avatar_of_judgment";
   private static final long BASE_COOLDOWN_SECONDS = 90L;
   private static final int BUFF_DURATION_TICKS = 200;
   public static final int REFERENCE_SKILL_LEVEL = 50;
   private static final double DAMAGE_BOOST_MULTIPLIER = 2.48;
   private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, BukkitRunnable> buffTasks = new ConcurrentHashMap<>();

   public AvatarOfJudgmentSkill() {
      super(
         SkillType.MACES,
         75,
         "Avatar of Judgment",
         "Become an avatar of divine wrath. Massive damage boost for 10 seconds.",
         SkillBonusType.COOLDOWN,
         4,
         "maces_avatar_of_judgment"
      );
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      return this.configFields != null && this.configFields.getCooldownSeconds() > 0.0
         ? Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)))
         : Math.max(60L, 90L - skillLevel / 3);
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
      this.activateAvatar(player);
   }

   public void activateAvatar(final Player player) {
      if (this.isActive(player) && !this.isOnCooldown(player)) {
         int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.MACES);
         stopBuff(player.getUniqueId());
         player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
         player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
         player.getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
         player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0F, 1.5F);
         player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0.0, 1.0, 0.0), 100, 1.0, 1.0, 1.0, 0.3);
         BukkitRunnable aura = new BukkitRunnable() {
            int ticksRemaining = 200;

            public void run() {
               if (AvatarOfJudgmentSkill.buffTasks.get(player.getUniqueId()) != this) {
                  this.cancel();
               } else if (this.ticksRemaining > 0 && player.isOnline()) {
                  if (this.ticksRemaining % 5 == 0) {
                     double angle = (200 - this.ticksRemaining) * 0.3;

                     for (int i = 0; i < 2; i++) {
                        double x = Math.cos(angle + i * Math.PI) * 0.8;
                        double z = Math.sin(angle + i * Math.PI) * 0.8;
                        player.getWorld()
                           .spawnParticle(Particle.END_ROD, player.getLocation().add(x, 1.0 + Math.sin(angle * 0.5) * 0.3, z), 1, 0.0, 0.0, 0.0, 0.0);
                     }
                  }

                  this.ticksRemaining -= 5;
               } else {
                  AvatarOfJudgmentSkill.buffTasks.remove(player.getUniqueId(), this);
                  if (player.isOnline()) {
                     ActionBarCompositor.show(player, ActionBarCompositor.Source.SKILL_FEEDBACK, DungeonsConfig.getAvatarFadesMessage());
                  }

                  this.cancel();
               }
            }
         };
         aura.runTaskTimer(MetadataHandler.PLUGIN, 0L, 5L);
         buffTasks.put(player.getUniqueId(), aura);
         this.startCooldown(player, skillLevel);
         this.incrementProcCount(player);
      }
   }

   public static boolean hasAvatarBuff(Player player) {
      return buffTasks.containsKey(player.getUniqueId());
   }

   public double getDamageBoost(int skillLevel) {
      return this.configFields != null ? this.configFields.calculateValue(skillLevel) : 2.48;
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      stopBuff(player.getUniqueId());
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
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(
         Map.of(
            "damageBoost",
            String.format("%.0f", (this.getDamageBoost(skillLevel) - 1.0) * 100.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageBoost(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of(
            "damageBoost",
            String.format("%.0f", (this.getDamageBoost(skillLevel) - 1.0) * 100.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      buffTasks.values().forEach(BukkitRunnable::cancel);
      buffTasks.clear();
      cooldowns.clear();
   }

   private static void stopBuff(UUID playerId) {
      BukkitRunnable task = buffTasks.remove(playerId);
      if (task != null) {
         task.cancel();
      }
   }
}
