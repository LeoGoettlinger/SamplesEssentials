package ac.grim.grimac.checks.impl.vehicle;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(
   name = "VehicleE",
   stableKey = "grim.vehicle.spoofed_boat",
   experimental = true,
   description = "Sent boat paddle states while not in a boat"
)
public class VehicleE extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("vehicle=[{entity}|null]");

   public VehicleE(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.STEER_BOAT) {
         EntityType vehicle = this.player.getVehicleType();
         if (!EntityTypes.isTypeInstanceOf(vehicle, EntityTypes.BOAT)
            && this.flag(
               V.write(this.verbose()).bool(vehicle != null).uint(vehicle == null ? 0 : VerboseCodecs.entity(vehicle, this.player.getClientVersion()))
            )
            && this.shouldModifyPackets()) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }
}
