package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;

public class PacketChangeGameState extends Check implements PacketCheck {
   public PacketChangeGameState(GrimPlayer playerData) {
      super(playerData);
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.CHANGE_GAME_STATE) {
         WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(event);
         switch (packet.getReason()) {
            case CHANGE_GAME_MODE:
               this.player.sendTransaction();
               this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> {
                  GameMode previous = this.player.gamemode;
                  int gamemode = (int)packet.getValue();
                  if (gamemode >= 0 && gamemode < GameMode.values().length) {
                     this.player.gamemode = GameMode.values()[gamemode];
                  } else {
                     this.player.gamemode = GameMode.SURVIVAL;
                  }

                  if (previous == GameMode.SPECTATOR && this.player.gamemode != GameMode.SPECTATOR) {
                     GrimAPI.INSTANCE.getSpectateManager().handlePlayerStopSpectating(this.player.uuid);
                  }
               });
               break;
            case ENABLE_RESPAWN_SCREEN:
               if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_15) || event.getServerVersion().isOlderThan(ServerVersion.V_1_15)) {
                  return;
               }

               this.player.sendTransaction();
               boolean enabled = packet.getValue() == 0.0F;
               this.player.addRealTimeTaskNow(() -> this.player.packetStateData.showsDeathScreen = enabled);
         }
      }
   }
}
