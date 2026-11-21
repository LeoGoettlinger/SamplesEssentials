package org.jdbi.v3.core.codec;

import com.google.errorprone.annotations.ThreadSafe;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.QualifiedArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.QualifiedColumnMapperFactory;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.meta.Beta;

@ThreadSafe
@Beta
public class CodecFactory implements QualifiedColumnMapperFactory, QualifiedArgumentFactory.Preparable {
   protected final ConcurrentMap<QualifiedType<?>, Codec<?>> codecMap = new ConcurrentHashMap<>();

   public static CodecFactory.Builder builder() {
      return new CodecFactory.Builder(CodecFactory::new);
   }

   public static CodecFactory forSingleCodec(QualifiedType<?> type, Codec<?> codec) {
      return new CodecFactory(Collections.singletonMap(type, codec));
   }

   public CodecFactory(final Map<QualifiedType<?>, Codec<?>> codecMap) {
      Objects.requireNonNull(codecMap, "codecMap is null");
      this.codecMap.putAll(codecMap);
   }

   @Override
   public final Optional<Function<Object, Argument>> prepare(final QualifiedType<?> type, final ConfigRegistry config) {
      return Optional.of(type).map(this::resolveType).map(key -> (Function<Object, Argument>)key.getArgumentFunction(config));
   }

   @Deprecated(
      since = "3.39.0",
      forRemoval = true
   )
   @Override
   public final Collection<QualifiedType<?>> prePreparedTypes() {
      return this.codecMap.keySet();
   }

   @Override
   public final Optional<Argument> build(final QualifiedType<?> type, final Object value, final ConfigRegistry config) {
      return this.prepare(type, config).map(f -> f.apply(value));
   }

   @Override
   public final Optional<ColumnMapper<?>> build(final QualifiedType<?> type, final ConfigRegistry config) {
      return Optional.of(type).map(this::resolveType).map(c -> c.getColumnMapper(config));
   }

   protected Codec<?> resolveType(QualifiedType<?> qualifiedType) {
      return this.codecMap.get(qualifiedType);
   }

   @Beta
   public static final class Builder {
      private final Map<QualifiedType<?>, Codec<?>> codecMap = new HashMap<>();
      private final Function<Map<QualifiedType<?>, Codec<?>>, CodecFactory> factory;

      public Builder(Function<Map<QualifiedType<?>, Codec<?>>, CodecFactory> factory) {
         this.factory = Objects.requireNonNull(factory, "factory is null");
      }

      public CodecFactory.Builder addCodec(final QualifiedType<?> type, final Codec<?> codec) {
         Objects.requireNonNull(type, "type is null");
         Objects.requireNonNull(codec, "codec is null");
         this.codecMap.put(type, codec);
         return this;
      }

      public CodecFactory.Builder addCodec(final Type type, final Codec<?> codec) {
         Objects.requireNonNull(type, "type is null");
         Objects.requireNonNull(codec, "codec is null");
         this.codecMap.put(QualifiedType.of(type), codec);
         return this;
      }

      public CodecFactory.Builder addCodec(final GenericType<?> type, final Codec<?> codec) {
         Objects.requireNonNull(type, "type is null");
         Objects.requireNonNull(codec, "codec is null");
         this.codecMap.put(QualifiedType.of(type.getType()), codec);
         return this;
      }

      public CodecFactory build() {
         return this.factory.apply(this.codecMap);
      }
   }
}
