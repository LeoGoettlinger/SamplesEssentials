package com.viaversion.viabackwards.protocol.v1_21_2to1_21;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.rewriter.BlockItemPacketRewriter1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.rewriter.ComponentRewriter1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.rewriter.EntityPacketRewriter1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.rewriter.ParticleRewriter1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.BackwardsStorables1_21_2;
import com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage.RecipeStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_21_2To1_21
   extends BackwardsProtocol<ClientboundPacket1_21_2, ClientboundPacket1_21, ServerboundPacket1_21_2, ServerboundPacket1_20_5> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21.2", "1.21", Protocol1_21To1_21_2.class);
   private final EntityPacketRewriter1_21_2 entityRewriter = new EntityPacketRewriter1_21_2(this);
   private final BlockItemPacketRewriter1_21_2 itemRewriter = new BlockItemPacketRewriter1_21_2(this);
   private final ParticleRewriter1_21_2 particleRewriter = new ParticleRewriter1_21_2(this);
   private final JsonNBTComponentRewriter<ClientboundPacket1_21_2> translatableRewriter = new ComponentRewriter1_21_2(this);
   private final TagRewriter<ClientboundPacket1_21_2> tagRewriter = new TagRewriter(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this);
   private final BlockRewriter<ClientboundPacket1_21_2> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_20_2::new);

   public Protocol1_21_2To1_21() {
      super(ClientboundPacket1_21_2.class, ClientboundPacket1_21.class, ServerboundPacket1_21_2.class, ServerboundPacket1_20_5.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.replaceClientbound(ClientboundPackets1_21_2.UPDATE_TAGS, this::storeTags);
      this.replaceClientbound(ClientboundConfigurationPackets1_21.UPDATE_TAGS, this::storeTags);
      this.registerServerbound(ServerboundPackets1_20_5.CLIENT_INFORMATION, this::clientInformation);
      this.registerServerbound(ServerboundConfigurationPackets1_20_5.CLIENT_INFORMATION, this::clientInformation);
      this.registerClientbound(ClientboundConfigurationPackets1_21.UPDATE_ENABLED_FEATURES, wrapper -> {
         String[] enabledFeatures = (String[])wrapper.read(Types.STRING_ARRAY);
         wrapper.write(Types.STRING_ARRAY, (String[])ArrayUtil.add(enabledFeatures, "bundle"));
      });
      this.cancelClientbound(ClientboundPackets1_21_2.MOVE_MINECART_ALONG_TRACK);
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
         wrapper.write(Types.BOOLEAN, true);
      });
      this.registerClientbound(ClientboundPackets1_21_2.SET_TIME, wrapper -> {
         wrapper.passthrough(Types.LONG);
         long dayTime = (Long)wrapper.read(Types.LONG);
         boolean daylightCycle = (Boolean)wrapper.read(Types.BOOLEAN);
         if (!daylightCycle) {
            if (dayTime == 0L) {
               dayTime = -1L;
            } else {
               dayTime = -dayTime;
            }
         }

         wrapper.write(Types.LONG, dayTime);
      });
   }

   private void storeTags(PacketWrapper wrapper) {
      this.tagRewriter.handleGeneric(wrapper);
      wrapper.resetReader();
      BackwardsStorables1_21_2 storables = (BackwardsStorables1_21_2)wrapper.user().storables(this);
      storables.itemTagStorage().readItemTags(wrapper);
   }

   private void clientInformation(PacketWrapper wrapper) {
      wrapper.passthrough(Types.STRING);
      wrapper.passthrough(Types.BYTE);
      wrapper.passthrough(Types.VAR_INT);
      wrapper.passthrough(Types.BOOLEAN);
      wrapper.passthrough(Types.UNSIGNED_BYTE);
      wrapper.passthrough(Types.VAR_INT);
      wrapper.passthrough(Types.BOOLEAN);
      wrapper.passthrough(Types.BOOLEAN);
      wrapper.write(Types.VAR_INT, 0);
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_20_5.PLAYER));
      BackwardsStorables1_21_2 storables = (BackwardsStorables1_21_2)user.storables(this);
      storables.setRecipeStorage(new RecipeStorage(this));
      user.put(storables.playerStorage());
   }

   public BackwardsStorables1_21_2 createStorables() {
      return new BackwardsStorables1_21_2();
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_2 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_2 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21_2> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter1_21_2 getParticleRewriter() {
      return this.particleRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPacket1_21_2> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_2> getTagRewriter() {
      return this.tagRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V1_21_2;
   }

   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V1_21;
   }

   protected PacketTypesProvider<ClientboundPacket1_21_2, ClientboundPacket1_21, ServerboundPacket1_21_2, ServerboundPacket1_20_5> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21_2.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_2.class, ServerboundConfigurationPackets1_20_5.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class}
         )
      );
   }
}
