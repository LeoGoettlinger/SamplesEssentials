package com.github.retrooper.packetevents.wrapper.configuration.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class WrapperConfigServerResourcePackRemove extends PacketWrapper<WrapperConfigServerResourcePackRemove> {
   @Nullable
   private UUID packId;

   public WrapperConfigServerResourcePackRemove(PacketSendEvent event) {
      super(event);
   }

   public WrapperConfigServerResourcePackRemove(@Nullable UUID packId) {
      super(PacketType.Configuration.Server.RESOURCE_PACK_REMOVE);
      this.packId = packId;
   }

   @Override
   public void read() {
      this.packId = this.readOptional(PacketWrapper::readUUID);
   }

   @Override
   public void write() {
      this.writeOptional(this.packId, PacketWrapper::writeUUID);
   }

   public void copy(WrapperConfigServerResourcePackRemove wrapper) {
      this.packId = wrapper.packId;
   }

   @Nullable
   public UUID getPackId() {
      return this.packId;
   }

   public void setPackId(@Nullable UUID packId) {
      this.packId = packId;
   }
}
