package com.github.retrooper.packetevents.protocol.component.predicates;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public class StaticComponentPredicateType<T extends IComponentPredicate> extends AbstractMappedEntity implements ComponentPredicateType<T> {
   private final PacketWrapper.Reader<T> reader;
   private final PacketWrapper.Writer<T> writer;

   public StaticComponentPredicateType(PacketWrapper.Reader<T> reader, PacketWrapper.Writer<T> writer) {
      this(null, reader, writer);
   }

   @Internal
   public StaticComponentPredicateType(@Nullable TypesBuilderData data, PacketWrapper.Reader<T> reader, PacketWrapper.Writer<T> writer) {
      super(data);
      this.reader = reader;
      this.writer = writer;
   }

   @Override
   public T read(PacketWrapper<?> wrapper) {
      return this.reader.apply(wrapper);
   }

   @Override
   public void write(PacketWrapper<?> wrapper, T predicate) {
      this.writer.accept(wrapper, predicate);
   }
}
