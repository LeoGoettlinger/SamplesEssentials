package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.internal.io.FastByteArrayOutputStream;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.util.collection.AdaptedMap;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IBlocks extends Trimable {
   boolean hasSection(int layer);

   default boolean hasNonEmptySection(int layer) {
      return this.hasSection(layer);
   }

   char[] load(int layer);

   @Nullable
   char[] loadIfPresent(int layer);

   BlockState getBlock(int x, int y, int z);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   default Map<BlockVector3, CompoundTag> getTiles() {
      return AdaptedMap.values(this.tiles(), ct -> FaweCompoundTag.of(ct.toLinTag()), IBlocks::toCompoundTag);
   }

   Map<BlockVector3, FaweCompoundTag> tiles();

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   default CompoundTag getTile(int x, int y, int z) {
      FaweCompoundTag tile = this.tile(x, y, z);
      return tile == null ? null : toCompoundTag(tile);
   }

   @Nonnull
   private static CompoundTag toCompoundTag(FaweCompoundTag tile) {
      return new CompoundTag(tile.linTag());
   }

   @Nullable
   FaweCompoundTag tile(int x, int y, int z);

   @Deprecated(
      forRemoval = true,
      since = "2.11.2"
   )
   default Set<CompoundTag> getEntities() {
      return this.entities().stream().map(IBlocks::toCompoundTag).collect(Collectors.toSet());
   }

   Collection<FaweCompoundTag> entities();

   BiomeType getBiomeType(int x, int y, int z);

   default int getBitMask() {
      return IntStream.range(this.getMinSectionPosition(), this.getMaxSectionPosition() + 1).filter(this::hasSection).map(layer -> 1 << layer).sum();
   }

   void removeSectionLighting(int layer, boolean sky);

   boolean trim(boolean aggressive, int layer);

   IBlocks reset();

   int getSectionCount();

   int getMaxSectionPosition();

   int getMinSectionPosition();

   int getX();

   int getZ();

   default byte[] toByteArray(boolean full, boolean stretched) {
      return this.toByteArray(null, this.getBitMask(), full, stretched);
   }

   default byte[] toByteArray(byte[] buffer, int bitMask, boolean full, boolean stretched) {
      if (buffer == null) {
         buffer = new byte[1024];
      }

      BlockRegistry registry = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry();
      FastByteArrayOutputStream sectionByteArray = new FastByteArrayOutputStream(buffer);

      try (FaweOutputStream sectionWriter = new FaweOutputStream(sectionByteArray)) {
         for (int layer = 0; layer < this.getSectionCount(); layer++) {
            if (this.hasSection(layer) && (bitMask & 1 << layer) != 0) {
               char[] ids = this.load(layer);
               int nonEmpty = 0;

               for (int i = 0; i < ids.length; i++) {
                  char ordinal = ids[i];
                  switch (ordinal) {
                     case '\u0000':
                     case '\u0002':
                     case '\u0003':
                        ids[i] = 1;
                     case '\u0001':
                        break;
                     default:
                        nonEmpty++;
                  }
               }

               sectionWriter.writeShort(nonEmpty);
               FaweCache.Palette palette;
               if (stretched) {
                  palette = FaweCache.INSTANCE.toPalette(0, ids);
               } else {
                  palette = FaweCache.INSTANCE.toPaletteUnstretched(0, ids);
               }

               sectionWriter.writeByte(palette.bitsPerEntry);
               sectionWriter.writeVarInt(palette.paletteToBlockLength);

               for (int i = 0; i < palette.paletteToBlockLength; i++) {
                  int ordinal = palette.paletteToBlock[i];
                  switch (ordinal) {
                     case 0:
                     case 1:
                     case 2:
                     case 3:
                        sectionWriter.write(0);
                        break;
                     default:
                        BlockState state = BlockState.getFromOrdinal(ordinal);
                        int mcId = registry.getInternalBlockStateId(state).getAsInt();
                        sectionWriter.writeVarInt(mcId);
                  }
               }

               sectionWriter.writeVarInt(palette.blockStatesLength);

               for (int i = 0; i < palette.blockStatesLength; i++) {
                  sectionWriter.writeLong(palette.blockStates[i]);
               }
            }
         }

         if (full) {
            for (int z = 0; z < 16; z++) {
               for (int x = 0; x < 16; x++) {
                  BiomeType biome = this.getBiomeType(x, 0, z);
                  if (biome != null) {
                     sectionWriter.writeInt(biome.getLegacyId());
                  } else {
                     sectionWriter.writeInt(0);
                  }
               }
            }
         }
      } catch (IOException var18) {
         var18.printStackTrace();
      }

      return sectionByteArray.toByteArray();
   }
}
