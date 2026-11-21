package ch.njol.skript.util.slot;

public abstract class SlotWithIndex extends Slot {
   public abstract int getIndex();

   public int getRawIndex() {
      return this.getIndex();
   }

   @Override
   public boolean isSameSlot(Slot slot) {
      return slot instanceof SlotWithIndex slotWithIndex && this.getRawIndex() == slotWithIndex.getRawIndex();
   }
}
