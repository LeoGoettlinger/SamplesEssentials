package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckData(
   name = "PacketOrderB",
   stableKey = "grim.packetorder.noswing",
   description = "Did not swing for attack"
)
public class PacketOrderB extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("[pre-attack|post-attack]");
   private final boolean is1_9 = this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9);
   private boolean sentAnimationSinceLastAttack = this.player.getClientVersion().isNewerThan(ClientVersion.V_1_8);
   private boolean sentAttack;
   private boolean sentAnimation;
   private boolean sentSlotSwitch;

   public PacketOrderB(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ANIMATION && new WrapperPlayClientAnimation(event).getHand() == InteractionHand.MAIN_HAND) {
         this.sentAnimationSinceLastAttack = this.sentAnimation = true;
         this.sentAttack = this.sentSlotSwitch = false;
      } else {
         if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
               this.onAttack(event);
               return;
            }
         }

         if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
            this.onAttack(event);
         } else {
            if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
               WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
               if (packet.getAction() == DiggingAction.STAB) {
                  this.onAttack(event);
                  return;
               }
            }

            if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE && !this.is1_9 && !this.sentSlotSwitch) {
               this.sentSlotSwitch = true;
            } else {
               if (!isAsync(event.getPacketType())) {
                  if (this.sentAttack && this.is1_9) {
                     this.flag(V.write(this.verbose()).bool(false));
                  }

                  this.sentAttack = this.sentAnimation = this.sentSlotSwitch = false;
               }
            }
         }
      }
   }

   private void onAttack(PacketReceiveEvent event) {
      if (this.player.gamemode != GameMode.SPECTATOR || !this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_11)) {
         this.sentAttack = true;
         if (this.is1_9 ? !this.sentAnimationSinceLastAttack : !this.sentAnimation) {
            this.sentAttack = false;
            if (this.flag(V.write(this.verbose()).bool(true)) && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         }

         this.sentAnimationSinceLastAttack = this.sentAnimation = this.sentSlotSwitch = false;
      }
   }
}
