package de.oliver.fancysitula.api.entities;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;
import de.oliver.fancysitula.api.utils.FS_EquipmentSlot;
import de.oliver.fancysitula.api.utils.FS_GameProfile;
import de.oliver.fancysitula.api.utils.entityData.FS_AvatarData;
import de.oliver.fancysitula.api.utils.entityData.FS_EntityData;
import de.oliver.fancysitula.api.utils.entityData.FS_MannequinData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class FS_Mannequin extends FS_Entity {
   protected Map<FS_EquipmentSlot, ItemStack> equipment;
   protected FS_ClientboundSetEntityDataPacket.EntityData skinCustomizationData = new FS_ClientboundSetEntityDataPacket.EntityData(
      FS_AvatarData.DATA_PLAYER_MODE_CUSTOMISATION, (byte)127
   );
   protected FS_ClientboundSetEntityDataPacket.EntityData mainHandData = new FS_ClientboundSetEntityDataPacket.EntityData(
      FS_AvatarData.DATA_PLAYER_MAIN_HAND, null
   );
   protected FS_ClientboundSetEntityDataPacket.EntityData profileData = new FS_ClientboundSetEntityDataPacket.EntityData(FS_MannequinData.DATA_PROFILE, null);
   protected FS_ClientboundSetEntityDataPacket.EntityData immovableData = new FS_ClientboundSetEntityDataPacket.EntityData(
      FS_MannequinData.DATA_IMMOVABLE, true
   );
   protected FS_ClientboundSetEntityDataPacket.EntityData descriptionData = new FS_ClientboundSetEntityDataPacket.EntityData(
      FS_MannequinData.DATA_DESCRIPTION, null
   );
   protected FS_ClientboundSetEntityDataPacket.EntityData poseData = new FS_ClientboundSetEntityDataPacket.EntityData(FS_EntityData.POSE, "STANDING");
   public static final String POSE_STANDING = "STANDING";
   public static final String POSE_CROUCHING = "CROUCHING";
   public static final String POSE_SWIMMING = "SWIMMING";
   public static final String POSE_FLYING = "FALL_FLYING";
   public static final String POSE_SLEEPING = "SLEEPING";

   public FS_Mannequin() {
      super(EntityType.MANNEQUIN);
      this.equipment = new ConcurrentHashMap<>();
   }

   public Map<FS_EquipmentSlot, ItemStack> getEquipment() {
      return Collections.unmodifiableMap(this.equipment);
   }

   public ItemStack getEquipment(FS_EquipmentSlot slot) {
      return this.equipment.get(slot);
   }

   public void setEquipment(Map<FS_EquipmentSlot, ItemStack> equipment) {
      if (equipment == null) {
         this.equipment.clear();
      } else {
         this.equipment.clear();
         this.equipment.putAll(equipment);
      }
   }

   public void setEquipment(FS_EquipmentSlot slot, ItemStack item) {
      if (slot != null) {
         if (item == null) {
            this.equipment.remove(slot);
         } else {
            this.equipment.put(slot, item);
         }
      }
   }

   public byte getSkinCustomization() {
      Object value = this.skinCustomizationData.getValue();
      return value != null ? (Byte)value : 127;
   }

   public void setSkinCustomization(byte skinCustomization) {
      this.skinCustomizationData.setValue(skinCustomization);
   }

   public String getMainHand() {
      Object value = this.mainHandData.getValue();
      return value != null ? (String)value : "RIGHT";
   }

   public void setMainHand(String mainHand) {
      this.mainHandData.setValue(mainHand);
   }

   public FS_GameProfile getProfile() {
      return (FS_GameProfile)this.profileData.getValue();
   }

   public void setProfile(FS_GameProfile profile) {
      this.profileData.setValue(profile);
   }

   public boolean isImmovable() {
      Object value = this.immovableData.getValue();
      return value != null ? (Boolean)value : true;
   }

   public void setImmovable(boolean immovable) {
      this.immovableData.setValue(immovable);
   }

   public Optional<Component> getDescription() {
      Optional<Component> value = (Optional<Component>)this.descriptionData.getValue();
      return value != null ? value : Optional.empty();
   }

   public void setDescription(Optional<Component> description) {
      this.descriptionData.setValue(description);
   }

   public void setDescription(Component description) {
      this.descriptionData.setValue(description != null ? Optional.of(description) : Optional.empty());
   }

   public String getPose() {
      Object value = this.poseData.getValue();
      return value != null ? (String)value : "STANDING";
   }

   public void setPose(String pose) {
      if (pose == null) {
         this.poseData.setValue("STANDING");
      } else {
         String var3 = pose.toUpperCase();

         String normalizedPose = switch (var3) {
            case "STANDING" -> "STANDING";
            case "CROUCHING", "SNEAKING", "CROUCH", "SNEAK" -> "CROUCHING";
            case "SWIMMING", "SWIM" -> "SWIMMING";
            case "FLYING", "FALL_FLYING", "ELYTRA", "FLY" -> "FALL_FLYING";
            case "SLEEPING", "SLEEP" -> "SLEEPING";
            default -> "STANDING";
         };
         this.poseData.setValue(normalizedPose);
      }
   }

   @Override
   public List<FS_ClientboundSetEntityDataPacket.EntityData> getEntityData() {
      List<FS_ClientboundSetEntityDataPacket.EntityData> entityData = super.getEntityData();
      entityData.add(this.skinCustomizationData);
      if (this.mainHandData.getValue() != null) {
         entityData.add(this.mainHandData);
      }

      if (this.profileData.getValue() != null) {
         entityData.add(this.profileData);
      }

      entityData.add(this.immovableData);
      if (this.descriptionData.getValue() != null) {
         entityData.add(this.descriptionData);
      }

      entityData.add(this.poseData);
      return entityData;
   }
}
