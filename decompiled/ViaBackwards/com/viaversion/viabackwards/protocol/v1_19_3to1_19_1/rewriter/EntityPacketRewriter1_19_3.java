package com.viaversion.viabackwards.protocol.v1_19_3to1_19_1.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_19_3to1_19_1.Protocol1_19_3To1_19_1;
import com.viaversion.viabackwards.protocol.v1_19_3to1_19_1.storage.ChatTypeStorage1_19_3;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.GameProfile.Property;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_3;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession1_19_3;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_19;
import com.viaversion.viaversion.api.type.types.version.Types1_19_3;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19to1_19_1.packet.ClientboundPackets1_19_1;
import com.viaversion.viaversion.util.TagUtil;
import java.util.BitSet;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityPacketRewriter1_19_3 extends EntityRewriter<ClientboundPackets1_19_3, Protocol1_19_3To1_19_1> {
   private static final int[] PROFILE_ACTIONS = new int[]{2, 3, 4, 5};
   private static final int ADD_PLAYER = 0;
   private static final int INITIALIZE_CHAT = 1;
   private static final int UPDATE_GAMEMODE = 2;
   private static final int UPDATE_LISTED = 3;
   private static final int UPDATE_LATENCY = 4;
   private static final int UPDATE_DISPLAYNAME = 5;

   public EntityPacketRewriter1_19_3(Protocol1_19_3To1_19_1 protocol) {
      super(protocol, Types1_19.ENTITY_DATA_TYPES.optionalComponentType, Types1_19.ENTITY_DATA_TYPES.booleanType);
   }

   protected void registerPackets() {
      this.registerSetEntityData(ClientboundPackets1_19_3.SET_ENTITY_DATA, Types1_19_3.ENTITY_DATA_LIST, Types1_19.ENTITY_DATA_LIST);
      ((Protocol1_19_3To1_19_1)this.protocol).registerClientbound(ClientboundPackets1_19_3.LOGIN, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.STRING_ARRAY);
            this.map(Types.NAMED_COMPOUND_TAG);
            this.map(Types.STRING);
            this.map(Types.STRING);
            this.handler(EntityPacketRewriter1_19_3.this.dimensionDataHandler());
            this.handler(EntityPacketRewriter1_19_3.this.biomeSizeTracker());
            this.handler(EntityPacketRewriter1_19_3.this.worldDataTrackerHandlerByKey());
            this.handler(wrapper -> {
               ChatTypeStorage1_19_3 chatTypeStorage = (ChatTypeStorage1_19_3)wrapper.user().get(ChatTypeStorage1_19_3.class);
               chatTypeStorage.clear();
               CompoundTag registry = (CompoundTag)wrapper.get(Types.NAMED_COMPOUND_TAG, 0);

               for (CompoundTag chatType : TagUtil.getRegistryEntries(registry, "chat_type", new ListTag(CompoundTag.class))) {
                  NumberTag idTag = chatType.getNumberTag("id");
                  chatTypeStorage.addChatType(idTag.asInt(), chatType);
               }
            });
            this.handler(wrapper -> {
               ChatSession1_19_3 chatSession = (ChatSession1_19_3)wrapper.user().get(ChatSession1_19_3.class);
               if (chatSession != null) {
                  PacketWrapper chatSessionUpdate = wrapper.create(ServerboundPackets1_19_3.CHAT_SESSION_UPDATE);
                  chatSessionUpdate.write(Types.UUID, chatSession.getSessionId());
                  chatSessionUpdate.write(Types.PROFILE_KEY, chatSession.getProfileKey());
                  chatSessionUpdate.sendToServer(Protocol1_19_3To1_19_1.class);
               }
            });
         }
      });
      ((Protocol1_19_3To1_19_1)this.protocol).registerClientbound(ClientboundPackets1_19_3.RESPAWN, new PacketHandlers() {
         public void register() {
            this.map(Types.STRING);
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.handler(EntityPacketRewriter1_19_3.this.worldDataTrackerHandlerByKey());
            this.handler(wrapper -> {
               byte keepDataMask = (Byte)wrapper.read(Types.BYTE);
               wrapper.write(Types.BOOLEAN, (keepDataMask & 1) != 0);
            });
         }
      });
      ((Protocol1_19_3To1_19_1)this.protocol)
         .registerClientbound(ClientboundPackets1_19_3.PLAYER_INFO_UPDATE, ClientboundPackets1_19_1.PLAYER_INFO, wrapper -> {
            wrapper.cancel();
            BitSet actions = (BitSet)wrapper.read(Types.PROFILE_ACTIONS_ENUM1_19_3);
            int entries = (Integer)wrapper.read(Types.VAR_INT);
            if (actions.get(0)) {
               PacketWrapper playerInfoPacket = wrapper.create(ClientboundPackets1_19_1.PLAYER_INFO);
               playerInfoPacket.write(Types.VAR_INT, 0);
               playerInfoPacket.write(Types.VAR_INT, entries);

               for (int i = 0; i < entries; i++) {
                  playerInfoPacket.write(Types.UUID, (UUID)wrapper.read(Types.UUID));
                  playerInfoPacket.write(Types.STRING, (String)wrapper.read(Types.STRING));
                  playerInfoPacket.write(Types.PROFILE_PROPERTY_ARRAY, (Property[])wrapper.read(Types.PROFILE_PROPERTY_ARRAY));
                  ProfileKey profileKey;
                  if (actions.get(1) && (Boolean)wrapper.read(Types.BOOLEAN)) {
                     wrapper.read(Types.UUID);
                     profileKey = (ProfileKey)wrapper.read(Types.PROFILE_KEY);
                  } else {
                     profileKey = null;
                  }

                  int gamemode = actions.get(2) ? (Integer)wrapper.read(Types.VAR_INT) : 0;
                  if (actions.get(3)) {
                     wrapper.read(Types.BOOLEAN);
                  }

                  int latency = actions.get(4) ? (Integer)wrapper.read(Types.VAR_INT) : 0;
                  JsonElement displayName = actions.get(5) ? (JsonElement)wrapper.read(Types.OPTIONAL_COMPONENT) : null;
                  playerInfoPacket.write(Types.VAR_INT, gamemode);
                  playerInfoPacket.write(Types.VAR_INT, latency);
                  playerInfoPacket.write(Types.OPTIONAL_COMPONENT, displayName);
                  playerInfoPacket.write(Types.OPTIONAL_PROFILE_KEY, profileKey);
               }

               playerInfoPacket.send(Protocol1_19_3To1_19_1.class);
            } else {
               EntityPacketRewriter1_19_3.PlayerProfileUpdate[] updates = new EntityPacketRewriter1_19_3.PlayerProfileUpdate[entries];

               for (int i = 0; i < entries; i++) {
                  UUID uuid = (UUID)wrapper.read(Types.UUID);
                  int gamemode = 0;
                  int latency = 0;
                  JsonElement displayName = null;

                  for (int action : PROFILE_ACTIONS) {
                     if (actions.get(action)) {
                        switch (action) {
                           case 2:
                              gamemode = (Integer)wrapper.read(Types.VAR_INT);
                              break;
                           case 3:
                              wrapper.read(Types.BOOLEAN);
                              break;
                           case 4:
                              latency = (Integer)wrapper.read(Types.VAR_INT);
                              break;
                           case 5:
                              displayName = (JsonElement)wrapper.read(Types.OPTIONAL_COMPONENT);
                        }
                     }
                  }

                  updates[i] = new EntityPacketRewriter1_19_3.PlayerProfileUpdate(uuid, gamemode, latency, displayName);
               }

               if (actions.get(2)) {
                  this.sendPlayerProfileUpdate(wrapper.user(), 1, updates);
               } else if (actions.get(4)) {
                  this.sendPlayerProfileUpdate(wrapper.user(), 2, updates);
               } else if (actions.get(5)) {
                  this.sendPlayerProfileUpdate(wrapper.user(), 3, updates);
               }
            }
         });
      ((Protocol1_19_3To1_19_1)this.protocol)
         .registerClientbound(ClientboundPackets1_19_3.PLAYER_INFO_REMOVE, ClientboundPackets1_19_1.PLAYER_INFO, wrapper -> {
            UUID[] uuids = (UUID[])wrapper.read(Types.UUID_ARRAY);
            wrapper.write(Types.VAR_INT, 4);
            wrapper.write(Types.VAR_INT, uuids.length);

            for (UUID uuid : uuids) {
               wrapper.write(Types.UUID, uuid);
            }
         });
   }

   private void sendPlayerProfileUpdate(UserConnection connection, int action, EntityPacketRewriter1_19_3.PlayerProfileUpdate[] updates) {
      PacketWrapper playerInfoPacket = PacketWrapper.create(ClientboundPackets1_19_1.PLAYER_INFO, connection);
      playerInfoPacket.write(Types.VAR_INT, action);
      playerInfoPacket.write(Types.VAR_INT, updates.length);

      for (EntityPacketRewriter1_19_3.PlayerProfileUpdate update : updates) {
         playerInfoPacket.write(Types.UUID, update.uuid());
         if (action == 1) {
            playerInfoPacket.write(Types.VAR_INT, update.gamemode());
         } else if (action == 2) {
            playerInfoPacket.write(Types.VAR_INT, update.latency());
         } else {
            if (action != 3) {
               throw new IllegalArgumentException("Invalid action: " + action);
            }

            playerInfoPacket.write(Types.OPTIONAL_COMPONENT, update.displayName());
         }
      }

      playerInfoPacket.send(Protocol1_19_3To1_19_1.class);
   }

   public void registerRewrites() {
      this.filter().handler((event, data) -> {
         int id = data.dataType().typeId();
         if (id > 2) {
            data.setDataType(Types1_19.ENTITY_DATA_TYPES.byId(id - 1));
         } else if (id != 2) {
            data.setDataType(Types1_19.ENTITY_DATA_TYPES.byId(id));
         }
      });
      this.registerEntityDataTypeHandler(
         Types1_19.ENTITY_DATA_TYPES.itemType,
         null,
         Types1_19.ENTITY_DATA_TYPES.optionalBlockStateType,
         Types1_19.ENTITY_DATA_TYPES.particleType,
         Types1_19.ENTITY_DATA_TYPES.componentType,
         Types1_19.ENTITY_DATA_TYPES.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_19_3.ABSTRACT_MINECART, 11);
      this.filter().dataType(Types1_19.ENTITY_DATA_TYPES.poseType).handler((event, data) -> {
         int pose = (Integer)data.value();
         if (pose == 10) {
            data.setValue(0);
         } else if (pose > 10) {
            data.setValue(pose - 1);
         }
      });
      this.filter().type(EntityTypes1_19_3.CAMEL).cancel(19);
      this.filter().type(EntityTypes1_19_3.CAMEL).cancel(20);
   }

   public void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.mapEntityTypeWithData(EntityTypes1_19_3.CAMEL, EntityTypes1_19_3.DONKEY).jsonName();
   }

   public EntityType typeFromId(int typeId) {
      return EntityTypes1_19_3.getTypeFromId(typeId);
   }

   private record PlayerProfileUpdate(UUID uuid, int gamemode, int latency, @Nullable JsonElement displayName) {
   }
}
