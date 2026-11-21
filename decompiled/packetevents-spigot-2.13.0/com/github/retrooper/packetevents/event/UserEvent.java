package com.github.retrooper.packetevents.event;

import com.github.retrooper.packetevents.protocol.player.User;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface UserEvent {
   User getUser();
}
