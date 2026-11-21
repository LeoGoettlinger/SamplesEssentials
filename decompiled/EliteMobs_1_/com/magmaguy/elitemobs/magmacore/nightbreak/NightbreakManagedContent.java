package com.magmaguy.elitemobs.magmacore.nightbreak;

import java.util.concurrent.CompletableFuture;

public interface NightbreakManagedContent {
   String getNightbreakSlug();

   String getDisplayName();

   String getDownloadLink();

   int getLocalVersion();

   boolean isInstalled();

   boolean isDownloaded();

   boolean isOutOfDate();

   void setOutOfDate(boolean var1);

   NightbreakAccount.AccessInfo getCachedAccessInfo();

   void setCachedAccessInfo(NightbreakAccount.AccessInfo var1);

   default CompletableFuture<Void> enableAfterDownload() {
      return CompletableFuture.completedFuture(null);
   }
}
