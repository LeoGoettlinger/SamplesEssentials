package de.oliver.fancysitula.versions.v26_1.packets;

import de.oliver.fancysitula.api.entities.FS_RealPlayer;
import de.oliver.fancysitula.api.packets.FS_ClientboundRemoveEntitiesPacket;
import de.oliver.fancysitula.versions.v26_1.utils.VanillaPlayerAdapter;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;

public class ClientboundRemoveEntitiesPacketImpl extends FS_ClientboundRemoveEntitiesPacket {
   public ClientboundRemoveEntitiesPacketImpl(List<Integer> entityIds) {
      super(entityIds);
   }

   @Override
   public Object createPacket() {
      int[] ids = new int[this.entityIds.size()];

      for (int i = 0; i < this.entityIds.size(); i++) {
         ids[i] = this.entityIds.get(i);
      }

      return new ClientboundRemoveEntitiesPacket(ids);
   }

   @Override
   public void sendPacketTo(FS_RealPlayer player) {
      ClientboundRemoveEntitiesPacket packet = (ClientboundRemoveEntitiesPacket)this.createPacket();
      ServerPlayer vanillaPlayer = VanillaPlayerAdapter.asVanilla(player.getBukkitPlayer());
      vanillaPlayer.connection.send(packet);
   }
}
