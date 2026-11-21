package com.skyblockexp.ezrtp.config.teleport;

public enum SearchPattern {
   RANDOM("random"),
   CIRCLE("circle"),
   TRIANGLE("triangle"),
   DIAMOND("diamond"),
   SQUARE("square");

   private final String configKey;

   private SearchPattern(String configKey) {
      this.configKey = configKey;
   }

   public String getConfigKey() {
      return this.configKey;
   }

   public static SearchPattern fromConfig(String rawValue, SearchPattern fallback) {
      if (rawValue != null && !rawValue.isBlank()) {
         for (SearchPattern pattern : values()) {
            if (pattern.name().equalsIgnoreCase(rawValue) || pattern.configKey.equalsIgnoreCase(rawValue)) {
               return pattern;
            }
         }

         return fallback != null ? fallback : RANDOM;
      } else {
         return fallback != null ? fallback : RANDOM;
      }
   }
}
