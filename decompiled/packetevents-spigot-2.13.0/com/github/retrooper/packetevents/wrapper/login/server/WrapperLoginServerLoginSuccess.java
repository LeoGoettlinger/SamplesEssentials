package com.github.retrooper.packetevents.wrapper.login.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class WrapperLoginServerLoginSuccess extends PacketWrapper<WrapperLoginServerLoginSuccess> {
   private static final UUID FALLBACK_SESSION_ID = new UUID(0L, 0L);
   private UserProfile userProfile;
   private UUID sessionId;
   @Obsolete
   private boolean strictErrorHandling;

   public WrapperLoginServerLoginSuccess(PacketSendEvent event) {
      super(event);
   }

   public WrapperLoginServerLoginSuccess(UUID uuid, String username) {
      this(new UserProfile(uuid, username));
   }

   public WrapperLoginServerLoginSuccess(UserProfile userProfile) {
      this(userProfile, true);
   }

   public WrapperLoginServerLoginSuccess(UserProfile userProfile, boolean strictErrorHandling) {
      this(userProfile, FALLBACK_SESSION_ID, strictErrorHandling);
   }

   public WrapperLoginServerLoginSuccess(UserProfile userProfile, UUID sessionId) {
      this(userProfile, sessionId, true);
   }

   public WrapperLoginServerLoginSuccess(UserProfile userProfile, UUID sessionId, boolean strictErrorHandling) {
      super(PacketType.Login.Server.LOGIN_SUCCESS);
      this.userProfile = userProfile;
      this.sessionId = sessionId;
      this.strictErrorHandling = strictErrorHandling;
   }

   @Override
   public void read() {
      UUID uuid;
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
         uuid = this.readUUID();
      } else {
         uuid = UUID.fromString(this.readString(36));
      }

      String username = this.readString(16);
      this.userProfile = new UserProfile(uuid, username);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         int propertyCount = this.readVarInt();

         for (int i = 0; i < propertyCount; i++) {
            String propertyName = this.readString();
            String propertyValue = this.readString();
            String propertySignature = this.readOptional(PacketWrapper::readString);
            TextureProperty textureProperty = new TextureProperty(propertyName, propertyValue, propertySignature);
            this.userProfile.getTextureProperties().add(textureProperty);
         }
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_26_2)) {
         this.sessionId = this.readUUID();
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_5) && this.serverVersion.isOlderThan(ServerVersion.V_1_21_2)) {
         this.strictErrorHandling = this.readBoolean();
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
         this.writeUUID(this.userProfile.getUUID());
      } else {
         this.writeString(this.userProfile.getUUID().toString(), 36);
      }

      this.writeString(this.userProfile.getName(), 16);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         this.writeVarInt(this.userProfile.getTextureProperties().size());

         for (TextureProperty textureProperty : this.userProfile.getTextureProperties()) {
            this.writeString(textureProperty.getName());
            this.writeString(textureProperty.getValue());
            this.writeOptional(textureProperty.getSignature(), PacketWrapper::writeString);
         }
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_26_2)) {
         this.writeUUID(this.sessionId);
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20_5) && this.serverVersion.isOlderThan(ServerVersion.V_1_21_2)) {
         this.writeBoolean(this.strictErrorHandling);
      }
   }

   public void copy(WrapperLoginServerLoginSuccess wrapper) {
      this.userProfile = wrapper.userProfile;
      this.sessionId = wrapper.sessionId;
      this.strictErrorHandling = wrapper.strictErrorHandling;
   }

   public UserProfile getUserProfile() {
      return this.userProfile;
   }

   public void setUserProfile(UserProfile userProfile) {
      this.userProfile = userProfile;
   }

   public UUID getSessionId() {
      return this.sessionId;
   }

   public void setSessionId(UUID sessionId) {
      this.sessionId = sessionId;
   }

   @Obsolete
   public boolean isStrictErrorHandling() {
      return this.strictErrorHandling;
   }

   @Obsolete
   public void setStrictErrorHandling(boolean strictErrorHandling) {
      this.strictErrorHandling = strictErrorHandling;
   }
}
