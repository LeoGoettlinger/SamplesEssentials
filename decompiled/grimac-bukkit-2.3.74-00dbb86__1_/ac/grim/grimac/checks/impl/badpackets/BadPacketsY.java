package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;

@CheckData(
   name = "BadPacketsY",
   stableKey = "grim.badpackets.oob_slot",
   description = "Sent out of bounds slot id"
)
public class BadPacketsY extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("slot={sint}");

   public BadPacketsY(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
         int slot = new WrapperPlayClientHeldItemChange(event).getSlot();
         if ((slot > 8 || slot < 0) && this.flag(V.write(this.verbose()).sint(slot)) && this.shouldModifyPackets()) {
            event.setCancelled(true);
            this.player.onPacketCancel();
         }
      }
   }
}
