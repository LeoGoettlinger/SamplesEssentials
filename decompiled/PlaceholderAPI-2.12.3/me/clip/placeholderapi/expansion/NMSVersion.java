package me.clip.placeholderapi.expansion;

public enum NMSVersion {
   UNKNOWN("unknown"),
   SPIGOT_1_7_R1("v1_7_R1"),
   SPIGOT_1_7_R2("v1_7_R2"),
   SPIGOT_1_7_R3("v1_7_R3"),
   SPIGOT_1_7_R4("v1_7_R4"),
   SPIGOT_1_8_R1("v1_8_R1"),
   SPIGOT_1_8_R2("v1_8_R2"),
   SPIGOT_1_8_R3("v1_8_R3"),
   SPIGOT_1_9_R1("v1_9_R1"),
   SPIGOT_1_9_R2("v1_9_R2"),
   SPIGOT_1_10_R1("v1_10_R1"),
   SPIGOT_1_11_R1("v1_11_R1"),
   SPIGOT_1_12_R1("v1_12_R1"),
   SPIGOT_1_13_R1("v1_13_R1"),
   SPIGOT_1_13_R2("v1_13_R2"),
   SPIGOT_1_14_R1("v1_14_R1"),
   SPIGOT_1_15_R1("v1_15_R1"),
   SPIGOT_1_16_R1("v1_16_R1"),
   SPIGOT_1_16_R2("v1_16_R2"),
   SPIGOT_1_16_R3("v1_16_R3"),
   SPIGOT_1_17_R1("v1_17_R1"),
   SPIGOT_1_18_R1("v1_18_R1"),
   SPIGOT_1_19_R1("v1_19_R1"),
   SPIGOT_1_19_R2("v1_19_R2"),
   SPIGOT_1_19_R3("v1_19_R3"),
   SPIGOT_1_20_R1("v1_20_R1"),
   SPIGOT_1_20_R2("v1_20_R2"),
   SPIGOT_1_20_R3("v1_20_R3"),
   SPIGOT_1_20_R4("v1_20_R4"),
   SPIGOT_1_21_R1("v1_21_R1"),
   SPIGOT_1_21_R2("V1_21_R2"),
   SPIGOT_1_21_R3("V1_21_R3"),
   SPIGOT_1_21_R4("V1_21_R4"),
   SPIGOT_1_21_R5("V1_21_R5"),
   SPIGOT_1_21_R6("V1_21_R6");

   private final String version;

   private NMSVersion(String version) {
      this.version = version;
   }

   public static NMSVersion getVersion(String version) {
      for (NMSVersion v : values()) {
         if (v.getVersion().equalsIgnoreCase(version)) {
            return v;
         }
      }

      return UNKNOWN;
   }

   public String getVersion() {
      return this.version;
   }
}
