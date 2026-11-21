package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SkillXPBar implements Listener {
   private static final int DISPLAY_DURATION_TICKS = 100;
   private static final int ANIMATION_TICKS = 20;
   private static final Map<UUID, Map<SkillType, SkillXPBar.SkillBarData>> playerBars = new HashMap<>();

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      removeAllBars(event.getPlayer());
   }

   public static void showXPGain(Player player, SkillType skillType, long oldXP, long newXP, long xpGained) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         if (SkillsConfig.isShowXPBar()) {
            if (SkillsConfig.isWorldExcludedFromSkills(player)) {
               removeAllBars(player);
            } else {
               UUID uuid = player.getUniqueId();
               playerBars.computeIfAbsent(uuid, k -> new HashMap<>());
               Map<SkillType, SkillXPBar.SkillBarData> skillBars = playerBars.get(uuid);
               SkillXPBar.SkillBarData barData = skillBars.get(skillType);
               int oldLevel = SkillXPCalculator.levelFromTotalXP(oldXP);
               int newLevel = SkillXPCalculator.levelFromTotalXP(newXP);
               boolean leveledUp = newLevel > oldLevel;
               if (barData == null || !barData.isValid()) {
                  barData = new SkillXPBar.SkillBarData(player, skillType);
                  skillBars.put(skillType, barData);
               }

               barData.updateProgress(oldXP, newXP, xpGained, leveledUp, newLevel);
            }
         }
      }
   }

   public static void removeAllBars(Player player) {
      Map<SkillType, SkillXPBar.SkillBarData> skillBars = playerBars.remove(player.getUniqueId());
      if (skillBars != null) {
         skillBars.values().forEach(SkillXPBar.SkillBarData::destroy);
      }
   }

   public static void shutdown() {
      for (Map<SkillType, SkillXPBar.SkillBarData> skillBars : playerBars.values()) {
         skillBars.values().forEach(SkillXPBar.SkillBarData::destroy);
      }

      playerBars.clear();
   }

   private static class SkillBarData {
      private final UUID playerUUID;
      private final SkillType skillType;
      private final BossBar bossBar;
      private final long sortKey;
      private BukkitTask animationTask;
      private BukkitTask hideTask;
      private double currentDisplayProgress;
      private double targetProgress;
      private int displayLevel;

      SkillBarData(Player player, SkillType skillType) {
         this.playerUUID = player.getUniqueId();
         this.skillType = skillType;
         this.sortKey = skillType.ordinal();
         this.bossBar = Bukkit.createBossBar(getBarTitle(skillType, 1, 0L), getBarColor(skillType), BarStyle.SOLID, new BarFlag[0]);
         BossBarOrderManager.show(player, this.bossBar, this.sortKey);
         this.bossBar.setVisible(true);
         this.currentDisplayProgress = 0.0;
         this.targetProgress = 0.0;
         this.displayLevel = 1;
      }

      private Player getPlayer() {
         return Bukkit.getPlayer(this.playerUUID);
      }

      boolean isValid() {
         Player player = this.getPlayer();
         return this.bossBar != null && player != null && player.isOnline();
      }

      void updateProgress(long oldXP, long newXP, long xpGained, boolean leveledUp, int newLevel) {
         Player player = this.getPlayer();
         if (player != null && player.isOnline()) {
            if (this.animationTask != null) {
               this.animationTask.cancel();
               this.animationTask = null;
            }

            if (this.hideTask != null) {
               this.hideTask.cancel();
               this.hideTask = null;
            }

            BossBarOrderManager.show(player, this.bossBar, this.sortKey);
            this.bossBar.setVisible(true);
            double oldProgress = SkillXPCalculator.levelProgress(oldXP);
            double newProgress = SkillXPCalculator.levelProgress(newXP);
            if (leveledUp) {
               this.animateLevelUp(oldProgress, newProgress, newLevel, xpGained);
            } else {
               this.displayLevel = newLevel;
               this.currentDisplayProgress = oldProgress;
               this.targetProgress = newProgress;
               this.animateProgress(xpGained);
            }

            this.scheduleHide();
         }
      }

      private void animateLevelUp(double oldProgress, final double newProgress, final int newLevel, final long xpGained) {
         int oldLevel = newLevel - 1;
         this.displayLevel = oldLevel;
         this.currentDisplayProgress = oldProgress;
         this.bossBar.setTitle(getBarTitle(this.skillType, this.displayLevel, xpGained));
         this.animationTask = (new BukkitRunnable() {
            int tick = 0;
            boolean filledBar = false;
            boolean showedLevelUp = false;

            public void run() {
               Player player = SkillBarData.this.getPlayer();
               if (player != null && player.isOnline()) {
                  if (!this.filledBar) {
                     double progress = SkillBarData.this.currentDisplayProgress + (1.0 - SkillBarData.this.currentDisplayProgress) * (this.tick / 10.0);
                     progress = Math.min(1.0, progress);
                     SkillBarData.this.bossBar.setProgress(progress);
                     if (this.tick >= 10) {
                        this.filledBar = true;
                        this.tick = 0;
                     }
                  } else if (!this.showedLevelUp) {
                     SkillBarData.this.displayLevel = newLevel;
                     SkillBarData.this.playLevelUpEffects();
                     SkillBarData.this.bossBar.setProgress(0.0);
                     SkillBarData.this.bossBar.setTitle(SkillXPBar.SkillBarData.getLevelUpTitle(SkillBarData.this.skillType, newLevel));
                     SkillBarData.this.bossBar.setColor(BarColor.YELLOW);
                     this.showedLevelUp = true;
                     this.tick = 0;
                  } else {
                     double progress = newProgress * (this.tick / 20.0);
                     progress = Math.min(newProgress, progress);
                     SkillBarData.this.bossBar.setProgress(progress);
                     if (this.tick == 1) {
                        SkillBarData.this.bossBar.setColor(SkillXPBar.SkillBarData.getBarColor(SkillBarData.this.skillType));
                        SkillBarData.this.bossBar.setTitle(SkillXPBar.SkillBarData.getBarTitle(SkillBarData.this.skillType, newLevel, xpGained));
                     }

                     if (this.tick >= 20) {
                        SkillBarData.this.bossBar.setProgress(newProgress);
                        SkillBarData.this.currentDisplayProgress = newProgress;
                        this.cancel();
                        return;
                     }
                  }

                  this.tick++;
               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
      }

      private void animateProgress(long xpGained) {
         final double startProgress = this.currentDisplayProgress;
         final double endProgress = this.targetProgress;
         this.bossBar.setTitle(getBarTitle(this.skillType, this.displayLevel, xpGained));
         this.animationTask = (new BukkitRunnable() {
            int tick = 0;

            public void run() {
               Player player = SkillBarData.this.getPlayer();
               if (player != null && player.isOnline()) {
                  double progress = startProgress + (endProgress - startProgress) * (this.tick / 20.0);
                  progress = Math.min(Math.max(0.0, progress), 1.0);
                  SkillBarData.this.bossBar.setProgress(progress);
                  if (this.tick >= 20) {
                     SkillBarData.this.bossBar.setProgress(endProgress);
                     SkillBarData.this.currentDisplayProgress = endProgress;
                     this.cancel();
                  } else {
                     this.tick++;
                  }
               } else {
                  this.cancel();
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
      }

      private void playLevelUpEffects() {
         Player player = this.getPlayer();
         if (player != null && player.isOnline()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.2F);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            (new BukkitRunnable() {
               int tick = 0;
               double angle = 0.0;

               public void run() {
                  Player p = SkillBarData.this.getPlayer();
                  if (p != null && p.isOnline() && this.tick <= 20) {
                     for (int i = 0; i < 3; i++) {
                        double x = Math.cos(this.angle + i * 2.094) * 1.0;
                        double z = Math.sin(this.angle + i * 2.094) * 1.0;
                        double y = this.tick * 0.1;
                        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(x, y + 0.5, z), 1, 0.0, 0.0, 0.0, 0.0);
                     }

                     if (this.tick % 5 == 0) {
                        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0.0, 1.0 + this.tick * 0.05, 0.0), 10, 0.5, 0.3, 0.5, 0.0);
                     }

                     this.angle += 0.5;
                     this.tick++;
                  } else {
                     this.cancel();
                  }
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
         }
      }

      private void scheduleHide() {
         this.hideTask = (new BukkitRunnable() {
            public void run() {
               if (SkillBarData.this.bossBar != null) {
                  Player player = SkillBarData.this.getPlayer();
                  if (player != null) {
                     BossBarOrderManager.hide(player, SkillBarData.this.bossBar);
                  }

                  SkillBarData.this.bossBar.setVisible(false);
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 100L);
      }

      void destroy() {
         if (this.animationTask != null) {
            this.animationTask.cancel();
         }

         if (this.hideTask != null) {
            this.hideTask.cancel();
         }

         if (this.bossBar != null) {
            Player player = this.getPlayer();
            if (player != null) {
               BossBarOrderManager.hide(player, this.bossBar);
            } else {
               this.bossBar.removeAll();
            }
         }
      }

      private static String getBarTitle(SkillType skillType, int level, long xpGained) {
         String xpText = xpGained > 0L ? " &a(+" + formatNumber(xpGained) + " XP)" : "";
         return ChatColorConverter.convert(
            SkillsConfig.getSkillBarTitleFormat()
               .replace("$skillName", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
               .replace("$level", String.valueOf(level))
               .replace("$xpText", xpText)
         );
      }

      private static String getLevelUpTitle(SkillType skillType, int newLevel) {
         return ChatColorConverter.convert(
            SkillsConfig.getSkillLevelUpTitleFormat()
               .replace("$skillName", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
               .replace("$level", String.valueOf(newLevel))
         );
      }

      private static BarColor getBarColor(SkillType skillType) {
         return switch (skillType) {
            case ARMOR -> BarColor.BLUE;
            case SWORDS -> BarColor.RED;
            case AXES -> BarColor.RED;
            case BOWS -> BarColor.GREEN;
            case CROSSBOWS -> BarColor.GREEN;
            case TRIDENTS -> BarColor.BLUE;
            case HOES -> BarColor.PURPLE;
            case MACES -> BarColor.YELLOW;
            case SPEARS -> BarColor.WHITE;
            case STAVES -> BarColor.PURPLE;
            case WANDS -> BarColor.PINK;
         };
      }

      private static String formatNumber(long number) {
         if (number >= 1000000L) {
            return String.format("%.1fM", number / 1000000.0);
         } else {
            return number >= 1000L ? String.format("%.1fK", number / 1000.0) : String.valueOf(number);
         }
      }
   }
}
