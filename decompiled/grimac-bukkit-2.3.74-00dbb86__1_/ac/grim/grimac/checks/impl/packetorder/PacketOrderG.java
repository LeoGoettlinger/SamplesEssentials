package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayDeque;

@CheckData(
   name = "PacketOrderG",
   stableKey = "grim.packetorder.hotbar_inventory_manage_order",
   description = "Managed hotbar or inventory while performing another conflicting action",
   experimental = true
)
public class PacketOrderG extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("action={str}, attacking={bool}, releasing={bool}, rightClicking={bool}, picking={bool}, digging={bool}");
   static final int ACTION_OPEN_INVENTORY = 0;
   static final int ACTION_SWAP = 1;
   static final int ACTION_DROP = 2;
   private final ArrayDeque<PacketOrderG.FlagData> flags = new ArrayDeque<>();

   public PacketOrderG(GrimPlayer player) {
      super(player);
   }

   static String actionName(int action) {
      return switch (action) {
         case 0 -> "openInventory";
         case 1 -> "swap";
         case 2 -> "drop";
         default -> "unknown";
      };
   }

   private static int action(DiggingAction action) {
      return action == null ? 0 : (action == DiggingAction.SWAP_ITEM_WITH_OFFHAND ? 1 : 2);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING
         || event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
            && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT) {
         DiggingAction action = null;
         if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            action = new WrapperPlayClientPlayerDigging(event).getAction();
            if (action != DiggingAction.SWAP_ITEM_WITH_OFFHAND && action != DiggingAction.DROP_ITEM && action != DiggingAction.DROP_ITEM_STACK) {
               return;
            }
         }

         if (this.player.packetOrderProcessor.isAttackingOrStabbing()
            || this.player.packetOrderProcessor.isReleasing()
            || this.player.packetOrderProcessor.isRightClicking()
            || this.player.packetOrderProcessor.isPicking()
            || this.player.packetOrderProcessor.isDigging()) {
            int actionKind = action(action);
            boolean attacking = this.player.packetOrderProcessor.isAttackingOrStabbing();
            boolean releasing = this.player.packetOrderProcessor.isReleasing();
            boolean rightClicking = this.player.packetOrderProcessor.isRightClicking();
            boolean picking = this.player.packetOrderProcessor.isPicking();
            boolean digging = this.player.packetOrderProcessor.isDigging();
            if (!this.player.canSkipTicks()) {
               if (this.flag(
                     V.write(this.verbose()).str(actionName(actionKind)).bool(attacking).bool(releasing).bool(rightClicking).bool(picking).bool(digging)
                  )
                  && this.shouldModifyPackets()
                  && this.canCancel(action)) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            } else {
               this.flags.add(new PacketOrderG.FlagData(actionKind, attacking, releasing, rightClicking, picking, digging));
            }
         }
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (PacketOrderG.FlagData data : this.flags) {
               this.flag(
                  V.write(this.verbose())
                     .str(actionName(data.action()))
                     .bool(data.attacking())
                     .bool(data.releasing())
                     .bool(data.rightClicking())
                     .bool(data.picking())
                     .bool(data.digging())
               );
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(int action, boolean attacking, boolean releasing, boolean rightClicking, boolean picking, boolean digging) {
   }
}
