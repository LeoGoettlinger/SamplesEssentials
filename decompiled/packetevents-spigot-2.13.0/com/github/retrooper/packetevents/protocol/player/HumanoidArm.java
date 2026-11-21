package com.github.retrooper.packetevents.protocol.player;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public enum HumanoidArm {
   LEFT,
   RIGHT;

   public static final HumanoidArm[] VALUES = values();

   public int getId() {
      return this == RIGHT ? 0 : 1;
   }

   public static HumanoidArm getById(int handValue) {
      return handValue == 0 ? RIGHT : LEFT;
   }

   public static HumanoidArm read(PacketWrapper<?> wrapper) {
      return wrapper.readVarInt() == 1 ? RIGHT : LEFT;
   }

   public static void write(PacketWrapper<?> wrapper, HumanoidArm arm) {
      wrapper.writeEnum(arm);
   }
}
