package su.nightexpress.nightcore.util.blocktracker;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.manager.AbstractListener;

public class TrackListener<P extends NightCorePlugin> extends AbstractListener<P> {
   private static final String META_TRACK_FALLING_BLOCK = "tracker_falling_block";

   public TrackListener(@NonNull P plugin) {
      super(plugin);
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onLoad(WorldLoadEvent event) {
      PlayerBlockTracker.initWorld(event.getWorld());
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onUnload(WorldUnloadEvent event) {
      PlayerBlockTracker.terminateWorld(event.getWorld());
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onLoad(ChunkLoadEvent event) {
      PlayerBlockTracker.initChunk(event.getChunk());
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onUnload(ChunkUnloadEvent event) {
      PlayerBlockTracker.terminateChunk(event.getChunk());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlace(BlockPlaceEvent event) {
      if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
         PlayerBlockTracker.track(event.getBlock());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBreak(BlockBreakEvent event) {
      PlayerBlockTracker.unTrack(event.getBlock());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onExplode(BlockExplodeEvent event) {
      PlayerBlockTracker.unTrack(event.blockList());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onExplode(EntityExplodeEvent event) {
      PlayerBlockTracker.unTrack(event.blockList());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBurn(BlockBurnEvent event) {
      PlayerBlockTracker.unTrack(event.getBlock());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onFade(BlockFadeEvent event) {
      if (!(event.getBlock().getBlockData() instanceof Lightable)) {
         PlayerBlockTracker.unTrack(event.getBlock());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onForm(BlockFormEvent event) {
      PlayerBlockTracker.unTrack(event.getBlock());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onFlow(BlockFromToEvent event) {
      PlayerBlockTracker.unTrack(event.getBlock());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onGrow(BlockGrowEvent event) {
      PlayerBlockTracker.unTrack(event.getBlock());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onStructureGrow(StructureGrowEvent e) {
      if (e.getPlayer() != null) {
         PlayerBlockTracker.track(e.getBlocks().stream().<Block>map(BlockState::getBlock).toList());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onMultiPlace(BlockMultiPlaceEvent event) {
      PlayerBlockTracker.track(event.getReplacedBlockStates().stream().<Block>map(BlockState::getBlock).toList());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPistonExtend(BlockPistonExtendEvent event) {
      PlayerBlockTracker.shift(event.getDirection(), event.getBlocks());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPistonRetract(BlockPistonRetractEvent event) {
      if (event.isSticky()) {
         PlayerBlockTracker.shift(event.getDirection(), event.getBlocks());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onSpread(BlockSpreadEvent event) {
      PlayerBlockTracker.unTrack(event.getBlock());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEntityForm(EntityBlockFormEvent event) {
      PlayerBlockTracker.unTrack(event.getBlock());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityFallingSpawn(EntitySpawnEvent event) {
      Entity entity = event.getEntity();
      if (entity instanceof FallingBlock) {
         Block block = entity.getLocation().getBlock();
         if (PlayerBlockTracker.isTracked(block)) {
            entity.setMetadata("tracker_falling_block", new FixedMetadataValue(this.plugin, true));
            PlayerBlockTracker.unTrack(block);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityFallingLand(EntityChangeBlockEvent event) {
      Entity entity = event.getEntity();
      if (entity.hasMetadata("tracker_falling_block")) {
         PlayerBlockTracker.trackForce(event.getBlock());
      }
   }
}
