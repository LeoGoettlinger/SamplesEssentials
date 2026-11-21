package revxrsal.commands.util;

import java.util.HashMap;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

public final class ClassMap<V> extends HashMap<Class<?>, V> {
   public boolean add(Class<?> type, V value) {
      Class<?> wrapped = Primitives.wrap(type);
      if (this.containsKey(wrapped)) {
         return false;
      } else {
         this.put(wrapped, value);
         return false;
      }
   }

   public V getFlexibleOrDefault(@NotNull Class<?> key, V def) {
      V value = this.getFlexible(key);
      return value == null ? def : value;
   }

   public V getFlexible(@NotNull Class<?> key) {
      key = Primitives.wrap(key);
      V v = this.get(key);
      if (v != null) {
         return v;
      } else {
         for (Entry<Class<?>, V> entry : this.entrySet()) {
            if (entry.getKey().isAssignableFrom(key)) {
               v = entry.getValue();
               break;
            }
         }

         this.put(key, v);
         return v;
      }
   }
}
