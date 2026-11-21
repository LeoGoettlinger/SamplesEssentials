package com.skyblockexp.ezrtp.platform;

public record PlatformRuntimeCapabilities(boolean paperApi, boolean purpurApi, boolean regionizedRuntime) {
   public static final PlatformRuntimeCapabilities BUKKIT = new PlatformRuntimeCapabilities(false, false, false);
   public static final PlatformRuntimeCapabilities PAPER = new PlatformRuntimeCapabilities(true, false, false);
   public static final PlatformRuntimeCapabilities PAPER_FOLIA = new PlatformRuntimeCapabilities(true, false, true);
   public static final PlatformRuntimeCapabilities PURPUR = new PlatformRuntimeCapabilities(true, true, false);
   public static final PlatformRuntimeCapabilities PURPUR_REGIONIZED = new PlatformRuntimeCapabilities(true, true, true);

   public boolean isStrictPaper() {
      return this.paperApi && !this.purpurApi;
   }
}
