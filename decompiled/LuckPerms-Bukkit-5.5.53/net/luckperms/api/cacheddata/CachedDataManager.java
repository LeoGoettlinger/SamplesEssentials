package net.luckperms.api.cacheddata;

import java.util.concurrent.CompletableFuture;
import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface CachedDataManager {
   @NonNull
   CachedDataManager.Container<CachedPermissionData> permissionData();

   @NonNull
   CachedDataManager.Container<CachedMetaData> metaData();

   @NonNull
   CachedPermissionData getPermissionData(@NonNull QueryOptions var1);

   @NonNull
   CachedMetaData getMetaData(@NonNull QueryOptions var1);

   @NonNull
   CachedPermissionData getPermissionData();

   @NonNull
   CachedMetaData getMetaData();

   void invalidate();

   void invalidatePermissionCalculators();

   public interface Container<T extends CachedData> {
      @NonNull
      T get(@NonNull QueryOptions var1);

      @NonNull
      T calculate(@NonNull QueryOptions var1);

      void recalculate(@NonNull QueryOptions var1);

      @NonNull
      CompletableFuture<? extends T> reload(@NonNull QueryOptions var1);

      void recalculate();

      @NonNull
      CompletableFuture<Void> reload();

      void invalidate(@NonNull QueryOptions var1);

      void invalidate();
   }
}
