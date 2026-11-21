package net.thenextlvl.nbt;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.EscapeTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

final class NBTInputStreamImpl extends DataInputStream implements NBTInputStream {
   private final Charset charset;
   private final Map<Byte, NBTInputStream.ReadingFunction> readers = new HashMap<>(
      Map.ofEntries(
         Map.entry((byte)7, TagReaders::readByteArray),
         Map.entry((byte)1, TagReaders::readByte),
         Map.entry((byte)10, input -> TagReaders.readCompound((NBTInputStreamImpl)input)),
         Map.entry((byte)6, TagReaders::readDouble),
         Map.entry((byte)0, ignored -> EscapeTag.INSTANCE),
         Map.entry((byte)5, TagReaders::readFloat),
         Map.entry((byte)11, TagReaders::readIntArray),
         Map.entry((byte)3, TagReaders::readInt),
         Map.entry((byte)9, input -> TagReaders.readList((NBTInputStreamImpl)input)),
         Map.entry((byte)12, TagReaders::readLongArray),
         Map.entry((byte)4, TagReaders::readLong),
         Map.entry((byte)2, TagReaders::readShort),
         Map.entry((byte)8, TagReaders::readString)
      )
   );

   public NBTInputStreamImpl(InputStream inputStream, Charset charset, Compression compression) throws IOException {
      super(new DataInputStream(new BufferedInputStream(compression.decompress(inputStream))));
      this.charset = charset;
   }

   @Override
   public CompoundTag readTag() throws IOException {
      byte type = this.readByte();
      if (type != 10) {
         throw new IllegalArgumentException("Root tag must be a CompoundTag");
      } else {
         this.skipNBytes(this.readShort());
         return TagReaders.readCompound(this);
      }
   }

   @Override
   public Entry<String, CompoundTag> readNamedTag() throws IOException, IllegalArgumentException {
      if (this.readByte() != 10) {
         throw new IllegalArgumentException("Root tag must be a CompoundTag");
      } else {
         return Map.entry(this.readName(), TagReaders.readCompound(this));
      }
   }

   @CheckReturnValue
   @Contract(
      mutates = "this"
   )
   @Nullable
   public Entry<String, Tag> readNamedTagInternal() throws IOException, IllegalArgumentException {
      byte type = this.readByte();
      return type == 0 ? null : Map.entry(this.readName(), this.readTag(type));
   }

   private String readName() throws IOException {
      short nameLength = this.readShort();
      byte[] bytes = new byte[nameLength];
      if (nameLength > 0) {
         this.readFully(bytes);
      }

      return new String(bytes, this.getCharset());
   }

   @CheckReturnValue
   @Contract(
      value = "_ -> new",
      mutates = "this"
   )
   public Tag readTag(byte type) throws IOException {
      NBTInputStream.ReadingFunction mapping = this.readers.get(type);
      if (mapping != null) {
         return mapping.read(this);
      } else {
         throw new IllegalArgumentException("Unknown tag type: " + type);
      }
   }

   @Override
   public Charset getCharset() {
      return this.charset;
   }

   @Override
   public void registerReader(byte typeId, NBTInputStream.ReadingFunction function) throws IllegalArgumentException {
      if (!this.readers.containsKey(typeId)) {
         this.readers.put(typeId, function);
      } else {
         throw new IllegalArgumentException("Reader for type " + typeId + " is already registered");
      }
   }

   @Override
   public boolean unregisterReader(byte typeId) {
      return this.readers.remove(typeId) != null;
   }
}
