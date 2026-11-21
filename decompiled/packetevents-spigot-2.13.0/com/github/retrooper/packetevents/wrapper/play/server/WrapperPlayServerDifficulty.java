package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.Difficulty;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerDifficulty extends PacketWrapper<WrapperPlayServerDifficulty> {
   private Difficulty difficulty;
   private boolean locked;

   public WrapperPlayServerDifficulty(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerDifficulty(Difficulty difficulty, boolean locked) {
      super(PacketType.Play.Server.SERVER_DIFFICULTY);
      this.difficulty = difficulty;
      this.locked = locked;
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
         this.difficulty = this.readEnum(Difficulty.class);
      } else {
         this.difficulty = Difficulty.getById(this.readUnsignedByte());
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14)) {
         this.locked = this.readBoolean();
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_6)) {
         this.writeEnum(this.difficulty);
      } else {
         this.writeByte(this.difficulty.getId());
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14)) {
         this.writeBoolean(this.locked);
      }
   }

   public void copy(WrapperPlayServerDifficulty wrapper) {
      this.difficulty = wrapper.difficulty;
      this.locked = wrapper.locked;
   }

   public Difficulty getDifficulty() {
      return this.difficulty;
   }

   public void setDifficulty(Difficulty difficulty) {
      this.difficulty = difficulty;
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean locked) {
      this.locked = locked;
   }
}
