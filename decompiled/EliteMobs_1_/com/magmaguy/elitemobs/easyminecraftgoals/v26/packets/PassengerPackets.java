package com.magmaguy.elitemobs.easyminecraftgoals.v26.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketPassengerRegistry;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;

final class PassengerPackets {
   private PassengerPackets() {
   }

   static ClientboundSetPassengersPacket create(int vehicle, int[] passengers) {
      FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

      ClientboundSetPassengersPacket var3;
      try {
         buffer.writeVarInt(vehicle);
         buffer.writeVarIntArray(passengers);
         var3 = (ClientboundSetPassengersPacket)ClientboundSetPassengersPacket.STREAM_CODEC.decode(buffer);
      } finally {
         buffer.release();
      }

      return var3;
   }

   static Packet<?> compose(UUID viewer, Packet<?> packet) {
      if (!(packet instanceof ClientboundSetPassengersPacket) && !(packet instanceof ClientboundBundlePacket)) {
         return packet;
      } else if (!PacketPassengerRegistry.hasAttachments(viewer)) {
         return packet;
      } else if (packet instanceof ClientboundSetPassengersPacket mount) {
         int[] passengers = PacketPassengerRegistry.compose(viewer, mount.getVehicle(), mount.getPassengers());
         return (Packet<?>)(Arrays.equals(passengers, mount.getPassengers()) ? packet : create(mount.getVehicle(), passengers));
      } else if (!(packet instanceof ClientboundBundlePacket bundle)) {
         return packet;
      } else {
         boolean hasMount = false;

         for (Packet<?> child : bundle.subPackets()) {
            if (child instanceof ClientboundSetPassengersPacket || child instanceof ClientboundBundlePacket) {
               hasMount = true;
               break;
            }
         }

         if (!hasMount) {
            return packet;
         } else {
            List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            boolean changed = false;

            for (Packet<? super ClientGamePacketListener> childx : bundle.subPackets()) {
               Packet<?> composed = compose(viewer, childx);
               changed |= composed != childx;
               packets.add((Packet<? super ClientGamePacketListener>)composed);
            }

            return (Packet<?>)(changed ? new ClientboundBundlePacket(packets) : packet);
         }
      }
   }
}
