package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.progression.AwardResult;
import com.magmaguy.elitemobs.antiexploit.FarmingProtection;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteSkillXpGainEvent;
import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.combatsystem.displays.BossHealthDisplay;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.MessageThrottler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkillXPHandler implements Listener {
   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onEliteMobDeath(EliteMobDeathEvent event) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         EliteEntity eliteEntity = event.getEliteEntity();
         if (!eliteEntity.isTriggeredAntiExploit()) {
            if (!eliteEntity.getDamagers().isEmpty()) {
               if (!eliteEntity.isReinforcementOrMount()) {
                  if (!(eliteEntity instanceof CustomBossEntity customBoss && !customBoss.getCustomBossesConfigFields().isDropsSkillXP())) {
                     Location deathLocation = eliteEntity.getLocation();
                     Map<UUID, Double> damageByPlayer = new HashMap<>();

                     for (Entry<Player, Double> contribution : eliteEntity.getDamagers().entrySet()) {
                        damageByPlayer.merge(contribution.getKey().getUniqueId(), contribution.getValue(), Double::sum);
                     }

                     double totalDamage = damageByPlayer.values().stream().mapToDouble(Double::doubleValue).sum();
                     if (!(totalDamage <= 0.0)) {
                        Set<Player> lockedOutPlayers = eliteEntity instanceof InstancedBossEntity instancedBoss ? instancedBoss.getLockoutPlayers() : Set.of();
                        Set<Player> meaningfulParticipants = (Set<Player>)(AdvancedCombatModule.isInitialized()
                           ? AdvancedCombatModule.get().meaningfulParticipants(eliteEntity)
                           : new LinkedHashSet<>(eliteEntity.getDamagers().keySet()));
                        double bossMultiplier = SkillXPCalculator.calculateBossXPMultiplier(
                           eliteEntity.getHealthMultiplier(), eliteEntity.getDamageMultiplier()
                        );
                        long rawClassReward = Math.max(0L, (long)(SkillXPCalculator.calculateMobXP(Math.max(1, eliteEntity.getLevel())) * bossMultiplier / 2.0));
                        Set<UUID> awardedPlayers = new HashSet<>();

                        for (Player participant : meaningfulParticipants) {
                           if (awardedPlayers.add(participant.getUniqueId())) {
                              Player player = Bukkit.getPlayer(participant.getUniqueId());
                              if (player != null && player.isOnline() && player.isValid()) {
                                 double damageDealt = damageByPlayer.getOrDefault(player.getUniqueId(), 0.0);
                                 if (!player.hasMetadata("NPC")
                                    && PlayerData.isInMemory(player.getUniqueId())
                                    && !lockedOutPlayers.contains(player)
                                    && !SkillsConfig.isWorldExcludedFromSkills(player)
                                    && (
                                       deathLocation == null
                                          || deathLocation.getWorld() == null
                                          || !SkillsConfig.isWorldExcludedFromSkills(deathLocation.getWorld().getName())
                                    )) {
                                    int rewardLevel = ScaledCombatRewardResolver.getRewardLevel(eliteEntity, player);
                                    if (eliteEntity instanceof CustomBossEntity || FarmingProtection.recordNaturalEliteKill(player, eliteEntity)) {
                                       AwardResult classAward = AdvancedCombatModule.isInitialized()
                                             && classXpInRange(player, rewardLevel, eliteEntity.isScaledCombat())
                                          ? AdvancedCombatModule.get().awardClassXp(player, rawClassReward)
                                          : null;
                                       long classXpEarned = classAward == null ? 0L : classAward.appliedXp();
                                       int effectiveMobLevel = FarmingProtection.getEffectiveMobLevelForXP(player, rewardLevel);
                                       double xpMultiplier = FarmingProtection.getXPMultiplier(player, rewardLevel);
                                       if (xpMultiplier <= 0.0) {
                                          if (classXpEarned > 0L && deathLocation != null) {
                                             BossHealthDisplay.createXPPopup(deathLocation, player, classXpEarned);
                                          }
                                       } else {
                                          if (effectiveMobLevel < rewardLevel) {
                                             this.notifyCombatLevelCap(player, rewardLevel);
                                          }

                                          long baseXP = SkillXPCalculator.calculateMobXP(effectiveMobLevel);
                                          baseXP = (long)(baseXP * bossMultiplier);
                                          double damagePercent = damageDealt / totalDamage;
                                          long earnedXP = (long)(baseXP * damagePercent);
                                          if (earnedXP <= 0L) {
                                             if (classXpEarned > 0L && deathLocation != null) {
                                                BossHealthDisplay.createXPPopup(deathLocation, player, classXpEarned);
                                             }
                                          } else {
                                             long weaponXP = this.awardWeaponXP(
                                                player, eliteEntity, earnedXP, damageDealt, rewardLevel, eliteEntity.isScaledCombat()
                                             );
                                             long armorXP = this.awardArmorXP(player, eliteEntity, earnedXP, rewardLevel, eliteEntity.isScaledCombat());
                                             long totalXPEarned = saturatedSum(saturatedSum(weaponXP, armorXP), classXpEarned);
                                             if (totalXPEarned > 0L && deathLocation != null) {
                                                BossHealthDisplay.createXPPopup(deathLocation, player, totalXPEarned);
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private long awardWeaponXP(Player player, EliteEntity eliteEntity, long playerEarnedXP, double playerDamage, int rewardLevel, boolean scaledCombat) {
      if (!PlayerData.isDataLoaded(player.getUniqueId())) {
         return 0L;
      } else {
         Map<SkillType, Double> contributions = eliteEntity.getSkillDamageContributions(player.getUniqueId());
         if (contributions.isEmpty()) {
            return 0L;
         } else {
            Map<SkillType, Long> shares = WeaponXpAttribution.distribute(playerEarnedXP, playerDamage, contributions);
            long totalAwarded = 0L;

            for (Entry<SkillType, Long> entry : shares.entrySet()) {
               totalAwarded = saturatedSum(totalAwarded, this.awardSkillXP(player, eliteEntity, entry.getKey(), entry.getValue(), rewardLevel, scaledCombat));
            }

            return totalAwarded;
         }
      }
   }

   private static boolean classXpInRange(Player player, int rewardLevel, boolean scaledCombat) {
      return FarmingProtection.isLevelRewardProtectionEnabled() && !scaledCombat
         ? AdvancedCombatModule.get()
            .profile(player.getUniqueId())
            .flatMap(snapshot -> snapshot.optionalActiveLineage())
            .map(active -> FarmingProtection.isSkillXPInRange(active.activeEffectiveLevel(), rewardLevel, false))
            .orElse(true)
         : true;
   }

   private long awardSkillXP(Player player, EliteEntity eliteEntity, SkillType skillType, long baseXP, int rewardLevel, boolean scaledCombat) {
      if (!PlayerData.isDataLoaded(player.getUniqueId())) {
         return 0L;
      } else {
         long oldXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
         int previousLevel = SkillXPCalculator.levelFromTotalXP(oldXP);
         if (FarmingProtection.isLevelRewardProtectionEnabled() && !FarmingProtection.isSkillXPInRange(previousLevel, rewardLevel, scaledCombat)) {
            this.notifySkillXPTooLow(player, skillType, rewardLevel, previousLevel);
            return 0L;
         } else {
            long xpToAdd = SkillXPCalculator.applySkillMultiplier(skillType, baseXP);
            xpToAdd = SkillXpPerks.apply(player, skillType, xpToAdd);
            EliteSkillXpGainEvent event = new EliteSkillXpGainEvent(player, skillType, eliteEntity, xpToAdd);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && event.getXp() != 0L && PlayerData.isDataLoaded(player.getUniqueId())) {
               oldXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
               if (oldXP < 0L) {
                  return 0L;
               } else {
                  previousLevel = SkillXPCalculator.levelFromTotalXP(oldXP);
                  xpToAdd = Math.min(event.getXp(), Long.MAX_VALUE - oldXP);
                  if (xpToAdd == 0L) {
                     return 0L;
                  } else {
                     long newXP = PlayerData.addSkillXP(player.getUniqueId(), skillType, xpToAdd);
                     long appliedXP = newXP - oldXP;
                     if (appliedXP <= 0L) {
                        return 0L;
                     } else {
                        SkillXPBar.showXPGain(player, skillType, oldXP, newXP, appliedXP);
                        int newLevel = SkillXPCalculator.levelFromTotalXP(newXP);
                        if (newLevel > previousLevel) {
                           this.notifyLevelUp(player, skillType, newLevel);
                        }

                        return appliedXP;
                     }
                  }
               }
            } else {
               return 0L;
            }
         }
      }
   }

   private long awardArmorXP(Player player, EliteEntity eliteEntity, long baseXP, int rewardLevel, boolean scaledCombat) {
      return !PlayerData.isDataLoaded(player.getUniqueId()) ? 0L : this.awardSkillXP(player, eliteEntity, SkillType.ARMOR, baseXP, rewardLevel, scaledCombat);
   }

   private static long saturatedSum(long first, long second) {
      return first > Long.MAX_VALUE - second ? Long.MAX_VALUE : first + second;
   }

   private void notifyLevelUp(Player player, SkillType skillType, int newLevel) {
      player.sendTitle(
         DungeonsConfig.getSkillLevelUpTitle(),
         DungeonsConfig.getSkillLevelUpSubtitle()
            .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
            .replace("$level", String.valueOf(newLevel)),
         10,
         70,
         20
      );
      player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
      player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0.0, 1.0, 0.0), 50, 0.5, 1.0, 0.5, 0.1);
      if (DungeonsConfig.isSkillLevelUpBroadcastEnabled() && newLevel % 5 == 0) {
         String announcement = DungeonsConfig.getSkillLevelUpBroadcast()
            .replace("$player", player.getName())
            .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
            .replace("$level", String.valueOf(newLevel));
         Bukkit.broadcastMessage(announcement);
      }

      CombatLevelDisplay.updateDisplay(player);
      if (skillType == SkillType.ARMOR) {
         ArmorSkillHealthBonus.updateHealthBonus(player);
      }

      DebugMessage.log(player, "Player " + player.getName() + " reached " + SkillBonusMenuConfig.getSkillTypeDisplayName(skillType) + " level " + newLevel);
   }

   private void notifyCombatLevelCap(Player player, int mobLevel) {
      int combatLevel = CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());
      int cappedLevel = FarmingProtection.getEffectiveRewardLevel(combatLevel, mobLevel);
      String message = DungeonsConfig.getSkillXpCappedMessage()
         .replace("$mobLevel", String.valueOf(mobLevel))
         .replace("$skill", "combat")
         .replace("$playerLevel", String.valueOf(combatLevel))
         .replace("$xp", String.valueOf(cappedLevel));
      player.sendMessage(ChatColorConverter.convert(message));
   }

   private void notifySkillXPTooLow(Player player, SkillType skillType, int mobLevel, int skillLevel) {
      String message = DungeonsConfig.getSkillXpNoGainMessage()
         .replace("$mobLevel", String.valueOf(mobLevel))
         .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
         .replace("$playerLevel", String.valueOf(skillLevel));
      MessageThrottler.pushNoXp(player, ChatColorConverter.convert(message));
   }
}
