package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.InitializeConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillSystemMigration {
   private static final String MIGRATION_FLAG_FILE = "skill_system_migrated.flag";
   private static final Set<UUID> warnedPlayers = new HashSet<>();
   private static boolean migrationComplete = false;

   private SkillSystemMigration() {
   }

   public static void initialize() {
      if (SkillsConfig.isSkillSystemEnabled()) {
         File flagFile = getMigrationFlagFile();
         if (flagFile.exists()) {
            migrationComplete = true;
            Logger.info("[Skills] Skill system migration already complete.");
         } else {
            migrationComplete = false;
            createMigrationFlag();
            Logger.warn("[Skills] =================================================");
            Logger.warn("[Skills] SKILL SYSTEM ACTIVATED FOR THE FIRST TIME");
            Logger.warn("[Skills] All players will start fresh with new skill data.");
            Logger.warn("[Skills] Old guild/prestige data is preserved but unused.");
            Logger.warn("[Skills] =================================================");
         }
      }
   }

   private static File getMigrationFlagFile() {
      return new File(MetadataHandler.PLUGIN.getDataFolder(), "skill_system_migrated.flag");
   }

   private static void createMigrationFlag() {
      File flagFile = getMigrationFlagFile();

      try {
         if (!flagFile.getParentFile().exists()) {
            flagFile.getParentFile().mkdirs();
         }

         if (flagFile.createNewFile()) {
            Logger.info("[Skills] Created migration flag file.");
         }
      } catch (IOException var2) {
         Logger.warn("[Skills] Could not create migration flag file: " + var2.getMessage());
      }
   }

   public static boolean shouldWarnPlayer(Player player) {
      return migrationComplete ? false : !warnedPlayers.contains(player.getUniqueId());
   }

   public static void markPlayerWarned(Player player) {
      warnedPlayers.add(player.getUniqueId());
   }

   public static void sendMigrationWarning(Player player) {
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationSeparator());
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationTitle());
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationLine1());
      Logger.sendSimpleMessage(player, "");
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationLine2());
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationLine3());
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationLine4());
      Logger.sendSimpleMessage(player, "");
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationLine5());
      player.spigot()
         .sendMessage(
            new BaseComponent[]{
               SpigotMessage.simpleMessage(DungeonsConfig.getSkillMigrationLine6()),
               SpigotMessage.commandHoverMessage(InitializeConfig.getEmCommandDisplay(), InitializeConfig.getEmCommandHover(), "/em"),
               SpigotMessage.simpleMessage(DungeonsConfig.getSkillMigrationLine6Suffix())
            }
         );
      Logger.sendSimpleMessage(player, DungeonsConfig.getSkillMigrationSeparator());
   }

   public static void shutdown() {
      warnedPlayers.clear();
   }

   public static class MigrationEvents implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPlayerJoin(PlayerJoinEvent event) {
         if (SkillsConfig.isSkillSystemEnabled()) {
            final Player player = event.getPlayer();
            (new BukkitRunnable() {
               public void run() {
                  if (player.isOnline()) {
                     if (SkillSystemMigration.shouldWarnPlayer(player)) {
                        SkillSystemMigration.sendMigrationWarning(player);
                        SkillSystemMigration.markPlayerWarned(player);
                     }
                  }
               }
            }).runTaskLater(MetadataHandler.PLUGIN, 40L);
         }
      }
   }
}
