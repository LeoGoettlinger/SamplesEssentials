package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;

@CheckData(
   name = "BadPacketsX",
   stableKey = "grim.badpackets.extra_input_actions",
   description = "Sent duplicate sneak or sprint input actions before the next movement packet",
   experimental = true
)
public class BadPacketsX extends Check implements PostPredictionCheck {
   private boolean sprint;
   private boolean sneak;
   private int flags;

   public BadPacketsX(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (!this.player.canSkipTicks()) {
         if (this.flags > 0) {
            this.setbackIfAboveSetbackVL();
         }

         this.flags = 0;
      } else {
         if (this.player.isTickingReliablyFor(3)) {
            while (this.flags > 0) {
               this.flagWithSetback();
               this.flags--;
            }
         }

         this.flags = 0;
      }
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.player.cameraEntity.isSelf() && !this.isTickPacket(event.getPacketType())) {
         if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            switch (new WrapperPlayClientEntityAction(event).getAction()) {
               case START_SNEAKING:
               case STOP_SNEAKING:
                  if (this.sneak && (this.player.canSkipTicks() || this.flag())) {
                     this.flags++;
                  }

                  this.sneak = true;
                  break;
               case START_SPRINTING:
               case STOP_SPRINTING:
                  if (this.player.inVehicle()) {
                     return;
                  }

                  if (this.sprint && (this.player.canSkipTicks() || this.flag())) {
                     this.flags++;
                  }

                  this.sprint = true;
            }
         }
      } else {
         this.sprint = this.sneak = false;
      }
   }
}
