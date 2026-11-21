package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.io.FastByteBufInputStream;
import com.viaversion.viaversion.io.FastByteBufOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NamedCompoundTagType extends Type<CompoundTag> {
   public NamedCompoundTagType() {
      super(CompoundTag.class);
   }

   public CompoundTag read(ByteBuf buffer) {
      try {
         return read(buffer, 2097152, true);
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   public void write(ByteBuf buffer, CompoundTag object) {
      try {
         write(buffer, object, "");
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }
   }

   public static CompoundTag read(ByteBuf buffer, int maxBytes, boolean readName) throws IOException {
      byte id = buffer.readByte();
      if (id == 0) {
         return null;
      } else if (id != 10) {
         throw new IOException(String.format("Expected root tag to be a CompoundTag, was %s", id));
      } else {
         if (readName) {
            buffer.skipBytes(buffer.readUnsignedShort());
         }

         TagLimiter tagLimiter = TagLimiter.create(maxBytes, 512);
         DataInput in = (DataInput)(TagType.USE_JAVA_DATA_IO ? new ByteBufInputStream(buffer) : new FastByteBufInputStream(buffer));
         return CompoundTag.read(in, tagLimiter, 0);
      }
   }

   public static void write(ByteBuf buffer, Tag tag, @Nullable String name) throws IOException {
      if (tag == null) {
         buffer.writeByte(0);
      } else {
         DataOutput out = (DataOutput)(TagType.USE_JAVA_DATA_IO ? new ByteBufOutputStream(buffer) : new FastByteBufOutputStream(buffer));
         out.writeByte(tag.getTagId());
         if (name != null) {
            out.writeUTF(name);
         }

         tag.write(out);
      }
   }

   public static final class OptionalNamedCompoundTagType extends OptionalType<CompoundTag> {
      public OptionalNamedCompoundTagType() {
         super(Types.NAMED_COMPOUND_TAG);
      }
   }
}
