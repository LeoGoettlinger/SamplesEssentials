package com.github.retrooper.packetevents.protocol.world.attributes;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.world.attributes.modifiers.AttributeModifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AttributeType<T> extends MappedEntity {
   boolean isSynced();

   NbtCodec<T> getValueCodec();

   NbtCodec<AttributeModifier<T, ?>> getModifierCodec();
}
