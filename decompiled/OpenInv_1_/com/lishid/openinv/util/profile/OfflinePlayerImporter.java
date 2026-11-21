package com.lishid.openinv.util.profile;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.runnable.WrappedRunnable;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public abstract class OfflinePlayerImporter extends WrappedRunnable {
   @NotNull
   private final BatchProfileStore profileStore;
   private final int batchSize;

   public OfflinePlayerImporter(@NotNull BatchProfileStore profileStore, int batchSize) {
      this.profileStore = profileStore;
      this.batchSize = batchSize;
   }

   @Override
   public void run() {
      Set<Profile> batch = new HashSet<>();

      for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
         String name = offline.getName();
         if (name != null) {
            batch.add(new Profile(name, offline.getUniqueId()));
            if (batch.size() >= this.batchSize) {
               this.profileStore.pushBatch(batch);
               batch.clear();
            }
         }
      }

      if (!batch.isEmpty()) {
         this.profileStore.pushBatch(batch);
      }

      this.onComplete();
   }

   public abstract void onComplete();
}
