package com.sk89q.worldedit.extent.clipboard.io;

import com.fastasyncworldedit.core.extent.clipboard.io.FastSchematicReaderV2;
import com.fastasyncworldedit.core.extent.clipboard.io.FastSchematicReaderV3;
import com.fastasyncworldedit.core.extent.clipboard.io.FastSchematicWriterV2;
import com.fastasyncworldedit.core.extent.clipboard.io.FastSchematicWriterV3;
import com.fastasyncworldedit.core.extent.clipboard.io.schematic.MinecraftStructure;
import com.fastasyncworldedit.core.extent.clipboard.io.schematic.PNGWriter;
import com.fastasyncworldedit.core.internal.io.ResettableFileInputStream;
import com.fastasyncworldedit.core.internal.io.parallelgzip.ParallelGZIPOutputStream;
import com.google.common.collect.ImmutableSet;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV1Reader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV2Reader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV2Writer;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV3Reader;
import com.sk89q.worldedit.extent.clipboard.io.sponge.SpongeSchematicV3Writer;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.stream.LinReadOptions;
import org.enginehub.linbus.tree.LinRootEntry;

public enum BuiltInClipboardFormat implements ClipboardFormat {
   FAST_V3("fast", "fawe", "schem") {
      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         return new FastSchematicReaderV3(inputStream);
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         OutputStream gzip;
         if (!(outputStream instanceof ParallelGZIPOutputStream) && !(outputStream instanceof GZIPOutputStream)) {
            OutputStream var4 = new BufferedOutputStream(outputStream);
            gzip = new ParallelGZIPOutputStream(var4);
         } else {
            gzip = outputStream;
         }

         NBTOutputStream nbtStream = new NBTOutputStream(new BufferedOutputStream(gzip));
         return new FastSchematicWriterV3(nbtStream);
      }

      @Override
      public boolean isFormat(final InputStream inputStream) {
         try (
            DataInputStream stream = new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
            NBTInputStream nbt = new NBTInputStream(stream);
         ) {
            if (stream.readByte() != 10) {
               return false;
            } else {
               stream.skipNBytes(2L);
               if (stream.readByte() != 10) {
                  return false;
               } else {
                  stream.skipNBytes(2L);
                  stream.skipNBytes(9L);

                  while (true) {
                     byte type = stream.readByte();
                     String name = stream.readUTF();
                     if (type == 0) {
                        return false;
                     }

                     if (type == 3 && name.equals("Version")) {
                        return stream.readInt() == 3;
                     }

                     nbt.readTagPayloadLazy(type, 0);
                  }
               }
            }
         } catch (IOException var11) {
            return false;
         }
      }

      @Override
      public boolean isFormat(File file) {
         String name = file.getName().toLowerCase(Locale.ROOT);
         return !name.endsWith(".schematic") && !name.endsWith(".mcedit") && !name.endsWith(".mce") ? super.isFormat(file) : false;
      }

      @Override
      public String getPrimaryFileExtension() {
         return "schem";
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Set.of("schem3", "sponge3", "fast3");
      }
   },
   FAST_V2("fast.2", "fawe.2", "schem.2") {
      @Override
      public String getPrimaryFileExtension() {
         return "schem";
      }

      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         if (inputStream instanceof FileInputStream) {
            inputStream = new ResettableFileInputStream((FileInputStream)inputStream);
         }

         BufferedInputStream buffered = new BufferedInputStream(inputStream);
         NBTInputStream nbtStream = new NBTInputStream(new BufferedInputStream(new GZIPInputStream(buffered)));
         return new FastSchematicReaderV2(nbtStream);
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         OutputStream gzip;
         if (!(outputStream instanceof ParallelGZIPOutputStream) && !(outputStream instanceof GZIPOutputStream)) {
            OutputStream var4 = new BufferedOutputStream(outputStream);
            gzip = new ParallelGZIPOutputStream(var4);
         } else {
            gzip = outputStream;
         }

         NBTOutputStream nbtStream = new NBTOutputStream(new BufferedOutputStream(gzip));
         return new FastSchematicWriterV2(nbtStream);
      }

      @Override
      public boolean isFormat(InputStream inputStream) {
         return BuiltInClipboardFormat.detectOldSpongeSchematic(inputStream, 2);
      }

      @Override
      public boolean isFormat(File file) {
         String name = file.getName().toLowerCase(Locale.ROOT);
         return !name.endsWith(".schematic") && !name.endsWith(".mcedit") && !name.endsWith(".mce") ? super.isFormat(file) : false;
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Set.of("schem2", "sponge2", "fast2");
      }
   },
   MCEDIT_SCHEMATIC("mcedit", "mce", "schematic") {
      @Override
      public String getPrimaryFileExtension() {
         return "schematic";
      }

      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(inputStream));
         return new MCEditSchematicReader(nbtStream);
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         throw new IOException(
            "The formats `.schematic`, `.mcedit` and `.mce` are discontinued on versions newer than 1.12 and superseded by the sponge schematic implementation known for `.schem` files."
         );
      }

      @Override
      public boolean isFormat(File file) {
         String name = file.getName().toLowerCase(Locale.ROOT);
         return !name.endsWith(".schematic") && !name.endsWith(".mcedit") && !name.endsWith(".mce") ? false : super.isFormat(file);
      }

      @Override
      public boolean isFormat(InputStream inputStream) {
         LinRootEntry rootEntry;
         try {
            DataInputStream stream = new DataInputStream(new GZIPInputStream(inputStream));
            rootEntry = LinBinaryIO.readUsing(stream, BuiltInClipboardFormat.LEGACY_OPTIONS, LinRootEntry::readFrom);
         } catch (Exception var4) {
            return false;
         }

         return !rootEntry.name().equals("Schematic") ? false : rootEntry.value().value().containsKey("Materials");
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Set.of("mcedit", "schem1", "sponge1", "fast1");
      }
   },
   SPONGE_V1_SCHEMATIC("sponge.1") {
      @Override
      public String getPrimaryFileExtension() {
         return "schem";
      }

      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         return new SpongeSchematicV1Reader(LinBinaryIO.read(new DataInputStream(new GZIPInputStream(inputStream)), BuiltInClipboardFormat.LEGACY_OPTIONS));
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         throw new IOException("This format does not support saving");
      }

      @Override
      public boolean isFormat(InputStream inputStream) {
         return BuiltInClipboardFormat.detectOldSpongeSchematic(inputStream, 1);
      }

      @Override
      public boolean isFormat(File file) {
         String name = file.getName().toLowerCase(Locale.ROOT);
         return !name.endsWith(".mcedit") && !name.endsWith(".mce") ? super.isFormat(file) : false;
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Collections.emptySet();
      }
   },
   @Deprecated
   SPONGE_V2_SCHEMATIC("slow.2", "safe.2", "sponge.2") {
      @Override
      public String getPrimaryFileExtension() {
         return "schem";
      }

      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         return new SpongeSchematicV2Reader(LinBinaryIO.read(new DataInputStream(new GZIPInputStream(inputStream)), BuiltInClipboardFormat.LEGACY_OPTIONS));
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         return new SpongeSchematicV2Writer(new DataOutputStream(new GZIPOutputStream(outputStream)));
      }

      @Override
      public boolean isFormat(InputStream inputStream) {
         return BuiltInClipboardFormat.detectOldSpongeSchematic(inputStream, 2);
      }

      @Override
      public boolean isFormat(File file) {
         return FAST_V2.isFormat(file);
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Collections.emptySet();
      }
   },
   SPONGE_V3_SCHEMATIC("sponge.3", "slow", "safe") {
      @Override
      public String getPrimaryFileExtension() {
         return "schem";
      }

      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         return new SpongeSchematicV3Reader(LinBinaryIO.read(new DataInputStream(new GZIPInputStream(inputStream))));
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         return new SpongeSchematicV3Writer(new DataOutputStream(new GZIPOutputStream(outputStream)));
      }

      @Override
      public boolean isFormat(File file) {
         return FAST_V3.isFormat(file);
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Collections.emptySet();
      }
   },
   BROKENENTITY("brokenentity", "legacyentity", "le", "be", "brokenentities", "legacyentities") {
      @Override
      public String getPrimaryFileExtension() {
         return "schem";
      }

      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         if (inputStream instanceof FileInputStream) {
            inputStream = new ResettableFileInputStream((FileInputStream)inputStream);
         }

         BufferedInputStream buffered = new BufferedInputStream(inputStream);
         NBTInputStream nbtStream = new NBTInputStream(new BufferedInputStream(new GZIPInputStream(buffered)));
         FastSchematicReaderV2 reader = new FastSchematicReaderV2(nbtStream);
         reader.setBrokenEntities(true);
         return reader;
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         OutputStream gzip;
         if (!(outputStream instanceof ParallelGZIPOutputStream) && !(outputStream instanceof GZIPOutputStream)) {
            OutputStream var5 = new BufferedOutputStream(outputStream);
            gzip = new ParallelGZIPOutputStream(var5);
         } else {
            gzip = outputStream;
         }

         NBTOutputStream nbtStream = new NBTOutputStream(new BufferedOutputStream(gzip));
         FastSchematicWriterV2 writer = new FastSchematicWriterV2(nbtStream);
         writer.setBrokenEntities(true);
         return writer;
      }

      @Override
      public boolean isFormat(File file) {
         return false;
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Collections.emptySet();
      }
   },
   MINECRAFT_STRUCTURE("structure") {
      @Override
      public String getPrimaryFileExtension() {
         return "nbt";
      }

      @Override
      public ClipboardReader getReader(InputStream inputStream) throws IOException {
         InputStream var2 = new BufferedInputStream(inputStream);
         return new MinecraftStructure(new DataInputStream(new GZIPInputStream(var2)));
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         OutputStream var3 = new BufferedOutputStream(outputStream);
         OutputStream gzip = new ParallelGZIPOutputStream(var3);
         return new MinecraftStructure(new DataOutputStream(new BufferedOutputStream(gzip)));
      }

      @Override
      public boolean isFormat(InputStream inputStream) {
         try (
            DataInputStream stream = new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
            NBTInputStream nbt = new NBTInputStream(stream);
         ) {
            if (stream.readByte() != 10) {
               return false;
            } else {
               NamedTag namedTag = nbt.readNamedTag();
               if (!namedTag.getName().isEmpty()) {
                  return false;
               } else {
                  while (true) {
                     byte type = stream.readByte();
                     String name = stream.readUTF();
                     if (type == 0) {
                        return false;
                     }

                     if (type == 9 && name.equals("size")) {
                        return true;
                     }

                     nbt.readTagPayloadLazy(type, 0);
                  }
               }
            }
         } catch (IOException var12) {
            return false;
         }
      }

      @Override
      public boolean isFormat(final File file) {
         return file.getName().toLowerCase(Locale.ROOT).endsWith(".nbt") && super.isFormat(file);
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Set.of("nbt");
      }
   },
   PNG("png", "image") {
      @Override
      public ClipboardReader getReader(InputStream inputStream) {
         return null;
      }

      @Override
      public ClipboardWriter getWriter(OutputStream outputStream) throws IOException {
         return new PNGWriter(new BufferedOutputStream(outputStream));
      }

      @Override
      public boolean isFormat(File file) {
         return file.getName().endsWith(".png");
      }

      @Override
      public String getPrimaryFileExtension() {
         return "png";
      }

      @Override
      public Set<String> getExplicitFileExtensions() {
         return Set.of("png");
      }
   };

   @Deprecated
   public static final BuiltInClipboardFormat SPONGE_SCHEMATIC = SPONGE_V2_SCHEMATIC;
   @Deprecated
   public static final BuiltInClipboardFormat FAST = FAST_V2;
   private static final LinReadOptions LEGACY_OPTIONS = LinReadOptions.builder().allowNormalUtf8Encoding(true).build();
   private final ImmutableSet<String> aliases;

   private static boolean detectOldSpongeSchematic(InputStream inputStream, int version) {
      try (
         DataInputStream stream = new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
         NBTInputStream nbt = new NBTInputStream(stream);
      ) {
         if (stream.readByte() != 10) {
            return false;
         } else {
            stream.skipNBytes(2L);
            stream.skipNBytes(9L);

            while (true) {
               byte type = stream.readByte();
               String name = stream.readUTF();
               if (type == 0) {
                  return false;
               }

               if (type == 3 && name.equals("Version")) {
                  return stream.readInt() == version;
               }

               nbt.readTagPayloadLazy(type, 0);
            }
         }
      } catch (IOException var11) {
         return false;
      }
   }

   private BuiltInClipboardFormat(String... aliases) {
      this.aliases = ImmutableSet.copyOf(aliases);
   }

   @Override
   public String getName() {
      return this.name();
   }

   @Override
   public Set<String> getAliases() {
      return this.aliases;
   }

   @Override
   public Set<String> getFileExtensions() {
      return ImmutableSet.of(this.getPrimaryFileExtension());
   }
}
