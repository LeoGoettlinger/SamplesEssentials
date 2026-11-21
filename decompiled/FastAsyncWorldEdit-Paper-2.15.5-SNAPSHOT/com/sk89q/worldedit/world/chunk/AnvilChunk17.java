package com.sk89q.worldedit.world.chunk;

import com.fastasyncworldedit.core.util.NbtUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

public class AnvilChunk17 implements Chunk {
   private final LinCompoundTag rootTag;
   private final Supplier<LinCompoundTag> entityTagSupplier;
   private BiomeType[] biomes;
   private BlockState[][] blocks;
   private Map<BlockVector3, LinCompoundTag> tileEntities;
   private List<BaseEntity> entities;
   private int minSectionPosition = 0;
   private int maxSectionPosition = 15;
   private int sectionCount = 16;

   @Deprecated
   public AnvilChunk17(CompoundTag tag, Supplier<CompoundTag> entitiesTag) throws DataException {
      this(tag.toLinTag(), () -> {
         CompoundTag compoundTag = entitiesTag.get();
         return compoundTag == null ? null : compoundTag.toLinTag();
      });
   }

   public AnvilChunk17(LinCompoundTag tag, Supplier<LinCompoundTag> entityTag) throws DataException {
      this.rootTag = tag;
      this.entityTagSupplier = entityTag;
      this.blocks = new BlockState[16][];
      LinListTag<LinTag<?>> sections = this.rootTag.getTag("Sections", LinTagType.listTag());

      for (LinTag<?> rawSectionTag : sections.value()) {
         if (rawSectionTag instanceof LinCompoundTag sectionTag) {
            LinByteTag sectionYTag = sectionTag.findTag("Y", LinTagType.byteTag());
            if (sectionYTag != null) {
               int y = NbtUtils.getInt(sectionTag, "Y");
               this.updateSectionIndexRange(y);
               LinListTag paletteEntries = sectionTag.getListTag("Palette", LinTagType.compoundTag());
               int paletteSize = paletteEntries.value().size();
               if (paletteSize != 0) {
                  BlockState[] palette = new BlockState[paletteSize];

                  for (int paletteEntryId = 0; paletteEntryId < paletteSize; paletteEntryId++) {
                     LinCompoundTag paletteEntry = (LinCompoundTag)paletteEntries.get(paletteEntryId);
                     BlockType type = BlockTypes.get(paletteEntry.getTag("Name", LinTagType.stringTag()).value());
                     if (type == null) {
                        throw new InvalidFormatException("Invalid block type: " + paletteEntry.getTag("Name", LinTagType.stringTag()).value());
                     }

                     BlockState blockState = type.getDefaultState();
                     LinCompoundTag properties = paletteEntry.findTag("Properties", LinTagType.compoundTag());
                     if (properties != null) {
                        for (Property<?> property : blockState.getStates().keySet()) {
                           LinStringTag stringTag = properties.findTag(property.getName(), LinTagType.stringTag());
                           if (stringTag != null) {
                              try {
                                 blockState = this.getBlockStateWith(blockState, property, stringTag.value());
                              } catch (IllegalArgumentException var21) {
                                 throw new InvalidFormatException(
                                    "Invalid block state for " + blockState.getBlockType().id() + ", " + property.getName() + ": " + stringTag.value()
                                 );
                              }
                           }
                        }
                     }

                     palette[paletteEntryId] = blockState;
                  }

                  long[] blockStatesSerialized = sectionTag.getTag("BlockStates", LinTagType.longArrayTag()).value();
                  BlockState[] chunkSectionBlocks = new BlockState[4096];
                  this.blocks[y - this.minSectionPosition] = chunkSectionBlocks;
                  this.readBlockStates(palette, blockStatesSerialized, chunkSectionBlocks);
               }
            }
         }
      }
   }

   private void updateSectionIndexRange(int layer) {
      if (layer < this.minSectionPosition || layer > this.maxSectionPosition) {
         if (layer < this.minSectionPosition) {
            int diff = this.minSectionPosition - layer;
            this.sectionCount += diff;
            BlockState[][] tmpBlocks = new BlockState[this.sectionCount][];
            System.arraycopy(this.blocks, 0, tmpBlocks, diff, this.blocks.length);
            this.blocks = tmpBlocks;
            this.minSectionPosition = layer;
         } else {
            int diff = layer - this.maxSectionPosition;
            this.sectionCount += diff;
            BlockState[][] tmpBlocks = new BlockState[this.sectionCount][];
            System.arraycopy(this.blocks, 0, tmpBlocks, 0, this.blocks.length);
            this.blocks = tmpBlocks;
            this.maxSectionPosition = layer;
         }
      }
   }

   protected void readBlockStates(BlockState[] palette, long[] blockStatesSerialized, BlockState[] chunkSectionBlocks) throws InvalidFormatException {
      PackedIntArrayReader reader = new PackedIntArrayReader(blockStatesSerialized);

      for (int blockPos = 0; blockPos < chunkSectionBlocks.length; blockPos++) {
         int index = reader.get(blockPos);
         if (index >= palette.length) {
            throw new InvalidFormatException("Invalid block state table entry: " + index);
         }

         chunkSectionBlocks[blockPos] = palette[index];
      }
   }

   private <T> BlockState getBlockStateWith(BlockState source, Property<T> property, String value) {
      return source.with(property, property.getValueFor(value));
   }

   private void populateTileEntities() {
      LinListTag<LinCompoundTag> tags = this.rootTag.findListTag("TileEntities", LinTagType.compoundTag());
      if (tags == null) {
         this.tileEntities = ImmutableMap.of();
      } else {
         Builder<BlockVector3, LinCompoundTag> tileEntitiesBuilder = ImmutableMap.builderWithExpectedSize(tags.value().size());

         for (LinCompoundTag tag : tags.value()) {
            int x = tag.getTag("x", LinTagType.intTag()).valueAsInt();
            int y = tag.getTag("y", LinTagType.intTag()).valueAsInt();
            int z = tag.getTag("z", LinTagType.intTag()).valueAsInt();
            BlockVector3 vec = BlockVector3.at(x, y, z);
            this.tileEntities.put(vec, tag);
         }

         this.tileEntities = tileEntitiesBuilder.build();
      }
   }

   @Nullable
   private LinCompoundTag getBlockTileEntity(BlockVector3 position) throws DataException {
      if (this.tileEntities == null) {
         this.populateTileEntities();
      }

      return this.tileEntities.get(position);
   }

   @Override
   public BaseBlock getBlock(BlockVector3 position) throws DataException {
      int x = position.x() & 15;
      int y = position.y();
      int z = position.z() & 15;
      int section = y >> 4;
      int yIndex = y & 15;
      if (section >= this.minSectionPosition && section <= this.maxSectionPosition) {
         BlockState[] sectionBlocks = this.blocks[section - this.minSectionPosition];
         BlockState state = sectionBlocks != null ? sectionBlocks[yIndex << 8 | z << 4 | x] : BlockTypes.AIR.getDefaultState();
         LinCompoundTag tileEntity = this.getBlockTileEntity(position);
         return tileEntity != null ? state.toBaseBlock(tileEntity) : state.toBaseBlock();
      } else {
         throw new DataException("Chunk does not contain position " + position);
      }
   }

   @Override
   public BiomeType getBiome(final BlockVector3 position) throws DataException {
      if (this.biomes == null) {
         this.populateBiomes();
      }

      int x = (position.x() & 15) >> 2;
      int y = position.y() - (this.minSectionPosition << 4) >> 2;
      int z = (position.z() & 15) >> 2;
      return this.biomes[y << 4 | z << 2 | x];
   }

   private void populateBiomes() throws DataException {
      this.biomes = new BiomeType[64 * this.blocks.length];
      LinIntArrayTag biomeTag = this.rootTag.findTag("Biomes", LinTagType.intArrayTag());
      if (biomeTag != null) {
         int[] stored = biomeTag.value();

         for (int i = 0; i < 1024; i++) {
            this.biomes[i] = BiomeTypes.getLegacy(stored[i]);
         }
      }
   }

   @Override
   public List<BaseEntity> getEntities() throws DataException {
      if (this.entities == null) {
         this.populateEntities();
      }

      return this.entities;
   }

   private void populateEntities() {
      this.entities = new ArrayList<>();
      LinCompoundTag entityTag;
      if (this.entityTagSupplier != null && (entityTag = this.entityTagSupplier.get()) != null) {
         LinListTag<LinCompoundTag> tags = entityTag.findListTag("Entities", LinTagType.compoundTag());
         if (tags != null) {
            for (LinCompoundTag tag : tags.value()) {
               this.entities.add(new BaseEntity(EntityTypes.get(tag.getTag("id", LinTagType.stringTag()).value()), LazyReference.computed(tag)));
            }
         }
      }
   }
}
