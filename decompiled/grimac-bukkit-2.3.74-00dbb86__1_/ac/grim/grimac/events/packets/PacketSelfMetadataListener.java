package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import ac.grim.grimac.utils.data.SprintingState;
import ac.grim.grimac.utils.nmsutil.WatchableIndexUtil;
import java.util.List;
import java.util.Optional;

public class PacketSelfMetadataListener extends PacketListenerAbstract {
   public PacketSelfMetadataListener() {
      super(PacketListenerPriority.HIGH);
   }

   @Override
   public boolean isPreVia() {
      return true;
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         WrapperPlayServerEntityMetadata entityMetadata = new WrapperPlayServerEntityMetadata(event);
         if (entityMetadata.getEntityId() != player.entityID) {
            return;
         }

         List<EntityData<?>> metadata = entityMetadata.getEntityMetadata();
         boolean hasSendTransaction = false;
         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)) {
            metadata.removeIf(element -> element.getIndex() == 6);
            entityMetadata.setEntityMetadata(metadata);
            event.markForReEncode(true);
         }

         EntityData<?> watchable = WatchableIndexUtil.getIndex(metadata, 0);
         if (watchable != null && watchable.getValue() instanceof Byte) {
            byte field = (Byte)watchable.getValue();
            boolean isGliding = (field & 128) == 128 && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9);
            boolean isSwimming = (field & 16) == 16;
            boolean isSprinting = (field & 8) == 8;
            if (!hasSendTransaction) {
               player.sendTransaction();
            }

            hasSendTransaction = true;
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
               player.isSwimming = isSwimming;
               player.lastSprinting = isSprinting;
               if (!isSprinting) {
                  player.vehicleData.camelSprintingState = SprintingState.STOPPING;
               }

               if (player.isGliding != isGliding) {
                  player.pointThreeEstimator.updatePlayerGliding();
               }

               player.isGliding = isGliding;
            });
         }

         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_10)) {
            EntityData<?> gravity = WatchableIndexUtil.getIndex(metadata, 5);
            if (gravity != null) {
               Object gravityObject = gravity.getValue();
               if (gravityObject instanceof Boolean) {
                  if (!hasSendTransaction) {
                     player.sendTransaction();
                  }

                  hasSendTransaction = true;
                  player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.playerEntityHasGravity = !(Boolean)gravityObject);
               }
            }
         }

         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) {
            EntityData<?> frozen = WatchableIndexUtil.getIndex(metadata, 7);
            if (frozen != null) {
               if (!hasSendTransaction) {
                  player.sendTransaction();
               }

               hasSendTransaction = true;
               player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.powderSnowFrozenTicks = (Integer)frozen.getValue());
            }
         }

         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)) {
            int id;
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_14_4)) {
               id = 12;
            } else if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_16_4)) {
               id = 13;
            } else {
               id = 14;
            }

            EntityData<?> bedObject = WatchableIndexUtil.getIndex(metadata, id);
            if (bedObject != null) {
               if (!hasSendTransaction) {
                  player.sendTransaction();
               }

               hasSendTransaction = true;
               player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
                  Optional<Vector3i> bed = (Optional<Vector3i>)bedObject.getValue();
                  if (bed.isPresent()) {
                     player.isInBed = true;
                     Vector3i bedPos = bed.get();
                     player.bedPosition = new Vector3d(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5);
                  } else {
                     player.isInBed = false;
                  }
               });
            }
         }

         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            int idx;
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17)) {
               idx = 8;
            } else if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)) {
               idx = 7;
            } else if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_10)) {
               idx = 6;
            } else {
               idx = 5;
            }

            EntityData<?> entityData = WatchableIndexUtil.getIndex(metadata, idx);
            if (entityData != null && entityData.getValue() instanceof Byte) {
               if (!hasSendTransaction) {
                  player.sendTransaction();
               }

               hasSendTransaction = true;
               byte data = (Byte)entityData.getValue();
               if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
                  boolean isRiptiding = (data & 4) == 4;
                  player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.isRiptidePose = isRiptiding);
               }

               boolean isActive = (data & 1) > 0;
               boolean isOffhand = (data & 2) > 0;
               player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.packetStateData.setSlowedByUsingItem(false));
               int markedTransaction = player.lastTransactionSent.get();
               player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get() + 1, () -> {
                  if (player.packetStateData.slowedByUsingItemTransaction < markedTransaction) {
                     PacketPlayerDigging.handleUseItem(player, isOffhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                     player.packetStateData.setSlowedByUsingItem(isActive);
                     if (isActive) {
                        player.packetStateData.itemInUseHand = isOffhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                     }
                  }
               });
               event.getTasksAfterSend().add(player::sendTransaction);
            }
         }
      }
   }
}
