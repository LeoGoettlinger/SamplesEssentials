package ac.grim.grimac.internal.storage.codec;

import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.api.storage.codec.Codecs;
import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApiStatus.Internal
public final class MethodHandleCodecFactory implements Codecs.Provider {
   private static final MethodHandleCodecFactory INSTANCE = new MethodHandleCodecFactory();
   private final ConcurrentMap<Class<?>, MethodHandleCodec<?>> cache = new ConcurrentHashMap<>();

   private MethodHandleCodecFactory() {
   }

   public static void installAsDefaultProvider() {
      Codecs.install(INSTANCE);
   }

   @NotNull
   public static MethodHandleCodecFactory get() {
      return INSTANCE;
   }

   @NotNull
   @Override
   public <R> Codec<R> create(@NotNull Class<R> recordType) {
      return (Codec<R>)this.cache.computeIfAbsent(recordType, MethodHandleCodecFactory::build);
   }

   @NotNull
   public <R> MethodHandleCodec<R> baseCodecFor(@NotNull Class<R> recordType) {
      return (MethodHandleCodec<R>)this.cache.computeIfAbsent(recordType, MethodHandleCodecFactory::build);
   }

   @NotNull
   private static MethodHandleCodec<?> build(@NotNull Class<?> recordType) {
      EncodeShape shape = CodecIntrospection.inspect(recordType);
      return new MethodHandleCodec<>(recordType, shape);
   }
}
