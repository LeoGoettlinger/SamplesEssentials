package ac.grim.grimac.shaded.fastutil.longs;

public abstract class AbstractLongIterator implements LongIterator {
   protected AbstractLongIterator() {
   }

   @Override
   public final void forEachRemaining(LongConsumer action) {
      this.forEachRemaining(action);
   }
}
