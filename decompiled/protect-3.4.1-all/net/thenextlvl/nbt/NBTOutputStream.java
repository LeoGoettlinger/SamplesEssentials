package net.thenextlvl.nbt;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface NBTOutputStream extends DataOutput, Closeable permits NBTOutputStreamImpl {
   @Contract(
      pure = true
   )
   Charset getCharset();

   @Contract(
      mutates = "this,io"
   )
   void writeTag(@Nullable String var1, Tag var2) throws IOException, IllegalArgumentException;

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static NBTOutputStream create(OutputStream output) throws IOException {
      return create(output, StandardCharsets.UTF_8);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTOutputStream create(OutputStream output, Charset charset) throws IOException {
      return create(output, charset, Compression.GZIP);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTOutputStream create(OutputStream output, Compression compression) throws IOException {
      return create(output, StandardCharsets.UTF_8, compression);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static NBTOutputStream create(OutputStream output, Charset charset, Compression compression) throws IOException {
      return new NBTOutputStreamImpl(output, charset, compression);
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static NBTOutputStream create(Path path) throws IOException {
      return create(path, StandardCharsets.UTF_8, Compression.GZIP);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTOutputStream create(Path path, Charset charset) throws IOException {
      return create(path, charset, Compression.GZIP);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static NBTOutputStream create(Path path, Compression compression) throws IOException {
      return create(path, StandardCharsets.UTF_8, compression);
   }

   @Contract(
      value = "_, _, _ -> new",
      pure = true
   )
   static NBTOutputStream create(Path path, Charset charset, Compression compression) throws IOException {
      return create(Files.newOutputStream(path), charset, compression);
   }
}
