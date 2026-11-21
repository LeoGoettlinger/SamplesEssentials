package com.github.retrooper.packetevents.protocol.recipe.display.slot;

import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OnlyWithComponentSlotDisplay extends SlotDisplay<OnlyWithComponentSlotDisplay> {
   private final SlotDisplay<?> source;
   private final ComponentType<?> component;

   public OnlyWithComponentSlotDisplay(SlotDisplay<?> source, ComponentType<?> component) {
      super(SlotDisplayTypes.ONLY_WITH_COMPONENT);
      this.source = source;
      this.component = component;
   }

   public static OnlyWithComponentSlotDisplay read(PacketWrapper<?> wrapper) {
      SlotDisplay<?> source = SlotDisplay.read(wrapper);
      ComponentType<?> component = wrapper.readMappedEntity(ComponentTypes.getRegistry());
      return new OnlyWithComponentSlotDisplay(source, component);
   }

   public static void write(PacketWrapper<?> wrapper, OnlyWithComponentSlotDisplay display) {
      SlotDisplay.write(wrapper, display.source);
      wrapper.writeMappedEntity(display.component);
   }

   public SlotDisplay<?> getSource() {
      return this.source;
   }

   public ComponentType<?> getComponent() {
      return this.component;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof OnlyWithComponentSlotDisplay)) {
         return false;
      } else {
         OnlyWithComponentSlotDisplay that = (OnlyWithComponentSlotDisplay)obj;
         return !this.source.equals(that.source) ? false : this.component.equals(that.component);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.source, this.component);
   }

   @Override
   public String toString() {
      return "OnlyWithComponentSlotDisplay{source=" + this.source + ", component=" + this.component + '}';
   }
}
