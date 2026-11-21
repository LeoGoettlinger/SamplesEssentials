package ac.grim.grimac.shaded.maps.weak;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

enum DynamicNothing implements Dynamic, Describer {
   INSTANCE;

   @Override
   public Dynamic get(Object key) {
      return new ParentAbsence.Barren<>(this, key);
   }

   @Override
   public boolean isPresent() {
      return false;
   }

   @Override
   public Object asObject() {
      throw new NoSuchElementException("null 'root' premature end of path *root*");
   }

   @Override
   public Stream<Dynamic> children() {
      return Stream.empty();
   }

   public Dynamic key() {
      return DynamicChild.key(this, "root");
   }

   @Override
   public String describe() {
      return "null";
   }

   @Override
   public String toString() {
      return "root:" + this.describe();
   }
}
