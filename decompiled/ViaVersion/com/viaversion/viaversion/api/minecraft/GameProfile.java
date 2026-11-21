package com.viaversion.viaversion.api.minecraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public record GameProfile(@Nullable String name, @Nullable UUID id, GameProfile.Property[] properties, boolean dynamic) {
   public GameProfile(@Nullable String name, @Nullable UUID id, GameProfile.Property[] properties) {
      this(name, id, properties, true);
   }

   public GameProfile(@Nullable String name, @Nullable UUID id) {
      this(name, id, new GameProfile.Property[0]);
   }

   public Map<String, List<GameProfile.Property>> propertiesMap() {
      Map<String, List<GameProfile.Property>> map = new HashMap<>();

      for (GameProfile.Property property : this.properties) {
         map.computeIfAbsent(property.name(), k -> new ArrayList<>()).add(property);
      }

      return map;
   }

   public record Property(String name, String value, @Nullable String signature) {
      public Property(String name, String value) {
         this(name, value, null);
      }
   }
}
