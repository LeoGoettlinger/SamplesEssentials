package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_TropicalFishData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.fish.TropicalFish", "DATA_ID_TYPE_VARIANT"
   );
   public static final int PATTERN_KOB = 0;
   public static final int PATTERN_SUNSTREAK = 1;
   public static final int PATTERN_SNOOPER = 2;
   public static final int PATTERN_DASHER = 3;
   public static final int PATTERN_BRINELY = 4;
   public static final int PATTERN_SPOTTY = 5;
   public static final int PATTERN_FLOPPER = 0;
   public static final int PATTERN_STRIPEY = 1;
   public static final int PATTERN_GLITTER = 2;
   public static final int PATTERN_BLOCKFISH = 3;
   public static final int PATTERN_BETTY = 4;
   public static final int PATTERN_CLAYFISH = 5;
   public static final int SIZE_SMALL = 0;
   public static final int SIZE_LARGE = 1;
   public static final int COLOR_WHITE = 0;
   public static final int COLOR_ORANGE = 1;
   public static final int COLOR_MAGENTA = 2;
   public static final int COLOR_LIGHT_BLUE = 3;
   public static final int COLOR_YELLOW = 4;
   public static final int COLOR_LIME = 5;
   public static final int COLOR_PINK = 6;
   public static final int COLOR_GRAY = 7;
   public static final int COLOR_LIGHT_GRAY = 8;
   public static final int COLOR_CYAN = 9;
   public static final int COLOR_PURPLE = 10;
   public static final int COLOR_BLUE = 11;
   public static final int COLOR_BROWN = 12;
   public static final int COLOR_GREEN = 13;
   public static final int COLOR_RED = 14;
   public static final int COLOR_BLACK = 15;

   public static int createVariantData(int size, int pattern, int bodyColor, int patternColor) {
      return size | pattern << 8 | bodyColor << 16 | patternColor << 24;
   }

   public static int[] getPatternAndSize(String patternName) {
      String var1 = patternName.toUpperCase();

      return switch (var1) {
         case "KOB" -> new int[]{0, 0};
         case "SUNSTREAK" -> new int[]{0, 1};
         case "SNOOPER" -> new int[]{0, 2};
         case "DASHER" -> new int[]{0, 3};
         case "BRINELY" -> new int[]{0, 4};
         case "SPOTTY" -> new int[]{0, 5};
         case "FLOPPER" -> new int[]{1, 0};
         case "STRIPEY" -> new int[]{1, 1};
         case "GLITTER" -> new int[]{1, 2};
         case "BLOCKFISH" -> new int[]{1, 3};
         case "BETTY" -> new int[]{1, 4};
         case "CLAYFISH" -> new int[]{1, 5};
         default -> new int[]{0, 0};
      };
   }
}
