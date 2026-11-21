package de.theredend2000.advancedhunt.commands;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.data.SqlRepository;
import de.theredend2000.advancedhunt.data.YamlRepository;
import de.theredend2000.advancedhunt.lib.cloud.Command;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.description.CommandDescription;
import de.theredend2000.advancedhunt.lib.cloud.help.HelpQuery;
import de.theredend2000.advancedhunt.lib.cloud.help.result.CommandEntry;
import de.theredend2000.advancedhunt.lib.cloud.help.result.HelpQueryResult;
import de.theredend2000.advancedhunt.lib.cloud.help.result.IndexCommandResult;
import de.theredend2000.advancedhunt.lib.cloud.help.result.MultipleCommandResult;
import de.theredend2000.advancedhunt.lib.cloud.help.result.VerboseCommandResult;
import de.theredend2000.advancedhunt.lib.cloud.paper.LegacyPaperCommandManager;
import de.theredend2000.advancedhunt.lib.cloud.parser.standard.IntegerParser;
import de.theredend2000.advancedhunt.lib.cloud.parser.standard.StringParser;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.Suggestion;
import de.theredend2000.advancedhunt.lib.cloud.suggestion.SuggestionProvider;
import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.ExecutionTime;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.managers.MigrationService;
import de.theredend2000.advancedhunt.managers.minigame.MinigameType;
import de.theredend2000.advancedhunt.menu.collection.CollectionEditorMenu;
import de.theredend2000.advancedhunt.menu.collection.CollectionListMenu;
import de.theredend2000.advancedhunt.menu.collection.CollectionSettingsMenu;
import de.theredend2000.advancedhunt.menu.collection.DeleteCollectionHandlingMenu;
import de.theredend2000.advancedhunt.menu.collection.LeaderboardMenu;
import de.theredend2000.advancedhunt.menu.collection.ProgressMenu;
import de.theredend2000.advancedhunt.menu.place.PlaceItemGroupMenu;
import de.theredend2000.advancedhunt.menu.reward.RewardsMenu;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.model.TreasureRewardHolder;
import de.theredend2000.advancedhunt.platform.PlatformAccess;
import de.theredend2000.advancedhunt.util.BlockUtils;
import de.theredend2000.advancedhunt.util.CronUtils;
import de.theredend2000.advancedhunt.util.MaterialUtils;
import de.theredend2000.advancedhunt.util.MessageUtils;
import de.theredend2000.advancedhunt.util.ParticleUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;

public class AdvancedHuntCommand {
   private final Main plugin;
   private LegacyPaperCommandManager<CommandSender> commandManager;

   public AdvancedHuntCommand(Main plugin) {
      this.plugin = plugin;
   }

   public void register(LegacyPaperCommandManager<CommandSender> commandManager, boolean debugEnabled) {
      this.commandManager = commandManager;
      SuggestionProvider<CommandSender> collectionsSuggestions = (context, input) -> CompletableFuture.completedFuture(
         this.plugin
            .getCollectionManager()
            .getAllCollectionNames()
            .stream()
            .map(name -> name.replace(" ", "_"))
            .map(Suggestion::suggestion)
            .collect(Collectors.toList())
      );
      SuggestionProvider<CommandSender> playerNameSuggestions = (context, input) -> CompletableFuture.supplyAsync(
         () -> Arrays.stream(Bukkit.getOfflinePlayers())
            .<String>map(OfflinePlayer::getName)
            .filter(name -> name != null && !name.isEmpty())
            .map(Suggestion::suggestion)
            .distinct()
            .collect(Collectors.toList())
      );
      SuggestionProvider<CommandSender> minigameSuggestions = (context, input) -> CompletableFuture.completedFuture(
         Arrays.stream(MinigameType.values()).map(Enum::name).map(Suggestion::suggestion).collect(Collectors.toList())
      );
      SuggestionProvider<CommandSender> bypassSuggestions = (context, input) -> CompletableFuture.completedFuture(
         Arrays.asList("on", "off").stream().map(Suggestion::suggestion).collect(Collectors.toList())
      );
      CommandComponent.Builder<CommandSender, String> collectionArg = CommandComponent.<CommandSender, String>builder()
         .name("collection")
         .parser(StringParser.stringParser())
         .suggestionProvider(collectionsSuggestions);
      commandManager.command(
         this.baseBuilder()
            .literal("help")
            .permission("advancedhunt.help")
            .optional("query", StringParser.greedyStringParser())
            .commandDescription(this.desc("help"))
            .handler(context -> {
               String query = context.getOrDefault("query", "");
               this.sendHelp(context.sender(), query);
            })
      );
      commandManager.command(
         this.baseBuilder()
            .literal("reload")
            .permission("advancedhunt.admin.reload")
            .commandDescription(this.desc("reload"))
            .handler(context -> this.reload(context.sender()))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("show")
            .required("radius", IntegerParser.integerParser())
            .required("seconds", IntegerParser.integerParser())
            .permission("advancedhunt.admin.show")
            .commandDescription(this.desc("show"))
            .handler(context -> this.showNearbyTreasures((Player)context.sender(), context.<Integer>get("radius"), context.<Integer>get("seconds")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("bypass")
            .optional("state", StringParser.stringParser(), bypassSuggestions)
            .permission("advancedhunt.treasure.bypass")
            .commandDescription(this.desc("bypass"))
            .handler(context -> this.toggleBypass((Player)context.sender(), context.<String>optional("state").orElse(null)))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("rewards")
            .permission("advancedhunt.admin.rewards")
            .commandDescription(this.desc("rewards"))
            .handler(context -> this.rewards((Player)context.sender()))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("placeitems")
            .permission("advancedhunt.admin.place_items")
            .commandDescription(this.desc("placeitems"))
            .handler(context -> new PlaceItemGroupMenu((Player)context.sender(), this.plugin).open())
      );
      commandManager.command(
         this.playerBuilder()
            .literal("collection")
            .permission("advancedhunt.admin.collection.editor")
            .commandDescription(this.desc("collection"))
            .handler(context -> this.editor((Player)context.sender()))
      );
      commandManager.command(
         this.baseBuilder()
            .literal("collection")
            .literal("create")
            .required("name", StringParser.stringParser())
            .permission("advancedhunt.admin.collection.create")
            .commandDescription(this.desc("collection_create"))
            .handler(context -> this.createCollection(context.sender(), context.get("name")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("collection")
            .literal("edit")
            .required("name", StringParser.stringParser(), collectionsSuggestions)
            .permission("advancedhunt.admin.collection.edit")
            .commandDescription(this.desc("collection_edit"))
            .handler(context -> this.editCollection((Player)context.sender(), context.get("name")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("collection")
            .literal("delete")
            .required("name", StringParser.stringParser(), collectionsSuggestions)
            .permission("advancedhunt.admin.collection.delete")
            .commandDescription(this.desc("collection_delete"))
            .handler(context -> this.deleteCollection((Player)context.sender(), context.get("name")))
      );
      commandManager.command(
         this.baseBuilder()
            .literal("collection")
            .literal("rename")
            .required("oldName", StringParser.stringParser(), collectionsSuggestions)
            .required("newName", StringParser.stringParser())
            .permission("advancedhunt.admin.collection.rename")
            .commandDescription(this.desc("collection_rename"))
            .handler(context -> this.renameCollection(context.sender(), context.get("oldName"), context.get("newName")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("collection")
            .literal("info")
            .required("name", StringParser.stringParser(), collectionsSuggestions)
            .permission("advancedhunt.admin.collection.info")
            .commandDescription(this.desc("collection_info"))
            .handler(context -> this.showCollectionInfo((Player)context.sender(), context.get("name")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("list")
            .permission("advancedhunt.admin.list")
            .commandDescription(this.desc("list"))
            .handler(context -> this.list((Player)context.sender(), null))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("list")
            .required(collectionArg)
            .permission("advancedhunt.admin.list")
            .commandDescription(this.desc("list_collection"))
            .handler(context -> this.list((Player)context.sender(), context.get("collection")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("leaderboard")
            .required(collectionArg)
            .permission("advancedhunt.leaderboard")
            .commandDescription(this.desc("leaderboard"))
            .handler(context -> this.leaderboard((Player)context.sender(), context.get("collection")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("progress")
            .required(collectionArg)
            .permission("advancedhunt.progress")
            .commandDescription(this.desc("progress"))
            .handler(context -> this.progress((Player)context.sender(), context.get("collection")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("place")
            .permission("advancedhunt.admin.place")
            .commandDescription(this.desc("place"))
            .handler(context -> this.placeMode((Player)context.sender(), null))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("place")
            .required(collectionArg)
            .permission("advancedhunt.admin.place")
            .commandDescription(this.desc("place_collection"))
            .handler(context -> this.placeMode((Player)context.sender(), context.get("collection")))
      );
      commandManager.command(
         this.baseBuilder()
            .literal("reset")
            .literal("all")
            .permission("advancedhunt.admin.reset")
            .commandDescription(this.desc("reset_all"))
            .handler(context -> this.resetAll(context.sender()))
      );
      commandManager.command(
         this.baseBuilder()
            .literal("reset")
            .literal("collection")
            .required(collectionArg)
            .permission("advancedhunt.admin.reset")
            .commandDescription(this.desc("reset_collection"))
            .handler(context -> this.resetCollection(context.sender(), context.get("collection")))
      );
      commandManager.command(
         this.baseBuilder()
            .literal("reset")
            .literal("player")
            .required("player", StringParser.stringParser(), playerNameSuggestions)
            .permission("advancedhunt.admin.reset")
            .commandDescription(this.desc("reset_player"))
            .handler(context -> this.resetPlayer(context.sender(), context.get("player")))
      );
      commandManager.command(
         this.baseBuilder()
            .literal("reset")
            .literal("player")
            .required("player", StringParser.stringParser(), playerNameSuggestions)
            .required(collectionArg)
            .permission("advancedhunt.admin.reset")
            .commandDescription(this.desc("reset_player_collection"))
            .handler(context -> this.resetPlayerCollection(context.sender(), context.get("player"), context.get("collection")))
      );
      commandManager.command(
         this.playerBuilder()
            .literal("hint")
            .permission("advancedhunt.hint")
            .commandDescription(this.desc("hint"))
            .handler(context -> this.hint((Player)context.sender()))
      );
      if (debugEnabled) {
         this.registerDebugCommands(minigameSuggestions, collectionsSuggestions);
      }

      SuggestionProvider<CommandSender> migrationTypes = (context, input) -> CompletableFuture.completedFuture(
         Arrays.asList("yaml", "sqlite", "mysql").stream().map(Suggestion::suggestion).collect(Collectors.toList())
      );
      commandManager.command(
         this.baseBuilder()
            .literal("migrate")
            .required("type", StringParser.stringParser(), migrationTypes)
            .flag(commandManager.flagBuilder("force"))
            .flag(commandManager.flagBuilder("purge"))
            .permission("advancedhunt.admin.migrate")
            .commandDescription(this.desc("migrate"))
            .handler(context -> {
               boolean force = context.flags().isPresent("force");
               boolean purge = context.flags().isPresent("purge");
               this.migrate(context.sender(), context.<String>get("type").toUpperCase(), force, purge);
            })
      );
   }

   private void registerDebugCommands(SuggestionProvider<CommandSender> minigameSuggestions, SuggestionProvider<CommandSender> collectionsSuggestions) {
      CommandComponent.Builder<CommandSender, String> collectionArg = CommandComponent.<CommandSender, String>builder()
         .name("collection")
         .parser(StringParser.stringParser())
         .suggestionProvider(collectionsSuggestions);
      this.commandManager
         .command(
            this.playerBuilder()
               .literal("debug")
               .literal("minigame")
               .required("type", StringParser.stringParser(), minigameSuggestions)
               .permission("advancedhunt.admin")
               .commandDescription(this.desc("debug_minigame"))
               .handler(context -> this.minigame((Player)context.sender(), context.get("type")))
         );
      this.commandManager
         .command(
            this.playerBuilder()
               .literal("debug")
               .literal("hint")
               .permission("advancedhunt.admin")
               .commandDescription(this.desc("debug_hint"))
               .handler(context -> this.debugHint((Player)context.sender()))
         );
      this.commandManager
         .command(
            this.playerBuilder()
               .literal("debug")
               .literal("placecollection")
               .required(collectionArg)
               .required("amount", StringParser.stringParser())
               .permission("advancedhunt.admin")
               .commandDescription(this.desc("debug_placecollection"))
               .handler(context -> this.debugPlaceCollectionRandom((Player)context.sender(), context.get("collection"), context.get("amount")))
         );
      this.commandManager
         .command(
            this.playerBuilder()
               .literal("debug")
               .literal("placecollection")
               .required(collectionArg)
               .required("amount", StringParser.stringParser())
               .literal("random")
               .permission("advancedhunt.admin")
               .commandDescription(this.desc("debug_placecollection_random"))
               .handler(context -> this.debugPlaceCollectionRandom((Player)context.sender(), context.get("collection"), context.get("amount")))
         );
      this.commandManager
         .command(
            this.playerBuilder()
               .literal("debug")
               .literal("placecollection")
               .required(collectionArg)
               .required("amount", StringParser.stringParser())
               .literal("plane")
               .permission("advancedhunt.admin")
               .commandDescription(this.desc("debug_placecollection_plane"))
               .handler(context -> this.debugPlaceCollectionPlane((Player)context.sender(), context.get("collection"), context.get("amount")))
         );
      this.commandManager
         .command(
            this.playerBuilder()
               .literal("debug")
               .literal("removecollectionplaced")
               .required(collectionArg)
               .permission("advancedhunt.admin")
               .commandDescription(this.desc("debug_removecollectionplaced"))
               .handler(context -> this.debugRemoveCollectionPlaced((Player)context.sender(), context.get("collection")))
         );
      this.commandManager
         .command(
            this.playerBuilder()
               .literal("debug")
               .literal("inspect")
               .permission("advancedhunt.admin")
               .commandDescription(this.desc("debug_inspect"))
               .handler(context -> this.debugInspectTreasure((Player)context.sender()))
         );
      this.commandManager
         .command(
            this.playerBuilder().literal("debug").literal("glow").permission("advancedhunt.admin").handler(context -> this.glowBlock((Player)context.sender()))
         );
   }

   private void showNearbyTreasures(final Player player, int radius, int seconds) {
      if (player != null) {
         if (radius >= 1 && radius <= 500) {
            if (seconds >= 1 && seconds <= 300) {
               Location playerLoc = player.getLocation();
               if (playerLoc.getWorld() != null) {
                  int playerChunkX = playerLoc.getBlockX() >> 4;
                  int playerChunkZ = playerLoc.getBlockZ() >> 4;
                  int chunkRadius = (radius >> 4) + 1;
                  double radiusSq = (double)radius * radius;
                  List<TreasureCore> nearby = new ArrayList<>();

                  for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
                     for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
                        for (TreasureCore core : this.plugin.getTreasureManager().getTreasureCoresInChunk(cx, cz)) {
                           if (core != null) {
                              Location loc = core.getLocation();
                              if (loc != null
                                 && loc.getWorld() != null
                                 && loc.getWorld().equals(playerLoc.getWorld())
                                 && !(loc.distanceSquared(playerLoc) > radiusSq)) {
                                 nearby.add(core);
                              }
                           }
                        }
                     }
                  }

                  if (nearby.isEmpty()) {
                     player.sendMessage(this.plugin.getMessageManager().getMessage("command.show.none_found", "%radius%", String.valueOf(radius)));
                  } else {
                     int maxMarkers = 80;
                     boolean truncated = nearby.size() > maxMarkers;
                     if (truncated) {
                        nearby = nearby.subList(0, maxMarkers);
                     }

                     int[] entityIds = new int[nearby.size()];
                     int spawnedCount = 0;

                     for (TreasureCore corex : nearby) {
                        Location loc = corex.getLocation();
                        if (loc != null && loc.getWorld() != null) {
                           Location hologramLoc = loc.clone().add(0.5, 1.5, 0.5);
                           int entityId = allocateClientSideEntityId(player.getWorld());
                           UUID entityUuid = UUID.randomUUID();
                           int x = loc.getBlockX();
                           int y = loc.getBlockY();
                           int z = loc.getBlockZ();
                           String name = "Treasure: " + x + " " + y + " " + z;
                           boolean spawned = PlatformAccess.get().spawnHologramArmorStandForPlayer(player, entityId, entityUuid, hologramLoc, name);
                           if (!spawned) {
                              if (spawnedCount == 0) {
                                 player.sendMessage(this.plugin.getMessageManager().getMessage("command.show.packetevents_missing"));
                                 return;
                              }
                              break;
                           }

                           entityIds[spawnedCount++] = entityId;
                        }
                     }

                     if (spawnedCount == 0) {
                        player.sendMessage(this.plugin.getMessageManager().getMessage("command.show.packetevents_missing"));
                     } else {
                        final int[] finalEntityIds = spawnedCount == entityIds.length ? entityIds : Arrays.copyOf(entityIds, spawnedCount);
                        long delayTicks = seconds * 20L;
                        (new BukkitRunnable() {
                           public void run() {
                              try {
                                 if (!player.isOnline()) {
                                    return;
                                 }

                                 PlatformAccess.get().destroyEntitiesForPlayer(player, finalEntityIds);
                              } catch (Throwable var2) {
                              }
                           }
                        }).runTaskLater(this.plugin, delayTicks);
                        String successKey = truncated ? "command.show.success_truncated" : "command.show.success";
                        player.sendMessage(
                           this.plugin
                              .getMessageManager()
                              .getMessage(
                                 successKey,
                                 "%seconds%",
                                 String.valueOf(seconds),
                                 "%radius%",
                                 String.valueOf(radius),
                                 "%count%",
                                 String.valueOf(spawnedCount),
                                 "%max%",
                                 String.valueOf(maxMarkers)
                              )
                        );
                     }
                  }
               }
            } else {
               player.sendMessage(this.plugin.getMessageManager().getMessage("command.show.invalid_duration"));
            }
         } else {
            player.sendMessage(this.plugin.getMessageManager().getMessage("command.show.invalid_radius"));
         }
      }
   }

   private void toggleBypass(Player player, String stateRaw) {
      if (player != null) {
         if (stateRaw != null && !stateRaw.isEmpty()) {
            String state = stateRaw.toLowerCase(Locale.ROOT);
            if ("on".equals(state)) {
               this.plugin.getTreasureVisibilityManager().setBypass(player, true);
               player.sendMessage(this.plugin.getMessageManager().getMessage("command.bypass.enabled"));
            } else if ("off".equals(state)) {
               this.plugin.getTreasureVisibilityManager().setBypass(player, false);
               player.sendMessage(this.plugin.getMessageManager().getMessage("command.bypass.disabled"));
            } else {
               player.sendMessage(this.plugin.getMessageManager().getMessage("command.bypass.usage"));
            }
         } else {
            boolean enableBypass = !this.plugin.getTreasureVisibilityManager().isBypassEnabled(player);
            this.plugin.getTreasureVisibilityManager().setBypass(player, enableBypass);
            player.sendMessage(this.plugin.getMessageManager().getMessage(enableBypass ? "command.bypass.enabled" : "command.bypass.disabled"));
         }
      }
   }

   private static int allocateClientSideEntityId(World world) {
      int min = 1000000000;
      int max = Integer.MAX_VALUE;
      Set<Integer> used = new HashSet<>();

      try {
         if (world != null) {
            for (Entity e : world.getEntities()) {
               used.add(e.getEntityId());
            }
         }
      } catch (Throwable var6) {
      }

      for (int i = 0; i < 1000; i++) {
         int candidate = ThreadLocalRandom.current().nextInt(min, max);
         if (!used.contains(candidate)) {
            return candidate;
         }
      }

      return ThreadLocalRandom.current().nextInt(min, max);
   }

   private CommandDescription desc(String key) {
      String description = this.plugin.getMessageManager().getMessage("command.help.descriptions." + key, false);
      return CommandDescription.commandDescription(description);
   }

   private Command.Builder<CommandSender> baseBuilder() {
      String[] parts = this.plugin.getConfig().getString("command.name", "advancedhunt|ah").split("\\|");
      return this.commandManager.commandBuilder(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
   }

   private Command.Builder<CommandSender> playerBuilder() {
      return this.baseBuilder().senderType(Player.class);
   }

   private void sendHelp(CommandSender sender, String rawQuery) {
      AdvancedHuntCommand.HelpRequest request = this.parseHelpRequest(rawQuery);
      HelpQueryResult<CommandSender> result = this.plugin.getMinecraftHelp().helpHandler().query(HelpQuery.of(sender, request.query()));
      if (result instanceof IndexCommandResult) {
         this.sendIndexHelp(sender, request, (IndexCommandResult<CommandSender>)result);
      } else if (result instanceof MultipleCommandResult) {
         this.sendMultipleHelp(sender, request, (MultipleCommandResult<CommandSender>)result);
      } else if (result instanceof VerboseCommandResult) {
         this.sendVerboseHelp(sender, (VerboseCommandResult<CommandSender>)result);
      } else {
         sender.sendMessage(this.plugin.getMessageManager().getMessage("error.generic"));
      }
   }

   private AdvancedHuntCommand.HelpRequest parseHelpRequest(String rawQuery) {
      String safeQuery = rawQuery == null ? "" : rawQuery.trim();
      if (safeQuery.isEmpty()) {
         return new AdvancedHuntCommand.HelpRequest("", 1);
      } else {
         String[] splitQuery = safeQuery.split("\\s+");
         String pageText = splitQuery[splitQuery.length - 1];

         try {
            int page = Integer.parseInt(pageText);
            String query = safeQuery.equals(pageText) ? "" : safeQuery.substring(0, safeQuery.lastIndexOf(pageText)).trim();
            return new AdvancedHuntCommand.HelpRequest(query, page);
         } catch (NumberFormatException var7) {
            return new AdvancedHuntCommand.HelpRequest(safeQuery, 1);
         }
      }
   }

   private void sendIndexHelp(CommandSender sender, AdvancedHuntCommand.HelpRequest request, IndexCommandResult<CommandSender> result) {
      if (result.isEmpty()) {
         this.sendNoResults(sender, request.query());
      } else {
         List<CommandEntry<CommandSender>> entries = result.entries();
         int pageSize = this.plugin.getMinecraftHelp().maxResultsPerPage();
         int maxPages = Math.max(1, (entries.size() + pageSize - 1) / pageSize);
         if (request.page() >= 1 && request.page() <= maxPages) {
            int start = (request.page() - 1) * pageSize;
            int end = Math.min(start + pageSize, entries.size());
            this.sendHelpHeader(sender, request.query(), request.page(), maxPages);
            sender.sendMessage(ChatColor.GRAY + this.plugin.getMessageManager().getMessage("command.help.minecraft.available_commands", false) + ":");

            for (int index = start; index < end; index++) {
               CommandEntry<CommandSender> entry = entries.get(index);
               sender.sendMessage(ChatColor.GOLD + "/" + entry.syntax() + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + this.describe(entry));
            }
         } else {
            sender.sendMessage(
               this.plugin.getMessageManager().getMessage("command.help.minecraft.page_out_of_range", false) + ChatColor.GRAY + " (1-" + maxPages + ")"
            );
         }
      }
   }

   private void sendMultipleHelp(CommandSender sender, AdvancedHuntCommand.HelpRequest request, MultipleCommandResult<CommandSender> result) {
      List<String> suggestions = result.childSuggestions();
      if (suggestions.isEmpty()) {
         this.sendNoResults(sender, request.query());
      } else {
         int pageSize = this.plugin.getMinecraftHelp().maxResultsPerPage();
         int maxPages = Math.max(1, (suggestions.size() + pageSize - 1) / pageSize);
         if (request.page() >= 1 && request.page() <= maxPages) {
            int start = (request.page() - 1) * pageSize;
            int end = Math.min(start + pageSize, suggestions.size());
            this.sendHelpHeader(sender, request.query(), request.page(), maxPages);
            sender.sendMessage(ChatColor.GRAY + "/" + result.longestPath());

            for (int index = start; index < end; index++) {
               sender.sendMessage(ChatColor.GOLD + "/" + suggestions.get(index));
            }
         } else {
            sender.sendMessage(
               this.plugin.getMessageManager().getMessage("command.help.minecraft.page_out_of_range", false) + ChatColor.GRAY + " (1-" + maxPages + ")"
            );
         }
      }
   }

   private void sendVerboseHelp(CommandSender sender, VerboseCommandResult<CommandSender> result) {
      CommandEntry<CommandSender> entry = result.entry();
      this.sendHelpHeader(sender, result.query().query(), 1, 1);
      sender.sendMessage(
         ChatColor.YELLOW
            + this.plugin.getMessageManager().getMessage("command.help.minecraft.command", false)
            + ChatColor.GRAY
            + ": "
            + ChatColor.GOLD
            + "/"
            + entry.syntax()
      );
      sender.sendMessage(
         ChatColor.YELLOW
            + this.plugin.getMessageManager().getMessage("command.help.minecraft.description", false)
            + ChatColor.GRAY
            + ": "
            + ChatColor.WHITE
            + this.describe(entry)
      );
   }

   private void sendNoResults(CommandSender sender, String query) {
      this.sendHelpHeader(sender, query, 1, 1);
      String visibleQuery = query != null && !query.isEmpty() ? "/" + query : "/advancedhunt";
      sender.sendMessage(
         ChatColor.RED
            + this.plugin.getMessageManager().getMessage("command.help.minecraft.no_results_for_query", false)
            + ChatColor.GRAY
            + ": "
            + visibleQuery
      );
   }

   private void sendHelpHeader(CommandSender sender, String query, int page, int maxPages) {
      sender.sendMessage(
         this.plugin.getMessageManager().getMessage("command.help.minecraft.help", false) + ChatColor.DARK_GRAY + " [" + page + "/" + maxPages + "]"
      );
      if (query != null && !query.isEmpty()) {
         sender.sendMessage(
            ChatColor.GRAY
               + this.plugin.getMessageManager().getMessage("command.help.minecraft.showing_results_for_query", false)
               + ChatColor.DARK_GRAY
               + ": "
               + ChatColor.YELLOW
               + query
         );
      }
   }

   private String describe(CommandEntry<CommandSender> entry) {
      CommandDescription description = entry.command().commandDescription();
      return description != null && !description.isEmpty()
         ? description.description().textDescription()
         : this.plugin.getMessageManager().getMessage("command.help.minecraft.no_description", false);
   }

   private void withCollection(CommandSender sender, String collectionName, Consumer<Collection> action) {
      Optional<Collection> collectionOpt = this.plugin.getCollectionManager().getCollectionByName(collectionName);
      if (collectionOpt.isPresent()) {
         action.accept(collectionOpt.get());
      } else {
         sender.sendMessage(this.plugin.getMessageManager().getMessage("command.collection_not_found"));
      }
   }

   private void invalidateAllPlayerCaches() {
      Bukkit.getOnlinePlayers().forEach(p -> this.plugin.getPlayerManager().invalidate(p.getUniqueId()));
   }

   private Optional<OfflinePlayer> validateOfflinePlayer(CommandSender sender, String playerName) {
      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
      if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
         sender.sendMessage(this.plugin.getMessageManager().getMessage("command.reset.player_not_found"));
         return Optional.empty();
      } else {
         return Optional.of(offlinePlayer);
      }
   }

   public void rewards(Player player) {
      Block targetBlock = player.getTargetBlockExact(5);
      if (targetBlock == null) {
         player.sendMessage(this.plugin.getMessageManager().getMessage("command.rewards.no_block"));
      } else {
         Treasure treasure = this.plugin.getTreasureManager().getTreasureAt(targetBlock.getLocation());
         if (treasure == null) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("command.rewards.not_treasure"));
         } else {
            new RewardsMenu(player, this.plugin, new TreasureRewardHolder(this.plugin, treasure)).open();
         }
      }
   }

   public void editor(Player player) {
      new CollectionEditorMenu(player, this.plugin).open();
   }

   public void reload(CommandSender sender) {
      this.plugin.reloadConfig();
      this.plugin.getMessageManager().reloadMessages();
      this.plugin.getSoundManager().reload();
      this.plugin.getTreasureVisibilityManager().reloadReplaceBlock();
      this.plugin.reloadStorageBackend();
      this.plugin.getParticleManager().reload();
      this.plugin.getHintManager().cancelAllVisualHints();
      this.plugin.getHintManager().reloadConfig();
      this.plugin.getProximityManager().reloadConfig();
      this.plugin.getTreasureManager().loadTreasures();
      CompletableFuture<Void> collectionsReload = this.plugin.getCollectionManager().reloadCollections();
      CompletableFuture<Void> presetsReload = this.plugin.getRewardPresetManager().reloadPresets();
      this.plugin.getLeaderboardManager().forceUpdate();
      Bukkit.getOnlinePlayers().forEach(p -> this.plugin.getPlayerManager().invalidate(p.getUniqueId()));
      CompletableFuture.allOf(collectionsReload, presetsReload)
         .whenComplete(
            (v, ex) -> {
               if (ex != null) {
                  this.plugin.getLogger().warning("Reload failed: " + ex.getMessage());
               }

               Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> sender.sendMessage(this.plugin.getMessageManager().getMessage(ex == null ? "command.reload.success" : "command.reload.failed"))
                  );
            }
         );
   }

   public void list(Player player, String collectionName) {
      if (collectionName == null) {
         UUID selectedId = this.plugin.getPlaceModeManager().getCollectionId(player);
         if (selectedId != null) {
            new CollectionListMenu(player, selectedId, this.plugin).open();
         } else {
            player.sendMessage(this.plugin.getMessageManager().getMessage("command.list.header"));

            for (Collection collection : this.plugin.getCollectionManager().getAllCollections()) {
               boolean isAvailable = this.plugin.getCollectionManager().isCollectionAvailable(collection);
               String availabilityStatus = isAvailable
                  ? this.plugin.getMessageManager().getMessage("collection.available", false)
                  : this.plugin.getMessageManager().getMessage("collection.not_available", false);
               player.sendMessage(
                  this.plugin.getMessageManager().getMessage("command.list.format", "%collection%", collection.getName() + " " + availabilityStatus)
               );
            }

            player.sendMessage(this.plugin.getMessageManager().getMessage("command.list.hint"));
         }
      } else {
         Optional<Collection> collectionOpt = this.plugin.getCollectionManager().getCollectionByName(collectionName);
         if (collectionOpt.isPresent()) {
            new CollectionListMenu(player, collectionOpt.get().getId(), this.plugin).open();
         } else {
            player.sendMessage(this.plugin.getMessageManager().getMessage("command.collection_not_found"));
         }
      }
   }

   public void leaderboard(Player player, String collectionName) {
      this.withCollection(player, collectionName, collection -> new LeaderboardMenu(player, this.plugin, collection, null).open());
   }

   public void progress(Player player, String collectionName) {
      this.withCollection(player, collectionName, collection -> {
         if (!this.plugin.getCollectionManager().isCollectionAvailable(collection)) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("collection.unavailable", "%collection%", collection.getName()));
            this.plugin.getCollectionManager().getNextActivation(collection).ifPresent(nextTime -> {
               String timeStr = this.plugin.getMessageManager().formatDateTime(nextTime);
               player.sendMessage(this.plugin.getMessageManager().getMessage("collection.available_at", "%time%", timeStr));
            });
         }

         new ProgressMenu(player, collection.getId(), collection.getName(), this.plugin).open();
      });
   }

   public void showCollectionInfo(Player player, String collectionName) {
      this.withCollection(
         player,
         collectionName,
         collection -> {
            String uuid = collection.getId().toString();
            int treasureCount = this.plugin.getTreasureManager().getTreasureCoresInCollection(collection.getId()).size();
            boolean isAvailable = this.plugin.getCollectionManager().isCollectionAvailable(collection);
            String statusKey = isAvailable ? "command.info.status_available" : "command.info.status_unavailable";
            String timingLine = "";
            if (!isAvailable) {
               Optional<ZonedDateTime> nextActivation = this.plugin.getCollectionManager().getNextActivation(collection);
               if (nextActivation.isPresent()) {
                  String timeStr = this.plugin.getMessageManager().formatDateTime(nextActivation.get());
                  timingLine = this.plugin.getMessageManager().getMessage("command.info.next_activation", false, "%time%", timeStr);
               }
            } else if (collection.getEnabledActRules() != null && !collection.getEnabledActRules().isEmpty()) {
               timingLine = this.plugin.getMessageManager().getMessage("command.info.active_until_note", false);
            }

            String resetLine;
            if (collection.getProgressResetCron() != null && !collection.getProgressResetCron().isEmpty()) {
               try {
                  Cron cron = CronUtils.getParser().parse(collection.getProgressResetCron());
                  ExecutionTime executionTime = ExecutionTime.forCron(cron);
                  Optional<ZonedDateTime> nextReset = executionTime.nextExecution(ZonedDateTime.now());
                  if (nextReset.isPresent()) {
                     String resetTimeStr = this.plugin.getMessageManager().formatDateTime(nextReset.get());
                     resetLine = this.plugin.getMessageManager().getMessage("command.info.next_reset", false, "%time%", resetTimeStr);
                  } else {
                     resetLine = this.plugin.getMessageManager().getMessage("command.info.no_reset", false);
                  }
               } catch (Exception var14) {
                  resetLine = this.plugin.getMessageManager().getMessage("command.info.invalid_reset_schedule", false);
               }
            } else {
               resetLine = this.plugin.getMessageManager().getMessage("command.info.no_reset", false);
            }

            String uuidDisplay = this.plugin.getMessageManager().getMessage("command.info.uuid", false, "%uuid%", uuid);
            String hoverText = this.plugin.getMessageManager().getMessage("command.info.click_to_copy", false);

            for (String line : this.plugin
               .getMessageManager()
               .getMessageList(
                  "command.info.lines",
                  false,
                  "%collection%",
                  collection.getName(),
                  "%count%",
                  String.valueOf(treasureCount),
                  "%status%",
                  this.plugin.getMessageManager().getMessage(statusKey, false),
                  "%timing%",
                  timingLine,
                  "%reset%",
                  resetLine,
                  "%uuid%",
                  uuidDisplay
               )) {
               if (line.equals(uuidDisplay)) {
                  PlatformAccess.get().sendClickableCopyText(player, line, uuid, hoverText);
               } else {
                  player.sendMessage(line);
               }
            }
         }
      );
   }

   public void editCollection(Player player, String name) {
      this.withCollection(player, name, collection -> new CollectionSettingsMenu(player, this.plugin, collection).open());
   }

   public void deleteCollection(Player player, String name) {
      this.withCollection(player, name, collection -> new DeleteCollectionHandlingMenu(player, this.plugin, collection, true).open());
   }

   public void renameCollection(CommandSender sender, String oldName, String newName) {
      this.plugin.getCollectionManager().renameCollection(oldName, newName).thenAccept(success -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         if (success) {
            sender.sendMessage(this.plugin.getMessageManager().getMessage("command.rename.success", "%old_name%", oldName, "%new_name%", newName));
         } else {
            sender.sendMessage(this.plugin.getMessageManager().getMessage("command.rename.failed"));
         }
      }));
   }

   public void createCollection(CommandSender sender, String name) {
      this.plugin.getCollectionManager().createCollection(name).thenAccept(success -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         if (success) {
            sender.sendMessage(this.plugin.getMessageManager().getMessage("command.create.success", "%name%", name));
         } else {
            sender.sendMessage(this.plugin.getMessageManager().getMessage("command.create.failed"));
         }
      }));
   }

   public void placeMode(Player player, String collectionName) {
      if (this.plugin.getPlaceModeManager().isInPlaceMode(player)) {
         this.plugin.getPlaceModeManager().removePlaceMode(player);
         player.sendMessage(this.plugin.getMessageManager().getMessage("command.place_mode.disabled"));
      } else if (collectionName == null) {
         player.sendMessage(this.plugin.getMessageManager().getMessage("command.place_mode.not_in_place_mode"));
      } else {
         Optional<Collection> collectionOpt = this.plugin.getCollectionManager().getCollectionByName(collectionName);
         if (collectionOpt.isPresent()) {
            Collection collection = collectionOpt.get();
            this.plugin.getPlaceModeManager().setPlaceMode(player, collection);
            player.sendMessage(this.plugin.getMessageManager().getMessage("command.place_mode.enabled", "%collection%", collection.getName()));
            player.sendMessage(this.plugin.getMessageManager().getMessage("command.place_mode.use_command"));
         } else {
            player.sendMessage(this.plugin.getMessageManager().getMessage("command.collection_not_found"));
         }
      }
   }

   public void resetAll(CommandSender sender) {
      this.plugin.getDataRepository().resetAllProgress().thenAccept(count -> Bukkit.getScheduler().runTask(this.plugin, () -> {
         this.plugin.getParticleManager().clearAllGlobalCache();
         this.invalidateAllPlayerCaches();

         for (Player player : Bukkit.getOnlinePlayers()) {
            this.plugin.getTreasureVisibilityManager().restoreFoundTreasuresForPlayer(player);
         }

         sender.sendMessage(this.plugin.getMessageManager().getMessage("command.reset.all_success", "%count%", String.valueOf(count)));
      }));
   }

   public void resetCollection(CommandSender sender, String collectionName) {
      this.withCollection(
         sender,
         collectionName,
         collection -> this.plugin
            .getDataRepository()
            .resetCollectionProgress(collection.getId())
            .thenAccept(
               count -> Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        this.plugin.getParticleManager().clearGlobalCache(collection.getId());
                        this.invalidateAllPlayerCaches();

                        for (Player player : Bukkit.getOnlinePlayers()) {
                           this.plugin.getTreasureVisibilityManager().restoreFoundTreasuresForPlayer(player, collection.getId());
                        }

                        sender.sendMessage(
                           this.plugin
                              .getMessageManager()
                              .getMessage("command.reset.collection_success", "%collection%", collection.getName(), "%count%", String.valueOf(count))
                        );
                     }
                  )
            )
      );
   }

   public void resetPlayer(CommandSender sender, String playerName) {
      Optional<OfflinePlayer> offlinePlayerOpt = this.validateOfflinePlayer(sender, playerName);
      if (offlinePlayerOpt.isPresent()) {
         UUID playerId = offlinePlayerOpt.get().getUniqueId();
         this.plugin
            .getDataRepository()
            .resetPlayerProgress(playerId)
            .thenAccept(
               count -> Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        this.plugin.getPlayerManager().invalidate(playerId);
                        List<CompletableFuture<Void>> recomputes = new ArrayList<>();

                        for (Collection collection : this.plugin.getCollectionManager().getAllCollections()) {
                           if (collection.isSinglePlayerFind()) {
                              recomputes.add(this.plugin.getParticleManager().refreshGlobalCache(collection.getId()));
                           }
                        }

                        CompletableFuture.allOf(recomputes.toArray(new CompletableFuture[0])).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                           for (Collection collectionx : this.plugin.getCollectionManager().getAllCollections()) {
                              if (collectionx.isHideAfterFound()) {
                                 this.plugin.getTreasureVisibilityManager().refreshHideAfterFound(collectionx);
                              }
                           }
                        }));
                        sender.sendMessage(
                           this.plugin.getMessageManager().getMessage("command.reset.player_success", "%player%", playerName, "%count%", String.valueOf(count))
                        );
                     }
                  )
            );
      }
   }

   public void resetPlayerCollection(CommandSender sender, String playerName, String collectionName) {
      Optional<OfflinePlayer> offlinePlayerOpt = this.validateOfflinePlayer(sender, playerName);
      if (offlinePlayerOpt.isPresent()) {
         UUID playerId = offlinePlayerOpt.get().getUniqueId();
         this.withCollection(
            sender,
            collectionName,
            collection -> this.plugin
               .getDataRepository()
               .resetPlayerCollectionProgress(playerId, collection.getId())
               .thenAccept(
                  count -> Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           this.plugin.getPlayerManager().invalidate(playerId);
                           if (collection.isSinglePlayerFind()) {
                              this.plugin
                                 .getParticleManager()
                                 .refreshGlobalCache(collection.getId())
                                 .thenRun(
                                    () -> Bukkit.getScheduler()
                                       .runTask(this.plugin, () -> this.plugin.getTreasureVisibilityManager().refreshHideAfterFound(collection))
                                 );
                           } else {
                              this.plugin.getTreasureVisibilityManager().refreshHideAfterFound(collection);
                           }

                           sender.sendMessage(
                              this.plugin
                                 .getMessageManager()
                                 .getMessage(
                                    "command.reset.player_collection_success",
                                    "%player%",
                                    playerName,
                                    "%collection%",
                                    collection.getName(),
                                    "%count%",
                                    String.valueOf(count)
                                 )
                           );
                        }
                     )
               )
         );
      }
   }

   public void minigame(Player player, String typeName) {
      MinigameType type;
      try {
         type = MinigameType.valueOf(typeName.toUpperCase());
      } catch (IllegalArgumentException var5) {
         player.sendMessage(this.plugin.getMessageManager().getMessage("minigame.error.invalid_type"));
         return;
      }

      this.plugin.getMinigameManager().startMinigame(player, type, success -> {
         if (success) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("minigame.success"));
         } else {
            player.sendMessage(this.plugin.getMessageManager().getMessage("minigame.failed"));
         }
      });
   }

   private void hint(Player player) {
      if (!this.plugin.getConfig().getBoolean("hint.enabled", true)) {
         player.sendMessage(this.plugin.getMessageManager().getMessage("error.feature_disabled"));
      } else if (this.plugin.getHintManager().isOnCooldown(player.getUniqueId())) {
         long remaining = this.plugin.getHintManager().getRemainingCooldown(player.getUniqueId());
         player.sendMessage(this.plugin.getMessageManager().getMessage("hint.cooldown", "%time%", String.valueOf(remaining)));
      } else {
         Optional<TreasureCore> treasureOpt = this.plugin.getHintManager().findRandomUnfoundTreasure(player);
         if (!treasureOpt.isPresent()) {
            player.sendMessage(this.plugin.getMessageManager().getMessage("hint.no_unfound_nearby"));
            this.plugin.getHintManager().applyFailureCooldown(player);
         } else {
            TreasureCore treasure = treasureOpt.get();
            String minigameTypeStr = this.plugin.getConfig().getString("hint.minigames.type", "REACTION");
            MinigameType minigameType;
            if (minigameTypeStr.equalsIgnoreCase("RANDOM")) {
               MinigameType[] types = MinigameType.values();
               minigameType = types[new Random().nextInt(types.length)];
            } else {
               try {
                  minigameType = MinigameType.valueOf(minigameTypeStr.toUpperCase());
               } catch (IllegalArgumentException var7) {
                  player.sendMessage(this.plugin.getMessageManager().getMessage("minigame.error.invalid_type"));
                  this.plugin.getLogger().warning("Invalid hint minigame type in config: " + minigameTypeStr);
                  return;
               }
            }

            this.plugin.getMinigameManager().startMinigame(player, minigameType, success -> {
               if (success) {
                  player.sendMessage(this.plugin.getMessageManager().getMessage("hint.success"));
                  this.plugin.getHintManager().deliverHint(player, treasure);
               } else {
                  player.sendMessage(this.plugin.getMessageManager().getMessage("hint.minigame_failed"));
                  this.plugin.getHintManager().applyFailureCooldown(player);
               }
            });
         }
      }
   }

   private void debugHint(Player player) {
      this.sendDebugChat(player, "&e[DEBUG] Searching for nearby treasure...");
      Optional<TreasureCore> treasureOpt = this.plugin.getHintManager().findRandomUnfoundTreasure(player);
      if (!treasureOpt.isPresent()) {
         this.sendDebugChat(player, "&c[DEBUG] No unfound treasures nearby in active collections!");
      } else {
         TreasureCore treasure = treasureOpt.get();
         this.sendDebugChat(player, "&a[DEBUG] Found treasure! Delivering hint...");
         this.plugin.getHintManager().deliverHint(player, treasure);
         this.sendDebugChat(player, "&e[DEBUG] Hint delivered. Visual effects active if enabled in config.");
      }
   }

   private void debugInspectTreasure(Player player) {
      if (player != null) {
         Block targetBlock = player.getTargetBlockExact(10);
         if (targetBlock == null) {
            this.sendDebugChat(player, "&c[DEBUG] Look at a treasure block first.");
         } else {
            TreasureCore core = this.plugin.getTreasureManager().getTreasureCoreAt(targetBlock.getLocation());
            if (core == null) {
               this.sendDebugChat(player, "&c[DEBUG] The looked-at block is not a stored treasure.");
            } else {
               Treasure treasure = this.plugin.getTreasureManager().getFullTreasure(core.getId());
               Location location = targetBlock.getLocation();
               World world = location.getWorld();
               this.sendDebugChat(
                  player,
                  "&e[DEBUG] Inspecting treasure at &f"
                     + (world != null ? world.getName() : "unknown")
                     + " "
                     + location.getBlockX()
                     + " "
                     + location.getBlockY()
                     + " "
                     + location.getBlockZ()
               );
               this.sendDebugField(player, "treasure_id", core.getId().toString());
               this.sendDebugField(player, "block_data", treasure != null ? treasure.getNbtData() : null);
               this.sendDebugField(player, "material", treasure != null ? treasure.getMaterial() : core.getMaterial());
               this.sendDebugField(player, "block_state", treasure != null ? treasure.getBlockState() : core.getBlockState());
               if (treasure == null) {
                  this.sendDebugChat(player, "&e[DEBUG] Full treasure data could not be loaded; block_data may be unavailable.");
               }
            }
         }
      }
   }

   private void debugPlaceCollectionRandom(final Player player, String collectionName, String amountRaw) {
      final int amount;
      try {
         amount = Integer.parseInt(amountRaw);
      } catch (NumberFormatException var27) {
         this.sendDebugChat(player, "&c[DEBUG] Amount must be a number.");
         return;
      }

      if (amount <= 0) {
         this.sendDebugChat(player, "&c[DEBUG] Amount must be >= 1.");
      } else {
         final List<AdvancedHuntCommand.PaletteEntry> palette = this.getHotbarPaletteEntries(player);
         if (palette.isEmpty()) {
            this.sendDebugChat(player, "&c[DEBUG] Put at least one placeable block in your hotbar (slots 1-9). ");
         } else {
            Optional<Collection> collectionOpt = this.plugin.getCollectionManager().getCollectionByName(collectionName);
            if (!collectionOpt.isPresent()) {
               this.sendDebugChat(player, "&c[DEBUG] Unknown collection: &f" + collectionName);
            } else {
               final Collection collection = collectionOpt.get();
               this.sendDebugChat(player, "&e[DEBUG] Placing &f" + amount + "&e treasures &7(random)&e for collection &b" + collection.getName() + "&e...");
               int radius = 12;
               int verticalRadius = 8;
               long maxAttemptsLong = Math.max(amount * 25L, 50L);
               final int maxAttempts = (int)Math.min(2147483647L, maxAttemptsLong);
               final Random random = new Random();
               final NamespacedKey debugPlacedKey = new NamespacedKey(this.plugin, "debug_placed");
               final Location base = player.getLocation().clone();
               List<Reward> defaultRewards = new ArrayList<>();
               Optional.ofNullable(collection.getDefaultTreasureRewardPresetId())
                  .flatMap(defaultPresetId -> this.plugin.getRewardPresetManager().getPreset(RewardPresetType.TREASURE, defaultPresetId))
                  .ifPresent(preset -> defaultRewards.addAll(preset.getRewards()));
               final List<Reward> sharedRewards = defaultRewards.isEmpty()
                  ? Collections.emptyList()
                  : Collections.unmodifiableList(new ArrayList<>(defaultRewards));
               int batchSize = 250;
               final int maxAttemptsPerTick = Math.max(7500, 500);
               final Set<String> reserved = new HashSet<>(Math.min(amount * 2, 32768));
               final AtomicInteger saved = new AtomicInteger(0);
               final AtomicInteger saveFailed = new AtomicInteger(0);
               int saveBatchSize = 200;
               int maxInFlightBatches = 2;
               final AtomicInteger inFlightBatches = new AtomicInteger(0);
               final List<Treasure> pendingSaveBatch = new ArrayList<>(200);
               (new BukkitRunnable() {
                     int placed = 0;
                     int attempts = 0;
                     boolean placingDone = false;

                     public void run() {
                        if (player.isOnline() && player.getWorld() != null) {
                           if (!this.placingDone && (this.placed >= amount || this.attempts >= maxAttempts)) {
                              this.placingDone = true;
                           }

                           if (this.placingDone && pendingSaveBatch.isEmpty() && inFlightBatches.get() == 0 && saved.get() + saveFailed.get() >= this.placed) {
                              MessageUtils.sendActionBar(
                                 player,
                                 "&a[DEBUG] Done: &f" + this.placed + "&a/" + amount + "&7 (Saved: " + saved.get() + ", Failed: " + saveFailed.get() + ")"
                              );
                              AdvancedHuntCommand.this.sendDebugChat(
                                 player,
                                 "&a[DEBUG] Placed &f"
                                    + this.placed
                                    + "&a/"
                                    + amount
                                    + "&a treasures.&7 Attempts: &f"
                                    + this.attempts
                                    + "&7. Saved: &f"
                                    + saved.get()
                                    + "&7, Failed: &f"
                                    + saveFailed.get()
                                    + "&7."
                              );
                              if (this.placed < amount) {
                                 AdvancedHuntCommand.this.sendDebugChat(
                                    player, "&e[DEBUG] Not enough valid spots found near you. Try moving to a flatter/open area."
                                 );
                              }

                              this.cancel();
                           } else {
                              if (this.placingDone && !pendingSaveBatch.isEmpty() && inFlightBatches.get() < 2) {
                                 List<Treasure> batch = new ArrayList<>(pendingSaveBatch);
                                 pendingSaveBatch.clear();
                                 inFlightBatches.incrementAndGet();
                                 AdvancedHuntCommand.this.plugin.getTreasureManager().addTreasuresBatchAsync(batch).thenRun(() -> {
                                    saved.addAndGet(batch.size());
                                    inFlightBatches.decrementAndGet();
                                 }).exceptionally(ex -> {
                                    saveFailed.addAndGet(batch.size());
                                    inFlightBatches.decrementAndGet();
                                    return null;
                                 });
                              }

                              int placedThisTick = 0;
                              int attemptsThisTick = 0;

                              while (
                                 !this.placingDone
                                    && placedThisTick < 250
                                    && attemptsThisTick < maxAttemptsPerTick
                                    && this.placed < amount
                                    && this.attempts < maxAttempts
                                    && (pendingSaveBatch.size() < 200 || inFlightBatches.get() < 2)
                              ) {
                                 this.attempts++;
                                 attemptsThisTick++;
                                 int dx = random.nextInt(25) - 12;
                                 int dz = random.nextInt(25) - 12;
                                 int dy = random.nextInt(17) - 8;
                                 int x = base.getBlockX() + dx;
                                 int y = base.getBlockY() + dy;
                                 int z = base.getBlockZ() + dz;
                                 int minY = base.getWorld().getMinHeight();
                                 int maxY = base.getWorld().getMaxHeight() - 1;
                                 if (y >= minY && y <= maxY) {
                                    Block block = base.getWorld().getBlockAt(x, y, z);
                                    if (MaterialUtils.isAir(block.getType())) {
                                       Location loc = block.getLocation();
                                       String key = loc.getWorld().getUID() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
                                       if (reserved.add(key) && AdvancedHuntCommand.this.plugin.getTreasureManager().getTreasureCoreAt(loc) == null) {
                                          AdvancedHuntCommand.PaletteEntry chosen = palette.get(random.nextInt(palette.size()));
                                          Treasure treasure = AdvancedHuntCommand.this.createDebugPlacedTreasure(
                                             collection, block, sharedRewards, chosen, debugPlacedKey
                                          );
                                          pendingSaveBatch.add(treasure);
                                          if (pendingSaveBatch.size() >= 200 && inFlightBatches.get() < 2) {
                                             List<Treasure> batch = new ArrayList<>(pendingSaveBatch);
                                             pendingSaveBatch.clear();
                                             inFlightBatches.incrementAndGet();
                                             AdvancedHuntCommand.this.plugin.getTreasureManager().addTreasuresBatchAsync(batch).thenRun(() -> {
                                                saved.addAndGet(batch.size());
                                                inFlightBatches.decrementAndGet();
                                             }).exceptionally(ex -> {
                                                saveFailed.addAndGet(batch.size());
                                                inFlightBatches.decrementAndGet();
                                                return null;
                                             });
                                          }

                                          this.placed++;
                                          placedThisTick++;
                                       }
                                    }
                                 }
                              }

                              if (amount > 0) {
                                 if (!this.placingDone) {
                                    MessageUtils.sendActionBar(
                                       player,
                                       "&e[DEBUG] Placing: &f"
                                          + this.placed
                                          + "&e/"
                                          + amount
                                          + "&7 (Saved: "
                                          + saved.get()
                                          + ", Failed: "
                                          + saveFailed.get()
                                          + ", Attempts: "
                                          + this.attempts
                                          + ")"
                                    );
                                 } else {
                                    MessageUtils.sendActionBar(
                                       player, "&e[DEBUG] Saving: &f" + saved.get() + "&e/" + this.placed + "&7 (Failed: " + saveFailed.get() + ")"
                                    );
                                 }
                              }
                           }
                        } else {
                           this.cancel();
                        }
                     }
                  })
                  .runTaskTimer(this.plugin, 1L, 1L);
            }
         }
      }
   }

   private void debugPlaceCollectionPlane(final Player player, String collectionName, String amountRaw) {
      final int amount;
      try {
         amount = Integer.parseInt(amountRaw);
      } catch (NumberFormatException var23) {
         this.sendDebugChat(player, "&c[DEBUG] Amount must be a number.");
         return;
      }

      if (amount <= 0) {
         this.sendDebugChat(player, "&c[DEBUG] Amount must be >= 1.");
      } else {
         final List<AdvancedHuntCommand.PaletteEntry> palette = this.getHotbarPaletteEntries(player);
         if (palette.isEmpty()) {
            this.sendDebugChat(player, "&c[DEBUG] Put at least one placeable block in your hotbar (slots 1-9). ");
         } else {
            Optional<Collection> collectionOpt = this.plugin.getCollectionManager().getCollectionByName(collectionName);
            if (!collectionOpt.isPresent()) {
               this.sendDebugChat(player, "&c[DEBUG] Unknown collection: &f" + collectionName);
            } else {
               final Collection collection = collectionOpt.get();
               this.sendDebugChat(player, "&e[DEBUG] Placing &f" + amount + "&e treasures &7(plane)&e for collection &b" + collection.getName() + "&e...");
               List<Reward> defaultRewards = new ArrayList<>();
               Optional.ofNullable(collection.getDefaultTreasureRewardPresetId())
                  .flatMap(defaultPresetId -> this.plugin.getRewardPresetManager().getPreset(RewardPresetType.TREASURE, defaultPresetId))
                  .ifPresent(preset -> defaultRewards.addAll(preset.getRewards()));
               final List<Reward> sharedRewards = defaultRewards.isEmpty()
                  ? Collections.emptyList()
                  : Collections.unmodifiableList(new ArrayList<>(defaultRewards));
               final NamespacedKey debugPlacedKey = new NamespacedKey(this.plugin, "debug_placed");
               final Random random = new Random();
               final Location base = player.getLocation().clone();
               final int y = Math.min(base.getWorld().getMaxHeight() - 1, base.getBlockY() + 1);
               final int side = (int)Math.ceil(Math.sqrt(amount));
               final int start = -side / 2;
               int batchSize = 400;
               final AtomicInteger saved = new AtomicInteger(0);
               final AtomicInteger saveFailed = new AtomicInteger(0);
               int saveBatchSize = 200;
               int maxInFlightBatches = 2;
               final AtomicInteger inFlightBatches = new AtomicInteger(0);
               final List<Treasure> pendingSaveBatch = new ArrayList<>(200);
               (new BukkitRunnable() {
                     int placed = 0;
                     int checked = 0;
                     int dx = 0;
                     int dz = 0;
                     boolean placingDone = false;

                     public void run() {
                        if (player.isOnline() && player.getWorld() != null) {
                           if (!this.placingDone && (this.placed >= amount || this.dz >= side)) {
                              this.placingDone = true;
                           }

                           if (this.placingDone && pendingSaveBatch.isEmpty() && inFlightBatches.get() == 0 && saved.get() + saveFailed.get() >= this.placed) {
                              MessageUtils.sendActionBar(
                                 player,
                                 "&a[DEBUG] Done: &f" + this.placed + "&a/" + amount + "&7 (Saved: " + saved.get() + ", Failed: " + saveFailed.get() + ")"
                              );
                              AdvancedHuntCommand.this.sendDebugChat(
                                 player,
                                 "&a[DEBUG] Placed &f"
                                    + this.placed
                                    + "&a/"
                                    + amount
                                    + "&a treasures on a plane.&7 Checked: &f"
                                    + this.checked
                                    + "&7. Saved: &f"
                                    + saved.get()
                                    + "&7, Failed: &f"
                                    + saveFailed.get()
                                    + "&7."
                              );
                              if (this.placed < amount) {
                                 AdvancedHuntCommand.this.sendDebugChat(player, "&e[DEBUG] Plane had blocked cells. Try an emptier area.");
                              }

                              this.cancel();
                           } else {
                              if (this.placingDone && !pendingSaveBatch.isEmpty() && inFlightBatches.get() < 2) {
                                 List<Treasure> batch = new ArrayList<>(pendingSaveBatch);
                                 pendingSaveBatch.clear();
                                 inFlightBatches.incrementAndGet();
                                 AdvancedHuntCommand.this.plugin.getTreasureManager().addTreasuresBatchAsync(batch).thenRun(() -> {
                                    saved.addAndGet(batch.size());
                                    inFlightBatches.decrementAndGet();
                                 }).exceptionally(ex -> {
                                    saveFailed.addAndGet(batch.size());
                                    inFlightBatches.decrementAndGet();
                                    return null;
                                 });
                              }

                              int placedThisTick = 0;

                              while (
                                 !this.placingDone
                                    && placedThisTick < 400
                                    && this.placed < amount
                                    && this.dz < side
                                    && (pendingSaveBatch.size() < 200 || inFlightBatches.get() < 2)
                              ) {
                                 int x = base.getBlockX() + start + this.dx;
                                 int z = base.getBlockZ() + start + this.dz;
                                 Block block = base.getWorld().getBlockAt(x, y, z);
                                 this.checked++;
                                 if (MaterialUtils.isAir(block.getType())
                                    && AdvancedHuntCommand.this.plugin.getTreasureManager().getTreasureCoreAt(block.getLocation()) == null) {
                                    AdvancedHuntCommand.PaletteEntry chosen = palette.get(random.nextInt(palette.size()));
                                    Treasure treasure = AdvancedHuntCommand.this.createDebugPlacedTreasure(
                                       collection, block, sharedRewards, chosen, debugPlacedKey
                                    );
                                    pendingSaveBatch.add(treasure);
                                    if (pendingSaveBatch.size() >= 200 && inFlightBatches.get() < 2) {
                                       List<Treasure> batch = new ArrayList<>(pendingSaveBatch);
                                       pendingSaveBatch.clear();
                                       inFlightBatches.incrementAndGet();
                                       AdvancedHuntCommand.this.plugin.getTreasureManager().addTreasuresBatchAsync(batch).thenRun(() -> {
                                          saved.addAndGet(batch.size());
                                          inFlightBatches.decrementAndGet();
                                       }).exceptionally(ex -> {
                                          saveFailed.addAndGet(batch.size());
                                          inFlightBatches.decrementAndGet();
                                          return null;
                                       });
                                    }

                                    this.placed++;
                                    placedThisTick++;
                                 }

                                 this.dx++;
                                 if (this.dx >= side) {
                                    this.dx = 0;
                                    this.dz++;
                                 }
                              }

                              if (amount > 0) {
                                 if (!this.placingDone) {
                                    MessageUtils.sendActionBar(
                                       player,
                                       "&e[DEBUG] Placing: &f"
                                          + this.placed
                                          + "&e/"
                                          + amount
                                          + "&7 (Saved: "
                                          + saved.get()
                                          + ", Failed: "
                                          + saveFailed.get()
                                          + ", Checked: "
                                          + this.checked
                                          + ")"
                                    );
                                 } else {
                                    MessageUtils.sendActionBar(
                                       player, "&e[DEBUG] Saving: &f" + saved.get() + "&e/" + this.placed + "&7 (Failed: " + saveFailed.get() + ")"
                                    );
                                 }
                              }
                           }
                        } else {
                           this.cancel();
                        }
                     }
                  })
                  .runTaskTimer(this.plugin, 1L, 1L);
            }
         }
      }
   }

   private void sendDebugChat(Player player, String message) {
      String prefix = this.plugin.getMessageManager().getMessage("prefix", false);
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
   }

   private void sendDebugField(Player player, String field, String value) {
      String prefix = this.plugin.getMessageManager().getMessage("prefix", false);
      player.sendMessage(prefix + ChatColor.YELLOW + "[DEBUG] " + ChatColor.GRAY + field + ": " + ChatColor.WHITE + this.formatDebugValue(value));
   }

   private String formatDebugValue(String value) {
      if (value == null) {
         return "<null>";
      } else {
         return value.isEmpty() ? "<empty>" : value.replace("\r", "\\r").replace("\n", "\\n");
      }
   }

   private List<AdvancedHuntCommand.PaletteEntry> getHotbarPaletteEntries(Player player) {
      List<AdvancedHuntCommand.PaletteEntry> entries = new ArrayList<>();
      ItemStack[] contents = player.getInventory().getContents();

      for (int slot = 0; slot <= 8 && contents.length > slot; slot++) {
         ItemStack item = contents[slot];
         if (item != null) {
            Material type = item.getType();
            if (!MaterialUtils.isAir(type) && type.isBlock()) {
               if (type == XMaterial.PLAYER_WALL_HEAD.get()) {
                  type = XMaterial.PLAYER_HEAD.get();
               }

               PlayerProfile ownerProfile = null;
               if (type == XMaterial.PLAYER_HEAD.get()) {
                  ItemMeta meta = item.getItemMeta();
                  if (meta instanceof SkullMeta) {
                     SkullMeta skullMeta = (SkullMeta)meta;

                     try {
                        ownerProfile = skullMeta.getOwnerProfile();
                     } catch (Throwable var11) {
                     }
                  }
               }

               entries.add(new AdvancedHuntCommand.PaletteEntry(type, ownerProfile, this.extractPaletteBlockEntityNbt(item)));
            }
         }
      }

      return entries;
   }

   private Treasure createDebugPlacedTreasure(
      Collection collection, Block block, List<Reward> rewards, AdvancedHuntCommand.PaletteEntry chosen, NamespacedKey debugPlacedKey
   ) {
      this.applyDebugPaletteEntry(block, chosen, debugPlacedKey);
      return new Treasure(
         this.plugin.getTreasureManager().generateUniqueTreasureId(),
         collection.getId(),
         block.getLocation(),
         rewards,
         this.captureBlockNbt(block),
         block.getType().name(),
         BlockUtils.getBlockStateString(block)
      );
   }

   private void applyDebugPaletteEntry(Block block, AdvancedHuntCommand.PaletteEntry chosen, NamespacedKey debugPlacedKey) {
      block.setType(chosen.type(), false);
      BlockState state = block.getState();
      if (state instanceof Skull && chosen.ownerProfile() != null) {
         Skull skullState = (Skull)state;

         try {
            skullState.setOwnerProfile(chosen.ownerProfile());
         } catch (Throwable var9) {
         }
      }

      if (state instanceof TileState) {
         if (chosen.blockEntityNbt() != null && !chosen.blockEntityNbt().isEmpty()) {
            try {
               NBT.modify(state, nbt -> {
                  try {
                     nbt.mergeCompound(NBT.parseNBT(chosen.blockEntityNbt()));
                  } catch (Throwable var3x) {
                  }
               });
            } catch (Throwable var8) {
            }
         }

         TileState tileState = (TileState)state;
         tileState.getPersistentDataContainer().set(debugPlacedKey, PersistentDataType.BYTE, (byte)1);
      }

      try {
         state.update(true, false);
      } catch (Throwable var7) {
      }
   }

   private String extractPaletteBlockEntityNbt(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         ItemMeta meta = item.getItemMeta();
         if (meta instanceof BlockStateMeta) {
            try {
               String blockStateNbt = (String)NBT.get(((BlockStateMeta)meta).getBlockState(), ReadableNBT::toString);
               if (blockStateNbt != null && !blockStateNbt.isEmpty()) {
                  return blockStateNbt;
               }
            } catch (Throwable var5) {
            }
         }

         try {
            return (String)NBT.get(item, nbt -> {
               if (!nbt.hasTag("BlockEntityTag")) {
                  return null;
               } else {
                  ReadableNBT blockEntityTag = nbt.getCompound("BlockEntityTag");
                  if (blockEntityTag == null) {
                     return null;
                  } else {
                     String blockEntityNbt = blockEntityTag.toString();
                     return blockEntityNbt != null && !blockEntityNbt.isEmpty() ? blockEntityNbt : null;
                  }
               }
            });
         } catch (Throwable var4) {
            return null;
         }
      }
   }

   private String captureBlockNbt(Block block) {
      if (block == null) {
         return null;
      } else {
         try {
            return (String)NBT.get(block.getState(), ReadableNBT::toString);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private void debugRemoveCollectionPlaced(final Player player, String collectionName) {
      Optional<Collection> collectionOpt = this.plugin.getCollectionManager().getCollectionByName(collectionName);
      if (!collectionOpt.isPresent()) {
         this.sendDebugChat(player, "&c[DEBUG] Unknown collection: &f" + collectionName);
      } else {
         Collection collection = collectionOpt.get();
         final List<TreasureCore> cores = new ArrayList<>(this.plugin.getTreasureManager().getTreasureCoresInCollection(collection.getId()));
         final int total = cores.size();
         this.sendDebugChat(player, "&e[DEBUG] Removing &f" + total + "&e treasures for collection &b" + collection.getName() + "&e...");
         this.plugin.getTreasureManager().removeCollection(collection.getId());
         final AtomicInteger blocksProcessed = new AtomicInteger(0);
         final AtomicInteger blocksRemoved = new AtomicInteger(0);
         final AtomicInteger blocksSkippedUnloaded = new AtomicInteger(0);
         final AtomicInteger dbDeleted = new AtomicInteger(-1);
         final AtomicInteger dbFailed = new AtomicInteger(0);
         this.plugin.getDataRepository().deleteTreasuresInCollection(collection.getId()).thenAccept(dbDeleted::set).exceptionally(ex -> {
            dbFailed.incrementAndGet();
            dbDeleted.set(0);
            return null;
         });
         int batchSize = 600;
         (new BukkitRunnable() {
               int index = 0;

               public void run() {
                  if (!player.isOnline()) {
                     this.cancel();
                  } else {
                     int processedThisTick = 0;

                     while (this.index < total && processedThisTick < 600) {
                        TreasureCore core = cores.get(this.index++);
                        processedThisTick++;
                        blocksProcessed.incrementAndGet();
                        Location loc = core.getLocation();
                        World world = loc.getWorld();
                        if (world != null) {
                           int cx = loc.getBlockX() >> 4;
                           int cz = loc.getBlockZ() >> 4;
                           if (!world.isChunkLoaded(cx, cz)) {
                              blocksSkippedUnloaded.incrementAndGet();
                           } else {
                              Block block = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                              if (!MaterialUtils.isAir(block.getType())) {
                                 block.setType(XMaterial.AIR.get(), false);
                                 blocksRemoved.incrementAndGet();
                              }
                           }
                        }
                     }

                     int deletedCount = dbDeleted.get();
                     int deletedClamped = deletedCount < 0 ? 0 : Math.min(deletedCount, total);
                     String deletePart = deletedCount >= 0
                        ? "&7 Deleted: &f" + deletedClamped + "&7/&f" + total
                        : "&7 Deleted: &f0&7/&f" + total + " &e(deleting...)";
                     MessageUtils.sendActionBar(
                        player,
                        "&e[DEBUG] Removing: &f"
                           + blocksProcessed.get()
                           + "&e/"
                           + total
                           + "&7 (Blocks: "
                           + blocksRemoved.get()
                           + ", Skipped: "
                           + blocksSkippedUnloaded.get()
                           + ") "
                           + deletePart
                     );
                     if (this.index >= total && dbDeleted.get() >= 0) {
                        MessageUtils.sendActionBar(player, "&a[DEBUG] Done: &f" + total + "&a treasures&7 (Deleted: " + dbDeleted.get() + ")");
                        AdvancedHuntCommand.this.sendDebugChat(
                           player,
                           "&a[DEBUG] Removed collection treasures.&7 Total: &f"
                              + total
                              + "&7, Blocks removed: &f"
                              + blocksRemoved.get()
                              + "&7, Skipped (unloaded): &f"
                              + blocksSkippedUnloaded.get()
                              + "&7, DB deleted: &f"
                              + dbDeleted.get()
                              + "&7."
                        );
                        if (dbFailed.get() > 0) {
                           AdvancedHuntCommand.this.sendDebugChat(player, "&c[DEBUG] Note: DB delete reported an error; check console logs.");
                        }

                        this.cancel();
                     }
                  }
               }
            })
            .runTaskTimer(this.plugin, 1L, 1L);
      }
   }

   private void migrate(CommandSender sender, String type, boolean force, boolean purge) {
      DataRepository targetRepo = this.createTargetRepository(type, sender);
      if (targetRepo != null) {
         sender.sendMessage(this.plugin.getMessageManager().getMessage("command.migration.started"));
         targetRepo.init();
         this.checkExistingDataAndMigrate(sender, type, targetRepo, force, purge);
      }
   }

   private DataRepository createTargetRepository(String type, CommandSender sender) {
      if (type.equalsIgnoreCase("YAML")) {
         return new YamlRepository(this.plugin);
      } else if (type.equalsIgnoreCase("SQLITE")) {
         return new SqlRepository(this.plugin, null, 0, null, null, null, true);
      } else {
         return type.equalsIgnoreCase("MYSQL") ? this.createMySQLRepository(sender) : null;
      }
   }

   private DataRepository createMySQLRepository(CommandSender sender) {
      String host = this.plugin.getConfig().getString("migration.target.host", "localhost");
      int port = this.plugin.getConfig().getInt("migration.target.port", 3306);
      String database = this.plugin.getConfig().getString("migration.target.database", "advancedhunt_target");
      String username = this.plugin.getConfig().getString("migration.target.username", "root");
      String password = this.plugin.getConfig().getString("migration.target.password", "password");
      boolean allDefaults = host.equals("localhost")
         && port == 3306
         && database.equals("advancedhunt_target")
         && username.equals("root")
         && password.equals("password");
      if (allDefaults) {
         sender.sendMessage(this.plugin.getMessageManager().getMessage("command.migration.mysql_not_configured"));
         return null;
      } else {
         return new SqlRepository(this.plugin, host, port, database, username, password, false);
      }
   }

   private void checkExistingDataAndMigrate(CommandSender sender, String type, DataRepository targetRepo, boolean force, boolean purge) {
      CompletableFuture.allOf(
            targetRepo.loadCollections(),
            targetRepo.getAllTreasureUUIDs(),
            targetRepo.getAllPlayerUUIDs(),
            targetRepo.loadRewardPresets(RewardPresetType.TREASURE),
            targetRepo.loadRewardPresets(RewardPresetType.COLLECTION)
         )
         .thenCompose(
            v -> {
               int existingCollections = this.safeGetSize(targetRepo.loadCollections());
               int existingTreasures = this.safeGetSize(targetRepo.getAllTreasureUUIDs());
               int existingPlayerData = this.safeGetSize(targetRepo.getAllPlayerUUIDs());
               int existingTreasurePresets = this.safeGetSize(targetRepo.loadRewardPresets(RewardPresetType.TREASURE));
               int existingCollectionPresets = this.safeGetSize(targetRepo.loadRewardPresets(RewardPresetType.COLLECTION));
               boolean hasExistingData = existingCollections > 0
                  || existingTreasures > 0
                  || existingPlayerData > 0
                  || existingTreasurePresets > 0
                  || existingCollectionPresets > 0;
               if (hasExistingData && !force && !purge) {
                  sender.sendMessage(
                     this.plugin
                        .getMessageManager()
                        .getMessage(
                           "command.migration.existing_data_abort",
                           "%collections%",
                           String.valueOf(existingCollections),
                           "%treasures%",
                           String.valueOf(existingTreasures),
                           "%players%",
                           String.valueOf(existingPlayerData)
                        )
                  );
                  targetRepo.shutdown();
                  CompletableFuture<Void> failed = new CompletableFuture<>();
                  failed.completeExceptionally(new IllegalStateException("Migration aborted - existing data"));
                  return failed;
               } else {
                  if (hasExistingData) {
                     sender.sendMessage(
                        this.plugin
                           .getMessageManager()
                           .getMessage(
                              "command.migration.warning_existing_data",
                              "%collections%",
                              String.valueOf(existingCollections),
                              "%treasures%",
                              String.valueOf(existingTreasures),
                              "%players%",
                              String.valueOf(existingPlayerData)
                           )
                     );
                  }

                  CompletableFuture<Void> prepareTarget;
                  if (purge && hasExistingData) {
                     prepareTarget = this.purgeTargetRepository(targetRepo);
                  } else {
                     prepareTarget = CompletableFuture.completedFuture(null);
                  }

                  return prepareTarget.thenCompose(
                     v2 -> {
                        AtomicInteger lastPercentSent = new AtomicInteger(-1);
                        AtomicReference<String> lastStageSent = new AtomicReference<>("");
                        final AtomicLong lastUpdateMs = new AtomicLong(System.currentTimeMillis());
                        final AtomicReference<MigrationService.MigrationProgress> lastProgress = new AtomicReference<>(
                           new MigrationService.MigrationProgress(0, "loading", 0, 0)
                        );
                        BukkitRunnable heartbeat;
                        if (sender instanceof Player) {
                           final Player player = (Player)sender;
                           heartbeat = new BukkitRunnable() {
                              public void run() {
                                 long now = System.currentTimeMillis();
                                 if (now - lastUpdateMs.get() >= 4000L) {
                                    MigrationService.MigrationProgress p = lastProgress.get();
                                    MessageUtils.sendActionBar(
                                       player,
                                       AdvancedHuntCommand.this.plugin
                                          .getMessageManager()
                                          .getMessage(
                                             "command.migration.progress_actionbar",
                                             false,
                                             "%percent%",
                                             String.valueOf(p.percent()),
                                             "%current%",
                                             String.valueOf(p.current()),
                                             "%total%",
                                             String.valueOf(p.total()),
                                             "%stage%",
                                             String.valueOf(p.stage())
                                          )
                                    );
                                 }
                              }
                           };
                           heartbeat.runTaskTimer(this.plugin, 40L, 40L);
                        } else {
                           heartbeat = null;
                        }

                        return this.plugin
                           .getMigrationService()
                           .migrate(
                              this.plugin.getDataRepository(),
                              targetRepo,
                              progress -> {
                                 lastProgress.set(progress);
                                 int percent = progress.percent();
                                 String stage = String.valueOf(progress.stage());
                                 long now = System.currentTimeMillis();
                                 boolean shouldSend = false;
                                 if (lastPercentSent.get() != percent) {
                                    shouldSend = true;
                                 } else if (!stage.equals(lastStageSent.get())) {
                                    shouldSend = true;
                                 } else if (now - lastUpdateMs.get() > 2000L) {
                                    shouldSend = true;
                                 }

                                 if (shouldSend) {
                                    lastPercentSent.set(percent);
                                    lastStageSent.set(stage);
                                    lastUpdateMs.set(now);
                                    Bukkit.getScheduler()
                                       .runTask(
                                          this.plugin,
                                          () -> {
                                             if (sender instanceof Player) {
                                                Player playerx = (Player)sender;
                                                MessageUtils.sendActionBar(
                                                   playerx,
                                                   this.plugin
                                                      .getMessageManager()
                                                      .getMessage(
                                                         "command.migration.progress_actionbar",
                                                         false,
                                                         "%percent%",
                                                         String.valueOf(progress.percent()),
                                                         "%current%",
                                                         String.valueOf(progress.current()),
                                                         "%total%",
                                                         String.valueOf(progress.total()),
                                                         "%stage%",
                                                         String.valueOf(progress.stage())
                                                      )
                                                );
                                             } else {
                                                sender.sendMessage(
                                                   this.plugin
                                                      .getMessageManager()
                                                      .getMessage(
                                                         "command.migration.progress",
                                                         "%percent%",
                                                         String.valueOf(progress.percent()),
                                                         "%current%",
                                                         String.valueOf(progress.current()),
                                                         "%total%",
                                                         String.valueOf(progress.total()),
                                                         "%stage%",
                                                         String.valueOf(progress.stage())
                                                      )
                                                );
                                             }
                                          }
                                       );
                                 }
                              }
                           )
                           .whenComplete((v3, t) -> {
                              if (heartbeat != null) {
                                 Bukkit.getScheduler().runTask(this.plugin, heartbeat::cancel);
                              }
                           });
                     }
                  );
               }
            }
         )
         .thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
            try {
               targetRepo.shutdown();
            } catch (Exception var5x) {
            }

            this.plugin.applyMigratedStorageConfig(type);
            sender.sendMessage(this.plugin.getMessageManager().getMessage("command.migration.success"));
            if (sender instanceof Player) {
               Player player = (Player)sender;
               MessageUtils.sendActionBar(player, "");
            }

            this.reload(sender);
            if (type != null && type.equalsIgnoreCase("MYSQL")) {
               this.resetMySQLConfig(sender);
            }
         }))
         .exceptionally(e -> {
            if (!(e.getCause() instanceof IllegalStateException) || !e.getCause().getMessage().equals("Migration aborted - existing data")) {
               sender.sendMessage(this.plugin.getMessageManager().getMessage("command.migration.failed"));
               this.plugin.getLogger().severe("Migration failed: " + e.getMessage());
            }

            if (sender instanceof Player) {
               Player player = (Player)sender;
               Bukkit.getScheduler().runTask(this.plugin, () -> MessageUtils.sendActionBar(player, ""));
            }

            targetRepo.shutdown();
            return null;
         });
   }

   private CompletableFuture<Void> purgeTargetRepository(DataRepository targetRepo) {
      CompletableFuture<Void> deletePresets = targetRepo.loadRewardPresets(RewardPresetType.TREASURE)
         .thenCompose(
            presets -> {
               if (presets != null && !presets.isEmpty()) {
                  CompletableFuture<?>[] futures = presets.stream()
                     .map(preset -> targetRepo.deleteRewardPreset(RewardPresetType.TREASURE, preset.getId()))
                     .toArray(CompletableFuture[]::new);
                  return CompletableFuture.allOf(futures);
               } else {
                  return CompletableFuture.completedFuture(null);
               }
            }
         )
         .thenCompose(
            v -> targetRepo.loadRewardPresets(RewardPresetType.COLLECTION)
               .thenCompose(
                  presets -> {
                     if (presets != null && !presets.isEmpty()) {
                        CompletableFuture<?>[] futures = presets.stream()
                           .map(preset -> targetRepo.deleteRewardPreset(RewardPresetType.COLLECTION, preset.getId()))
                           .toArray(CompletableFuture[]::new);
                        return CompletableFuture.allOf(futures);
                     } else {
                        return CompletableFuture.completedFuture(null);
                     }
                  }
               )
         );
      CompletableFuture<Void> deleteCollections = targetRepo.loadCollections()
         .thenCompose(
            collections -> {
               if (collections != null && !collections.isEmpty()) {
                  CompletableFuture<?>[] futures = collections.stream()
                     .map(collection -> targetRepo.deleteCollection(collection.getId()))
                     .toArray(CompletableFuture[]::new);
                  return CompletableFuture.allOf(futures);
               } else {
                  return CompletableFuture.completedFuture(null);
               }
            }
         );
      CompletableFuture<Void> deleteRemainingTreasures = targetRepo.getAllTreasureUUIDs().thenCompose(treasureIds -> {
         if (treasureIds != null && !treasureIds.isEmpty()) {
            CompletableFuture<?>[] futures = treasureIds.stream().map(targetRepo::deleteTreasure).toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
         } else {
            return CompletableFuture.completedFuture(null);
         }
      });
      CompletableFuture<Void> resetProgress = targetRepo.resetAllProgress().thenApply(v -> null);
      return deletePresets.<Void>thenCompose(v -> deleteCollections).thenCompose(v -> deleteRemainingTreasures).thenCompose(v -> resetProgress);
   }

   private int safeGetSize(CompletableFuture<? extends java.util.Collection<?>> future) {
      try {
         return future.join().size();
      } catch (Exception var3) {
         return 0;
      }
   }

   private void resetMySQLConfig(CommandSender sender) {
      this.plugin.getConfig().set("migration.target.host", "localhost");
      this.plugin.getConfig().set("migration.target.port", 3306);
      this.plugin.getConfig().set("migration.target.database", "advancedhunt_target");
      this.plugin.getConfig().set("migration.target.username", "root");
      this.plugin.getConfig().set("migration.target.password", "password");
      this.plugin.saveConfig();
      sender.sendMessage(this.plugin.getMessageManager().getMessage("command.migration.mysql_config_reset"));
   }

   private void glowBlock(final Player player) {
      if (player != null) {
         int seconds = 10;
         Block block = player.getTargetBlockExact(10);
         if (block == null) {
            player.sendMessage("§cLook at a block.");
         } else {
            final Location blockLoc = block.getLocation();
            final World world = blockLoc.getWorld();
            if (world != null) {
               final int entityId = allocateClientSideEntityId(world);
               UUID entityUuid = UUID.randomUUID();
               boolean spawned = PlatformAccess.get().spawnGlowingBlockMarkerForPlayer(player, entityId, entityUuid, blockLoc);
               if (spawned) {
                  (new BukkitRunnable() {
                     public void run() {
                        try {
                           if (!player.isOnline()) {
                              return;
                           }

                           PlatformAccess.get().destroyEntitiesForPlayer(player, entityId);
                        } catch (Throwable var2) {
                        }
                     }
                  }).runTaskLater(this.plugin, 200L);
                  player.sendMessage("§aGlowing block marker shown for 10s.");
               } else if (!ParticleUtils.supportsPerPlayerParticles()) {
                  player.sendMessage("§cPacketEvents missing; cannot show client-side marker.");
               } else {
                  int iterations = 100;
                  (new BukkitRunnable() {
                     int remaining = 100;

                     public void run() {
                        try {
                           if (!player.isOnline() || this.remaining-- <= 0) {
                              this.cancel();
                              return;
                           }

                           double step = 0.25;
                           double x0 = blockLoc.getX();
                           double y0 = blockLoc.getY();
                           double z0 = blockLoc.getZ();

                           for (double t = 0.0; t <= 1.0; t += step) {
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + t, y0 + 1.01, z0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + t, y0 + 1.01, z0 + 1.0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0, y0 + 1.01, z0 + t), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + 1.0, y0 + 1.01, z0 + t), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                           }

                           for (double t = 0.0; t <= 1.0; t += step) {
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + t, y0 + 0.01, z0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + t, y0 + 0.01, z0 + 1.0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0, y0 + 0.01, z0 + t), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + 1.0, y0 + 0.01, z0 + t), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                           }

                           for (double y = 0.0; y <= 1.0; y += step) {
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0, y0 + y, z0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + 1.0, y0 + y, z0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0, y0 + y, z0 + 1.0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                              ParticleUtils.spawnParticleForPlayer(player, new Location(world, x0 + 1.0, y0 + y, z0 + 1.0), "REDSTONE", 1, 0.0, 0.0, 0.0, 0.0);
                           }
                        } catch (Throwable var11) {
                        }
                     }
                  }).runTaskTimer(this.plugin, 0L, 2L);
                  player.sendMessage("§eParticle block marker shown for 10s.");
               }
            }
         }
      }
   }

   private static final class HelpRequest {
      private final String query;
      private final int page;

      private HelpRequest(String query, int page) {
         this.query = query;
         this.page = page;
      }

      private String query() {
         return this.query;
      }

      private int page() {
         return this.page;
      }
   }

   private static final class PaletteEntry {
      private final Material type;
      private final PlayerProfile ownerProfile;
      private final String blockEntityNbt;

      private PaletteEntry(Material type, PlayerProfile ownerProfile, String blockEntityNbt) {
         this.type = type;
         this.ownerProfile = ownerProfile;
         this.blockEntityNbt = blockEntityNbt;
      }

      private Material type() {
         return this.type;
      }

      private PlayerProfile ownerProfile() {
         return this.ownerProfile;
      }

      private String blockEntityNbt() {
         return this.blockEntityNbt;
      }
   }
}
