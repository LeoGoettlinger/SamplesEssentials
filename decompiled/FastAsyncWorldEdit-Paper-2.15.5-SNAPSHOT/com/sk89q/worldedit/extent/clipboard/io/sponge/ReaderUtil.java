package com.sk89q.worldedit.extent.clipboard.io.sponge;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.VarIntIterator;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.storage.NBTConversions;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

public class ReaderUtil {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   static void checkSchematicVersion(int version, LinCompoundTag schematicTag) throws IOException {
      int schematicVersion = getSchematicVersion(schematicTag);
      Preconditions.checkState(version == schematicVersion, "Schematic is not version %s, but %s", version, schematicVersion);
   }

   public static int getSchematicVersion(LinCompoundTag schematicTag) throws IOException {
      return schematicTag.getTag("Version", LinTagType.intTag()).valueAsInt();
   }

   static VersionedDataFixer getVersionedDataFixer(LinCompoundTag schematic, Platform platform, int liveDataVersion) {
      return getVersionedDataFixer(schematic.getTag("DataVersion", LinTagType.intTag()).valueAsInt(), platform, liveDataVersion);
   }

   public static VersionedDataFixer getVersionedDataFixer(int schematicDataVersion, Platform platform, int liveDataVersion) {
      DataFixer fixer = null;
      if (schematicDataVersion < 0) {
         LOGGER.warn("Schematic has an unknown data version ({}). Data may be incompatible.", schematicDataVersion);
      } else if (schematicDataVersion > liveDataVersion) {
         LOGGER.warn("Schematic was made in a newer Minecraft version ({} > {}). Data may be incompatible.", schematicDataVersion, liveDataVersion);
      } else if (schematicDataVersion < liveDataVersion) {
         fixer = platform.getDataFixer();
         if (fixer != null) {
            LOGGER.debug("Schematic was made in an older Minecraft version ({} < {}), will attempt DFU.", schematicDataVersion, liveDataVersion);
         } else {
            LOGGER.info(
               "Schematic was made in an older Minecraft version ({} < {}), but DFU is not available. Data may be incompatible.",
               schematicDataVersion,
               liveDataVersion
            );
         }
      }

      return new VersionedDataFixer(schematicDataVersion, fixer);
   }

   static Map<Integer, BlockState> decodePalette(LinCompoundTag paletteObject, VersionedDataFixer fixer) throws IOException {
      Map<Integer, BlockState> palette = new HashMap<>();
      ParserContext parserContext = new ParserContext();
      parserContext.setRestricted(false);
      parserContext.setTryLegacy(false);
      parserContext.setPreferringWildcard(false);

      for (Entry<String, LinTag<?>> palettePart : paletteObject.value().entrySet()) {
         if (!(palettePart.getValue() instanceof LinIntTag idTag)) {
            throw new IOException("Invalid palette entry: " + palettePart);
         }

         int var12 = idTag.valueAsInt();
         String paletteName = fixer.fixUp(DataFixer.FixTypes.BLOCK_STATE, palettePart.getKey());

         BlockState state;
         try {
            state = WorldEdit.getInstance().getBlockFactory().parseFromInput(paletteName, parserContext).toImmutableState();
         } catch (InputParseException var11) {
            LOGGER.warn("Invalid BlockState in palette: " + palettePart + ". Block will be replaced with air.");
            state = BlockTypes.AIR.getDefaultState();
         }

         palette.put(var12, state);
      }

      return palette;
   }

   static void initializeClipboardFromBlocks(
      Clipboard clipboard,
      Map<Integer, BlockState> palette,
      byte[] blocks,
      LinListTag<LinCompoundTag> tileEntities,
      VersionedDataFixer fixer,
      boolean dataIsNested
   ) throws IOException {
      Map<BlockVector3, LinCompoundTag> tileEntitiesMap = new HashMap<>();
      if (tileEntities != null) {
         for (LinCompoundTag tileEntity : tileEntities.value()) {
            BlockVector3 pt = clipboard.getMinimumPoint().add(decodeBlockVector3(tileEntity.getTag("Pos", LinTagType.intArrayTag())));
            LinCompoundTag.Builder values = extractData(dataIsNested, tileEntity);
            values.putInt("x", pt.x());
            values.putInt("y", pt.y());
            values.putInt("z", pt.z());
            values.put("id", tileEntity.value().get("Id"));
            if (fixer.isActive()) {
               tileEntity = fixer.fixUp(DataFixer.FixTypes.BLOCK_ENTITY, values.build());
            } else {
               tileEntity = values.build();
            }

            tileEntitiesMap.put(pt, tileEntity);
         }
      }

      int width = clipboard.getRegion().getWidth();
      int length = clipboard.getRegion().getLength();
      int index = 0;

      for (VarIntIterator iter = new VarIntIterator(blocks); iter.hasNext(); index++) {
         int nextBlockId = iter.nextInt();
         BlockState state = palette.get(nextBlockId);
         BlockVector3 rawPos = decodePositionFromDataIndex(width, length, index);

         try {
            BlockVector3 offsetPos = clipboard.getMinimumPoint().add(rawPos);
            LinCompoundTag tileEntity = tileEntitiesMap.get(offsetPos);
            clipboard.setBlock(offsetPos, state.toBaseBlock(tileEntity));
         } catch (WorldEditException var16) {
            throw new IOException("Failed to load a block in the schematic", var16);
         }
      }
   }

   private static LinCompoundTag.Builder extractData(boolean dataIsNested, LinCompoundTag tag) {
      if (dataIsNested) {
         LinCompoundTag dataTag = tag.findTag("Data", LinTagType.compoundTag());
         return dataTag != null ? dataTag.toBuilder() : LinCompoundTag.builder();
      } else {
         LinCompoundTag.Builder values = tag.toBuilder();
         values.remove("Id");
         values.remove("Pos");
         return values;
      }
   }

   static BlockVector3 decodePositionFromDataIndex(int width, int length, int index) {
      int y = index / (width * length);
      int remainder = index - y * width * length;
      int z = remainder / width;
      int x = remainder - z * width;
      return BlockVector3.at(x, y, z);
   }

   static BlockVector3 decodeBlockVector3(@Nullable LinIntArrayTag tag) throws IOException {
      if (tag == null) {
         return BlockVector3.ZERO;
      } else {
         int[] parts = tag.value();
         if (parts.length != 3) {
            throw new IOException("Invalid block vector specified in schematic.");
         } else {
            return BlockVector3.at(parts[0], parts[1], parts[2]);
         }
      }
   }

   static void readEntities(BlockArrayClipboard clipboard, List<? extends LinCompoundTag> entList, VersionedDataFixer fixer, boolean positionIsRelative) {
      if (!entList.isEmpty()) {
         for (LinCompoundTag entityTag : entList) {
            String id = entityTag.getTag("Id", LinTagType.stringTag()).value();
            LinCompoundTag.Builder values = extractData(positionIsRelative, entityTag);
            LinCompoundTag dataTag = values.putString("id", id).build();
            dataTag = fixer.fixUp(DataFixer.FixTypes.ENTITY, dataTag);
            EntityType entityType = EntityTypes.get(id);
            if (entityType != null) {
               Location location = NBTConversions.toLocation(
                  clipboard, entityTag.getListTag("Pos", LinTagType.doubleTag()), dataTag.getListTag("Rotation", LinTagType.floatTag())
               );
               BaseEntity state = new BaseEntity(entityType, LazyReference.computed(dataTag));
               if (positionIsRelative) {
                  location = location.setPosition(location.toVector().add(clipboard.getMinimumPoint().toVector3()));
               }

               clipboard.createEntity(location, state);
            } else {
               LOGGER.warn("Unknown entity when pasting schematic: " + id);
            }
         }
      }
   }

   static Int2ObjectMap<BiomeType> readBiomePalette(VersionedDataFixer fixer, LinCompoundTag paletteTag, Logger logger) throws IOException {
      Int2ObjectMap<BiomeType> palette = new Int2ObjectLinkedOpenHashMap(paletteTag.value().size());

      for (Entry<String, LinTag<?>> palettePart : paletteTag.value().entrySet()) {
         String key = palettePart.getKey();
         key = fixer.fixUp(DataFixer.FixTypes.BIOME, key);
         BiomeType biome = BiomeTypes.get(key);
         if (biome == null) {
            logger.warn("Unknown biome type :" + key + " in palette. Are you missing a mod or using a schematic made in a newer version of Minecraft?");
         }

         if (!(palettePart.getValue() instanceof LinIntTag idTag)) {
            throw new IOException("Biome mapped to non-Int tag.");
         }

         palette.put(idTag.valueAsInt(), biome);
      }

      return palette;
   }

   private ReaderUtil() {
   }
}
