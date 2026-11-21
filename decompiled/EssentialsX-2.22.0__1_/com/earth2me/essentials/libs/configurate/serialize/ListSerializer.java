package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

final class ListSerializer extends AbstractListChildSerializer<List<?>> {
   static final TypeToken<List<?>> TYPE = new TypeToken<List<?>>() {};

   @Override
   protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
      if (!(containerType instanceof AnnotatedParameterizedType)) {
         throw new SerializationException(containerType, "Raw types are not supported for collections");
      } else {
         return ((AnnotatedParameterizedType)containerType).getAnnotatedActualTypeArguments()[0];
      }
   }

   protected List<?> createNew(final int length, final AnnotatedType elementType) {
      return new ArrayList(length);
   }

   protected void forEachElement(final List<?> collection, final CheckedConsumer<Object, SerializationException> action) throws SerializationException {
      for (Object el : collection) {
         action.accept(el);
      }
   }

   protected void deserializeSingle(final int index, final List<?> collection, @Nullable final Object deserialized) {
      ((List<Object>)collection).add(deserialized);
   }
}
