package com.fastasyncworldedit.core.queue.implementation.blocks;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.math.BlockVector3ChunkMap;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.Pool;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class CharSetBlocks extends CharBlocks implements IChunkSet {
   private static final Pool<CharSetBlocks> POOL = FaweCache.INSTANCE.registerPool(CharSetBlocks.class, CharSetBlocks::new, Settings.settings().QUEUE.POOL);
   public BiomeType[][] biomes;
   public char[][] light;
   public char[][] skyLight;
   public BlockVector3ChunkMap<FaweCompoundTag> tiles;
   public HashSet<FaweCompoundTag> entities;
   public HashSet<UUID> entityRemoves;
   public EnumMap<HeightMapType, int[]> heightMaps;
   private boolean fastMode = false;
   private int bitMask = -1;
   private SideEffectSet sideEffectSet = SideEffectSet.defaults();

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public static CharSetBlocks newInstance() {
      return POOL.poll();
   }

   public static CharSetBlocks newInstance(int x, int z) {
      CharSetBlocks set = POOL.poll();
      set.init(x, z);
      return set;
   }

   private CharSetBlocks() {
      super(0, 15);
   }

   @Override
   public void recycle() {
      this.reset();
      POOL.offer(this);
   }

   @Override
   public BiomeType[][] getBiomes() {
      return this.biomes;
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      if (this.biomes == null || y >> 4 < this.minSectionPosition || y >> 4 > this.maxSectionPosition) {
         return null;
      } else {
         int layer;
         return this.biomes[layer = (y >> 4) - this.minSectionPosition] == null ? null : this.biomes[layer][(y & 15) >> 2 | z >> 2 << 2 | x >> 2];
      }
   }

   @Override
   public Map<BlockVector3, FaweCompoundTag> tiles() {
      return (Map<BlockVector3, FaweCompoundTag>)(this.tiles == null ? Collections.emptyMap() : this.tiles);
   }

   @Nullable
   @Override
   public FaweCompoundTag tile(final int x, final int y, final int z) {
      return this.tiles == null ? null : this.tiles.get(x, y, z);
   }

   @Override
   public Collection<FaweCompoundTag> entities() {
      return (Collection<FaweCompoundTag>)(this.entities == null ? Collections.emptySet() : this.entities);
   }

   @Override
   public Set<UUID> getEntityRemoves() {
      return (Set<UUID>)(this.entityRemoves == null ? Collections.emptySet() : this.entityRemoves);
   }

   @Override
   public Map<HeightMapType, int[]> getHeightMaps() {
      return this.heightMaps == null ? new EnumMap<>(HeightMapType.class) : this.heightMaps;
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      this.updateSectionIndexRange(y >> 4);
      int layer = (y >> 4) - this.minSectionPosition;
      if (this.biomes == null) {
         this.biomes = new BiomeType[this.sectionCount][];
         this.biomes[layer] = new BiomeType[64];
      } else if (this.biomes[layer] == null) {
         this.biomes[layer] = new BiomeType[64];
      }

      this.biomes[layer][(y & 12) << 2 | z & 12 | (x & 12) >> 2] = biome;
      return true;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T holder) {
      this.updateSectionIndexRange(y >> 4);
      this.set(x, y, z, holder.getOrdinalChar());
      holder.applyTileEntity(this, x, y, z);
      return true;
   }

   @Override
   public void setBlocks(int layer, char[] data) {
      this.updateSectionIndexRange(layer);
      layer -= this.minSectionPosition;
      this.blocks[layer] = data;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
      return this.setBlock(position.x(), position.y(), position.z(), block);
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tag) {
      if (this.tiles == null) {
         this.tiles = new BlockVector3ChunkMap<>();
      }

      this.updateSectionIndexRange(y >> 4);
      this.tiles.put(x, y, z, tag);
      return true;
   }

   @Override
   public void setBlockLight(int x, int y, int z, int value) {
      this.updateSectionIndexRange(y >> 4);
      if (this.light == null) {
         this.light = new char[this.sectionCount][];
      }

      int layer = (y >> 4) - this.minSectionPosition;
      if (this.light[layer] == null) {
         char[] c = new char[4096];
         Arrays.fill(c, '\u0010');
         this.light[layer] = c;
      }

      int index = (y & 15) << 8 | (z & 15) << 4 | x & 15;
      this.light[layer][index] = (char)value;
   }

   @Override
   public void setSkyLight(int x, int y, int z, int value) {
      this.updateSectionIndexRange(y >> 4);
      if (this.skyLight == null) {
         this.skyLight = new char[this.sectionCount][];
      }

      int layer = (y >> 4) - this.minSectionPosition;
      if (this.skyLight[layer] == null) {
         char[] c = new char[4096];
         Arrays.fill(c, '\u0010');
         this.skyLight[layer] = c;
      }

      int index = (y & 15) << 8 | (z & 15) << 4 | x & 15;
      this.skyLight[layer][index] = (char)value;
   }

   @Override
   public void setHeightMap(HeightMapType type, int[] heightMap) {
      if (this.heightMaps == null) {
         this.heightMaps = new EnumMap<>(HeightMapType.class);
      }

      this.heightMaps.put(type, heightMap);
   }

   @Override
   public void setLightLayer(int layer, char[] toSet) {
      this.updateSectionIndexRange(layer);
      if (this.light == null) {
         this.light = new char[this.sectionCount][];
      }

      layer -= this.minSectionPosition;
      this.light[layer] = toSet;
   }

   @Override
   public void setSkyLightLayer(int layer, char[] toSet) {
      this.updateSectionIndexRange(layer);
      if (this.skyLight == null) {
         this.skyLight = new char[this.sectionCount][];
      }

      layer -= this.minSectionPosition;
      this.skyLight[layer] = toSet;
   }

   @Override
   public char[][] getLight() {
      return this.light;
   }

   @Override
   public char[][] getSkyLight() {
      return this.skyLight;
   }

   @Override
   public void removeSectionLighting(int layer, boolean sky) {
      this.updateSectionIndexRange(layer);
      layer -= this.minSectionPosition;
      if (this.light == null) {
         this.light = new char[this.sectionCount][];
      }

      if (this.light[layer] == null) {
         this.light[layer] = new char[4096];
      }

      Arrays.fill(this.light[layer], '\u0000');
      if (sky) {
         if (this.skyLight == null) {
            this.skyLight = new char[this.sectionCount][];
         }

         if (this.skyLight[layer] == null) {
            this.skyLight[layer] = new char[4096];
         }

         Arrays.fill(this.skyLight[layer], '\u0000');
      }
   }

   @Override
   public void setFullBright(int layer) {
      this.updateSectionIndexRange(layer);
      layer -= this.minSectionPosition;
      if (this.light == null) {
         this.light = new char[this.sectionCount][];
      }

      if (this.light[layer] == null) {
         this.light[layer] = new char[4096];
      }

      if (this.skyLight == null) {
         this.skyLight = new char[this.sectionCount][];
      }

      if (this.skyLight[layer] == null) {
         this.skyLight[layer] = new char[4096];
      }

      Arrays.fill(this.light[layer], '\u000f');
      Arrays.fill(this.skyLight[layer], '\u000f');
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.setBiome(position.x(), position.y(), position.z(), biome);
   }

   @Override
   public void entity(final FaweCompoundTag tag) {
      if (this.entities == null) {
         this.entities = new HashSet<>();
      }

      this.entities.add(tag);
   }

   @Override
   public void removeEntity(UUID uuid) {
      if (this.entityRemoves == null) {
         this.entityRemoves = new HashSet<>();
      }

      this.entityRemoves.add(uuid);
   }

   @Override
   public void setFastMode(boolean fastMode) {
      this.fastMode = fastMode;
   }

   @Override
   public boolean isFastMode() {
      return this.fastMode;
   }

   @Override
   public void setBitMask(int bitMask) {
      this.bitMask = bitMask;
   }

   @Override
   public int getBitMask() {
      return this.bitMask;
   }

   @Override
   public boolean isEmpty() {
      if (this.biomes == null
         && this.light == null
         && this.skyLight == null
         && (this.entities == null || this.entities.isEmpty())
         && (this.tiles == null || this.tiles.isEmpty())
         && (this.entityRemoves == null || this.entityRemoves.isEmpty())
         && (this.heightMaps == null || this.heightMaps.isEmpty())) {
         for (int i = this.minSectionPosition; i <= this.maxSectionPosition; i++) {
            if (this.hasSection(i)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public IChunkSet reset() {
      this.biomes = null;
      this.tiles = null;
      this.entities = null;
      this.entityRemoves = null;
      this.light = null;
      this.skyLight = null;
      this.heightMaps = null;
      super.reset();
      return null;
   }

   @Override
   public boolean hasBiomes(int layer) {
      layer -= this.minSectionPosition;
      return layer >= 0 && layer < this.blocks.length ? this.biomes != null && this.biomes[layer] != null : false;
   }

   public ThreadUnsafeCharBlocks createCopy() {
      char[][] blocksCopy = new char[this.sectionCount][];

      for (int i = 0; i < this.sectionCount; i++) {
         if (this.blocks[i] != null) {
            blocksCopy[i] = new char[4096];
            System.arraycopy(this.blocks[i], 0, blocksCopy[i], 0, 4096);
         }
      }

      BiomeType[][] biomesCopy;
      if (this.biomes == null) {
         biomesCopy = null;
      } else {
         biomesCopy = new BiomeType[this.sectionCount][];

         for (int ix = 0; ix < this.sectionCount; ix++) {
            if (this.biomes[ix] != null) {
               biomesCopy[ix] = new BiomeType[this.biomes[ix].length];
               System.arraycopy(this.biomes[ix], 0, biomesCopy[ix], 0, this.biomes[ix].length);
            }
         }
      }

      char[][] lightCopy = createLightCopy(this.light, this.sectionCount);
      char[][] skyLightCopy = createLightCopy(this.skyLight, this.sectionCount);
      return new ThreadUnsafeCharBlocks(
         blocksCopy,
         this.minSectionPosition,
         this.maxSectionPosition,
         biomesCopy,
         this.sectionCount,
         lightCopy,
         skyLightCopy,
         this.tiles != null ? new BlockVector3ChunkMap<>(this.tiles) : null,
         this.entities != null ? new HashSet<>(this.entities) : null,
         this.entityRemoves != null ? new HashSet<>(this.entityRemoves) : null,
         this.heightMaps != null ? new EnumMap<>(this.heightMaps) : null,
         this.defaultOrdinal(),
         this.fastMode,
         this.bitMask,
         this.sideEffectSet,
         this.getX(),
         this.getZ()
      );
   }

   @Override
   public void setSideEffectSet(SideEffectSet sideEffectSet) {
      this.sideEffectSet = sideEffectSet;
   }

   @Override
   public SideEffectSet getSideEffectSet() {
      return this.sideEffectSet;
   }

   static char[][] createLightCopy(char[][] lightArr, int sectionCount) {
      if (lightArr == null) {
         return null;
      } else {
         char[][] lightCopy = new char[sectionCount][];

         for (int i = 0; i < sectionCount; i++) {
            if (lightArr[i] != null) {
               lightCopy[i] = new char[lightArr[i].length];
               System.arraycopy(lightArr[i], 0, lightCopy[i], 0, lightArr[i].length);
            }
         }

         return lightCopy;
      }
   }

   @Override
   public char[] load(final int layer) {
      this.updateSectionIndexRange(layer);
      return super.load(layer);
   }

   @Override
   protected char defaultOrdinal() {
      return '\u0000';
   }

   private void updateSectionIndexRange(int layer) {
      if (layer < this.minSectionPosition || layer > this.maxSectionPosition) {
         if (layer < this.minSectionPosition) {
            int diff = this.minSectionPosition - layer;
            this.sectionCount += diff;
            this.minSectionPosition = layer;
            this.resizeSectionsArrays(diff, false);
         } else {
            int diff = layer - this.maxSectionPosition;
            this.sectionCount += diff;
            this.maxSectionPosition = layer;
            this.resizeSectionsArrays(diff, true);
         }
      }
   }

   private void resizeSectionsArrays(int diff, boolean appendNew) {
      char[][] tmpBlocks = new char[this.sectionCount][];
      Object[] tmpSectionLocks = new Object[this.sectionCount];
      int destPos = appendNew ? 0 : diff;
      System.arraycopy(this.blocks, 0, tmpBlocks, destPos, this.blocks.length);
      System.arraycopy(this.sectionLocks, 0, tmpSectionLocks, destPos, this.blocks.length);
      int toFillFrom = appendNew ? this.sectionCount - diff : 0;
      int toFillTo = appendNew ? this.sectionCount : diff;

      for (int i = toFillFrom; i < toFillTo; i++) {
         tmpSectionLocks[i] = new Object();
      }

      this.blocks = tmpBlocks;
      this.sectionLocks = tmpSectionLocks;
      if (this.biomes != null) {
         BiomeType[][] tmpBiomes = new BiomeType[this.sectionCount][64];
         System.arraycopy(this.biomes, 0, tmpBiomes, destPos, this.biomes.length);
         this.biomes = tmpBiomes;
      }

      if (this.light != null) {
         char[][] tmplight = new char[this.sectionCount][];
         System.arraycopy(this.light, 0, tmplight, destPos, this.light.length);
         this.light = tmplight;
      }

      if (this.skyLight != null) {
         char[][] tmplight = new char[this.sectionCount][];
         System.arraycopy(this.skyLight, 0, tmplight, destPos, this.skyLight.length);
         this.skyLight = tmplight;
      }
   }
}
