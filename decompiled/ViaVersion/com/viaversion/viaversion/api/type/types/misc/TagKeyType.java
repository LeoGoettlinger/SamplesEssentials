package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;

public class TagKeyType extends KeyType {
   @Override
   public void write(Ops ops, Key value) {
      Types.STRING.write(ops, "#" + value.toString());
   }
}
