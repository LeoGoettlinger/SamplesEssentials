package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.VarIntType;
import com.viaversion.viaversion.util.Key;

public final class RegistryValueType extends VarIntType implements RegistryKey {
   private final Key registry;
   private final Key[] keys;

   public RegistryValueType(Key registry, String... keys) {
      this.registry = registry;
      this.keys = new Key[keys.length];

      for (int i = 0; i < keys.length; i++) {
         this.keys[i] = Key.of(keys[i]);
      }
   }

   @Override
   public void write(Ops ops, Integer value) {
      Types.IDENTIFIER.write(ops, this.keys[value]);
   }

   public Key[] keys() {
      return this.keys;
   }

   public Key byId(int id) {
      return this.keys[id];
   }

   @Override
   public Key registry() {
      return this.registry;
   }
}
