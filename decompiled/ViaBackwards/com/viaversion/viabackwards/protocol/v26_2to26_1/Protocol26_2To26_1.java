package com.viaversion.viabackwards.protocol.v26_2to26_1;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v26_2to26_1.rewriter.BlockItemPacketRewriter26_2;
import com.viaversion.viabackwards.protocol.v26_2to26_1.rewriter.ComponentRewriter26_2;
import com.viaversion.viabackwards.protocol.v26_2to26_1.rewriter.EntityPacketRewriter26_2;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys26_2;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.api.type.types.version.Types26_1;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v26_1to26_2.Protocol26_1To26_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol26_2To26_1 extends BackwardsProtocol<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket26_1, ServerboundPacket26_1> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("26.2", "26.1", Protocol26_1To26_2.class);
   private static final int WHITE_TEAM_COLOR = 15;
   private final EntityPacketRewriter26_2 entityRewriter = new EntityPacketRewriter26_2(this);
   private final BlockItemPacketRewriter26_2 itemRewriter = new BlockItemPacketRewriter26_2(this);
   private final ParticleRewriter<ClientboundPacket26_1> particleRewriter = new ParticleRewriter(this);
   private final NBTComponentRewriter<ClientboundPacket26_1> translatableRewriter = new ComponentRewriter26_2(this);
   private final TagRewriter<ClientboundPacket26_1> tagRewriter = new TagRewriter(this);
   private final BlockRewriter<ClientboundPacket26_1> blockRewriter = new BlockRewriter1_21_5(this, ChunkType26_1::new);
   private final RecipeDisplayRewriter<ClientboundPacket26_1> recipeRewriter = new RecipeDisplayRewriter1_21_5(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this) {
      public void updateEnchantmentTerm(CompoundTag term) {
         super.updateEnchantmentTerm(term);
         String condition = term.getString("condition");
         if (Key.equals(condition, "entity_properties")) {
            CompoundTag predicate = term.getCompoundTag("predicate");
            if (predicate != null) {
               Tag typeTag = predicate.remove("entity_type");
               if (typeTag != null) {
                  predicate.put("type", typeTag);
               }
            }
         }
      }
   };

   public Protocol26_2To26_1() {
      super(ClientboundPacket26_1.class, ClientboundPacket26_1.class, ServerboundPacket26_1.class, ServerboundPacket26_1.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.replaceClientbound(ClientboundPackets26_1.SET_PLAYER_TEAM, wrapper -> {
         wrapper.passthrough(Types.STRING);
         byte action = (Byte)wrapper.passthrough(Types.BYTE);
         if (action == 0 || action == 2) {
            this.translatableRewriter.passthroughAndProcess(wrapper);
            Tag prefix = (Tag)wrapper.read(Types.TAG);
            Tag suffix = (Tag)wrapper.read(Types.TAG);
            this.translatableRewriter.processTag(wrapper.user(), prefix);
            this.translatableRewriter.processTag(wrapper.user(), suffix);
            int nametagVisibility = (Integer)wrapper.read(Types.VAR_INT);
            int collisionRule = (Integer)wrapper.read(Types.VAR_INT);
            Integer color = (Integer)wrapper.read(Types.BOOL_OPTIONAL_VAR_INT);
            byte flags = (Byte)wrapper.read(Types.BYTE);
            wrapper.write(Types.BYTE, flags);
            wrapper.write(Types.VAR_INT, nametagVisibility);
            wrapper.write(Types.VAR_INT, collisionRule);
            wrapper.write(Types.VAR_INT, color != null ? color : 15);
            wrapper.write(Types.TAG, prefix);
            wrapper.write(Types.TAG, suffix);
         }
      });
      this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_FINISHED, wrapper -> {
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
         wrapper.read(Types.UUID);
      });
      this.registryDataRewriter.remove("sulfur_cube_archetype");
   }

   public void init(UserConnection connection) {
      this.addEntityTracker(connection);
      this.addItemHasher(connection);
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter26_2 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter26_2 getItemRewriter() {
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

   public Types26_1<StructuredDataKeys26_2, EntityDataTypes26_1> types() {
      return VersionedTypes.V26_2;
   }

   public Types26_1<StructuredDataKeys1_21_11, EntityDataTypes26_1> mappedTypes() {
      return VersionedTypes.V26_1;
   }

   protected PacketTypesProvider<ClientboundPacket26_1, ClientboundPacket26_1, ServerboundPacket26_1, ServerboundPacket26_1> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets26_1.class, ServerboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, new Class[]{ServerboundPackets26_1.class, ServerboundConfigurationPackets1_21_9.class})
      );
   }
}
