package org.jdbi.v3.core.collector;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

class ArrayBuilder {
   Class<?> componentType;
   List<Object> list = new ArrayList<>();

   ArrayBuilder(Class<?> componentType) {
      this.componentType = componentType;
   }

   public void add(Object element) {
      this.list.add(element);
   }

   public Object build() {
      Object array = Array.newInstance(this.componentType, this.list.size());

      for (int i = 0; i < this.list.size(); i++) {
         Array.set(array, i, this.list.get(i));
      }

      return array;
   }
}
