package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerResetScore extends PacketWrapper<WrapperPlayServerResetScore> {
   private String targetName;
   @Nullable
   private String objective;

   public WrapperPlayServerResetScore(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerResetScore(String targetName, @Nullable String objective) {
      super(PacketType.Play.Server.RESET_SCORE);
      this.targetName = targetName;
      this.objective = objective;
   }

   @Override
   public void read() {
      this.targetName = this.readString();
      this.objective = this.readOptional(PacketWrapper::readString);
   }

   @Override
   public void write() {
      this.writeString(this.targetName);
      this.writeOptional(this.objective, PacketWrapper::writeString);
   }

   public void copy(WrapperPlayServerResetScore wrapper) {
      this.targetName = wrapper.targetName;
      this.objective = wrapper.objective;
   }

   public String getTargetName() {
      return this.targetName;
   }

   public void setTargetName(String targetName) {
      this.targetName = targetName;
   }

   @Nullable
   public String getObjective() {
      return this.objective;
   }

   public void setObjective(@Nullable String objective) {
      this.objective = objective;
   }
}
