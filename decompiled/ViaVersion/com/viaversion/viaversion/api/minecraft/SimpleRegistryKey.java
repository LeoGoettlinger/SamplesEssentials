package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.util.Key;

record SimpleRegistryKey(Key registry) implements RegistryKey {
   @Override
   public String toString() {
      return this.registry.toString();
   }
}
