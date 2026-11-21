package com.viaversion.viabackwards.protocol.template;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v26_2to26_3.rewriter.RecipeDisplayRewriter26_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.util.ProtocolUtil;

final class Protocol99_1To98_1 extends BackwardsProtocol<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket1_21_9> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("99.1", "98.1", Protocol1_21_11To26_1.class);
   private final EntityPacketRewriter99_1 entityRewriter = new EntityPacketRewriter99_1(this);
   private final BlockItemPacketRewriter99_1 itemRewriter = new BlockItemPacketRewriter99_1(this);
   private final ParticleRewriter<ClientboundPacket26_1> particleRewriter = new ParticleRewriter(this);
   private final NBTComponentRewriter<ClientboundPacket26_1> translatableRewriter = new ComponentRewriter99_1(this);
   private final TagRewriter<ClientboundPacket26_1> tagRewriter = new TagRewriter(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this);
   private final BlockRewriter<ClientboundPacket26_1> blockRewriter = new BlockRewriter1_21_5(this, ChunkType26_1::new);
   private final RecipeDisplayRewriter<ClientboundPacket26_1> recipeRewriter = new RecipeDisplayRewriter26_3(this);

   public Protocol99_1To98_1() {
      super(ClientboundPacket26_1.class, ClientboundPacket26_1.class, ServerboundPacket1_21_9.class, ServerboundPacket1_21_9.class);
   }

   protected void registerPackets() {
      super.registerPackets();
   }

   public void init(UserConnection connection) {
      this.addEntityTracker(connection);
      this.addItemHasher(connection);
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter99_1 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter99_1 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket26_1> getBlockRewriter() {
      return this.blockRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket26_1> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket26_1> getParticleRewriter() {
      return this.particleRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket26_1> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket26_1> getTagRewriter() {
      return this.tagRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V26_1;
   }

   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V26_1;
   }

   protected PacketTypesProvider<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket1_21_9> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class}
         )
      );
   }
}
