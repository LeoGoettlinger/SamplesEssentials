package com.viaversion.viabackwards.protocol.v26_3to26_2;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter.BlockItemPacketRewriter26_3;
import com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter.BlockPacketRewriter26_3;
import com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter.ComponentRewriter26_3;
import com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter.EntityPacketRewriter26_3;
import com.viaversion.viabackwards.protocol.v26_3to26_2.rewriter.RegistryDataRewriter26_3;
import com.viaversion.viabackwards.protocol.v26_3to26_2.storage.ProtocolStorables26_3;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundConfigurationPackets26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPacket26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPacket26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPackets26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.rewriter.RecipeDisplayRewriter26_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ProtocolUtil;
import java.util.BitSet;

public final class Protocol26_3To26_2 extends BackwardsProtocol<ClientboundPacket26_3, ClientboundPacket26_1, ServerboundPacket26_3, ServerboundPacket26_1> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("26.3", "26.2", Protocol26_2To26_3.class);
   private final EntityPacketRewriter26_3 entityRewriter = new EntityPacketRewriter26_3(this);
   private final BlockItemPacketRewriter26_3 itemRewriter = new BlockItemPacketRewriter26_3(this);
   private final ParticleRewriter<ClientboundPacket26_3> particleRewriter = new ParticleRewriter(this);
   private final NBTComponentRewriter<ClientboundPacket26_3> translatableRewriter = new ComponentRewriter26_3(this);
   private final TagRewriter<ClientboundPacket26_3> tagRewriter = new TagRewriter(this);
   private final BlockRewriter<ClientboundPacket26_3> blockRewriter = new BlockPacketRewriter26_3(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new RegistryDataRewriter26_3(this);
   private final RecipeDisplayRewriter<ClientboundPacket26_3> recipeRewriter = new RecipeDisplayRewriter26_3<ClientboundPacket26_3>(this) {
      protected void handleSlotDisplay(PacketWrapper wrapper) {
         FullMappings mappings = this.protocol.getMappingData().getSlotDisplayMappings();
         int type = (Integer)wrapper.passthrough(Types.VAR_INT);
         wrapper.rewindReader(1);
         if (type != mappings.id("tag")) {
            super.handleSlotDisplay(wrapper);
         } else {
            wrapper.read(Types.VAR_INT);
            HolderSet items = (HolderSet)wrapper.read(Types.HOLDER_SET);
            if (items.hasTagKey()) {
               wrapper.write(Types.VAR_INT, mappings.getNewId(type));
               wrapper.write(Types.STRING, items.tagKey());
            } else {
               wrapper.write(Types.VAR_INT, mappings.mappedId("composite"));
               wrapper.write(Types.VAR_INT, items.ids().length);

               for (int id : items.ids()) {
                  wrapper.write(Types.VAR_INT, mappings.mappedId("item"));
                  wrapper.write(Types.VAR_INT, this.rewriteItemId(id));
               }
            }
         }
      }
   };

   public Protocol26_3To26_2() {
      super(ClientboundPacket26_3.class, ClientboundPacket26_1.class, ServerboundPacket26_3.class, ServerboundPacket26_1.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.tagRewriter.renameTag(RegistryType.BLOCK, "convertible_to_mud", "convertable_to_mud");
      this.registryDataRewriter.remove("decorated_pot_pattern");
      this.registryDataRewriter.remove("block_transformer");
      this.registryDataRewriter.remove("worldgen/block_state_provider");
      this.registryDataRewriter.addHandler("trim_material", (key, tag) -> {
         StringTag paletteId = (StringTag)tag.removeUnchecked("palette_id");
         tag.putString("asset_name", com.viaversion.viaversion.protocols.v26_2to26_3.rewriter.BlockItemPacketRewriter26_3.trimAssetName(paletteId.getValue()));
      });
      this.registryDataRewriter.addHandler("dimension_type", (key, tag) -> this.handleEnvironmentAttributes(tag));
      this.registryDataRewriter.addHandler("worldgen/biome", (key, tag) -> this.handleEnvironmentAttributes(tag));
      CommandRewriter1_19_4<ClientboundPacket26_3> commandRewriter = new CommandRewriter1_19_4<ClientboundPacket26_3>(this) {
         public void handleArgument(PacketWrapper wrapper, String argumentType) {
            if (!argumentType.equals("minecraft:feature")
               && !argumentType.equals("minecraft:slot_source")
               && !argumentType.equals("minecraft:swing_animation")
               && !argumentType.equals("minecraft:context_float_provider")
               && !argumentType.equals("minecraft:context_int_provider")) {
               super.handleArgument(wrapper, argumentType);
            } else {
               wrapper.write(Types.VAR_INT, 1);
            }
         }
      };
      this.replaceClientbound(ClientboundPackets26_3.COMMANDS, commandRewriter::handle1_19);
      this.replaceClientbound(ClientboundPackets26_3.PLAYER_CHAT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.LONG);
         int lastSeen = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < lastSeen; i++) {
            int index = (Integer)wrapper.passthrough(Types.VAR_INT);
            if (index == 0) {
               wrapper.passthrough(Types.SIGNATURE_BYTES);
            }
         }

         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
         int filterMaskType = (Integer)wrapper.passthrough(Types.VAR_INT);
         if (filterMaskType == 2) {
            BitSet mask = (BitSet)wrapper.read(Types.BIT_SET);
            wrapper.write(Types.LONG_ARRAY_PRIMITIVE, mask.toLongArray());
         }

         wrapper.passthrough(ChatType.TYPE);
         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_TAG));
         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
      });
      this.cancelClientbound(ClientboundPackets26_3.ADD_TRANSIENT_BLOCK);
      this.cancelClientbound(ClientboundPackets26_3.POST_EFFECTS);
      this.cancelClientbound(ClientboundConfigurationPackets26_3.POST_EFFECTS);
   }

   private void handleEnvironmentAttributes(CompoundTag tag) {
      CompoundTag attributes = tag.getCompoundTag("attributes");
      if (attributes != null) {
         for (Tag value : attributes.values()) {
            if (value instanceof CompoundTag compoundTag) {
               StringTag modifier = compoundTag.getStringTag("modifier");
               if (modifier != null && (modifier.getValue().equals("append") || modifier.getValue().equals("overlay"))) {
                  modifier.setValue("override");
               }
            }
         }
      }
   }

   public void init(UserConnection connection) {
      this.addEntityTracker(connection);
      this.addItemHasher(connection);
   }

   public ProtocolStorablesBase createStorables() {
      return new ProtocolStorables26_3();
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter26_3 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter26_3 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket26_3> getBlockRewriter() {
      return this.blockRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket26_3> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket26_3> getParticleRewriter() {
      return this.particleRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket26_3> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket26_3> getTagRewriter() {
      return this.tagRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V26_3;
   }

   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V26_2;
   }

   protected PacketTypesProvider<ClientboundPacket26_3, ClientboundPacket26_1, ServerboundPacket26_3, ServerboundPacket26_1> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets26_3.class, ClientboundConfigurationPackets26_3.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets26_3.class, ServerboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, new Class[]{ServerboundPackets26_1.class, ServerboundConfigurationPackets1_21_9.class})
      );
   }
}
