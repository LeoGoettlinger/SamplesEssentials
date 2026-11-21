package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.util.Key;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum RegistryType implements RegistryKey {
   BLOCK("block"),
   ITEM("item"),
   FLUID("fluid"),
   ENTITY("entity_type"),
   GAME_EVENT("game_event"),
   ENCHANTMENT("enchantment"),
   DAMAGE_TYPE("damage_type"),
   BANNER_PATTERN("banner_pattern");

   private static final Map<String, RegistryType> MAP = new HashMap<>();
   private static final RegistryType[] VALUES = values();
   private final Key key;

   public static RegistryType[] getValues() {
      return VALUES;
   }

   @Nullable
   public static RegistryType getByKey(String identifier) {
      return MAP.get(identifier);
   }

   private RegistryType(String identifier) {
      this.key = Key.of(identifier);
   }

   public String identifier() {
      return this.key.path();
   }

   @Override
   public Key registry() {
      return this.key;
   }

   static {
      for (RegistryType type : getValues()) {
         MAP.put(type.identifier(), type);
      }
   }
}
