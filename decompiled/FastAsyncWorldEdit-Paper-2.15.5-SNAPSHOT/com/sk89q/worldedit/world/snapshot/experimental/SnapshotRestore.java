package com.sk89q.worldedit.world.snapshot.experimental;

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
import java.util.Map.Entry;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTagType;

public class SnapshotRestore {
   private final Map<BlockVector2, ArrayList<BlockVector3>> neededChunks = new LinkedHashMap<>();
   private final Snapshot snapshot;
   private final EditSession editSession;
   private final boolean restoreBiomes;
   private final boolean restoreEntities;
   private ArrayList<BlockVector2> missingChunks;
   private ArrayList<BlockVector2> errorChunks;
   private String lastErrorMessage;

   public SnapshotRestore(Snapshot snapshot, EditSession editSession, Region region) {
      this(snapshot, editSession, region, false, false);
   }

   public SnapshotRestore(Snapshot snapshot, EditSession editSession, Region region, boolean restoreBiomes, boolean restoreEntities) {
      this.snapshot = snapshot;
      this.editSession = editSession;
      this.restoreBiomes = restoreBiomes;
      this.restoreEntities = restoreEntities;
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
            this.neededChunks.put(chunkPos, new ArrayList<>());
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

      for (Entry<BlockVector2, ArrayList<BlockVector3>> entry : this.neededChunks.entrySet()) {
         BlockVector2 chunkPos = entry.getKey();

         try {
            Chunk chunk = this.snapshot.getChunk(chunkPos.toBlockVector3());

            for (BlockVector3 pos : entry.getValue()) {
               try {
                  this.editSession.setBlock(pos, chunk.getBlock(pos));
                  if (this.restoreBiomes && (pos.x() & 3) == 0 && (pos.y() & 3) == 0 && (pos.z() & 3) == 0) {
                     this.editSession.setBiome(pos, chunk.getBiome(pos));
                  }
               } catch (DataException var19) {
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
                     this.editSession.createEntity(location, entity);
                  }
               } catch (DataException var20) {
               }
            }
         } catch (MissingChunkException var21) {
            this.missingChunks.add(chunkPos);
         } catch (DataException | IOException var22) {
            this.errorChunks.add(chunkPos);
            this.lastErrorMessage = var22.getMessage();
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
