package com.earth2me.essentials.libs.configurate.reference;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.MonotonicNonNull;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.NodePath;
import com.earth2me.essentials.libs.configurate.ScopedConfigurationNode;
import com.earth2me.essentials.libs.configurate.loader.ConfigurationLoader;
import com.earth2me.essentials.libs.configurate.reactive.Processor;
import com.earth2me.essentials.libs.configurate.reactive.Publisher;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.function.Function;

class ManualConfigurationReference<N extends ScopedConfigurationNode<N>> implements ConfigurationReference<N> {
   @MonotonicNonNull
   protected volatile N node;
   private final ConfigurationLoader<? extends N> loader;
   protected final Processor.TransactionalIso<N> updateListener;
   protected final Processor.Iso<Entry<ConfigurationReference.ErrorPhase, Throwable>> errorListener;

   ManualConfigurationReference(final ConfigurationLoader<? extends N> loader, final Executor taskExecutor) {
      this.loader = loader;
      this.updateListener = Processor.createTransactional(taskExecutor);
      this.errorListener = Processor.create(taskExecutor);
      this.errorListener.fallbackHandler(it -> {
         System.err.println("Unhandled error while performing a " + it.getKey() + " for a configuration reference: " + it.getValue());
         it.getValue().printStackTrace();
      });
   }

   @Override
   public final void load() throws ConfigurateException {
      synchronized (this.loader) {
         this.updateListener.submit(this.node = (N)this.loader.load());
      }
   }

   @Override
   public final void save() throws ConfigurateException {
      this.save(this.node);
   }

   @Override
   public void save(final ConfigurationNode newNode) throws ConfigurateException {
      Objects.requireNonNull(newNode, "newNode");
      synchronized (this.loader) {
         ConfigurationNode existing = this.node;
         if (existing.getClass().equals(newNode.getClass())) {
            this.node = (N)newNode;
            this.loader.save(this.node);
            if (newNode != existing) {
               this.updateListener.submit(this.node);
            }
         } else {
            this.loader.save(this.node.from(newNode));
            this.updateListener.submit(this.node);
         }
      }
   }

   @Override
   public final Publisher<N> saveAsync() {
      return Publisher.execute(() -> {
         this.save();
         return this.node();
      }, this.updateListener.executor());
   }

   @Override
   public final Publisher<N> updateAsync(final Function<N, ? extends N> updater) {
      return Publisher.execute(() -> {
         N newNode = (N)updater.apply(this.node());
         this.save(newNode);
         return newNode;
      }, this.updateListener.executor());
   }

   public final N node() {
      return this.node;
   }

   @Override
   public final ConfigurationLoader<? extends N> loader() {
      return this.loader;
   }

   public final N get(final Object... path) {
      return this.node().node(path);
   }

   public final N get(final Iterable<?> path) {
      return this.node().node(path);
   }

   @Override
   public final <T> ValueReference<T, N> referenceTo(final TypeToken<T> type, final NodePath path, @Nullable final T def) throws SerializationException {
      return new ValueReferenceImpl<>(this, path, type, def);
   }

   @Override
   public final <T> ValueReference<T, N> referenceTo(final Class<T> type, final NodePath path, @Nullable final T def) throws SerializationException {
      return new ValueReferenceImpl<>(this, path, type, def);
   }

   @Override
   public final Publisher<N> updates() {
      return this.updateListener;
   }

   @Override
   public final Publisher<Entry<ConfigurationReference.ErrorPhase, Throwable>> errors() {
      return this.errorListener;
   }

   @Override
   public void close() {
      this.updateListener.onClose();
   }
}
