package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.TreeGenerator;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.BlockState;

public abstract class FaweAdapter<TAG, SERVER_LEVEL> extends CachedBukkitAdapter implements IDelegateBukkitImplAdapter<TAG> {
   protected final BukkitImplAdapter<TAG> parent;
   protected int[] ibdToOrdinal = null;
   protected int[] ordinalToIbdID = null;
   protected boolean initialised = false;
   protected Map<String, List<Property<?>>> allBlockProperties = null;

   protected FaweAdapter(final BukkitImplAdapter<TAG> parent) {
      this.parent = parent;
   }

   @Override
   public void initializeRegistries() {
      this.parent.initializeRegistries();
   }

   @Override
   public boolean generateTree(final TreeGenerator.TreeType treeType, final EditSession editSession, BlockVector3 blockVector3, final World world) {
      TreeType bukkitType = BukkitWorld.toBukkitTreeType(treeType);
      if (bukkitType == TreeType.CHORUS_PLANT) {
         blockVector3 = blockVector3.add(BlockVector3.UNIT_Y);
      }

      BlockVector3 target = blockVector3;
      SERVER_LEVEL serverLevel = this.getServerLevel(world);
      List<BlockState> placed = TaskManager.taskManager().sync(() -> {
         this.preCaptureStates(serverLevel);

         Object var5x;
         try {
            if (world.generateTree(BukkitAdapter.adapt(world, target), bukkitType)) {
               return this.getCapturedBlockStatesCopy(serverLevel);
            }

            var5x = null;
         } finally {
            this.postCaptureBlockStates(serverLevel);
         }

         return (List<BlockState>)var5x;
      });
      if (placed != null && !placed.isEmpty()) {
         for (BlockState blockState : placed) {
            if (blockState != null && blockState.getType() != Material.AIR) {
               editSession.setBlock(blockState.getX(), blockState.getY(), blockState.getZ(), BukkitAdapter.adapt(blockState.getBlockData()));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public void mapFromGlobalPalette(char[] data) {
      assert data.length == 4096;

      this.ensureInit();

      for (int i = 0; i < 4096; i++) {
         data[i] = (char)this.ibdToOrdinal[data[i]];
      }
   }

   public void mapWithPalette(char[] data, char[] paletteToOrdinal) {
      for (int i = 0; i < 4096; i++) {
         char paletteVal = data[i];
         char val = paletteToOrdinal[paletteVal];

         assert val != '\uffff';

         data[i] = val;
      }
   }

   protected abstract void ensureInit();

   protected abstract void preCaptureStates(SERVER_LEVEL serverLevel);

   protected abstract List<BlockState> getCapturedBlockStatesCopy(SERVER_LEVEL serverLevel);

   protected abstract void postCaptureBlockStates(SERVER_LEVEL serverLevel);

   protected abstract SERVER_LEVEL getServerLevel(World world);
}
