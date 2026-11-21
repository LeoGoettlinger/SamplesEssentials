package com.github.retrooper.packetevents.protocol.mapper;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Internal
public interface ResolvableEntity {
   void doResolve(PacketWrapper<?> wrapper);
}
