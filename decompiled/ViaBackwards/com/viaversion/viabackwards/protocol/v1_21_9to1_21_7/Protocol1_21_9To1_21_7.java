package com.viaversion.viabackwards.protocol.v1_21_9to1_21_7;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.SoundRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.rewriter.BlockItemPacketRewriter1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.rewriter.ComponentRewriter1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.rewriter.EntityPacketRewriter1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.rewriter.ParticleRewriter1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.rewriter.RegistryDataRewriter1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage.ProtocolStorables1_21_9;
import com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.tracker.EntityTracker1_21_9;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.Protocol1_21_7To1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_21_9To1_21_7
   extends BackwardsProtocol<ClientboundPacket1_21_9, ClientboundPacket1_21_6, ServerboundPacket1_21_9, ServerboundPacket1_21_6> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21.9", "1.21.7", Protocol1_21_7To1_21_9.class);
   private final EntityPacketRewriter1_21_9 entityRewriter = new EntityPacketRewriter1_21_9(this);
   private final BlockItemPacketRewriter1_21_9 itemRewriter = new BlockItemPacketRewriter1_21_9(this);
   private final ParticleRewriter<ClientboundPacket1_21_9> particleRewriter = new ParticleRewriter1_21_9(this);
   private final NBTComponentRewriter<ClientboundPacket1_21_9> translatableRewriter = new ComponentRewriter1_21_9(this);
   private final TagRewriter<ClientboundPacket1_21_9> tagRewriter = new TagRewriter(this);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_9> recipeRewriter = new RecipeDisplayRewriter1_21_5(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new RegistryDataRewriter1_21_9(this);
   private final BlockRewriter<ClientboundPacket1_21_9> blockRewriter = new BlockRewriter1_21_5(this, ChunkType1_21_5::new);

   public Protocol1_21_9To1_21_7() {
      super(ClientboundPacket1_21_9.class, ClientboundPacket1_21_6.class, ServerboundPacket1_21_9.class, ServerboundPacket1_21_6.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      SoundRewriter<ClientboundPacket1_21_9> soundRewriter = new SoundRewriter(this);
      this.replaceClientbound(ClientboundPackets1_21_9.EXPLODE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.read(Types.FLOAT);
         wrapper.read(Types.INT);
         if ((Boolean)wrapper.passthrough(Types.BOOLEAN)) {
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
         }

         this.particleRewriter.passthroughParticle(wrapper);
         soundRewriter.soundHolderHandler().handle(wrapper);
         int blockParticles = (Integer)wrapper.read(Types.VAR_INT);

         for (int i = 0; i < blockParticles; i++) {
            wrapper.read(this.particleRewriter.particleType());
            wrapper.read(Types.FLOAT);
            wrapper.read(Types.FLOAT);
            wrapper.read(Types.VAR_INT);
         }
      });
      this.registerClientbound(ClientboundConfigurationPackets1_21_9.CODE_OF_CONDUCT, ClientboundConfigurationPackets1_21_6.SHOW_DIALOG, wrapper -> {
         boolean supportsDialogs = wrapper.user().getProtocolInfo().protocolVersion().newerThan(ProtocolVersion.v1_21_5);
         if (ViaBackwards.getConfig().codeOfConductAsDialog() && supportsDialogs) {
            String codeOfConduct = (String)wrapper.read(Types.STRING);
            CompoundTag tag = new CompoundTag();
            tag.putString("type", "minecraft:confirmation");
            tag.putString("title", this.translatableRewriter.mappedTranslationKey("multiplayer.codeOfConduct.title"));
            CompoundTag body = new CompoundTag();
            body.putString("type", "minecraft:plain_message");
            body.putString("contents", codeOfConduct);
            tag.put("body", body);
            CompoundTag yes = new CompoundTag();
            CompoundTag yesLabel = new CompoundTag();
            yesLabel.putString("translate", "gui.acknowledge");
            yes.put("label", yesLabel);
            CompoundTag acceptAction = new CompoundTag();
            acceptAction.putString("type", "minecraft:custom");
            acceptAction.putString("id", "viabackwards:ack_code_of_conduct");
            yes.put("action", acceptAction);
            tag.put("yes", yes);
            CompoundTag no = new CompoundTag();
            CompoundTag noLabel = new CompoundTag();
            noLabel.putString("translate", "menu.disconnect");
            no.put("label", noLabel);
            CompoundTag disconnectAction = new CompoundTag();
            disconnectAction.putString("type", "minecraft:custom");
            disconnectAction.putString("id", "viabackwards:disconnect");
            no.put("action", disconnectAction);
            tag.put("no", no);
            wrapper.write(Types.TRUSTED_COMPOUND_TAG, tag);
         } else {
            wrapper.cancel();
            PacketWrapper acceptPacket = wrapper.create(ServerboundConfigurationPackets1_21_9.ACCEPT_CODE_OF_CONDUCT);
            acceptPacket.sendToServer(Protocol1_21_9To1_21_7.class);
         }
      });
      this.registerServerbound(ServerboundConfigurationPackets1_21_6.CUSTOM_CLICK_ACTION, wrapper -> {
         String id = (String)wrapper.passthrough(Types.STRING);
         if ("viabackwards:ack_code_of_conduct".equals(id)) {
            wrapper.cancel();
            PacketWrapper acceptPacket = wrapper.create(ServerboundConfigurationPackets1_21_9.ACCEPT_CODE_OF_CONDUCT);
            acceptPacket.sendToServer(Protocol1_21_9To1_21_7.class);
         } else if ("viabackwards:disconnect".equals(id)) {
            wrapper.cancel();
            wrapper.user().disconnect(this.translatableRewriter.mappedTranslationKey("multiplayer.disconnect.code_of_conduct"));
         }
      });
      this.registerServerbound(ServerboundPackets1_21_6.DEBUG_SUBSCRIPTION_REQUEST, wrapper -> {
         int sampleType = (Integer)wrapper.read(Types.VAR_INT);
         if (sampleType == 0) {
            wrapper.write(Types.VAR_INT, 1);
            wrapper.write(Types.VAR_INT, 0);
         }
      });
      this.registerClientbound(ClientboundPackets1_21_9.BUNDLE_DELIMITER, wrapper -> {
         ProtocolStorables1_21_9 storables = (ProtocolStorables1_21_9)wrapper.user().storables(this);
         storables.bundleStateTracker().toggleBundling();
      });
      this.cancelClientbound(ClientboundPackets1_21_9.DEBUG_BLOCK_VALUE);
      this.cancelClientbound(ClientboundPackets1_21_9.DEBUG_CHUNK_VALUE);
      this.cancelClientbound(ClientboundPackets1_21_9.DEBUG_ENTITY_VALUE);
      this.cancelClientbound(ClientboundPackets1_21_9.DEBUG_EVENT);
      this.cancelClientbound(ClientboundPackets1_21_9.GAME_TEST_HIGHLIGHT_POS);
   }

   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTracker1_21_9(connection, EntityTypes1_21_9.PLAYER));
      connection.storables(this).setItemHasher(new ItemHasherBase(this, connection));
   }

   public ProtocolStorables1_21_9 createStorables() {
      return new ProtocolStorables1_21_9();
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_9 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_9 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21_9> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_9> getParticleRewriter() {
      return this.particleRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket1_21_9> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_9> getTagRewriter() {
      return this.tagRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket1_21_9> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V1_21_9;
   }

   public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> mappedTypes() {
      return VersionedTypes.V1_21_6;
   }

   protected PacketTypesProvider<ClientboundPacket1_21_9, ClientboundPacket1_21_6, ServerboundPacket1_21_9, ServerboundPacket1_21_6> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(
            this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21_9.class, ClientboundConfigurationPackets1_21_9.class}
         ),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class}
         )
      );
   }
}
