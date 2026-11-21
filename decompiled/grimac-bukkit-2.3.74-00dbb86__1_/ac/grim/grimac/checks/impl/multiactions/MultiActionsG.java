package ac.grim.grimac.checks.impl.multiactions;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;

@CheckData(
   name = "MultiActionsG",
   stableKey = "grim.multiactions.action_while_rowing",
   description = "Attacking or using items while rowing a boat",
   experimental = true
)
public class MultiActionsG extends BlockPlaceCheck {
   private static final Verbose V = Verbose.of("action=interact").or("action=attack").or("action=spectateEntity").or("action=use").or("action=place");
   private static final int ACTION_INTERACT = 0;
   private static final int ACTION_ATTACK = 1;
   private static final int ACTION_SPECTATE_ENTITY = 2;
   private static final int ACTION_USE = 3;
   private static final int ACTION_PLACE = 4;

   public MultiActionsG(GrimPlayer player) {
      super(player);
   }

   private Verbose.Writer writeAction(int action) {
      return V.write(this.verbose(), action);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
         && this.isCheckActive()
         && this.flag(this.writeAction(0))
         && this.shouldModifyPackets()) {
         event.setCancelled(true);
         this.player.onPacketCancel();
      }

      if (event.getPacketType() == PacketType.Play.Client.ATTACK && this.isCheckActive() && this.flag(this.writeAction(1)) && this.shouldModifyPackets()) {
         event.setCancelled(true);
         this.player.onPacketCancel();
      }

      if (event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY
         && this.isCheckActive()
         && this.flag(this.writeAction(2))
         && this.shouldModifyPackets()) {
         event.setCancelled(true);
         this.player.onPacketCancel();
      }

      if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && this.isCheckActive() && this.flag(this.writeAction(3)) && this.shouldModifyPackets()) {
         event.setCancelled(true);
         this.player.onPacketCancel();
      }
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      int action = place.getFace() == BlockFace.OTHER ? 3 : 4;
      if (this.isCheckActive() && this.flag(this.writeAction(action)) && this.shouldModifyPackets() && this.shouldCancel()) {
         place.resync();
      }
   }

   public boolean isCheckActive() {
      return this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
         && !this.player.vehicleData.wasVehicleSwitch
         && this.player.inVehicle()
         && this.player.compensatedEntities.self.getRiding().getType().isInstanceOf(EntityTypes.BOAT)
         && (this.player.vehicleData.nextVehicleForward != 0.0F || this.player.vehicleData.nextVehicleHorizontal != 0.0F);
   }
}
