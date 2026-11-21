package com.viaversion.viaversion.api.data;

import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LegacyFullStringMappingsBase extends FullMappingsBase {
   private final String[] idToString;
   private final String[] mappedIdToString;

   public LegacyFullStringMappingsBase(MappingDataLoader.IdentifiersPair identifiersPair, Mappings mappings) {
      super(toInverseMap(identifiersPair.unmapped()), toInverseMap(identifiersPair.mapped()), new Key[0], new Key[0], mappings);
      this.idToString = identifiersPair.unmapped().toArray(new String[0]);
      this.mappedIdToString = identifiersPair.mapped().toArray(new String[0]);
   }

   private LegacyFullStringMappingsBase(
      Object2IntMap<String> stringToId, Object2IntMap<String> mappedStringToId, String[] idToKey, String[] mappedIdToString, Mappings mappings
   ) {
      super(stringToId, mappedStringToId, new Key[0], new Key[0], mappings);
      this.idToString = idToKey;
      this.mappedIdToString = mappedIdToString;
   }

   @Override
   public String identifier(int id) {
      return id >= 0 && id < this.idToString.length ? Key.namespaced(this.idToString[id]) : null;
   }

   @Nullable
   @Override
   public String identifier(String mappedIdentifier) {
      int mappedId = this.mappedId(mappedIdentifier);
      if (mappedId == -1) {
         return null;
      } else {
         int id = this.mappings.inverse().getNewId(mappedId);
         return id != -1 ? this.identifier(id) : null;
      }
   }

   @Nullable
   @Override
   public String mappedIdentifier(int mappedId) {
      return mappedId >= 0 && mappedId < this.mappedIdToString.length ? Key.namespaced(this.mappedIdToString[mappedId]) : null;
   }

   @Nullable
   @Override
   public String mappedIdentifier(String identifier) {
      int id = this.id(identifier);
      if (id == -1) {
         return null;
      } else {
         int mappedId = this.mappings.getNewId(id);
         return mappedId != -1 ? this.mappedIdentifier(mappedId) : null;
      }
   }

   @Nullable
   @Override
   public Key keyFromId(int id) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public Key keyFromMappedKey(String mappedIdentifier) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public Key mappedKeyFromMappedId(int mappedId) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public Key mappedKeyFromKey(String identifier) {
      throw new UnsupportedOperationException();
   }

   @Override
   public FullMappings inverse() {
      return new LegacyFullStringMappingsBase(this.mappedStringToId, this.stringToId, this.mappedIdToString, this.idToString, this.mappings.inverse());
   }
}
