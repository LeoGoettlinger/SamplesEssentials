package com.github.retrooper.packetevents.protocol.entity.data;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class EntityDataType<T> extends AbstractMappedEntity {
   private final PacketWrapper.Reader<? extends T> reader;
   private final PacketWrapper.Writer<T> writer;

   @Internal
   public EntityDataType(@Nullable TypesBuilderData data, PacketWrapper.Reader<? extends T> reader, PacketWrapper.Writer<T> writer) {
      super(data);
      this.reader = reader;
      this.writer = writer;
   }

   public T read(PacketWrapper<?> wrapper) {
      return (T)this.reader.apply(wrapper);
   }

   public void write(PacketWrapper<?> wrapper, T serializer) {
      this.writer.accept(wrapper, serializer);
   }

   @Deprecated
   public Function<PacketWrapper<?>, T> getDataDeserializer() {
      return this.reader::apply;
   }

   @Deprecated
   public BiConsumer<PacketWrapper<?>, T> getDataSerializer() {
      return this.writer;
   }
}
