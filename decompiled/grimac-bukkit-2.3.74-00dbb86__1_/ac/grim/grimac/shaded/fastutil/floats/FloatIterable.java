package ac.grim.grimac.shaded.fastutil.floats;

import ac.grim.grimac.shaded.fastutil.doubles.DoubleIterator;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleIterators;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleSpliterator;
import ac.grim.grimac.shaded.fastutil.doubles.DoubleSpliterators;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public interface FloatIterable extends Iterable<Float> {
   FloatIterator iterator();

   default DoubleIterator doubleIterator() {
      return DoubleIterators.wrap(this.iterator());
   }

   default FloatSpliterator spliterator() {
      return FloatSpliterators.asSpliteratorUnknownSize(this.iterator(), 0);
   }

   default DoubleSpliterator doubleSpliterator() {
      return DoubleSpliterators.wrap(this.spliterator());
   }

   default void forEach(FloatConsumer action) {
      Objects.requireNonNull(action);
      this.iterator().forEachRemaining(action);
   }

   default void forEach(DoubleConsumer action) {
      this.forEach(action instanceof FloatConsumer ? (FloatConsumer)action : action::accept);
   }

   @Deprecated
   @Override
   default void forEach(Consumer<? super Float> action) {
      this.forEach(action instanceof FloatConsumer ? (FloatConsumer)action : action::accept);
   }
}
