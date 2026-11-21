package ac.grim.grimac.checks.impl.timer;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(
   name = "TickTimer",
   stableKey = "grim.timer.tick",
   description = "Did not send client tick end packet",
   setback = 1.0
)
public class TickTimer extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("type=[end|flying], packets={uint}");
   private boolean receivedTickEnd = true;
   private int flyingPackets = 0;

   public TickTimer(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.player.supportsEndTick()) {
         if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !this.player.packetStateData.lastPacketWasTeleport) {
            if (!this.receivedTickEnd && this.flagWithSetback(V.write(this.verbose()).bool(false).uint(this.flyingPackets))) {
               this.handleViolation();
            }

            this.receivedTickEnd = false;
            this.flyingPackets++;
         } else if (event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
            this.receivedTickEnd = true;
            if (this.flyingPackets > 1 && this.flagWithSetback(V.write(this.verbose()).bool(true).uint(this.flyingPackets))) {
               this.handleViolation();
            }

            this.flyingPackets = 0;
         }
      }
   }

   private void handleViolation() {
      this.player.onPacketCancel();
   }
}
