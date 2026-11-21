package com.magmaguy.elitemobs.thirdparty.libsdisguises;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class DisguiseEntity {
   private static int undisguiseFailureCount = 0;

   public static void disguise(String disguiseName, Entity entity, String customDisguiseData, String filename) {
      Disguise disguise = buildDisguise(disguiseName, customDisguiseData, filename, entity.getCustomName());
      if (disguise != null) {
         scheduleDisguise(disguise, entity);
      }
   }

   public static boolean disguiseNext(String disguiseName, String displayName, boolean nameVisible, String customDisguiseData, String filename) {
      Disguise disguise = buildDisguise(disguiseName, customDisguiseData, filename, displayName);
      if (disguise == null) {
         return false;
      } else {
         configureDisguiseName(disguise, displayName, nameVisible);

         try {
            DisguiseAPI.disguiseNextEntity(disguise);
            return true;
         } catch (Throwable var7) {
            Logger.warn("Failed to queue LibsDisguises disguise for " + filename + " !");
            Logger.warn("Does the installed LibsDisguises version support disguiseNextEntity?");
            return false;
         }
      }
   }

   private static Disguise buildDisguise(String disguiseName, String customDisguiseData, String filename, String entityName) {
      if (disguiseName == null) {
         return null;
      } else if (disguiseName.contains("player:")) {
         return new PlayerDisguise(disguiseName.replace("player:", ""));
      } else if (disguiseName.contains("custom")) {
         return customDisguise(disguiseName.replace("custom:", ""), customDisguiseData, filename);
      } else {
         boolean baby = false;
         if (disguiseName.contains(":baby")) {
            disguiseName = disguiseName.replace(":baby", "");
            baby = true;
         }

         DisguiseType disguiseType;
         try {
            disguiseType = DisguiseType.valueOf(disguiseName);
         } catch (Exception var7) {
            Logger.warn("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entityName + " will not have a disguise.");
            return null;
         }

         if (disguiseType.isMob()) {
            return new MobDisguise(disguiseType, !baby);
         } else if (disguiseType.isMisc()) {
            return new MiscDisguise(disguiseType);
         } else {
            Logger.warn("Disguise " + disguiseName + " is not a valid disguise name! Entity " + entityName + " will not have a disguise.");
            return null;
         }
      }
   }

   private static Disguise customDisguise(String customDisguise, String customDisguiseData, String filename) {
      Disguise disguise = DisguiseAPI.getCustomDisguise(customDisguise);

      try {
         if (disguise == null && customDisguiseData != null) {
            DisguiseAPI.addCustomDisguise(customDisguise, customDisguiseData);
            disguise = DisguiseAPI.getCustomDisguise(customDisguise);
         }

         if (disguise == null) {
            throw new NullPointerException();
         } else {
            return disguise;
         }
      } catch (Exception var5) {
         Logger.warn("Failed to set custom disguise for " + filename + " !");
         Logger.warn("Does the disguise exist? Is LibsDisguises up-to-date?");
         return null;
      }
   }

   private static void scheduleDisguise(Disguise disguise, Entity entity) {
      applyDisguise(disguise, entity);
      Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
         if (entity.isValid()) {
            applyDisguise(disguise, entity);
         }
      }, 20L);
   }

   private static void applyDisguise(Disguise disguise, Entity entity) {
      disguise.setEntity(entity);
      configureDisguiseName(
         disguise, entity.getCustomName(), DefaultConfig.isAlwaysShowNametags() || entity.isCustomNameVisible() || entity.getType().equals(EntityType.VILLAGER)
      );
      disguise.startDisguise();
   }

   private static void configureDisguiseName(Disguise disguise, String displayName, boolean nameVisible) {
      disguise.setDisguiseName(displayName);
      disguise.setDynamicName(true);
      if (disguise instanceof PlayerDisguise) {
         ((PlayerDisguise)disguise).setNameVisible(nameVisible);
      }
   }

   public static void undisguise(Entity entity) {
      if (entity != null) {
         try {
            if (!DisguiseAPI.isDisguised(entity)) {
               return;
            }

            DisguiseAPI.undisguiseToAll(entity);
            if (DisguiseAPI.isDisguised(entity)) {
               warnUndisguiseFailure(entity, null);
            }
         } catch (Throwable var2) {
            warnUndisguiseFailure(entity, var2);
         }
      }
   }

   private static void warnUndisguiseFailure(Entity entity, Throwable throwable) {
      undisguiseFailureCount++;
      if (undisguiseFailureCount <= 3 || undisguiseFailureCount % 100 == 0) {
         Logger.warn(
            "Failed to remove a LibsDisguises disguise from "
               + entity.getType()
               + " (failure #"
               + undisguiseFailureCount
               + "). LibsDisguises keeps a hard reference to every disguised entity, so repeated failures leak memory - and can keep whole unloaded worlds in RAM. Cause: "
               + (
                  throwable != null
                     ? throwable
                     : "LibsDisguises still reports the entity as disguised after undisguiseToAll (is another plugin cancelling UndisguiseEvent?)"
               )
         );
      }
   }

   public static void setDisguiseNameVisibility(boolean disguiseNameVisibility, Entity entity, String name) {
      if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
         Disguise disguise = DisguiseAPI.getDisguise(entity);
         if (disguise != null) {
            if (disguise instanceof PlayerDisguise) {
               ((PlayerDisguise)disguise).setNameVisible(disguiseNameVisibility);
            }
         }
      }
   }

   public static double getDisguiseHeight(Entity entity, double scale) {
      Disguise disguise = DisguiseAPI.getDisguise(entity);
      if (disguise == null) {
         return entity.getHeight();
      } else {
         double height = disguise.getHeight();
         return Double.isFinite(height) && height > 0.0 ? height * scale : entity.getHeight();
      }
   }
}
