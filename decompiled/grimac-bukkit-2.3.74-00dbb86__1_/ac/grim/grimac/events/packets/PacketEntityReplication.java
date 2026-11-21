package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.UserProfile;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPainting;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.data.SprintingState;
import ac.grim.grimac.utils.data.TrackerData;
import ac.grim.grimac.utils.data.packetentity.DashableEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHook;
import ac.grim.grimac.utils.data.packetentity.PacketEntityTrackXRot;
import ac.grim.grimac.utils.viaversion.ViaVersionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketEntityReplication extends Check implements PacketCheck {
   private final AtomicBoolean hasSentPreWavePacket = new AtomicBoolean(true);
   private final List<Integer> despawnedEntitiesThisTransaction = new ArrayList<>();
   private int maxFireworkBoostPing = 1000;

   public PacketEntityReplication(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.isTickPacket(event.getPacketType())) {
         this.player.compensatedEntities.entitiesRemovedThisTick.clear();
         boolean isTickingReliably = this.player.isTickingReliablyFor(3);
         PacketEntity playerVehicle = this.player.compensatedEntities.self.getRiding();

         for (PacketEntity entity : this.player.compensatedEntities.entityMap.values()) {
            if (entity == playerVehicle && !this.player.vehicleData.lastDummy) {
               entity.setPositionRaw(this.player, entity.getPossibleLocationBoxes());
            } else {
               entity.onMovement(isTickingReliably);
            }
         }

         if (this.player.vehicleData.camelSprintingState == SprintingState.STOPPING) {
            this.player.vehicleData.camelSprintingState = SprintingState.STOPPED;
         } else if (this.player.vehicleData.camelSprintingState == SprintingState.STOPPED && this.player.isSprinting) {
            this.player.vehicleData.camelSprintingState = SprintingState.STARTED;
         }
      }
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if ((event.getPacketType() == PacketType.Play.Server.PING || event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION)
         && this.player.packetStateData.lastServerTransWasValid) {
         this.despawnedEntitiesThisTransaction.clear();
      } else if (event.getPacketType() == PacketType.Play.Server.SPAWN_LIVING_ENTITY) {
         WrapperPlayServerSpawnLivingEntity packetOutEntity = new WrapperPlayServerSpawnLivingEntity(event);
         this.addEntity(
            packetOutEntity.getEntityId(),
            packetOutEntity.getEntityUUID(),
            packetOutEntity.getEntityType(),
            packetOutEntity.getPosition(),
            packetOutEntity.getYaw(),
            packetOutEntity.getPitch(),
            packetOutEntity.getEntityMetadata(),
            0
         );
      } else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY) {
         WrapperPlayServerSpawnEntity packetOutEntity = new WrapperPlayServerSpawnEntity(event);
         this.addEntity(
            packetOutEntity.getEntityId(),
            packetOutEntity.getUUID().orElse(null),
            packetOutEntity.getEntityType(),
            packetOutEntity.getPosition(),
            packetOutEntity.getYaw(),
            packetOutEntity.getPitch(),
            null,
            packetOutEntity.getData()
         );
      } else if (event.getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
         WrapperPlayServerSpawnPlayer packetOutEntity = new WrapperPlayServerSpawnPlayer(event);
         this.addEntity(
            packetOutEntity.getEntityId(),
            packetOutEntity.getUUID(),
            EntityTypes.PLAYER,
            packetOutEntity.getPosition(),
            packetOutEntity.getYaw(),
            packetOutEntity.getPitch(),
            packetOutEntity.getEntityMetadata(),
            0
         );
      } else if (event.getPacketType() == PacketType.Play.Server.SPAWN_PAINTING) {
         WrapperPlayServerSpawnPainting packetOutEntity = new WrapperPlayServerSpawnPainting(event);
         this.addEntity(
            packetOutEntity.getEntityId(),
            packetOutEntity.getUUID(),
            EntityTypes.PAINTING,
            packetOutEntity.getPosition().toVector3d(),
            0.0F,
            0.0F,
            null,
            packetOutEntity.getDirection().getHorizontalIndex()
         );
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
         WrapperPlayServerEntityRelativeMove move = new WrapperPlayServerEntityRelativeMove(event);
         this.handleMoveEntity(event, move.getEntityId(), move.getDeltaX(), move.getDeltaY(), move.getDeltaZ(), null, null, true, true);
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
         WrapperPlayServerEntityRelativeMoveAndRotation move = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
         this.handleMoveEntity(
            event,
            move.getEntityId(),
            move.getDeltaX(),
            move.getDeltaY(),
            move.getDeltaZ(),
            move.getYaw() * 0.7111111F,
            move.getPitch() * 0.7111111F,
            true,
            true
         );
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
         WrapperPlayServerEntityTeleport move = new WrapperPlayServerEntityTeleport(event);
         Vector3d pos = move.getPosition();
         this.handleMoveEntity(event, move.getEntityId(), pos.getX(), pos.getY(), pos.getZ(), move.getYaw(), move.getPitch(), false, true);
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_POSITION_SYNC) {
         WrapperPlayServerEntityPositionSync move = new WrapperPlayServerEntityPositionSync(event);
         EntityPositionData values = move.getValues();
         Vector3d pos = values.getPosition();
         this.handleMoveEntity(event, move.getId(), pos.getX(), pos.getY(), pos.getZ(), values.getYaw(), values.getPitch(), false, true);
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_ROTATION) {
         WrapperPlayServerEntityRotation move = new WrapperPlayServerEntityRotation(event);
         this.handleMoveEntity(event, move.getEntityId(), 0.0, 0.0, 0.0, move.getYaw() * 0.7111111F, move.getPitch() * 0.7111111F, true, false);
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
         WrapperPlayServerEntityMetadata entityMetadata = new WrapperPlayServerEntityMetadata(event);
         this.player
            .latencyUtils
            .addRealTimeTask(
               this.player.lastTransactionSent.get(),
               () -> this.player.compensatedEntities.updateEntityMetadata(entityMetadata.getEntityId(), entityMetadata.getEntityMetadata())
            );
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
         WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment(event);
         this.player
            .latencyUtils
            .addRealTimeTask(
               this.player.lastTransactionSent.get(),
               () -> this.player.compensatedEntities.updateEntityEquipment(equipment.getEntityId(), equipment.getEquipment())
            );
      } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
         WrapperPlayServerPlayerInfoUpdate info = new WrapperPlayServerPlayerInfoUpdate(event);
         if (info.getActions().contains(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER)) {
            this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> {
               for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry : info.getEntries()) {
                  UserProfile gameProfile = entry.getGameProfile();
                  UUID uuid = gameProfile.getUUID();
                  this.player.compensatedEntities.profiles.put(uuid, gameProfile);
               }
            });
         }
      } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_REMOVE) {
         WrapperPlayServerPlayerInfoRemove remove = new WrapperPlayServerPlayerInfoRemove(event);
         this.player
            .latencyUtils
            .addRealTimeTask(this.player.lastTransactionSent.get(), () -> remove.getProfileIds().forEach(this.player.compensatedEntities.profiles::remove));
      } else if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {
         WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo(event);
         this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> {
            if (info.getAction() == WrapperPlayServerPlayerInfo.Action.ADD_PLAYER) {
               for (WrapperPlayServerPlayerInfo.PlayerData entry : info.getPlayerDataList()) {
                  UserProfile gameProfile = entry.getUserProfile();
                  UUID uuid = gameProfile.getUUID();
                  this.player.compensatedEntities.profiles.put(uuid, gameProfile);
               }
            } else if (info.getAction() == WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER) {
               info.getPlayerDataList().forEach(profile -> this.player.compensatedEntities.profiles.remove(profile.getUserProfile().getUUID()));
            }
         });
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
         WrapperPlayServerEntityEffect effect = new WrapperPlayServerEntityEffect(event);
         PotionType type = effect.getPotionType();
         if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_9) && ViaVersionUtil.isAvailable && type.getId(this.player.getClientVersion()) > 23) {
            event.setCancelled(true);
            return;
         }

         if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_13) && ViaVersionUtil.isAvailable && type.getId(this.player.getClientVersion()) == 30
            )
          {
            event.setCancelled(true);
            return;
         }

         if (this.isDirectlyAffectingPlayer(this.player, effect.getEntityId())) {
            this.player.sendTransaction();
         }

         this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> {
            PacketEntity entity = this.player.compensatedEntities.getEntity(effect.getEntityId());
            if (entity != null) {
               entity.addPotionEffect(type, effect.getEffectAmplifier());
            }
         });
      } else if (event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
         WrapperPlayServerRemoveEntityEffect effectx = new WrapperPlayServerRemoveEntityEffect(event);
         if (this.isDirectlyAffectingPlayer(this.player, effectx.getEntityId())) {
            this.player.sendTransaction();
         }

         this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> {
            PacketEntity entity = this.player.compensatedEntities.getEntity(effect.getEntityId());
            if (entity != null) {
               entity.removePotionEffect(effect.getPotionType());
            }
         });
      } else if (event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES) {
         WrapperPlayServerUpdateAttributes attributes = new WrapperPlayServerUpdateAttributes(event);
         int entityID = attributes.getEntityId();
         if (this.isDirectlyAffectingPlayer(this.player, entityID)) {
            this.player.sendTransaction();
         }

         this.player
            .latencyUtils
            .addRealTimeTask(
               this.player.lastTransactionSent.get(), () -> this.player.compensatedEntities.updateAttributes(entityID, attributes.getProperties())
            );
      } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_STATUS) {
         WrapperPlayServerEntityStatus status = new WrapperPlayServerEntityStatus(event);
         if (status.getStatus() == 3) {
            PacketEntity entity = this.player.compensatedEntities.getEntity(status.getEntityId());
            if (entity == null) {
               return;
            }

            entity.isDead = true;
         }

         if (status.getStatus() == 9) {
            if (status.getEntityId() != this.player.entityID) {
               return;
            }

            this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> this.player.packetStateData.setSlowedByUsingItem(false));
            this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get() + 1, () -> this.player.packetStateData.setSlowedByUsingItem(false));
         }

         if (status.getStatus() == 31) {
            if (!(this.player.compensatedEntities.getEntity(status.getEntityId()) instanceof PacketEntityHook hookEntity)) {
               return;
            }

            if (hookEntity.attached == this.player.entityID) {
               this.player.sendTransaction();
               this.player
                  .latencyUtils
                  .addRealTimeTask(this.player.lastTransactionSent.get(), () -> this.player.uncertaintyHandler.fishingRodPulls.add(hookEntity.owner));
            }
         }

         if (status.getStatus() >= 24 && status.getStatus() <= 28 && status.getEntityId() == this.player.entityID) {
            this.player.compensatedEntities.self.opLevel = status.getStatus() - 24;
         }
      } else if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
         WrapperPlayServerSetSlot slot = new WrapperPlayServerSetSlot(event);
         if (slot.getWindowId() == 0) {
            Runnable task = () -> {
               if (slot.getSlot() - 36 == this.player.packetStateData.lastSlotSelected
                     && (
                        !this.player.inventory.getHeldItem().is(slot.getItem().getType())
                           || this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)
                     )
                  || slot.getSlot() == 45 && !this.player.inventory.getOffHand().is(slot.getItem().getType())) {
                  InteractionHand hand = slot.getSlot() == 45 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                  if (hand == this.player.packetStateData.itemInUseHand) {
                     this.player.packetStateData.setSlowedByUsingItem(false);
                  }

                  if (this.player.isResetItemUsageOnItemUpdate() && hand == GrimAPI.INSTANCE.getItemResetHandler().getItemUsageHand(this.player.platformPlayer)
                     )
                   {
                     GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(this.player.platformPlayer);
                  }
               }
            };
            this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), task);
            this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get() + 1, task);
         }
      } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
         WrapperPlayServerWindowItems items = new WrapperPlayServerWindowItems(event);
         if (items.getWindowId() == 0) {
            Runnable task = () -> {
               if (this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                  this.player.packetStateData.setSlowedByUsingItem(false);
                  if (this.player.isResetItemUsageOnItemUpdate()) {
                     GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(this.player.platformPlayer);
                  }
               } else {
                  if (items.getItems().size() > 45 && !this.player.inventory.getOffHand().is(items.getItems().get(45).getType())) {
                     if (this.player.packetStateData.itemInUseHand == InteractionHand.OFF_HAND) {
                        this.player.packetStateData.setSlowedByUsingItem(false);
                     }

                     if (this.player.isResetItemUsageOnItemUpdate()
                        && GrimAPI.INSTANCE.getItemResetHandler().getItemUsageHand(this.player.platformPlayer) == InteractionHand.OFF_HAND) {
                        GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(this.player.platformPlayer);
                     }
                  }

                  if (!this.player.inventory.getHeldItem().is(items.getItems().get(this.player.packetStateData.lastSlotSelected + 36).getType())) {
                     if (this.player.packetStateData.itemInUseHand == InteractionHand.MAIN_HAND) {
                        this.player.packetStateData.setSlowedByUsingItem(false);
                     }

                     if (this.player.isResetItemUsageOnItemUpdate()
                        && GrimAPI.INSTANCE.getItemResetHandler().getItemUsageHand(this.player.platformPlayer) == InteractionHand.MAIN_HAND) {
                        GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(this.player.platformPlayer);
                     }
                  }
               }
            };
            this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), task);
            this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get() + 1, task);
         }
      } else if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
         this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> this.player.packetStateData.setSlowedByUsingItem(false));
         this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get() + 1, () -> this.player.packetStateData.setSlowedByUsingItem(false));
      } else if (event.getPacketType() == PacketType.Play.Server.OPEN_HORSE_WINDOW) {
         this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> this.player.packetStateData.setSlowedByUsingItem(false));
         this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get() + 1, () -> this.player.packetStateData.setSlowedByUsingItem(false));
      } else if (event.getPacketType() == PacketType.Play.Server.SET_PASSENGERS) {
         WrapperPlayServerSetPassengers mount = new WrapperPlayServerSetPassengers(event);
         int vehicleID = mount.getEntityId();
         int[] passengers = mount.getPassengers();
         this.handleMountVehicle(event, vehicleID, passengers);
      } else if (event.getPacketType() == PacketType.Play.Server.ATTACH_ENTITY) {
         WrapperPlayServerAttachEntity attach = new WrapperPlayServerAttachEntity(event);
         if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
            return;
         }

         if (!attach.isLeash()) {
            int vehicleID = attach.getHoldingId();
            int attachID = attach.getAttachedId();
            TrackerData trackerData = this.player.compensatedEntities.getTrackedEntity(attachID);
            if (trackerData != null) {
               if (vehicleID == -1) {
                  vehicleID = trackerData.getLegacyPointEightMountedUpon();
                  this.handleMountVehicle(event, vehicleID, new int[0]);
               } else {
                  trackerData.setLegacyPointEightMountedUpon(vehicleID);
                  this.handleMountVehicle(event, vehicleID, new int[]{attachID});
               }
            } else {
               LogUtil.warn(
                  "Server sent an invalid attach entity packet for entity "
                     + attach.getHoldingId()
                     + " with passenger "
                     + attach.getAttachedId()
                     + "! The client ignores this."
               );
            }
         }
      } else if (event.getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
         WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(event);
         int[] destroyEntityIds = destroy.getEntityIds();

         for (int entityID : destroyEntityIds) {
            this.despawnedEntitiesThisTransaction.add(entityID);
            this.player.compensatedEntities.serverPositionsMap.remove(entityID);
            if (this.player.compensatedEntities.serverPlayerVehicle != null && this.player.compensatedEntities.serverPlayerVehicle == entityID) {
               this.player.compensatedEntities.serverPlayerVehicle = null;
            }
         }

         int destroyTransaction = this.player.lastTransactionSent.get() + 1;
         this.player.latencyUtils.addRealTimeTask(destroyTransaction, () -> {
            for (int entityId : destroyEntityIds) {
               this.player.compensatedEntities.removeEntity(entityId);
               this.player.dashableEntities.removeEntity(entityId);
               this.player.fireworks.removeFirework(entityId);
               this.player.compensatedEntities.entitiesRemovedThisTick.add(entityId);
            }
         });
         if (this.maxFireworkBoostPing > 0) {
            this.player.runNettyTaskInMs(() -> {
               if (this.player.lastTransactionReceived.get() < destroyTransaction) {
                  for (int entityIDx : destroyEntityIds) {
                     if (this.player.fireworks.hasFirework(entityIDx)) {
                        this.player.getSetbackTeleportUtil().executeViolationSetback();
                        break;
                     }
                  }
               }
            }, this.maxFireworkBoostPing);
         }
      }
   }

   private void handleMountVehicle(PacketSendEvent event, int vehicleID, int[] passengers) {
      boolean wasInVehicle = this.player.compensatedEntities.serverPlayerVehicle != null && this.player.compensatedEntities.serverPlayerVehicle == vehicleID;
      boolean inThisVehicle = false;

      for (int passenger : passengers) {
         inThisVehicle = passenger == this.player.entityID;
         if (inThisVehicle) {
            break;
         }
      }

      if (inThisVehicle && !wasInVehicle) {
         this.player.handleMountVehicle(vehicleID);
      }

      if (!inThisVehicle && wasInVehicle) {
         this.player.handleDismountVehicle(event);
      }

      if (wasInVehicle || inThisVehicle) {
         this.player.sendTransaction();
      }

      this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> {
         PacketEntity vehicle = this.player.compensatedEntities.getEntity(vehicleID);
         if (vehicle != null) {
            for (PacketEntity passengerxx : new ArrayList<>(vehicle.passengers)) {
               passengerxx.eject();
            }

            for (int entityID : passengers) {
               PacketEntity passengerx = this.player.compensatedEntities.getEntity(entityID);
               if (passengerx != null) {
                  passengerx.mount(vehicle);
               }
            }
         }
      });
   }

   private void handleMoveEntity(
      PacketSendEvent event, int entityId, double deltaX, double deltaY, double deltaZ, Float yaw, Float pitch, boolean isRelative, boolean hasPos
   ) {
      TrackerData data = this.player.compensatedEntities.getTrackedEntity(entityId);
      boolean didNotSendPreWave = this.hasSentPreWavePacket.compareAndSet(false, true);
      if (didNotSendPreWave) {
         this.player.sendTransaction();
      }

      if (data != null) {
         if (!isRelative) {
            data.setX(deltaX);
            data.setY(deltaY);
            data.setZ(deltaZ);
         } else {
            boolean vanillaVehicleFlight = this.player.compensatedEntities.serverPlayerVehicle != null
               && this.player.compensatedEntities.serverPlayerVehicle == entityId
               && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
               && PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21_2)
               && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9);
            if (vanillaVehicleFlight
               || (Math.abs(deltaX) >= 3.9375 || Math.abs(deltaY) >= 3.9375 || Math.abs(deltaZ) >= 3.9375)
                  && this.player.getClientVersion().isOlderThan(ClientVersion.V_1_9)
                  && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
               this.player
                  .user
                  .writePacket(
                     (PacketWrapper<?>)(new WrapperPlayServerEntityTeleport(
                        entityId,
                        new Vector3d(data.getX() + deltaX, data.getY() + deltaY, data.getZ() + deltaZ),
                        yaw == null ? data.getXRot() : yaw,
                        pitch == null ? data.getYRot() : pitch,
                        false
                     ))
                  );
               event.setCancelled(true);
               return;
            }

            data.setX(data.getX() + deltaX);
            data.setY(data.getY() + deltaY);
            data.setZ(data.getZ() + deltaZ);
         }

         if (yaw != null) {
            data.setXRot(yaw);
            data.setYRot(pitch);
         }

         if (data.getLastTransactionHung() == this.player.lastTransactionSent.get()) {
            this.player.sendTransaction();
         }

         data.setLastTransactionHung(this.player.lastTransactionSent.get());
      }

      int lastTrans = this.player.lastTransactionSent.get();
      this.player.latencyUtils.addRealTimeTask(lastTrans, () -> {
         PacketEntity entity = this.player.compensatedEntities.getEntity(entityId);
         if (entity != null) {
            if (entity instanceof PacketEntityTrackXRot xRotEntity && yaw != null) {
               xRotEntity.packetYaw = yaw;
               xRotEntity.steps = entity.isBoat ? 10 : 3;
            }

            entity.onFirstTransaction(isRelative, hasPos, deltaX, deltaY, deltaZ, this.player);
         }
      });
      this.player.latencyUtils.addRealTimeTask(lastTrans + 1, () -> {
         PacketEntity entity = this.player.compensatedEntities.getEntity(entityId);
         if (entity != null) {
            entity.onSecondTransaction();
         }
      });
   }

   public void addEntity(int entityID, UUID uuid, EntityType type, Vector3d position, float xRot, float yRot, List<EntityData<?>> entityMetadata, int extraData) {
      if (this.despawnedEntitiesThisTransaction.contains(entityID)) {
         this.player.sendTransaction();
      }

      this.player
         .compensatedEntities
         .serverPositionsMap
         .put(entityID, new TrackerData(position.getX(), position.getY(), position.getZ(), xRot, yRot, type, this.player.lastTransactionSent.get()));
      this.player.latencyUtils.addRealTimeTask(this.player.lastTransactionSent.get(), () -> {
         if (this.player.compensatedEntities.addEntity(entityID, uuid, type, position, xRot, extraData) instanceof DashableEntity dashable) {
            this.player.dashableEntities.addEntity(entityID, dashable);
         }

         if (entityMetadata != null) {
            this.player.compensatedEntities.updateEntityMetadata(entityID, entityMetadata);
         }
      });
   }

   private boolean isDirectlyAffectingPlayer(GrimPlayer player, int entityID) {
      return player.compensatedEntities.serverPlayerVehicle == null && entityID == player.entityID
         || player.compensatedEntities.serverPlayerVehicle != null && entityID == player.compensatedEntities.serverPlayerVehicle;
   }

   public void onEndOfTickEvent(boolean async, boolean flush) {
      this.player.sendTransaction(async);
      if (flush) {
         this.player.user.flushPackets();
      }
   }

   public void tickStartTick() {
      this.hasSentPreWavePacket.set(false);
   }

   @Override
   public void onReload(ConfigManager config) {
      this.maxFireworkBoostPing = config.getIntElse("max-ping-firework-boost", 1000);
   }
}
