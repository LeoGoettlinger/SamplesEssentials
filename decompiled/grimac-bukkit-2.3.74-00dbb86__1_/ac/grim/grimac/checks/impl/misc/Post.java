package ac.grim.grimac.checks.impl.misc;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.lists.EvictingQueue;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;

@CheckData(
   name = "Post",
   stableKey = "grim.post.invalid_order",
   description = "Sent packets out of the expected movement tick order"
)
public class Post extends Check implements PacketCheck, PostPredictionCheck {
   private static final Verbose V = Verbose.of("{str}");
   private final ArrayDeque<PacketTypeCommon> post = new ArrayDeque<>();
   private final List<String> flags = new EvictingQueue<>(10);
   private boolean sentFlying = false;
   private int isExemptFromSwingingCheck = Integer.MIN_VALUE;

   public Post(GrimPlayer playerData) {
      super(playerData);
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (!this.flags.isEmpty()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (String flag : this.flags) {
               this.flag(V.write(this.verbose()).str(flag));
            }
         }

         this.flags.clear();
      }
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.ENTITY_ANIMATION) {
         WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(event);
         if (animation.getEntityId() == this.player.entityID
            && (
               animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM
                  || animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND
            )) {
            this.isExemptFromSwingingCheck = this.player.lastTransactionSent.get();
         }
      }
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isTickPacket(event.getPacketType())) {
         this.post.clear();
         this.sentFlying = true;
      } else {
         PacketTypeCommon packetType = event.getPacketType();
         if (isTransaction(packetType) && this.player.packetStateData.lastTransactionPacketWasValid) {
            if (this.sentFlying && !this.post.isEmpty()) {
               this.flags
                  .add(this.post.getFirst().toString().toLowerCase(Locale.ROOT).replace("_", " ") + " v" + this.player.getClientVersion().getReleaseName());
            }

            this.post.clear();
            this.sentFlying = false;
         } else if (this.sentFlying) {
            if (PacketType.Play.Client.PLAYER_ABILITIES.equals(packetType)
               || PacketType.Play.Client.HELD_ITEM_CHANGE.equals(packetType) && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)
               || PacketType.Play.Client.INTERACT_ENTITY.equals(packetType)
               || PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT.equals(packetType)
               || PacketType.Play.Client.ATTACK.equals(packetType)
               || PacketType.Play.Client.SPECTATE_ENTITY.equals(packetType)
               || PacketType.Play.Client.USE_ITEM.equals(packetType)
               || PacketType.Play.Client.PLAYER_DIGGING.equals(packetType)) {
               this.post.add(event.getPacketType());
            } else if (PacketType.Play.Client.CLICK_WINDOW.equals(packetType) && this.player.getClientVersion().isOlderThan(ClientVersion.V_1_13)) {
               this.post.add(event.getPacketType());
            } else if (PacketType.Play.Client.ANIMATION.equals(packetType)
               && (
                  this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                     || PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)
               )
               && this.player.getClientVersion().isOlderThan(ClientVersion.V_1_13)
               && this.isExemptFromSwingingCheck < this.player.lastTransactionReceived.get()) {
               this.post.add(event.getPacketType());
            } else if (PacketType.Play.Client.ENTITY_ACTION.equals(packetType)
               && (
                  this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
                     || new WrapperPlayClientEntityAction(event).getAction() != WrapperPlayClientEntityAction.Action.START_FLYING_WITH_ELYTRA
               )) {
               if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_3) && this.player.inVehicle()) {
                  return;
               }

               if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)
                  && new WrapperPlayClientEntityAction(event).getAction() == WrapperPlayClientEntityAction.Action.LEAVE_BED) {
                  return;
               }

               this.post.add(event.getPacketType());
            }
         }
      }
   }
}
