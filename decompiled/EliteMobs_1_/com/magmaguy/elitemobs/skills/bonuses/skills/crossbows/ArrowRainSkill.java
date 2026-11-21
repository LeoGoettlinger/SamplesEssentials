package com.magmaguy.elitemobs.skills.bonuses.skills.crossbows;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArrowRainSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "crossbows_arrow_rain";
   private static final long BASE_COOLDOWN = 30L;
   private static final double BASE_ARROW_DAMAGE = 0.12;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> cooldownEndTimes = new ConcurrentHashMap<>();
   private static final Map<UUID, BukkitRunnable> volleys = new ConcurrentHashMap<>();

   public ArrowRainSkill() {
      super(SkillType.CROSSBOWS, 75, "Arrow Rain", "Rain arrows on the target location.", SkillBonusType.COOLDOWN, 4, "crossbows_arrow_rain");
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
   public void onActivate(final Player player, Object event) {
      if (!CombatSimulator.isTestingActive(player)) {
         if (this.isActive(player) && !this.isOnCooldown(player)) {
            if (event instanceof EliteMobDamagedByPlayerEvent damageEvent) {
               if (damageEvent.getEliteMobEntity().getLivingEntity() != null) {
                  if (damageEvent.getEntityDamageByEntityEvent() != null
                     && damageEvent.getEntityDamageByEntityEvent().getDamager() instanceof Projectile origin) {
                     if (!ItemTagger.isSecondaryArrow(origin)) {
                        int skillLevel = damageEvent.getRangedSkillLevel();
                        final double damageMultiplier = this.getArrowDamageMultiplier(skillLevel);
                        final Location targetLoc = damageEvent.getEliteMobEntity().getLivingEntity().getLocation().add(0.0, 10.0, 0.0);
                        final ItemTagger.ArrowCombatSnapshot attack = ItemTagger.snapshotArrowCombat(origin);
                        stopVolley(player.getUniqueId());
                        this.startCooldown(player, skillLevel);
                        BukkitRunnable volley = new BukkitRunnable() {
                           int count = 0;

                           public void run() {
                              if (ArrowRainSkill.volleys.get(player.getUniqueId()) == this
                                 && this.count < 5
                                 && player.isOnline()
                                 && ArrowRainSkill.this.isActive(player)
                                 && player.getWorld().equals(targetLoc.getWorld())) {
                                 try {
                                    for (int i = 0; i < 3; i++) {
                                       Location spawnLoc = targetLoc.clone()
                                          .add(ThreadLocalRandom.current().nextDouble(-2.0, 2.0), 0.0, ThreadLocalRandom.current().nextDouble(-2.0, 2.0));
                                       Arrow arrow = (Arrow)targetLoc.getWorld().spawn(spawnLoc, Arrow.class);
                                       arrow.setShooter(player);
                                       arrow.setVelocity(new Vector(0, -2, 0));
                                       arrow.setPickupStatus(PickupStatus.DISALLOWED);
                                       attack.applyToSecondary(arrow, damageMultiplier);
                                    }

                                    this.count++;
                                 } catch (Error | RuntimeException var4) {
                                    ArrowRainSkill.volleys.remove(player.getUniqueId(), this);
                                    this.cancel();
                                    throw var4;
                                 }
                              } else {
                                 ArrowRainSkill.volleys.remove(player.getUniqueId(), this);
                                 this.cancel();
                              }
                           }
                        };

                        try {
                           volley.runTaskTimer(MetadataHandler.PLUGIN, 0L, 5L);
                           volleys.put(player.getUniqueId(), volley);
                        } catch (RuntimeException var12) {
                           this.endCooldown(player);
                           throw var12;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private double getArrowDamageMultiplier(int skillLevel) {
      return scaled(0.12, 0.0018, 0.3, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      stopVolley(player.getUniqueId());
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
      return this.applyLoreTemplates(
         Map.of(
            "arrowDamage",
            String.format("%.0f", this.getArrowDamageMultiplier(skillLevel) * 100.0),
            "cooldown",
            String.valueOf(this.getCooldownSeconds(skillLevel))
         )
      );
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getArrowDamageMultiplier(skillLevel);
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("arrowDamage", String.format("%.0f", this.getArrowDamageMultiplier(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      volleys.values().forEach(BukkitRunnable::cancel);
      volleys.clear();
      cooldownEndTimes.clear();
   }

   private static void stopVolley(UUID playerId) {
      BukkitRunnable volley = volleys.remove(playerId);
      if (volley != null) {
         volley.cancel();
      }
   }
}
