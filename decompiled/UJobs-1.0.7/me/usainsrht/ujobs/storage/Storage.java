package me.usainsrht.ujobs.storage;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.usainsrht.ujobs.models.PlayerJobData;
import org.jetbrains.annotations.Nullable;

public interface Storage {
   void save(UUID var1);

   void save();

   CompletableFuture<PlayerJobData> load(UUID var1);

   boolean isCached(UUID var1);

   @Nullable
   PlayerJobData getCached(UUID var1);

   void set(UUID var1, PlayerJobData var2);

   void remove(UUID var1);

   void removeFromCache(UUID var1);

   Collection<PlayerJobData> getAllCachedData();

   default void close() {
   }
}
