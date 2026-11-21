package com.magmaguy.elitemobs.advancedcombat.constructs;

import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import java.util.ArrayList;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class PacketConstructService implements Listener, AutoCloseable {
   private static final long UPDATE_PERIOD_TICKS = 2L;
   private final Plugin plugin;
   private final Map<UUID, PacketConstructService.Session> sessions = new LinkedHashMap<>();
   private final Map<UUID, PacketConstructService.ViewerState> viewers = new HashMap<>();
   private final BukkitTask updateTask;
   private boolean closed;

   public PacketConstructService(Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 2L);
   }

   public Optional<PacketConstructService.Handle> spawn(Player owner, Location anchor, PacketConstructDefinition definition, int durationTicks) {
      return this.spawn(owner, anchor, definition, durationTicks, ignored -> {});
   }

   public Optional<PacketConstructService.Handle> spawn(
      Player owner, Location anchor, PacketConstructDefinition definition, int durationTicks, Consumer<UUID> onRemoved
   ) {
      Objects.requireNonNull(anchor, "anchor");
      Location fixed = anchor.clone();
      return this.spawnFollowing(owner, () -> fixed, definition, durationTicks, onRemoved);
   }

   public Optional<PacketConstructService.Handle> spawnFollowing(
      Player owner, Supplier<Location> anchor, PacketConstructDefinition definition, int durationTicks
   ) {
      return this.spawnFollowing(owner, anchor, definition, durationTicks, ignored -> {});
   }

   public Optional<PacketConstructService.Handle> spawnFollowing(
      Player owner, Supplier<Location> anchor, PacketConstructDefinition definition, int durationTicks, Consumer<UUID> onRemoved
   ) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(anchor, "anchor");
      Objects.requireNonNull(definition, "definition");
      Objects.requireNonNull(onRemoved, "onRemoved");
      if (!this.closed && Bukkit.isPrimaryThread() && durationTicks >= 1 && owner.isOnline() && owner.isValid() && !owner.isDead()) {
         Location initial = resolve(anchor);
         if (initial != null && initial.getWorld() != null && initial.getWorld().equals(owner.getWorld())) {
            long now = MonotonicTickClock.currentTick();
            long expiresAt = Long.MAX_VALUE - now < durationTicks ? Long.MAX_VALUE : now + durationTicks;
            UUID id = UUID.randomUUID();
            PacketConstructService.Session session = new PacketConstructService.Session(
               id, owner.getUniqueId(), definition, anchor, initial.getWorld().getUID(), expiresAt, onRemoved, new PacketConstructService.Layout()
            );
            this.sessions.put(id, session);
            this.reconcile();
            return Optional.of(new PacketConstructService.HandleImpl(id));
         } else {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public void deactivate(Player owner) {
      if (owner != null) {
         this.deactivate(owner.getUniqueId());
      }
   }

   public void deactivate(UUID ownerId) {
      if (ownerId != null) {
         this.sessions
            .values()
            .stream()
            .filter(session -> session.ownerId().equals(ownerId))
            .map(PacketConstructService.Session::id)
            .toList()
            .forEach(this::remove);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      this.deactivate(event.getPlayer());
      this.viewers.remove(event.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDeath(PlayerDeathEvent event) {
      this.deactivate(event.getEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldChange(PlayerChangedWorldEvent event) {
      this.deactivate(event.getPlayer());
      this.clearViewer(event.getPlayer(), false);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      UUID worldId = event.getWorld().getUID();
      this.sessions
         .values()
         .stream()
         .filter(session -> session.worldId().equals(worldId))
         .map(PacketConstructService.Session::id)
         .toList()
         .forEach(this::remove);

      for (Player player : List.copyOf(event.getWorld().getPlayers())) {
         this.clearViewer(player, false);
      }
   }

   private void tick() {
      if (!this.closed) {
         this.reconcile();
      }
   }

   private void reconcile() {
      if (!this.sessions.isEmpty() || !this.viewers.isEmpty()) {
         long now = MonotonicTickClock.currentTick();
         Map<UUID, PacketConstructService.Frame> frames = new LinkedHashMap<>();

         for (PacketConstructService.Session session : List.copyOf(this.sessions.values())) {
            Player owner = Bukkit.getPlayer(session.ownerId());
            Location anchor = resolve(session.anchor());
            if (now < session.expiresAtTick()
               && owner != null
               && owner.isOnline()
               && owner.isValid()
               && !owner.isDead()
               && anchor != null
               && anchor.getWorld() != null
               && anchor.getWorld().getUID().equals(session.worldId())) {
               frames.put(session.id(), new PacketConstructService.Frame(session, anchor, session.layout().blocks(session.definition(), anchor)));
            } else {
               this.remove(session.id());
            }
         }

         Set<UUID> online = new LinkedHashSet<>();

         for (Player viewer : Bukkit.getOnlinePlayers()) {
            online.add(viewer.getUniqueId());
            this.reconcileViewer(viewer, frames);
         }

         this.viewers.keySet().removeIf(playerId -> !online.contains(playerId));
      }
   }

   private void reconcileViewer(Player player, Map<UUID, PacketConstructService.Frame> frames) {
      PacketConstructService.ViewerState viewer = this.viewers.computeIfAbsent(player.getUniqueId(), ignored -> new PacketConstructService.ViewerState());
      Set<UUID> desired = new LinkedHashSet<>();

      for (PacketConstructService.Frame frame : frames.values()) {
         if (canSee(player, frame) && !frame.blocks().isEmpty()) {
            desired.add(frame.session().id());
            this.send(player, viewer.layers().replace(frame.session().id(), frame.blocks()));
         }
      }

      for (UUID stale : new ArrayList<>(viewer.visibleConstructs())) {
         if (!desired.contains(stale)) {
            this.send(player, viewer.layers().remove(stale));
         }
      }

      viewer.visibleConstructs().clear();
      viewer.visibleConstructs().addAll(desired);
      if (viewer.layers().isEmpty() && viewer.visibleConstructs().isEmpty()) {
         this.viewers.remove(player.getUniqueId(), viewer);
      }
   }

   private void remove(UUID constructId) {
      PacketConstructService.Session removed = this.sessions.remove(constructId);
      if (removed != null) {
         for (Entry<UUID, PacketConstructService.ViewerState> entry : new ArrayList<>(this.viewers.entrySet())) {
            PacketConstructService.ViewerState state = entry.getValue();
            if (state.visibleConstructs().remove(constructId)) {
               Player player = Bukkit.getPlayer(entry.getKey());
               List<PacketConstructLayerIndex.VisibilityChange<PacketConstructService.BlockCoordinate, String>> changes = state.layers().remove(constructId);
               if (player != null && player.isOnline()) {
                  this.send(player, changes);
               }

               if (state.layers().isEmpty() && state.visibleConstructs().isEmpty()) {
                  this.viewers.remove(entry.getKey(), state);
               }
            }
         }

         try {
            removed.onRemoved().accept(removed.id());
         } catch (RuntimeException var8) {
            this.plugin.getLogger().log(Level.FINE, "Could not report packet construct removal " + removed.id(), (Throwable)var8);
         }
      }
   }

   private void clearViewer(Player player, boolean sendRestoration) {
      PacketConstructService.ViewerState state = this.viewers.remove(player.getUniqueId());
      if (state != null) {
         for (UUID constructId : List.copyOf(state.visibleConstructs())) {
            List<PacketConstructLayerIndex.VisibilityChange<PacketConstructService.BlockCoordinate, String>> changes = state.layers().remove(constructId);
            if (sendRestoration && player.isOnline()) {
               this.send(player, changes);
            }
         }

         state.visibleConstructs().clear();
      }
   }

   private void send(Player player, List<PacketConstructLayerIndex.VisibilityChange<PacketConstructService.BlockCoordinate, String>> changes) {
      for (PacketConstructLayerIndex.VisibilityChange<PacketConstructService.BlockCoordinate, String> change : changes) {
         PacketConstructService.BlockCoordinate position = change.key();
         World world = Bukkit.getWorld(position.worldId());
         if (world != null && player.getWorld().equals(world) && world.isChunkLoaded(position.x() >> 4, position.z() >> 4)) {
            try {
               Location location = position.location(world);
               BlockData data = change.after().isPresent()
                  ? Bukkit.createBlockData(change.after().orElseThrow())
                  : world.getBlockAt(position.x(), position.y(), position.z()).getBlockData();
               player.sendBlockChange(location, data);
            } catch (RuntimeException var9) {
               this.plugin.getLogger().log(Level.FINE, "Could not update packet-only class construct for " + player.getName(), (Throwable)var9);
            }
         }
      }
   }

   private static boolean canSee(Player player, PacketConstructService.Frame frame) {
      if (player.isOnline() && player.isValid() && player.getWorld().equals(frame.anchor().getWorld())) {
         double range = frame.session().definition().viewRange();
         return player.getLocation().distanceSquared(frame.anchor()) <= range * range;
      } else {
         return false;
      }
   }

   private static Location resolve(Supplier<Location> anchor) {
      try {
         Location location = anchor.get();
         return location == null ? null : location.clone();
      } catch (RuntimeException var2) {
         return null;
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.updateTask.cancel();

         for (UUID constructId : new ArrayList<>(this.sessions.keySet())) {
            this.remove(constructId);
         }

         for (Player player : Bukkit.getOnlinePlayers()) {
            this.clearViewer(player, true);
         }

         this.sessions.clear();
         this.viewers.clear();
         HandlerList.unregisterAll(this);
      }
   }

   private record BlockCoordinate(UUID worldId, int x, int y, int z) {
      private Location location(World world) {
         return new Location(world, this.x, this.y, this.z);
      }
   }

   private record ChunkCoordinate(int x, int z) {
   }

   private record Frame(PacketConstructService.Session session, Location anchor, Map<PacketConstructService.BlockCoordinate, String> blocks) {
   }

   public interface Handle extends AutoCloseable {
      UUID id();

      boolean active();

      @Override
      void close();
   }

   private final class HandleImpl implements PacketConstructService.Handle {
      private final UUID id;

      private HandleImpl(UUID id) {
         this.id = id;
      }

      @Override
      public UUID id() {
         return this.id;
      }

      @Override
      public boolean active() {
         return PacketConstructService.this.sessions.containsKey(this.id);
      }

      @Override
      public void close() {
         if (!PacketConstructService.this.closed && PacketConstructService.this.sessions.containsKey(this.id)) {
            if (Bukkit.isPrimaryThread()) {
               PacketConstructService.this.remove(this.id);
            } else {
               Bukkit.getScheduler().runTask(PacketConstructService.this.plugin, () -> PacketConstructService.this.remove(this.id));
            }
         }
      }
   }

   private static final class Layout {
      private World world;
      private int x;
      private int y;
      private int z;
      private Map<PacketConstructService.ChunkCoordinate, Map<PacketConstructService.BlockCoordinate, String>> chunks = Map.of();
      private final Set<PacketConstructService.ChunkCoordinate> loaded = new LinkedHashSet<>();
      private Map<PacketConstructService.BlockCoordinate, String> visible = Map.of();

      private Map<PacketConstructService.BlockCoordinate, String> blocks(PacketConstructDefinition definition, Location anchor) {
         World currentWorld = anchor.getWorld();
         if (currentWorld == null) {
            return Map.of();
         } else {
            boolean changed = this.world != currentWorld || this.x != anchor.getBlockX() || this.y != anchor.getBlockY() || this.z != anchor.getBlockZ();
            if (changed) {
               this.world = currentWorld;
               this.x = anchor.getBlockX();
               this.y = anchor.getBlockY();
               this.z = anchor.getBlockZ();
               Map<PacketConstructService.ChunkCoordinate, Map<PacketConstructService.BlockCoordinate, String>> replacement = new LinkedHashMap<>();

               for (PacketConstructDefinition.BlockVisual visual : definition.blocks()) {
                  int blockX = this.x + visual.relativeX();
                  int blockY = this.y + visual.relativeY();
                  int blockZ = this.z + visual.relativeZ();
                  replacement.computeIfAbsent(new PacketConstructService.ChunkCoordinate(blockX >> 4, blockZ >> 4), ignored -> new LinkedHashMap<>())
                     .put(new PacketConstructService.BlockCoordinate(this.world.getUID(), blockX, blockY, blockZ), visual.serializedBlockData());
               }

               this.chunks = replacement;
               this.loaded.clear();
            }

            for (PacketConstructService.ChunkCoordinate chunk : this.chunks.keySet()) {
               changed |= this.world.isChunkLoaded(chunk.x(), chunk.z()) ? this.loaded.add(chunk) : this.loaded.remove(chunk);
            }

            if (changed) {
               Map<PacketConstructService.BlockCoordinate, String> replacement = new LinkedHashMap<>();

               for (PacketConstructService.ChunkCoordinate chunk : this.loaded) {
                  replacement.putAll(this.chunks.get(chunk));
               }

               this.visible = Map.copyOf(replacement);
            }

            return this.visible;
         }
      }
   }

   private record Session(
      UUID id,
      UUID ownerId,
      PacketConstructDefinition definition,
      Supplier<Location> anchor,
      UUID worldId,
      long expiresAtTick,
      Consumer<UUID> onRemoved,
      PacketConstructService.Layout layout
   ) {
   }

   private record ViewerState(PacketConstructLayerIndex<PacketConstructService.BlockCoordinate, String> layers, Set<UUID> visibleConstructs) {
      private ViewerState() {
         this(new PacketConstructLayerIndex<>(), new LinkedHashSet<>());
      }
   }
}
