package ac.grim.grimac.shaded.maps.weak;

import ac.grim.grimac.shaded.maps.LiteJoiner;
import java.util.Objects;
import java.util.stream.Stream;

abstract class AbstractAbsence<Parent extends Dynamic> implements DynamicChild {
   protected final Parent parent;
   protected final Object key;

   AbstractAbsence(Parent parent, Object key) {
      this.parent = parent;
      this.key = key;
   }

   @Override
   public Parent parent() {
      return this.parent;
   }

   public Dynamic key() {
      return DynamicChild.key(this, this.key);
   }

   @Override
   public boolean isPresent() {
      return false;
   }

   @Override
   public Stream<Dynamic> children() {
      return Stream.empty();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AbstractAbsence other = (AbstractAbsence)o;
         return Objects.equals(this.parent, other.parent) && Objects.equals(this.key, other.key);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.parent, this.key);
   }

   @Override
   public String toString() {
      return LiteJoiner.on("->").join(DynamicChildLogic.using(this).getAscendingKeyChainWithRoot()) + ":absent";
   }
}
