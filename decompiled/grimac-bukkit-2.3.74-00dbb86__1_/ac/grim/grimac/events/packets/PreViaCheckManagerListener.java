package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.ConnectionState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public class PreViaCheckManagerListener extends PacketListenerAbstract {
   public PreViaCheckManagerListener() {
      super(PacketListenerPriority.LOW);
   }

   @Override
   public boolean isPreVia() {
      return true;
   }

   @Override
   public void onPacketReceive(@NotNull PacketReceiveEvent event) {
      if (event.getConnectionState() == ConnectionState.CONFIGURATION) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.checkManager.onPreViaPacketReceive(event);
      }

      if (event.getConnectionState() == ConnectionState.PLAY) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player != null) {
            player.checkManager.onPreViaPacketReceive(event);
         }
      }
   }

   @Override
   public void onPacketSend(@NotNull PacketSendEvent event) {
      if (event.getConnectionState() == ConnectionState.CONFIGURATION) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.checkManager.onPreViaPacketSend(event);
      }

      if (event.getConnectionState() == ConnectionState.PLAY) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player != null) {
            PacketTypeCommon packetType = event.getPacketType();
            if (packetType == PacketType.Play.Server.OPEN_WINDOW || packetType == PacketType.Play.Server.OPEN_HORSE_WINDOW) {
               player.sendTransaction();
               player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.serverOpenedInventoryThisTick = true);
            }

            if (packetType == PacketType.Play.Server.BUNDLE) {
               player.packetStateData.sendingBundlePacket = !player.packetStateData.sendingBundlePacket;
            }

            player.checkManager.onPreViaPacketSend(event);
         }
      }
   }
}
