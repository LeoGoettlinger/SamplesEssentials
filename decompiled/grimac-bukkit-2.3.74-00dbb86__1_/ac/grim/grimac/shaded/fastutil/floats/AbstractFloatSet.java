package ac.grim.grimac.shaded.fastutil.floats;

import ac.grim.grimac.shaded.fastutil.HashCommon;
import java.util.Set;

public abstract class AbstractFloatSet extends AbstractFloatCollection implements Cloneable, FloatSet {
   protected AbstractFloatSet() {
   }

   @Override
   public abstract FloatIterator iterator();

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Set)) {
         return false;
      } else {
         Set<?> s = (Set<?>)o;
         if (s.size() != this.size()) {
            return false;
         } else {
            return s instanceof FloatSet ? this.containsAll((FloatSet)s) : this.containsAll(s);
         }
      }
   }

   @Override
   public int hashCode() {
      int h = 0;
      int n = this.size();
      FloatIterator i = this.iterator();

      while (n-- != 0) {
         float k = i.nextFloat();
         h += HashCommon.float2int(k);
      }

      return h;
   }

   @Override
   public boolean remove(float k) {
      return super.rem(k);
   }

   @Deprecated
   @Override
   public boolean rem(float k) {
      return this.remove(k);
   }
}
