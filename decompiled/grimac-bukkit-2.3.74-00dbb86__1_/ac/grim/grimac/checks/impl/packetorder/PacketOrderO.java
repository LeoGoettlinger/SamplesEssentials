package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(
   name = "PacketOrderO",
   stableKey = "grim.packetorder.tick_end_order",
   description = "Sent packets after movement before the expected client tick end",
   experimental = true
)
public class PacketOrderO extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("type={packet}");
   private boolean flying;

   public PacketOrderO(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
         this.flying = false;
      }

      if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && this.player.supportsEndTick() && !this.player.packetStateData.lastPacketWasTeleport) {
         this.flying = true;
      } else {
         if (this.flying && !isAsync(event.getPacketType()) && event.getPacketType() != PacketType.Play.Client.VEHICLE_MOVE) {
            if (this.player.inVehicle() && event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
               WrapperPlayClientEntityAction.Action action = new WrapperPlayClientEntityAction(event).getAction();
               if (action == WrapperPlayClientEntityAction.Action.START_SPRINTING || action == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                  return;
               }
            }

            int packetId = VerboseCodecs.packet(event.getPacketType(), this.player.getClientVersion());
            this.flag(V.write(this.verbose()).sint(packetId));
         }
      }
   }
}
