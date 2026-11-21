package com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage;

import com.viaversion.viabackwards.api.entities.storage.PlayerPosRotStorage;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.Protocol1_21_2To1_21;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;

public final class PlayerStorage extends PlayerPosRotStorage {
   private static final PlayerStorage.PlayerInput EMPTY = new PlayerStorage.PlayerInput(false, false, false, false, false, false, false);
   private static final float PLAYER_JUMP_HEIGHT = 0.42F;
   private boolean playerCommandTrackedSneaking;
   private boolean playerCommandTrackedSprinting;
   private PlayerStorage.PlayerInput lastInput = EMPTY;
   private double prevX;
   private double prevY;
   private double prevZ;
   private boolean tickStarted;
   private boolean tickEndedEarly;

   public void tick(UserConnection user) {
      double deltaX = this.x() - this.prevX;
      double deltaY = this.y() - this.prevY;
      double deltaZ = this.z() - this.prevZ;
      double magnitude = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      double directionX = magnitude > 0.0 ? deltaX / magnitude : 0.0;
      double directionZ = magnitude > 0.0 ? deltaZ / magnitude : 0.0;
      directionX = Math.max(-1.0, Math.min(1.0, directionX));
      directionZ = Math.max(-1.0, Math.min(1.0, directionZ));
      double angle = Math.toRadians(-this.yRot());
      double newDirectionX = directionX * Math.cos(angle) - directionZ * Math.sin(angle);
      double newDirectionZ = directionX * Math.sin(angle) + directionZ * Math.cos(angle);
      boolean forward = newDirectionZ >= 0.65F;
      boolean backwards = newDirectionZ <= -0.65F;
      boolean left = newDirectionX >= 0.65F;
      boolean right = newDirectionX <= -0.65F;
      boolean jump = Math.abs(deltaY - 0.42F) <= 1.0E-4F;
      PlayerStorage.PlayerInput input = new PlayerStorage.PlayerInput(
         forward, backwards, left, right, jump, this.playerCommandTrackedSneaking, this.playerCommandTrackedSprinting
      );
      if (!this.lastInput.equals(input)) {
         PacketWrapper playerInputPacket = PacketWrapper.create(ServerboundPackets1_21_2.PLAYER_INPUT, user);
         byte flags = 0;
         flags = (byte)(flags | (input.forward() ? 1 : 0));
         flags = (byte)(flags | (input.backward() ? 2 : 0));
         flags = (byte)(flags | (input.left() ? 4 : 0));
         flags = (byte)(flags | (input.right() ? 8 : 0));
         flags = (byte)(flags | (input.jump() ? 16 : 0));
         flags = (byte)(flags | (input.sneak() ? 32 : 0));
         flags = (byte)(flags | (input.sprint() ? 64 : 0));
         playerInputPacket.write(Types.BYTE, flags);
         playerInputPacket.sendToServer(Protocol1_21_2To1_21.class);
         this.lastInput = input;
      }

      this.prevX = this.x();
      this.prevY = this.y();
      this.prevZ = this.z();
   }

   public void setPlayerCommandTrackedSneaking(boolean playerCommandTrackedSneaking) {
      this.playerCommandTrackedSneaking = playerCommandTrackedSneaking;
   }

   public void setPlayerCommandTrackedSprinting(boolean playerCommandTrackedSprinting) {
      this.playerCommandTrackedSprinting = playerCommandTrackedSprinting;
   }

   public boolean startTick() {
      if (!this.tickStarted) {
         this.tickStarted = true;
         return false;
      } else {
         this.tickEndedEarly = true;
         return true;
      }
   }

   public boolean endTick() {
      if (this.tickEndedEarly) {
         this.tickEndedEarly = false;
         return false;
      } else {
         this.tickStarted = false;
         return true;
      }
   }

   public boolean setSneaking(boolean sneaking) {
      boolean changed = this.playerCommandTrackedSneaking != sneaking;
      this.playerCommandTrackedSneaking = sneaking;
      return changed;
   }

   public record PlayerInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint) {
   }
}
