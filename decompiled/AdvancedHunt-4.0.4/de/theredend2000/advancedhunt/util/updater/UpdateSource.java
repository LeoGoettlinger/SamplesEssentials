package de.theredend2000.advancedhunt.util.updater;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface UpdateSource {
   CompletableFuture<UpdateInfo> getLatestUpdate(String var1);

   CompletableFuture<Boolean> downloadPlugin(String var1, UpdateInfo var2, File var3);

   String getName();
}
