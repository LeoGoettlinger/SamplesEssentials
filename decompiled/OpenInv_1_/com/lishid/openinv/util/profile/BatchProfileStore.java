package com.lishid.openinv.util.profile;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.WrappedJavaPlugin;
import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.runnable.WrappedRunnable;
import com.github.jikoo.openinv.lib.planarwrappers.scheduler.TickTimeUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

public abstract class BatchProfileStore implements ProfileStore {
   private final Set<Profile> pending = Collections.synchronizedSet(new HashSet<>());
   private final AtomicReference<WrappedRunnable> insertTask = new AtomicReference<>();
   @NotNull
   protected final WrappedJavaPlugin plugin;

   protected BatchProfileStore(@NotNull WrappedJavaPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public void addProfile(@NotNull Profile profile) {
      this.pending.add(profile);
      this.buildBatch();
   }

   private void buildBatch() {
      if (this.insertTask.compareAndSet(null, new WrappedRunnable() {
         @Override
         public void run() {
            BatchProfileStore.this.pushBatch();
         }
      })) {
         try {
            this.insertTask.get().runTaskLaterAsynchronously(this.plugin, TickTimeUnit.toTicks(5L, TimeUnit.SECONDS));
         } catch (IllegalStateException var2) {
            this.insertTask.set(null);
         }
      }
   }

   private void pushBatch() {
      Set<Profile> batch = new HashSet<>(this.pending);
      this.pending.removeAll(batch);
      this.pushBatch(batch);
      WrappedRunnable running = this.insertTask.getAndSet(null);
      if (running != null) {
         running.cancel();
      }

      if (!this.pending.isEmpty()) {
         this.buildBatch();
      }
   }

   @Override
   public void shutdown() {
      WrappedRunnable wrappedRunnable = this.insertTask.get();
      if (wrappedRunnable != null) {
         wrappedRunnable.cancel();
      }

      this.pushBatch();
      this.insertTask.set(null);
   }

   protected abstract void pushBatch(@NotNull Set<Profile> var1);
}
