package com.viaversion.viaversion.api.data;

import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FullIdentityMappings extends FullMappingsBase {
   public FullIdentityMappings(MappingDataLoader.IdentifiersPair identifiersPair, Mappings mappings) {
      this(toInverseMap(identifiersPair.unmapped()), identifiersPair.unmapped().stream().map(Key::of).toArray(Key[]::new), mappings);
   }

   private FullIdentityMappings(Object2IntMap<String> stringToId, Key[] idToKey, Mappings mappings) {
      super(stringToId, stringToId, idToKey, idToKey, mappings);
   }

   @Nullable
   @Override
   public String identifier(String mappedIdentifier) {
      return this.mappedIdentifier(mappedIdentifier);
   }

   @Nullable
   @Override
   public Key keyFromMappedKey(String mappedIdentifier) {
      return this.mappedKeyFromKey(mappedIdentifier);
   }

   @Override
   public boolean isIdentity() {
      return true;
   }

   @Override
   public boolean isIntIdIdentity() {
      return true;
   }

   public FullIdentityMappings inverse() {
      return new FullIdentityMappings(this.stringToId, this.idToKey, this.mappings.inverse());
   }
}
