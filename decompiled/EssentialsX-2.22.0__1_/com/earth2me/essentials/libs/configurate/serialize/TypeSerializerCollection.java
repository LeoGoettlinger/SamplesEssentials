package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.objectmapping.ConfigSerializable;
import com.earth2me.essentials.libs.configurate.objectmapping.ObjectMapper;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class TypeSerializerCollection {
   private static final TypeSerializerCollection DEFAULTS = builder()
      .registerExact(Scalars.STRING)
      .registerExact(Scalars.BOOLEAN)
      .register(MapSerializer.TYPE, new MapSerializer())
      .register(ListSerializer.TYPE, new ListSerializer())
      .registerExact(Scalars.BYTE)
      .registerExact(Scalars.SHORT)
      .registerExact(Scalars.INTEGER)
      .registerExact(Scalars.LONG)
      .registerExact(Scalars.FLOAT)
      .registerExact(Scalars.DOUBLE)
      .registerAnnotatedObjects(ObjectMapper.factory())
      .register(Scalars.ENUM)
      .registerExact(Scalars.CHAR)
      .registerExact(Scalars.URI)
      .registerExact(Scalars.URL)
      .registerExact(Scalars.UUID)
      .registerExact(Scalars.PATTERN)
      .register(ArraySerializer.Objects::accepts, new ArraySerializer.Objects())
      .registerExact(ArraySerializer.Booleans.TYPE, new ArraySerializer.Booleans())
      .registerExact(ArraySerializer.Bytes.TYPE, new ArraySerializer.Bytes())
      .registerExact(ArraySerializer.Chars.TYPE, new ArraySerializer.Chars())
      .registerExact(ArraySerializer.Shorts.TYPE, new ArraySerializer.Shorts())
      .registerExact(ArraySerializer.Ints.TYPE, new ArraySerializer.Ints())
      .registerExact(ArraySerializer.Longs.TYPE, new ArraySerializer.Longs())
      .registerExact(ArraySerializer.Floats.TYPE, new ArraySerializer.Floats())
      .registerExact(ArraySerializer.Doubles.TYPE, new ArraySerializer.Doubles())
      .register(SetSerializer::accepts, new SetSerializer())
      .register(ConfigurationNodeSerializer.TYPE, new ConfigurationNodeSerializer())
      .register(PathSerializer.TYPE, PathSerializer.INSTANCE)
      .registerExact(FileSerializer.TYPE, FileSerializer.INSTANCE)
      .register(OptionalSerializer.TYPE, OptionalSerializer.INSTANCE)
      .registerExact(OptionalSerializer.OfInt.TYPE, OptionalSerializer.OfInt.INSTANCE)
      .registerExact(OptionalSerializer.OfLong.TYPE, OptionalSerializer.OfLong.INSTANCE)
      .registerExact(OptionalSerializer.OfDouble.TYPE, OptionalSerializer.OfDouble.INSTANCE)
      .build();
   @Nullable
   private final TypeSerializerCollection parent;
   final List<TypeSerializerCollection.RegisteredSerializer> serializers;
   private final Map<Type, TypeSerializer<?>> typeMatches = new ConcurrentHashMap<>();
   private final Map<AnnotatedType, TypeSerializer<?>> annotatedTypeMatches = new ConcurrentHashMap<>();

   private TypeSerializerCollection(@Nullable final TypeSerializerCollection parent, final List<TypeSerializerCollection.RegisteredSerializer> serializers) {
      this.parent = parent;
      this.serializers = UnmodifiableCollections.copyOf(serializers);
   }

   @Nullable
   public <T> TypeSerializer<T> get(final TypeToken<T> token) {
      Objects.requireNonNull(token, "type");
      return (TypeSerializer<T>)this.get0(token.getAnnotatedType());
   }

   @Nullable
   public <T> TypeSerializer<T> get(final Class<T> token) {
      Objects.requireNonNull(token, "type");
      Types.requireCompleteParameters(token);
      return (TypeSerializer<T>)this.get(token);
   }

   @Nullable
   public TypeSerializer<?> get(final Type type) {
      return this.get0(GenericTypeReflector.box(type));
   }

   @Nullable
   public TypeSerializer<?> get(final AnnotatedType type) {
      return this.get0(GenericTypeReflector.toCanonicalBoxed(type));
   }

   private TypeSerializer<?> get0(final AnnotatedType canonical) {
      TypeSerializer<?> serial = this.annotatedTypeMatches.computeIfAbsent(canonical, param -> {
         for (TypeSerializerCollection.RegisteredSerializer ent : this.serializers) {
            if (ent.matches(param)) {
               return ent.serializer();
            }
         }

         return TypeSerializerCollection.NoOp.INSTANCE;
      });
      if (serial == TypeSerializerCollection.NoOp.INSTANCE) {
         serial = null;
      }

      if (serial == null && this.parent != null) {
         serial = this.parent.get0(canonical);
      }

      return serial;
   }

   private TypeSerializer<?> get0(final Type canonical) {
      TypeSerializer<?> serial = this.typeMatches.computeIfAbsent(canonical, param -> {
         for (TypeSerializerCollection.RegisteredSerializer ent : this.serializers) {
            if (ent.matches(param)) {
               return ent.serializer();
            }
         }

         return TypeSerializerCollection.NoOp.INSTANCE;
      });
      if (serial == TypeSerializerCollection.NoOp.INSTANCE) {
         serial = null;
      }

      if (serial == null && this.parent != null) {
         serial = this.parent.get0(canonical);
      }

      return serial;
   }

   public TypeSerializerCollection.Builder childBuilder() {
      return new TypeSerializerCollection.Builder(this);
   }

   @Override
   public String toString() {
      return "TypeSerializerCollection{parent=" + this.parent + ", serializers=" + this.serializers + '}';
   }

   @Override
   public boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof TypeSerializerCollection)) {
         return false;
      } else {
         TypeSerializerCollection that = (TypeSerializerCollection)other;
         return Objects.equals(this.parent, that.parent) && this.serializers.equals(that.serializers);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.parent, this.serializers);
   }

   public static TypeSerializerCollection.Builder builder() {
      return new TypeSerializerCollection.Builder(null);
   }

   public static TypeSerializerCollection defaults() {
      return DEFAULTS;
   }

   static final class AnnotatedTypeRegistration implements TypeSerializerCollection.RegisteredSerializer {
      private final Predicate<AnnotatedType> predicate;
      private final TypeSerializer<?> serializer;

      AnnotatedTypeRegistration(final Predicate<AnnotatedType> predicate, final TypeSerializer<?> serializer) {
         this.predicate = predicate;
         this.serializer = serializer;
      }

      @Override
      public boolean matches(final Type test) {
         return this.predicate.test(GenericTypeReflector.annotate(test));
      }

      @Override
      public boolean matches(final AnnotatedType annotated) {
         return this.predicate.test(annotated);
      }

      @Override
      public TypeSerializer<?> serializer() {
         return this.serializer;
      }
   }

   public static class Builder {
      @Nullable
      private final TypeSerializerCollection parent;
      private final List<TypeSerializerCollection.RegisteredSerializer> serializers = new ArrayList<>();

      Builder(@Nullable final TypeSerializerCollection parent) {
         this.parent = parent;
      }

      public <T> TypeSerializerCollection.Builder register(final TypeToken<T> type, final TypeSerializer<? super T> serializer) {
         return this.register0(type.getType(), serializer);
      }

      public <T> TypeSerializerCollection.Builder register(final Class<T> type, final TypeSerializer<? super T> serializer) {
         return this.register0(type, serializer);
      }

      public <T> TypeSerializerCollection.Builder register(final Predicate<Type> test, final TypeSerializer<? super T> serializer) {
         Objects.requireNonNull(test, "test");
         Objects.requireNonNull(serializer, "serializer");
         this.serializers.add(new TypeSerializerCollection.TypeRegistration(test, serializer));
         return this;
      }

      public <T> TypeSerializerCollection.Builder register(final ScalarSerializer<T> serializer) {
         Objects.requireNonNull(serializer, "serializer");
         return this.register(serializer.type(), serializer);
      }

      public <T> TypeSerializerCollection.Builder registerAnnotated(final Predicate<AnnotatedType> test, final TypeSerializer<? super T> serializer) {
         Objects.requireNonNull(test, "test");
         Objects.requireNonNull(serializer, "serializer");
         this.serializers.add(new TypeSerializerCollection.AnnotatedTypeRegistration(test, serializer));
         return this;
      }

      private TypeSerializerCollection.Builder register0(final Type type, final TypeSerializer<?> serializer) {
         Objects.requireNonNull(type, "type");
         Objects.requireNonNull(serializer, "serializer");
         this.serializers.add(new TypeSerializerCollection.TypeRegistration(test -> {
            if (GenericTypeReflector.isSuperType(type, test)) {
               return true;
            } else {
               if (test instanceof WildcardType) {
                  Type[] upperBounds = ((WildcardType)test).getUpperBounds();
                  if (upperBounds.length == 1) {
                     return GenericTypeReflector.isSuperType(type, upperBounds[0]);
                  }
               }

               return false;
            }
         }, serializer));
         return this;
      }

      public <T> TypeSerializerCollection.Builder registerExact(final TypeToken<T> type, final TypeSerializer<? super T> serializer) {
         return this.registerExact0(type.getType(), serializer);
      }

      public <T> TypeSerializerCollection.Builder registerExact(final Class<T> type, final TypeSerializer<? super T> serializer) {
         return this.registerExact0(type, serializer);
      }

      public <T> TypeSerializerCollection.Builder registerExact(final ScalarSerializer<T> serializer) {
         Objects.requireNonNull(serializer, "serializer");
         return this.registerExact(serializer.type(), serializer);
      }

      private TypeSerializerCollection.Builder registerExact0(final Type type, final TypeSerializer<?> serializer) {
         Objects.requireNonNull(type, "type");
         Objects.requireNonNull(serializer, "serializer");
         this.serializers.add(new TypeSerializerCollection.TypeRegistration(test -> test.equals(type), serializer));
         return this;
      }

      public TypeSerializerCollection.Builder registerAll(final TypeSerializerCollection other) {
         this.serializers.addAll(Objects.requireNonNull(other, "other").serializers);
         return this;
      }

      public TypeSerializerCollection.Builder registerAnnotatedObjects(final ObjectMapper.Factory factory) {
         return this.register(TypeSerializerCollection.Builder::isAnnotatedTarget, factory.asTypeSerializer());
      }

      static boolean isAnnotatedTarget(final Type type) {
         return GenericTypeReflector.annotate(type).isAnnotationPresent(ConfigSerializable.class);
      }

      public TypeSerializerCollection build() {
         return new TypeSerializerCollection(this.parent, this.serializers);
      }
   }

   static final class NoOp implements TypeSerializer<Void> {
      static final TypeSerializerCollection.NoOp INSTANCE = new TypeSerializerCollection.NoOp();

      private NoOp() {
      }

      public Void deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
         throw new UnsupportedOperationException("this is a placeholder for null, should not be called directly");
      }

      public void serialize(final Type type, @Nullable final Void obj, final ConfigurationNode node) throws SerializationException {
         throw new UnsupportedOperationException("this is a placeholder for null, should not be called directly");
      }
   }

   interface RegisteredSerializer {
      boolean matches(Type test);

      boolean matches(AnnotatedType annotated);

      TypeSerializer<?> serializer();
   }

   static final class TypeRegistration implements TypeSerializerCollection.RegisteredSerializer {
      private final Predicate<Type> predicate;
      private final TypeSerializer<?> serializer;

      TypeRegistration(final Predicate<Type> predicate, final TypeSerializer<?> serializer) {
         this.predicate = predicate;
         this.serializer = serializer;
      }

      @Override
      public boolean matches(final Type test) {
         return this.predicate.test(test);
      }

      @Override
      public boolean matches(final AnnotatedType annotated) {
         return this.predicate.test(annotated.getType());
      }

      @Override
      public TypeSerializer<?> serializer() {
         return this.serializer;
      }
   }
}
