package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(
   name = "BadPacketsQ",
   stableKey = "grim.badpackets.invalid_horse_jump",
   description = "Sent a horse jump packet with an invalid entity, action, or boost value"
)
public class BadPacketsQ extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("boost={sint}, action={entityaction}, entity={sint}");

   public BadPacketsQ(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
         WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);
         if (Math.abs(wrapper.getJumpBoost()) > 100
            || wrapper.getEntityId() != this.player.entityID
            || wrapper.getAction() != WrapperPlayClientEntityAction.Action.START_JUMPING_WITH_HORSE && wrapper.getJumpBoost() != 0) {
            int boost = wrapper.getJumpBoost();
            int action = VerboseCodecs.enumId(wrapper.getAction());
            int entity = wrapper.getEntityId();
            if (this.flag(V.write(this.verbose()).sint(boost).uint(action).sint(entity)) && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         }
      }
   }
}
