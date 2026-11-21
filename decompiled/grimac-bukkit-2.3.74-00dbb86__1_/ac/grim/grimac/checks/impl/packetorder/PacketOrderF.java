package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayDeque;

@CheckData(
   name = "PacketOrderF",
   stableKey = "grim.packetorder.input_tick_to_sneak_sprint_order",
   description = "Sent action packets after sneak or sprint input in an invalid order",
   experimental = true
)
public class PacketOrderF extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("action={str}, sprinting={bool}, sneaking={bool}");
   static final int ACTION_INTERACT = 0;
   static final int ACTION_ATTACK = 1;
   static final int ACTION_SPECTATE_ENTITY = 2;
   static final int ACTION_PLACE = 3;
   static final int ACTION_USE = 4;
   static final int ACTION_PICK = 5;
   static final int ACTION_DIG = 6;
   static final int ACTION_OPEN_INVENTORY = 7;
   private final ArrayDeque<PacketOrderF.FlagData> flags = new ArrayDeque<>();

   public PacketOrderF(GrimPlayer player) {
      super(player);
   }

   static String actionName(int action) {
      return switch (action) {
         case 0 -> "interact";
         case 1 -> "attack";
         case 2 -> "spectateEntity";
         case 3 -> "place";
         case 4 -> "use";
         case 5 -> "pick";
         case 6 -> "dig";
         case 7 -> "openInventory";
         default -> "unknown";
      };
   }

   private static int action(PacketReceiveEvent event) {
      return event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
         ? 0
         : (
            event.getPacketType() == PacketType.Play.Client.ATTACK
               ? 1
               : (
                  event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY
                     ? 2
                     : (
                        event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
                           ? 3
                           : (
                              event.getPacketType() == PacketType.Play.Client.USE_ITEM
                                 ? 4
                                 : (
                                    event.getPacketType() == PacketType.Play.Client.PICK_ITEM
                                       ? 5
                                       : (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING ? 6 : 7)
                                 )
                           )
                     )
               )
         );
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if ((
            event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
               || event.getPacketType() == PacketType.Play.Client.ATTACK
               || event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY
               || event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT
               || event.getPacketType() == PacketType.Play.Client.USE_ITEM
               || event.getPacketType() == PacketType.Play.Client.PICK_ITEM
               || event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING
               || event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
                  && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT
         )
         && (this.player.packetOrderProcessor.isSprinting() || this.player.packetOrderProcessor.isSneaking())) {
         int action = action(event);
         boolean sprinting = this.player.packetOrderProcessor.isSprinting();
         boolean sneaking = this.player.packetOrderProcessor.isSneaking();
         if (!this.player.canSkipTicks()) {
            if (this.flag(V.write(this.verbose()).str(actionName(action)).bool(sprinting).bool(sneaking)) && this.shouldModifyPackets()) {
               if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING && !this.canCancel(new WrapperPlayClientPlayerDigging(event).getAction())) {
                  return;
               }

               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.flags.add(new PacketOrderF.FlagData(action, sprinting, sneaking));
         }
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (PacketOrderF.FlagData data : this.flags) {
               this.flag(V.write(this.verbose()).str(actionName(data.action())).bool(data.sprinting()).bool(data.sneaking()));
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(int action, boolean sprinting, boolean sneaking) {
   }
}
