package com.github.retrooper.packetevents.protocol.attribute;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class DefaultAttributeDisplay implements AttributeDisplay {
   public static final DefaultAttributeDisplay INSTANCE = new DefaultAttributeDisplay();

   private DefaultAttributeDisplay() {
   }

   public static DefaultAttributeDisplay read(PacketWrapper<?> wrapper) {
      return INSTANCE;
   }

   public static void write(PacketWrapper<?> wrapper, DefaultAttributeDisplay display) {
   }

   @Override
   public AttributeDisplayType<?> getType() {
      return AttributeDisplayTypes.DEFAULT;
   }
}
