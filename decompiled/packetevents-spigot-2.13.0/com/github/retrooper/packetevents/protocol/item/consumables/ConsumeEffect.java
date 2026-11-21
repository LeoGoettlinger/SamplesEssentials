package com.github.retrooper.packetevents.protocol.item.consumables;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public abstract class ConsumeEffect<T extends ConsumeEffect<?>> {
   protected final ConsumeEffectType<T> type;

   protected ConsumeEffect(ConsumeEffectType<T> type) {
      this.type = type;
   }

   public static ConsumeEffect<?> readFull(PacketWrapper<?> wrapper) {
      ConsumeEffectType<?> type = wrapper.readMappedEntity(ConsumeEffectTypes.getRegistry());
      return type.read(wrapper);
   }

   public static <T extends ConsumeEffect<?>> void writeFull(PacketWrapper<?> wrapper, ConsumeEffect<T> effect) {
      wrapper.writeMappedEntity(effect.getType());
      effect.getType().write(wrapper, (T)effect);
   }

   public ConsumeEffectType<T> getType() {
      return this.type;
   }
}
