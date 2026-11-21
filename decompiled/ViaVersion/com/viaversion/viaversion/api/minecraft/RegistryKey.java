package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.util.Key;

public interface RegistryKey {
   Key registry();

   static RegistryKey of(String key) {
      return new SimpleRegistryKey(Key.of(key));
   }
}
