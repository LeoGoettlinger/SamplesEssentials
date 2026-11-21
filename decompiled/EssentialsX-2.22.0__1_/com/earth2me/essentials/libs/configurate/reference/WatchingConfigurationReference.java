package com.earth2me.essentials.libs.configurate.reference;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ScopedConfigurationNode;
import com.earth2me.essentials.libs.configurate.loader.ConfigurationLoader;
import com.earth2me.essentials.libs.configurate.reactive.Disposable;
import com.earth2me.essentials.libs.configurate.reactive.Subscriber;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.Executor;

class WatchingConfigurationReference<N extends ScopedConfigurationNode<N>> extends ManualConfigurationReference<N> implements Subscriber<WatchEvent<?>> {
   private volatile boolean saveSuppressed;
   @Nullable
   private Disposable disposable;

   WatchingConfigurationReference(final ConfigurationLoader<? extends N> loader, final Executor taskExecutor) {
      super(loader, taskExecutor);
   }

   @Override
   public void save(final ConfigurationNode newNode) throws ConfigurateException {
      synchronized (this.loader()) {
         try {
            this.saveSuppressed = true;
            super.save(newNode);
         } finally {
            this.saveSuppressed = false;
         }
      }
   }

   @Override
   public void close() {
      super.close();
      if (this.disposable != null) {
         this.disposable.dispose();
         this.disposable = null;
      }
   }

   public void submit(final WatchEvent<?> item) {
      if (!this.saveSuppressed || item.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
         try {
            this.load();
         } catch (Exception var3) {
            this.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ConfigurationReference.ErrorPhase.LOADING, var3));
         }
      }
   }

   @Override
   public void onError(final Throwable thrown) {
      this.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ConfigurationReference.ErrorPhase.UNKNOWN, thrown));
   }

   @Override
   public void onClose() {
      this.close();
   }

   void disposable(final Disposable disposable) {
      this.disposable = disposable;
   }
}
