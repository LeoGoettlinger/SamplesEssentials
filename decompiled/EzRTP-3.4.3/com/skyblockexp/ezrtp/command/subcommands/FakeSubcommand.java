package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.teleport.SearchPattern;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.heatmap.HeatmapSimulationStore;
import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.CircularSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.DiamondSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.SquareSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.TriangleSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.WorldBorderCompat;
import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsClaimService;
import com.skyblockexp.teamsapi.api.TeamsService;
import com.skyblockexp.teamsapi.model.Team;
import com.skyblockexp.teamsapi.model.TeamClaim;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FakeSubcommand extends Subcommand {
   private static final int MAX_FAKE_POINTS_PER_COMMAND = 5000;
   private final EzRtpPlugin plugin;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;
   private final Supplier<EzRtpConfiguration> configurationSupplier;
   private final HeatmapSimulationStore heatmapSimulationStore;

   public FakeSubcommand(
      EzRtpPlugin plugin,
      Supplier<RandomTeleportService> teleportServiceSupplier,
      Supplier<EzRtpConfiguration> configurationSupplier,
      HeatmapSimulationStore heatmapSimulationStore
   ) {
      super("fake", "ezrtp.heatmap.fake");
      this.plugin = plugin;
      this.teleportServiceSupplier = teleportServiceSupplier;
      this.configurationSupplier = configurationSupplier;
      this.heatmapSimulationStore = heatmapSimulationStore;
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      this.handleHeatmapSimulationCommand(sender, args);
      return true;
   }

   @NotNull
   @Override
   public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!this.hasPermission(sender)) {
         return Collections.emptyList();
      } else if (args.length == 1) {
         List<String> suggestions = new ArrayList<>();
         suggestions.add("clear");
         suggestions.add("<amount>");
         return suggestions;
      } else if (args.length != 2) {
         return Collections.emptyList();
      } else if ("claims".equalsIgnoreCase(args[1])) {
         List<String> suggestions = new ArrayList<>();

         for (World world : Bukkit.getWorlds()) {
            suggestions.add(world.getName());
         }

         return suggestions;
      } else {
         List<String> suggestions = new ArrayList<>();

         for (World world : Bukkit.getWorlds()) {
            suggestions.add(world.getName());
         }

         return suggestions;
      }
   }

   private void handleHeatmapSimulationCommand(CommandSender sender, String[] args) {
      if (!this.hasPermission(sender)) {
         MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.COMMAND_NO_PERMISSION));
      } else if (this.heatmapSimulationStore == null) {
         MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_STORE_MISSING));
      } else if (args.length < 1) {
         MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_USAGE));
      } else {
         boolean claimsMode = args.length >= 2 && "claims".equalsIgnoreCase(args[1]);
         String worldArgument = claimsMode ? (args.length >= 3 ? args[2] : null) : (args.length >= 2 ? args[1] : null);
         if (worldArgument != null && !worldArgument.isBlank()) {
            boolean var35 = true;
         } else {
            boolean var10000 = false;
         }

         World targetWorld = claimsMode ? null : this.resolveSimulationWorld(sender, worldArgument);
         if (targetWorld != null || claimsMode) {
            String action = args[0];
            if ("clear".equalsIgnoreCase(action)) {
               int cleared = this.heatmapSimulationStore.clearWorld(targetWorld.getName());
               MessageUtil.send(
                  sender,
                  this.plugin
                     .getMessageProvider()
                     .format(
                        MessageKey.HEATMAP_SIMULATION_CLEARED,
                        Map.of("count", String.valueOf(cleared), "s", cleared == 1 ? "" : "s", "world", targetWorld.getName())
                     )
               );
            } else {
               int amount;
               try {
                  amount = Integer.parseInt(args[0]);
               } catch (NumberFormatException var30) {
                  MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_INVALID_AMOUNT, Map.of("amount", args[0])));
                  return;
               }

               if (amount <= 0) {
                  MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_AMOUNT_NEGATIVE));
               } else {
                  int commandLimit = Math.min(5000, this.heatmapSimulationStore.getPerWorldCapacity());
                  if (amount > commandLimit) {
                     MessageUtil.send(
                        sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_AMOUNT_TOO_LARGE, Map.of("limit", String.valueOf(commandLimit)))
                     );
                  } else if (claimsMode) {
                     this.handleFactionClaimSimulation(sender, amount, worldArgument);
                  } else {
                     EzRtpConfiguration configuration = this.configurationSupplier.get();
                     if (configuration == null) {
                        MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_CONFIG_MISSING));
                     } else {
                        RandomTeleportSettings settings = configuration.getSettingsForWorld(targetWorld.getName());
                        if (settings == null) {
                           MessageUtil.send(
                              sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_SETTINGS_MISSING, Map.of("world", targetWorld.getName()))
                           );
                        } else {
                           BiomeSearchStrategy strategy = this.resolveSearchStrategy(settings.getSearchPattern());
                           int minRadius = Math.max(0, settings.getMinimumRadius());
                           int maxRadius = Math.max(minRadius, this.resolveMaximumRadius(targetWorld, settings));
                           int centerX = settings.getCenterX();
                           int centerZ = settings.getCenterZ();
                           if (settings.useWorldBorderRadius()) {
                              Optional<Double> radius = WorldBorderCompat.getBorderRadius(targetWorld);
                              if (radius.isPresent()) {
                                 int resolved = (int)Math.floor(radius.get());
                                 maxRadius = Math.max(minRadius, resolved);
                              }
                           }

                           double sampleY = this.resolveSimulationY(targetWorld, settings);
                           int ASYNC_THRESHOLD = 200;
                           if (amount >= 200) {
                              MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_HINT));
                              int maxRadiusFinal = maxRadius;
                              PlatformRuntimeRegistry.get()
                                 .scheduler()
                                 .executeAsync(
                                    () -> {
                                       List<int[]> coords = new ArrayList<>(amount);

                                       for (int i = 0; i < amount; i++) {
                                          int[] c = strategy.generateCandidateCoordinates(
                                             targetWorld, centerX, centerZ, minRadius, maxRadiusFinal, settings.getBiomeInclude(), null
                                          );
                                          if (c != null && c.length >= 2) {
                                             coords.add(new int[]{c[0], c[1]});
                                          }
                                       }

                                       PlatformRuntimeRegistry.get()
                                          .scheduler()
                                          .executeGlobal(
                                             () -> {
                                                List<Location> generatedLocations = new ArrayList<>(coords.size());

                                                for (int[] cx : coords) {
                                                   generatedLocations.add(new Location(targetWorld, cx[0], sampleY, cx[1]));
                                                }

                                                int insertedx = this.heatmapSimulationStore.addSamples(targetWorld.getName(), generatedLocations);
                                                int totalSimulatedx = this.heatmapSimulationStore.getSamples(targetWorld.getName()).size();
                                                MessageUtil.send(
                                                   sender,
                                                   this.plugin
                                                      .getMessageProvider()
                                                      .format(
                                                         MessageKey.HEATMAP_SIMULATION_ADDED,
                                                         Map.of(
                                                            "count",
                                                            String.valueOf(insertedx),
                                                            "s",
                                                            insertedx == 1 ? "" : "s",
                                                            "world",
                                                            targetWorld.getName(),
                                                            "pattern",
                                                            settings.getSearchPattern().getConfigKey()
                                                         )
                                                      )
                                                );
                                                MessageUtil.send(
                                                   sender,
                                                   this.plugin
                                                      .getMessageProvider()
                                                      .format(
                                                         MessageKey.FAKE_SIMULATION_STATUS,
                                                         Map.of(
                                                            "count",
                                                            String.valueOf(totalSimulatedx),
                                                            "s",
                                                            totalSimulatedx == 1 ? "" : "s",
                                                            "capacity",
                                                            String.valueOf(this.heatmapSimulationStore.getPerWorldCapacity())
                                                         )
                                                      )
                                                );
                                             }
                                          );
                                    }
                                 );
                           } else {
                              List<Location> generated = new ArrayList<>(amount);

                              for (int i = 0; i < amount; i++) {
                                 int[] coordinates = strategy.generateCandidateCoordinates(
                                    targetWorld, centerX, centerZ, minRadius, maxRadius, settings.getBiomeInclude(), null
                                 );
                                 if (coordinates != null && coordinates.length >= 2) {
                                    Location loc = new Location(targetWorld, coordinates[0], sampleY, coordinates[1]);
                                    generated.add(loc);
                                 }
                              }

                              int inserted = this.heatmapSimulationStore.addSamples(targetWorld.getName(), generated);
                              int totalSimulated = this.heatmapSimulationStore.getSamples(targetWorld.getName()).size();
                              MessageUtil.send(
                                 sender,
                                 this.plugin
                                    .getMessageProvider()
                                    .format(
                                       MessageKey.HEATMAP_SIMULATION_ADDED,
                                       Map.of(
                                          "count",
                                          String.valueOf(inserted),
                                          "s",
                                          inserted == 1 ? "" : "s",
                                          "world",
                                          targetWorld.getName(),
                                          "pattern",
                                          settings.getSearchPattern().getConfigKey()
                                       )
                                    )
                              );
                              MessageUtil.send(
                                 sender,
                                 this.plugin
                                    .getMessageProvider()
                                    .format(
                                       MessageKey.FAKE_SIMULATION_STATUS,
                                       Map.of(
                                          "count",
                                          String.valueOf(totalSimulated),
                                          "s",
                                          totalSimulated == 1 ? "" : "s",
                                          "capacity",
                                          String.valueOf(this.heatmapSimulationStore.getPerWorldCapacity())
                                       )
                                    )
                              );
                              MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_HINT));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private World resolveSimulationWorld(CommandSender sender, String explicitWorld) {
      if (explicitWorld != null && !explicitWorld.isBlank()) {
         World world = Bukkit.getWorld(explicitWorld);
         if (world == null) {
            MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_WORLD_MISSING, Map.of("world", explicitWorld)));
            return null;
         } else {
            return world;
         }
      } else if (sender instanceof Player player) {
         return player.getWorld();
      } else {
         MessageUtil.send(sender, this.plugin.getMessageProvider().format(MessageKey.FAKE_WORLD_REQUIRED_CONSOLE));
         return null;
      }
   }

   private void handleFactionClaimSimulation(CommandSender sender, int amount, String worldFilter) {
      if (!(sender instanceof Player player)) {
         MessageUtil.send(sender, "<red>Claim-based simulation requires a player context.</red>");
      } else if (TeamsAPI.isAvailable() && TeamsAPI.isClaimAvailable()) {
         TeamsService teamsService = TeamsAPI.getService();
         TeamsClaimService claimService = TeamsAPI.getClaimService();
         if (teamsService != null && claimService != null) {
            Team team = (Team)teamsService.getPlayerTeam(player.getUniqueId()).orElse(null);
            if (team == null) {
               MessageUtil.send(sender, "<red>You are not in a faction/team.</red>");
            } else {
               List<TeamClaim> allClaims = new ArrayList<>(claimService.getTeamClaims(team.getId()));
               if (worldFilter != null && !worldFilter.isBlank()) {
                  allClaims.removeIf(c -> !worldFilter.equalsIgnoreCase(c.getWorldName()));
               }

               if (allClaims.isEmpty()) {
                  MessageUtil.send(sender, "<red>Your faction/team has no claims for this simulation scope.</red>");
               } else {
                  Random random = new Random();
                  List<Location> generated = new ArrayList<>(amount);
                  int invalidWorldClaims = 0;

                  for (int i = 0; i < amount; i++) {
                     TeamClaim claim = allClaims.get(random.nextInt(allClaims.size()));
                     World world = Bukkit.getWorld(claim.getWorldName());
                     if (world == null) {
                        invalidWorldClaims++;
                     } else {
                        int chunkBaseX = claim.getChunkX() * 16;
                        int chunkBaseZ = claim.getChunkZ() * 16;
                        int x = chunkBaseX + random.nextInt(16);
                        int z = chunkBaseZ + random.nextInt(16);
                        generated.add(new Location(world, x, this.resolveSimulationY(world, null), z));
                     }
                  }

                  Map<String, List<Location>> byWorld = new HashMap<>();

                  for (Location location : generated) {
                     byWorld.computeIfAbsent(location.getWorld().getName(), ignored -> new ArrayList<>()).add(location);
                  }

                  int insertedTotal = 0;

                  for (Entry<String, List<Location>> entry : byWorld.entrySet()) {
                     insertedTotal += this.heatmapSimulationStore.addSamples(entry.getKey(), entry.getValue());
                  }

                  MessageUtil.send(sender, "<green>Added <white>" + insertedTotal + "</white> simulated RTP points on faction claims.</green>");
                  MessageUtil.send(sender, "<gray>Faction claim chunks used: <white>" + allClaims.size() + "</white></gray>");
                  if (invalidWorldClaims > 0) {
                     MessageUtil.send(sender, "<yellow>Skipped <white>" + invalidWorldClaims + "</white> samples due to unloaded claim worlds.</yellow>");
                  }
               }
            }
         } else {
            MessageUtil.send(sender, "<red>TeamsAPI services are unavailable right now.</red>");
         }
      } else {
         MessageUtil.send(sender, "<red>TeamsAPI claim service is unavailable.</red>");
      }
   }

   private BiomeSearchStrategy resolveSearchStrategy(SearchPattern pattern) {
      if (pattern == null) {
         return new UniformSearchStrategy();
      } else {
         return (BiomeSearchStrategy)(switch (pattern) {
            case CIRCLE -> new CircularSearchStrategy();
            case DIAMOND -> new DiamondSearchStrategy();
            case TRIANGLE -> new TriangleSearchStrategy();
            case SQUARE -> new SquareSearchStrategy();
            case RANDOM -> new UniformSearchStrategy();
         });
      }
   }

   private int resolveMaximumRadius(World world, RandomTeleportSettings settings) {
      if (!settings.useWorldBorderRadius()) {
         return settings.getMaximumRadius();
      } else {
         Optional<Double> radius = WorldBorderCompat.getBorderRadius(world);
         if (radius.isPresent()) {
            int resolved = (int)Math.floor(radius.get());
            return Math.max(settings.getMinimumRadius(), resolved);
         } else {
            return settings.getMaximumRadius();
         }
      }
   }

   private double resolveSimulationY(World world, RandomTeleportSettings settings) {
      if (settings == null) {
         Location spawn = world.getSpawnLocation();
         return spawn != null ? spawn.getY() : 64.0;
      } else if (settings.getMaxY() != null) {
         return (settings.getMinY() + settings.getMaxY()) / 2.0;
      } else if (settings.getMinY() != null) {
         return settings.getMinY().intValue() + 10.0;
      } else {
         Location spawn = world.getSpawnLocation();
         return spawn != null ? spawn.getY() : 64.0;
      }
   }
}
