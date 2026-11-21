package de.oliver.fancysitula.api.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FS_GameProfile {
   private UUID uuid;
   private String name;
   private Map<String, FS_GameProfile.Property> properties;
   private String skinTextureAsset;
   private String capeTextureAsset;
   private String elytraTextureAsset;
   private String modelType;

   public FS_GameProfile(UUID uuid, String name, Map<String, FS_GameProfile.Property> properties) {
      this.uuid = uuid;
      this.name = name;
      this.properties = properties;
   }

   public FS_GameProfile(UUID uuid, String name) {
      this(uuid, name, new HashMap<>());
   }

   public static FS_GameProfile fromBukkit(PlayerProfile gameProfile) {
      FS_GameProfile fsGameProfile = new FS_GameProfile(gameProfile.getId(), gameProfile.getName());

      for (ProfileProperty property : gameProfile.getProperties()) {
         fsGameProfile.getProperties().put(property.getName(), new FS_GameProfile.Property(property.getName(), property.getValue(), property.getSignature()));
      }

      return fsGameProfile;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public void setUUID(UUID uuid) {
      this.uuid = uuid;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Map<String, FS_GameProfile.Property> getProperties() {
      return this.properties;
   }

   public void setProperties(Map<String, FS_GameProfile.Property> properties) {
      this.properties = properties;
   }

   public String getSkinTextureAsset() {
      return this.skinTextureAsset;
   }

   public void setSkinTextureAsset(String skinTextureAsset) {
      this.skinTextureAsset = skinTextureAsset;
   }

   public String getCapeTextureAsset() {
      return this.capeTextureAsset;
   }

   public void setCapeTextureAsset(String capeTextureAsset) {
      this.capeTextureAsset = capeTextureAsset;
   }

   public String getElytraTextureAsset() {
      return this.elytraTextureAsset;
   }

   public void setElytraTextureAsset(String elytraTextureAsset) {
      this.elytraTextureAsset = elytraTextureAsset;
   }

   public String getModelType() {
      return this.modelType;
   }

   public void setModelType(String modelType) {
      this.modelType = modelType;
   }

   public boolean hasTextureAssets() {
      return this.skinTextureAsset != null || this.capeTextureAsset != null || this.elytraTextureAsset != null;
   }

   public record Property(String name, String value, String signature) {
   }
}
