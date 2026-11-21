package com.viaversion.viaversion.api.minecraft.codec.hash;

import com.viaversion.viaversion.api.minecraft.codec.Ops;

public interface Hasher extends Ops {
   int hash();

   void reset();
}
