package com.github.retrooper.packetevents.protocol.entity.data.struct;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum WeatheringCopperState {
   UNAFFECTED,
   EXPOSED,
   WEATHERED,
   OXIDIZED;

   private static final WeatheringCopperState[] STATES = values();

   public static WeatheringCopperState read(PacketWrapper<?> wrapper) {
      return wrapper.readEnum(STATES);
   }

   public static void write(PacketWrapper<?> wrapper, WeatheringCopperState state) {
      wrapper.writeEnum(state);
   }
}
