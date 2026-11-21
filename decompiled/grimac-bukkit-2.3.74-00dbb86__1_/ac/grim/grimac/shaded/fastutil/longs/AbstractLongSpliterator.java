package ac.grim.grimac.shaded.fastutil.longs;

public abstract class AbstractLongSpliterator implements LongSpliterator {
   protected AbstractLongSpliterator() {
   }

   @Override
   public final boolean tryAdvance(LongConsumer action) {
      return this.tryAdvance(action);
   }

   @Override
   public final void forEachRemaining(LongConsumer action) {
      this.forEachRemaining(action);
   }
}
