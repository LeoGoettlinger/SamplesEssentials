package com.viaversion.viaversion.api.type;

import com.viaversion.viaversion.api.minecraft.codec.Ops;

@FunctionalInterface
public interface CodecWriter<T> {
   void write(Ops var1, T var2);
}
