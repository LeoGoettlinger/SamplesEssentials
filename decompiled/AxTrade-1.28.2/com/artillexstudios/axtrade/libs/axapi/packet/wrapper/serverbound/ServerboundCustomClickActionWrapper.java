package com.artillexstudios.axtrade.libs.axapi.packet.wrapper.serverbound;

import com.artillexstudios.axtrade.libs.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axtrade.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axtrade.libs.axapi.packet.PacketType;
import com.artillexstudios.axtrade.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axtrade.libs.axapi.packet.wrapper.PacketWrapper;
import net.kyori.adventure.key.Key;

public final class ServerboundCustomClickActionWrapper extends PacketWrapper {
   private static final int PAYLOAD_LIMIT = 65536;
   private Key key;
   private CompoundTag tag;

   public ServerboundCustomClickActionWrapper(PacketEvent event) {
      super(event);
   }

   @Override
   public void write(FriendlyByteBuf out) {
      this.key = out.readResourceLocation();
      FriendlyByteBuf buf = FriendlyByteBuf.alloc();
      buf.writeBoolean(this.tag != null);
      if (this.tag != null) {
         buf.writeNBT(this.tag);
      }

      int i = buf.readableBytes();
      if (i > 65536) {
         throw new IllegalArgumentException("Buffer size " + i + " is  larger than maximum of 65536!");
      } else {
         out.writeVarInt(i);
         out.writeBytes(buf);
      }
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      buf.writeResourceLocation(this.key);
      int length = buf.readVarInt();
      if (length > 65536) {
         throw new IllegalArgumentException("Buffer size " + length + " is larger than maximum of 65536!");
      } else {
         int i1 = buf.readerIndex();
         FriendlyByteBuf byteBuf = buf.slice(i1, length);
         boolean b = byteBuf.readBoolean();
         if (b) {
            this.tag = byteBuf.readNBT();
         }

         buf.readerIndex(i1 + length);
      }
   }

   @Override
   public PacketType packetType() {
      return ServerboundPacketTypes.CUSTOM_CLICK_ACTION;
   }
}
