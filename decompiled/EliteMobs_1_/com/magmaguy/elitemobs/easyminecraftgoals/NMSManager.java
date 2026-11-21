package com.magmaguy.elitemobs.easyminecraftgoals;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityTracker;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.LongRangePathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain.PacketTerrainImpactRenderer;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactService;
import java.util.Set;
import java.util.logging.Level;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class NMSManager {
   private static final String PACKAGE = "com.magmaguy.elitemobs.easyminecraftgoals.";
   private static final String MINIMUM_SUPPORTED_MINECRAFT_VERSION = "1.21.4";
   private static final Set<String> SUPPORTED_INTERNALS = Set.of("v1_21_R3", "v1_21_R4", "v1_21_R5", "v1_21_R6", "v1_21_R7", "v26");
   public static Plugin pluginProvider;
   private static NMSAdapter adapter;
   private static boolean isEnabled = false;

   public static void initializeAdapter(Plugin plugin) {
      pluginProvider = plugin;
      String version = getServerVersion();
      if (version != null) {
         try {
            String versionName;
            if ("v1_21_R7".equals(version)) {
               if (isPaper()) {
                  versionName = "com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.NMSAdapter";
               } else {
                  versionName = "com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.NMSAdapter";
               }
            } else {
               versionName = "com.magmaguy.elitemobs.easyminecraftgoals." + version + ".NMSAdapter";
            }

            adapter = (NMSAdapter)Class.forName(versionName).getDeclaredConstructor().newInstance();
            plugin.getLogger().log(Level.INFO, "Supported server version detected: {0}", version);
            isEnabled = true;
            PacketEntityTracker.getInstance().initialize(plugin);
            adapter.initializePacketInteractionListener(plugin);

            try {
               TerrainImpactService.install(new PacketTerrainImpactRenderer(plugin, adapter));
            } catch (RuntimeException var10) {
               plugin.getLogger().log(Level.WARNING, "Packet terrain impacts are unavailable: {0}", var10.getMessage());
            }
         } catch (ClassNotFoundException var11) {
            plugin.getLogger().log(Level.SEVERE, "Class not found: {0}", var11.getMessage());
         } catch (ReflectiveOperationException var12) {
            plugin.getLogger().log(Level.SEVERE, "Error instantiating class: {0}", var12.getMessage());
         } catch (Exception var13) {
            plugin.getLogger().log(Level.SEVERE, "Unexpected error: {0}", var13.getMessage());
         } finally {
            if (!isEnabled) {
               plugin.getLogger().log(Level.SEVERE, "Server version \"{0}\" is unsupported! Please check for updates!", version);
            }
         }
      }
   }

   private static boolean isPaper() {
      try {
         Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   public static NMSAdapter getAdapter() {
      return adapter;
   }

   public static void shutdown() {
      LongRangePathfindingHandle.shutdownPlanning();
      TerrainImpactService.shutdown();
      if (adapter != null) {
         adapter.shutdownMindHost();
         adapter.shutdownPacketInteractionListener();
      }

      PacketEntityTracker.getInstance().shutdown();
      isEnabled = false;
      adapter = null;
   }

   private static String getServerVersion() {
      String packageName = Bukkit.getServer().getClass().getPackage().getName();
      String versionString = Bukkit.getServer().getVersion();
      if (packageName.contains("_R")) {
         String legacyInternals = packageName.split("\\.")[3];
         if (SUPPORTED_INTERNALS.contains(legacyInternals)) {
            return legacyInternals;
         } else {
            logUnsupportedVersion(versionString);
            return null;
         }
      } else if (!versionString.contains("-")) {
         pluginProvider.getLogger()
            .warning("Incompatible Minecraft version detected! [1] Package: " + packageName + " version: " + versionString + " ! Report this to the developer.");
         return null;
      } else {
         try {
            String justVersion = versionString.split("-")[0];
            String[] parts = justVersion.split("\\.");
            int major;
            int minor;
            if (parts[0].equals("1")) {
               major = Integer.parseInt(parts[1]);

               try {
                  minor = Integer.parseInt(parts[2]);
               } catch (Exception var8) {
                  minor = 0;
               }
            } else {
               major = Integer.parseInt(parts[0]);

               try {
                  minor = Integer.parseInt(parts[1]);
               } catch (Exception var7) {
                  minor = 0;
               }
            }

            return getInternalsFromRevision(major, minor);
         } catch (Exception var9) {
            pluginProvider.getLogger()
               .warning(
                  "Incompatible Minecraft version detected! [2] Package: " + packageName + " version: " + versionString + " ! Report this to the developer."
               );
            return null;
         }
      }
   }

   private static String getInternalsFromRevision(int major, int minor) {
      if (major == 21) {
         String versionString = "v1_21_";
         if (minor == 4) {
            return versionString + "R3";
         }

         if (minor == 5) {
            return versionString + "R4";
         }

         if (minor == 6 || minor == 7 || minor == 8) {
            return versionString + "R5";
         }

         if (minor == 9 || minor == 10) {
            return versionString + "R6";
         }

         if (minor == 11) {
            return versionString + "R7";
         }
      } else if (major >= 26) {
         return "v26";
      }

      logUnsupportedVersion(Bukkit.getServer().getVersion());
      return null;
   }

   private static void logUnsupportedVersion(String versionString) {
      pluginProvider.getLogger()
         .log(
            Level.SEVERE,
            "Unsupported Minecraft version: {0}. This build requires Minecraft {1} or newer; NMS features (packet entities, hitboxes, custom pathfinding, scoreboard number hiding) will stay disabled. Update your server or use an older release of this plugin.",
            new Object[]{versionString, "1.21.4"}
         );
   }

   @Generated
   public static boolean isEnabled() {
      return isEnabled;
   }
}
