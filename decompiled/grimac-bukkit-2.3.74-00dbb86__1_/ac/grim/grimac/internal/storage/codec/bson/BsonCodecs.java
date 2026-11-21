package ac.grim.grimac.internal.storage.codec.bson;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.internal.storage.codec.CodecIntrospection;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApiStatus.Internal
public final class BsonCodecs {
   private static final ConcurrentMap<Class<?>, BsonCodecImpl<?>> REGULAR = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<?>, BsonTsCodecImpl<?>> TIMESERIES = new ConcurrentHashMap<>();

   private BsonCodecs() {
   }

   @NotNull
   public static <R> BsonCodec<R> regular(@NotNull Class<R> recordType) {
      return (BsonCodec<R>)REGULAR.computeIfAbsent(recordType, t -> {
         EncodeShape shape = CodecIntrospection.inspect((Class<?>)t);
         return new BsonCodecImpl<>((Class<?>)t, shape);
      });
   }

   @NotNull
   public static <R> BsonTsCodec<R> timeseries(@NotNull Class<R> recordType) {
      return (BsonTsCodec<R>)TIMESERIES.computeIfAbsent(recordType, t -> {
         EncodeShape shape = CodecIntrospection.inspect((Class<?>)t);
         return new BsonTsCodecImpl<>((Class<?>)t, shape);
      });
   }
}
