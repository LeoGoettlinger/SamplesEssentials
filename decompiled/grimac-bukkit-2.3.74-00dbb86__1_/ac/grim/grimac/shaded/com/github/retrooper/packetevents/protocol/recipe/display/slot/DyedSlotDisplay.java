package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.recipe.display.slot;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class DyedSlotDisplay extends SlotDisplay<DyedSlotDisplay> {
   private final SlotDisplay<?> source;
   private final SlotDisplay<?> target;

   public DyedSlotDisplay(SlotDisplay<?> source, SlotDisplay<?> target) {
      super(SlotDisplayTypes.DYED);
      this.source = source;
      this.target = target;
   }

   public static DyedSlotDisplay read(PacketWrapper<?> wrapper) {
      SlotDisplay<?> source = SlotDisplay.read(wrapper);
      SlotDisplay<?> target = SlotDisplay.read(wrapper);
      return new DyedSlotDisplay(source, target);
   }

   public static void write(PacketWrapper<?> wrapper, DyedSlotDisplay display) {
      SlotDisplay.write(wrapper, display.source);
      SlotDisplay.write(wrapper, display.target);
   }

   public SlotDisplay<?> getSource() {
      return this.source;
   }

   public SlotDisplay<?> getTarget() {
      return this.target;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof DyedSlotDisplay)) {
         return false;
      } else {
         DyedSlotDisplay that = (DyedSlotDisplay)obj;
         return !this.source.equals(that.source) ? false : this.target.equals(that.target);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.source, this.target);
   }

   @Override
   public String toString() {
      return "DyedSlotDisplay{source=" + this.source + ", target=" + this.target + '}';
   }
}
