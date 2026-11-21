package com.skyblockexp.ezrtp.platform;

public interface PlatformRuntime {
   PlatformRuntimeCapabilities capabilities();

   default boolean isPaper() {
      return this.capabilities().paperApi();
   }

   default boolean isPurpur() {
      return this.capabilities().purpurApi();
   }

   default boolean isRegionizedRuntime() {
      return this.capabilities().regionizedRuntime();
   }

   PlatformWorldAccess worldAccess();

   PlatformScheduler scheduler();
}
