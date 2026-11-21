package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;

public class PlayerPositionStorage {
   private final IntSet pendingPongs = new IntOpenHashSet();
   private boolean captureNextPlayerPositionPacket;
   private PlayerPositionStorage.PlayerPosition playerPosition;

   public void sendPing(UserConnection connection, int id) {
      if (!this.pendingPongs.add(id)) {
         throw new IllegalStateException("Pong already pending for id " + id);
      } else {
         PacketWrapper ping = PacketWrapper.create(ClientboundPackets1_21_2.PING, connection);
         ping.write(Types.INT, id);
         ping.send(Protocol1_21To1_21_2.class);
      }
   }

   public boolean checkPong(int id) {
      if (this.pendingPongs.remove(id)) {
         this.reset();
         this.captureNextPlayerPositionPacket = true;
         return true;
      } else {
         return false;
      }
   }

   public boolean checkCaptureNextPlayerPositionPacket() {
      if (this.captureNextPlayerPositionPacket) {
         this.captureNextPlayerPositionPacket = false;
         return true;
      } else {
         this.reset();
         return false;
      }
   }

   public void setPlayerPosition(PlayerPositionStorage.PlayerPosition playerPosition) {
      this.playerPosition = playerPosition;
   }

   public boolean checkHasPlayerPosition() {
      if (this.playerPosition != null) {
         return true;
      } else {
         this.reset();
         return false;
      }
   }

   public void sendMovePlayerPosRot(UserConnection user) {
      PacketWrapper movePlayerPosRot = PacketWrapper.create(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT, user);
      movePlayerPosRot.write(Types.DOUBLE, this.playerPosition.x);
      movePlayerPosRot.write(Types.DOUBLE, this.playerPosition.y);
      movePlayerPosRot.write(Types.DOUBLE, this.playerPosition.z);
      movePlayerPosRot.write(Types.FLOAT, this.playerPosition.yaw);
      movePlayerPosRot.write(Types.FLOAT, this.playerPosition.pitch);
      movePlayerPosRot.write(Types.BOOLEAN, this.playerPosition.onGround);
      movePlayerPosRot.sendToServer(Protocol1_21To1_21_2.class);
      this.reset();
   }

   public void reset() {
      this.captureNextPlayerPositionPacket = false;
      this.playerPosition = null;
   }

   public record PlayerPosition(double x, double y, double z, float yaw, float pitch, boolean onGround) {
   }
}
