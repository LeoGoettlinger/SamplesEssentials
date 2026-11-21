package ac.grim.grimac.shaded.fastutil.objects;

public abstract class AbstractObjectSortedSet<K> extends AbstractObjectSet<K> implements ObjectSortedSet<K> {
   protected AbstractObjectSortedSet() {
   }

   @Override
   public abstract ObjectBidirectionalIterator<K> iterator();
}
