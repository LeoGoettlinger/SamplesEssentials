package com.viaversion.viaversion.protocols.v1_20to1_20_2;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter.BlockItemPacketRewriter1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter.BlockRewriter1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter.EntityPacketRewriter1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.ConfigurationState;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.LastResourcePack;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.LastTags;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.storage.ProtocolStorables1_20_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_20To1_20_2
   extends AbstractProtocol<ClientboundPackets1_19_4, ClientboundPackets1_20_2, ServerboundPackets1_19_4, ServerboundPackets1_20_2> {
   public static final MappingData MAPPINGS = new MappingDataBase("1.20", "1.20.2");
   private final EntityPacketRewriter1_20_2 entityPacketRewriter = new EntityPacketRewriter1_20_2(this);
   private final BlockItemPacketRewriter1_20_2 itemPacketRewriter = new BlockItemPacketRewriter1_20_2(this);
   private final ParticleRewriter<ClientboundPackets1_19_4> particleRewriter = new ParticleRewriter<>(this);
   private final TagRewriter<ClientboundPackets1_19_4> tagRewriter = new TagRewriter<>(this);
   private final BlockRewriter<ClientboundPackets1_19_4> blockRewriter = new BlockRewriter1_20_2(this);

   public Protocol1_20To1_20_2() {
      super(ClientboundPackets1_19_4.class, ClientboundPackets1_20_2.class, ServerboundPackets1_19_4.class, ServerboundPackets1_20_2.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      this.registerClientbound(ClientboundPackets1_19_4.CUSTOM_PAYLOAD, new PacketHandlers() {
         @Override
         protected void register() {
            this.map(Types.STRING);
            this.handlerSoftFail(Protocol1_20To1_20_2::sanitizeCustomPayload);
         }
      });
      this.registerServerbound(ServerboundPackets1_20_2.CUSTOM_PAYLOAD, wrapper -> {
         wrapper.passthrough(Types.STRING);
         sanitizeCustomPayload(wrapper);
      });
      this.registerClientbound(ClientboundPackets1_19_4.SYSTEM_CHAT, wrapper -> {
         if (!wrapper.user().isClientSide() && !Via.getPlatform().isProxy()) {
            JsonElement component = wrapper.passthrough(Types.COMPONENT);
            if (component instanceof JsonObject object && object.has("translate")) {
               JsonElement translate = object.get("translate");
               if (translate != null && translate.getAsString().equals("multiplayer.message_not_delivered")) {
                  wrapper.cancel();
               }
            }
         }
      });
      this.registerClientbound(ClientboundPackets1_19_4.RESOURCE_PACK, wrapper -> {
         String url = wrapper.passthrough(Types.STRING);
         String hash = wrapper.passthrough(Types.STRING);
         boolean required = wrapper.passthrough(Types.BOOLEAN);
         JsonElement prompt = wrapper.passthrough(Types.OPTIONAL_COMPONENT);
         ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
         storables.setLastResourcePack(new LastResourcePack(url, hash, required, prompt));
      });
      this.replaceClientbound(ClientboundPackets1_19_4.UPDATE_TAGS, wrapper -> {
         this.tagRewriter.handleGeneric(wrapper);
         wrapper.resetReader();
         ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
         storables.setLastTags(new LastTags(wrapper));
      });
      this.registerClientbound(State.CONFIGURATION, ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, this::handleConfigTags);
      this.registerClientbound(ClientboundPackets1_19_4.SET_DISPLAY_OBJECTIVE, wrapper -> {
         byte slot = wrapper.read(Types.BYTE);
         wrapper.write(Types.VAR_INT, Integer.valueOf(slot));
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO, wrapper -> {
         wrapper.passthrough(Types.STRING);
         UUID uuid = wrapper.read(Types.UUID);
         wrapper.write(Types.OPTIONAL_UUID, uuid);
      });
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
         ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
         storables.configurationState().setBridgePhase(ConfigurationState.BridgePhase.PROFILE_SENT);
         wrapper.user().getProtocolInfo().setServerState(State.PLAY);
      });
      this.registerServerbound(State.LOGIN, ServerboundLoginPackets.LOGIN_ACKNOWLEDGED.getId(), -1, wrapper -> {
         wrapper.cancel();
         wrapper.user().getProtocolInfo().setServerState(State.PLAY);
         ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
         ConfigurationState configurationState = storables.configurationState();
         configurationState.setBridgePhase(ConfigurationState.BridgePhase.CONFIGURATION);
         configurationState.sendQueuedPackets(wrapper.user());
      });
      this.registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.FINISH_CONFIGURATION.getId(), -1, wrapper -> {
         wrapper.cancel();
         wrapper.user().getProtocolInfo().setClientState(State.PLAY);
         ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
         ConfigurationState configurationState = storables.configurationState();
         configurationState.setBridgePhase(ConfigurationState.BridgePhase.NONE);
         configurationState.sendQueuedPackets(wrapper.user());
         configurationState.clear();
      });
      this.registerServerbound(
         State.CONFIGURATION,
         ServerboundConfigurationPackets1_20_2.CLIENT_INFORMATION.getId(),
         -1,
         wrapper -> {
            ConfigurationState.ClientInformation clientInformation = new ConfigurationState.ClientInformation(
               wrapper.read(Types.STRING),
               wrapper.read(Types.BYTE),
               wrapper.read(Types.VAR_INT),
               wrapper.read(Types.BOOLEAN),
               wrapper.read(Types.UNSIGNED_BYTE),
               wrapper.read(Types.VAR_INT),
               wrapper.read(Types.BOOLEAN),
               wrapper.read(Types.BOOLEAN)
            );
            ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
            storables.configurationState().setClientInformation(clientInformation);
            wrapper.cancel();
         }
      );
      this.registerServerbound(
         State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.CUSTOM_PAYLOAD.getId(), -1, queueServerboundPacket(ServerboundPackets1_20_2.CUSTOM_PAYLOAD)
      );
      this.registerServerbound(
         State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.KEEP_ALIVE.getId(), -1, queueServerboundPacket(ServerboundPackets1_20_2.KEEP_ALIVE)
      );
      this.registerServerbound(
         State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.PONG.getId(), -1, queueServerboundPacket(ServerboundPackets1_20_2.PONG)
      );
      this.registerServerbound(State.CONFIGURATION, ServerboundConfigurationPackets1_20_2.RESOURCE_PACK.getId(), -1, PacketWrapper::cancel);
      this.cancelClientbound(ClientboundPackets1_19_4.UPDATE_ENABLED_FEATURES);
      this.registerServerbound(ServerboundPackets1_20_2.CONFIGURATION_ACKNOWLEDGED, null, wrapper -> {
         wrapper.cancel();
         ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
         ConfigurationState configurationState = storables.configurationState();
         if (configurationState.bridgePhase() == ConfigurationState.BridgePhase.REENTERING_CONFIGURATION) {
            wrapper.user().getProtocolInfo().setClientState(State.CONFIGURATION);
            configurationState.setBridgePhase(ConfigurationState.BridgePhase.CONFIGURATION);
            this.sendConfigurationPackets(wrapper.user(), configurationState.lastDimensionRegistry(), storables.lastResourcePack());
         }
      });
      this.cancelServerbound(ServerboundPackets1_20_2.CHUNK_BATCH_RECEIVED);
      this.registerServerbound(ServerboundPackets1_20_2.PING_REQUEST, null, wrapper -> {
         wrapper.cancel();
         long time = wrapper.read(Types.LONG);
         PacketWrapper responsePacket = wrapper.create(ClientboundPackets1_20_2.PONG_RESPONSE);
         responsePacket.write(Types.LONG, time);
         responsePacket.sendFuture(Protocol1_20To1_20_2.class);
      });
   }

   private void handleConfigTags(PacketWrapper wrapper) {
      this.tagRewriter.handleGeneric(wrapper);
      wrapper.resetReader();
      LastTags lastTags = new LastTags(wrapper);
      lastTags.setSentDuringConfigPhase(true);
      ProtocolStorables1_20_2 storables = wrapper.user().storables(this);
      storables.setLastTags(lastTags);
   }

   private static void sanitizeCustomPayload(PacketWrapper wrapper) {
      String channel = Key.namespaced(wrapper.get(Types.STRING, 0));
      if (channel.equals("minecraft:brand")) {
         wrapper.passthrough(Types.STRING);
         wrapper.clearInputBuffer();
      } else if (channel.equals("minecraft:debug/game_test_add_marker")) {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.passthrough(Types.INT);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.INT);
         wrapper.clearInputBuffer();
      } else if (channel.equals("minecraft:debug/game_test_clear")) {
         wrapper.clearInputBuffer();
      }
   }

   @Override
   public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws InformativeException, CancelException {
      if (direction == Direction.SERVERBOUND) {
         super.transform(direction, state, packetWrapper);
      } else {
         UserConnection connection = packetWrapper.user();
         if (connection.isActive()) {
            ProtocolStorables1_20_2 storables = connection.storables(this);
            ConfigurationState configurationBridge = storables.configurationState();
            ConfigurationState.BridgePhase phase = configurationBridge.bridgePhase();
            if (phase == ConfigurationState.BridgePhase.NONE) {
               super.transform(direction, state, packetWrapper);
            } else {
               int unmappedId = packetWrapper.getId();
               if (phase != ConfigurationState.BridgePhase.PROFILE_SENT && phase != ConfigurationState.BridgePhase.REENTERING_CONFIGURATION) {
                  if (packetWrapper.getPacketType() != null && packetWrapper.getPacketType().state() == State.CONFIGURATION) {
                     super.transform(direction, State.CONFIGURATION, packetWrapper);
                  } else if (unmappedId == ClientboundPackets1_19_4.LOGIN.getId()) {
                     super.transform(direction, State.PLAY, packetWrapper);
                  } else if (configurationBridge.queuedOrSentJoinGame()) {
                     ProtocolVersion serverProtocolVersion = Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
                     if (serverProtocolVersion.newerThanOrEqualTo(ProtocolVersion.v1_13)
                        && !connection.isClientSide()
                        && !Via.getPlatform().isProxy()
                        && unmappedId == ClientboundPackets1_19_4.SYSTEM_CHAT.getId()) {
                        super.transform(direction, State.PLAY, packetWrapper);
                     } else {
                        configurationBridge.addClientboundPacketToQueue(packetWrapper);
                        throw CancelException.generate();
                     }
                  } else {
                     if (unmappedId == ClientboundPackets1_19_4.CUSTOM_PAYLOAD.getId()) {
                        packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.CUSTOM_PAYLOAD);
                     } else if (unmappedId == ClientboundPackets1_19_4.DISCONNECT.getId()) {
                        packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.DISCONNECT);
                     } else if (unmappedId == ClientboundPackets1_19_4.KEEP_ALIVE.getId()) {
                        packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.KEEP_ALIVE);
                     } else if (unmappedId == ClientboundPackets1_19_4.PING.getId()) {
                        packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.PING);
                     } else if (unmappedId == ClientboundPackets1_19_4.UPDATE_ENABLED_FEATURES.getId()) {
                        packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.UPDATE_ENABLED_FEATURES);
                     } else {
                        if (unmappedId != ClientboundPackets1_19_4.UPDATE_TAGS.getId()) {
                           configurationBridge.addClientboundPacketToQueue(packetWrapper);
                           throw CancelException.generate();
                        }

                        this.handleConfigTags(packetWrapper);
                        packetWrapper.setPacketType(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS);
                     }
                  }
               } else {
                  if (unmappedId == ClientboundPackets1_19_4.UPDATE_TAGS.getId()) {
                     storables.setLastTags(null);
                  }

                  configurationBridge.addClientboundPacketToQueue(packetWrapper);
                  throw CancelException.generate();
               }
            }
         }
      }
   }

   public void sendConfigurationPackets(UserConnection connection, CompoundTag dimensionRegistry, @Nullable LastResourcePack lastResourcePack) {
      ProtocolInfo protocolInfo = connection.getProtocolInfo();
      protocolInfo.setServerState(State.CONFIGURATION);
      PacketWrapper registryDataPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.REGISTRY_DATA, connection);
      registryDataPacket.write(Types.COMPOUND_TAG, dimensionRegistry);
      registryDataPacket.send(Protocol1_20To1_20_2.class);
      ProtocolStorables1_20_2 storables = connection.storables(this);
      LastTags lastTags = storables.lastTags();
      boolean sentTags = false;
      if (lastTags != null) {
         if (lastTags.sentDuringConfigPhase()) {
            lastTags.setSentDuringConfigPhase(false);
            sentTags = true;
         } else {
            sentTags = lastTags.sendLastTags(connection);
         }
      }

      if (!sentTags) {
         PacketWrapper packet = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, connection);
         packet.write(Types.VAR_INT, 0);
         packet.send(Protocol1_20To1_20_2.class);
      }

      if (lastResourcePack != null && connection.getProtocolInfo().protocolVersion() == ProtocolVersion.v1_20_2) {
         PacketWrapper resourcePackPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.RESOURCE_PACK, connection);
         resourcePackPacket.write(Types.STRING, lastResourcePack.url());
         resourcePackPacket.write(Types.STRING, lastResourcePack.hash());
         resourcePackPacket.write(Types.BOOLEAN, lastResourcePack.required());
         resourcePackPacket.write(Types.OPTIONAL_COMPONENT, lastResourcePack.prompt());
         resourcePackPacket.send(Protocol1_20To1_20_2.class);
      }

      PacketWrapper finishConfigurationPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.FINISH_CONFIGURATION, connection);
      finishConfigurationPacket.send(Protocol1_20To1_20_2.class);
      protocolInfo.setServerState(State.PLAY);
   }

   private static PacketHandler queueServerboundPacket(ServerboundPackets1_20_2 packetType) {
      return wrapper -> {
         wrapper.setPacketType(packetType);
         ProtocolStorables1_20_2 storables = wrapper.user().storables(Protocol1_20To1_20_2.class);
         storables.configurationState().addServerboundPacketToQueue(wrapper);
         wrapper.cancel();
      };
   }

   @Override
   public MappingData getMappingData() {
      return MAPPINGS;
   }

   @Override
   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_19_4.PLAYER));
      ProtocolStorables1_20_2 storables = user.storables(this);
      user.put(storables.configurationState());
   }

   @Override
   public ProtocolStorablesBase createStorables() {
      return new ProtocolStorables1_20_2();
   }

   public EntityPacketRewriter1_20_2 getEntityRewriter() {
      return this.entityPacketRewriter;
   }

   public BlockItemPacketRewriter1_20_2 getItemRewriter() {
      return this.itemPacketRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPackets1_19_4> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPackets1_19_4> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPackets1_19_4> getTagRewriter() {
      return this.tagRewriter;
   }
}
