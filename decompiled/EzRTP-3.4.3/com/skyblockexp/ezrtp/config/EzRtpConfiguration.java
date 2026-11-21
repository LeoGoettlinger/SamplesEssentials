package com.skyblockexp.ezrtp.config;

import com.skyblockexp.ezrtp.config.gui.FactionGuiSettings;
import com.skyblockexp.ezrtp.config.gui.GuiSettings;
import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
import com.skyblockexp.ezrtp.config.network.TeleportQueueSettings;
import com.skyblockexp.ezrtp.config.teleport.RtpLimitSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class EzRtpConfiguration {
   private final Map<String, Map<String, RtpLimitSettings>> worldGroupLimits = new LinkedHashMap<>();
   private final RtpLimitSettings defaultLimitSettings;
   private final List<String> bypassPermissions;
   private final RandomTeleportSettings defaultSettings;
   private final GuiSettings guiSettings;
   private final FactionGuiSettings factionGuiSettings;
   private final TeleportQueueSettings queueSettings;
   private final NetworkConfiguration networkConfiguration;
   private final boolean allowGuiDuringCooldown;
   private final boolean humanReadableCooldown;
   private final Map<String, NamedCenter> namedCenters;
   private final boolean suppressPlayerMessages;
   private final boolean suppressConsoleMessages;

   private EzRtpConfiguration(
      RandomTeleportSettings defaultSettings,
      GuiSettings guiSettings,
      TeleportQueueSettings queueSettings,
      NetworkConfiguration networkConfiguration,
      FactionGuiSettings factionGuiSettings,
      boolean allowGuiDuringCooldown,
      boolean humanReadableCooldown,
      Map<String, NamedCenter> namedCenters,
      boolean suppressPlayerMessages,
      boolean suppressConsoleMessages
   ) {
      this.defaultSettings = defaultSettings;
      this.guiSettings = guiSettings;
      this.queueSettings = queueSettings;
      this.networkConfiguration = networkConfiguration;
      this.factionGuiSettings = factionGuiSettings;
      this.allowGuiDuringCooldown = allowGuiDuringCooldown;
      this.humanReadableCooldown = humanReadableCooldown;
      this.namedCenters = namedCenters;
      this.suppressPlayerMessages = suppressPlayerMessages;
      this.suppressConsoleMessages = suppressConsoleMessages;
      ConfigurationSection config = defaultSettings.getConfigSection().getRoot();
      ConfigurationSection rtpLimits = config.getConfigurationSection("rtp-limits");
      if (rtpLimits != null) {
         ConfigurationSection def = rtpLimits.getConfigurationSection("default");
         this.defaultLimitSettings = parseLimitSection(def);
         ConfigurationSection worlds = rtpLimits.getConfigurationSection("worlds");
         if (worlds != null) {
            for (String world : worlds.getKeys(false)) {
               ConfigurationSection worldSec = worlds.getConfigurationSection(world);
               Map<String, RtpLimitSettings> groupMap = new LinkedHashMap<>();

               for (String group : worldSec.getKeys(false)) {
                  ConfigurationSection groupSec = worldSec.getConfigurationSection(group);
                  groupMap.put(group, parseLimitSection(groupSec));
               }

               this.worldGroupLimits.put(world, groupMap);
            }
         }

         this.bypassPermissions = rtpLimits.getStringList("bypass-permissions");
      } else {
         this.defaultLimitSettings = new RtpLimitSettings(300, 10, 50, null);
         this.bypassPermissions = Collections.emptyList();
      }
   }

   private static RtpLimitSettings parseLimitSection(ConfigurationSection section) {
      if (section == null) {
         return new RtpLimitSettings(300, 10, 50, null);
      } else {
         int cooldown = section.getInt("cooldown-seconds", 300);
         int daily = section.getInt("daily-limit", 10);
         int weekly = section.getInt("weekly-limit", 50);
         Double cost = section.contains("cost") ? section.getDouble("cost") : null;
         return new RtpLimitSettings(cooldown, daily, weekly, cost);
      }
   }

   public RtpLimitSettings getLimitSettings(String world, String group) {
      Map<String, RtpLimitSettings> groupMap = this.worldGroupLimits.get(world);
      if (groupMap != null) {
         if (group != null && groupMap.containsKey(group)) {
            return groupMap.get(group);
         }

         if (groupMap.containsKey("default")) {
            return groupMap.get("default");
         }
      }

      return this.defaultLimitSettings;
   }

   public List<String> getBypassPermissions() {
      return this.bypassPermissions;
   }

   public String resolveGroup(Player player, String world) {
      if (player != null && world != null) {
         Map<String, RtpLimitSettings> groupMap = this.worldGroupLimits.get(world);
         if (groupMap == null) {
            return null;
         } else {
            for (String groupKey : groupMap.keySet()) {
               if (groupKey.startsWith("group.")) {
                  String groupName = groupKey.substring("group.".length());
                  if (!groupName.isEmpty() && player.hasPermission("ezrtp.group." + groupName)) {
                     return groupKey;
                  }
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   public double resolveTeleportCost(Player player, RandomTeleportSettings settings) {
      if (settings == null) {
         return 0.0;
      } else {
         double fallbackCost = settings.getTeleportCost();
         if (player == null) {
            return fallbackCost;
         } else {
            String world = settings.getWorldName();
            String group = this.resolveGroup(player, world);
            Map<String, RtpLimitSettings> groupMap = this.worldGroupLimits.get(world);
            if (groupMap != null) {
               if (group != null) {
                  RtpLimitSettings groupSettings = groupMap.get(group);
                  if (groupSettings != null && groupSettings.getCost() != null) {
                     return groupSettings.getCost();
                  }
               }

               RtpLimitSettings worldDefault = groupMap.get("default");
               if (worldDefault != null && worldDefault.getCost() != null) {
                  return worldDefault.getCost();
               }
            }

            return this.defaultLimitSettings != null && this.defaultLimitSettings.getCost() != null ? this.defaultLimitSettings.getCost() : fallbackCost;
         }
      }
   }

   public RandomTeleportSettings getDefaultSettings() {
      return this.suppressPlayerMessages ? this.defaultSettings.withSuppressPlayerMessages(true) : this.defaultSettings;
   }

   public RandomTeleportSettings getSettingsForWorld(String worldName) {
      if (worldName != null && !worldName.isEmpty()) {
         for (GuiWorldOption option : this.guiSettings.getWorldOptions()) {
            if (worldName.equals(option.getSettings().getWorldName())) {
               RandomTeleportSettings s = option.getSettings();
               return this.suppressPlayerMessages ? s.withSuppressPlayerMessages(true) : s;
            }
         }

         return this.getDefaultSettings();
      } else {
         return this.getDefaultSettings();
      }
   }

   public GuiSettings getGuiSettings() {
      return this.guiSettings;
   }

   public TeleportQueueSettings getQueueSettings() {
      return this.queueSettings;
   }

   public NetworkConfiguration getNetworkConfiguration() {
      return this.networkConfiguration;
   }

   public FactionGuiSettings getFactionGuiSettings() {
      return this.factionGuiSettings;
   }

   public boolean isAllowGuiDuringCooldown() {
      return this.allowGuiDuringCooldown;
   }

   public boolean isHumanReadableCooldown() {
      return this.humanReadableCooldown;
   }

   public boolean isSuppressConsoleMessages() {
      return this.suppressConsoleMessages;
   }

   public List<String> getNamedCenterNames() {
      return new ArrayList<>(this.namedCenters.keySet());
   }

   public Optional<NamedCenter> getNamedCenter(String name) {
      return name != null && !name.isBlank() ? Optional.ofNullable(this.namedCenters.get(name.toLowerCase(Locale.ROOT))) : Optional.empty();
   }

   public static EzRtpConfiguration fromConfiguration(FileConfiguration configuration, Logger logger) {
      return fromConfigurations(
         configuration,
         configuration.getConfigurationSection("messages"),
         configuration.getConfigurationSection("gui"),
         configuration.getConfigurationSection("faction-gui"),
         configuration.getConfigurationSection("queue"),
         configuration.getConfigurationSection("network"),
         logger
      );
   }

   public static EzRtpConfiguration fromConfigurations(
      ConfigurationSection baseConfiguration,
      ConfigurationSection messagesConfiguration,
      ConfigurationSection guiConfiguration,
      ConfigurationSection factionGuiConfiguration,
      ConfigurationSection queueConfiguration,
      ConfigurationSection networkConfiguration,
      Logger logger
   ) {
      MemoryConfiguration mergedConfiguration = new MemoryConfiguration();
      if (baseConfiguration != null) {
         copySection(baseConfiguration, mergedConfiguration);
      }

      if (isSectionPopulated(messagesConfiguration)) {
         mergedConfiguration.set("messages", null);
         copySection(messagesConfiguration, mergedConfiguration.createSection("messages"));
      }

      RandomTeleportSettings defaultSettings = RandomTeleportSettings.fromConfiguration(mergedConfiguration, logger);
      NetworkConfiguration networkConfig = NetworkConfiguration.fromConfiguration(networkConfiguration, logger);
      GuiSettings guiSettings = GuiSettings.fromConfiguration(
         guiConfiguration, defaultSettings, networkConfig, defaultSettings.getRareBiomeOptimizationSettings(), logger
      );
      FactionGuiSettings factionGuiSettings = FactionGuiSettings.fromConfiguration(factionGuiConfiguration);
      TeleportQueueSettings queueSettings = TeleportQueueSettings.fromConfiguration(queueConfiguration);
      boolean allowGuiDuringCooldown = baseConfiguration != null && baseConfiguration.getBoolean("rtp-limits.allow-gui-during-cooldown", true);
      boolean humanReadableCooldown = baseConfiguration != null && baseConfiguration.getBoolean("rtp.human-readable-cooldown", true);
      Map<String, NamedCenter> namedCenters = parseNamedCenters(baseConfiguration, logger);
      boolean suppressPlayerMessages = baseConfiguration != null && baseConfiguration.getBoolean("messages.suppress-player", false);
      boolean suppressConsoleMessages = baseConfiguration != null && baseConfiguration.getBoolean("messages.suppress-console", false);
      return new EzRtpConfiguration(
         defaultSettings,
         guiSettings,
         queueSettings,
         networkConfig,
         factionGuiSettings,
         allowGuiDuringCooldown,
         humanReadableCooldown,
         namedCenters,
         suppressPlayerMessages,
         suppressConsoleMessages
      );
   }

   public static EzRtpConfiguration fromConfigurations(
      ConfigurationSection baseConfiguration,
      ConfigurationSection messagesConfiguration,
      ConfigurationSection guiConfiguration,
      ConfigurationSection queueConfiguration,
      ConfigurationSection networkConfiguration,
      Logger logger
   ) {
      return fromConfigurations(baseConfiguration, messagesConfiguration, guiConfiguration, null, queueConfiguration, networkConfiguration, logger);
   }

   private static void copySection(ConfigurationSection source, ConfigurationSection target) {
      if (source != null && target != null) {
         for (String key : source.getKeys(false)) {
            Object value = source.get(key);
            if (value instanceof ConfigurationSection) {
               ConfigurationSection existing = target.getConfigurationSection(key);
               if (existing == null) {
                  existing = target.createSection(key);
               }

               copySection((ConfigurationSection)value, existing);
            } else {
               target.set(key, value);
            }
         }
      }
   }

   private EzRtpConfiguration(
      RandomTeleportSettings defaultSettings, GuiSettings guiSettings, TeleportQueueSettings queueSettings, NetworkConfiguration networkConfiguration
   ) {
      this(
         defaultSettings,
         guiSettings,
         queueSettings,
         networkConfiguration,
         FactionGuiSettings.fromConfiguration(null),
         true,
         true,
         Collections.emptyMap(),
         false,
         false
      );
   }

   private static boolean isSectionPopulated(ConfigurationSection section) {
      return section != null && !section.getKeys(true).isEmpty();
   }

   private static Map<String, NamedCenter> parseNamedCenters(ConfigurationSection baseConfiguration, Logger logger) {
      if (baseConfiguration == null) {
         return Collections.emptyMap();
      } else {
         ConfigurationSection centersSection = baseConfiguration.getConfigurationSection("centers.named");
         if (centersSection == null) {
            return Collections.emptyMap();
         } else {
            Map<String, NamedCenter> parsed = new LinkedHashMap<>();

            for (String rawName : centersSection.getKeys(false)) {
               ConfigurationSection entry = centersSection.getConfigurationSection(rawName);
               if (entry != null) {
                  String normalizedName = rawName.toLowerCase(Locale.ROOT);
                  String world = entry.getString("world", baseConfiguration.getString("world", ""));
                  if (world != null && !world.isBlank()) {
                     int x = entry.getInt("center.x", 0);
                     int z = entry.getInt("center.z", 0);
                     parsed.put(normalizedName, new NamedCenter(rawName, world, x, z));
                  } else {
                     logger.warning("Named center '" + rawName + "' is missing a world and was skipped.");
                  }
               }
            }

            return Collections.unmodifiableMap(parsed);
         }
      }
   }
}
