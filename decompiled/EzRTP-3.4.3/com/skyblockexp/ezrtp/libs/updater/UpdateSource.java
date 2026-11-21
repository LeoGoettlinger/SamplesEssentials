package com.skyblockexp.ezrtp.libs.updater;

import java.util.concurrent.CompletableFuture;

public interface UpdateSource {
   String getSourceName();

   UpdateResult checkNow();

   CompletableFuture<UpdateResult> checkNowAsync();
}
