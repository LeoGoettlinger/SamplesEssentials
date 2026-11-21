package com.viaversion.viaversion.api.data;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface Mappings {
   int getNewId(int var1);

   default int getNewIdOrDefault(int id, int def) {
      int mappedId = this.getNewId(id);
      return mappedId != -1 ? mappedId : def;
   }

   default boolean contains(int id) {
      return this.getNewId(id) != -1;
   }

   void setNewId(int var1, int var2);

   int size();

   int mappedSize();

   Mappings inverse();

   default boolean isIdentity() {
      return false;
   }

   static boolean isIntIdIdentity(@Nullable Mappings mappings) {
      if (mappings == null) {
         return true;
      } else {
         return mappings instanceof FullMappings fullMappings ? fullMappings.isIntIdIdentity() : mappings.isIdentity();
      }
   }

   static boolean isFullIdentity(@Nullable Mappings mappings) {
      return mappings == null || mappings.isIdentity();
   }
}
