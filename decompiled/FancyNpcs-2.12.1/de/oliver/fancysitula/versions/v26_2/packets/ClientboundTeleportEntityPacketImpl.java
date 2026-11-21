package de.oliver.fancysitula.versions.v26_2.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundTeleportEntityPacket;
import de.oliver.fancysitula.versions.v26_2.utils.VanillaPlayerAdapter;
import java.util.Set;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

public class ClientboundTeleportEntityPacketImpl extends FS_ClientboundTeleportEntityPacket {
   public ClientboundTeleportEntityPacketImpl(int entityId, double x, double y, double z, float yaw, float pitch, boolean onGround) {
      super(entityId, x, y, z, yaw, pitch, onGround);
   }

   @Override
   public Object createPacket() {
      return new ClientboundTeleportEntityPacket(
         this.entityId, new PositionMoveRotation(new Vec3(this.x, this.y, this.z), Vec3.ZERO, this.yaw, this.pitch), Set.of(), this.onGround
      );
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundTeleportEntityPacket packet = (ClientboundTeleportEntityPacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
