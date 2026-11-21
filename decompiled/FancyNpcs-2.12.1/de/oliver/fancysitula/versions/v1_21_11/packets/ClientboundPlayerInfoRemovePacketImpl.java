package de.oliver.fancysitula.versions.v1_21_11.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundPlayerInfoRemovePacket;
import de.oliver.fancysitula.versions.v1_21_11.utils.VanillaPlayerAdapter;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.server.level.ServerPlayer;

public class ClientboundPlayerInfoRemovePacketImpl extends FS_ClientboundPlayerInfoRemovePacket {
   public ClientboundPlayerInfoRemovePacketImpl(List<UUID> uuids) {
      super(uuids);
   }

   @Override
   public Object createPacket() {
      return new ClientboundPlayerInfoRemovePacket(this.uuids);
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundPlayerInfoRemovePacket packet = (ClientboundPlayerInfoRemovePacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
