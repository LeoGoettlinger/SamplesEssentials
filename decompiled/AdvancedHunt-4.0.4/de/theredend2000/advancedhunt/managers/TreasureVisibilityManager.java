package de.theredend2000.advancedhunt.managers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock;
import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Cache;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Caffeine;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.platform.PlatformAccess;
import de.theredend2000.advancedhunt.util.HeadHelper;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import de.theredend2000.advancedhunt.util.MaterialUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TreasureVisibilityManager implements Listener {
   private static final Material DEFAULT_FURNITURE_BLOCK = Optional.ofNullable(XMaterial.GRAY_STAINED_GLASS.get()).orElse(Material.STONE);
   private static final String FURNITURE_MARKER_NAME = "Furniture";
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final CollectionManager collectionManager;
   private volatile String replaceBlockName = "barrier";
   private final Set<UUID> bypassPlayers = ConcurrentHashMap.newKeySet();
   private final Map<UUID, Boolean> availabilityCache = new ConcurrentHashMap<>();
   private final Map<UUID, Map<Long, Integer>> furnitureMarkers = new ConcurrentHashMap<>();
   private final Cache<UUID, HeadHelper.SkullProfileData> headProfileCache = Caffeine.newBuilder()
      .maximumSize(10000L)
      .expireAfterAccess(10L, TimeUnit.MINUTES)
      .build();
   private final Map<UUID, Set<UUID>> treasureFoundByCache = new ConcurrentHashMap<>();
   private final Set<UUID> headProfileLoading = ConcurrentHashMap.newKeySet();
   private final Cache<String, BlockData> blockDataCache = Caffeine.newBuilder().maximumSize(4000L).expireAfterAccess(30L, TimeUnit.MINUTES).build();
   private final Cache<Material, BlockData> materialDataCache = Caffeine.newBuilder().maximumSize(256L).expireAfterAccess(30L, TimeUnit.MINUTES).build();
   private final Cache<TreasureVisibilityManager.WrappedStateKey, WrappedBlockState> wrappedStateCache = Caffeine.newBuilder()
      .maximumSize(10000L)
      .expireAfterAccess(30L, TimeUnit.MINUTES)
      .build();
   private final Map<String, Material> materialNameCache = new ConcurrentHashMap<>();
   private final Map<UUID, AtomicInteger> worldEntityIdCounters = new ConcurrentHashMap<>();
   private BukkitTask availabilityTask;
   private Object packetListener;

   public TreasureVisibilityManager(Main plugin, TreasureManager treasureManager, CollectionManager collectionManager) {
      this.plugin = plugin;
      this.treasureManager = treasureManager;
      this.collectionManager = collectionManager;
   }

   public void start() {
      this.reloadReplaceBlock();
      Bukkit.getPluginManager().registerEvents(this, this.plugin);
      this.registerPacketListener();
      this.availabilityTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this::refreshAvailabilityStates, 200L, 1200L);
      Bukkit.getScheduler().runTaskLater(this.plugin, this::refreshAvailabilityStates, 40L);
   }

   public void stop() {
      HandlerList.unregisterAll(this);
      if (this.availabilityTask != null) {
         this.availabilityTask.cancel();
         this.availabilityTask = null;
      }

      this.unregisterPacketListener();
      this.treasureFoundByCache.clear();
      this.availabilityCache.clear();
      this.bypassPlayers.clear();
      this.headProfileCache.invalidateAll();
      this.headProfileLoading.clear();
      this.blockDataCache.invalidateAll();
      this.materialDataCache.invalidateAll();
      this.wrappedStateCache.invalidateAll();
      this.materialNameCache.clear();
      this.worldEntityIdCounters.clear();
      this.clearAllFurnitureMarkers();
   }

   public void reloadReplaceBlock() {
      String block = this.plugin.getConfig().getString("treasure-settings.replace-block", "BARRIER").toUpperCase();
      if (!block.equals("BARRIER") && !block.equals("AIR")) {
         block = "BARRIER";
      }

      this.replaceBlockName = block.toLowerCase();
      if (this.isPacketEventsReady()) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (data != null) {
               for (UUID treasureId : data.getFoundTreasures()) {
                  TreasureCore core = this.treasureManager.getTreasureCoreById(treasureId);
                  if (core != null) {
                     Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(core.getCollectionId());
                     if (collectionOpt.isPresent() && collectionOpt.get().isHideAfterFound()) {
                        Location loc = core.getLocation();
                        if (loc != null && loc.getWorld() != null && loc.getWorld().equals(player.getWorld())) {
                           WrappedBlockState state = this.getReplaceBlockState();
                           if (state != null) {
                              this.sendBlockChangeToPlayer(player, loc, state);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean isBypassEnabled(Player player) {
      if (player == null) {
         return false;
      } else {
         return !player.hasPermission("advancedhunt.treasure.bypass") ? false : this.bypassPlayers.contains(player.getUniqueId());
      }
   }

   public void setBypass(Player player, boolean enabled) {
      if (player != null) {
         if (enabled) {
            this.bypassPlayers.add(player.getUniqueId());
            this.sendVirtualBlocksInView(player);
         } else {
            this.bypassPlayers.remove(player.getUniqueId());
            this.clearFurnitureMarkers(player.getUniqueId());
            this.sendCollectionHiddenStateForPlayer(player);
            this.sendHideAfterFoundStateForPlayer(player);
         }
      }
   }

   private void sendCollectionHiddenStateForPlayer(Player player) {
      if (player != null && this.isPacketEventsReady()) {
         int viewDistance = this.getServerViewDistance();
         int cx = player.getLocation().getBlockX() >> 4;
         int cz = player.getLocation().getBlockZ() >> 4;
         World world = player.getWorld();

         for (int x = cx - viewDistance; x <= cx + viewDistance; x++) {
            for (int z = cz - viewDistance; z <= cz + viewDistance; z++) {
               for (TreasureCore core : this.treasureManager.getTreasureCoresInChunk(x, z)) {
                  if (core != null && this.isCollectionHidden(core.getCollectionId())) {
                     Location loc = core.getLocation();
                     if (loc != null && loc.getWorld() != null && loc.getWorld().equals(world)) {
                        WrappedBlockState airState = WrappedBlockState.getByString("minecraft:air");
                        if (airState != null) {
                           this.sendBlockChangeToPlayer(player, loc, airState);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void sendVirtualBlocksInView(Player player) {
      if (player != null && player.getWorld() != null && this.isPacketEventsReady()) {
         int viewDistance = this.getServerViewDistance();
         int cx = player.getLocation().getBlockX() >> 4;
         int cz = player.getLocation().getBlockZ() >> 4;
         World world = player.getWorld();

         for (int x = cx - viewDistance; x <= cx + viewDistance; x++) {
            for (int z = cz - viewDistance; z <= cz + viewDistance; z++) {
               for (TreasureCore core : this.treasureManager.getTreasureCoresInChunk(x, z)) {
                  if (core != null) {
                     Location loc = core.getLocation();
                     if (loc != null && loc.getWorld() != null && loc.getWorld().equals(world)) {
                        boolean collectionHidden = this.isCollectionHidden(core.getCollectionId());
                        boolean foundHidden = this.shouldHideFoundForPlayer(core, player);
                        if (collectionHidden || foundHidden) {
                           WrappedBlockState state = this.resolveWrappedBlockState(core, player);
                           if (state != null) {
                              this.sendBlockChangeToPlayer(player, loc, state);
                              this.scheduleVirtualExtras(player, core, loc);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void sendHideAfterFoundStateForPlayer(Player player) {
      if (player != null && this.isPacketEventsReady()) {
         PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
         if (data != null) {
            int viewDistance = this.getServerViewDistance();
            int cx = player.getLocation().getBlockX() >> 4;
            int cz = player.getLocation().getBlockZ() >> 4;

            for (int x = cx - viewDistance; x <= cx + viewDistance; x++) {
               for (int z = cz - viewDistance; z <= cz + viewDistance; z++) {
                  for (TreasureCore core : this.treasureManager.getTreasureCoresInChunk(x, z)) {
                     if (core != null) {
                        Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(core.getCollectionId());
                        if (collectionOpt.isPresent() && collectionOpt.get().isHideAfterFound()) {
                           Location loc = core.getLocation();
                           if (loc != null && loc.getWorld().equals(player.getWorld())) {
                              if (data.hasFound(core.getId())) {
                                 WrappedBlockState replaceState = this.getReplaceBlockState();
                                 if (replaceState != null) {
                                    this.sendBlockChangeToPlayer(player, loc, replaceState);
                                 }
                              } else {
                                 WrappedBlockState state = this.resolveWrappedBlockState(core, player);
                                 if (state != null) {
                                    this.sendBlockChangeToPlayer(player, loc, state);
                                    this.scheduleVirtualExtras(player, core, loc);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void hideFoundTreasureForPlayer(Player player, TreasureCore core) {
      if (player != null && core != null && this.isPacketEventsReady()) {
         Location loc = core.getLocation();
         if (loc != null && loc.getWorld() != null) {
            if (loc.getWorld().equals(player.getWorld())) {
               WrappedBlockState replaceState = this.getReplaceBlockState();
               if (replaceState != null) {
                  this.sendBlockChangeToPlayer(player, loc, replaceState);
               }
            }
         }
      }
   }

   public void restoreFoundTreasuresForPlayer(Player player, UUID collectionId) {
      if (player != null && this.isPacketEventsReady()) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
               if (this.isBypassEnabled(player)) {
                  this.sendVirtualBlocksInView(player);
               } else {
                  for (TreasureCore core : collectionId != null ? this.treasureManager.getTreasureCoresInCollection(collectionId) : new ArrayList()) {
                     if (core != null) {
                        Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(core.getCollectionId());
                        if (collectionOpt.isPresent() && collectionOpt.get().isHideAfterFound()) {
                           Location loc = core.getLocation();
                           if (loc != null && loc.getWorld() != null && loc.getWorld().equals(player.getWorld())) {
                              WrappedBlockState state = this.resolveWrappedBlockState(core, player);
                              if (state != null) {
                                 this.sendBlockChangeToPlayer(player, loc, state);
                                 this.scheduleVirtualExtras(player, core, loc);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }, 2L);
      }
   }

   public void restoreFoundTreasuresForPlayer(Player player) {
      if (player != null && this.isPacketEventsReady()) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
               if (this.isBypassEnabled(player)) {
                  this.sendVirtualBlocksInView(player);
               } else {
                  for (Collection collection : this.collectionManager.getAllCollections()) {
                     if (collection.isHideAfterFound()) {
                        for (TreasureCore core : this.treasureManager.getTreasureCoresInCollection(collection.getId())) {
                           if (core != null) {
                              Location loc = core.getLocation();
                              if (loc != null && loc.getWorld() != null && loc.getWorld().equals(player.getWorld())) {
                                 WrappedBlockState state = this.resolveWrappedBlockState(core, player);
                                 if (state != null) {
                                    this.sendBlockChangeToPlayer(player, loc, state);
                                    this.scheduleVirtualExtras(player, core, loc);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }, 2L);
      }
   }

   public void refreshAvailabilityStates() {
      for (Collection collection : this.collectionManager.getAllCollections()) {
         boolean actAvailable = this.collectionManager.isCollectionAvailable(collection);
         Boolean previous = this.availabilityCache.put(collection.getId(), actAvailable);
         boolean shouldBeHidden = this.shouldHideCollection(collection, actAvailable);
         if (previous == null) {
            if (shouldBeHidden) {
               this.hideCollectionTreasures(collection.getId());
            } else {
               this.restoreCollectionTreasures(collection.getId());
            }
         } else if (previous && !actAvailable) {
            this.hideCollectionTreasures(collection.getId());
         } else if (!previous && actAvailable && !shouldBeHidden) {
            this.restoreCollectionTreasures(collection.getId());
         }
      }
   }

   public void refreshCollectionVisibility(UUID collectionId) {
      Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(collectionId);
      if (collectionOpt.isPresent()) {
         this.refreshCollectionVisibility(collectionOpt.get());
      }
   }

   public void refreshCollectionVisibility(Collection collection) {
      if (collection != null) {
         boolean actAvailable = this.collectionManager.isCollectionAvailable(collection);
         this.availabilityCache.put(collection.getId(), actAvailable);
         boolean shouldBeHidden = this.shouldHideCollection(collection, actAvailable);
         if (shouldBeHidden) {
            this.hideCollectionTreasures(collection.getId());
         } else {
            this.restoreCollectionTreasures(collection.getId());
         }
      }
   }

   private boolean shouldHideCollection(Collection collection, boolean actAvailable) {
      return !collection.isHideWhenNotAvailable() ? false : !collection.isEnabled() || !actAvailable;
   }

   private boolean isCollectionHidden(UUID collectionId) {
      Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(collectionId);
      if (!collectionOpt.isPresent()) {
         return false;
      } else {
         Collection collection = collectionOpt.get();
         Boolean actAvailable = this.availabilityCache.get(collectionId);
         if (actAvailable == null) {
            actAvailable = this.collectionManager.isCollectionAvailable(collection);
         }

         return this.shouldHideCollection(collection, actAvailable);
      }
   }

   private void hideCollectionTreasures(UUID collectionId) {
      final List<TreasureCore> cores = new ArrayList<>(this.treasureManager.getTreasureCoresInCollection(collectionId));
      if (!cores.isEmpty()) {
         final TreasureVisibilityManager self = this;
         (new BukkitRunnable() {
            int index = 0;

            public void run() {
               int processed = 0;

               while (this.index < cores.size() && processed < 400) {
                  TreasureCore core = cores.get(this.index++);
                  processed++;
                  if (core != null) {
                     self.hideTreasureBlock(core);
                     if (self.isPacketEventsReady()) {
                        Location loc = core.getLocation();
                        if (loc != null && loc.getWorld() != null) {
                           for (Player player : Bukkit.getOnlinePlayers()) {
                              if (self.isBypassEnabled(player) && loc.getWorld().equals(player.getWorld())) {
                                 WrappedBlockState state = self.resolveWrappedBlockState(core, player);
                                 if (state != null) {
                                    self.sendBlockChangeToPlayer(player, loc, state);
                                    self.scheduleVirtualExtras(player, core, loc);
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (this.index >= cores.size()) {
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 1L, 1L);
      }
   }

   private void restoreCollectionTreasures(UUID collectionId) {
      final List<TreasureCore> cores = new ArrayList<>(this.treasureManager.getTreasureCoresInCollection(collectionId));
      if (!cores.isEmpty()) {
         final TreasureVisibilityManager self = this;
         (new BukkitRunnable() {
            int index = 0;

            public void run() {
               int processed = 0;

               while (this.index < cores.size() && processed < 200) {
                  TreasureCore core = cores.get(this.index++);
                  processed++;
                  if (core != null) {
                     self.restoreTreasureBlock(core);
                  }
               }

               if (this.index >= cores.size()) {
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 1L, 1L);
      }
   }

   private void hideTreasureBlock(TreasureCore core) {
      if (core != null && core.getLocation() != null) {
         Location loc = core.getLocation();
         World world = loc.getWorld();
         if (world != null) {
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (world.isChunkLoaded(cx, cz)) {
               if (this.isItemsAdder(core)) {
                  if (this.isItemsAdderFurniture(core)) {
                     ItemsAdderAdapter.removeCustomFurniture(loc);
                  } else if (!ItemsAdderAdapter.removeCustomBlock(loc)) {
                     world.getBlockAt(loc).setType(Material.AIR, false);
                  }
               } else {
                  Block block = world.getBlockAt(loc);
                  if (!MaterialUtils.isAir(block.getType())) {
                     block.setType(Material.AIR, false);
                  }
               }
            }
         }
      }
   }

   private void restoreTreasureBlock(TreasureCore core) {
      if (core != null && core.getLocation() != null) {
         Location loc = core.getLocation();
         World world = loc.getWorld();
         if (world != null) {
            int cx = loc.getBlockX() >> 4;
            int cz = loc.getBlockZ() >> 4;
            if (world.isChunkLoaded(cx, cz)) {
               if (this.isItemsAdder(core)) {
                  if (this.isItemsAdderFurniture(core)) {
                     ItemsAdderAdapter.spawnCustomFurniture(core.getBlockState(), loc);
                  } else {
                     ItemsAdderAdapter.placeCustomBlock(core.getBlockState(), loc);
                  }
               } else {
                  Block block = world.getBlockAt(loc);
                  if (MaterialUtils.isAir(block.getType())) {
                     Material type = this.resolveMaterial(core.getMaterial());
                     if (type != null) {
                        BlockData data = this.resolveBlockData(core, type);

                        try {
                           if (data != null) {
                              block.setBlockData(data, false);
                           } else {
                              block.setType(type, false);
                           }
                        } catch (Throwable var10) {
                           block.setType(type, false);
                        }

                        this.treasureManager.getFullTreasureAsync(core.getId()).thenAccept(treasure -> {
                           if (treasure != null) {
                              this.applyTreasureNbt(loc, treasure);
                           }
                        });
                     }
                  }
               }
            }
         }
      }
   }

   private void applyTreasureNbt(Location location, Treasure treasure) {
      if (location != null && treasure != null) {
         String nbtData = treasure.getNbtData();
         if (nbtData != null && !nbtData.isEmpty()) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
               try {
                  Block block = location.getBlock();
                  if (block == null) {
                     return;
                  }

                  BlockState state = block.getState();
                  if (HeadHelper.applySkullProfile(state, nbtData)) {
                     state.update(true, false);
                     return;
                  }

                  NBT.modify(state, nbt -> {
                     try {
                        ReadWriteNBT parsed = NBT.parseNBT(nbtData);
                        nbt.mergeCompound(parsed);
                     } catch (Throwable var3x) {
                     }
                  });
                  state.update(true, false);
               } catch (Throwable var4) {
               }
            });
         }
      }
   }

   private void registerPacketListener() {
      if (!this.isPacketEventsReady()) {
         this.plugin.getLogger().info("[TreasureVisibility] PacketEvents not available — bypass disabled.");
      } else {
         try {
            PacketListenerAbstract listener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
               public void onPacketSend(PacketSendEvent event) {
                  TreasureVisibilityManager.this.handlePacketSend(event);
               }
            };
            PacketEvents.getAPI().getEventManager().registerListener(listener);
            this.packetListener = listener;
         } catch (Throwable var2) {
         }
      }
   }

   private void unregisterPacketListener() {
      if (this.packetListener != null) {
         try {
            PacketEvents.getAPI().getEventManager().unregisterListener((PacketListenerAbstract)this.packetListener);
         } catch (Throwable var2) {
         }

         this.packetListener = null;
      }
   }

   private void handlePacketSend(PacketSendEvent event) {
      if (event != null) {
         Object playerObj = event.getPlayer();
         if (playerObj instanceof Player) {
            Player player = (Player)playerObj;
            boolean bypass = this.isBypassEnabled(player);
            boolean anyHideAfterFound = this.hasAnyHideAfterFoundCollections();
            if (bypass || anyHideAfterFound) {
               PacketTypeCommon packetType = event.getPacketType();
               if (packetType == Server.BLOCK_CHANGE) {
                  this.handleBlockChange(event, player, bypass);
               } else if (packetType == Server.MULTI_BLOCK_CHANGE) {
                  this.handleMultiBlockChange(event, player, bypass);
               } else if (packetType == Server.CHUNK_DATA) {
                  this.handleChunkData(event, player, bypass);
               }
            }
         }
      }
   }

   private void handleBlockChange(PacketSendEvent event, Player player, boolean bypass) {
      WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event);
      Vector3i pos = wrapper.getBlockPosition();
      if (pos != null) {
         Location loc = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
         TreasureCore core = this.treasureManager.getTreasureCoreAt(loc);
         if (core != null) {
            boolean collectionHidden = this.isCollectionHidden(core.getCollectionId());
            boolean foundHidden = this.shouldHideFoundForPlayer(core, player);
            if (!bypass || !collectionHidden && !foundHidden) {
               if (!bypass && !collectionHidden && foundHidden) {
                  WrappedBlockState replaceState = this.getReplaceBlockState();
                  if (replaceState == null) {
                     return;
                  }

                  wrapper.setBlockState(replaceState);
                  wrapper.write();
                  event.markForReEncode(true);
               }
            } else {
               WrappedBlockState blockState = this.resolveWrappedBlockState(core, player);
               if (blockState != null) {
                  wrapper.setBlockState(blockState);
                  wrapper.write();
                  event.markForReEncode(true);
                  this.scheduleVirtualExtras(player, core, loc);
               }
            }
         }
      }
   }

   private void handleMultiBlockChange(PacketSendEvent event, Player player, boolean bypass) {
      WrapperPlayServerMultiBlockChange wrapper = new WrapperPlayServerMultiBlockChange(event);
      Vector3i chunkPos = wrapper.getChunkPosition();
      if (chunkPos != null) {
         EncodedBlock[] blocks = wrapper.getBlocks();
         if (blocks != null && blocks.length != 0) {
            boolean changed = false;
            int baseX = chunkPos.getX() << 4;
            int baseZ = chunkPos.getZ() << 4;

            for (EncodedBlock block : blocks) {
               int x = baseX + block.getX();
               int y = block.getY();
               int z = baseZ + block.getZ();
               Location loc = new Location(player.getWorld(), x, y, z);
               TreasureCore core = this.treasureManager.getTreasureCoreAt(loc);
               if (core != null) {
                  boolean collectionHidden = this.isCollectionHidden(core.getCollectionId());
                  boolean foundHidden = this.shouldHideFoundForPlayer(core, player);
                  if (bypass && (collectionHidden || foundHidden)) {
                     WrappedBlockState state = this.resolveWrappedBlockState(core, player);
                     if (state != null) {
                        block.setBlockState(state);
                        changed = true;
                        this.scheduleVirtualExtras(player, core, loc);
                     }
                  } else if (!bypass && !collectionHidden && foundHidden) {
                     WrappedBlockState replaceState = this.getReplaceBlockState();
                     if (replaceState != null) {
                        block.setBlockState(replaceState);
                        changed = true;
                     }
                  }
               }
            }

            if (changed) {
               wrapper.setBlocks(blocks);
               wrapper.write();
               event.markForReEncode(true);
            }
         }
      }
   }

   private void handleChunkData(PacketSendEvent event, Player player, boolean bypass) {
      WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
      Column column = wrapper.getColumn();
      if (column != null) {
         int chunkX = column.getX();
         int chunkZ = column.getZ();
         List<TreasureCore> cores = this.treasureManager.getTreasureCoresInChunk(chunkX, chunkZ);
         if (!cores.isEmpty()) {
            BaseChunk[] sections = column.getChunks();
            if (sections != null && sections.length != 0) {
               int minY = player.getWorld().getMinHeight();
               boolean changed = false;

               for (TreasureCore core : cores) {
                  if (core != null) {
                     Location loc = core.getLocation();
                     if (loc != null && loc.getWorld() != null && loc.getWorld().equals(player.getWorld())) {
                        boolean collectionHidden = this.isCollectionHidden(core.getCollectionId());
                        boolean foundHidden = this.shouldHideFoundForPlayer(core, player);
                        WrappedBlockState state = null;
                        if (!bypass || !collectionHidden && !foundHidden) {
                           if (!bypass && !collectionHidden && foundHidden) {
                              state = this.getReplaceBlockState();
                           }
                        } else {
                           state = this.resolveWrappedBlockState(core, player);
                        }

                        if (state != null) {
                           int y = loc.getBlockY();
                           int sectionIndex = y - minY >> 4;
                           if (sectionIndex >= 0 && sectionIndex < sections.length) {
                              BaseChunk section = sections[sectionIndex];
                              if (section != null) {
                                 int localX = loc.getBlockX() & 15;
                                 int localY = y - minY & 15;
                                 int localZ = loc.getBlockZ() & 15;
                                 section.set(localX, localY, localZ, state);
                                 changed = true;
                                 if (bypass && (collectionHidden || foundHidden)) {
                                    this.scheduleVirtualExtras(player, core, loc);
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (changed) {
                  wrapper.setColumn(column);
                  wrapper.write();
                  event.markForReEncode(true);
               }
            }
         }
      }
   }

   private void sendBlockChangeToPlayer(Player player, Location loc, WrappedBlockState state) {
      if (player != null && loc != null && state != null) {
         try {
            WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), state);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
         } catch (Throwable var5) {
         }
      }
   }

   public boolean shouldHideFoundForPlayer(TreasureCore core, Player player) {
      if (core != null && player != null) {
         Optional<Collection> collectionOpt = this.collectionManager.getCollectionById(core.getCollectionId());
         if (collectionOpt.isPresent() && collectionOpt.get().isHideAfterFound()) {
            Collection collection = collectionOpt.get();
            if (collection.isSinglePlayerFind()) {
               return this.plugin.getParticleManager().isTreasureGloballyClaimed(core.getId());
            } else {
               PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
               return data != null && data.hasFound(core.getId());
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean hasAnyHideAfterFoundCollections() {
      for (Collection c : this.collectionManager.getAllCollections()) {
         if (c.isHideAfterFound()) {
            return true;
         }
      }

      return false;
   }

   private void scheduleVirtualExtras(Player player, TreasureCore core, Location loc) {
      if (HeadHelper.isHeadMaterialName(core.getMaterial())) {
         HeadHelper.SkullProfileData profileData = this.headProfileCache.getIfPresent(core.getId());
         if (profileData != null && profileData.hasRenderableData()) {
            this.sendHeadBlockEntityData(player, loc, profileData);
         } else {
            this.loadHeadProfileAsync(player, core, loc);
         }
      }

      if (this.isItemsAdder(core) && this.isItemsAdderFurniture(core)) {
         this.ensureFurnitureMarker(player, loc);
      }
   }

   private void loadHeadProfileAsync(Player player, TreasureCore core, Location loc) {
      if (this.headProfileLoading.add(core.getId())) {
         this.treasureManager.getFullTreasureAsync(core.getId()).thenAccept(treasure -> {
            try {
               if (treasure == null) {
                  return;
               }

               HeadHelper.SkullProfileData profileData = HeadHelper.getSkullProfileData(treasure.getNbtData());
               if (profileData != null && profileData.hasRenderableData()) {
                  this.headProfileCache.put(core.getId(), profileData);
                  Bukkit.getScheduler().runTask(this.plugin, () -> {
                     if (player.isOnline()) {
                        WrappedBlockState state = this.resolveWrappedBlockState(core, player);
                        if (state != null) {
                           this.sendBlockChangeToPlayer(player, loc, state);
                        }

                        this.sendHeadBlockEntityData(player, loc, profileData);
                     }
                  });
                  return;
               }
            } finally {
               this.headProfileLoading.remove(core.getId());
            }
         }).exceptionally(ex -> {
            this.headProfileLoading.remove(core.getId());
            return null;
         });
      }
   }

   private void sendHeadBlockEntityData(Player player, Location loc, HeadHelper.SkullProfileData profileData) {
      if (player != null && loc != null && profileData != null && profileData.hasRenderableData()) {
         PlatformAccess.get().sendSkullUpdatePacket(player, loc, profileData.texture(), profileData.ownerName());
      }
   }

   private void ensureFurnitureMarker(Player player, Location loc) {
      if (player != null && loc != null) {
         Map<Long, Integer> playerMarkers = this.furnitureMarkers.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
         long key = this.locationKey(loc);
         if (!playerMarkers.containsKey(key)) {
            int entityId = this.allocateEntityId(loc.getWorld());
            if (entityId != -1) {
               boolean spawned = PlatformAccess.get()
                  .spawnHologramArmorStandForPlayer(player, entityId, UUID.randomUUID(), loc.clone().add(0.5, 1.2, 0.5), "Furniture");
               if (spawned) {
                  playerMarkers.put(key, entityId);
               }
            }
         }
      }
   }

   private void clearFurnitureMarkers(UUID playerId) {
      Map<Long, Integer> markers = this.furnitureMarkers.remove(playerId);
      if (markers != null && !markers.isEmpty()) {
         Player player = Bukkit.getPlayer(playerId);
         if (player != null && player.isOnline()) {
            int[] ids = markers.values().stream().mapToInt(Integer::intValue).toArray();
            PlatformAccess.get().destroyEntitiesForPlayer(player, ids);
         }
      }
   }

   private void clearAllFurnitureMarkers() {
      for (UUID playerId : new ArrayList<>(this.furnitureMarkers.keySet())) {
         this.clearFurnitureMarkers(playerId);
      }
   }

   private boolean isItemsAdder(TreasureCore core) {
      return core != null && "ITEMS_ADDER".equalsIgnoreCase(core.getMaterial());
   }

   private boolean isItemsAdderFurniture(TreasureCore core) {
      if (core == null) {
         return false;
      } else {
         String blockState = core.getBlockState();
         return blockState != null && !blockState.isEmpty() ? ItemsAdderAdapter.isCustomFurnitureId(blockState) : false;
      }
   }

   private WrappedBlockState resolveWrappedBlockState(TreasureCore core, Player player) {
      if (core == null) {
         return null;
      } else {
         ClientVersion clientVersion = null;

         try {
            if (player != null) {
               clientVersion = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
            }
         } catch (Throwable var9) {
         }

         String blockState = core.getBlockState();
         String cacheKey = null;
         if (blockState != null && !blockState.isEmpty() && !this.isLegacyBlockData(blockState)) {
            cacheKey = blockState;
         } else if (core.getMaterial() != null && !core.getMaterial().isEmpty()) {
            cacheKey = "material:" + core.getMaterial();
         }

         if (cacheKey != null) {
            WrappedBlockState cached = this.wrappedStateCache.getIfPresent(new TreasureVisibilityManager.WrappedStateKey(cacheKey, clientVersion));
            if (cached != null) {
               return cached;
            }
         }

         BlockData data = this.resolveBlockData(core, this.resolveMaterial(core.getMaterial()));
         if (data != null) {
            try {
               WrappedBlockState state = SpigotConversionUtil.fromBukkitBlockData(data);
               if (state != null) {
                  if (cacheKey != null) {
                     this.wrappedStateCache.put(new TreasureVisibilityManager.WrappedStateKey(cacheKey, clientVersion), state);
                  }

                  return state;
               }
            } catch (Throwable var8) {
            }
         }

         if (blockState != null && !blockState.isEmpty() && !this.isLegacyBlockData(blockState)) {
            try {
               WrappedBlockState state = clientVersion != null
                  ? WrappedBlockState.getByString(clientVersion, blockState)
                  : WrappedBlockState.getByString(blockState);
               if (state != null && cacheKey != null) {
                  this.wrappedStateCache.put(new TreasureVisibilityManager.WrappedStateKey(cacheKey, clientVersion), state);
               }

               return state;
            } catch (Throwable var10) {
            }
         }

         return null;
      }
   }

   private BlockData resolveBlockData(TreasureCore core, Material fallbackMaterial) {
      if (core == null) {
         return null;
      } else if (this.isItemsAdder(core)) {
         if (this.isItemsAdderFurniture(core)) {
            return this.materialDataCache.get(DEFAULT_FURNITURE_BLOCK, Material::createBlockData);
         } else {
            String blockState = core.getBlockState();
            if (blockState != null && !blockState.isEmpty()) {
               BlockData cached = this.blockDataCache.getIfPresent(blockState);
               if (cached != null) {
                  return cached;
               }
            }

            BlockData data = ItemsAdderAdapter.getCustomBlockData(core.getBlockState());
            if (data != null) {
               if (core.getBlockState() != null && !core.getBlockState().isEmpty()) {
                  this.blockDataCache.put(core.getBlockState(), data);
               }

               return data;
            } else {
               return this.materialDataCache.get(DEFAULT_FURNITURE_BLOCK, Material::createBlockData);
            }
         }
      } else {
         String blockStatex = core.getBlockState();
         if (blockStatex != null && !blockStatex.isEmpty() && !this.isLegacyBlockData(blockStatex)) {
            BlockData cached = this.blockDataCache.getIfPresent(blockStatex);
            if (cached != null) {
               return cached;
            }

            try {
               BlockData data = Bukkit.createBlockData(blockStatex);
               if (data != null) {
                  this.blockDataCache.put(blockStatex, data);
               }

               return data;
            } catch (Throwable var7) {
            }
         }

         if (fallbackMaterial != null) {
            try {
               return this.materialDataCache.get(fallbackMaterial, Material::createBlockData);
            } catch (Throwable var6) {
            }
         }

         return null;
      }
   }

   private Material resolveMaterial(String materialName) {
      if (materialName != null && !materialName.isEmpty()) {
         String key = materialName.toUpperCase(Locale.ROOT);
         Material cached = this.materialNameCache.get(key);
         if (cached != null) {
            return cached;
         } else {
            try {
               Material material = Material.getMaterial(key);
               if (material != null) {
                  this.materialNameCache.put(key, material);
                  return material;
               }
            } catch (Throwable var5) {
            }

            return DEFAULT_FURNITURE_BLOCK;
         }
      } else {
         return null;
      }
   }

   private boolean isLegacyBlockData(String blockState) {
      if (blockState != null && !blockState.isEmpty()) {
         for (int i = 0; i < blockState.length(); i++) {
            if (!Character.isDigit(blockState.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void refreshChunksAroundPlayer(Player player) {
      if (player != null && player.getWorld() != null) {
         int viewDistance = this.getServerViewDistance();
         int cx = player.getLocation().getBlockX() >> 4;
         int cz = player.getLocation().getBlockZ() >> 4;

         for (int x = cx - viewDistance; x <= cx + viewDistance; x++) {
            for (int z = cz - viewDistance; z <= cz + viewDistance; z++) {
               player.getWorld().refreshChunk(x, z);
            }
         }
      }
   }

   public void refreshHideAfterFound(Collection collection) {
      if (collection != null && this.isPacketEventsReady()) {
         List<TreasureCore> cores = this.treasureManager.getTreasureCoresInCollection(collection.getId());
         if (!cores.isEmpty()) {
            boolean hiding = collection.isHideAfterFound();

            for (Player player : Bukkit.getOnlinePlayers()) {
               for (TreasureCore core : cores) {
                  if (core != null) {
                     Location loc = core.getLocation();
                     if (loc != null && loc.getWorld() != null && loc.getWorld().equals(player.getWorld())) {
                        boolean shouldHide = hiding && this.shouldHideFoundForPlayer(core, player);
                        if (shouldHide) {
                           WrappedBlockState replaceState = this.getReplaceBlockState();
                           if (replaceState != null) {
                              this.sendBlockChangeToPlayer(player, loc, replaceState);
                           }
                        } else {
                           WrappedBlockState state = this.resolveWrappedBlockState(core, player);
                           if (state != null) {
                              this.sendBlockChangeToPlayer(player, loc, state);
                              this.scheduleVirtualExtras(player, core, loc);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private int getServerViewDistance() {
      try {
         int dist = Bukkit.getViewDistance();
         return dist > 0 ? dist : 8;
      } catch (Throwable var2) {
         return 8;
      }
   }

   private int allocateEntityId(World world) {
      if (world == null) {
         return -1;
      } else {
         int min = 1000000000;
         int max = Integer.MAX_VALUE;
         AtomicInteger counter = this.worldEntityIdCounters
            .computeIfAbsent(world.getUID(), k -> new AtomicInteger(1000000000 + this.plugin.getRandom().nextInt(1000000)));
         int id = counter.updateAndGet(current -> current >= Integer.MAX_VALUE ? 1000000000 : current + 1);
         return id > 0 ? id : -1;
      }
   }

   private boolean isPacketEventsReady() {
      try {
         if (!Bukkit.getPluginManager().isPluginEnabled("packetevents") && !Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
            return false;
         }
      } catch (Throwable var3) {
         return false;
      }

      try {
         return PacketEvents.getAPI().isInitialized();
      } catch (Throwable var2) {
         return false;
      }
   }

   private long locationKey(Location loc) {
      if (loc != null && loc.getWorld() != null) {
         long posKey = this.packBlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
         UUID worldId = loc.getWorld().getUID();
         long worldHash = worldId.getMostSignificantBits() ^ worldId.getLeastSignificantBits();
         return posKey ^ worldHash;
      } else {
         return 0L;
      }
   }

   private long packBlockPos(int x, int y, int z) {
      long lx = x & 67108863;
      long ly = y & 4095;
      long lz = z & 67108863;
      return lx << 38 | lz << 12 | ly;
   }

   private WrappedBlockState getReplaceBlockState() {
      if (this.replaceBlockName.equals("barrier")) {
         try {
            BlockData barrierData = Material.BARRIER.createBlockData();
            return SpigotConversionUtil.fromBukkitBlockData(barrierData);
         } catch (Throwable var2) {
         }
      }

      return WrappedBlockState.getByString("minecraft:air");
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent event) {
      if (event != null && event.getChunk() != null) {
         int cx = event.getChunk().getX();
         int cz = event.getChunk().getZ();
         final List<TreasureCore> cores = this.treasureManager.getTreasureCoresInChunk(cx, cz);
         if (!cores.isEmpty()) {
            final TreasureVisibilityManager self = this;
            (new BukkitRunnable() {
               int index = 0;

               public void run() {
                  int processed = 0;

                  while (this.index < cores.size() && processed < 200) {
                     TreasureCore core = cores.get(this.index++);
                     processed++;
                     if (core != null) {
                        if (self.isCollectionHidden(core.getCollectionId())) {
                           self.hideTreasureBlock(core);
                           if (self.isPacketEventsReady()) {
                              Location loc = core.getLocation();
                              if (loc != null && loc.getWorld() != null) {
                                 for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (self.isBypassEnabled(player) && loc.getWorld().equals(player.getWorld())) {
                                       WrappedBlockState state = self.resolveWrappedBlockState(core, player);
                                       if (state != null) {
                                          self.sendBlockChangeToPlayer(player, loc, state);
                                          self.scheduleVirtualExtras(player, core, loc);
                                       }
                                    }
                                 }
                              }
                           }
                        } else {
                           self.restoreTreasureBlock(core);
                        }
                     }
                  }

                  if (this.index >= cores.size()) {
                     this.cancel();
                  }
               }
            }).runTaskTimer(this.plugin, 1L, 1L);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      if (event != null && event.getPlayer() != null) {
         UUID playerId = event.getPlayer().getUniqueId();
         this.bypassPlayers.remove(playerId);
         this.clearFurnitureMarkers(playerId);
      }
   }

   public Map<UUID, Set<UUID>> getTreasureFoundByCache() {
      return this.treasureFoundByCache;
   }

   private static final class WrappedStateKey {
      private final String key;
      private final ClientVersion clientVersion;

      private WrappedStateKey(String key, ClientVersion clientVersion) {
         this.key = key;
         this.clientVersion = clientVersion;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof TreasureVisibilityManager.WrappedStateKey)) {
            return false;
         } else {
            TreasureVisibilityManager.WrappedStateKey that = (TreasureVisibilityManager.WrappedStateKey)o;
            return Objects.equals(this.key, that.key) && this.clientVersion == that.clientVersion;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.key, this.clientVersion);
      }
   }
}
