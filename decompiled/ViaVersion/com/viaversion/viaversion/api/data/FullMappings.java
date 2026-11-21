package com.viaversion.viaversion.api.data;

import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface FullMappings extends BiMappings {
   int id(String var1);

   int mappedId(String var1);

   @Nullable
   default String identifier(int id) {
      Key key = this.keyFromId(id);
      return key != null ? key.toString() : null;
   }

   @Nullable
   default String identifier(String mappedIdentifier) {
      Key key = this.keyFromMappedKey(mappedIdentifier);
      return key != null ? key.toString() : null;
   }

   @Nullable
   default String mappedIdentifier(int mappedId) {
      Key key = this.mappedKeyFromMappedId(mappedId);
      return key != null ? key.toString() : null;
   }

   @Nullable
   default String mappedIdentifier(String identifier) {
      Key key = this.mappedKeyFromKey(identifier);
      return key != null ? key.toString() : null;
   }

   @Nullable
   Key keyFromId(int var1);

   @Nullable
   Key keyFromMappedKey(String var1);

   @Nullable
   Key mappedKeyFromMappedId(int var1);

   @Nullable
   Key mappedKeyFromKey(String var1);

   boolean isIntIdIdentity();

   FullMappings inverse();
}
