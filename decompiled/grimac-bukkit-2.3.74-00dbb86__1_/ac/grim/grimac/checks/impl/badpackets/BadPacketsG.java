package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(
   name = "BadPacketsG",
   stableKey = "grim.badpackets.duplicate_sneak",
   description = "Sent duplicate sneaking status"
)
public class BadPacketsG extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("state={bool}");
   private boolean lastSneaking;
   private boolean respawn;

   public BadPacketsG(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
         WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
         if (packet.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
            if (this.lastSneaking && !this.respawn) {
               boolean state = true;
               if (this.flag(V.write(this.verbose()).bool(state)) && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            } else {
               this.lastSneaking = true;
            }

            this.respawn = false;
         } else if (packet.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
            if (!this.lastSneaking && !this.respawn) {
               boolean state = false;
               if (this.flag(V.write(this.verbose()).bool(state)) && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            } else {
               this.lastSneaking = false;
            }

            this.respawn = false;
         }
      }
   }

   public void handleRespawn() {
      this.respawn = true;
   }
}
