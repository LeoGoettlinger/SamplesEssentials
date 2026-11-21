package com.viaversion.viabackwards.protocol.v1_20_5to1_20_3;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.provider.TransferProvider;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.rewriter.BlockItemPacketRewriter1_20_5;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.rewriter.BlockPacketRewriter1_20_5;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.rewriter.ComponentRewriter1_20_5;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.rewriter.EntityPacketRewriter1_20_5;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.storage.CookieStorage;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.storage.RegistryDataStorage;
import com.viaversion.viabackwards.protocol.v1_20_5to1_20_3.storage.SecureChatStorage;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.BannerPatternStorage;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_20_5To1_20_3
   extends BackwardsProtocol<ClientboundPacket1_20_5, ClientboundPacket1_20_3, ServerboundPacket1_20_5, ServerboundPacket1_20_3> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.20.5", "1.20.3", Protocol1_20_3To1_20_5.class);
   private final EntityPacketRewriter1_20_5 entityRewriter = new EntityPacketRewriter1_20_5(this);
   private final BlockItemPacketRewriter1_20_5 itemRewriter = new BlockItemPacketRewriter1_20_5(this);
   private final ParticleRewriter<ClientboundPacket1_20_5> particleRewriter = new ParticleRewriter(this);
   private final JsonNBTComponentRewriter<ClientboundPacket1_20_5> translatableRewriter = new ComponentRewriter1_20_5(this);
   private final TagRewriter<ClientboundPacket1_20_5> tagRewriter = new TagRewriter(this);
   private final BlockPacketRewriter1_20_5 blockRewriter = new BlockPacketRewriter1_20_5(this);

   public Protocol1_20_5To1_20_3() {
      super(ClientboundPacket1_20_5.class, ClientboundPacket1_20_3.class, ServerboundPacket1_20_5.class, ServerboundPacket1_20_3.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.tagRewriter.renameTag(RegistryType.ITEM, "axolotl_food", "axolotl_tempt_items");
      this.replaceClientbound(ClientboundConfigurationPackets1_20_5.UPDATE_TAGS, wrapper -> {
         this.sendRegistryData(wrapper.user());
         this.tagRewriter.handleGeneric(wrapper);
      });
      this.appendClientbound(ClientboundConfigurationPackets1_20_5.FINISH_CONFIGURATION, wrapper -> this.sendRegistryData(wrapper.user()));
      this.appendClientbound(
         ClientboundPackets1_20_5.START_CONFIGURATION, wrapper -> ((RegistryDataStorage)wrapper.user().get(RegistryDataStorage.class)).clear()
      );
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
         wrapper.passthrough(Types.BYTE_ARRAY_PRIMITIVE);
         wrapper.read(Types.BOOLEAN);
      });
      this.replaceClientbound(ClientboundPackets1_20_5.SERVER_DATA, wrapper -> {
         this.translatableRewriter.passthroughAndProcess(wrapper);
         wrapper.passthrough(Types.OPTIONAL_BYTE_ARRAY_PRIMITIVE);
         wrapper.write(Types.BOOLEAN, ((SecureChatStorage)wrapper.user().get(SecureChatStorage.class)).enforcesSecureChat());
      });
      this.registerServerbound(ServerboundPackets1_20_3.CHAT_COMMAND, ServerboundPackets1_20_5.CHAT_COMMAND_SIGNED);
      this.registerClientbound(
         State.LOGIN, ClientboundLoginPackets.COOKIE_REQUEST.getId(), -1, wrapper -> this.handleCookieRequest(wrapper, ServerboundLoginPackets.COOKIE_RESPONSE)
      );
      this.cancelClientbound(ClientboundConfigurationPackets1_20_5.RESET_CHAT);
      this.registerClientbound(
         ClientboundConfigurationPackets1_20_5.COOKIE_REQUEST,
         null,
         wrapper -> this.handleCookieRequest(wrapper, ServerboundConfigurationPackets1_20_5.COOKIE_RESPONSE)
      );
      this.registerClientbound(ClientboundConfigurationPackets1_20_5.STORE_COOKIE, null, this::handleStoreCookie);
      this.registerClientbound(ClientboundConfigurationPackets1_20_5.TRANSFER, null, this::handleTransfer);
      this.registerClientbound(
         ClientboundPackets1_20_5.COOKIE_REQUEST, null, wrapper -> this.handleCookieRequest(wrapper, ServerboundPackets1_20_5.COOKIE_RESPONSE)
      );
      this.registerClientbound(ClientboundPackets1_20_5.STORE_COOKIE, null, this::handleStoreCookie);
      this.registerClientbound(ClientboundPackets1_20_5.TRANSFER, null, this::handleTransfer);
      this.registerClientbound(ClientboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS, null, wrapper -> {
         wrapper.cancel();
         PacketWrapper response = wrapper.create(ServerboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS);
         response.write(Types.VAR_INT, 0);
         response.sendToServer(Protocol1_20_5To1_20_3.class);
      });
      CommandRewriter1_19_4<ClientboundPacket1_20_5> commandRewriter = new CommandRewriter1_19_4<ClientboundPacket1_20_5>(this) {
         public void handleArgument(PacketWrapper wrapper, String argumentType) {
            if (!argumentType.equals("minecraft:loot_table")
               && !argumentType.equals("minecraft:loot_predicate")
               && !argumentType.equals("minecraft:loot_modifier")) {
               super.handleArgument(wrapper, argumentType);
            } else {
               wrapper.write(Types.VAR_INT, 0);
            }
         }
      };
      this.replaceClientbound(ClientboundPackets1_20_5.COMMANDS, commandRewriter::handle1_19);
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
         wrapper.read(Types.BOOLEAN);
      });
      this.cancelClientbound(ClientboundPackets1_20_5.PROJECTILE_POWER);
      this.cancelClientbound(ClientboundPackets1_20_5.DEBUG_SAMPLE);
   }

   private void sendRegistryData(UserConnection connection) {
      RegistryDataStorage registryDataStorage = (RegistryDataStorage)connection.get(RegistryDataStorage.class);
      CompoundTag registryData = registryDataStorage.registryData();
      if (!registryDataStorage.sentRegistryData() && !registryData.isEmpty()) {
         PacketWrapper registryDataPacket = PacketWrapper.create(ClientboundConfigurationPackets1_20_3.REGISTRY_DATA, connection);
         registryDataPacket.write(Types.COMPOUND_TAG, registryData.copy());
         registryDataPacket.send(Protocol1_20_5To1_20_3.class);
         registryDataStorage.setSentRegistryData();
      }
   }

   private void handleStoreCookie(PacketWrapper wrapper) {
      wrapper.cancel();
      String resourceLocation = (String)wrapper.read(Types.STRING);
      byte[] data = (byte[])wrapper.read(Types.BYTE_ARRAY_PRIMITIVE);
      if (data.length > 5120) {
         throw new IllegalArgumentException("Cookie data too large");
      } else {
         ((CookieStorage)wrapper.user().get(CookieStorage.class)).cookies().put(resourceLocation, data);
      }
   }

   private void handleCookieRequest(PacketWrapper wrapper, ServerboundPacketType responseType) {
      wrapper.cancel();
      String resourceLocation = (String)wrapper.read(Types.STRING);
      byte[] data = ((CookieStorage)wrapper.user().get(CookieStorage.class)).cookies().get(resourceLocation);
      PacketWrapper responsePacket = wrapper.create(responseType);
      responsePacket.write(Types.STRING, resourceLocation);
      responsePacket.write(Types.OPTIONAL_BYTE_ARRAY_PRIMITIVE, data);
      responsePacket.sendToServer(Protocol1_20_5To1_20_3.class);
   }

   private void handleTransfer(PacketWrapper wrapper) {
      wrapper.cancel();
      String host = (String)wrapper.read(Types.STRING);
      int port = (Integer)wrapper.read(Types.VAR_INT);
      ((TransferProvider)Via.getManager().getProviders().get(TransferProvider.class)).connectToServer(wrapper.user(), host, port);
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_20_5.PLAYER));
      user.put(new SecureChatStorage());
      user.put(new CookieStorage());
      user.put(new RegistryDataStorage());
      user.put(new BannerPatternStorage());
      user.put(new ArmorTrimStorage());
   }

   public void register(ViaProviders providers) {
      providers.register(TransferProvider.class, TransferProvider.NOOP);
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_20_5 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_20_5 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockPacketRewriter1_20_5 getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_20_5> getParticleRewriter() {
      return this.particleRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPacket1_20_5> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_20_5> getTagRewriter() {
      return this.tagRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V1_20_5;
   }

   public VersionedTypesHolder mappedTypes() {
      return new Types1_20_3();
   }

   protected PacketTypesProvider<ClientboundPacket1_20_5, ClientboundPacket1_20_3, ServerboundPacket1_20_5, ServerboundPacket1_20_3> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(
            this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_20_5.class, ClientboundConfigurationPackets1_20_5.class}
         ),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_20_3.class, ClientboundConfigurationPackets1_20_3.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_20_3.class, ServerboundConfigurationPackets1_20_2.class}
         )
      );
   }
}
