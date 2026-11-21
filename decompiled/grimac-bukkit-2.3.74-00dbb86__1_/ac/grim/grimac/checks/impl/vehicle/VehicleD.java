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
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(
   name = "VehicleD",
   stableKey = "grim.vehicle.spoofed_jump",
   experimental = true,
   description = "Jumped in a vehicle that cannot jump"
)
public class VehicleD extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("vehicle=[{entity}|null]");

   public VehicleD(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION
         && new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE) {
         EntityType vehicle = this.player.getVehicleType();
         if (!EntityTypes.isTypeInstanceOf(vehicle, EntityTypes.ABSTRACT_HORSE)
            && !EntityTypes.isTypeInstanceOf(vehicle, EntityTypes.ABSTRACT_NAUTILUS)
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
