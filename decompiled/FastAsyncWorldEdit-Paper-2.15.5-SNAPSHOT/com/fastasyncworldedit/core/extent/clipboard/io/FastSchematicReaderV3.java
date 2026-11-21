package com.fastasyncworldedit.core.extent.clipboard.io;

import com.fastasyncworldedit.core.extent.clipboard.LinearClipboard;
import com.fastasyncworldedit.core.extent.clipboard.SimpleClipboard;
import com.fastasyncworldedit.core.internal.io.ResettableFileInputStream;
import com.fastasyncworldedit.core.internal.io.VarIntStreamIterator;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.util.IOUtil;
import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.ReaderUtil;
import com.sk89q.worldedit.extent.clipboard.io.sponge.VersionedDataFixer;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import com.sk89q.worldedit.world.entity.EntityType;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinTagType;
import org.jetbrains.annotations.ApiStatus.Internal;

public class FastSchematicReaderV3 implements ClipboardReader {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final byte CACHE_IDENTIFIER_END = 0;
   private static final byte CACHE_IDENTIFIER_BLOCK = 1;
   private static final byte CACHE_IDENTIFIER_BIOMES = 2;
   private static final byte CACHE_IDENTIFIER_ENTITIES = 3;
   private static final byte CACHE_IDENTIFIER_BLOCK_TILE_ENTITIES = 4;
   private final InputStream parentStream;
   private final MutableBlockVector3 dimensions = MutableBlockVector3.at(0, 0, 0);
   private final Set<Byte> remainingTags;
   private DataInputStream dataInputStream;
   private NBTInputStream nbtInputStream;
   private VersionedDataFixer dataFixer;
   private BlockVector3 offset;
   private BlockVector3 origin = BlockVector3.ZERO;
   private BlockState[] blockPalette;
   private BiomeType[] biomePalette;
   private int dataVersion = -1;
   private byte[] dataCache;
   private byte[] paletteCache;
   private OutputStream dataCacheWriter;
   private OutputStream paletteCacheWriter;

   public FastSchematicReaderV3(@NonNull InputStream stream) {
      Objects.requireNonNull(stream, "stream");
      if (stream instanceof ResettableFileInputStream) {
         stream.mark(Integer.MAX_VALUE);
         this.remainingTags = new HashSet<>();
      } else if (stream instanceof FileInputStream fileInputStream) {
         stream = new ResettableFileInputStream(fileInputStream);
         stream.mark(Integer.MAX_VALUE);
         this.remainingTags = new HashSet<>();
      } else if (!(stream instanceof FastBufferedInputStream) && !(stream instanceof BufferedInputStream)) {
         stream = new FastBufferedInputStream(stream);
         this.remainingTags = null;
      } else {
         this.remainingTags = null;
      }

      this.parentStream = stream;
   }

   @Override
   public Clipboard read(final UUID uuid, final Function<BlockVector3, Clipboard> createOutput) throws IOException {
      Clipboard clipboard = null;
      this.setSubStreams();
      skipHeader(this.dataInputStream);

      byte type;
      while ((type = this.dataInputStream.readByte()) != 0) {
         String tag = this.dataInputStream.readUTF();
         switch (tag) {
            case "DataVersion":
               Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
               this.dataVersion = this.dataInputStream.readInt();
               this.dataFixer = ReaderUtil.getVersionedDataFixer(this.dataVersion, platform, platform.getDataVersion());
               break;
            case "Metadata":
               LinCompoundTag metadataCompoundTag = (LinCompoundTag)this.nbtInputStream.readTagPayload(10, 0).toLinTag();
               LinCompoundTag worldEditTag = metadataCompoundTag.findTag("WorldEdit", LinTagType.compoundTag());
               if (worldEditTag != null) {
                  LinIntArrayTag originTag = worldEditTag.findTag("Origin", LinTagType.intArrayTag());
                  if (originTag != null) {
                     int[] parts = originTag.value();
                     if (parts.length != 3) {
                        throw new IOException("`Metadata > WorldEdit > Origin` int array length is invalid.");
                     }

                     this.origin = BlockVector3.at(parts[0], parts[1], parts[2]);
                  }
               }
               break;
            case "Offset":
               this.dataInputStream.skipNBytes(4L);
               this.offset = BlockVector3.at(this.dataInputStream.readInt(), this.dataInputStream.readInt(), this.dataInputStream.readInt());
               break;
            case "Width":
               this.dimensions.mutX(this.dataInputStream.readShort() & '\uffff');
               break;
            case "Height":
               this.dimensions.mutY(this.dataInputStream.readShort() & '\uffff');
               break;
            case "Length":
               this.dimensions.mutZ(this.dataInputStream.readShort() & '\uffff');
               break;
            case "Blocks":
               this.readBlocks(clipboard);
               break;
            case "Biomes":
               this.readBiomes(clipboard);
               break;
            case "Entities":
               this.readEntities(clipboard);
               break;
            default:
               this.nbtInputStream.readTagPayloadLazy(type, 0);
         }

         if (clipboard == null && this.areDimensionsAvailable()) {
            clipboard = createOutput.apply(this.dimensions.toImmutable());
         }
      }

      if (clipboard == null) {
         throw new IOException("Invalid schematic - missing dimensions");
      } else if (this.dataFixer == null) {
         throw new IOException("Invalid schematic - missing DataVersion");
      } else {
         if (this.supportsReset() && !this.remainingTags.isEmpty()) {
            this.readRemainingDataReset(clipboard);
         } else if (this.dataCacheWriter != null || this.paletteCacheWriter != null) {
            this.readRemainingDataCache(clipboard);
         }

         clipboard.setOrigin(this.offset.multiply(-1));
         if (clipboard instanceof SimpleClipboard simpleClipboard && !this.offset.equals(BlockVector3.ZERO)) {
            clipboard = new BlockArrayClipboard(simpleClipboard, this.offset.add(this.origin));
         }

         return clipboard;
      }
   }

   private void readRemainingDataCache(Clipboard clipboard) throws IOException {
      if (this.paletteCacheWriter != null) {
         this.paletteCacheWriter.close();
      }

      if (this.dataCacheWriter != null) {
         this.dataCacheWriter.close();
      }

      if (this.paletteCache != null) {
         byte identifier;
         try (DataInputStream cacheStream = new DataInputStream(
               new FastBufferedInputStream(new LZ4BlockInputStream(new FastBufferedInputStream(new ByteArrayInputStream(this.paletteCache))))
            )) {
            while ((identifier = cacheStream.readByte()) != 0) {
               if (identifier == 1) {
                  this.readPaletteMap(cacheStream, this.provideBlockPaletteInitializer());
               } else {
                  if (identifier != 2) {
                     throw new IOException("invalid cache state - got identifier: 0x" + identifier);
                  }

                  this.readPaletteMap(cacheStream, this.provideBiomePaletteInitializer());
               }
            }
         }
      }

      byte identifier;
      try (
         DataInputStream cacheStream = new DataInputStream(
            new FastBufferedInputStream(new LZ4BlockInputStream(new FastBufferedInputStream(new ByteArrayInputStream(this.dataCache))))
         );
         NBTInputStream cacheNbtIn = new NBTInputStream(cacheStream);
      ) {
         while ((identifier = cacheStream.readByte()) != 0) {
            switch (identifier) {
               case 1:
                  this.readPaletteData(cacheStream, this.getBlockWriter(clipboard));
                  break;
               case 2:
                  this.readPaletteData(cacheStream, this.getBiomeWriter(clipboard));
                  break;
               case 3:
                  this.readEntityContainers(cacheStream, cacheNbtIn, DataFixer.FixTypes.ENTITY, this.provideEntityTransformer(clipboard));
                  break;
               case 4:
                  this.readEntityContainers(cacheStream, cacheNbtIn, DataFixer.FixTypes.BLOCK_ENTITY, this.provideTileEntityTransformer(clipboard));
                  break;
               default:
                  throw new IOException("invalid cache state - got identifier: 0x" + identifier);
            }
         }
      }
   }

   private void readRemainingDataReset(Clipboard clipboard) throws IOException {
      while (!this.remainingTags.isEmpty()) {
         this.reset();
         skipHeader(this.dataInputStream);

         byte type;
         while ((type = this.dataInputStream.readByte()) != 0) {
            String tag = this.dataInputStream.readUTF();
            byte b = (byte)(tag.equals("Blocks") ? 1 : (tag.equals("Biomes") ? 2 : (tag.equals("Entities") ? 3 : 0)));
            if (!this.remainingTags.remove(b)) {
               this.nbtInputStream.readTagPayloadLazy(type, 0);
            } else {
               switch (tag) {
                  case "Blocks":
                     this.readBlocks(clipboard);
                     break;
                  case "Biomes":
                     this.readBiomes(clipboard);
                     break;
                  case "Entities":
                     this.readEntities(clipboard);
                     break;
                  default:
                     this.nbtInputStream.readTagPayloadLazy(type, 0);
               }

               if (this.remainingTags.isEmpty()) {
                  return;
               }
            }
         }
      }
   }

   @Override
   public OptionalInt getDataVersion() {
      return this.dataVersion > -1 ? OptionalInt.of(this.dataVersion) : OptionalInt.empty();
   }

   private void readBlocks(Clipboard target) throws IOException {
      this.blockPalette = new BlockState[BlockTypesCache.states.length];
      this.readPalette(
         target != null, (byte)1, () -> this.blockPalette[0] != null, this.provideBlockPaletteInitializer(), this.getBlockWriter(target), (type, tag) -> {
            if (!tag.equals("BlockEntities")) {
               try {
                  this.nbtInputStream.readTagPayloadLazy(9, 0);
               } catch (IOException var5) {
                  LOGGER.error("Failed to skip additional tag", var5);
               }
            } else {
               try {
                  this.readTileEntities(target);
               } catch (IOException var6) {
                  LOGGER.warn("Failed to read tile entities", var6);
               }
            }
         }
      );
   }

   private void readBiomes(Clipboard target) throws IOException {
      this.biomePalette = new BiomeType[BiomeType.REGISTRY.size()];
      this.readPalette(
         target != null, (byte)2, () -> this.biomePalette[0] != null, this.provideBiomePaletteInitializer(), this.getBiomeWriter(target), (type, tag) -> {
            try {
               this.nbtInputStream.readTagPayloadLazy(type, 0);
            } catch (IOException var4) {
               LOGGER.error("Failed to skip additional tag in biome container: {}", tag, var4);
            }
         }
      );
   }

   private void readEntities(@Nullable Clipboard target) throws IOException {
      if (target != null && this.dataFixer != null) {
         this.readEntityContainers(this.dataInputStream, this.nbtInputStream, DataFixer.FixTypes.ENTITY, this.provideEntityTransformer(target));
      } else if (this.supportsReset()) {
         this.remainingTags.add((byte)3);
         this.nbtInputStream.readTagPayloadLazy(9, 0);
      } else {
         NBTOutputStream cacheStream = new NBTOutputStream(this.getDataCacheWriter());
         cacheStream.writeByte(3);
         cacheStream.writeTagPayload(this.nbtInputStream.readTagPayload(9, 0));
      }
   }

   private void readTileEntities(Clipboard target) throws IOException {
      if (target != null && this.dataFixer != null) {
         this.readEntityContainers(this.dataInputStream, this.nbtInputStream, DataFixer.FixTypes.BLOCK_ENTITY, this.provideTileEntityTransformer(target));
      } else if (this.supportsReset()) {
         this.remainingTags.add((byte)1);
         this.nbtInputStream.readTagPayloadLazy(9, 0);
      } else {
         NBTOutputStream cacheStream = new NBTOutputStream(this.getDataCacheWriter());
         cacheStream.writeByte(4);
         cacheStream.writeTagPayload(this.nbtInputStream.readTagPayload(9, 0));
      }
   }

   private void readEntityContainers(
      DataInputStream stream, NBTInputStream nbtStream, DataFixer.FixType<LinCompoundTag> fixType, FastSchematicReaderV3.EntityTransformer transformer
   ) throws IOException {
      int listType = stream.read();
      int count = stream.readInt();
      if (count > 0 && listType != 10) {
         throw new IOException("Expected a compound block for tile entity list");
      } else {
         while (count-- > 0) {
            double x = -1.0;
            double y = -1.0;
            double z = -1.0;
            LinCompoundTag tag = null;
            String id = null;

            byte type;
            while ((type = stream.readByte()) != 0) {
               switch (type) {
                  case 8:
                     if (!stream.readUTF().equals("Id")) {
                        throw new IOException("Expected STRING tag to be Id");
                     }

                     id = stream.readUTF();
                     break;
                  case 9:
                     if (!stream.readUTF().equals("Pos")) {
                        throw new IOException("Expected LIST tag to be Pos");
                     }

                     if (stream.readByte() != 6) {
                        throw new IOException("Expected LIST Pos tag to contain DOUBLE");
                     }

                     stream.skipNBytes(4L);
                     x = stream.readDouble();
                     y = stream.readDouble();
                     z = stream.readDouble();
                     break;
                  case 10:
                     if (!stream.readUTF().equals("Data")) {
                        throw new IOException("Expected COMPOUND tag to be Data");
                     }

                     if (!(nbtStream.readTagPayload(10, 0).toLinTag() instanceof LinCompoundTag lin)) {
                        throw new IOException("Data tag could not be read into LinCompoundTag");
                     }

                     tag = lin;
                     break;
                  case 11:
                     if (!stream.readUTF().equals("Pos")) {
                        throw new IOException("Expected INT_ARRAY tag to be Pos");
                     }

                     stream.skipNBytes(4L);
                     x = stream.readInt();
                     y = stream.readInt();
                     z = stream.readInt();
                     break;
                  default:
                     throw new IOException("Unexpected tag in compound: " + type);
               }
            }

            if (id == null) {
               throw new IOException("Missing Id tag in compound");
            }

            if (x < 0.0 || y < 0.0 || z < 0.0) {
               throw new IOException("Missing position for entity " + id);
            }

            if (tag == null) {
               transformer.transform(x, y, z, id, LinCompoundTag.of(Map.of()));
            } else {
               tag = tag.toBuilder().putString("id", id).remove("Id").build();
               tag = this.dataFixer.fixUp(fixType, tag);
               if (tag == null) {
                  LOGGER.warn("Failed to fix-up entity for {} @ {},{},{} - skipping", id, x, y, z);
               } else {
                  transformer.transform(x, y, z, id, tag);
               }
            }
         }
      }
   }

   private void readPalette(
      boolean hasClipboard,
      byte paletteType,
      BooleanSupplier paletteAlreadyInitialized,
      FastSchematicReaderV3.PaletteInitializer paletteInitializer,
      FastSchematicReaderV3.PaletteDataApplier paletteDataApplier,
      FastSchematicReaderV3.AdditionalTagConsumer additionalTag
   ) throws IOException {
      boolean hasPalette = paletteAlreadyInitialized.getAsBoolean();

      byte type;
      while ((type = this.dataInputStream.readByte()) != 0) {
         String tag = this.dataInputStream.readUTF();
         if (tag.equals("Palette")) {
            if (hasPalette) {
               this.nbtInputStream.readTagPayloadLazy(10, 0);
            } else if (!this.readPaletteMap(this.dataInputStream, paletteInitializer)) {
               if (this.supportsReset()) {
                  this.remainingTags.add(paletteType);
                  this.nbtInputStream.readTagPayloadLazy(10, 0);
               } else {
                  NBTOutputStream cacheWriter = new NBTOutputStream(this.getPaletteCacheWriter());
                  cacheWriter.write(paletteType);
                  cacheWriter.writeTagPayload(this.nbtInputStream.readTagPayload(10, 0));
               }
            } else {
               hasPalette = true;
            }
         } else if (tag.equals("Data")) {
            if (hasPalette && this.dataFixer != null && hasClipboard) {
               this.readPaletteData(this.dataInputStream, paletteDataApplier);
            } else if (this.supportsReset()) {
               this.remainingTags.add(paletteType);
               this.nbtInputStream.readTagPayloadLazy(7, 0);
            } else {
               int byteLen = this.dataInputStream.readInt();
               DataOutputStream cacheWriter = new DataOutputStream(this.getDataCacheWriter());
               cacheWriter.write(paletteType);
               cacheWriter.writeInt(byteLen);
               IOUtil.copy(this.dataInputStream, cacheWriter, byteLen);
            }
         } else {
            additionalTag.accept(type, tag);
         }
      }
   }

   private void readPaletteData(DataInputStream stream, FastSchematicReaderV3.PaletteDataApplier applier) throws IOException {
      int length = stream.readInt();
      int i = 0;
      if (this.needsVarIntReading(length)) {
         for (VarIntStreamIterator iter = new VarIntStreamIterator(stream, length); iter.hasNext(); i++) {
            applier.apply(i, (char)iter.nextInt());
         }
      } else {
         while (i < length) {
            applier.apply(i++, (char)stream.readUnsignedByte());
         }
      }
   }

   private boolean readPaletteMap(DataInputStream stream, FastSchematicReaderV3.PaletteInitializer initializer) throws IOException {
      if (this.dataFixer == null) {
         return false;
      } else {
         while (stream.readByte() != 0) {
            String value = stream.readUTF();
            char index = (char)stream.readInt();
            initializer.initialize(index, value);
         }

         return true;
      }
   }

   private void indexToPosition(int index, FastSchematicReaderV3.PositionConsumer supplier) {
      int y = index / (this.dimensions.x() * this.dimensions.z());
      int remainder = index - y * this.dimensions.x() * this.dimensions.z();
      int z = remainder / this.dimensions.x();
      int x = remainder - z * this.dimensions.x();
      supplier.accept(x, y, z);
   }

   private FastSchematicReaderV3.PaletteDataApplier getBlockWriter(Clipboard target) {
      return target instanceof LinearClipboard linearClipboard
         ? (index, ordinal) -> linearClipboard.setBlock(index, this.blockPalette[ordinal])
         : (index, ordinal) -> this.indexToPosition(index, (x, y, z) -> target.setBlock(x, y, z, this.blockPalette[ordinal]));
   }

   private FastSchematicReaderV3.PaletteDataApplier getBiomeWriter(Clipboard target) {
      return (index, ordinal) -> this.indexToPosition(index, (x, y, z) -> target.setBiome(x, y, z, this.biomePalette[ordinal]));
   }

   private FastSchematicReaderV3.PaletteInitializer provideBlockPaletteInitializer() {
      return (index, value) -> {
         if (this.dataFixer == null) {
            throw new IllegalStateException("Can't read block palette map if DataFixer is not yet available");
         } else {
            value = this.dataFixer.fixUp(DataFixer.FixTypes.BLOCK_STATE, value);

            try {
               this.blockPalette[index] = BlockState.get(value);
            } catch (InputParseException var4) {
               LOGGER.warn("Invalid BlockState in palette: {}. Block will be replaced with air.", value);
               this.blockPalette[index] = BlockTypes.AIR.getDefaultState();
            }
         }
      };
   }

   private FastSchematicReaderV3.PaletteInitializer provideBiomePaletteInitializer() {
      return (index, value) -> {
         if (this.dataFixer == null) {
            throw new IllegalStateException("Can't read biome palette map if DataFixer is not yet available");
         } else {
            value = this.dataFixer.fixUp(DataFixer.FixTypes.BIOME, value);
            BiomeType biomeType = BiomeTypes.get(value);
            if (biomeType == null) {
               biomeType = BiomeTypes.PLAINS;
               LOGGER.warn("Invalid biome type in palette: {}. Biome will be replaced with plains.", value);
            }

            this.biomePalette[index] = biomeType;
         }
      };
   }

   private FastSchematicReaderV3.EntityTransformer provideEntityTransformer(Clipboard clipboard) {
      return (x, y, z, id, tag) -> {
         EntityType type = EntityType.REGISTRY.get(id);
         if (type == null) {
            LOGGER.warn("Invalid entity id: {} - skipping", id);
         } else {
            clipboard.createEntity(
               new Location(clipboard, Location.at(x, y, z).add(clipboard.getMinimumPoint().toVector3())), new BaseEntity(type, LazyReference.computed(tag))
            );
         }
      };
   }

   private FastSchematicReaderV3.EntityTransformer provideTileEntityTransformer(Clipboard clipboard) {
      return (x, y, z, id, tag) -> clipboard.tile(
         MathMan.roundInt(x + clipboard.getMinimumPoint().x()),
         MathMan.roundInt(y + clipboard.getMinimumPoint().y()),
         MathMan.roundInt(z + clipboard.getMinimumPoint().z()),
         FaweCompoundTag.of(tag)
      );
   }

   private boolean areDimensionsAvailable() {
      return this.dimensions.x() != 0 && this.dimensions.y() != 0 && this.dimensions.z() != 0;
   }

   @Override
   public void close() throws IOException {
      this.parentStream.close();
   }

   private void reset() throws IOException {
      if (this.supportsReset()) {
         this.parentStream.reset();
         this.parentStream.mark(Integer.MAX_VALUE);
         this.setSubStreams();
      }
   }

   private boolean supportsReset() {
      return this.remainingTags != null;
   }

   private void setSubStreams() throws IOException {
      FastBufferedInputStream buffer = new FastBufferedInputStream(new GZIPInputStream(this.parentStream));
      this.dataInputStream = new DataInputStream(buffer);
      this.nbtInputStream = new NBTInputStream(buffer);
   }

   private OutputStream getDataCacheWriter() {
      if (this.dataCacheWriter == null) {
         final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
         this.dataCacheWriter = new FastBufferedOutputStream(new LZ4BlockOutputStream(byteArrayOutputStream)) {
            public void close() throws IOException {
               this.write(0);
               super.close();
               FastSchematicReaderV3.this.dataCache = byteArrayOutputStream.toByteArray();
            }
         };
      }

      return this.dataCacheWriter;
   }

   private OutputStream getPaletteCacheWriter() {
      if (this.paletteCacheWriter == null) {
         final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256);
         this.paletteCacheWriter = new FastBufferedOutputStream(new LZ4BlockOutputStream(byteArrayOutputStream)) {
            public void close() throws IOException {
               this.write(0);
               super.close();
               FastSchematicReaderV3.this.paletteCache = byteArrayOutputStream.toByteArray();
            }
         };
      }

      return this.paletteCacheWriter;
   }

   private boolean needsVarIntReading(int byteArrayLength) {
      return byteArrayLength > this.dimensions.x() * this.dimensions.y() * this.dimensions.z();
   }

   private static void skipHeader(DataInputStream dataInputStream) throws IOException {
      dataInputStream.skipNBytes(3L);
      dataInputStream.skipNBytes(12L);
   }

   @FunctionalInterface
   @Internal
   private interface AdditionalTagConsumer {
      void accept(byte type, String name);
   }

   @FunctionalInterface
   @Internal
   private interface EntityTransformer {
      void transform(double x, double y, double z, String id, LinCompoundTag tag);
   }

   @FunctionalInterface
   @Internal
   private interface PaletteDataApplier {
      void apply(int index, char ordinal);
   }

   @FunctionalInterface
   @Internal
   private interface PaletteInitializer {
      void initialize(char index, String value);
   }

   @FunctionalInterface
   @Internal
   private interface PositionConsumer {
      void accept(int x, int y, int z);
   }
}
