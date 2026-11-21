package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WrapperPlayServerLowDiskSpaceWarning extends PacketWrapper<WrapperPlayServerLowDiskSpaceWarning> {
   public WrapperPlayServerLowDiskSpaceWarning(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerLowDiskSpaceWarning() {
      super(PacketType.Play.Server.LOW_DISK_SPACE_WARNING);
   }
}
