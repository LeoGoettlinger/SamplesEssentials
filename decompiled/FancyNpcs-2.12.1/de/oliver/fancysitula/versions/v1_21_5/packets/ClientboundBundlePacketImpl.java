package de.oliver.fancysitula.versions.v1_21_5.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundBundlePacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundPacket;
import de.oliver.fancysitula.versions.v1_21_5.utils.VanillaPlayerAdapter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;

public class ClientboundBundlePacketImpl extends FS_ClientboundBundlePacket {
   public ClientboundBundlePacketImpl(List<FS_ClientboundPacket> packets) {
      super(packets);
   }

   @Override
   public Object createPacket() {
      List<Packet<? super ClientGamePacketListener>> rawPackets = new ArrayList<>();

      for (Object raw : this.createRawPackets()) {
         if (raw instanceof Packet<? super ClientGamePacketListener> packet) {
            rawPackets.add(packet);
         }
      }

      return new ClientboundBundlePacket(rawPackets);
   }

   @Override
   protected void sendPacketTo(FS_RealPlayer player) {
      ClientboundBundlePacket packet = (ClientboundBundlePacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
