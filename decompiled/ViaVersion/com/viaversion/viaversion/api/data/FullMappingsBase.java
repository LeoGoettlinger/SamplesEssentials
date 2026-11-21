package com.viaversion.viaversion.api.data;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.util.Key;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FullMappingsBase implements FullMappings {
   protected final Object2IntMap<String> stringToId;
   protected final Object2IntMap<String> mappedStringToId;
   protected final Key[] idToKey;
   protected final Key[] mappedIdToKey;
   protected final Mappings mappings;

   public FullMappingsBase(MappingDataLoader.IdentifiersPair identifiersPair, Mappings mappings) {
      Preconditions.checkNotNull(mappings, "Mappings cannot be null");
      this.mappings = mappings;
      this.stringToId = toInverseMap(identifiersPair.unmapped());
      this.idToKey = identifiersPair.unmapped().stream().map(Key::of).toArray(Key[]::new);
      this.mappedStringToId = toInverseMap(identifiersPair.mapped());
      this.mappedIdToKey = identifiersPair.mapped().stream().map(Key::of).toArray(Key[]::new);
   }

   protected FullMappingsBase(Object2IntMap<String> stringToId, Object2IntMap<String> mappedStringToId, Key[] idToKey, Key[] mappedIdToKey, Mappings mappings) {
      this.stringToId = stringToId;
      this.mappedStringToId = mappedStringToId;
      this.idToKey = idToKey;
      this.mappedIdToKey = mappedIdToKey;
      this.mappings = mappings;
   }

   public static FullMappings of(MappingDataLoader.IdentifiersPair identifiersPair, Mappings mappings) {
      return (FullMappings)(mappings.isIdentity() && identifiersPair.identity()
         ? new FullIdentityMappings(identifiersPair, mappings)
         : new FullMappingsBase(identifiersPair, mappings));
   }

   static Object2IntMap<String> toInverseMap(List<String> list) {
      Object2IntMap<String> map = new Object2IntOpenHashMap<>(list.size());
      map.defaultReturnValue(-1);

      for (int i = 0; i < list.size(); i++) {
         map.put(list.get(i), i);
      }

      return map;
   }

   @Override
   public int id(String identifier) {
      return this.stringToId.getInt(Key.stripMinecraftNamespace(identifier));
   }

   @Override
   public int mappedId(String mappedIdentifier) {
      return this.mappedStringToId.getInt(Key.stripMinecraftNamespace(mappedIdentifier));
   }

   @Nullable
   @Override
   public Key keyFromId(int id) {
      return id >= 0 && id < this.idToKey.length ? this.idToKey[id] : null;
   }

   @Nullable
   @Override
   public Key keyFromMappedKey(String mappedIdentifier) {
      int mappedId = this.mappedId(mappedIdentifier);
      if (mappedId == -1) {
         return null;
      } else {
         int id = this.mappings.inverse().getNewId(mappedId);
         return id != -1 ? this.keyFromId(id) : null;
      }
   }

   @Nullable
   @Override
   public Key mappedKeyFromMappedId(int mappedId) {
      return mappedId >= 0 && mappedId < this.mappedIdToKey.length ? this.mappedIdToKey[mappedId] : null;
   }

   @Nullable
   @Override
   public Key mappedKeyFromKey(String identifier) {
      int id = this.id(identifier);
      if (id == -1) {
         return null;
      } else {
         int mappedId = this.mappings.getNewId(id);
         return mappedId != -1 ? this.mappedKeyFromMappedId(mappedId) : null;
      }
   }

   @Override
   public int getNewId(int id) {
      return this.mappings.getNewId(id);
   }

   @Override
   public void setNewId(int id, int mappedId) {
      this.mappings.setNewId(id, mappedId);
   }

   @Override
   public int size() {
      return this.mappings.size();
   }

   @Override
   public int mappedSize() {
      return this.mappings.mappedSize();
   }

   @Override
   public boolean isIntIdIdentity() {
      return this.mappings.isIdentity();
   }

   @Override
   public FullMappings inverse() {
      return new FullMappingsBase(this.mappedStringToId, this.stringToId, this.mappedIdToKey, this.idToKey, this.mappings.inverse());
   }
}
