package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class KeyType extends Type<Key> {
   public KeyType() {
      super(Key.class);
   }

   public Key read(ByteBuf buffer) {
      String identifier = Types.STRING.read(buffer);
      return Key.of(identifier);
   }

   public void write(ByteBuf buffer, Key key) {
      Types.STRING.write(buffer, key.original());
   }

   public void write(Ops ops, Key value) {
      Types.STRING.write(ops, value.toString());
   }

   public static final class OptionalKeyType extends OptionalType<Key> {
      public OptionalKeyType() {
         super(Types.IDENTIFIER);
      }
   }
}
