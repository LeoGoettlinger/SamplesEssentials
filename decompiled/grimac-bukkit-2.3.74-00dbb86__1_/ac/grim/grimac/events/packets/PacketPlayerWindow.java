package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import ac.grim.grimac.utils.data.packetentity.PacketEntitySelf;
import ac.grim.grimac.utils.inventory.InventoryDesyncStatus;

public class PacketPlayerWindow extends PacketListenerAbstract {
   public PacketPlayerWindow() {
      super(PacketListenerPriority.LOWEST);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !event.isCancelled()) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         if (player.hasInventoryOpen && this.isNearNetherPortal(player)) {
            this.handleInventoryClose(player, InventoryDesyncStatus.NETHER_PORTAL);
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
         WrapperPlayClientClientStatus wrapper = new WrapperPlayClientClientStatus(event);
         if (wrapper.getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT) {
            GrimPlayer playerx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (playerx == null) {
               return;
            }

            this.handleInventoryOpen(playerx);
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
         GrimPlayer playerx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (playerx == null) {
            return;
         }

         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
            && playerx.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
            this.handleInventoryOpen(playerx);
         }

         if (playerx.getClientVersion().isNewerThan(ClientVersion.V_1_8)) {
            this.handleInventoryOpen(playerx);
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
         GrimPlayer playerxx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (playerxx == null) {
            return;
         }

         this.handleInventoryClose(playerxx, InventoryDesyncStatus.NOT_DESYNCED);
      }
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.sendTransaction();
         player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> this.handleInventoryClose(player, InventoryDesyncStatus.NOT_DESYNCED));
      } else if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
         WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(event);
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.sendTransaction();
         String legacyType = wrapper.getLegacyType();
         int modernType = wrapper.getType();
         InventoryDesyncStatus inventoryDesyncStatus = this.getContainerDesyncStatus(player, legacyType, modernType);
         player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
            if (inventoryDesyncStatus == InventoryDesyncStatus.NOT_DESYNCED) {
               this.handleInventoryOpen(player);
            } else {
               this.handleInventoryClose(player, inventoryDesyncStatus);
            }
         });
      } else if (event.getPacketType() == PacketType.Play.Server.OPEN_HORSE_WINDOW) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.sendTransaction();
         player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> this.handleInventoryOpen(player));
      } else if (event.getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         player.sendTransaction();
         player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> this.handleInventoryClose(player, InventoryDesyncStatus.NOT_DESYNCED));
      }
   }

   private void handleInventoryOpen(GrimPlayer player) {
      if (!player.hasInventoryOpen) {
         player.lastInventoryOpen = System.currentTimeMillis();
      }

      player.hasInventoryOpen = true;
   }

   private void handleInventoryClose(GrimPlayer player, InventoryDesyncStatus desyncStatus) {
      player.hasInventoryOpen = false;
      player.inventoryDesyncStatus = desyncStatus;
   }

   public InventoryDesyncStatus getContainerDesyncStatus(GrimPlayer player, String legacyType, int modernType) {
      if (!player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) || !"minecraft:beacon".equals(legacyType) && modernType != 8) {
         return this.isNearNetherPortal(player)
            ? (player.inventoryDesyncStatus = InventoryDesyncStatus.NETHER_PORTAL)
            : (player.inventoryDesyncStatus = InventoryDesyncStatus.NOT_DESYNCED);
      } else {
         return player.inventoryDesyncStatus = InventoryDesyncStatus.BEACON;
      }
   }

   public boolean isNearNetherPortal(GrimPlayer player) {
      if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_1) && player.pointThreeEstimator.isNearNetherPortal) {
         PacketEntitySelf playerEntity = player.compensatedEntities.self;
         return !playerEntity.inVehicle() && playerEntity.passengers.isEmpty();
      } else {
         return false;
      }
   }
}
