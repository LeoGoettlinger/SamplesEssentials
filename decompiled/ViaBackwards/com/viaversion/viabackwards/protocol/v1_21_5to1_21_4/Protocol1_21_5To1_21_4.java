package com.viaversion.viabackwards.protocol.v1_21_5to1_21_4;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter.BlockItemPacketRewriter1_21_5;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter.BlockPacketRewriter1_21_5;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter.ComponentRewriter1_21_5;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter.EntityPacketRewriter1_21_5;
import com.viaversion.viabackwards.protocol.v1_21_5to1_21_4.rewriter.RegistryDataRewriter1_21_5;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_2;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_4;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimPattern;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPacket1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_21_5To1_21_4
   extends BackwardsProtocol<ClientboundPacket1_21_5, ClientboundPacket1_21_2, ServerboundPacket1_21_5, ServerboundPacket1_21_4> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21.5", "1.21.4", Protocol1_21_4To1_21_5.class);
   private final EntityPacketRewriter1_21_5 entityRewriter = new EntityPacketRewriter1_21_5(this);
   private final BlockItemPacketRewriter1_21_5 itemRewriter = new BlockItemPacketRewriter1_21_5(this);
   private final ParticleRewriter<ClientboundPacket1_21_5> particleRewriter = new ParticleRewriter<ClientboundPacket1_21_5>(this) {
      public void rewriteParticle(UserConnection connection, Particle particle) {
         if (particle.id() == Protocol1_21_5To1_21_4.MAPPINGS.getParticleMappings().id("tinted_leaves")) {
            particle.getArguments().clear();
         }

         super.rewriteParticle(connection, particle);
      }
   };
   private final ComponentRewriter1_21_5 translatableRewriter = new ComponentRewriter1_21_5(this);
   private final TagRewriter<ClientboundPacket1_21_5> tagRewriter = new TagRewriter(this);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_5> recipeRewriter = new RecipeDisplayRewriter1_21_5<ClientboundPacket1_21_5>(this) {
      protected void handleSmithingTrimSlotDisplay(PacketWrapper wrapper) {
         this.handleSlotDisplay(wrapper);
         this.handleSlotDisplay(wrapper);
         wrapper.read(ArmorTrimPattern.TYPE1_21_5);
         wrapper.write(Types.VAR_INT, 0);
      }
   };
   private final BackwardsRegistryRewriter registryDataRewriter = new RegistryDataRewriter1_21_5(this);
   private final BlockRewriter<ClientboundPacket1_21_5> blockRewriter = new BlockPacketRewriter1_21_5(this);

   public Protocol1_21_5To1_21_4() {
      super(ClientboundPacket1_21_5.class, ClientboundPacket1_21_2.class, ServerboundPacket1_21_5.class, ServerboundPacket1_21_4.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.tagRewriter.renameTag(RegistryType.BLOCK, "dry_vegetation_may_place_on", "dead_bush_may_place_on");
      CommandRewriter1_19_4<ClientboundPacket1_21_5> commandRewriter = new CommandRewriter1_19_4<ClientboundPacket1_21_5>(this) {
         public void handleArgument(PacketWrapper wrapper, String argumentType) {
            if (argumentType.equals("minecraft:resource")) {
               String resource = (String)wrapper.read(Types.STRING);
               if (Key.equals(resource, "test_instance")) {
                  resource = "minecraft:item";
               }

               wrapper.write(Types.STRING, resource);
            } else if (argumentType.equals("minecraft:resource_selector")) {
               wrapper.read(Types.STRING);
               wrapper.write(Types.VAR_INT, 1);
            } else {
               super.handleArgument(wrapper, argumentType);
            }
         }
      };
      this.replaceClientbound(ClientboundPackets1_21_5.COMMANDS, commandRewriter::handle1_19);
      this.replaceClientbound(ClientboundPackets1_21_5.PLAYER_CHAT, wrapper -> {
         wrapper.read(Types.VAR_INT);
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
            wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);
         }

         wrapper.passthrough(ChatType.TYPE);
         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_TAG));
         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
      });
      this.registerServerbound(ServerboundPackets1_21_4.CHAT_COMMAND_SIGNED, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.LONG);
         int signatures = Limit.max((Integer)wrapper.passthrough(Types.VAR_INT), 8);

         for (int i = 0; i < signatures; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.SIGNATURE_BYTES);
         }

         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.ACKNOWLEDGED_BIT_SET);
         wrapper.write(Types.BYTE, (byte)0);
      });
      this.registerServerbound(ServerboundPackets1_21_4.CHAT, wrapper -> {
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.ACKNOWLEDGED_BIT_SET);
         wrapper.write(Types.BYTE, (byte)0);
      });
      this.cancelClientbound(ClientboundPackets1_21_5.TEST_INSTANCE_BLOCK_STATUS);
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_21_4.PLAYER));
      this.addItemHasher(user);
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_5 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_5 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21_5> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_5> getParticleRewriter() {
      return this.particleRewriter;
   }

   public ComponentRewriter1_21_5 getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_5> getTagRewriter() {
      return this.tagRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket1_21_5> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> types() {
      return VersionedTypes.V1_21_5;
   }

   public Types1_20_5<StructuredDataKeys1_21_2, EntityDataTypes1_21_2> mappedTypes() {
      return VersionedTypes.V1_21_4;
   }

   protected PacketTypesProvider<ClientboundPacket1_21_5, ClientboundPacket1_21_2, ServerboundPacket1_21_5, ServerboundPacket1_21_4> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21_5.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21_2.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_5.class, ServerboundConfigurationPackets1_20_5.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_4.class, ServerboundConfigurationPackets1_20_5.class}
         )
      );
   }
}
