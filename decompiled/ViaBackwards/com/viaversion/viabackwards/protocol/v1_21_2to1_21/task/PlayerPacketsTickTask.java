package com.viaversion.viabackwards.protocol.v1_21_2to1_21.task;

import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.Protocol1_21_2To1_21;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.BackwardsStorables1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.PlayerStorage;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.connection.StorableObjectTask;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import java.util.logging.Level;

public final class PlayerPacketsTickTask extends StorableObjectTask<PlayerStorage> {
   public PlayerPacketsTickTask() {
      super(PlayerStorage.class);
   }

   public void run(UserConnection connection, PlayerStorage storableObject) {
      ProtocolInfo protocolInfo = connection.getProtocolInfo();
      if (protocolInfo.getClientState() == State.PLAY && protocolInfo.getServerState() == State.PLAY) {
         Protocol1_21_2To1_21 protocol = (Protocol1_21_2To1_21)Via.getManager().getProtocolManager().getProtocol(Protocol1_21_2To1_21.class);
         EntityTracker entityTracker = connection.getEntityTracker(protocol);
         if (entityTracker.hasClientEntityId()) {
            try {
               BackwardsStorables1_21_2 storables = (BackwardsStorables1_21_2)connection.storables(protocol);
               if (storables.clientVehicleStorage() == null) {
                  storableObject.tick(connection);
               }
            } catch (Throwable var8) {
               ViaBackwards.getPlatform().getLogger().log(Level.SEVERE, "Error while sending player input packet.", var8);
            }

            try {
               if (storableObject.endTick()) {
                  PacketWrapper clientTickEndPacket = PacketWrapper.create(ServerboundPackets1_21_2.CLIENT_TICK_END, connection);
                  clientTickEndPacket.sendToServer(Protocol1_21_2To1_21.class);
               }
            } catch (Throwable var7) {
               ViaBackwards.getPlatform().getLogger().log(Level.SEVERE, "Error while sending client tick end packet.", var7);
            }
         }
      }
   }
}
