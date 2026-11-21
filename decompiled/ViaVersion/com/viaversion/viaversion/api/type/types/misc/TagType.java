package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.io.TagRegistry;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.io.FastByteBufInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.Map.Entry;

public class TagType extends Type<Tag> {
   static final boolean USE_JAVA_DATA_IO = Runtime.version().feature() >= 24;
   private final int maxBytes;

   public TagType() {
      this(true);
   }

   public TagType(boolean limitBytes) {
      super(Tag.class);
      this.maxBytes = limitBytes ? 2097152 : Integer.MAX_VALUE;
   }

   public Tag read(ByteBuf buffer) {
      byte id = buffer.readByte();
      if (id == 0) {
         return null;
      } else {
         TagLimiter tagLimiter = TagLimiter.create(this.maxBytes, 512);

         try {
            DataInput in = (DataInput)(USE_JAVA_DATA_IO ? new ByteBufInputStream(buffer) : new FastByteBufInputStream(buffer));
            return TagRegistry.read(id, in, tagLimiter, 0);
         } catch (IOException var5) {
            if (Via.getManager().isDebug()) {
               throw new RuntimeException(var5);
            } else {
               throw new RuntimeException("Error reading tag :" + var5.getMessage());
            }
         }
      }
   }

   public void write(ByteBuf buffer, Tag tag) {
      try {
         NamedCompoundTagType.write(buffer, tag, null);
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }
   }

   public void write(Ops ops, Tag value) {
      if (value == null) {
         throw new IllegalArgumentException("Cannot write null tag");
      } else {
         if (value instanceof StringTag stringTag) {
            ops.writeString(stringTag.getValue());
         } else if (value instanceof NumberTag) {
            if (value instanceof IntTag intTag) {
               ops.writeInt(intTag.asInt());
            } else if (value instanceof ByteTag byteTag) {
               ops.writeByte(byteTag.asByte());
            } else if (value instanceof FloatTag floatTag) {
               ops.writeFloat(floatTag.asFloat());
            } else if (value instanceof DoubleTag doubleTag) {
               ops.writeDouble(doubleTag.asDouble());
            } else if (value instanceof ShortTag shortTag) {
               ops.writeShort(shortTag.asShort());
            } else if (value instanceof LongTag longTag) {
               ops.writeLong(longTag.asLong());
            }
         } else if (value instanceof CompoundTag compoundTag) {
            ops.writeMap(map -> {
               for (Entry<String, Tag> entry : compoundTag.entrySet()) {
                  map.write(entry.getKey(), this, entry.getValue());
               }
            });
         } else if (value instanceof ListTag<?> listTag) {
            ops.writeList(list -> {
               for (Tag tag : listTag) {
                  list.write(this, tag);
               }
            });
         } else if (value instanceof IntArrayTag intArrayTag) {
            ops.writeInts(intArrayTag.getValue());
         } else if (value instanceof ByteArrayTag byteArrayTag) {
            ops.writeBytes(byteArrayTag.getValue());
         } else {
            if (!(value instanceof LongArrayTag longArrayTag)) {
               throw new IllegalArgumentException("Unknown tag type: " + value);
            }

            ops.writeLongs(longArrayTag.getValue());
         }
      }
   }

   public static final class OptionalTagType extends OptionalType<Tag> {
      private OptionalTagType(Type<Tag> tagType) {
         super(tagType);
      }

      public static TagType.OptionalTagType type() {
         return new TagType.OptionalTagType(Types.TAG);
      }

      public static TagType.OptionalTagType trustedType() {
         return new TagType.OptionalTagType(Types.TRUSTED_TAG);
      }
   }
}
