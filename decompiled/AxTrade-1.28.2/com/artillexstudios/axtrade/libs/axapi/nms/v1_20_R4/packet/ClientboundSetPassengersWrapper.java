package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R4.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutMount;

public class ClientboundSetPassengersWrapper {
   public static PacketPlayOutMount createNew(int ridingEntity, int[] passengerIds) {
      PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
      buf.c(ridingEntity);
      buf.a(passengerIds);
      PacketPlayOutMount packet = (PacketPlayOutMount)PacketPlayOutMount.a.decode(buf);
      buf.release();
      return packet;
   }
}
