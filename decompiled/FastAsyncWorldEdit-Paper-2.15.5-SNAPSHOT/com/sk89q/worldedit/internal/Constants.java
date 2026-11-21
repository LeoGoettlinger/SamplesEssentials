package com.sk89q.worldedit.internal;

import java.util.List;

public final class Constants {
   public static final List<String> NO_COPY_ENTITY_NBT_FIELDS = List.of(
      "UUIDLeast", "UUIDMost", "UUID", "WorldUUIDLeast", "WorldUUIDMost", "PersistentIDMSB", "PersistentIDLSB"
   );
   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static final int DATA_VERSION_MC_1_13 = 1519;
   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static final int DATA_VERSION_MC_1_13_2 = 1631;
   public static final int DATA_VERSION_MC_1_15 = 2225;
   public static final int DATA_VERSION_MC_1_16 = 2566;
   public static final int DATA_VERSION_MC_1_17 = 2724;
   public static final int DATA_VERSION_MC_1_18 = 2860;
   public static final int DATA_VERSION_MC_1_19 = 3105;
   public static final int DATA_VERSION_MC_1_20 = 3463;
   public static final int DATA_VERSION_MC_1_21 = 3953;
   public static final int DATA_VERSION_MC_1_21_2 = 4080;
   public static final int DATA_VERSION_MC_1_21_3 = 4082;
   public static final int DATA_VERSION_MC_1_21_4 = 4189;
   public static final int DATA_VERSION_MC_1_21_5 = 4325;
   public static final int DATA_VERSION_MC_1_21_6 = 4435;
   public static final int DATA_VERSION_MC_1_21_7 = 4438;
   public static final int DATA_VERSION_MC_1_21_8 = 4440;
   public static final int DATA_VERSION_MC_1_21_9 = 4554;
   public static final int DATA_VERSION_MC_1_21_10 = 4556;
   public static final int DATA_VERSION_MC_1_21_11 = 4671;
   public static final int DATA_VERSION_MC_26_1 = 4786;
   public static final int DATA_VERSION_MC_26_1_1 = 4788;
   public static final int DATA_VERSION_MC_26_1_2 = 4790;
   public static final int DATA_VERSION_MC_26_2 = 4903;

   private Constants() {
   }
}
