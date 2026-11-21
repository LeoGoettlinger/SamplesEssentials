package com.viaversion.viaversion.protocols.v1_20_5to1_21;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.AttributeModifierMappings1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.MappingData1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.BlockItemPacketRewriter1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.ComponentRewriter1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter.EntityPacketRewriter1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.ProtocolStorables1_21;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.ProtocolUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public final class Protocol1_20_5To1_21
   extends AbstractProtocol<ClientboundPacket1_20_5, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> {
   public static final MappingData1_21 MAPPINGS = new MappingData1_21();
   private final EntityPacketRewriter1_21 entityRewriter = new EntityPacketRewriter1_21(this);
   private final BlockItemPacketRewriter1_21 itemRewriter = new BlockItemPacketRewriter1_21(this);
   private final ParticleRewriter<ClientboundPacket1_20_5> particleRewriter = new ParticleRewriter<>(this);
   private final TagRewriter<ClientboundPacket1_20_5> tagRewriter = new TagRewriter<>(this);
   private final JsonNBTComponentRewriter<ClientboundPacket1_20_5> componentRewriter = new ComponentRewriter1_21(this);
   private final BlockRewriter<ClientboundPacket1_20_5> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_20_2::new);

   public Protocol1_20_5To1_21() {
      super(ClientboundPacket1_20_5.class, ClientboundPacket1_21.class, ServerboundPacket1_20_5.class, ServerboundPacket1_20_5.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      this.replaceClientbound(ClientboundPackets1_20_5.DISGUISED_CHAT, wrapper -> {
         this.componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_TAG));
         int chatType = wrapper.read(Types.VAR_INT);
         wrapper.write(ChatType.TYPE, Holder.of(chatType));
      });
      this.replaceClientbound(ClientboundPackets1_20_5.PLAYER_CHAT, wrapper -> {
         wrapper.passthrough(Types.UUID);
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES);
         wrapper.passthrough(Types.STRING);
         wrapper.passthrough(Types.LONG);
         wrapper.passthrough(Types.LONG);
         int lastSeen = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < lastSeen; i++) {
            int index = wrapper.passthrough(Types.VAR_INT);
            if (index == 0) {
               wrapper.passthrough(Types.SIGNATURE_BYTES);
            }
         }

         this.componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
         int filterMaskType = wrapper.passthrough(Types.VAR_INT);
         if (filterMaskType == 2) {
            wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE);
         }

         int chatType = wrapper.read(Types.VAR_INT);
         wrapper.write(ChatType.TYPE, Holder.of(chatType));
         this.componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_TAG));
         this.componentRewriter.processTag(wrapper.user(), wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
      });
      this.registerClientbound(ClientboundPackets1_20_5.UPDATE_ATTRIBUTES, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            int attributeId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, MAPPINGS.getNewAttributeId(attributeId));
            wrapper.passthrough(Types.DOUBLE);
            int modifierSize = wrapper.passthrough(Types.VAR_INT);

            for (int j = 0; j < modifierSize; j++) {
               UUID uuid = wrapper.read(Types.UUID);
               wrapper.write(Types.STRING, mapAttributeUUID(uuid, null));
               wrapper.passthrough(Types.DOUBLE);
               wrapper.passthrough(Types.BYTE);
            }
         }
      });
      this.registerClientbound(ClientboundPackets1_20_5.PROJECTILE_POWER, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         double xPower = wrapper.read(Types.DOUBLE);
         double yPower = wrapper.read(Types.DOUBLE);
         double zPower = wrapper.read(Types.DOUBLE);
         double accelerationPower = Math.sqrt(xPower * xPower + yPower * yPower + zPower * zPower);
         wrapper.write(Types.DOUBLE, accelerationPower);
      });
      this.appendClientbound(ClientboundPackets1_20_5.GAME_EVENT, wrapper -> {
         wrapper.resetReader();
         short event = wrapper.passthrough(Types.UNSIGNED_BYTE);
         if (event == 4) {
            float value = wrapper.passthrough(Types.FLOAT);
            if (!(value >= 0.5F)) {
               wrapper.cancel();
               PacketWrapper clientCommandPacket = wrapper.create(ServerboundPackets1_20_5.CLIENT_COMMAND);
               clientCommandPacket.write(Types.VAR_INT, 0);
               clientCommandPacket.sendToServer(Protocol1_20_5To1_21.class);
            }
         }
      });
   }

   public static String mapAttributeUUID(UUID uuid, String name) {
      String id = AttributeModifierMappings1_21.uuidToId(uuid);
      if (id != null) {
         return id;
      } else {
         if (name != null) {
            id = AttributeModifierMappings1_21.nameToId(name);
         }

         return id != null ? id : uuid.toString().toLowerCase(Locale.ROOT);
      }
   }

   public static UUID mapAttributeId(String id) {
      UUID uuid = AttributeModifierMappings1_21.idToUuid(id);
      if (uuid != null) {
         return uuid;
      } else {
         uuid = UUIDUtil.parseUUID(Key.stripNamespace(id).toUpperCase(Locale.ROOT));
         return uuid != null ? uuid : UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
      }
   }

   @Override
   protected void onMappingDataLoaded() {
      ParticleType.Fillers.fill1_20_5(this, this.mappedTypes().particle());
      VersionedTypes.V1_21
         .structuredData
         .filler(this)
         .add(StructuredDataKey.CUSTOM_DATA)
         .add(StructuredDataKey.MAX_STACK_SIZE)
         .add(StructuredDataKey.MAX_DAMAGE)
         .add(StructuredDataKey.DAMAGE)
         .add(StructuredDataKey.UNBREAKABLE1_20_5)
         .add(StructuredDataKey.RARITY)
         .add(StructuredDataKey.HIDE_TOOLTIP)
         .add(StructuredDataKey.FIRE_RESISTANT)
         .add(StructuredDataKey.CUSTOM_NAME)
         .add(StructuredDataKey.LORE)
         .add(StructuredDataKey.ENCHANTMENTS1_20_5)
         .add(StructuredDataKey.CAN_PLACE_ON1_20_5)
         .add(StructuredDataKey.CAN_BREAK1_20_5)
         .add(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5)
         .add(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP)
         .add(StructuredDataKey.REPAIR_COST)
         .add(StructuredDataKey.CREATIVE_SLOT_LOCK)
         .add(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE)
         .add(StructuredDataKey.INTANGIBLE_PROJECTILE)
         .add(StructuredDataKey.STORED_ENCHANTMENTS1_20_5)
         .add(StructuredDataKey.DYED_COLOR1_20_5)
         .add(StructuredDataKey.MAP_COLOR)
         .add(StructuredDataKey.MAP_ID)
         .add(StructuredDataKey.MAP_DECORATIONS)
         .add(StructuredDataKey.MAP_POST_PROCESSING)
         .add(StructuredDataKey.POTION_CONTENTS1_20_5)
         .add(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS)
         .add(StructuredDataKey.WRITABLE_BOOK_CONTENT)
         .add(StructuredDataKey.WRITTEN_BOOK_CONTENT)
         .add(StructuredDataKey.TRIM1_20_5)
         .add(StructuredDataKey.DEBUG_STICK_STATE)
         .add(StructuredDataKey.ENTITY_DATA1_20_5)
         .add(StructuredDataKey.BUCKET_ENTITY_DATA)
         .add(StructuredDataKey.BLOCK_ENTITY_DATA1_20_5)
         .add(StructuredDataKey.INSTRUMENT1_20_5)
         .add(StructuredDataKey.RECIPES)
         .add(StructuredDataKey.LODESTONE_TRACKER)
         .add(StructuredDataKey.FIREWORK_EXPLOSION)
         .add(StructuredDataKey.FIREWORKS)
         .add(StructuredDataKey.PROFILE1_20_5)
         .add(StructuredDataKey.NOTE_BLOCK_SOUND)
         .add(StructuredDataKey.BANNER_PATTERNS)
         .add(StructuredDataKey.BASE_COLOR)
         .add(StructuredDataKey.POT_DECORATIONS1_20_5)
         .add(StructuredDataKey.BLOCK_STATE)
         .add(StructuredDataKey.BEES1_20_5)
         .add(StructuredDataKey.LOCK1_20_5)
         .add(StructuredDataKey.CONTAINER_LOOT)
         .add(StructuredDataKey.TOOL1_20_5)
         .add(StructuredDataKey.ITEM_NAME)
         .add(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER)
         .add(StructuredDataKey.FOOD1_21)
         .add(StructuredDataKey.JUKEBOX_PLAYABLE1_21)
         .add(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21);
      super.onMappingDataLoaded();
      this.tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:blocks_wind_charge_explosions");
      this.tagRewriter
         .addEmptyTags(
            RegistryType.ENTITY,
            "minecraft:can_turn_in_boats",
            "minecraft:deflects_projectiles",
            "minecraft:immune_to_infested",
            "minecraft:immune_to_oozing",
            "minecraft:no_anger_from_wind_charge"
         );
      this.tagRewriter.addTag(RegistryType.ENCHANTMENT, "minecraft:curse", 10, 41);
      this.tagRewriter.addTag(RegistryType.ENCHANTMENT, "minecraft:prevents_decorated_pot_shattering", 21);
      this.tagRewriter
         .addEmptyTags(
            RegistryType.ENCHANTMENT,
            "double_trade_price",
            "in_enchanting_table",
            "non_treasure",
            "on_mob_spawn_equipment",
            "on_random_loot",
            "on_traded_equipment",
            "prevents_bee_spawns_when_mining",
            "prevents_ice_melting",
            "prevents_infested_spawns",
            "smelts_loot",
            "tooltip_order",
            "tradeable",
            "treasure",
            "exclusive_set/armor",
            "exclusive_set/boots",
            "exclusive_set/bow",
            "exclusive_set/crossbow",
            "exclusive_set/damage",
            "exclusive_set/mining",
            "exclusive_set/riptide"
         );
   }

   @Override
   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
   }

   @Override
   public ProtocolStorablesBase createStorables() {
      return new ProtocolStorables1_21();
   }

   public MappingData1_21 getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPacket1_20_5> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_20_5> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPacket1_20_5> getTagRewriter() {
      return this.tagRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPacket1_20_5> getComponentRewriter() {
      return this.componentRewriter;
   }

   @Override
   public VersionedTypesHolder types() {
      return VersionedTypes.V1_20_5;
   }

   @Override
   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V1_21;
   }

   @Override
   protected PacketTypesProvider<ClientboundPacket1_20_5, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> createPacketTypesProvider() {
      return new SimplePacketTypesProvider<>(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, ClientboundPackets1_20_5.class, ClientboundConfigurationPackets1_20_5.class),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class)
      );
   }
}
