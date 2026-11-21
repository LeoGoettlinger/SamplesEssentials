package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AttributeDisplay {
   static AttributeDisplay read(PacketWrapper<?> wrapper) {
      AttributeDisplayType<?> type = wrapper.readMappedEntity(AttributeDisplayTypes.getRegistry());
      return type.read(wrapper);
   }

   static void write(PacketWrapper<?> wrapper, AttributeDisplay display) {
      wrapper.writeMappedEntity(display.getType());
      ((AttributeDisplayType<AttributeDisplay>)display.getType()).write(wrapper, display);
   }

   AttributeDisplayType<?> getType();
}
