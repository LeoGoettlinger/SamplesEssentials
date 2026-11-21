package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

final class AbyssalGateManager implements Listener, AutoCloseable {
   private final Plugin plugin;
   private final Map<UUID, AbyssalGateOwnershipLedger> ledgersByWorld = new HashMap<>();
   private final Map<UUID, AbyssalGateManager.GateCast> casts = new LinkedHashMap<>();
   private boolean closed;

   AbyssalGateManager(Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   Optional<AbyssalGateManager.GateHandle> place(Player owner, Location aimed, int durationTicks) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(aimed, "aimed");
      if (!this.closed && aimed.getWorld() != null && durationTicks >= 1) {
         World world = aimed.getWorld();
         GateAxis axis = facingAxis(owner.getEyeLocation().getDirection());
         AbyssalGateOwnershipLedger ledger = this.ledger(world.getUID());
         AbyssalGateFootprint footprint = AbyssalGateFootprintPlanner.find(position(aimed), axis, new AbyssalGateManager.BukkitProbe(world, ledger, axis))
            .orElse(null);
         if (footprint == null) {
            return Optional.empty();
         } else {
            UUID gateId = UUID.randomUUID();
            ledger.reserve(gateId, footprint.blocks());
            boolean placed = false;

            try {
               BlockData portalData = Material.NETHER_PORTAL.createBlockData();
               if (portalData instanceof Orientable orientable) {
                  orientable.setAxis(axis == GateAxis.X ? Axis.X : Axis.Z);
               }

               for (GateBlockPosition position : footprint.blocks()) {
                  Block block = block(world, position);
                  if (block.getType() != Material.NETHER_PORTAL || !ledger.isOwned(position)) {
                     if (!block.getType().isAir()) {
                        return Optional.empty();
                     }

                     block.setBlockData(portalData.clone(), false);
                     if (block.getType() != Material.NETHER_PORTAL) {
                        return Optional.empty();
                     }
                  }
               }

               long now = MonotonicTickClock.currentTick();
               long expiresAt = Long.MAX_VALUE - now < durationTicks ? Long.MAX_VALUE : now + durationTicks;
               this.casts.put(gateId, new AbyssalGateManager.GateCast(gateId, owner.getUniqueId(), world.getUID(), footprint, expiresAt));
               placed = true;
               return Optional.of(new AbyssalGateManager.GateHandle(gateId));
            } finally {
               if (!placed) {
                  this.clearReservation(world, ledger, gateId);
               }
            }
         }
      } else {
         return Optional.empty();
      }
   }

   void tick(long currentTick) {
      if (!this.closed) {
         for (UUID gateId : this.casts.values().stream().filter(cast -> currentTick >= cast.expiresAtTick()).map(AbyssalGateManager.GateCast::gateId).toList()) {
            this.remove(gateId);
         }
      }
   }

   void removeOwner(UUID ownerId) {
      for (UUID gateId : this.casts.values().stream().filter(cast -> cast.playerOwnerId().equals(ownerId)).map(AbyssalGateManager.GateCast::gateId).toList()) {
         this.remove(gateId);
      }
   }

   void remove(AbyssalGateManager.GateHandle handle) {
      if (handle != null) {
         this.remove(handle.gateId());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPhysics(BlockPhysicsEvent event) {
      if (this.ownedPortal(event.getBlock())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent event) {
      if (this.ownedPortal(event.getBlock())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlace(BlockPlaceEvent event) {
      if (this.owned(event.getBlockPlaced())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onFlow(BlockFromToEvent event) {
      if (this.owned(event.getToBlock())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPistonExtend(BlockPistonExtendEvent event) {
      if (event.getBlocks().stream().anyMatch(this::owned)
         || event.getBlocks().stream().map(block -> block.getRelative(event.getDirection())).anyMatch(this::owned)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPistonRetract(BlockPistonRetractEvent event) {
      if (event.getBlocks().stream().anyMatch(this::owned)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityExplosion(EntityExplodeEvent event) {
      event.blockList().removeIf(this::owned);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockExplosion(BlockExplodeEvent event) {
      event.blockList().removeIf(this::owned);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPortalCreate(PortalCreateEvent event) {
      if (event.getBlocks().stream().anyMatch(state -> this.owned(state.getBlock()))) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerPortal(PlayerPortalEvent event) {
      if (this.touchesOwnedPortal(event.getPlayer(), event.getFrom())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityPortal(EntityPortalEvent event) {
      if (this.touchesOwnedPortal(event.getEntity(), event.getFrom())) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onChunkUnload(ChunkUnloadEvent event) {
      UUID worldId = event.getWorld().getUID();
      int chunkX = event.getChunk().getX();
      int chunkZ = event.getChunk().getZ();

      for (UUID gateId : this.casts
         .values()
         .stream()
         .filter(cast -> cast.worldId().equals(worldId))
         .filter(cast -> cast.footprint().blocks().stream().anyMatch(block -> block.x() >> 4 == chunkX && block.z() >> 4 == chunkZ))
         .map(AbyssalGateManager.GateCast::gateId)
         .toList()) {
         this.remove(gateId);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      UUID worldId = event.getWorld().getUID();

      for (UUID gateId : this.casts.values().stream().filter(cast -> cast.worldId().equals(worldId)).map(AbyssalGateManager.GateCast::gateId).toList()) {
         this.remove(gateId);
      }

      this.ledgersByWorld.remove(worldId);
   }

   @Override
   public void close() {
      if (!this.closed) {
         for (UUID gateId : new ArrayList<>(this.casts.keySet())) {
            this.remove(gateId);
         }

         this.casts.clear();
         this.ledgersByWorld.clear();
         this.closed = true;
         HandlerList.unregisterAll(this);
      }
   }

   private void remove(UUID gateId) {
      AbyssalGateManager.GateCast cast = this.casts.remove(gateId);
      if (cast != null) {
         World world = Bukkit.getWorld(cast.worldId());
         AbyssalGateOwnershipLedger ledger = this.ledgersByWorld.get(cast.worldId());
         if (ledger != null) {
            Set<GateBlockPosition> clearable = ledger.release(gateId);
            if (world != null) {
               clearBlocks(world, clearable);
            }

            if (ledger.isEmpty()) {
               this.ledgersByWorld.remove(cast.worldId());
            }
         }
      }
   }

   private void clearReservation(World world, AbyssalGateOwnershipLedger ledger, UUID gateId) {
      clearBlocks(world, ledger.release(gateId));
      if (ledger.isEmpty()) {
         this.ledgersByWorld.remove(world.getUID());
      }
   }

   private static void clearBlocks(World world, Set<GateBlockPosition> positions) {
      for (GateBlockPosition position : positions) {
         if (world.isChunkLoaded(position.x() >> 4, position.z() >> 4)) {
            Block block = block(world, position);
            if (block.getType() == Material.NETHER_PORTAL) {
               block.setType(Material.AIR, false);
            }
         }
      }
   }

   private boolean touchesOwnedPortal(Entity entity, Location from) {
      if (from != null && this.ownedPortal(from.getBlock())) {
         return true;
      } else {
         BoundingBox bounds = entity.getBoundingBox().expand(0.05);
         World world = entity.getWorld();
         int minX = floor(bounds.getMinX());
         int maxX = floor(bounds.getMaxX());
         int minY = floor(bounds.getMinY());
         int maxY = floor(bounds.getMaxY());
         int minZ = floor(bounds.getMinZ());
         int maxZ = floor(bounds.getMaxZ());

         for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
               for (int z = minZ; z <= maxZ; z++) {
                  Block block = world.getBlockAt(x, y, z);
                  if (this.ownedPortal(block)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private boolean ownedPortal(Block block) {
      return block.getType() == Material.NETHER_PORTAL && this.owned(block);
   }

   private boolean owned(Block block) {
      AbyssalGateOwnershipLedger ledger = this.ledgersByWorld.get(block.getWorld().getUID());
      return ledger != null && ledger.isOwned(position(block.getLocation()));
   }

   private AbyssalGateOwnershipLedger ledger(UUID worldId) {
      return this.ledgersByWorld.computeIfAbsent(worldId, ignored -> new AbyssalGateOwnershipLedger());
   }

   private static GateAxis facingAxis(Vector direction) {
      return Math.abs(direction.getX()) >= Math.abs(direction.getZ()) ? GateAxis.Z : GateAxis.X;
   }

   private static GateBlockPosition position(Location location) {
      return new GateBlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
   }

   private static Block block(World world, GateBlockPosition position) {
      return world.getBlockAt(position.x(), position.y(), position.z());
   }

   private static int floor(double value) {
      return (int)Math.floor(value);
   }

   private static final class BukkitProbe implements AbyssalGateFootprintPlanner.Probe {
      private final World world;
      private final AbyssalGateOwnershipLedger ledger;
      private final GateAxis axis;

      private BukkitProbe(World world, AbyssalGateOwnershipLedger ledger, GateAxis axis) {
         this.world = world;
         this.ledger = ledger;
         this.axis = axis;
      }

      @Override
      public boolean loaded(GateBlockPosition position) {
         return this.world.isChunkLoaded(position.x() >> 4, position.z() >> 4);
      }

      @Override
      public boolean solid(GateBlockPosition position) {
         return AbyssalGateManager.block(this.world, position).getType().isSolid();
      }

      @Override
      public boolean available(GateBlockPosition position) {
         Block block = AbyssalGateManager.block(this.world, position);
         if (block.getType().isAir()) {
            return true;
         } else {
            return block.getType() == Material.NETHER_PORTAL && this.ledger.isOwned(position)
               ? !(block.getBlockData() instanceof Orientable orientable && orientable.getAxis() != (this.axis == GateAxis.X ? Axis.X : Axis.Z))
               : false;
         }
      }
   }

   private record GateCast(UUID gateId, UUID playerOwnerId, UUID worldId, AbyssalGateFootprint footprint, long expiresAtTick) {
   }

   record GateHandle(UUID gateId) {
      GateHandle(UUID gateId) {
         Objects.requireNonNull(gateId, "gateId");
         this.gateId = gateId;
      }
   }
}
