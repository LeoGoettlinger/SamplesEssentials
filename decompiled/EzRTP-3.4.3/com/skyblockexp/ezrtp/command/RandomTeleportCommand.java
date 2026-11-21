package com.skyblockexp.ezrtp.command;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.command.subcommands.AddCenterSubcommand;
import com.skyblockexp.ezrtp.command.subcommands.FakeSubcommand;
import com.skyblockexp.ezrtp.command.subcommands.HeatmapSubcommand;
import com.skyblockexp.ezrtp.command.subcommands.PregenerateSubcommand;
import com.skyblockexp.ezrtp.command.subcommands.ReloadSubcommand;
import com.skyblockexp.ezrtp.command.subcommands.SetCenterSubcommand;
import com.skyblockexp.ezrtp.command.subcommands.StatsSubcommand;
import com.skyblockexp.ezrtp.command.subcommands.Subcommand;
import com.skyblockexp.ezrtp.command.subcommands.UnsafeStatsSubcommand;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.NamedCenter;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.teleport.RtpLimitSettings;
import com.skyblockexp.ezrtp.gui.FactionClaimSelectionGuiManager;
import com.skyblockexp.ezrtp.gui.RandomTeleportGuiManager;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.protection.ProtectionRegistry;
import com.skyblockexp.ezrtp.protection.WorldGuardProtectionProvider;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.ChunkyProvider;
import com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import com.skyblockexp.ezrtp.teleport.heatmap.HeatmapSimulationStore;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.PluginMessageHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RandomTeleportCommand implements CommandExecutor, TabCompleter {
   private final EzRtpPlugin plugin;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;
   private final Supplier<EzRtpConfiguration> configurationSupplier;
   private final Supplier<ProtectionRegistry> protectionRegistrySupplier;
   private final RandomTeleportGuiManager guiManager;
   private final FactionClaimSelectionGuiManager factionClaimGuiManager;
   private final RtpUsageStorage usageStorage;
   private final HeatmapSimulationStore heatmapSimulationStore;
   private final ChunkyProvider chunkyAPI;
   private final ChunkyWarmupCoordinator chunkyWarmupCoordinator;
   private final Map<String, Subcommand> subcommands = new HashMap<>();
   private final Map<String, RandomTeleportCommand.RegionOverrideCacheEntry> regionOverrideCache = new ConcurrentHashMap<>();

   public RandomTeleportCommand(
      EzRtpPlugin plugin,
      Supplier<RandomTeleportService> teleportServiceSupplier,
      Supplier<EzRtpConfiguration> configurationSupplier,
      Supplier<ProtectionRegistry> protectionRegistrySupplier,
      RandomTeleportGuiManager guiManager,
      FactionClaimSelectionGuiManager factionClaimGuiManager,
      RtpUsageStorage usageStorage,
      HeatmapSimulationStore heatmapSimulationStore,
      ChunkyProvider chunkyAPI,
      ChunkyWarmupCoordinator chunkyWarmupCoordinator
   ) {
      this.plugin = plugin;
      this.teleportServiceSupplier = teleportServiceSupplier;
      this.configurationSupplier = configurationSupplier;
      this.protectionRegistrySupplier = protectionRegistrySupplier;
      this.guiManager = guiManager;
      this.factionClaimGuiManager = factionClaimGuiManager;
      this.usageStorage = usageStorage;
      this.heatmapSimulationStore = heatmapSimulationStore;
      this.chunkyAPI = chunkyAPI;
      this.chunkyWarmupCoordinator = chunkyWarmupCoordinator;
      this.subcommands.put("reload", new ReloadSubcommand(plugin));
      this.subcommands.put("stats", new StatsSubcommand(plugin, teleportServiceSupplier));
      this.subcommands.put("unsafe-stats", new UnsafeStatsSubcommand(plugin));
      this.subcommands.put("heatmap", new HeatmapSubcommand(plugin, teleportServiceSupplier, configurationSupplier, heatmapSimulationStore));
      this.subcommands.put("fake", new FakeSubcommand(plugin, teleportServiceSupplier, configurationSupplier, heatmapSimulationStore));
      this.subcommands.put("setcenter", new SetCenterSubcommand(plugin, configurationSupplier));
      this.subcommands.put("addcenter", new AddCenterSubcommand(plugin));
      this.subcommands.put("pregenerate", new PregenerateSubcommand(plugin, chunkyAPI, chunkyWarmupCoordinator));
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      boolean skipMessage = false;
      List<String> filteredArgsList = new ArrayList<>();

      for (String arg : args) {
         if ("--skip-message".equalsIgnoreCase(arg)) {
            skipMessage = true;
         } else {
            filteredArgsList.add(arg);
         }
      }

      args = filteredArgsList.toArray(new String[0]);
      if (args.length > 0) {
         Subcommand subcommand = this.subcommands.get(args[0].toLowerCase());
         if (subcommand != null) {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
            return subcommand.execute(sender, subArgs);
         }

         if (args.length == 1) {
            if ("faction".equalsIgnoreCase(args[0])) {
               if (sender instanceof Player player) {
                  if (this.factionClaimGuiManager != null) {
                     return this.factionClaimGuiManager.openSelection(player);
                  }

                  return true;
               }

               MessageUtil.send(sender, "<red>This command may only be used by players.</red>");
               return true;
            }

            if (this.handleWorldGuardRegionTeleport(sender, args[0], skipMessage)) {
               return true;
            }

            if (this.handleNamedCenterTeleport(sender, args[0], skipMessage)) {
               return true;
            }
         }
      }

      return this.handleDefaultRtp(sender, skipMessage);
   }

   private boolean handleNamedCenterTeleport(CommandSender sender, String centerName, boolean skipMessage) {
      if (sender instanceof Player player) {
         EzRtpConfiguration configuration = this.configurationSupplier.get();
         if (configuration == null) {
            return false;
         } else {
            Optional<NamedCenter> namedCenterOptional = configuration.getNamedCenter(centerName);
            if (namedCenterOptional.isEmpty()) {
               return false;
            } else {
               NamedCenter namedCenter = namedCenterOptional.get();
               RandomTeleportSettings baseSettings = configuration.getSettingsForWorld(namedCenter.world());
               if (baseSettings == null) {
                  MessageUtil.send(player, "<red>Unable to load RTP settings for center world '<white>" + namedCenter.world() + "</white>'.</red>");
                  return true;
               } else {
                  MemoryConfiguration centerOverride = new MemoryConfiguration();
                  centerOverride.set("world", namedCenter.world());
                  centerOverride.set("center.x", namedCenter.x());
                  centerOverride.set("center.z", namedCenter.z());
                  RandomTeleportSettings centerSettings = RandomTeleportSettings.fromConfiguration(centerOverride, this.plugin.getLogger(), baseSettings);
                  if (skipMessage) {
                     centerSettings = centerSettings.withSuppressPlayerMessages(true);
                  }

                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  if (service == null) {
                     MessageUtil.send(player, "<red>Teleport service is not available right now.</red>");
                     return true;
                  } else {
                     service.teleportPlayer(player, centerSettings, TeleportReason.COMMAND);
                     return true;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   private boolean handleWorldGuardRegionTeleport(CommandSender sender, String regionId, boolean skipMessage) {
      if (sender instanceof Player player) {
         if (!this.plugin.getConfig().getBoolean("worldguard.region-command.enabled", false)) {
            return false;
         } else if (regionId != null && !regionId.isBlank()) {
            ProtectionRegistry protectionRegistry = this.protectionRegistrySupplier != null ? this.protectionRegistrySupplier.get() : null;
            WorldGuardProtectionProvider worldGuard = protectionRegistry != null ? protectionRegistry.getWorldGuardProvider() : null;
            if (worldGuard != null && worldGuard.isAvailable()) {
               String normalizedRegionId = regionId.toLowerCase(Locale.ROOT);
               Optional<WorldGuardProtectionProvider.RegionBounds> boundsOptional = worldGuard.getRegionBounds(player.getWorld(), normalizedRegionId);
               if (boundsOptional.isEmpty()) {
                  return false;
               } else {
                  EzRtpConfiguration configuration = this.configurationSupplier.get();
                  RandomTeleportSettings baseSettings = configuration != null ? configuration.getSettingsForWorld(player.getWorld().getName()) : null;
                  if (baseSettings == null) {
                     MessageUtil.send(player, "<red>EzRTP settings have not finished loading yet.</red>");
                     return true;
                  } else {
                     RandomTeleportSettings regionSettings = this.resolveRegionSettings(player, normalizedRegionId, baseSettings, boundsOptional.get());
                     if (skipMessage) {
                        regionSettings = regionSettings.withSuppressPlayerMessages(true);
                     }

                     RandomTeleportService service = this.teleportServiceSupplier.get();
                     if (service == null) {
                        MessageUtil.send(player, "<red>Teleport service is not available right now.</red>");
                        return true;
                     } else {
                        service.teleportPlayer(player, regionSettings, TeleportReason.COMMAND);
                        return true;
                     }
                  }
               }
            } else {
               MessageUtil.send(player, "<red>WorldGuard region teleport is unavailable because WorldGuard is not active.</red>");
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private RandomTeleportSettings resolveRegionSettings(
      Player player, String regionId, RandomTeleportSettings baseSettings, WorldGuardProtectionProvider.RegionBounds bounds
   ) {
      MemoryConfiguration regionBase = new MemoryConfiguration();
      regionBase.set("world", player.getWorld().getName());
      regionBase.set("center.x", bounds.centerX());
      regionBase.set("center.z", bounds.centerZ());
      regionBase.set("radius.min", 0);
      int boundedMaxRadius = Math.min(Math.max(1, baseSettings.getMaximumRadius()), bounds.maxRadiusInsideRegion());
      regionBase.set("radius.max", boundedMaxRadius);
      regionBase.set("radius.use-world-border", false);
      RandomTeleportSettings regionSettings = RandomTeleportSettings.fromConfiguration(regionBase, this.plugin.getLogger(), baseSettings);
      File overrideFile = new File(this.plugin.getDataFolder(), "worldguard/" + regionId + ".yml");
      if (!overrideFile.exists()) {
         this.regionOverrideCache.remove(regionId);
         return regionSettings;
      } else {
         YamlConfiguration overrideConfig = this.loadRegionOverrideConfiguration(regionId, overrideFile);
         return RandomTeleportSettings.fromConfiguration(overrideConfig, this.plugin.getLogger(), regionSettings);
      }
   }

   private YamlConfiguration loadRegionOverrideConfiguration(String regionId, File overrideFile) {
      long lastModified = overrideFile.lastModified();
      RandomTeleportCommand.RegionOverrideCacheEntry cached = this.regionOverrideCache.get(regionId);
      if (cached != null && cached.lastModified() == lastModified) {
         return cached.configuration();
      } else {
         YamlConfiguration loaded = YamlConfiguration.loadConfiguration(overrideFile);
         RandomTeleportCommand.RegionOverrideCacheEntry updated = new RandomTeleportCommand.RegionOverrideCacheEntry(lastModified, loaded);
         this.regionOverrideCache.put(regionId, updated);
         return loaded;
      }
   }

   private boolean handleDefaultRtp(CommandSender sender, boolean skipMessage) {
      if (sender instanceof Player player) {
         EzRtpConfiguration configuration = this.configurationSupplier.get();
         RandomTeleportSettings settings = configuration != null ? configuration.getDefaultSettings() : null;
         if (settings == null) {
            MessageUtil.send(sender, "<red>EzRTP settings have not finished loading yet.</red>");
            return true;
         } else {
            String world = player.getWorld().getName();
            String group = configuration.resolveGroup(player, world);
            boolean hasBypass = player.isOp();
            if (!hasBypass) {
               for (String perm : configuration.getBypassPermissions()) {
                  if (player.hasPermission(perm)) {
                     hasBypass = true;
                     break;
                  }
               }
            }

            if (skipMessage) {
               settings = settings.withSuppressPlayerMessages(true);
            }

            if (hasBypass) {
               if (this.guiManager != null && this.guiManager.openSelection(player)) {
                  return true;
               } else {
                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  if (service != null) {
                     service.teleportPlayer(player, settings, TeleportReason.COMMAND);
                  }

                  return true;
               }
            } else {
               RtpLimitSettings limit = configuration.getLimitSettings(world, group);
               long now = System.currentTimeMillis();
               long lastRtp = this.usageStorage.getLastRtpTime(player.getUniqueId(), world);
               int daily = this.usageStorage.getUsageCount(player.getUniqueId(), world, "daily");
               int weekly = this.usageStorage.getUsageCount(player.getUniqueId(), world, "weekly");
               boolean skipCooldownForGui = this.guiManager != null && configuration.isAllowGuiDuringCooldown();
               if (!skipCooldownForGui && limit.getCooldownSeconds() > 0 && lastRtp > 0L && now - lastRtp < limit.getCooldownSeconds() * 1000L) {
                  long wait = (limit.getCooldownSeconds() * 1000L - (now - lastRtp)) / 1000L;
                  PluginMessageHelper.sendCooldownMessage(sender, this.plugin, configuration, wait);
                  return true;
               } else if (!limit.isDisableDailyLimit() && limit.getDailyLimit() > 0 && daily >= limit.getDailyLimit()) {
                  PluginMessageHelper.sendLimitMessage(sender, this.plugin, "daily");
                  return true;
               } else if (!limit.isDisableDailyLimit() && limit.getWeeklyLimit() > 0 && weekly >= limit.getWeeklyLimit()) {
                  PluginMessageHelper.sendLimitMessage(sender, this.plugin, "weekly");
                  return true;
               } else if (this.guiManager != null && this.guiManager.openSelection(player)) {
                  return true;
               } else {
                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  if (service != null) {
                     service.teleportPlayer(player, settings, TeleportReason.COMMAND, success -> {
                        if (success) {
                           this.usageStorage.setLastRtpTime(player.getUniqueId(), world, System.currentTimeMillis());
                           this.usageStorage.incrementUsage(player.getUniqueId(), world, "daily");
                           this.usageStorage.incrementUsage(player.getUniqueId(), world, "weekly");
                           PlatformRuntimeRegistry.get().scheduler().executeAsync(() -> this.usageStorage.save());
                        }
                     });
                  }

                  return true;
               }
            }
         }
      } else {
         MessageUtil.send(sender, "<red>This command may only be used by players.</red>");
         return true;
      }
   }

   @NotNull
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
      if (args.length == 1) {
         List<String> completions = new ArrayList<>();

         for (Subcommand subcommand : this.subcommands.values()) {
            if (subcommand.getName().startsWith(args[0].toLowerCase()) && sender.hasPermission(subcommand.getPermission())) {
               completions.add(subcommand.getName());
            }
         }

         completions.addAll(this.getWorldGuardRegionCompletions(sender, args[0]));
         completions.addAll(this.getNamedCenterCompletions(args[0]));
         if ("faction".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            completions.add("faction");
         }

         completions.add("--skip-message");
         return completions;
      } else {
         if (args.length > 1) {
            Subcommand subcommandx = this.subcommands.get(args[0].toLowerCase());
            if (subcommandx != null && sender.hasPermission(subcommandx.getPermission())) {
               String[] subArgs = new String[args.length - 1];
               System.arraycopy(args, 1, subArgs, 0, subArgs.length);
               return subcommandx.tabComplete(sender, subArgs);
            }

            if (!Arrays.asList(args).contains("--skip-message")) {
               return Collections.singletonList("--skip-message");
            }
         }

         return Collections.emptyList();
      }
   }

   private List<String> getWorldGuardRegionCompletions(CommandSender sender, String input) {
      if (sender instanceof Player player) {
         if (this.plugin.getConfig().getBoolean("worldguard.region-command.enabled", false)
            && this.plugin.getConfig().getBoolean("worldguard.region-command.autocomplete", false)) {
            ProtectionRegistry protectionRegistry = this.protectionRegistrySupplier != null ? this.protectionRegistrySupplier.get() : null;
            WorldGuardProtectionProvider worldGuard = protectionRegistry != null ? protectionRegistry.getWorldGuardProvider() : null;
            if (worldGuard != null && worldGuard.isAvailable()) {
               String loweredInput = input == null ? "" : input.toLowerCase(Locale.ROOT);
               List<String> completions = new ArrayList<>();

               for (String regionId : worldGuard.getAllRegions(player.getWorld()).keySet()) {
                  if (regionId.startsWith(loweredInput)) {
                     completions.add(regionId);
                  }
               }

               return completions;
            } else {
               return Collections.emptyList();
            }
         } else {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   private List<String> getNamedCenterCompletions(String input) {
      EzRtpConfiguration configuration = this.configurationSupplier != null ? this.configurationSupplier.get() : null;
      if (configuration == null) {
         return Collections.emptyList();
      } else {
         String loweredInput = input == null ? "" : input.toLowerCase(Locale.ROOT);
         List<String> completions = new ArrayList<>();

         for (String centerName : configuration.getNamedCenterNames()) {
            if (centerName.startsWith(loweredInput)) {
               completions.add(centerName);
            }
         }

         return completions;
      }
   }

   private record RegionOverrideCacheEntry(long lastModified, YamlConfiguration configuration) {
      private RegionOverrideCacheEntry(long lastModified, YamlConfiguration configuration) {
         Objects.requireNonNull(configuration, "configuration");
         this.lastModified = lastModified;
         this.configuration = configuration;
      }
   }
}
