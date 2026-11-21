package com.fastasyncworldedit.core.extent.filter.block;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.FilterBlockMask;
import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.implementation.Flood;
import com.fastasyncworldedit.core.queue.implementation.blocks.CharGetBlocks;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public class CharFilterBlock extends ChunkFilterBlock {
   private static final CharFilterBlock.SetDelegate FULL = (block, value) -> block.setArr[block.index] = value;
   private static final CharFilterBlock.SetDelegate NULL = (block, value) -> block.initSet().set(block, value);
   private int maxLayer;
   private int minLayer;
   protected CharGetBlocks get;
   protected IChunkSet set;
   protected char[] getArr;
   @Nullable
   protected char[] setArr;
   protected CharFilterBlock.SetDelegate delegate;
   protected int layer;
   private int index;
   private int x;
   private int y;
   private int z;
   private int xx;
   private int yy;
   private int zz;
   private int chunkX;
   private int chunkZ;

   public CharFilterBlock(Extent extent) {
      super(extent);
   }

   @Override
   public final synchronized ChunkFilterBlock initChunk(int chunkX, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
      this.xx = chunkX << 4;
      this.zz = chunkZ << 4;
      return this;
   }

   @Override
   public final synchronized ChunkFilterBlock initLayer(IBlocks iget, IChunkSet iset, int layer) {
      this.get = (CharGetBlocks)iget;
      this.minLayer = this.get.getMinSectionPosition();
      this.maxLayer = this.get.getMaxSectionPosition();
      this.layer = layer;
      if (!iget.hasSection(layer)) {
         this.getArr = FaweCache.INSTANCE.EMPTY_CHAR_4096;
      } else {
         this.getArr = iget.load(layer);
      }

      this.set = iset;
      if (this.set.hasSection(layer)) {
         this.setArr = this.set.load(layer);
         this.delegate = FULL;
      } else {
         this.delegate = NULL;
         this.setArr = null;
      }

      this.yy = layer << 4;
      return this;
   }

   @Override
   public synchronized void flood(IChunkGet iget, IChunkSet iset, int layer, Flood flood, FilterBlockMask mask) {
      int maxDepth = flood.getMaxDepth();
      boolean checkDepth = maxDepth < 65535;
      if (this.initLayer(iget, iset, layer) != null) {
         while ((this.index = flood.poll()) != -1) {
            this.x = this.index & 15;
            this.z = this.index >> 4 & 15;
            this.y = this.index >> 8 & 15;
            if (mask.applyBlock(this)) {
               int depth = this.index >> 12;
               if (!checkDepth || depth <= maxDepth) {
                  flood.apply(this.x, this.y, this.z, depth);
               }
            }
         }
      }
   }

   @Override
   public synchronized void filter(Filter filter, int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.index = x | z << 4 | y << 8;
      filter.applyBlock(this);
   }

   @Override
   public synchronized void filter(Filter filter, int startY, int endY) {
      this.y = startY;

      for (this.index = startY << 8; this.y <= endY; this.y++) {
         for (this.z = 0; this.z < 16; this.z++) {
            for (this.x = 0; this.x < 16; this.index++) {
               filter.applyBlock(this);
               this.x++;
            }
         }
      }
   }

   @Override
   public synchronized void filter(Filter filter, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      int yis = minY << 8;
      int zis = minZ << 4;
      int zie = 15 - maxZ << 4;
      int xie = 15 - maxX;
      this.y = minY;

      for (this.index = yis; this.y <= maxY; this.y++) {
         this.index += zis;

         for (this.z = minZ; this.z <= maxZ; this.z++) {
            this.index += minX;

            for (this.x = minX; this.x <= maxX; this.index++) {
               filter.applyBlock(this);
               this.x++;
            }

            this.index += xie;
         }

         this.index += zie;
      }
   }

   @Override
   public final synchronized void filter(Filter filter, Region region) {
      this.y = 0;

      for (this.index = 0; this.y < 16; this.y++) {
         int absY = this.yy + this.y;

         for (this.z = 0; this.z < 16; this.z++) {
            int absZ = this.zz + this.z;

            for (this.x = 0; this.x < 16; this.index++) {
               int absX = this.xx + this.x;
               if (region.contains(absX, absY, absZ)) {
                  filter.applyBlock(this);
               }

               this.x++;
            }
         }
      }
   }

   @Override
   public synchronized void filter(Filter filter) {
      this.y = 0;

      for (this.index = 0; this.y < 16; this.y++) {
         for (this.z = 0; this.z < 16; this.z++) {
            for (this.x = 0; this.x < 16; this.index++) {
               filter.applyBlock(this);
               this.x++;
            }
         }
      }
   }

   @Override
   public void setBiome(BiomeType biome) {
      this.set.setBiome(this.x, this.y, this.z, biome);
   }

   @Override
   public BiomeType getBiome() {
      return this.get.getBiomeType(this.x, this.y, this.z);
   }

   @Override
   public final int x() {
      return this.xx + this.x;
   }

   @Override
   public final int y() {
      return this.yy + this.y;
   }

   @Override
   public final int z() {
      return this.zz + this.z;
   }

   @Override
   public final int getLocalX() {
      return this.x;
   }

   @Override
   public final int getLocalY() {
      return this.y;
   }

   @Override
   public final int getLocalZ() {
      return this.z;
   }

   @Override
   public final int getChunkX() {
      return this.chunkX;
   }

   @Override
   public final int getChunkZ() {
      return this.chunkZ;
   }

   public final char getOrdinalChar() {
      return this.getArr[this.index];
   }

   @Override
   public final int getOrdinal() {
      return this.getArr[this.index];
   }

   @Override
   public void setOrdinal(int ordinal) {
      this.delegate.set(this, (char)ordinal);
   }

   @Override
   public final BlockState getBlock() {
      int ordinal = this.getArr[this.index];
      return BlockTypesCache.states[ordinal];
   }

   @Override
   public void setBlock(BlockState state) {
      this.delegate.set(this, state.getOrdinalChar());
   }

   @Override
   public final BaseBlock getFullBlock() {
      BlockState state = this.getBlock();
      BlockMaterial material = state.getMaterial();
      if (material.hasContainer()) {
         FaweCompoundTag tag = this.get.tile(this.x, this.y + this.yy, this.z);
         return state.toBaseBlock(tag == null ? null : tag.linTag());
      } else {
         return state.toBaseBlock();
      }
   }

   @Override
   public void setFullBlock(BaseBlock block) {
      this.delegate.set(this, block.getOrdinalChar());
      LazyReference<LinCompoundTag> nbt = block.getNbtReference();
      if (nbt != null) {
         this.set.tile(this.x, this.yy + this.y, this.z, FaweCompoundTag.of(nbt));
      }
   }

   @Nullable
   @Override
   public LinCompoundTag getNbt() {
      FaweCompoundTag tile = this.get.tile(this.x, this.y + this.yy, this.z);
      return tile == null ? null : tile.linTag();
   }

   @Override
   public void setNbt(@Nullable final LinCompoundTag nbtData) {
      if (nbtData != null) {
         this.set.tile(this.x, this.y + this.yy, this.z, FaweCompoundTag.of(nbtData));
      }
   }

   @Override
   public void setNbtReference(@Nullable final LazyReference<LinCompoundTag> nbtData) {
      if (nbtData != null) {
         this.set.tile(this.x, this.y + this.yy, this.z, FaweCompoundTag.of(nbtData));
      }
   }

   @Override
   public boolean hasNbtData() {
      BlockState state = this.getBlock();
      BlockMaterial material = state.getMaterial();
      return material.hasContainer();
   }

   @Override
   public final BlockState getBlockNorth() {
      return this.z > 0 ? BlockTypesCache.states[this.getArr[this.index - 16]] : this.getExtent().getBlock(this.x(), this.y(), this.z() - 1);
   }

   @Override
   public final BlockState getBlockEast() {
      return this.x < 15 ? BlockTypesCache.states[this.getArr[this.index + 1]] : this.getExtent().getBlock(this.x() + 1, this.y(), this.z());
   }

   @Override
   public final BlockState getBlockSouth() {
      return this.z < 15 ? BlockTypesCache.states[this.getArr[this.index + 16]] : this.getExtent().getBlock(this.x(), this.y(), this.z() + 1);
   }

   @Override
   public final BlockState getBlockWest() {
      return this.x > 0 ? BlockTypesCache.states[this.getArr[this.index - 1]] : this.getExtent().getBlock(this.x() - 1, this.y(), this.z());
   }

   @Override
   public final BlockState getBlockBelow() {
      if (this.y > 0) {
         return BlockTypesCache.states[this.getArr[this.index - 256]];
      } else if (this.layer > this.minLayer) {
         int newLayer = this.layer - 1;
         return BlockTypesCache.states[this.get.get(newLayer, this.index + 3840)];
      } else {
         return BlockTypes.__RESERVED__.getDefaultState();
      }
   }

   @Override
   public final BlockState getBlockAbove() {
      if (this.y < 16) {
         return BlockTypesCache.states[this.getArr[this.index + 256]];
      } else if (this.layer < this.maxLayer) {
         int newLayer = this.layer + 1;
         return BlockTypesCache.states[this.get.get(newLayer, this.index - 3840)];
      } else {
         return BlockTypes.__RESERVED__.getDefaultState();
      }
   }

   @Override
   public final BlockState getBlockRelativeY(int y) {
      int newY = this.y + y;
      int layerAdd = newY >> 4;
      if (layerAdd == 0) {
         return BlockTypesCache.states[this.getArr[this.index + (y << 8)]];
      } else if ((layerAdd <= 0 || layerAdd >= this.maxLayer - this.layer) && (layerAdd >= 0 || layerAdd >= this.minLayer - this.layer)) {
         return BlockTypes.__RESERVED__.getDefaultState();
      } else {
         int newLayer = this.layer + layerAdd;
         int index = this.index + ((y & 15) << 8) & 4095;
         return BlockTypesCache.states[this.get.get(newLayer, index)];
      }
   }

   @Override
   public char getOrdinalChar(Extent orDefault) {
      return this.getOrdinalChar();
   }

   protected final CharFilterBlock.SetDelegate initSet() {
      this.setArr = this.set.load(this.layer);
      return this.delegate = FULL;
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return x >> 4 == this.chunkX && z >> 4 == this.chunkZ ? this.get.getBiomeType(x & 15, y, z & 15) : this.getExtent().getBiomeType(x, y, z);
   }

   @Override
   public BiomeType getBiome(final BlockVector3 position) {
      return this.getBiomeType(position.x(), position.y(), position.z());
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.getExtent().setBlock(x, y, z, block);
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.setBiome(position.x(), position.y(), position.z(), biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return x >> 4 == this.chunkX && z >> 4 == this.chunkZ ? this.set.setBiome(x & 15, y, z & 15, biome) : this.getExtent().setBiome(x, y, z, biome);
   }

   @Internal
   protected interface SetDelegate {
      void set(@Nonnull CharFilterBlock block, char value);
   }
}
