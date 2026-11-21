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
   name = "BadPacketsF",
   stableKey = "grim.badpackets.duplicate_sprint",
   description = "Sent duplicate sprinting status"
)
public class BadPacketsF extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("state={bool}");
   public boolean lastSprinting;
   public boolean exemptNext = true;

   public BadPacketsF(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
         WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
         if (packet.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
            if (this.lastSprinting) {
               if (this.exemptNext) {
                  this.exemptNext = false;
                  return;
               }

               boolean state = true;
               if (this.flag(V.write(this.verbose()).bool(state)) && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            }

            this.lastSprinting = true;
         } else if (packet.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
            if (!this.lastSprinting) {
               if (this.exemptNext) {
                  this.exemptNext = false;
                  return;
               }

               boolean state = false;
               if (this.flag(V.write(this.verbose()).bool(state)) && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            }

            this.lastSprinting = false;
         }
      }
   }
}
