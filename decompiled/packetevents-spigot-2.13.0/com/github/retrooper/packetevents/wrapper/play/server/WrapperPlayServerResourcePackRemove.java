package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerResourcePackRemove extends PacketWrapper<WrapperPlayServerResourcePackRemove> {
   @Nullable
   private UUID packId;

   public WrapperPlayServerResourcePackRemove(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerResourcePackRemove(@Nullable UUID packId) {
      super(PacketType.Play.Server.RESOURCE_PACK_REMOVE);
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

   public void copy(WrapperPlayServerResourcePackRemove wrapper) {
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
