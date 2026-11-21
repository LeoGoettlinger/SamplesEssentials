package de.oliver.fancysitula.factories;

import de.oliver.fancysitula.api.dialogs.FS_Dialog;
import de.oliver.fancysitula.api.packets.FS_ClientboundAddEntityPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundAnimatePacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundBundlePacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundClearDialogPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundCreateOrUpdateTeamPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundPlayerInfoRemovePacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundPlayerInfoUpdatePacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundRemoveEntitiesPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundRotateHeadPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundSetEquipmentPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundSetPassengersPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundShowDialogPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundTeleportEntityPacket;
import de.oliver.fancysitula.api.packets.FS_ClientboundUpdateAttributesPacket;
import de.oliver.fancysitula.api.utils.FS_EquipmentSlot;
import de.oliver.fancysitula.api.utils.ServerVersion;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundAddEntityPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundAnimatePacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundBundlePacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundClearDialogPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundCreateOrUpdateTeamPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundPlayerInfoRemovePacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundPlayerInfoUpdatePacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundRemoveEntitiesPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundRotateHeadPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundSetEntityDataPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundSetEquipmentPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundSetPassengersPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundShowDialogPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundTeleportEntityPacketImpl;
import de.oliver.fancysitula.versions.v26_3.packets.ClientboundUpdateAttributesPacketImpl;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class PacketFactory {
   public FS_ClientboundPlayerInfoUpdatePacket createPlayerInfoUpdatePacket(
      EnumSet<FS_ClientboundPlayerInfoUpdatePacket.Action> actions, List<FS_ClientboundPlayerInfoUpdatePacket.Entry> entries
   ) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundPlayerInfoUpdatePacketImpl(actions, entries);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundPlayerInfoUpdatePacketImpl(actions, entries);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundPlayerInfoUpdatePacketImpl(actions, entries);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundPlayerInfoUpdatePacketImpl(actions, entries);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundPlayerInfoUpdatePacketImpl(actions, entries);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundPlayerInfoUpdatePacketImpl(actions, entries);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundAddEntityPacket createAddEntityPacket(
      int entityId,
      UUID entityUUID,
      EntityType entityType,
      double x,
      double y,
      double z,
      float yaw,
      float pitch,
      float headYaw,
      int velocityX,
      int velocityY,
      int velocityZ,
      int data
   ) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundAddEntityPacketImpl(entityId, entityUUID, entityType, x, y, z, yaw, pitch, headYaw, velocityX, velocityY, velocityZ, data);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundAddEntityPacketImpl(
               entityId, entityUUID, entityType, x, y, z, yaw, pitch, headYaw, velocityX, velocityY, velocityZ, data
            );
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundAddEntityPacketImpl(
               entityId, entityUUID, entityType, x, y, z, yaw, pitch, headYaw, velocityX, velocityY, velocityZ, data
            );
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundAddEntityPacketImpl(
               entityId, entityUUID, entityType, x, y, z, yaw, pitch, headYaw, velocityX, velocityY, velocityZ, data
            );
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundAddEntityPacketImpl(
               entityId, entityUUID, entityType, x, y, z, yaw, pitch, headYaw, velocityX, velocityY, velocityZ, data
            );
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundAddEntityPacketImpl(
               entityId, entityUUID, entityType, x, y, z, yaw, pitch, headYaw, velocityX, velocityY, velocityZ, data
            );
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundPlayerInfoRemovePacket createPlayerInfoRemovePacket(List<UUID> uuids) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundPlayerInfoRemovePacketImpl(uuids);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundPlayerInfoRemovePacketImpl(uuids);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundPlayerInfoRemovePacketImpl(uuids);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundPlayerInfoRemovePacketImpl(uuids);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundPlayerInfoRemovePacketImpl(uuids);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundPlayerInfoRemovePacketImpl(uuids);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundRemoveEntitiesPacket createRemoveEntitiesPacket(List<Integer> entityIds) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundRemoveEntitiesPacketImpl(entityIds);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundRemoveEntitiesPacketImpl(entityIds);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundRemoveEntitiesPacketImpl(entityIds);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundRemoveEntitiesPacketImpl(entityIds);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundRemoveEntitiesPacketImpl(entityIds);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundRemoveEntitiesPacketImpl(entityIds);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundTeleportEntityPacket createTeleportEntityPacket(int entityId, double x, double y, double z, float yaw, float pitch, boolean onGround) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundTeleportEntityPacketImpl(entityId, x, y, z, yaw, pitch, onGround);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundTeleportEntityPacketImpl(entityId, x, y, z, yaw, pitch, onGround);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundTeleportEntityPacketImpl(entityId, x, y, z, yaw, pitch, onGround);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundTeleportEntityPacketImpl(entityId, x, y, z, yaw, pitch, onGround);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundTeleportEntityPacketImpl(entityId, x, y, z, yaw, pitch, onGround);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundTeleportEntityPacketImpl(entityId, x, y, z, yaw, pitch, onGround);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundRotateHeadPacket createRotateHeadPacket(int entityId, float headYaw) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundRotateHeadPacketImpl(entityId, headYaw);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundRotateHeadPacketImpl(entityId, headYaw);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundRotateHeadPacketImpl(entityId, headYaw);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundRotateHeadPacketImpl(entityId, headYaw);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundRotateHeadPacketImpl(entityId, headYaw);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundRotateHeadPacketImpl(entityId, headYaw);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundSetEntityDataPacket createSetEntityDataPacket(int entityId, List<FS_ClientboundSetEntityDataPacket.EntityData> entityData) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundSetEntityDataPacketImpl(entityId, entityData);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundSetEntityDataPacketImpl(entityId, entityData);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundSetEntityDataPacketImpl(entityId, entityData);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundSetEntityDataPacketImpl(entityId, entityData);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundSetEntityDataPacketImpl(entityId, entityData);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundSetEntityDataPacketImpl(entityId, entityData);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundSetEquipmentPacket createSetEquipmentPacket(int entityId, Map<FS_EquipmentSlot, ItemStack> equipment) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundSetEquipmentPacketImpl(entityId, equipment);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundSetEquipmentPacketImpl(entityId, equipment);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundSetEquipmentPacketImpl(entityId, equipment);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundSetEquipmentPacketImpl(entityId, equipment);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundSetEquipmentPacketImpl(entityId, equipment);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundSetEquipmentPacketImpl(entityId, equipment);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundSetPassengersPacket createSetPassengersPacket(int entityId, List<Integer> passengers) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundSetPassengersPacketImpl(entityId, passengers);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundSetPassengersPacketImpl(entityId, passengers);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundSetPassengersPacketImpl(entityId, passengers);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundSetPassengersPacketImpl(entityId, passengers);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundSetPassengersPacketImpl(entityId, passengers);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundSetPassengersPacketImpl(entityId, passengers);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundCreateOrUpdateTeamPacket createCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.CreateTeam createTeam) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundCreateOrUpdateTeamPacketImpl(teamName, createTeam);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, createTeam);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, createTeam);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, createTeam);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, createTeam);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, createTeam);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundCreateOrUpdateTeamPacket createCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.RemoveTeam removeTeam) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeTeam);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeTeam);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeTeam);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeTeam);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeTeam);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeTeam);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundCreateOrUpdateTeamPacket createCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.UpdateTeam updateTeam) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundCreateOrUpdateTeamPacketImpl(teamName, updateTeam);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, updateTeam);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, updateTeam);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, updateTeam);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, updateTeam);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, updateTeam);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundCreateOrUpdateTeamPacket createCreateOrUpdateTeamPacket(String teamName, FS_ClientboundCreateOrUpdateTeamPacket.AddEntity addEntity) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundCreateOrUpdateTeamPacketImpl(teamName, addEntity);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, addEntity);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, addEntity);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, addEntity);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, addEntity);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, addEntity);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundCreateOrUpdateTeamPacket createCreateOrUpdateTeamPacket(
      String teamName, FS_ClientboundCreateOrUpdateTeamPacket.RemoveEntity removeEntity
   ) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeEntity);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeEntity);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeEntity);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeEntity);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeEntity);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundCreateOrUpdateTeamPacketImpl(teamName, removeEntity);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundShowDialogPacket createShowDialogPacket(FS_Dialog dialog) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundShowDialogPacketImpl(dialog);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundShowDialogPacketImpl(dialog);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundShowDialogPacketImpl(dialog);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundShowDialogPacketImpl(dialog);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundShowDialogPacketImpl(dialog);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundShowDialogPacketImpl(dialog);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundClearDialogPacket createClearDialogPacket() {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundClearDialogPacketImpl();
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundClearDialogPacketImpl();
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundClearDialogPacketImpl();
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundClearDialogPacketImpl();
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundClearDialogPacketImpl();
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundClearDialogPacketImpl();
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundUpdateAttributesPacket createUpdateAttributesPacket(
      int entityId, List<FS_ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes
   ) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundUpdateAttributesPacketImpl(entityId, attributes);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundUpdateAttributesPacketImpl(entityId, attributes);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundUpdateAttributesPacketImpl(entityId, attributes);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundUpdateAttributesPacketImpl(entityId, attributes);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundUpdateAttributesPacketImpl(entityId, attributes);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundUpdateAttributesPacketImpl(entityId, attributes);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundAnimatePacket createAnimatePacket(int entityId, int animationId) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundAnimatePacketImpl(entityId, animationId);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundAnimatePacketImpl(entityId, animationId);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundAnimatePacketImpl(entityId, animationId);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundAnimatePacketImpl(entityId, animationId);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundAnimatePacketImpl(entityId, animationId);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundAnimatePacketImpl(entityId, animationId);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundBundlePacket createBundlePacket(List<FS_ClientboundPacket> packets) {
      switch (ServerVersion.getCurrentVersion()) {
         case v26_3:
            return new ClientboundBundlePacketImpl(packets);
         case v26_2:
            return new de.oliver.fancysitula.versions.v26_2.packets.ClientboundBundlePacketImpl(packets);
         case v26_1_2:
            return new de.oliver.fancysitula.versions.v26_1.packets.ClientboundBundlePacketImpl(packets);
         case v1_21_11:
            return new de.oliver.fancysitula.versions.v1_21_11.packets.ClientboundBundlePacketImpl(packets);
         case v1_21_9:
         case v1_21_10:
            return new de.oliver.fancysitula.versions.v1_21_9.packets.ClientboundBundlePacketImpl(packets);
         case v1_21_6:
         case v1_21_7:
         case v1_21_8:
            return new de.oliver.fancysitula.versions.v1_21_6.packets.ClientboundBundlePacketImpl(packets);
         default:
            throw new IllegalArgumentException("Unsupported server version: " + ServerVersion.getCurrentVersion());
      }
   }

   public FS_ClientboundBundlePacket createBundlePacket(FS_ClientboundPacket... packets) {
      return this.createBundlePacket(Arrays.asList(packets));
   }
}
