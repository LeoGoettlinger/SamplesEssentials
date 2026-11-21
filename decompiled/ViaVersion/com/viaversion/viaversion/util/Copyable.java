package com.viaversion.viaversion.util;

import com.viaversion.nbt.tag.Tag;
import java.lang.reflect.Array;

public interface Copyable {
   static <T> T copy(T object) {
      if (object == null) {
         return null;
      } else if (object instanceof Tag tag) {
         return (T)tag.copy();
      } else if (object instanceof Copyable copyable) {
         return (T)copyable.copy();
      } else if (!object.getClass().isArray()) {
         return object;
      } else {
         Class<?> componentType = object.getClass().getComponentType();
         int length = Array.getLength(object);
         Object copy = Array.newInstance(componentType, length);
         if (componentType.isPrimitive()) {
            for (int i = 0; i < length; i++) {
               Array.set(copy, i, Array.get(object, i));
            }
         } else {
            for (int i = 0; i < length; i++) {
               Array.set(copy, i, copy(Array.get(object, i)));
            }
         }

         return (T)copy;
      }
   }

   Object copy();
}
