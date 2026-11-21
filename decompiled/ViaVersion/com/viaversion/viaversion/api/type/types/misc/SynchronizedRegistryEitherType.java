package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.EitherType;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;

public final class SynchronizedRegistryEitherType extends EitherType<Integer, String> {
   private final RegistryKey registryKey;

   public SynchronizedRegistryEitherType(RegistryKey registryKey) {
      super(Types.VAR_INT, Types.STRING);
      this.registryKey = registryKey;
   }

   public void write(Ops ops, Either<Integer, String> value) {
      if (value.isLeft()) {
         Key key = ops.context().registryAccess().registryKey(this.registryKey.registry().toString(), value.left());
         Types.IDENTIFIER.write(ops, key);
      } else {
         Types.IDENTIFIER.write(ops, Key.of(value.right()));
      }
   }
}
