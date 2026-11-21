package com.earth2me.essentials.libs.configurate.reference;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.NodePath;
import com.earth2me.essentials.libs.configurate.ScopedConfigurationNode;
import com.earth2me.essentials.libs.configurate.loader.ConfigurationLoader;
import com.earth2me.essentials.libs.configurate.reactive.Publisher;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public interface ConfigurationReference<N extends ConfigurationNode> extends AutoCloseable {
   static <N extends ScopedConfigurationNode<N>> ConfigurationReference<N> fixed(final ConfigurationLoader<? extends N> loader) throws ConfigurateException {
      ConfigurationReference<N> ret = new ManualConfigurationReference<>(loader, ForkJoinPool.commonPool());
      ret.load();
      return ret;
   }

   static <T extends ScopedConfigurationNode<T>> ConfigurationReference<T> watching(
      final Function<Path, ConfigurationLoader<? extends T>> loaderCreator, final Path file, final WatchServiceListener listener
   ) throws ConfigurateException {
      WatchingConfigurationReference<T> ret = new WatchingConfigurationReference<>(loaderCreator.apply(file), listener.taskExecutor);
      ret.load();
      ret.disposable(listener.listenToFile(file, ret));
      return ret;
   }

   void load() throws ConfigurateException;

   void save() throws ConfigurateException;

   void save(ConfigurationNode newNode) throws ConfigurateException;

   Publisher<N> saveAsync();

   Publisher<N> updateAsync(Function<N, ? extends N> updater);

   N node();

   ConfigurationLoader<? extends N> loader();

   N get(Object... path);

   N get(Iterable<?> path);

   default void set(final Object[] path, @Nullable final Object value) throws SerializationException {
      this.node().node(path).set(value);
   }

   default <T> void set(final Object[] path, final Class<T> type, @Nullable final T value) throws SerializationException {
      this.node().node(path).set(type, value);
   }

   default <T> void set(final Object[] path, final TypeToken<T> type, @Nullable final T value) throws SerializationException {
      this.node().node(path).set(type, value);
   }

   default void set(final NodePath path, @Nullable final Object value) throws SerializationException {
      this.node().node(path).set(value);
   }

   default <T> void set(final NodePath path, final Class<T> type, @Nullable final T value) throws SerializationException {
      this.node().node(path).set(type, value);
   }

   default <T> void set(final NodePath path, final TypeToken<T> type, @Nullable final T value) throws SerializationException {
      this.node().node(path).set(type, value);
   }

   default <T> ValueReference<T, N> referenceTo(final TypeToken<T> type, final Object... path) throws SerializationException {
      return this.referenceTo(type, NodePath.of(path));
   }

   default <T> ValueReference<T, N> referenceTo(final Class<T> type, final Object... path) throws SerializationException {
      return this.referenceTo(type, NodePath.of(path));
   }

   default <T> ValueReference<T, N> referenceTo(final TypeToken<T> type, final NodePath path) throws SerializationException {
      return this.referenceTo(type, path, null);
   }

   default <T> ValueReference<T, N> referenceTo(final Class<T> type, final NodePath path) throws SerializationException {
      return this.referenceTo(type, path, null);
   }

   <T> ValueReference<T, N> referenceTo(TypeToken<T> type, NodePath path, @Nullable T defaultValue) throws SerializationException;

   <T> ValueReference<T, N> referenceTo(Class<T> type, NodePath path, @Nullable T defaultValue) throws SerializationException;

   Publisher<N> updates();

   Publisher<Entry<ConfigurationReference.ErrorPhase, Throwable>> errors();

   @Override
   void close();

   public static enum ErrorPhase {
      LOADING,
      SAVING,
      UNKNOWN,
      VALUE;
   }
}
