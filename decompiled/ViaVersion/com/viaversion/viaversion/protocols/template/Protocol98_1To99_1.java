package com.viaversion.viaversion.protocols.template;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
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
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.ProtocolUtil;

final class Protocol98_1To99_1 extends AbstractProtocol<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket1_21_9> {
   public static final MappingData MAPPINGS = new MappingDataBase("98.1", "99.1");
   private final EntityPacketRewriter99_1 entityRewriter = new EntityPacketRewriter99_1(this);
   private final BlockItemPacketRewriter99_1 itemRewriter = new BlockItemPacketRewriter99_1(this);
   private final BlockRewriter<ClientboundPacket26_1> blockRewriter = new BlockRewriter1_21_5<>(this, ChunkType26_1::new);
   private final ParticleRewriter<ClientboundPacket26_1> particleRewriter = new ParticleRewriter<>(this);
   private final TagRewriter<ClientboundPacket26_1> tagRewriter = new TagRewriter<>(this);
   private final NBTComponentRewriter<ClientboundPacket26_1> componentRewriter = new ComponentRewriter99_1(this);
   private final RecipeDisplayRewriter<ClientboundPacket26_1> recipeRewriter = new RecipeDisplayRewriter26_3<>(this);
   private final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(this);

   public Protocol98_1To99_1() {
      super(ClientboundPacket26_1.class, ClientboundPacket26_1.class, ServerboundPacket1_21_9.class, ServerboundPacket1_21_9.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
   }

   @Override
   protected void onMappingDataLoaded() {
      super.onMappingDataLoaded();
   }

   @Override
   public void init(UserConnection connection) {
      this.addEntityTracker(connection);
      this.addItemHasher(connection);
   }

   @Override
   public MappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter99_1 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter99_1 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPacket26_1> getBlockRewriter() {
      return this.blockRewriter;
   }

   public RegistryDataRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   @Override
   public RecipeDisplayRewriter<ClientboundPacket26_1> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public ParticleRewriter<ClientboundPacket26_1> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPacket26_1> getTagRewriter() {
      return this.tagRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket26_1> getComponentRewriter() {
      return this.componentRewriter;
   }

   public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes26_1> types() {
      return VersionedTypes.V26_1;
   }

   public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes26_1> mappedTypes() {
      return VersionedTypes.V26_1;
   }

   @Override
   protected PacketTypesProvider<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket1_21_9, ServerboundPacket1_21_9> createPacketTypesProvider() {
      return new SimplePacketTypesProvider<>(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class)
      );
   }
}
