package com.fastasyncworldedit.core.queue.implementation.blocks;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.math.BlockVector3ChunkMap;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ThreadUnsafeCharBlocks implements IChunkSet, IBlocks {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final char defaultOrdinal;
   private final int chunkX;
   private final int chunkZ;
   private char[][] blocks;
   private int minSectionPosition;
   private int maxSectionPosition;
   private int sectionCount;
   private BiomeType[][] biomes;
   private char[][] light;
   private char[][] skyLight;
   private BlockVector3ChunkMap<FaweCompoundTag> tiles;
   private HashSet<FaweCompoundTag> entities;
   private HashSet<UUID> entityRemoves;
   private Map<HeightMapType, int[]> heightMaps;
   private boolean fastMode;
   private int bitMask;
   private SideEffectSet sideEffectSet;

   ThreadUnsafeCharBlocks(
      char[][] blocks,
      int minSectionPosition,
      int maxSectionPosition,
      BiomeType[][] biomes,
      int sectionCount,
      char[][] light,
      char[][] skyLight,
      BlockVector3ChunkMap<FaweCompoundTag> tiles,
      HashSet<FaweCompoundTag> entities,
      HashSet<UUID> entityRemoves,
      Map<HeightMapType, int[]> heightMaps,
      char defaultOrdinal,
      boolean fastMode,
      int bitMask,
      SideEffectSet sideEffectSet,
      int chunkX,
      int chunkZ
   ) {
      this.blocks = blocks;
      this.minSectionPosition = minSectionPosition;
      this.maxSectionPosition = maxSectionPosition;
      this.biomes = biomes;
      this.sectionCount = sectionCount;
      this.light = light;
      this.skyLight = skyLight;
      this.tiles = tiles;
      this.entities = entities;
      this.entityRemoves = entityRemoves;
      this.heightMaps = heightMaps;
      this.defaultOrdinal = defaultOrdinal;
      this.fastMode = fastMode;
      this.bitMask = bitMask;
      this.sideEffectSet = sideEffectSet;
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   @Override
   public boolean hasSection(int layer) {
      layer -= this.minSectionPosition;
      return layer >= 0 && layer < this.blocks.length && this.blocks[layer] != null && this.blocks[layer].length == 4096;
   }

   @Override
   public char[] load(int layer) {
      this.updateSectionIndexRange(layer);
      layer -= this.minSectionPosition;
      char[] arr = this.blocks[layer];
      if (arr == null) {
         arr = this.blocks[layer] = new char[4096];
      }

      return arr;
   }

   @Nullable
   @Override
   public char[] loadIfPresent(int layer) {
      if (layer >= this.minSectionPosition && layer <= this.maxSectionPosition) {
         layer -= this.minSectionPosition;
         return this.blocks[layer];
      } else {
         return null;
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
      return (Collection<FaweCompoundTag>)(this.entities == null ? Collections.emptyList() : this.entities);
   }

   @Override
   public Map<HeightMapType, int[]> getHeightMaps() {
      return (Map<HeightMapType, int[]>)(this.heightMaps == null ? new HashMap<>() : this.heightMaps);
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
   public boolean trim(boolean aggressive, int layer) {
      return false;
   }

   @Override
   public int getSectionCount() {
      return this.sectionCount;
   }

   @Override
   public int getMaxSectionPosition() {
      return this.maxSectionPosition;
   }

   @Override
   public int getMinSectionPosition() {
      return this.minSectionPosition;
   }

   @Override
   public int getX() {
      return this.chunkX;
   }

   @Override
   public int getZ() {
      return this.chunkZ;
   }

   public char get(int x, int y, int z) {
      int layer = y >> 4;
      if (!this.hasSection(layer)) {
         return this.defaultOrdinal;
      } else {
         int index = (y & 15) << 8 | z << 4 | x;
         return this.blocks[layer - this.minSectionPosition][index];
      }
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
   public BlockState getBlock(int x, int y, int z) {
      return BlockTypesCache.states[this.get(x, y, z)];
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
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.setBiome(position.x(), position.y(), position.z(), biome);
   }

   public void set(int x, int y, int z, char value) {
      int layer = (y >> 4) - this.minSectionPosition;
      int index = (y & 15) << 8 | z << 4 | x;

      try {
         this.blocks[layer][index] = value;
      } catch (ArrayIndexOutOfBoundsException var8) {
         LOGGER.error("Tried setting block at coordinates (" + x + "," + y + "," + z + ")");

         assert Fawe.platform() != null;

         LOGGER.error("Layer variable was = {}", layer, var8);
      }
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
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tag) {
      this.updateSectionIndexRange(y >> 4);
      if (this.tiles == null) {
         this.tiles = new BlockVector3ChunkMap<>();
      }

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
   public Set<UUID> getEntityRemoves() {
      return (Set<UUID>)(this.entityRemoves == null ? Collections.emptySet() : this.entityRemoves);
   }

   @Override
   public BiomeType[][] getBiomes() {
      return this.biomes;
   }

   @Override
   public boolean hasBiomes() {
      return IChunkSet.super.hasBiomes();
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
   public boolean hasLight() {
      return IChunkSet.super.hasLight();
   }

   @Override
   public IChunkSet reset() {
      this.blocks = new char[this.sectionCount][];
      this.biomes = new BiomeType[this.sectionCount][];
      this.light = new char[this.sectionCount][];
      this.skyLight = new char[this.sectionCount][];
      this.tiles = null;
      this.entities = null;
      this.entityRemoves = null;
      this.heightMaps = null;
      return this;
   }

   @Override
   public boolean hasBiomes(int layer) {
      layer -= this.minSectionPosition;
      return layer >= 0 && layer < this.biomes.length && this.biomes[layer] != null && this.biomes[layer].length > 0;
   }

   @Override
   public IChunkSet createCopy() {
      char[][] blocksCopy = new char[this.sectionCount][];

      for (int i = 0; i < this.sectionCount; i++) {
         blocksCopy[i] = new char[4096];
         if (this.blocks[i] != null) {
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

      char[][] lightCopy = CharSetBlocks.createLightCopy(this.light, this.sectionCount);
      char[][] skyLightCopy = CharSetBlocks.createLightCopy(this.skyLight, this.sectionCount);
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
         this.heightMaps != null ? new HashMap<>(this.heightMaps) : null,
         this.defaultOrdinal,
         this.fastMode,
         this.bitMask,
         this.sideEffectSet,
         this.chunkX,
         this.chunkZ
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

   @Override
   public boolean trim(boolean aggressive) {
      return false;
   }

   private void updateSectionIndexRange(int layer) {
      if (layer < this.minSectionPosition || layer > this.maxSectionPosition) {
         if (layer < this.minSectionPosition) {
            int diff = this.minSectionPosition - layer;
            this.sectionCount += diff;
            this.minSectionPosition = layer;
            this.resizeSectionsArrays(layer, diff, false);
         } else {
            int diff = layer - this.maxSectionPosition;
            this.sectionCount += diff;
            this.maxSectionPosition = layer;
            this.resizeSectionsArrays(layer, diff, true);
         }
      }
   }

   private void resizeSectionsArrays(int layer, int diff, boolean appendNew) {
      char[][] tmpBlocks = new char[this.sectionCount][];
      int destPos = appendNew ? 0 : diff;
      System.arraycopy(this.blocks, 0, tmpBlocks, destPos, this.blocks.length);
      this.blocks = tmpBlocks;
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
