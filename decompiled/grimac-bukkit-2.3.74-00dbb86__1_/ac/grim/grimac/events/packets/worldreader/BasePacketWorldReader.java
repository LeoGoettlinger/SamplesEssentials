package ac.grim.grimac.events.packets.worldreader;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.events.packets.worldreader.multiblockchange.LegacyMultiBlockChangeHandler;
import ac.grim.grimac.events.packets.worldreader.multiblockchange.V1160MultiBlockChangeBitRepackHandler;
import ac.grim.grimac.events.packets.worldreader.multiblockchange.VersionedMultiBlockChangeHandler;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgePlayerDigging;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkDataBulk;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import ac.grim.grimac.utils.chunks.Column;
import ac.grim.grimac.utils.data.TeleportData;

public class BasePacketWorldReader extends PacketListenerAbstract {
   private static final VersionedMultiBlockChangeHandler versionedMultiBlockChangeHandler = (VersionedMultiBlockChangeHandler)(PacketEvents.getAPI()
         .getServerManager()
         .getVersion()
         .isNewerThanOrEquals(ServerVersion.V_1_16)
      ? new V1160MultiBlockChangeBitRepackHandler()
      : new LegacyMultiBlockChangeHandler());

   public BasePacketWorldReader() {
      super(PacketListenerPriority.HIGH);
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.UNLOAD_CHUNK) {
         WrapperPlayServerUnloadChunk unloadChunk = new WrapperPlayServerUnloadChunk(event);
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         this.unloadChunk(player, unloadChunk.getChunkX(), unloadChunk.getChunkZ());
      }

      if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK_BULK) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         this.handleMapChunkBulk(player, event);
      }

      if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         this.handleMapChunk(player, event);
      }

      if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         this.handleBlockChange(player, event);
      }

      if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         this.handleMultiBlockChange(player, event);
      }

      if (event.getPacketType() == PacketType.Play.Server.ACKNOWLEDGE_BLOCK_CHANGES) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         WrapperPlayServerAcknowledgeBlockChanges changes = new WrapperPlayServerAcknowledgeBlockChanges(event);
         player.compensatedWorld.handlePredictionConfirmation(changes.getSequence());
      }

      if (event.getPacketType() == PacketType.Play.Server.ACKNOWLEDGE_PLAYER_DIGGING) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         WrapperPlayServerAcknowledgePlayerDigging ack = new WrapperPlayServerAcknowledgePlayerDigging(event);
         player.compensatedWorld.handleBlockBreakAck(ack.getBlockPosition(), ack.getBlockId(), ack.getAction(), ack.isSuccessful());
      }

      if (event.getPacketType() == PacketType.Play.Server.CHANGE_GAME_STATE) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         WrapperPlayServerChangeGameState newState = new WrapperPlayServerChangeGameState(event);
         player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
            if (newState.getReason() == WrapperPlayServerChangeGameState.Reason.BEGIN_RAINING) {
               player.compensatedWorld.isRaining = true;
            } else if (newState.getReason() == WrapperPlayServerChangeGameState.Reason.END_RAINING) {
               player.compensatedWorld.isRaining = false;
            } else if (newState.getReason() == WrapperPlayServerChangeGameState.Reason.RAIN_LEVEL_CHANGE) {
               player.compensatedWorld.isRaining = newState.getValue() > 0.2F;
            }
         });
      }
   }

   public void handleMapChunkBulk(GrimPlayer player, PacketSendEvent event) {
      WrapperPlayServerChunkDataBulk chunkData = new WrapperPlayServerChunkDataBulk(event);

      for (int i = 0; i < chunkData.getChunks().length; i++) {
         this.addChunkToCache(event, player, chunkData.getChunks()[i], true, chunkData.getX()[i], chunkData.getZ()[i], null);
      }
   }

   public void handleMapChunk(GrimPlayer player, PacketSendEvent event) {
      WrapperPlayServerChunkData chunkData = new WrapperPlayServerChunkData(event);
      this.addChunkToCache(
         event,
         player,
         chunkData.getColumn().getChunks(),
         chunkData.getColumn().isFullChunk(),
         chunkData.getColumn().getX(),
         chunkData.getColumn().getZ(),
         null
      );
      event.setLastUsedWrapper(null);
   }

   public void addChunkToCache(
      PacketSendEvent event, GrimPlayer player, BaseChunk[] chunks, boolean isGroundUp, int chunkX, int chunkZ, TileEntity[] tileEntities
   ) {
      double chunkCenterX = (chunkX << 4) + 8;
      double chunkCenterZ = (chunkZ << 4) + 8;
      boolean shouldPostTrans = Math.abs(player.x - chunkCenterX) < 16.0 && Math.abs(player.z - chunkCenterZ) < 16.0;

      for (TeleportData teleports : player.getSetbackTeleportUtil().pendingTeleports) {
         if (teleports.getFlags().getMask() == 0) {
            shouldPostTrans = shouldPostTrans
               || Math.abs(teleports.getLocation().getX() - chunkCenterX) < 16.0 && Math.abs(teleports.getLocation().getZ() - chunkCenterZ) < 16.0;
         }
      }

      if (shouldPostTrans) {
         event.getTasksAfterSend().add(player::sendTransaction);
      }

      if (isGroundUp) {
         Column column = new Column(chunkX, chunkZ, chunks, player.lastTransactionSent.get());
         player.compensatedWorld.addToCache(column, chunkX, chunkZ, tileEntities);
      } else {
         player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
            Column existingColumn = player.compensatedWorld.getChunk(chunkX, chunkZ);
            if (existingColumn != null) {
               existingColumn.mergeChunks(chunks);
            }
         });
      }
   }

   public void unloadChunk(GrimPlayer player, int x, int z) {
      if (player != null) {
         player.compensatedWorld.removeChunkLater(x, z);
      }
   }

   public void handleBlockChange(GrimPlayer player, PacketSendEvent event) {
      WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(event);
      int range = 16;
      Vector3i blockPosition = blockChange.getBlockPosition();
      if (Math.abs(blockPosition.getX() - player.x) < range
         && Math.abs(blockPosition.getY() - player.y) < range
         && Math.abs(blockPosition.getZ() - player.z) < range
         && player.lastTransSent + 2L < System.currentTimeMillis()) {
         player.sendTransaction();
      }

      int x = blockPosition.getX();
      int y = blockPosition.getY();
      int z = blockPosition.getZ();
      int blockId = blockChange.getBlockId();
      player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.compensatedWorld.updateBlock(x, y, z, blockId));
   }

   public void handleMultiBlockChange(GrimPlayer player, PacketSendEvent event) {
      versionedMultiBlockChangeHandler.handleMultiBlockChange(player, event);
   }
}
