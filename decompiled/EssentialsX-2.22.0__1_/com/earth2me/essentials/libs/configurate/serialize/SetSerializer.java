package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

final class SetSerializer extends AbstractListChildSerializer<Set<?>> {
   static boolean accepts(final Type type) {
      Class<?> erased = GenericTypeReflector.erase(type);
      return Set.class.isAssignableFrom(erased) && (erased.isAssignableFrom(EnumSet.class) || erased.isAssignableFrom(LinkedHashSet.class));
   }

   @Override
   protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
      if (!(containerType instanceof AnnotatedParameterizedType)) {
         throw new SerializationException("Raw types are not supported for collections");
      } else {
         return ((AnnotatedParameterizedType)containerType).getAnnotatedActualTypeArguments()[0];
      }
   }

   protected Set<?> createNew(final int length, final AnnotatedType elementType) {
      Class<?> erased = GenericTypeReflector.erase(elementType.getType());
      return (Set<?>)(erased.isEnum() ? EnumSet.noneOf(erased.asSubclass(Enum.class)) : new LinkedHashSet(length));
   }

   protected void forEachElement(final Set<?> collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
      for (Object el : collection) {
         action.accept(el);
      }
   }

   protected void deserializeSingle(final int index, final Set<?> collection, @Nullable final Object deserialized) {
      ((Set<Object>)collection).add(deserialized);
   }
}
