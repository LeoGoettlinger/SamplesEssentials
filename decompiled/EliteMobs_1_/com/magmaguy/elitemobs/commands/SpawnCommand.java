package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.HashSet;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.util.Vector;

public class SpawnCommand {
   public static void spawnEliteEntityTypeCommand(Player player, EntityType entityType, Integer level, Optional<String> powers) {
      spawnEliteEntityTypeCommand(player, getLocation(player), entityType, level, powers);
   }

   public static void spawnEliteEntityTypeCommand(
      CommandSender commandSender, EntityType entityType, String world, Vector coords, Integer level, Optional<String> powers
   ) {
      Location location = new Location(Bukkit.getWorld(world), coords.getX(), coords.getY(), coords.getZ());
      spawnEliteEntityTypeCommand(commandSender, location, entityType, level, powers);
   }

   public static void spawnEliteEntityTypeCommand(CommandSender commandSender, Location location, EntityType entityType, Integer level, Optional<String> powers) {
      if (level != null) {
         if (location == null
            || location.getWorld() == null
            || !Double.isFinite(location.getX())
            || !Double.isFinite(location.getY())
            || !Double.isFinite(location.getZ())) {
            commandSender.sendMessage(CommandMessagesConfig.getWorldNotValidMessage());
         } else if (EliteMobProperties.getPluginData(entityType) == null) {
            commandSender.sendMessage(CommandMessagesConfig.getInvalidEntityTypeMessage().replace("$type", entityType.toString()));
         } else {
            HashSet<PowersConfigFields> mobPowers = new HashSet<>();
            if (powers.isPresent()) {
               String[] powersArray = powers.get().split(" ");
               mobPowers = getPowers(powersArray, commandSender);
               if (mobPowers == null) {
                  return;
               }
            }

            if (EliteMobProperties.getPluginData(entityType).getBehavior() != null) {
               try {
                  EliteMindServiceModule.spawnBehaviorElite(location, entityType, level, SpawnReason.CUSTOM, powers.isPresent() ? mobPowers : null);
               } catch (RuntimeException var45) {
                  Logger.sendMessage(commandSender, "Could not spawn " + entityType + ": " + var45.getMessage());
               }
            } else {
               EliteEntity eliteEntity = new EliteEntity();
               eliteEntity.setLevel(level);
               eliteEntity.setNaturalEntity(true);
               LivingEntity livingEntity = (LivingEntity)location.getWorld().spawnEntity(location, entityType);
               boolean accepted = false;

               try {
                  if (!livingEntity.isValid()) {
                     return;
                  }

                  eliteEntity.setLivingEntity(livingEntity, SpawnReason.CUSTOM);
                  if (eliteEntity.getLivingEntity() != null) {
                     if (powers.isPresent()) {
                        eliteEntity.applyPowers(mobPowers);
                     } else {
                        eliteEntity.randomizePowers(EliteMobProperties.getPluginData(livingEntity));
                     }

                     accepted = true;
                     return;
                  }
               } finally {
                  if (!accepted) {
                     try {
                        eliteEntity.remove(RemovalReason.OTHER);
                     } finally {
                        livingEntity.remove();
                     }
                  }
               }
            }
         }
      }
   }

   public static void spawnCustomBossCommand(CommandSender commandSender, String fileName, String world, Vector coords) {
      try {
         Location location = new Location(Bukkit.getWorld(world), coords.getX(), coords.getY(), coords.getZ());
         CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
         if (customBossesConfigFields == null) {
            commandSender.sendMessage(CommandMessagesConfig.getFilenameNotValidMessage().replace("$filename", fileName));
            return;
         }

         CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
         customBossEntity.setSpawnLocation(location);
         customBossEntity.spawn(false);
      } catch (Exception var7) {
         commandSender.sendMessage(CommandMessagesConfig.getWorldNotValidMessage());
      }
   }

   public static void spawnCustomBossCommand(CommandSender commandSender, String fileName, String world, Vector coords, int level) {
      try {
         Location location = new Location(Bukkit.getWorld(world), coords.getX(), coords.getY(), coords.getZ());
         CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
         if (customBossesConfigFields == null) {
            commandSender.sendMessage(CommandMessagesConfig.getFilenameNotValidMessage().replace("$filename", fileName));
            return;
         }

         CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
         customBossEntity.setSpawnLocation(location);
         customBossEntity.setLevel(level);
         customBossEntity.spawn(false);
      } catch (Exception var8) {
         commandSender.sendMessage(CommandMessagesConfig.getWorldNotValidMessage());
      }
   }

   public static void spawnCustomBossCommand(Player player, String fileName, int level) {
      CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
      if (customBossesConfigFields == null) {
         player.sendMessage(CommandMessagesConfig.getFilenameNotValidMessage().replace("$filename", fileName));
      } else {
         CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
         customBossEntity.setSpawnLocation(getLocation(player));
         customBossEntity.setLevel(level);
         customBossEntity.spawn(false);
      }
   }

   public static void spawnCustomBossCommand(Player player, String fileName) {
      CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
      if (customBossesConfigFields == null) {
         player.sendMessage(CommandMessagesConfig.getFilenameNotValidMessage().replace("$filename", fileName));
      } else {
         CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
         customBossEntity.setSpawnLocation(getLocation(player));
         customBossEntity.spawn(false);
      }
   }

   private static Location getLocation(Player player) {
      return player.getTargetBlock(null, 30).getLocation().add(0.5, 1.0, 0.5);
   }

   private static HashSet<PowersConfigFields> getPowers(String[] mobPowers, CommandSender commandSender) {
      HashSet<PowersConfigFields> elitePowers = new HashSet<>();

      for (String string : mobPowers) {
         PowersConfigFields powersConfigFields = PowersConfig.getPower(string);
         if (powersConfigFields == null) {
            commandSender.sendMessage(CommandMessagesConfig.getInvalidPowerMessage().replace("$power", string));
            StringBuilder allPowers = new StringBuilder();

            for (CustomConfigFields iteratedField : ElitePower.getElitePowers().values()) {
               allPowers.append(iteratedField.getFilename()).append(", ");
            }

            allPowers.append("custom");
            commandSender.sendMessage(allPowers.toString());
            return null;
         }

         elitePowers.add(powersConfigFields);
      }

      return elitePowers;
   }
}
