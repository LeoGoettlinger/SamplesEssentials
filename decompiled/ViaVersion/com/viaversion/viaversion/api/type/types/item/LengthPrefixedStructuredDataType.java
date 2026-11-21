package com.viaversion.viaversion.api.type.types.item;

import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LengthPrefixedStructuredDataType extends Type<StructuredData<?>> implements StructuredDataTypeBase {
   private final StructuredDataType wrapped;

   public LengthPrefixedStructuredDataType(StructuredDataType dataType) {
      super(StructuredData.class);
      this.wrapped = dataType;
   }

   @Override
   public void write(ByteBuf buffer, StructuredData<?> value) {
      Types.VAR_INT.writePrimitive(buffer, value.id());
      ByteBuf tempBuf = buffer.alloc().buffer();

      try {
         value.write(tempBuf);
         Types.VAR_INT.writePrimitive(buffer, tempBuf.readableBytes());
         buffer.writeBytes(tempBuf);
      } finally {
         tempBuf.release();
      }
   }

   @Override
   public StructuredData<?> read(ByteBuf buffer) {
      int id = Types.VAR_INT.readPrimitive(buffer);
      StructuredDataKey<?> key = this.key(id);
      if (key == null) {
         throw new IllegalArgumentException("No data component serializer found for id " + id);
      } else {
         int size = Types.VAR_INT.readPrimitive(buffer);
         ByteBuf slicedBuf = buffer.readSlice(size);
         return this.wrapped.readData(slicedBuf, key, id);
      }
   }

   @Nullable
   @Override
   public StructuredDataKey<?> key(int id) {
      return this.wrapped.key(id);
   }
}
