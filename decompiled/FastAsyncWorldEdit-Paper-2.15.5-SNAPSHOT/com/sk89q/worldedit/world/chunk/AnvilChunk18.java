package com.sk89q.worldedit.world.chunk;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongArrayTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTagType;

public class AnvilChunk18 implements Chunk {
   private final CompoundTag rootTag;
   private final Supplier<LinCompoundTag> entityTagSupplier;
   private final Int2ObjectOpenHashMap<BlockState[]> blocks;
   private final int rootX;
   private final int rootZ;
   private Map<BlockVector3, Map<String, Tag<?, ?>>> tileEntities;
   private List<BaseEntity> entities;

   public AnvilChunk18(CompoundTag tag, Supplier<LinCompoundTag> entityTag) throws DataException {
      this.rootTag = tag;
      this.entityTagSupplier = entityTag;
      this.rootX = NBTUtils.getChildTag(this.rootTag.getValue(), "xPos", IntTag.class).getValue();
      this.rootZ = NBTUtils.getChildTag(this.rootTag.getValue(), "zPos", IntTag.class).getValue();
      List<Tag> sections = NBTUtils.getChildTag(this.rootTag.getValue(), "sections", ListTag.class).getValue();
      this.blocks = new Int2ObjectOpenHashMap(sections.size());

      for (Tag rawSectionTag : sections) {
         if (rawSectionTag instanceof CompoundTag sectionTag) {
            Object yValue = sectionTag.getValue().get("Y").getValue();
            if (!(yValue instanceof Number)) {
               throw new InvalidFormatException("Y is not numeric: " + yValue);
            }

            int y = ((Number)yValue).intValue();
            Tag rawBlockStatesTag = sectionTag.getValue().get("block_states");
            if (rawBlockStatesTag instanceof CompoundTag blockStatesTag) {
               List<CompoundTag> paletteEntries = blockStatesTag.getList("palette", CompoundTag.class);
               int paletteSize = paletteEntries.size();
               if (paletteSize != 0) {
                  BlockState[] palette = new BlockState[paletteSize];

                  for (int paletteEntryId = 0; paletteEntryId < paletteSize; paletteEntryId++) {
                     CompoundTag paletteEntry = paletteEntries.get(paletteEntryId);
                     BlockType type = BlockTypes.get(paletteEntry.getString("Name"));
                     if (type == null) {
                        throw new InvalidFormatException("Invalid block type: " + paletteEntry.getString("Name"));
                     }

                     BlockState blockState = type.getDefaultState();
                     if (paletteEntry.containsKey("Properties")) {
                        CompoundTag properties = NBTUtils.getChildTag(paletteEntry.getValue(), "Properties", CompoundTag.class);

                        for (Property<?> property : blockState.getStates().keySet()) {
                           if (properties.containsKey(property.getName())) {
                              String value = properties.getString(property.getName());

                              try {
                                 blockState = this.getBlockStateWith(blockState, property, value);
                              } catch (IllegalArgumentException var23) {
                                 throw new InvalidFormatException(
                                    "Invalid block state for " + blockState.getBlockType().id() + ", " + property.getName() + ": " + value
                                 );
                              }
                           }
                        }
                     }

                     palette[paletteEntryId] = blockState;
                  }

                  if (paletteSize == 1) {
                     this.blocks.put(y, palette);
                  } else {
                     long[] blockStatesSerialized = NBTUtils.getChildTag(blockStatesTag.getValue(), "data", LongArrayTag.class).getValue();
                     BlockState[] chunkSectionBlocks = new BlockState[4096];
                     this.blocks.put(y, chunkSectionBlocks);
                     this.readBlockStates(palette, blockStatesSerialized, chunkSectionBlocks);
                  }
               }
            }
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

   private void populateTileEntities() throws DataException {
      this.tileEntities = new HashMap<>();
      if (this.rootTag.getValue().containsKey("block_entities")) {
         for (Tag tag : NBTUtils.getChildTag(this.rootTag.getValue(), "block_entities", ListTag.class).getValue()) {
            if (!(tag instanceof CompoundTag t)) {
               throw new InvalidFormatException("CompoundTag expected in block_entities");
            }

            Map<String, Tag<?, ?>> values = new HashMap<>(t.getValue());
            int x = ((IntTag)values.get("x")).getValue();
            int y = ((IntTag)values.get("y")).getValue();
            int z = ((IntTag)values.get("z")).getValue();
            BlockVector3 vec = BlockVector3.at(x, y, z);
            this.tileEntities.put(vec, values);
         }
      }
   }

   @Nullable
   private CompoundTag getBlockTileEntity(BlockVector3 position) throws DataException {
      if (this.tileEntities == null) {
         this.populateTileEntities();
      }

      Map<String, Tag<?, ?>> values = this.tileEntities.get(position);
      return values == null ? null : new CompoundTag(values);
   }

   @Override
   public BaseBlock getBlock(BlockVector3 position) throws DataException {
      int x = position.x() - this.rootX * 16;
      int y = position.y();
      int z = position.z() - this.rootZ * 16;
      int section = y >> 4;
      int yIndex = y & 15;
      BlockState[] sectionBlocks = (BlockState[])this.blocks.get(section);
      if (sectionBlocks == null) {
         return BlockTypes.AIR.getDefaultState().toBaseBlock();
      } else {
         BlockState state = sectionBlocks[sectionBlocks.length == 1 ? 0 : yIndex << 8 | z << 4 | x];
         CompoundTag tileEntity = this.getBlockTileEntity(position);
         return tileEntity != null ? state.toBaseBlock(tileEntity) : state.toBaseBlock();
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
