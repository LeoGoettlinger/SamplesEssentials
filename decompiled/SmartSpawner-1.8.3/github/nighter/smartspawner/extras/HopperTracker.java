package github.nighter.smartspawner.extras;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.utils.BlockPos;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class HopperTracker implements Listener {
   private final SmartSpawner plugin;
   private final HopperRegistry registry;

   public HopperTracker(SmartSpawner plugin, HopperRegistry registry) {
      this.plugin = plugin;
      this.registry = registry;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   public void tryAdd(Block hopper) {
      if (hopper.getType() == Material.HOPPER) {
         if (hopper.getRelative(BlockFace.UP).getType() == Material.SPAWNER) {
            this.registry.add(new BlockPos(hopper.getLocation()));
         }
      }
   }

   public void removeBelowSpawner(Block spawner) {
      if (this.plugin.getHopperConfig().isHopperEnabled()) {
         Block hopper = spawner.getRelative(BlockFace.DOWN);
         if (hopper.getType() == Material.HOPPER) {
            this.registry.remove(new BlockPos(hopper.getLocation()));
         }
      }
   }

   public void scanLoadedWorlds() {
      for (World world : this.plugin.getServer().getWorlds()) {
         this.scanLoadedChunksInWorld(world);
      }
   }

   public void scanLoadedChunksInWorld(World world) {
      for (Chunk loadedChunk : world.getLoadedChunks()) {
         int x = loadedChunk.getX();
         int z = loadedChunk.getZ();
         Scheduler.runChunkTask(world, x, z, () -> this.scanChunkInternal(world, x, z));
      }
   }

   private void scanChunkInternal(World world, int chunkX, int chunkZ) {
      if (world.isChunkLoaded(chunkX, chunkZ)) {
         Chunk chunk = world.getChunkAt(chunkX, chunkZ);
         this.scanChunk(chunk);
      }
   }

   private void scanChunk(Chunk chunk) {
      for (BlockState state : chunk.getTileEntities(b -> b.getType() == Material.HOPPER, false)) {
         this.tryAdd(state.getBlock());
      }
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent e) {
      if (this.plugin.getHopperConfig().isHopperEnabled()) {
         this.scanLoadedChunksInWorld(e.getWorld());
      }
   }

   @EventHandler
   public void onWorldUnload(WorldUnloadEvent e) {
      if (this.plugin.getHopperConfig().isHopperEnabled()) {
         this.registry.removeWorld(e.getWorld());
      }
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent e) {
      if (this.plugin.getHopperConfig().isHopperEnabled()) {
         this.scanChunk(e.getChunk());
      }
   }

   @EventHandler
   public void onChunkUnload(ChunkUnloadEvent e) {
      if (this.plugin.getHopperConfig().isHopperEnabled()) {
         Chunk chunk = e.getChunk();
         this.registry.removeChunk(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
      }
   }

   @EventHandler
   public void onPlace(BlockPlaceEvent e) {
      if (this.plugin.getHopperConfig().isHopperEnabled()) {
         if (e.getBlockPlaced().getType() == Material.HOPPER) {
            this.tryAdd(e.getBlockPlaced());
         }
      }
   }

   @EventHandler
   public void onBreak(BlockBreakEvent e) {
      if (this.plugin.getHopperConfig().isHopperEnabled()) {
         if (e.getBlock().getType() == Material.HOPPER) {
            this.registry.remove(new BlockPos(e.getBlock().getLocation()));
         }
      }
   }
}
