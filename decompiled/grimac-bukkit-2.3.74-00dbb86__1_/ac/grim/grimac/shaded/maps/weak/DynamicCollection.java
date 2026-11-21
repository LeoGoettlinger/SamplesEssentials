package ac.grim.grimac.shaded.maps.weak;

import ac.grim.grimac.shaded.maps.LiteJoiner;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

class DynamicCollection extends AbstractDynamic<Collection> implements Dynamic, Describer {
   static final String NO_KEY = "?";

   DynamicCollection(Collection inner) {
      super(inner);
   }

   @Override
   protected Object keyLiteral() {
      return "root";
   }

   @Override
   public Dynamic get(Object childKey) {
      return (Dynamic)(this.inner.isEmpty() ? new ParentAbsence.Empty<>(this, childKey) : new ChildAbsence.Missing<>(this, childKey));
   }

   @Override
   public Stream<Dynamic> children() {
      return this.inner.stream().map(val -> (Dynamic)(val == null ? new ChildAbsence.Null(this, "?") : DynamicChild.from(this, "?", val)));
   }

   @Override
   public String describe() {
      String type = this.inner instanceof Set ? "Set" : "Collection";
      return this.inner.isEmpty() ? "Empty-" + type : type + "[size:" + this.inner.size() + "]";
   }

   @Override
   public String toString() {
      return this.keyLiteral() + ":" + this.describe();
   }

   static class Child extends DynamicCollection implements DynamicChild {
      private final Dynamic parent;
      private final Object key;

      Child(Dynamic parent, Object key, Collection inner) {
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
