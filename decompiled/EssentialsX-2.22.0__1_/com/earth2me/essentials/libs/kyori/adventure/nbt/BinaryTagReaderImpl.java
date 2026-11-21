package com.earth2me.essentials.libs.kyori.adventure.nbt;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

final class BinaryTagReaderImpl implements BinaryTagIO.Reader {
   private final long maxBytes;
   static final BinaryTagIO.Reader UNLIMITED = new BinaryTagReaderImpl(-1L);
   static final BinaryTagIO.Reader DEFAULT_LIMIT = new BinaryTagReaderImpl(131082L);

   BinaryTagReaderImpl(final long maxBytes) {
      this.maxBytes = maxBytes;
   }

   @NotNull
   @Override
   public CompoundBinaryTag read(@NotNull final Path path, @NotNull final BinaryTagIO.Compression compression) throws IOException {
      InputStream is = Files.newInputStream(path);

      CompoundBinaryTag var4;
      try {
         var4 = this.read(is, compression);
      } catch (Throwable var7) {
         if (is != null) {
            try {
               is.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (is != null) {
         is.close();
      }

      return var4;
   }

   @NotNull
   @Override
   public CompoundBinaryTag read(@NotNull final InputStream input, @NotNull final BinaryTagIO.Compression compression) throws IOException {
      DataInputStream dis = new DataInputStream(new BufferedInputStream(compression.decompress(IOStreamUtil.closeShield(input))));

      CompoundBinaryTag var4;
      try {
         var4 = this.read(dis);
      } catch (Throwable var7) {
         try {
            dis.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      dis.close();
      return var4;
   }

   @NotNull
   @Override
   public CompoundBinaryTag read(@NotNull final DataInput input) throws IOException {
      return this.read(input, true);
   }

   @NotNull
   private CompoundBinaryTag read(@NotNull DataInput input, final boolean named) throws IOException {
      if (!(input instanceof TrackingDataInput)) {
         input = new TrackingDataInput(input, this.maxBytes);
      }

      BinaryTagType<? extends BinaryTag> type = BinaryTagType.binaryTagType(input.readByte());
      requireCompound(type);
      if (named) {
         input.skipBytes(input.readUnsignedShort());
      }

      return BinaryTagTypes.COMPOUND.read(input);
   }

   @NotNull
   @Override
   public CompoundBinaryTag readNameless(@NotNull final Path path, @NotNull final BinaryTagIO.Compression compression) throws IOException {
      InputStream is = Files.newInputStream(path);

      CompoundBinaryTag var4;
      try {
         var4 = this.readNameless(is, compression);
      } catch (Throwable var7) {
         if (is != null) {
            try {
               is.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (is != null) {
         is.close();
      }

      return var4;
   }

   @NotNull
   @Override
   public CompoundBinaryTag readNameless(@NotNull final InputStream input, @NotNull final BinaryTagIO.Compression compression) throws IOException {
      DataInputStream dis = new DataInputStream(new BufferedInputStream(compression.decompress(IOStreamUtil.closeShield(input))));

      CompoundBinaryTag var4;
      try {
         var4 = this.readNameless(dis);
      } catch (Throwable var7) {
         try {
            dis.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      dis.close();
      return var4;
   }

   @NotNull
   @Override
   public CompoundBinaryTag readNameless(@NotNull final DataInput input) throws IOException {
      return this.read(input, false);
   }

   @NotNull
   @Override
   public Entry<String, CompoundBinaryTag> readNamed(@NotNull final Path path, @NotNull final BinaryTagIO.Compression compression) throws IOException {
      InputStream is = Files.newInputStream(path);

      Entry var4;
      try {
         var4 = this.readNamed(is, compression);
      } catch (Throwable var7) {
         if (is != null) {
            try {
               is.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (is != null) {
         is.close();
      }

      return var4;
   }

   @NotNull
   @Override
   public Entry<String, CompoundBinaryTag> readNamed(@NotNull final InputStream input, @NotNull final BinaryTagIO.Compression compression) throws IOException {
      DataInputStream dis = new DataInputStream(new BufferedInputStream(compression.decompress(IOStreamUtil.closeShield(input))));

      Entry var4;
      try {
         var4 = this.readNamed(dis);
      } catch (Throwable var7) {
         try {
            dis.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      dis.close();
      return var4;
   }

   @NotNull
   @Override
   public Entry<String, CompoundBinaryTag> readNamed(@NotNull final DataInput input) throws IOException {
      BinaryTagType<? extends BinaryTag> type = BinaryTagType.binaryTagType(input.readByte());
      requireCompound(type);
      String name = input.readUTF();
      return new SimpleImmutableEntry<>(name, BinaryTagTypes.COMPOUND.read(input));
   }

   private static void requireCompound(final BinaryTagType<? extends BinaryTag> type) throws IOException {
      if (type != BinaryTagTypes.COMPOUND) {
         throw new IOException(String.format("Expected root tag to be a %s, was %s", BinaryTagTypes.COMPOUND, type));
      }
   }
}
