package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world;

import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@Deprecated
public enum DimensionType {
   NETHER(-1, "minecraft:the_nether"),
   OVERWORLD(0, "minecraft:overworld"),
   END(1, "minecraft:the_end"),
   CUSTOM(-999, "minecraft:custom");

   private static final DimensionType[] VALUES = values();
   private final int id;
   private final String name;

   private DimensionType(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public static DimensionType getById(int id) {
      return VALUES[id + 1];
   }

   @Nullable
   public static DimensionType getByName(String name) {
      for (DimensionType type : VALUES) {
         if (type.name.equals(name)) {
            return type;
         }
      }

      return null;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static boolean isFlat(String levelType) {
      return WorldType.FLAT.getName().equals(levelType);
   }

   public static boolean isDebug(String levelType) {
      return WorldType.DEBUG_ALL_BLOCK_STATES.getName().equals(levelType);
   }
}
