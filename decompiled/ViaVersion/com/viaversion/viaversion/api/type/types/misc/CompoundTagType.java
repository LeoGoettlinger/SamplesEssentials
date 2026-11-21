package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.Map.Entry;

public class CompoundTagType extends Type<CompoundTag> {
   private final int maxBytes;

   public CompoundTagType() {
      this(true);
   }

   public CompoundTagType(boolean limitMaxBytes) {
      super(CompoundTag.class);
      this.maxBytes = limitMaxBytes ? 2097152 : Integer.MAX_VALUE;
   }

   public CompoundTag read(ByteBuf buffer) {
      try {
         return NamedCompoundTagType.read(buffer, this.maxBytes, false);
      } catch (IOException var3) {
         throw new RuntimeException(var3);
      }
   }

   public void write(ByteBuf buffer, CompoundTag object) {
      try {
         NamedCompoundTagType.write(buffer, object, null);
      } catch (IOException var4) {
         throw new RuntimeException(var4);
      }
   }

   public void write(Ops ops, CompoundTag value) {
      ops.writeMap(map -> {
         for (Entry<String, Tag> entry : value.entrySet()) {
            map.write(entry.getKey(), Types.TAG, entry.getValue());
         }
      });
   }

   public static final class OptionalCompoundTagType extends OptionalType<CompoundTag> {
      private OptionalCompoundTagType(Type<CompoundTag> tagType) {
         super(tagType);
      }

      public static CompoundTagType.OptionalCompoundTagType type() {
         return new CompoundTagType.OptionalCompoundTagType(Types.COMPOUND_TAG);
      }

      public static CompoundTagType.OptionalCompoundTagType trustedType() {
         return new CompoundTagType.OptionalCompoundTagType(Types.TRUSTED_COMPOUND_TAG);
      }
   }
}
