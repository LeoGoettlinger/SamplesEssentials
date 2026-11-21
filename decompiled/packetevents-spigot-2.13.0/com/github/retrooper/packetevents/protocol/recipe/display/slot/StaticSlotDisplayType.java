package com.github.retrooper.packetevents.protocol.recipe.display.slot;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticSlotDisplayType<T extends SlotDisplay<?>> extends AbstractMappedEntity implements SlotDisplayType<T> {
   private final PacketWrapper.Reader<T> reader;
   private final PacketWrapper.Writer<T> writer;

   @Internal
   public StaticSlotDisplayType(@Nullable TypesBuilderData data, PacketWrapper.Reader<T> reader, PacketWrapper.Writer<T> writer) {
      super(data);
      this.reader = reader;
      this.writer = writer;
   }

   @Override
   public T read(PacketWrapper<?> wrapper) {
      return this.reader.apply(wrapper);
   }

   @Override
   public void write(PacketWrapper<?> wrapper, T display) {
      this.writer.accept(wrapper, display);
   }
}
