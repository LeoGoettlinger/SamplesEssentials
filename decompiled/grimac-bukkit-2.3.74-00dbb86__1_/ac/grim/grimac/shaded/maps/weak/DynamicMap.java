package ac.grim.grimac.shaded.maps.weak;

import ac.grim.grimac.shaded.maps.LiteJoiner;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

class DynamicMap extends AbstractDynamic<Map<?, ?>> implements Dynamic, Describer {
   public DynamicMap(Map<?, ?> inner) {
      super(inner);
   }

   @Override
   public Dynamic get(Object childKey) {
      if (this.inner.isEmpty()) {
         return new ParentAbsence.Empty<>(this, childKey);
      } else if (this.inner.containsKey(childKey)) {
         Object val = this.inner.get(childKey);
         return (Dynamic)(val != null ? DynamicChild.from(this, childKey, val) : new ChildAbsence.Null(this, childKey));
      } else {
         if (childKey instanceof String) {
            for (Entry<?, ?> entry : this.inner.entrySet()) {
               if (childKey.equals(entry.getKey().toString())) {
                  return (Dynamic)(entry.getValue() != null ? DynamicChild.from(this, childKey, entry.getValue()) : new ChildAbsence.Null(this, childKey));
               }
            }
         }

         String keyString = childKey.toString();
         if (this.inner.containsKey(keyString)) {
            Object val = this.inner.get(keyString);
            return (Dynamic)(val != null ? DynamicChild.from(this, keyString, val) : new ChildAbsence.Null(this, keyString));
         } else {
            return new ChildAbsence.Missing<>(this, childKey);
         }
      }
   }

   @Override
   public Stream<Dynamic> children() {
      return this.inner.keySet().stream().map(this::get);
   }

   @Override
   public String describe() {
      return this.inner.isEmpty() ? "Empty-Map" : "Map" + this.inner.keySet().toString();
   }

   @Override
   public Object keyLiteral() {
      return "root";
   }

   @Override
   public String toString() {
      return this.keyLiteral() + ":" + this.describe();
   }

   static class Child extends DynamicMap implements DynamicChild {
      private final Dynamic parent;
      private final Object key;

      Child(Dynamic parent, Object key, Map inner) {
         super(inner);
         this.parent = parent;
         this.key = key;
      }

      @Override
      public Dynamic parent() {
         return this.parent;
      }

      @Override
      public Object keyLiteral() {
         return this.key;
      }

      @Override
      public String toString() {
         return LiteJoiner.on("->").join(DynamicChildLogic.using(this).getAscendingKeyChainWithRoot()) + ":" + this.describe();
      }
   }
}
