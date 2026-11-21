package ac.grim.grimac.shaded.fastutil.objects;

import ac.grim.grimac.shaded.fastutil.Size64;
import java.util.Collection;

public interface ObjectCollection<K> extends Collection<K>, ObjectIterable<K> {
   @Override
   ObjectIterator<K> iterator();

   @Override
   default ObjectSpliterator<K> spliterator() {
      return ObjectSpliterators.asSpliterator(this.iterator(), Size64.sizeOf(this), 64);
   }
}
