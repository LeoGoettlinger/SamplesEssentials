package com.github.retrooper.packetevents.protocol.packettype;

import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

public interface PacketTypeCommon {
   default String getName() {
      return ((Enum)this).name();
   }

   int getId(ClientVersion version);

   PacketSide getSide();

   @Nullable
   Class<? extends PacketWrapper<?>> getWrapperClass();
}
