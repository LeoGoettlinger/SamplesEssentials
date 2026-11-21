package com.github.retrooper.packetevents.protocol.entity.data.struct;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum CopperGolemState {
   IDLE,
   GETTING_ITEM,
   GETTING_NO_ITEM,
   DROPPING_ITEM,
   DROPPING_NO_ITEM;

   private static final CopperGolemState[] STATES = values();

   public static CopperGolemState read(PacketWrapper<?> wrapper) {
      return wrapper.readEnum(STATES);
   }

   public static void write(PacketWrapper<?> wrapper, CopperGolemState state) {
      wrapper.writeEnum(state);
   }
}
