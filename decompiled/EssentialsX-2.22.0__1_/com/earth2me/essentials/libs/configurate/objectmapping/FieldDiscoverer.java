package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import java.util.function.Supplier;

public interface FieldDiscoverer<I> {
   static FieldDiscoverer<?> record() {
      return RecordFieldDiscoverer.INSTANCE;
   }

   static FieldDiscoverer<?> object(final CheckedFunction<AnnotatedType, Supplier<Object>, SerializationException> instanceFactory) {
      return new ObjectFieldDiscoverer(Objects.requireNonNull(instanceFactory, "instanceFactory"), null, false);
   }

   static FieldDiscoverer<?> object(
      final CheckedFunction<AnnotatedType, Supplier<Object>, SerializationException> instanceFactory, final String instanceUnavailableErrorMessage
   ) {
      Objects.requireNonNull(instanceUnavailableErrorMessage, "instanceUnavailableErrorMessage");
      return new ObjectFieldDiscoverer(Objects.requireNonNull(instanceFactory, "instanceFactory"), instanceUnavailableErrorMessage, false);
   }

   static FieldDiscoverer<?> instantiableObject(final CheckedFunction<AnnotatedType, Supplier<Object>, SerializationException> instanceFactory) {
      return new ObjectFieldDiscoverer(Objects.requireNonNull(instanceFactory, "instanceFactory"), null, true);
   }

   static FieldDiscoverer<?> emptyConstructorObject() {
      return ObjectFieldDiscoverer.EMPTY_CONSTRUCTOR_INSTANCE;
   }

   @Nullable
   <V> FieldDiscoverer.InstanceFactory<I> discover(AnnotatedType target, FieldDiscoverer.FieldCollector<I, V> collector) throws SerializationException;

   @FunctionalInterface
   public interface FieldCollector<I, V> {
      void accept(
         String name,
         AnnotatedType type,
         AnnotatedElement annotations,
         FieldData.Deserializer<I> deserializer,
         CheckedFunction<V, Object, Exception> serializer
      );
   }

   public interface InstanceFactory<I> {
      I begin();

      Object complete(I intermediate) throws SerializationException;

      boolean canCreateInstances();
   }

   public interface MutableInstanceFactory<I> extends FieldDiscoverer.InstanceFactory<I> {
      void complete(Object instance, I intermediate) throws SerializationException;
   }
}
