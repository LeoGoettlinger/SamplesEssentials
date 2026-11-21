package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.crypto.SignatureData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerPlayerInfo extends PacketWrapper<WrapperPlayServerPlayerInfo> {
   @Nullable
   private WrapperPlayServerPlayerInfo.Action action;
   private List<WrapperPlayServerPlayerInfo.PlayerData> playerDataList;

   public WrapperPlayServerPlayerInfo(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerPlayerInfo(@NotNull WrapperPlayServerPlayerInfo.Action action, List<WrapperPlayServerPlayerInfo.PlayerData> playerDataList) {
      super(PacketType.Play.Server.PLAYER_INFO);
      this.action = action;
      this.playerDataList = playerDataList;
   }

   public WrapperPlayServerPlayerInfo(@NotNull WrapperPlayServerPlayerInfo.Action action, WrapperPlayServerPlayerInfo.PlayerData... playerData) {
      super(PacketType.Play.Server.PLAYER_INFO);
      this.action = action;
      this.playerDataList = new ArrayList<>();
      Collections.addAll(this.playerDataList, playerData);
   }

   @Override
   public void read() {
      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         this.playerDataList = new ArrayList<>(1);
         String rawUsername = this.readString();
         Component username = Component.text(rawUsername);
         boolean online = this.readBoolean();
         int ping = this.readShort();
         WrapperPlayServerPlayerInfo.PlayerData data = new WrapperPlayServerPlayerInfo.PlayerData(username, null, GameMode.SURVIVAL, ping);
         this.playerDataList.add(data);
         if (online) {
            this.action = null;
         } else {
            this.action = WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER;
         }
      } else {
         this.action = WrapperPlayServerPlayerInfo.Action.VALUES[this.readVarInt()];
         int playerDataCount = this.readVarInt();
         this.playerDataList = new ArrayList<>(playerDataCount);

         for (int i = 0; i < playerDataCount; i++) {
            WrapperPlayServerPlayerInfo.PlayerData data = null;
            UUID uuid = this.readUUID();
            switch (this.action) {
               case ADD_PLAYER:
                  String playerUsername = this.readString(16);
                  UserProfile userProfile = new UserProfile(uuid, playerUsername);
                  int propertyCount = this.readVarInt();

                  for (int j = 0; j < propertyCount; j++) {
                     String propertyName = this.readString();
                     String propertyValue = this.readString();
                     String propertySignature = this.readOptional(PacketWrapper::readString);
                     TextureProperty textureProperty = new TextureProperty(propertyName, propertyValue, propertySignature);
                     userProfile.getTextureProperties().add(textureProperty);
                  }

                  GameMode gameMode = GameMode.getById(this.readVarInt());
                  int ping = this.readVarInt();
                  Component displayName = this.readBoolean() ? this.readComponent() : null;
                  SignatureData signatureData = null;
                  if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
                     signatureData = this.readOptional(PacketWrapper::readSignatureData);
                  }

                  data = new WrapperPlayServerPlayerInfo.PlayerData(displayName, userProfile, gameMode, signatureData, ping);
                  break;
               case UPDATE_GAME_MODE:
                  GameMode gameMode = GameMode.getById(this.readVarInt());
                  data = new WrapperPlayServerPlayerInfo.PlayerData((Component)null, new UserProfile(uuid, null), gameMode, -1);
                  break;
               case UPDATE_LATENCY:
                  int ping = this.readVarInt();
                  data = new WrapperPlayServerPlayerInfo.PlayerData((Component)null, new UserProfile(uuid, null), null, ping);
                  break;
               case UPDATE_DISPLAY_NAME:
                  Component displayName = this.readBoolean() ? this.readComponent() : null;
                  data = new WrapperPlayServerPlayerInfo.PlayerData(displayName, new UserProfile(uuid, null), null, -1);
                  break;
               case REMOVE_PLAYER:
                  data = new WrapperPlayServerPlayerInfo.PlayerData((Component)null, new UserProfile(uuid, null), null, -1);
            }

            if (data != null) {
               this.playerDataList.add(data);
            }
         }
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         WrapperPlayServerPlayerInfo.PlayerData data = this.playerDataList.get(0);
         String rawUsername = ((TextComponent)data.displayName).content();
         this.writeString(rawUsername);
         this.writeBoolean(this.action != WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER);
         this.writeShort(data.ping);
      } else {
         this.writeVarInt(this.action.ordinal());
         this.writeVarInt(this.playerDataList.size());

         for (WrapperPlayServerPlayerInfo.PlayerData data : this.playerDataList) {
            this.writeUUID(data.userProfile.getUUID());
            switch (this.action) {
               case ADD_PLAYER:
                  this.writeString(data.userProfile.getName(), 16);
                  this.writeList(data.userProfile.getTextureProperties(), (wrapper, textureProperty) -> {
                     wrapper.writeString(textureProperty.getName());
                     wrapper.writeString(textureProperty.getValue());
                     wrapper.writeOptional(textureProperty.getSignature(), PacketWrapper::writeString);
                  });
                  this.writeVarInt(data.gameMode.ordinal());
                  this.writeVarInt(data.ping);
                  if (data.displayName != null) {
                     this.writeBoolean(true);
                     this.writeComponent(data.displayName);
                  } else {
                     this.writeBoolean(false);
                  }

                  if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
                     this.writeOptional(data.getSignatureData(), PacketWrapper::writeSignatureData);
                  }
                  break;
               case UPDATE_GAME_MODE:
                  this.writeVarInt(data.gameMode.ordinal());
                  break;
               case UPDATE_LATENCY:
                  this.writeVarInt(data.ping);
                  break;
               case UPDATE_DISPLAY_NAME:
                  if (data.displayName != null) {
                     this.writeBoolean(true);
                     this.writeComponent(data.displayName);
                  } else {
                     this.writeBoolean(false);
                  }
               case REMOVE_PLAYER:
            }
         }
      }
   }

   public void copy(WrapperPlayServerPlayerInfo wrapper) {
      this.action = wrapper.action;
      this.playerDataList = wrapper.playerDataList;
   }

   @Nullable
   public WrapperPlayServerPlayerInfo.Action getAction() {
      return this.action;
   }

   public void setAction(@NotNull WrapperPlayServerPlayerInfo.Action action) {
      this.action = action;
   }

   public List<WrapperPlayServerPlayerInfo.PlayerData> getPlayerDataList() {
      return this.playerDataList;
   }

   public void setPlayerDataList(List<WrapperPlayServerPlayerInfo.PlayerData> playerDataList) {
      this.playerDataList = playerDataList;
   }

   public static enum Action {
      ADD_PLAYER,
      UPDATE_GAME_MODE,
      UPDATE_LATENCY,
      UPDATE_DISPLAY_NAME,
      REMOVE_PLAYER;

      public static final WrapperPlayServerPlayerInfo.Action[] VALUES = values();
   }

   public static class PlayerData {
      @Nullable
      private Component displayName;
      @Nullable
      private UserProfile userProfile;
      @Nullable
      private GameMode gameMode;
      @Nullable
      private SignatureData signatureData;
      private int ping;

      public PlayerData(
         @Nullable Component displayName, @Nullable UserProfile userProfile, @Nullable GameMode gameMode, @Nullable SignatureData signatureData, int ping
      ) {
         this.displayName = displayName;
         this.userProfile = userProfile;
         this.gameMode = gameMode;
         this.signatureData = signatureData;
         this.ping = ping;
      }

      public PlayerData(@Nullable Component displayName, @Nullable UserProfile userProfile, @Nullable GameMode gameMode, int ping) {
         this(displayName, userProfile, gameMode, null, ping);
      }

      @Nullable
      public UserProfile getUserProfile() {
         return this.userProfile;
      }

      public void setUserProfile(@Nullable UserProfile userProfile) {
         this.userProfile = userProfile;
      }

      public SignatureData getSignatureData() {
         return this.signatureData;
      }

      public void setSignatureData(SignatureData signatureData) {
         this.signatureData = signatureData;
      }

      @Deprecated
      @Nullable
      public UserProfile getUser() {
         return this.userProfile;
      }

      @Deprecated
      public void setUser(@Nullable UserProfile userProfile) {
         this.userProfile = userProfile;
      }

      @Nullable
      public GameMode getGameMode() {
         return this.gameMode;
      }

      public void setGameMode(@Nullable GameMode gameMode) {
         this.gameMode = gameMode;
      }

      public int getPing() {
         return this.ping;
      }

      public void setPing(int ping) {
         this.ping = ping;
      }

      @Nullable
      public Component getDisplayName() {
         return this.displayName;
      }

      public void setDisplayName(@Nullable Component displayName) {
         this.displayName = displayName;
      }
   }
}
