package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@CheckData(
   name = "BadPacketsC",
   stableKey = "grim.badpackets.wake_not_sleeping",
   description = "Tried to wake up while not sleeping",
   experimental = true
)
public class BadPacketsC extends Check implements PacketCheck {
   public BadPacketsC(@NotNull GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION
         && new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.LEAVE_BED
         && !this.player.isInBed) {
         this.flag();
      }
   }
}
