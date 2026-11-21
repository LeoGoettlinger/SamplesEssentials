package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayDeque;

@CheckData(
   name = "PacketOrderK",
   stableKey = "grim.packetorder.inventory_open_order",
   description = "Opened, clicked, or closed inventory in the wrong packet order",
   experimental = true
)
public class PacketOrderK extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("open, clicking={bool}, closing={bool}").or("click").or("close");
   static final int KIND_OPEN = 0;
   static final int KIND_CLICK = 1;
   static final int KIND_CLOSE = 2;
   private final ArrayDeque<PacketOrderK.FlagData> flags = new ArrayDeque<>();

   public PacketOrderK(GrimPlayer player) {
      super(player);
   }

   private Verbose.Writer write(int kind, boolean clicking, boolean closing) {
      Verbose.Writer writer = V.write(this.verbose(), kind);
      if (kind == 0) {
         writer.bool(clicking).bool(closing);
      }

      return writer;
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
         && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT
         && (this.player.packetOrderProcessor.isClickingInInventory() || this.player.packetOrderProcessor.isClosingInventory())) {
         boolean clicking = this.player.packetOrderProcessor.isClickingInInventory();
         boolean closing = this.player.packetOrderProcessor.isClosingInventory();
         if (!this.player.canSkipTicks()) {
            this.flag(this.write(0, clicking, closing));
         } else {
            this.flags.add(new PacketOrderK.FlagData(0, clicking, closing));
         }
      }

      if ((event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW || event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW)
         && this.player.packetOrderProcessor.isOpeningInventory()) {
         int kind = event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW ? 1 : 2;
         if (!this.player.canSkipTicks()) {
            if (this.flag(this.write(kind, false, false)) && this.shouldModifyPackets() && event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.flags.add(new PacketOrderK.FlagData(kind, false, false));
         }
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (PacketOrderK.FlagData data : this.flags) {
               this.flag(this.write(data.kind(), data.clicking(), data.closing()));
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(int kind, boolean clicking, boolean closing) {
   }
}
