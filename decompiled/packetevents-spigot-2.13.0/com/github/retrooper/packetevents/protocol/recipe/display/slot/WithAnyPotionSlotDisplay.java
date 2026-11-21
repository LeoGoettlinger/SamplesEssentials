package com.github.retrooper.packetevents.protocol.recipe.display.slot;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WithAnyPotionSlotDisplay extends SlotDisplay<WithAnyPotionSlotDisplay> {
   private final SlotDisplay<?> display;

   public WithAnyPotionSlotDisplay(SlotDisplay<?> display) {
      super(SlotDisplayTypes.WITH_ANY_POTION);
      this.display = display;
   }

   public static WithAnyPotionSlotDisplay read(PacketWrapper<?> wrapper) {
      return new WithAnyPotionSlotDisplay(SlotDisplay.read(wrapper));
   }

   public static void write(PacketWrapper<?> wrapper, WithAnyPotionSlotDisplay display) {
      SlotDisplay.write(wrapper, display.display);
   }

   public SlotDisplay<?> getDisplay() {
      return this.display;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof WithAnyPotionSlotDisplay)) {
         return false;
      } else {
         WithAnyPotionSlotDisplay that = (WithAnyPotionSlotDisplay)obj;
         return this.display.equals(that.display);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.display);
   }

   @Override
   public String toString() {
      return "WithAnyPotionSlotDisplay{display=" + this.display + '}';
   }
}
