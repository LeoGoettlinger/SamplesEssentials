package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R3.packet;

import com.artillexstudios.axrankmenu.libs.axapi.packetentity.PacketEntity;
import java.util.Set;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;

public class ClientboundTeleportEntityWrapper {
   public static PacketPlayOutEntityTeleport createNew(PacketEntity entity, Location location) {
      return PacketPlayOutEntityTeleport.a(
         entity.id(),
         new PositionMoveRotation(
            new Vec3D(location.getX(), location.getY(), location.getZ()), Vec3D.c, location.getYaw(), location.getPitch() * 256.0F / 360.0F
         ),
         Set.of(),
         true
      );
   }
}
