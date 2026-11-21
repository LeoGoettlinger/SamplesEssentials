package com.fastasyncworldedit.core.extent.clipboard;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.jnbt.streamer.IntValueReader;
import com.fastasyncworldedit.core.math.IntTriple;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.util.MainUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MemoryOptimizedClipboard extends LinearClipboard {
   private static final int BLOCK_SIZE = 2097152;
   private static final int BLOCK_MASK = 1048575;
   private static final int BLOCK_SHIFT = 20;
   private final byte[][] states;
   private final byte[] buffer = new byte[MainUtil.getMaxCompressedLength(2097152)];
   private byte[] biomes = null;
   private final HashMap<IntTriple, CompoundTag> nbtMap;
   private int lastOrdinalsI = -1;
   private byte[] lastOrdinals;
   private boolean saveOrdinals = false;
   private final int compressionLevel;
   private int lastI;
   private int lastIMin;
   private int lastIMax;

   public MemoryOptimizedClipboard(Region region) {
      this(region, Settings.settings().CLIPBOARD.COMPRESSION_LEVEL);
   }

   public MemoryOptimizedClipboard(Region region, int compressionLevel) {
      super(region.getDimensions(), region.getMinimumPoint());
      this.states = new byte[1 + (this.getVolume() >> 20)][];
      this.nbtMap = new HashMap<>();
      this.compressionLevel = compressionLevel;
   }

   @Override
   public boolean hasBiomes() {
      return this.biomes != null;
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.setBiome(position.x(), position.y(), position.z(), biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      this.setBiome(this.getBiomeIndex(x, y, z), biome);
      return true;
   }

   @Override
   public void setBiome(int index, BiomeType biome) {
      if (this.biomes == null) {
         this.biomes = new byte[((this.getHeight() >> 2) + 1) * ((this.getLength() >> 2) + 1) * ((this.getWidth() >> 2) + 1)];
      }

      this.biomes[index] = (byte)biome.getInternalId();
   }

   @Override
   public void streamBiomes(IntValueReader task) {
      if (this.hasBiomes()) {
         try {
            for (int y = 0; y < this.getHeight(); y++) {
               for (int z = 0; z < this.getLength(); z++) {
                  for (int x = 0; x < this.getWidth(); x++) {
                     task.applyInt(this.getIndex(x, y, z), this.biomes[this.getBiomeIndex(x, y, z)] & 255);
                  }
               }
            }
         } catch (IOException var5) {
            var5.printStackTrace();
            throw new RuntimeException(var5);
         }
      }
   }

   @Override
   public BiomeType getBiome(int index) {
      return !this.hasBiomes() ? null : BiomeTypes.get(this.biomes[index]);
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return this.getBiome(this.getBiomeIndex(x, y, z));
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.getBiome(this.getBiomeIndex(position.x(), position.y(), position.z()));
   }

   private int getOrdinal(int index) {
      int i = index >> 20;
      int li = (index & 1048575) << 1;
      if (i != this.lastOrdinalsI) {
         this.saveOrdinals();
         byte[] compressed = this.states[this.lastOrdinalsI = i];
         if (compressed == null) {
            this.lastOrdinals = null;
            return 0;
         }

         this.lastOrdinals = MainUtil.decompress(compressed, this.lastOrdinals, 2097152, this.compressionLevel);
      }

      return this.lastOrdinals == null ? 0 : ((this.lastOrdinals[li] & 0xFF) << 8) + (this.lastOrdinals[li + 1] & 0xFF);
   }

   private void saveOrdinals() {
      if (this.saveOrdinals && this.lastOrdinals != null) {
         this.states[this.lastOrdinalsI] = MainUtil.compress(this.lastOrdinals, this.buffer, this.compressionLevel);
      }

      this.saveOrdinals = false;
   }

   private int getLocalIndex(int index) {
      if (index < this.lastIMin || index > this.lastIMax) {
         this.lastI = index >> 20;
         this.lastIMin = this.lastI << 20;
         this.lastIMax = this.lastIMin + 1048575;
      }

      return this.lastI;
   }

   private void setOrdinal(int index, int v) {
      int i = this.getLocalIndex(index);
      if (i != this.lastOrdinalsI) {
         this.saveOrdinals();
         byte[] compressed = this.states[this.lastOrdinalsI = i];
         if (compressed != null) {
            this.lastOrdinals = MainUtil.decompress(compressed, this.lastOrdinals, 2097152, this.compressionLevel);
         } else {
            this.lastOrdinals = null;
         }
      }

      if (this.lastOrdinals == null) {
         BlockType bt = BlockTypes.getFromStateOrdinal(v);
         if (bt.getMaterial().isAir()) {
            return;
         }

         this.lastOrdinals = new byte[2097152];
      }

      int li = (index & 1048575) << 1;
      this.lastOrdinals[li] = (byte)(v >>> 8 & 0xFF);
      this.lastOrdinals[li + 1] = (byte)(v & 0xFF);
      this.saveOrdinals = true;
   }

   @Override
   public Collection<CompoundTag> getTileEntities() {
      return this.nbtMap.values();
   }

   public int getIndex(int x, int y, int z) {
      return x + y * this.getArea() + z * this.getWidth();
   }

   public int getBiomeIndex(int x, int y, int z) {
      return (x >> 2) + (y >> 2) * (this.getWidth() >> 2) * (this.getLength() >> 2) + (z >> 2) * (this.getWidth() >> 2);
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      int index = this.getIndex(x, y, z);
      return this.getFullBlock(index);
   }

   private BaseBlock toBaseBlock(BlockState state, int i) {
      if (state.getMaterial().hasContainer() && !this.nbtMap.isEmpty()) {
         CompoundTag nbt;
         if (this.nbtMap.size() < 4) {
            nbt = null;

            for (Entry<IntTriple, CompoundTag> entry : this.nbtMap.entrySet()) {
               IntTriple trio = entry.getKey();
               int index = this.getIndex(trio.x(), trio.y(), trio.z());
               if (index == i) {
                  nbt = entry.getValue();
                  break;
               }
            }
         } else {
            int y = i / this.getArea();
            int newI = i - y * this.getArea();
            int z = newI / this.getWidth();
            int x = newI - z * this.getWidth();
            nbt = this.nbtMap.get(new IntTriple(x, y, z));
         }

         return state.toBaseBlock(nbt);
      } else {
         return state.toBaseBlock();
      }
   }

   @Override
   public BaseBlock getFullBlock(int index) {
      return this.toBaseBlock(this.getBlock(index), index);
   }

   @Override
   public BlockState getBlock(int index) {
      int combinedID = this.getOrdinal(index);
      return BlockState.getFromOrdinal(combinedID);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.getBlock(this.getIndex(x, y, z));
   }

   public int size() {
      this.saveOrdinals();
      int total = 0;

      for (byte[] array : this.states) {
         if (array != null) {
            total += array.length;
         }
      }

      return total;
   }

   @Override
   public boolean setTile(int x, int y, int z, CompoundTag tag) {
      Map<String, Tag<?, ?>> values = new HashMap<>(tag.getValue());
      values.put("x", new IntTag(x));
      values.put("y", new IntTag(y));
      values.put("z", new IntTag(z));
      this.nbtMap.put(new IntTriple(x, y, z), new CompoundTag(values));
      return true;
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tile) throws WorldEditException {
      return this.setTile(x, y, z, new CompoundTag(tile.linTag()));
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) {
      return this.setBlock(this.getIndex(x, y, z), block);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int index, B block) {
      int ordinal = block.getOrdinal();
      if (ordinal == 0) {
         ordinal = 1;
      }

      this.setOrdinal(index, ordinal);
      boolean hasNbt = block instanceof BaseBlock && block.hasNbtData();
      if (hasNbt) {
         int y = index / this.getArea();
         int newI = index - y * this.getArea();
         int z = newI / this.getWidth();
         int x = newI - z * this.getWidth();
         this.setTile(x, y, z, block.getNbtData());
      }

      return true;
   }
}
