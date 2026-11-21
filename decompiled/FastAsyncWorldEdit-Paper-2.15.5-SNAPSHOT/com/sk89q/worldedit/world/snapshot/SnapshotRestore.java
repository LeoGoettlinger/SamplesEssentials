package com.sk89q.worldedit.world.snapshot;

import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.MissingChunkException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTagType;

public class SnapshotRestore {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<BlockVector2, Set<BlockVector3>> neededChunks = new LinkedHashMap<>();
   private final ChunkStore chunkStore;
   private final EditSession editSession;
   private final boolean restoreBiomes;
   private final boolean restoreEntities;
   private final Region region;
   private ArrayList<BlockVector2> missingChunks;
   private ArrayList<BlockVector2> errorChunks;
   private String lastErrorMessage;

   public SnapshotRestore(ChunkStore chunkStore, EditSession editSession, Region region) {
      this(chunkStore, editSession, region, false, false);
   }

   public SnapshotRestore(ChunkStore chunkStore, EditSession editSession, Region region, boolean restoreBiomes, boolean restoreEntities) {
      this.chunkStore = chunkStore;
      this.editSession = editSession;
      this.restoreBiomes = restoreBiomes;
      this.restoreEntities = restoreEntities;
      this.region = region;
      if (region instanceof CuboidRegion) {
         this.findNeededCuboidChunks(region);
      } else {
         this.findNeededChunks(region);
      }
   }

   private void findNeededCuboidChunks(Region region) {
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3 max = region.getMaximumPoint();

      for (int x = min.x(); x <= max.x(); x++) {
         for (int y = min.y(); y <= max.y(); y++) {
            for (int z = min.z(); z <= max.z(); z++) {
               BlockVector3 pos = BlockVector3.at(x, y, z);
               this.checkAndAddBlock(pos);
            }
         }
      }
   }

   private void findNeededChunks(Region region) {
      for (BlockVector3 pos : region) {
         this.checkAndAddBlock(pos);
      }
   }

   private void checkAndAddBlock(BlockVector3 pos) {
      if (this.editSession.getMask() == null || this.editSession.getMask().test(pos)) {
         BlockVector2 chunkPos = ChunkStore.toChunk(pos);
         if (!this.neededChunks.containsKey(chunkPos)) {
            this.neededChunks.put(chunkPos, new LocalBlockVectorSet());
         }

         this.neededChunks.get(chunkPos).add(pos);
      }
   }

   public int getChunksAffected() {
      return this.neededChunks.size();
   }

   public void restore() throws MaxChangedBlocksException {
      this.missingChunks = new ArrayList<>();
      this.errorChunks = new ArrayList<>();

      for (Entry<BlockVector2, Set<BlockVector3>> entry : this.neededChunks.entrySet()) {
         BlockVector2 chunkPos = entry.getKey();

         try {
            Chunk chunk = this.chunkStore.getChunk(chunkPos, this.editSession.getWorld());

            for (BlockVector3 pos : entry.getValue()) {
               try {
                  this.editSession.setBlock(pos, chunk.getBlock(pos));
                  if (this.restoreBiomes && (pos.x() & 3) == 0 && (pos.y() & 3) == 0 && (pos.z() & 3) == 0) {
                     this.editSession.setBiome(pos, chunk.getBiome(pos));
                  }
               } catch (DataException var20) {
               }
            }

            if (this.restoreEntities) {
               try {
                  for (BaseEntity entity : chunk.getEntities()) {
                     LinCompoundTag tag = entity.getNbtReference().getValue();
                     LinListTag<LinDoubleTag> pos = tag.getListTag("Pos", LinTagType.doubleTag());
                     LinListTag<LinFloatTag> rotation = tag.getListTag("Rotation", LinTagType.floatTag());
                     double x = pos.get(0).value();
                     double y = pos.get(1).value();
                     double z = pos.get(2).value();
                     float yRot = rotation.get(0).value();
                     float xRot = rotation.get(1).value();
                     Location location = new Location(this.editSession.getWorld(), x, y, z, yRot, xRot);
                     BlockVector3 blockVector3 = BlockVector3.at(x, y, z);
                     if (this.region.contains(blockVector3) && (this.editSession.getMask() == null || this.editSession.getMask().test(blockVector3))) {
                        this.editSession.createEntity(location, entity);
                     }
                  }
               } catch (DataException var21) {
               }
            }
         } catch (MissingChunkException var22) {
            this.missingChunks.add(chunkPos);
         } catch (DataException | IOException var23) {
            LOGGER.info(() -> "Failed to load chunk at " + chunkPos, var23);
            this.errorChunks.add(chunkPos);
            this.lastErrorMessage = var23.getMessage();
         }
      }
   }

   public List<BlockVector2> getMissingChunks() {
      return this.missingChunks;
   }

   public List<BlockVector2> getErrorChunks() {
      return this.errorChunks;
   }

   public boolean hadTotalFailure() {
      return this.missingChunks.size() + this.errorChunks.size() == this.getChunksAffected();
   }

   public String getLastErrorMessage() {
      return this.lastErrorMessage;
   }
}
