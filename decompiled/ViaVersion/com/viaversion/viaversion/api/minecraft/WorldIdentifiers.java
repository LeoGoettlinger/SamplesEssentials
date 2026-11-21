package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.connection.StorableObject;

public record WorldIdentifiers(String overworld, String nether, String end) implements StorableObject {
   public static final String OVERWORLD_DEFAULT = "minecraft:overworld";
   public static final String NETHER_DEFAULT = "minecraft:the_nether";
   public static final String END_DEFAULT = "minecraft:the_end";

   public WorldIdentifiers(String overworld) {
      this(overworld, "minecraft:the_nether", "minecraft:the_end");
   }
}
