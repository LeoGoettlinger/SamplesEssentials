package ac.grim.grimac.checks.impl.vehicle;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;

@CheckData(
   name = "VehicleA",
   stableKey = "grim.vehicle.impossible_input",
   description = "Impossible input values"
)
public class VehicleA extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("forwards={f32}, sideways={f32}");

   public VehicleA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
         WrapperPlayClientSteerVehicle packet = new WrapperPlayClientSteerVehicle(event);
         if ((Math.abs(packet.getForward()) > 0.98F || Math.abs(packet.getSideways()) > 0.98F)
            && this.flag(V.write(this.verbose()).f32(packet.getForward()).f32(packet.getSideways()))
            && this.shouldModifyPackets()) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }
}
