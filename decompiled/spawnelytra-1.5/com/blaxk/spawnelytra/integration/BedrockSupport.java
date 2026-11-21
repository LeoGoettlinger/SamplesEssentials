package com.blaxk.spawnelytra.integration;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public enum BedrockSupport {
   private static final Map<UUID, Boolean> CACHE = new ConcurrentHashMap<>();
   private static Plugin plugin;
   private static BedrockSupport.DetectionMode detectionMode = BedrockSupport.DetectionMode.UNRESOLVED;
   private static Object floodgateApi;
   private static Method floodgateIsBedrockPlayer;
   private static Object geyserApi;
   private static Method geyserConnectionByUuid;
   private static boolean supportEnabled = true;

   public static void initialize(Plugin plugin) {
      BedrockSupport.plugin = plugin;
      reloadSettings(plugin);
      resolveDetectionMode();
   }

   public static void reloadSettings(Plugin plugin) {
      BedrockSupport.plugin = plugin;
      supportEnabled = plugin.getConfig().getBoolean("bedrock.enabled", true);
   }

   private static void resolveDetectionMode() {
      if (Bukkit.getPluginManager().getPlugin("floodgate") != null) {
         try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            floodgateApi = apiClass.getMethod("getInstance").invoke(null);
            floodgateIsBedrockPlayer = apiClass.getMethod("isFloodgatePlayer", UUID.class);
            if (floodgateApi != null) {
               detectionMode = BedrockSupport.DetectionMode.FLOODGATE;
               plugin.getLogger().info("Bedrock support: detecting Bedrock players via the Floodgate API.");
               return;
            }
         } catch (Throwable var2) {
            plugin.getLogger().warning("Floodgate is installed but its API could not be accessed: " + var2.getMessage());
         }
      }

      if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null) {
         try {
            Class<?> apiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            geyserApi = apiClass.getMethod("api").invoke(null);
            geyserConnectionByUuid = apiClass.getMethod("connectionByUuid", UUID.class);
            if (geyserApi != null) {
               detectionMode = BedrockSupport.DetectionMode.GEYSER;
               plugin.getLogger().info("Bedrock support: detecting Bedrock players via the Geyser API.");
               return;
            }
         } catch (Throwable var1) {
            plugin.getLogger().warning("Geyser-Spigot is installed but its API could not be accessed: " + var1.getMessage());
         }
      }

      detectionMode = BedrockSupport.DetectionMode.UUID_HEURISTIC;
   }

   public static boolean isBedrockPlayer(Player player) {
      return player == null ? false : CACHE.computeIfAbsent(player.getUniqueId(), BedrockSupport::detect);
   }

   public static boolean isManaged(Player player) {
      return supportEnabled && isBedrockPlayer(player);
   }

   public static void forget(UUID uuid) {
      if (uuid != null) {
         CACHE.remove(uuid);
      }
   }

   private static boolean detect(UUID uuid) {
      if (detectionMode == BedrockSupport.DetectionMode.UNRESOLVED) {
         resolveDetectionMode();
      }

      switch (detectionMode) {
         case FLOODGATE:
            try {
               return (Boolean)floodgateIsBedrockPlayer.invoke(floodgateApi, uuid);
            } catch (Throwable var3) {
               return isFloodgateStyleUuid(uuid);
            }
         case GEYSER:
            try {
               return geyserConnectionByUuid.invoke(geyserApi, uuid) != null;
            } catch (Throwable var2) {
               return isFloodgateStyleUuid(uuid);
            }
         default:
            return isFloodgateStyleUuid(uuid);
      }
   }

   private static boolean isFloodgateStyleUuid(UUID uuid) {
      return uuid != null && uuid.getMostSignificantBits() == 0L;
   }

   private static enum DetectionMode {
      UNRESOLVED,
      FLOODGATE,
      GEYSER,
      UUID_HEURISTIC;
   }
}
