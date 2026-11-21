package net.thenextlvl.nbt;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;

public sealed interface NBTInputStream extends DataInput, Closeable permits NBTInputStreamImpl {
   @Contract(
      pure = true
   )
   Charset getCharset();

   @CheckReturnValue
   @Contract(
      value = " -> new",
      mutates = "this"
   )
   CompoundTag readTag() throws IOException;

   @CheckReturnValue
   @Contract(
      value = " -> new",
      mutates = "this"
   )
   Entry<String, CompoundTag> readNamedTag() throws IOException, IllegalArgumentException;

   @Contract(
      mutates = "this"
   )
   void registerReader(byte var1, NBTInputStream.ReadingFunction var2) throws IllegalArgumentException;

   @Contract(
      mutates = "this"
   )
   boolean unregisterReader(byte var1);

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static NBTInputStream create(InputStream input) throws IOException {
      return create(input, StandardCharsets.UTF_8);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTInputStream create(InputStream input, Charset charset) throws IOException {
      return create(input, charset, Compression.GZIP);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTInputStream create(InputStream input, Compression compression) throws IOException {
      return create(input, StandardCharsets.UTF_8, compression);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static NBTInputStream create(InputStream input, Charset charset, Compression compression) throws IOException {
      return new NBTInputStreamImpl(input, charset, compression);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static NBTInputStream create(Path path) throws IOException {
      return create(path, StandardCharsets.UTF_8, Compression.GZIP);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTInputStream create(Path path, Charset charset) throws IOException {
      return create(path, charset, Compression.GZIP);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTInputStream create(Path path, Compression compression) throws IOException {
      return create(path, StandardCharsets.UTF_8, compression);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static NBTInputStream create(Path path, Charset charset, Compression compression) throws IOException {
      return create(Files.newInputStream(path), charset, compression);
   }

   @FunctionalInterface
   public interface ReadingFunction {
      @Contract(
         value = "_ -> new",
         mutates = "param1"
      )
      Tag read(NBTInputStream var1) throws IOException;
   }
}
