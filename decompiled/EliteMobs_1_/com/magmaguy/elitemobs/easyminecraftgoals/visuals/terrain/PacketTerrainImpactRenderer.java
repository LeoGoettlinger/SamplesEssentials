package com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSAdapter;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactHandle;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactRenderer;
import com.magmaguy.elitemobs.magmacore.visuals.terrain.TerrainImpactRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PacketTerrainImpactRenderer implements TerrainImpactRenderer, Listener {
   private static final int VIEWER_RECONCILIATION_INTERVAL = 2;
   private static final int SOURCE_VALIDATION_INTERVAL = 2;
   private static final int MASK_REFRESH_INTERVAL = 10;
   private static final int MAX_ACTIVE_IMPACTS = 32;
   private static final long WARNING_INTERVAL_NANOS = 30000000000L;
   private final Plugin plugin;
   private final NMSAdapter adapter;
   private final TerrainImpactPlanner planner = new TerrainImpactPlanner();
   private final Map<UUID, PacketTerrainImpactRenderer.ActiveImpact> impacts = new LinkedHashMap<>();
   private final Map<UUID, PacketTerrainImpactRenderer.ViewerState> viewerStates = new HashMap<>();
   private final BlockData mask;
   private final BukkitTask tickTask;
   private long tick;
   private long nextWarningNanos;
   private boolean closed;

   public PacketTerrainImpactRenderer(Plugin plugin, NMSAdapter adapter) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.adapter = Objects.requireNonNull(adapter, "adapter");
      this.mask = Material.BARRIER.createBlockData();
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
   }

   @Override
   public TerrainImpactHandle show(TerrainImpactRequest request) {
      if (!this.closed && request != null && Bukkit.isPrimaryThread()) {
         Location center = request.center();
         if (center.getWorld() == null) {
            return TerrainImpactHandle.inactive();
         } else {
            TerrainImpactPlan plan;
            try {
               plan = this.planner.plan(request);
            } catch (RuntimeException var9) {
               this.warn("Could not plan a packet terrain impact", var9);
               return TerrainImpactHandle.inactive();
            }

            if (plan.tiles().isEmpty()) {
               return TerrainImpactHandle.inactive();
            } else {
               while (this.impacts.size() >= 32) {
                  PacketTerrainImpactRenderer.ActiveImpact oldest = this.impacts.values().iterator().next();
                  this.closeImpact(oldest);
               }

               PacketTerrainImpactRenderer.ActiveImpact impact = new PacketTerrainImpactRenderer.ActiveImpact(request);

               for (TerrainImpactPlan.Tile tile : plan.tiles()) {
                  try {
                     PacketTerrainImpactRenderer.TileVisual visual = this.createVisual(center.getWorld(), request, tile);
                     impact.tiles.put(visual.key, visual);
                  } catch (RuntimeException var8) {
                     this.warn("Skipped one packet terrain tile", var8);
                  }
               }

               if (impact.tiles.isEmpty()) {
                  return TerrainImpactHandle.inactive();
               } else {
                  this.impacts.put(impact.id, impact);
                  this.reconcileViewers(impact);
                  return impact;
               }
            }
         }
      } else {
         return TerrainImpactHandle.inactive();
      }
   }

   private PacketTerrainImpactRenderer.TileVisual createVisual(World world, TerrainImpactRequest request, TerrainImpactPlan.Tile tile) {
      PacketTerrainImpactRenderer.BlockKey key = new PacketTerrainImpactRenderer.BlockKey(world.getUID(), tile.x(), tile.y(), tile.z());
      Location location = new Location(world, tile.x(), tile.y(), tile.z());
      PacketEntityInterface packetEntity = this.adapter.createPacketEntity(EntityType.BLOCK_DISPLAY, location);

      try {
         BlockDisplay display = packetEntity.getBukkitEntity();
         display.setBlock(tile.blockData().clone());
         display.setViewRange((float)request.viewRange());
         display.setShadowRadius(0.0F);
         display.setShadowStrength(0.0F);
         display.setDisplayWidth(2.0F);
         display.setDisplayHeight(2.0F);
         display.setTeleportDuration(0);
         display.setInterpolationDelay(-1);
         display.setInterpolationDuration(0);
         display.setTransformation(identityTransformation());
         return new PacketTerrainImpactRenderer.TileVisual(key, tile.blockData().clone(), packetEntity, display, toBukkitTransformation(tile.transform()));
      } catch (RuntimeException var10) {
         try {
            packetEntity.remove();
         } catch (RuntimeException var9) {
         }

         throw var10;
      }
   }

   private void tick() {
      if (!this.closed) {
         this.tick++;

         for (PacketTerrainImpactRenderer.ActiveImpact impact : new ArrayList<>(this.impacts.values())) {
            if (impact.active) {
               impact.age++;
               if (impact.age == 1) {
                  int riseTicks = Math.min(3, Math.max(1, impact.request.durationTicks() / 4));
                  this.applyTransform(impact.tiles.values(), true, riseTicks);
               }

               int settleTicks = Math.min(5, Math.max(1, impact.request.durationTicks() / 3));
               int settleStart = Math.max(1, impact.request.durationTicks() - settleTicks);
               if (!impact.settling && impact.age >= settleStart) {
                  impact.settling = true;
                  this.applyTransform(impact.tiles.values(), false, settleTicks);
               }

               if (this.tick % 2L == 0L) {
                  this.validateSources(impact);
               }

               if (this.tick % 2L == 0L) {
                  this.reconcileViewers(impact);
               }

               if (impact.age >= impact.request.durationTicks()) {
                  this.closeImpact(impact);
               }
            }
         }

         if (this.tick % 10L == 0L) {
            this.refreshMasks();
         }
      }
   }

   private void applyTransform(Collection<PacketTerrainImpactRenderer.TileVisual> visuals, boolean impacted, int interpolationTicks) {
      for (PacketTerrainImpactRenderer.TileVisual visual : visuals) {
         try {
            visual.display.setInterpolationDelay(-1);
            visual.display.setInterpolationDuration(interpolationTicks);
            visual.display.setTransformation(impacted ? visual.impactedTransform : identityTransformation());
            visual.packetEntity.syncMetadata();
         } catch (RuntimeException var7) {
            this.warn("Could not animate one packet terrain tile", var7);
         }
      }
   }

   private void validateSources(PacketTerrainImpactRenderer.ActiveImpact impact) {
      World world = Bukkit.getWorld(impact.worldId);
      if (world == null) {
         this.closeImpact(impact);
      } else {
         List<PacketTerrainImpactRenderer.BlockKey> invalid = new ArrayList<>();

         for (PacketTerrainImpactRenderer.TileVisual visual : impact.tiles.values()) {
            if (!world.isChunkLoaded(visual.key.chunkX(), visual.key.chunkZ())) {
               invalid.add(visual.key);
            } else {
               Block block = world.getBlockAt(visual.key.x, visual.key.y, visual.key.z);
               if (!block.getBlockData().equals(visual.sourceBlockData)) {
                  invalid.add(visual.key);
               }
            }
         }

         if (!invalid.isEmpty()) {
            this.dropTiles(impact, invalid);
         }
      }
   }

   private void dropTiles(PacketTerrainImpactRenderer.ActiveImpact impact, Collection<PacketTerrainImpactRenderer.BlockKey> keys) {
      List<PacketTerrainImpactRenderer.TileVisual> removed = new ArrayList<>();

      for (PacketTerrainImpactRenderer.BlockKey key : keys) {
         PacketTerrainImpactRenderer.TileVisual visual = impact.tiles.remove(key);
         if (visual != null) {
            removed.add(visual);
         }
      }

      if (!removed.isEmpty()) {
         for (UUID viewerId : new LinkedHashSet<>(impact.viewers)) {
            PacketTerrainImpactRenderer.ViewerState state = this.viewerStates.get(viewerId);
            if (state != null) {
               try {
                  this.applyChanges(viewerId, state, state.layers.replace(impact.id, impact.tiles));
               } catch (RuntimeException var8) {
                  this.warn("Could not reconcile a removed packet terrain tile", var8);
                  this.purgeViewer(viewerId);
               }
            }
         }

         removed.forEach(visual -> visual.packetEntity.remove());
         if (impact.tiles.isEmpty()) {
            this.closeImpact(impact);
         }
      }
   }

   private void reconcileViewers(PacketTerrainImpactRenderer.ActiveImpact impact) {
      World world = Bukkit.getWorld(impact.worldId);
      if (world == null) {
         this.closeImpact(impact);
      } else {
         Set<UUID> candidates = new LinkedHashSet<>(impact.viewers);

         for (Player player : world.getPlayers()) {
            candidates.add(player.getUniqueId());
         }

         for (UUID playerId : candidates) {
            Player player = Bukkit.getPlayer(playerId);
            boolean shouldSee = this.shouldSee(player, impact);
            boolean currentlySees = impact.viewers.contains(playerId);
            if (shouldSee && !currentlySees) {
               this.addViewer(impact, player);
            } else if (!shouldSee && currentlySees) {
               this.removeViewer(impact, playerId);
            }
         }
      }
   }

   private boolean shouldSee(Player player, PacketTerrainImpactRenderer.ActiveImpact impact) {
      if (player != null && player.isOnline() && !player.isDead()) {
         Location center = impact.request.center();
         return center.getWorld() != null && player.getWorld().equals(center.getWorld())
            ? player.getLocation().distanceSquared(center) <= impact.request.viewRange() * impact.request.viewRange()
            : false;
      } else {
         return false;
      }
   }

   private void addViewer(PacketTerrainImpactRenderer.ActiveImpact impact, Player player) {
      if (player != null) {
         UUID viewerId = player.getUniqueId();
         PacketTerrainImpactRenderer.ViewerState state = this.viewerStates.computeIfAbsent(viewerId, ignored -> new PacketTerrainImpactRenderer.ViewerState());
         List<LayeredTerrainIndex.VisibilityChange<PacketTerrainImpactRenderer.BlockKey, PacketTerrainImpactRenderer.TileVisual>> changes = state.layers
            .replace(impact.id, impact.tiles);

         try {
            this.applyChanges(viewerId, state, changes);
            impact.viewers.add(viewerId);
         } catch (RuntimeException var9) {
            try {
               this.applyChanges(viewerId, state, state.layers.remove(impact.id));
            } catch (RuntimeException var8) {
               this.purgeViewer(viewerId);
            }

            this.warn("Could not show a packet terrain impact to one viewer", var9);
         }
      }
   }

   private void removeViewer(PacketTerrainImpactRenderer.ActiveImpact impact, UUID viewerId) {
      impact.viewers.remove(viewerId);
      PacketTerrainImpactRenderer.ViewerState state = this.viewerStates.get(viewerId);
      if (state != null) {
         this.applyChanges(viewerId, state, state.layers.remove(impact.id));
         if (state.layers.isEmpty()) {
            this.viewerStates.remove(viewerId);
         }
      }
   }

   private void applyChanges(
      UUID viewerId,
      PacketTerrainImpactRenderer.ViewerState state,
      List<LayeredTerrainIndex.VisibilityChange<PacketTerrainImpactRenderer.BlockKey, PacketTerrainImpactRenderer.TileVisual>> changes
   ) {
      Player player = Bukkit.getPlayer(viewerId);

      for (LayeredTerrainIndex.VisibilityChange<PacketTerrainImpactRenderer.BlockKey, PacketTerrainImpactRenderer.TileVisual> change : changes) {
         Optional<PacketTerrainImpactRenderer.TileVisual> before = change.before();
         Optional<PacketTerrainImpactRenderer.TileVisual> after = change.after();
         before.ifPresent(visual -> visual.packetEntity.hideFrom(viewerId));
         if (before.isEmpty() && after.isPresent()) {
            state.masked.add(change.key());
            this.mask(player, change.key());
         }

         after.ifPresent(visual -> visual.packetEntity.displayTo(viewerId));
         if (after.isEmpty()) {
            state.masked.remove(change.key());
            this.restore(player, change.key());
         }
      }
   }

   private void refreshMasks() {
      for (Entry<UUID, PacketTerrainImpactRenderer.ViewerState> entry : new ArrayList<>(this.viewerStates.entrySet())) {
         Player player = Bukkit.getPlayer(entry.getKey());
         if (player != null && player.isOnline()) {
            for (PacketTerrainImpactRenderer.BlockKey key : entry.getValue().masked) {
               this.mask(player, key);
            }
         } else {
            this.purgeViewer(entry.getKey());
         }
      }
   }

   private void mask(Player player, PacketTerrainImpactRenderer.BlockKey key) {
      if (canSendBlockChange(player, key)) {
         World world = player.getWorld();
         player.sendBlockChange(key.location(world), this.mask);
      }
   }

   private void restore(Player player, PacketTerrainImpactRenderer.BlockKey key) {
      if (canSendBlockChange(player, key)) {
         World world = player.getWorld();
         Block block = world.getBlockAt(key.x, key.y, key.z);
         player.sendBlockChange(key.location(world), block.getBlockData());
      }
   }

   private static boolean canSendBlockChange(Player player, PacketTerrainImpactRenderer.BlockKey key) {
      return player != null && player.isOnline() && player.getWorld().getUID().equals(key.worldId)
         ? player.getWorld().isChunkLoaded(key.chunkX(), key.chunkZ())
         : false;
   }

   private void closeImpact(PacketTerrainImpactRenderer.ActiveImpact impact) {
      if (impact != null && impact.active) {
         impact.active = false;
         this.impacts.remove(impact.id);

         for (UUID viewerId : new LinkedHashSet<>(impact.viewers)) {
            try {
               this.removeViewer(impact, viewerId);
            } catch (RuntimeException var6) {
               this.warn("Could not reconcile a closing packet terrain impact", var6);
               this.purgeViewer(viewerId);
            }
         }

         for (PacketTerrainImpactRenderer.TileVisual visual : impact.tiles.values()) {
            try {
               visual.packetEntity.remove();
            } catch (RuntimeException var5) {
               this.warn("Could not remove one packet terrain tile", var5);
            }
         }

         impact.tiles.clear();
      }
   }

   private void purgeViewer(UUID viewerId) {
      PacketTerrainImpactRenderer.ViewerState state = this.viewerStates.remove(viewerId);
      if (state != null) {
         for (PacketTerrainImpactRenderer.ActiveImpact impact : this.impacts.values()) {
            impact.viewers.remove(viewerId);
         }

         for (PacketTerrainImpactRenderer.TileVisual visual : this.allVisuals()) {
            try {
               visual.packetEntity.hideFrom(viewerId);
            } catch (RuntimeException var6) {
               this.warn("Could not hide one packet terrain tile", var6);
            }
         }

         Player player = Bukkit.getPlayer(viewerId);

         for (PacketTerrainImpactRenderer.BlockKey key : state.masked) {
            this.restore(player, key);
         }

         state.masked.clear();
      }
   }

   private List<PacketTerrainImpactRenderer.TileVisual> allVisuals() {
      List<PacketTerrainImpactRenderer.TileVisual> visuals = new ArrayList<>();

      for (PacketTerrainImpactRenderer.ActiveImpact impact : this.impacts.values()) {
         visuals.addAll(impact.tiles.values());
      }

      return visuals;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.purgeViewer(event.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      this.purgeViewer(event.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      this.purgeViewer(event.getEntity().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      this.purgeViewer(event.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onChunkUnload(ChunkUnloadEvent event) {
      UUID worldId = event.getWorld().getUID();
      int chunkX = event.getChunk().getX();
      int chunkZ = event.getChunk().getZ();

      for (PacketTerrainImpactRenderer.ActiveImpact impact : new ArrayList<>(this.impacts.values())) {
         if (impact.worldId.equals(worldId)) {
            List<PacketTerrainImpactRenderer.BlockKey> affected = impact.tiles
               .keySet()
               .stream()
               .filter(key -> key.chunkX() == chunkX && key.chunkZ() == chunkZ)
               .toList();
            if (!affected.isEmpty()) {
               this.dropTiles(impact, affected);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      UUID worldId = event.getWorld().getUID();

      for (PacketTerrainImpactRenderer.ActiveImpact impact : new ArrayList<>(this.impacts.values())) {
         if (impact.worldId.equals(worldId)) {
            this.closeImpact(impact);
         }
      }
   }

   @Override
   public void close() {
      if (!Bukkit.isPrimaryThread() && this.plugin.isEnabled()) {
         Bukkit.getScheduler().runTask(this.plugin, this::close);
      } else if (!this.closed) {
         this.closed = true;
         this.tickTask.cancel();

         for (PacketTerrainImpactRenderer.ActiveImpact impact : new ArrayList<>(this.impacts.values())) {
            this.closeImpact(impact);
         }

         for (UUID viewerId : new ArrayList<>(this.viewerStates.keySet())) {
            this.purgeViewer(viewerId);
         }

         HandlerList.unregisterAll(this);
      }
   }

   private void warn(String message, RuntimeException exception) {
      long now = System.nanoTime();
      if (now >= this.nextWarningNanos) {
         this.nextWarningNanos = now + 30000000000L;
         this.plugin
            .getLogger()
            .warning(message + ": " + exception.getClass().getSimpleName() + (exception.getMessage() == null ? "" : " - " + exception.getMessage()));
      }
   }

   private static Transformation toBukkitTransformation(TerrainImpactPlan.Transform transform) {
      return new Transformation(
         new Vector3f((float)transform.translationX(), (float)transform.translationY(), (float)transform.translationZ()),
         new Quaternionf((float)transform.rotationX(), (float)transform.rotationY(), (float)transform.rotationZ(), (float)transform.rotationW()),
         new Vector3f((float)transform.uniformScale(), (float)transform.uniformScale(), (float)transform.uniformScale()),
         new Quaternionf()
      );
   }

   private static Transformation identityTransformation() {
      return new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1.0F, 1.0F, 1.0F), new Quaternionf());
   }

   private final class ActiveImpact implements TerrainImpactHandle {
      private final UUID id = UUID.randomUUID();
      private final TerrainImpactRequest request;
      private final UUID worldId;
      private final Map<PacketTerrainImpactRenderer.BlockKey, PacketTerrainImpactRenderer.TileVisual> tiles = new LinkedHashMap<>();
      private final Set<UUID> viewers = new LinkedHashSet<>();
      private int age;
      private boolean settling;
      private boolean active = true;

      private ActiveImpact(TerrainImpactRequest request) {
         this.request = request;
         this.worldId = Objects.requireNonNull(request.center().getWorld()).getUID();
      }

      @Override
      public UUID id() {
         return this.id;
      }

      @Override
      public boolean active() {
         return this.active;
      }

      @Override
      public void close() {
         if (this.active) {
            if (!Bukkit.isPrimaryThread() && PacketTerrainImpactRenderer.this.plugin.isEnabled()) {
               Bukkit.getScheduler().runTask(PacketTerrainImpactRenderer.this.plugin, () -> PacketTerrainImpactRenderer.this.closeImpact(this));
            } else {
               PacketTerrainImpactRenderer.this.closeImpact(this);
            }
         }
      }
   }

   private record BlockKey(UUID worldId, int x, int y, int z) {
      private int chunkX() {
         return this.x >> 4;
      }

      private int chunkZ() {
         return this.z >> 4;
      }

      private Location location(World world) {
         return new Location(world, this.x, this.y, this.z);
      }
   }

   private static final class TileVisual {
      private final PacketTerrainImpactRenderer.BlockKey key;
      private final BlockData sourceBlockData;
      private final PacketEntityInterface packetEntity;
      private final BlockDisplay display;
      private final Transformation impactedTransform;

      private TileVisual(
         PacketTerrainImpactRenderer.BlockKey key,
         BlockData sourceBlockData,
         PacketEntityInterface packetEntity,
         BlockDisplay display,
         Transformation impactedTransform
      ) {
         this.key = key;
         this.sourceBlockData = sourceBlockData;
         this.packetEntity = packetEntity;
         this.display = display;
         this.impactedTransform = impactedTransform;
      }
   }

   private static final class ViewerState {
      private final LayeredTerrainIndex<PacketTerrainImpactRenderer.BlockKey, PacketTerrainImpactRenderer.TileVisual> layers = new LayeredTerrainIndex<>();
      private final Set<PacketTerrainImpactRenderer.BlockKey> masked = new LinkedHashSet<>();
   }
}
