package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.event.events.GrimTransactionReceivedEvent;
import ac.grim.grimac.api.event.events.GrimTransactionSendEvent;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import ac.grim.grimac.utils.data.ShortToLongPair;

public class PacketPingListener extends PacketListenerAbstract {
   private static final GrimTransactionSendEvent.Channel SEND_CHANNEL = GrimAPI.INSTANCE.getEventBus().get(GrimTransactionSendEvent.class);
   private static final GrimTransactionReceivedEvent.Channel RECEIVED_CHANNEL = GrimAPI.INSTANCE.getEventBus().get(GrimTransactionReceivedEvent.class);

   public PacketPingListener() {
      super(PacketListenerPriority.LOWEST);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.packetStateData.lastTransactionPacketWasValid = false;
         WrapperPlayClientWindowConfirmation transaction = new WrapperPlayClientWindowConfirmation(event);
         short id = transaction.getActionId();
         if (id <= 0 && player.addTransactionResponse(id)) {
            player.packetStateData.lastTransactionPacketWasValid = true;
            boolean shouldCancel = !GrimAPI.INSTANCE.getConfigManager().isDisablePongCancelling();
            event.setCancelled(shouldCancel);
            RECEIVED_CHANNEL.fire(player, id, shouldCancel, event.getTimestamp());
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.PONG) {
         WrapperPlayClientPong pong = new WrapperPlayClientPong(event);
         GrimPlayer playerx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (playerx == null) {
            return;
         }

         playerx.packetStateData.lastTransactionPacketWasValid = false;
         int id = pong.getId();
         if (id == (short)id) {
            short shortID = (short)id;
            if (playerx.addTransactionResponse(shortID)) {
               playerx.packetStateData.lastTransactionPacketWasValid = true;
               boolean shouldCancel = !GrimAPI.INSTANCE.getConfigManager().isDisablePongCancelling();
               event.setCancelled(shouldCancel);
               RECEIVED_CHANNEL.fire(playerx, id, shouldCancel, event.getTimestamp());
            }
         }
      }
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
         WrapperPlayServerWindowConfirmation confirmation = new WrapperPlayServerWindowConfirmation(event);
         short id = confirmation.getActionId();
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.packetStateData.lastServerTransWasValid = false;
         if (id <= 0 && player.didWeSendThatTrans.remove(id)) {
            player.packetStateData.lastServerTransWasValid = true;
            player.transactionsSent.add(new ShortToLongPair(id, System.nanoTime()));
            player.lastTransactionSent.getAndIncrement();
            SEND_CHANNEL.fire(player, id, event.getTimestamp());
         }
      }

      if (event.getPacketType() == PacketType.Play.Server.PING) {
         WrapperPlayServerPing pong = new WrapperPlayServerPing(event);
         int idx = pong.getId();
         GrimPlayer playerx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (playerx == null) {
            return;
         }

         playerx.packetStateData.lastServerTransWasValid = false;
         if (idx == (short)idx) {
            short shortID = (short)idx;
            if (playerx.didWeSendThatTrans.remove(shortID)) {
               playerx.packetStateData.lastServerTransWasValid = true;
               playerx.transactionsSent.add(new ShortToLongPair(shortID, System.nanoTime()));
               playerx.lastTransactionSent.getAndIncrement();
               SEND_CHANNEL.fire(playerx, idx, event.getTimestamp());
            }
         }
      }
   }
}
