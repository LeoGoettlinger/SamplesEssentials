package com.github.retrooper.packetevents.event;

import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlayerEvent {
   @UnknownNullability
   <T> T getPlayer();
}
