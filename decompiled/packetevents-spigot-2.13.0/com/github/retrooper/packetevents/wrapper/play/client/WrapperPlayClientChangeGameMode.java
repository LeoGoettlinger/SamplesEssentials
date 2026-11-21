package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientChangeGameMode extends PacketWrapper<WrapperPlayClientChangeGameMode> {
   private GameMode gameMode;

   public WrapperPlayClientChangeGameMode(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientChangeGameMode(GameMode gameMode) {
      super(PacketType.Play.Client.CHANGE_GAME_MODE);
      this.gameMode = gameMode;
   }

   @Override
   public void read() {
      this.gameMode = GameMode.getById(this.readVarInt());
   }

   @Override
   public void write() {
      this.writeVarInt(this.gameMode.getId());
   }

   public void copy(WrapperPlayClientChangeGameMode wrapper) {
      this.gameMode = wrapper.gameMode;
   }

   public GameMode getGameMode() {
      return this.gameMode;
   }

   public void setGameMode(GameMode gamemode) {
      this.gameMode = gamemode;
   }
}
