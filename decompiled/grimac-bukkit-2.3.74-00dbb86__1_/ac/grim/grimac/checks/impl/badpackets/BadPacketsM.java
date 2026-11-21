package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.Combat;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCombatEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;

@CheckData(
   name = "BadPacketsM",
   stableKey = "grim.badpackets.respawn_alive",
   description = "Tried to respawn while alive",
   experimental = true
)
public class BadPacketsM extends Check implements PacketCheck {
   private int exempt;
   private boolean menu;

   public BadPacketsM(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
         && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.PERFORM_RESPAWN) {
         if (this.exempt > 0) {
            this.exempt--;
         } else {
            if (!this.player.compensatedEntities.self.isDead && !this.menu) {
               this.flag();
            }

            this.menu = this.player.compensatedEntities.self.isDead && this.player.packetStateData.showsDeathScreen;
         }
      }
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (!this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
         if (event.getPacketType() == PacketType.Play.Server.CHANGE_GAME_STATE) {
            WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(event);
            if (packet.getReason() != WrapperPlayServerChangeGameState.Reason.WIN_GAME) {
               return;
            }

            if (packet.getValue() != 0.0F && packet.getValue() != 1.0F) {
               return;
            }

            this.player.sendTransaction();
            this.player.addRealTimeTaskNow(() -> {
               this.exempt++;
               this.menu = false;
            });
         }

         if (event.getPacketType() == PacketType.Play.Server.DEATH_COMBAT_EVENT
            && new WrapperPlayServerDeathCombatEvent(event).getPlayerId() == this.player.entityID) {
            this.player.sendTransaction();
            this.player.addRealTimeTaskNow(this::onDeathCombatEvent);
         }

         if (event.getPacketType() == PacketType.Play.Server.COMBAT_EVENT) {
            WrapperPlayServerCombatEvent packetx = new WrapperPlayServerCombatEvent(event);
            if (packetx.getCombat() == Combat.ENTITY_DEAD && packetx.getPlayerId() == this.player.entityID) {
               this.player.sendTransaction();
               this.player.addRealTimeTaskNow(this::onDeathCombatEvent);
            }
         }
      }
   }

   private void onDeathCombatEvent() {
      if (this.player.packetStateData.showsDeathScreen) {
         this.menu = true;
      } else {
         this.exempt++;
      }
   }

   public void onRespawn() {
      this.menu = false;
   }

   public void onDeath() {
      if (this.player.packetStateData.showsDeathScreen) {
         this.menu = true;
      }
   }

   public void exemptVia() {
      this.exempt++;
   }
}
