package com.fastasyncworldedit.core.extent.clipboard;

import com.fastasyncworldedit.core.jnbt.streamer.IntValueReader;
import com.fastasyncworldedit.core.math.IntTriple;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CPUOptimizedClipboard extends LinearClipboard {
   private BiomeType[] biomes = null;
   private final char[] states = new char[this.getVolume()];
   private final HashMap<IntTriple, CompoundTag> nbtMapLoc = new HashMap<>();
   private final HashMap<Integer, CompoundTag> nbtMapIndex = new HashMap<>();

   public CPUOptimizedClipboard(Region region) {
      super(region.getDimensions(), region.getMinimumPoint());
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
         this.biomes = new BiomeType[((this.getHeight() >> 2) + 1) * ((this.getLength() >> 2) + 1) * ((this.getWidth() >> 2) + 1)];
      }

      this.biomes[index] = biome;
   }

   @Override
   public void streamBiomes(IntValueReader task) {
      if (this.hasBiomes()) {
         try {
            for (int y = 0; y < this.getHeight(); y++) {
               for (int z = 0; z < this.getLength(); z++) {
                  for (int x = 0; x < this.getWidth(); x++) {
                     task.applyInt(this.getIndex(x, y, z), this.biomes[this.getBiomeIndex(x, y, z)].getInternalId());
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
      return !this.hasBiomes() ? null : this.biomes[index];
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return this.getBiome(this.getBiomeIndex(x, y, z));
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.getBiome(this.getBiomeIndex(position.x(), position.y(), position.z()));
   }

   public void convertTilesToIndex() {
      if (!this.nbtMapLoc.isEmpty()) {
         for (Entry<IntTriple, CompoundTag> entry : this.nbtMapLoc.entrySet()) {
            IntTriple key = entry.getKey();
            this.setTile(this.getIndex(key.x(), key.y(), key.z()), entry.getValue());
         }

         this.nbtMapLoc.clear();
      }
   }

   private CompoundTag getTag(int index) {
      this.convertTilesToIndex();
      return this.nbtMapIndex.get(index);
   }

   public int getBiomeIndex(int x, int y, int z) {
      return (x >> 2) + (y >> 2) * (this.getWidth() >> 2) * (this.getLength() >> 2) + (z >> 2) * (this.getWidth() >> 2);
   }

   public int getIndex(int x, int y, int z) {
      return x + y * this.getArea() + z * this.getWidth();
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      int index = this.getIndex(x, y, z);
      return this.getFullBlock(index);
   }

   @Override
   public BaseBlock getFullBlock(int index) {
      BlockState block = this.getBlock(index);
      if (block.getMaterial().hasContainer()) {
         CompoundTag nbt = this.getTag(index);
         if (nbt != null) {
            return block.toBaseBlock(nbt);
         }
      }

      return block.toBaseBlock();
   }

   @Override
   public BlockState getBlock(int index) {
      char ordinal = this.states[index];
      return BlockState.getFromOrdinal(ordinal);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.getBlock(this.getIndex(x, y, z));
   }

   @Override
   public Collection<CompoundTag> getTileEntities() {
      this.convertTilesToIndex();
      this.nbtMapIndex.replaceAll((index, tag) -> {
         Map<String, Tag<?, ?>> values = new HashMap<>(tag.getValue());
         if (!values.containsKey("x")) {
            int y = index / this.getArea();
            index = index - y * this.getArea();
            int z = index / this.getWidth();
            int x = index - z * this.getWidth();
            values.put("x", new IntTag(x));
            values.put("y", new IntTag(y));
            values.put("z", new IntTag(z));
            return new CompoundTag(values);
         } else {
            return (CompoundTag)tag;
         }
      });
      return this.nbtMapIndex.values();
   }

   @Override
   public boolean setTile(int x, int y, int z, CompoundTag tag) {
      this.nbtMapLoc.put(new IntTriple(x, y, z), new CompoundTag(tag.getValue()));
      return true;
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tile) throws WorldEditException {
      return this.setTile(x, y, z, new CompoundTag(tile.linTag()));
   }

   private boolean setTile(int index, CompoundTag tag) {
      Map<String, Tag<?, ?>> values = new HashMap<>(tag.getValue());
      values.remove("x");
      values.remove("y");
      values.remove("z");
      this.nbtMapIndex.put(index, new CompoundTag(values));
      return true;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) {
      return this.setBlock(this.getIndex(x, y, z), block);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int index, B block) {
      char ordinal = block.getOrdinalChar();
      if (ordinal == 0) {
         ordinal = 1;
      }

      this.states[index] = ordinal;
      boolean hasNbt = block instanceof BaseBlock && block.hasNbtData();
      if (hasNbt) {
         this.setTile(index, block.getNbtData());
      }

      return true;
   }
}
