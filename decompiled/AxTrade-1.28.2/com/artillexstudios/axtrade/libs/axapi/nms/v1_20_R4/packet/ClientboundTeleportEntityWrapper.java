package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R4.packet;

import com.artillexstudios.axtrade.libs.axapi.packetentity.PacketEntity;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import org.bukkit.Location;

public class ClientboundTeleportEntityWrapper {
   public static PacketPlayOutEntityTeleport createNew(PacketEntity entity, Location location) {
      PacketDataSerializer buf = PacketTransformer.newByteBuf().buf();
      buf.c(entity.id());
      buf.a(location.getX());
      buf.a(location.getY());
      buf.a(location.getZ());
      buf.k((byte)(location.getYaw() * 256.0F / 360.0F));
      buf.k((byte)(location.getPitch() * 256.0F / 360.0F));
      buf.a(true);
      return (PacketPlayOutEntityTeleport)PacketPlayOutEntityTeleport.a.decode(buf);
   }
}
