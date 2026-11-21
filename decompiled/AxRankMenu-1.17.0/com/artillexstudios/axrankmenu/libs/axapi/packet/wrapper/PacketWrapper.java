package com.artillexstudios.axrankmenu.libs.axapi.packet.wrapper;

import com.artillexstudios.axrankmenu.libs.axapi.nms.wrapper.ServerWrapper;
import com.artillexstudios.axrankmenu.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axrankmenu.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axrankmenu.libs.axapi.packet.PacketType;
import com.artillexstudios.axrankmenu.libs.axapi.packet.exception.PacketReadingException;

public abstract class PacketWrapper {
   private Object cachedPacket;
   private boolean dirty;

   public PacketWrapper() {
      this(null);
   }

   public PacketWrapper(PacketEvent event) {
      if (event != null) {
         FriendlyByteBuf buf = event.in();
         if (buf == null) {
            throw PacketReadingException.INSTANCE;
         }

         event.setWrapper(this);
         this.read(buf);
      }
   }

   public abstract void write(FriendlyByteBuf out);

   public abstract void read(FriendlyByteBuf buf);

   public void markDirty() {
      this.dirty = true;
   }

   public Object cached() {
      if (this.cachedPacket == null || this.dirty) {
         FriendlyByteBuf buf = FriendlyByteBuf.alloc();
         buf.writeVarInt(ClientboundPacketTypes.forPacketType(this.packetType()));
         this.write(buf);
         this.cachedPacket = ServerWrapper.INSTANCE.transformPacket(buf);
         this.dirty = false;
      }

      return this.cachedPacket;
   }

   public abstract PacketType packetType();
}
