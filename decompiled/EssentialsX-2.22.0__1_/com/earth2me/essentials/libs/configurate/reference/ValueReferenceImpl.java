package com.earth2me.essentials.libs.configurate.reference;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.NodePath;
import com.earth2me.essentials.libs.configurate.ScopedConfigurationNode;
import com.earth2me.essentials.libs.configurate.reactive.Disposable;
import com.earth2me.essentials.libs.configurate.reactive.Publisher;
import com.earth2me.essentials.libs.configurate.reactive.Subscriber;
import com.earth2me.essentials.libs.configurate.reactive.TransactionFailedException;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.util.concurrent.Executor;
import java.util.function.Function;

class ValueReferenceImpl<T, N extends ScopedConfigurationNode<N>> implements ValueReference<T, N>, Publisher<T> {
   private final ManualConfigurationReference<N> root;
   private final NodePath path;
   private final TypeToken<T> type;
   private final TypeSerializer<T> serializer;
   private final Publisher.Cached<T> deserialized;

   ValueReferenceImpl(final ManualConfigurationReference<N> root, final NodePath path, final TypeToken<T> type, final T def) throws SerializationException {
      this.root = root;
      this.path = path;
      this.type = type;
      TypeSerializer<T> serializer = root.node().options().serializers().get(type);
      if (serializer == null) {
         throw new SerializationException(this.path, type.getType(), "Unsupported type" + type);
      } else {
         this.serializer = serializer;
         this.deserialized = root.updateListener.map(n -> {
            try {
               return this.deserializedValueFrom((N)n, def);
            } catch (SerializationException var5x) {
               root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ConfigurationReference.ErrorPhase.VALUE, var5x));
               throw new TransactionFailedException(var5x);
            }
         }).cache(this.deserializedValueFrom(root.node(), def));
      }
   }

   ValueReferenceImpl(final ManualConfigurationReference<N> root, final NodePath path, final Class<T> type, @Nullable final T def) throws SerializationException {
      this(root, path, TypeToken.get(type), def);
   }

   private T deserializedValueFrom(final N parent, final T defaultVal) throws SerializationException {
      N node = parent.node(this.path);
      if (!node.virtual()) {
         return this.serializer.deserialize(this.type.getType(), node);
      } else {
         T defaultOrEmpty = defaultVal == null ? this.serializer.emptyValue(this.type.getType(), node.options()) : defaultVal;
         if (node.options().shouldCopyDefaults()) {
            this.serializer.serialize(this.type.getType(), defaultOrEmpty, node);
         }

         return defaultOrEmpty;
      }
   }

   @Nullable
   @Override
   public T get() {
      return this.deserialized.get();
   }

   @Override
   public boolean set(@Nullable final T value) {
      try {
         this.serializer.serialize(this.type.getType(), value, this.node());
         this.deserialized.submit(value);
         return true;
      } catch (SerializationException var3) {
         this.root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ConfigurationReference.ErrorPhase.SAVING, var3));
         return false;
      }
   }

   @Override
   public boolean setAndSave(@Nullable final T value) {
      try {
         if (this.set(value)) {
            this.root.save();
            return true;
         }
      } catch (ConfigurateException var3) {
         this.root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ConfigurationReference.ErrorPhase.SAVING, var3));
      }

      return false;
   }

   @Override
   public Publisher<Boolean> setAndSaveAsync(@Nullable final T value) {
      return Publisher.execute(() -> {
         this.serializer.serialize(this.type.getType(), value, this.node());
         this.deserialized.submit(value);
         this.root.save();
         return true;
      }, this.root.updates().executor());
   }

   @Override
   public boolean update(final Function<T, ? extends T> action) {
      try {
         return this.set((T)action.apply(this.get()));
      } catch (Exception var3) {
         this.root.errorListener.submit(UnmodifiableCollections.immutableMapEntry(ConfigurationReference.ErrorPhase.VALUE, var3));
         return false;
      }
   }

   @Override
   public Publisher<Boolean> updateAsync(final Function<T, ? extends T> action) {
      return Publisher.execute(() -> {
         T orig = this.get();
         T updated = (T)action.apply(orig);
         this.serializer.serialize(this.type.getType(), updated, this.node());
         this.deserialized.submit(updated);
         this.root.save();
         return true;
      }, this.root.updates().executor());
   }

   public N node() {
      return this.root.node().node(this.path);
   }

   @Override
   public Disposable subscribe(final Subscriber<? super T> subscriber) {
      return this.deserialized.subscribe(subscriber);
   }

   @Override
   public boolean hasSubscribers() {
      return this.deserialized.hasSubscribers();
   }

   @Override
   public Executor executor() {
      return this.deserialized.executor();
   }
}
