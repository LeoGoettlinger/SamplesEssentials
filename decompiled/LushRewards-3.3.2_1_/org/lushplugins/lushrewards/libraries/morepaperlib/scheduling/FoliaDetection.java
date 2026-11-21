package org.lushplugins.lushrewards.libraries.morepaperlib.scheduling;

public interface FoliaDetection {
   boolean isUsingFolia();

   static FoliaDetection disabled() {
      return () -> false;
   }

   static FoliaDetection enabled() {
      return () -> true;
   }
}
