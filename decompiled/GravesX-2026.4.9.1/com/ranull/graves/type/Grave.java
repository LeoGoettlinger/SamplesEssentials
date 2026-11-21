package com.ranull.graves.type;

import com.ranull.graves.data.LocationData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Grave implements InventoryHolder, Serializable {
   private static final long serialVersionUID = 1L;
   private final UUID uuid;
   private transient Inventory inventory;
   private Map<EquipmentSlot, ItemStack> equipmentMap = new EnumMap<>(EquipmentSlot.class);
   private List<String> permissionList = new ArrayList<>();
   private LocationData locationDeath;
   private float yaw;
   private float pitch;
   private EntityType ownerType;
   private String ownerName;
   private String ownerNameDisplay;
   private UUID ownerUUID;
   private String ownerTexture;
   private String ownerTextureSignature;
   private EntityType killerType;
   private String killerName;
   private String killerNameDisplay;
   private UUID killerUUID;
   private int experience;
   private String deathCause;
   private boolean protection;
   private boolean is_abandoned;
   private long timeAlive;
   private long timeCreation;
   private long timeProtection;
   private Location location;
   private boolean isPreview;
   private String providerId;

   public Grave(UUID uuid) {
      this.uuid = uuid;
      this.timeCreation = System.currentTimeMillis();
   }

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }

   public void setInventory(Inventory inventory) {
      this.inventory = inventory;
   }

   public Map<EquipmentSlot, ItemStack> getEquipmentMap() {
      return this.equipmentMap;
   }

   public void setEquipmentMap(Map<EquipmentSlot, ItemStack> equipmentMap) {
      this.equipmentMap = (Map<EquipmentSlot, ItemStack>)(equipmentMap != null ? equipmentMap : new EnumMap<>(EquipmentSlot.class));
   }

   public List<ItemStack> getInventoryItemStack() {
      return (List<ItemStack>)(this.inventory != null ? Arrays.asList(this.inventory.getContents()) : new ArrayList<>());
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public List<String> getPermissionList() {
      return this.permissionList;
   }

   public void setPermissionList(List<String> permissionList) {
      this.permissionList = (List<String>)(permissionList != null ? permissionList : new ArrayList<>());
   }

   public Location getLocationDeath() {
      return this.locationDeath != null ? this.locationDeath.getLocation() : null;
   }

   public LocationData getLocationDeathData() {
      return this.locationDeath;
   }

   public void setLocationDeath(Location locationDeath) {
      this.locationDeath = locationDeath != null ? new LocationData(locationDeath) : null;
   }

   public float getYaw() {
      return this.yaw;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public EntityType getOwnerType() {
      return this.ownerType;
   }

   public void setOwnerType(EntityType ownerType) {
      this.ownerType = ownerType;
   }

   public String getOwnerName() {
      return this.ownerName;
   }

   public void setOwnerName(String ownerName) {
      this.ownerName = ownerName;
   }

   public String getOwnerNameDisplay() {
      return this.ownerNameDisplay;
   }

   public void setOwnerNameDisplay(String ownerNameDisplay) {
      this.ownerNameDisplay = ownerNameDisplay;
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public void setOwnerUUID(UUID ownerUUID) {
      this.ownerUUID = ownerUUID;
   }

   public String getOwnerTexture() {
      return this.ownerTexture;
   }

   public void setOwnerTexture(String ownerTexture) {
      this.ownerTexture = ownerTexture;
   }

   public String getOwnerTextureSignature() {
      return this.ownerTextureSignature;
   }

   public void setOwnerTextureSignature(String ownerTextureSignature) {
      this.ownerTextureSignature = ownerTextureSignature;
   }

   public EntityType getKillerType() {
      return this.killerType;
   }

   public void setKillerType(EntityType killerType) {
      this.killerType = killerType;
   }

   public String getKillerName() {
      return this.killerName;
   }

   public void setKillerName(String killerName) {
      this.killerName = killerName;
   }

   public String getKillerNameDisplay() {
      return this.killerNameDisplay;
   }

   public void setKillerNameDisplay(String killerNameDisplay) {
      this.killerNameDisplay = killerNameDisplay;
   }

   public UUID getKillerUUID() {
      return this.killerUUID;
   }

   public void setKillerUUID(UUID killerUUID) {
      this.killerUUID = killerUUID;
   }

   public int getExperience() {
      return this.experience;
   }

   public void setExperience(int experience) {
      this.experience = experience;
   }

   public String getDeathCause() {
      return this.deathCause == null ? "KILL" : this.deathCause;
   }

   public void setDeathCause(String deathCause) {
      if (deathCause == null) {
         this.deathCause = "KILL";
      } else {
         this.deathCause = deathCause;
      }
   }

   public boolean getProtection() {
      return this.protection;
   }

   public void setProtection(boolean protection) {
      this.protection = protection;
   }

   public long getTimeAlive() {
      return this.timeAlive;
   }

   public void setTimeAlive(long aliveTime) {
      this.timeAlive = aliveTime;
   }

   public long getTimeCreation() {
      return this.timeCreation;
   }

   public void setTimeCreation(long timeCreation) {
      this.timeCreation = timeCreation;
   }

   public long getTimeProtection() {
      return this.timeProtection;
   }

   public void setTimeProtection(long timeProtection) {
      this.timeProtection = timeProtection;
   }

   public void setTimeAliveRemaining(long timeAlive) {
      this.timeAlive = timeAlive;
   }

   public long getTimeAliveRemaining() {
      if (this.timeAlive < 0L) {
         return -1L;
      } else {
         long elapsedTime = System.currentTimeMillis() - this.timeCreation;
         long timeAliveRemaining = this.timeAlive - elapsedTime;
         return Math.max(timeAliveRemaining, 0L);
      }
   }

   public long getTimeProtectionRemaining() {
      if (this.timeProtection < 0L) {
         return -1L;
      } else {
         long elapsedTime = System.currentTimeMillis() - this.timeCreation;
         long timeProtectionRemaining = this.timeProtection - elapsedTime;
         return Math.max(timeProtectionRemaining, 0L);
      }
   }

   public long getLivedTime() {
      return System.currentTimeMillis() - this.timeCreation;
   }

   public boolean isAbandoned() {
      return this.is_abandoned;
   }

   public void setAbandoned(boolean is_abandoned) {
      this.is_abandoned = is_abandoned;
   }

   public int getItemAmount() {
      int counter = 0;
      if (this.inventory != null) {
         for (ItemStack itemStack : this.inventory.getContents()) {
            if (itemStack != null) {
               counter++;
            }
         }
      }

      return counter;
   }

   public String getOwnerDisplayName() {
      return this.ownerNameDisplay;
   }

   public void setOwnerDisplayName(String ownerNameDisplay) {
      this.ownerNameDisplay = ownerNameDisplay;
   }

   @Deprecated
   public Location getLocation() {
      return this.locationDeath != null ? this.locationDeath.getLocation() : null;
   }

   public void setGravePreview(boolean isPreview) {
      this.isPreview = isPreview;
   }

   public boolean getGravePreview() {
      return this.isPreview;
   }

   public String getProviderId() {
      return this.providerId;
   }

   public void setProviderId(String providerId) {
      this.providerId = providerId != null && !providerId.isBlank() ? providerId : null;
   }

   @Deprecated
   public void setLocation(Location location) {
      this.locationDeath = location != null ? new LocationData(location) : null;
   }

   public static enum StorageMode {
      EXACT,
      COMPACT,
      @Deprecated
      CHESTSORT;
   }
}
