package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_1;

import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.util.NbtUtils;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import org.apache.logging.log4j.Logger;

public class PaperweightGetBlocks_Copy implements IChunkGet {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Map<BlockVector3, FaweCompoundTag> tiles = new HashMap<>();
   private final Set<FaweCompoundTag> entities = new HashSet<>();
   private final char[][] blocks;
   private final int minHeight;
   private final int maxHeight;
   private final int chunkX;
   private final int chunkZ;
   final ServerLevel serverLevel;
   final LevelChunk levelChunk;
   private Holder<Biome>[][] biomes = null;

   protected PaperweightGetBlocks_Copy(LevelChunk levelChunk) {
      this.levelChunk = levelChunk;
      this.serverLevel = levelChunk.level;
      this.minHeight = this.serverLevel.getMinY();
      this.maxHeight = this.serverLevel.getMaxY() - 1;
      this.blocks = new char[this.getSectionCount()][];
      this.chunkX = levelChunk.getPos().x();
      this.chunkZ = levelChunk.getPos().z();
   }

   protected void storeTile(BlockEntity blockEntity) {
      LinValueOutput output = PaperweightPlatformAdapter.createOutput();
      blockEntity.saveWithId(output);
      this.tiles
         .put(
            BlockVector3.at(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ()),
            FaweCompoundTag.of(output::buildResult)
         );
   }

   protected void storeEntity(Entity entity) {
      LinValueOutput output = PaperweightPlatformAdapter.createOutput();
      entity.save(output);
      this.entities.add(FaweCompoundTag.of(output::buildResult));
   }

   @Override
   public Collection<FaweCompoundTag> entities() {
      return this.entities;
   }

   @Override
   public Set<com.sk89q.worldedit.entity.Entity> getFullEntities() {
      throw new UnsupportedOperationException("Cannot get full entities from GET copy.");
   }

   @Nullable
   @Override
   public FaweCompoundTag entity(final UUID uuid) {
      for (FaweCompoundTag tag : this.entities) {
         if (uuid.equals(NbtUtils.uuid(tag))) {
            return tag;
         }
      }

      return null;
   }

   @Override
   public boolean isCreateCopy() {
      return false;
   }

   @Override
   public int setCreateCopy(boolean createCopy) {
      return -1;
   }

   @Override
   public void setLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
   }

   @Override
   public void setSkyLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
   }

   @Override
   public void setHeightmapToGet(HeightMapType type, int[] data) {
   }

   @Override
   public int getMaxY() {
      return this.maxHeight;
   }

   @Override
   public int getMinY() {
      return this.minHeight;
   }

   @Override
   public int getMaxSectionPosition() {
      return this.maxHeight >> 4;
   }

   @Override
   public int getMinSectionPosition() {
      return this.minHeight >> 4;
   }

   @Override
   public int getX() {
      return this.chunkX;
   }

   @Override
   public int getZ() {
      return this.chunkZ;
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      Holder<Biome> biome = this.biomes[(y >> 4) - this.getMinSectionPosition()][(y & 12) << 2 | z & 12 | (x & 12) >> 2];
      return PaperweightPlatformAdapter.adapt(biome, this.serverLevel);
   }

   @Override
   public void removeSectionLighting(int layer, boolean sky) {
   }

   @Override
   public boolean trim(boolean aggressive, int layer) {
      return false;
   }

   @Override
   public IBlocks reset() {
      return null;
   }

   @Override
   public int getSectionCount() {
      return this.serverLevel.getSectionsCount();
   }

   protected void storeSection(int layer, char[] data) {
      this.blocks[layer] = data;
   }

   protected void storeBiomes(int layer, PalettedContainerRO<Holder<Biome>> biomeData) {
      if (this.biomes == null) {
         this.biomes = new Holder[this.getSectionCount()][];
      }

      if (this.biomes[layer] == null) {
         this.biomes[layer] = new Holder[64];
      }

      if (biomeData instanceof PalettedContainer<Holder<Biome>> palettedContainer) {
         if (PaperLib.isPaper()) {
            for (int i = 0; i < 64; i++) {
               this.biomes[layer][i] = (Holder<Biome>)palettedContainer.get(i);
            }
         } else {
            try {
               for (int i = 0; i < 64; i++) {
                  this.biomes[layer][i] = (Holder)PaperweightPlatformAdapter.PALETTED_CONTAINER_GET.invoke((PalettedContainer)palettedContainer, (int)i);
               }
            } catch (Throwable var5) {
               throw new RuntimeException(var5);
            }
         }
      } else {
         LOGGER.error(
            "Cannot correctly save biomes to history. Expected class type {} but got {}",
            PalettedContainer.class.getSimpleName(),
            biomeData.getClass().getSimpleName()
         );
      }
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      BlockState state = BlockTypesCache.states[this.get(x, y, z)];
      return state.toBaseBlock((IBlocks)this, x, y, z);
   }

   @Override
   public boolean hasSection(int layer) {
      layer -= this.getMinSectionPosition();
      return this.blocks[layer] != null;
   }

   @Override
   public char[] load(int layer) {
      layer -= this.getMinSectionPosition();
      if (this.blocks[layer] == null) {
         this.blocks[layer] = new char[4096];
         Arrays.fill(this.blocks[layer], '\u0001');
      }

      return this.blocks[layer];
   }

   @Override
   public char[] loadIfPresent(int layer) {
      layer -= this.getMinSectionPosition();
      return this.blocks[layer];
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return BlockTypesCache.states[this.get(x, y, z)];
   }

   @Override
   public Map<BlockVector3, FaweCompoundTag> tiles() {
      return this.tiles;
   }

   @Nullable
   @Override
   public FaweCompoundTag tile(final int x, final int y, final int z) {
      return this.tiles.get(BlockVector3.at(x, y, z));
   }

   @Override
   public int getSkyLight(int x, int y, int z) {
      return 0;
   }

   @Override
   public int getEmittedLight(int x, int y, int z) {
      return 0;
   }

   @Override
   public int[] getHeightMap(HeightMapType type) {
      return new int[0];
   }

   @Override
   public <T extends Future<T>> T call(IQueueExtent<? extends IChunk> owner, IChunkSet set, Runnable finalize) {
      return null;
   }

   public char get(int x, int y, int z) {
      int layer = (y >> 4) - this.getMinSectionPosition();
      int index = (y & 15) << 8 | z << 4 | x;
      return this.blocks[layer][index];
   }

   @Override
   public boolean trim(boolean aggressive) {
      return false;
   }
}
