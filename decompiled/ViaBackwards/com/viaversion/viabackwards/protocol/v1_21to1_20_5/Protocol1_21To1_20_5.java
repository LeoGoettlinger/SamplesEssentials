package com.viaversion.viabackwards.protocol.v1_21to1_20_5;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.rewriter.BlockItemPacketRewriter1_21;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.rewriter.ComponentRewriter1_21;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.rewriter.EntityPacketRewriter1_21;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.EnchantmentsPaintingsStorage;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.OpenScreenStorage;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.PlayerRotationStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_20_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.Types1_21;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_21To1_20_5
   extends BackwardsProtocol<ClientboundPacket1_21, ClientboundPacket1_20_5, ServerboundPacket1_20_5, ServerboundPacket1_20_5> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21", "1.20.5", Protocol1_20_5To1_21.class);
   private final EntityPacketRewriter1_21 entityRewriter = new EntityPacketRewriter1_21(this);
   private final BlockItemPacketRewriter1_21 itemRewriter = new BlockItemPacketRewriter1_21(this);
   private final ParticleRewriter<ClientboundPacket1_21> particleRewriter = new ParticleRewriter(this);
   private final JsonNBTComponentRewriter<ClientboundPacket1_21> translatableRewriter = new ComponentRewriter1_21(this);
   private final TagRewriter<ClientboundPacket1_21> tagRewriter = new TagRewriter(this);
   private final BlockRewriter<ClientboundPacket1_21> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_20_2::new);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this);

   public Protocol1_21To1_20_5() {
      super(ClientboundPacket1_21.class, ClientboundPacket1_20_5.class, ServerboundPacket1_20_5.class, ServerboundPacket1_20_5.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.cancelClientbound(ClientboundPackets1_21.PROJECTILE_POWER);
      this.cancelClientbound(ClientboundPackets1_21.CUSTOM_REPORT_DETAILS);
      this.cancelClientbound(ClientboundPackets1_21.SERVER_LINKS);
      this.cancelClientbound(ClientboundConfigurationPackets1_21.CUSTOM_REPORT_DETAILS);
      this.cancelClientbound(ClientboundConfigurationPackets1_21.SERVER_LINKS);
      this.replaceClientbound(ClientboundPackets1_21.DISGUISED_CHAT, wrapper -> {
         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_TAG));
         Holder<ChatType> chatType = (Holder<ChatType>)wrapper.read(ChatType.TYPE);
         if (chatType.isDirect()) {
            wrapper.write(Types.VAR_INT, 0);
         } else {
            wrapper.write(Types.VAR_INT, chatType.id());
         }
      });
      this.replaceClientbound(ClientboundPackets1_21.PLAYER_CHAT, wrapper -> {
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

         wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG);
         int filterMaskType = (Integer)wrapper.passthrough(Types.VAR_INT);
         if (filterMaskType == 2) {
            wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);
         }

         Holder<ChatType> chatType = (Holder<ChatType>)wrapper.read(ChatType.TYPE);
         if (chatType.isDirect()) {
            wrapper.write(Types.VAR_INT, 0);
         } else {
            wrapper.write(Types.VAR_INT, chatType.id());
         }

         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_TAG));
         this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
      });
      this.replaceClientbound(ClientboundPackets1_21.UPDATE_ATTRIBUTES, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = (Integer)wrapper.passthrough(Types.VAR_INT);
         int newSize = size;

         for (int i = 0; i < size; i++) {
            int attributeId = (Integer)wrapper.read(Types.VAR_INT);
            int mappedId = MAPPINGS.getNewAttributeId(attributeId);
            if (mappedId == -1) {
               newSize--;
               wrapper.read(Types.DOUBLE);
               int modifierSize = (Integer)wrapper.read(Types.VAR_INT);

               for (int j = 0; j < modifierSize; j++) {
                  wrapper.read(Types.STRING);
                  wrapper.read(Types.DOUBLE);
                  wrapper.read(Types.BYTE);
               }
            } else {
               wrapper.write(Types.VAR_INT, mappedId);
               wrapper.passthrough(Types.DOUBLE);
               int modifierSize = (Integer)wrapper.passthrough(Types.VAR_INT);

               for (int j = 0; j < modifierSize; j++) {
                  String id = (String)wrapper.read(Types.STRING);
                  wrapper.write(Types.UUID, Protocol1_20_5To1_21.mapAttributeId(id));
                  wrapper.passthrough(Types.DOUBLE);
                  wrapper.passthrough(Types.BYTE);
               }
            }
         }

         if (size != newSize) {
            wrapper.set(Types.VAR_INT, 1, newSize);
         }
      });
      this.registerClientbound(ClientboundConfigurationPackets1_21.UPDATE_ENABLED_FEATURES, wrapper -> {
         String[] enabledFeatures = (String[])wrapper.read(Types.STRING_ARRAY);
         wrapper.write(Types.STRING_ARRAY, (String[])ArrayUtil.add(enabledFeatures, "minecraft:update_1_21"));
      });
      this.appendClientbound(ClientboundPackets1_21.GAME_EVENT, wrapper -> {
         wrapper.resetReader();
         short event = (Short)wrapper.passthrough(Types.UNSIGNED_BYTE);
         if (event == 4) {
            wrapper.read(Types.FLOAT);
            wrapper.write(Types.FLOAT, 1.0F);
         }
      });
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_20_5.PLAYER));
      user.put(new EnchantmentsPaintingsStorage());
      user.put(new OpenScreenStorage());
      user.put(new PlayerRotationStorage());
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21> getParticleRewriter() {
      return this.particleRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPacket1_21> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_21> getTagRewriter() {
      return this.tagRewriter;
   }

   public Types1_21 types() {
      return VersionedTypes.V1_21;
   }

   public Types1_20_5<StructuredDataKeys1_20_5, EntityDataTypes1_20_5> mappedTypes() {
      return VersionedTypes.V1_20_5;
   }

   protected PacketTypesProvider<ClientboundPacket1_21, ClientboundPacket1_20_5, ServerboundPacket1_20_5, ServerboundPacket1_20_5> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_20_5.class, ClientboundConfigurationPackets1_20_5.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class}
         )
      );
   }
}
