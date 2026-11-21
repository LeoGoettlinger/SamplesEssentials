package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R4.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketPassengerRegistry;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutMount;

final class PassengerPackets {
   private PassengerPackets() {
   }

   static PacketPlayOutMount create(int vehicle, int[] passengers) {
      PacketDataSerializer buffer = new PacketDataSerializer(Unpooled.buffer());

      PacketPlayOutMount var3;
      try {
         buffer.c(vehicle);
         buffer.a(passengers);
         var3 = (PacketPlayOutMount)PacketPlayOutMount.a.decode(buffer);
      } finally {
         buffer.release();
      }

      return var3;
   }

   static Packet<?> compose(UUID viewer, Packet<?> packet) {
      if (!(packet instanceof PacketPlayOutMount) && !(packet instanceof ClientboundBundlePacket)) {
         return packet;
      } else if (!PacketPassengerRegistry.hasAttachments(viewer)) {
         return packet;
      } else if (packet instanceof PacketPlayOutMount mount) {
         int[] passengers = PacketPassengerRegistry.compose(viewer, mount.e(), mount.b());
         return (Packet<?>)(Arrays.equals(passengers, mount.b()) ? packet : create(mount.e(), passengers));
      } else if (!(packet instanceof ClientboundBundlePacket bundle)) {
         return packet;
      } else {
         boolean hasMount = false;

         for (Packet<?> child : bundle.b()) {
            if (child instanceof PacketPlayOutMount || child instanceof ClientboundBundlePacket) {
               hasMount = true;
               break;
            }
         }

         if (!hasMount) {
            return packet;
         } else {
            List<Packet<? super PacketListenerPlayOut>> packets = new ArrayList<>();
            boolean changed = false;

            for (Packet<? super PacketListenerPlayOut> childx : bundle.b()) {
               Packet<?> composed = compose(viewer, childx);
               changed |= composed != childx;
               packets.add((Packet<? super PacketListenerPlayOut>)composed);
            }

            return (Packet<?>)(changed ? new ClientboundBundlePacket(packets) : packet);
         }
      }
   }
}
