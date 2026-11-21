package com.viaversion.viaversion.api.type.types.item;

import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface StructuredDataTypeBase {
   StructuredData<?> read(ByteBuf var1);

   void write(ByteBuf var1, StructuredData<?> var2);

   @Nullable
   StructuredDataKey<?> key(int var1);
}
