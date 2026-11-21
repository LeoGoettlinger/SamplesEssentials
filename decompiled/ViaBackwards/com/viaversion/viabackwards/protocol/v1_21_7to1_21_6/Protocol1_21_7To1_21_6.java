package com.viaversion.viabackwards.protocol.v1_21_7to1_21_6;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_21_7to1_21_6.rewriter.BlockItemPacketRewriter1_21_7;
import com.viaversion.viabackwards.protocol.v1_21_7to1_21_6.rewriter.EntityPacketRewriter1_21_7;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_6;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_6to1_21_7.Protocol1_21_6To1_21_7;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_21_7To1_21_6
   extends BackwardsProtocol<ClientboundPacket1_21_6, ClientboundPacket1_21_6, ServerboundPacket1_21_6, ServerboundPacket1_21_6> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21.7", "1.21.6", Protocol1_21_6To1_21_7.class);
   private final EntityPacketRewriter1_21_7 entityRewriter = new EntityPacketRewriter1_21_7(this);
   private final BlockItemPacketRewriter1_21_7 itemRewriter = new BlockItemPacketRewriter1_21_7(this);
   private final ParticleRewriter<ClientboundPacket1_21_6> particleRewriter = new ParticleRewriter(this);
   private final NBTComponentRewriter<ClientboundPacket1_21_6> translatableRewriter = new NBTComponentRewriter(this);
   private final TagRewriter<ClientboundPacket1_21_6> tagRewriter = new TagRewriter(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_6> recipeRewriter = new RecipeDisplayRewriter1_21_5(this);
   private final BlockRewriter<ClientboundPacket1_21_6> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_21_5::new);

   public Protocol1_21_7To1_21_6() {
      super(ClientboundPacket1_21_6.class, ClientboundPacket1_21_6.class, ServerboundPacket1_21_6.class, ServerboundPacket1_21_6.class);
   }

   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_6.PLAYER));
      connection.storables(this).setItemHasher(new ItemHasherBase(this, connection));
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> types() {
      return VersionedTypes.V1_21_6;
   }

   public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> mappedTypes() {
      return VersionedTypes.V1_21_6;
   }

   public NBTComponentRewriter<ClientboundPacket1_21_6> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public EntityPacketRewriter1_21_7 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_7 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21_6> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket1_21_6> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_6> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_6> getTagRewriter() {
      return this.tagRewriter;
   }

   protected PacketTypesProvider<ClientboundPacket1_21_6, ClientboundPacket1_21_6, ServerboundPacket1_21_6, ServerboundPacket1_21_6> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(
            this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class}
         ),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class}
         )
      );
   }
}
