package com.github.retrooper.packetevents.protocol.mapper;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface DeepComparableEntity {
   boolean deepEquals(@Nullable Object obj);

   int deepHashCode();
}
