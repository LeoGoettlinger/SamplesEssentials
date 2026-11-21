package com.viaversion.viaversion.protocols.v1_21_7to1_21_9;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_5;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_9;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter.BlockItemPacketRewriter1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter.ComponentRewriter1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter.EntityPacketRewriter1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter.ParticleRewriter1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.rewriter.RegistryDataRewriter1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.storage.ProtocolStorables1_21_9;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.LastExplosionPowerStorage;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.ProtocolStorables1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Limit;
import com.viaversion.viaversion.util.ProtocolUtil;

public final class Protocol1_21_7To1_21_9
   extends AbstractProtocol<ClientboundPacket1_21_6, ClientboundPacket1_21_9, ServerboundPacket1_21_6, ServerboundPacket1_21_9> {
   public static final MappingData MAPPINGS = new MappingDataBase("1.21.7", "1.21.9");
   private final EntityPacketRewriter1_21_9 entityRewriter = new EntityPacketRewriter1_21_9(this);
   private final BlockItemPacketRewriter1_21_9 itemRewriter = new BlockItemPacketRewriter1_21_9(this);
   private final ParticleRewriter<ClientboundPacket1_21_6> particleRewriter = new ParticleRewriter1_21_9(this);
   private final TagRewriter<ClientboundPacket1_21_6> tagRewriter = new TagRewriter<>(this);
   private final NBTComponentRewriter<ClientboundPacket1_21_6> componentRewriter = new ComponentRewriter1_21_9(this);
   private final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter1_21_9(this);
   private final BlockRewriter<ClientboundPacket1_21_6> blockRewriter = new BlockRewriter1_21_5<>(this, ChunkType1_21_5::new);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_6> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(this);

   public Protocol1_21_7To1_21_9() {
      super(ClientboundPacket1_21_6.class, ClientboundPacket1_21_9.class, ServerboundPacket1_21_6.class, ServerboundPacket1_21_9.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      SoundRewriter<ClientboundPacket1_21_6> soundRewriter = new SoundRewriter<>(this);
      this.replaceClientbound(ClientboundPackets1_21_6.EXPLODE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         float radius = 0.0F;
         int affectedBlocks = 0;
         if (wrapper.user().getProtocolInfo().serverProtocolVersion().olderThan(ProtocolVersion.v1_21_2)) {
            ProtocolStorables1_21_2 storables1_21_2 = wrapper.user().storables(Protocol1_21To1_21_2.class);
            LastExplosionPowerStorage lastExplosionPowerStorage = storables1_21_2.lastExplosionPowerStorage();
            if (lastExplosionPowerStorage != null) {
               radius = lastExplosionPowerStorage.power();
               affectedBlocks = lastExplosionPowerStorage.affectedBlocks();
            }
         }

         wrapper.write(Types.FLOAT, radius);
         wrapper.write(Types.INT, affectedBlocks);
         if (wrapper.passthrough(Types.BOOLEAN)) {
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
            wrapper.passthrough(Types.DOUBLE);
         }

         this.particleRewriter.passthroughParticle(wrapper);
         soundRewriter.soundHolderHandler().handle(wrapper);
         wrapper.write(Types.VAR_INT, 0);
      });
      this.registerServerbound(ServerboundPackets1_21_6.DEBUG_SUBSCRIPTION_REQUEST, wrapper -> {
         int count = Limit.max(wrapper.read(Types.VAR_INT), 32);

         for (int i = 0; i < count; i++) {
            int id = wrapper.read(Types.VAR_INT);
            if (id == 0) {
               wrapper.clearPacket();
               wrapper.write(Types.VAR_INT, 0);
               return;
            }
         }

         wrapper.cancel();
      });
      this.cancelServerbound(ServerboundConfigurationPackets1_21_9.ACCEPT_CODE_OF_CONDUCT);
   }

   @Override
   protected void onMappingDataLoaded() {
      EntityTypes1_21_9.initialize(this);
      ParticleType.Fillers.fill1_21_9(this);
      this.mappedTypes()
         .structuredData
         .filler(this)
         .add(
            StructuredDataKey.CUSTOM_DATA,
            StructuredDataKey.MAX_STACK_SIZE,
            StructuredDataKey.MAX_DAMAGE,
            StructuredDataKey.UNBREAKABLE1_21_5,
            StructuredDataKey.RARITY,
            StructuredDataKey.TOOLTIP_DISPLAY,
            StructuredDataKey.DAMAGE_RESISTANT1_21_2,
            StructuredDataKey.CUSTOM_NAME,
            StructuredDataKey.LORE,
            StructuredDataKey.ENCHANTMENTS1_21_5,
            StructuredDataKey.CUSTOM_MODEL_DATA1_21_4,
            StructuredDataKey.BLOCKS_ATTACKS1_21_5,
            StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5,
            StructuredDataKey.REPAIR_COST,
            StructuredDataKey.CREATIVE_SLOT_LOCK,
            StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE,
            StructuredDataKey.INTANGIBLE_PROJECTILE,
            StructuredDataKey.STORED_ENCHANTMENTS1_21_5,
            StructuredDataKey.DYED_COLOR1_21_5,
            StructuredDataKey.MAP_COLOR,
            StructuredDataKey.MAP_ID,
            StructuredDataKey.MAP_DECORATIONS,
            StructuredDataKey.MAP_POST_PROCESSING,
            StructuredDataKey.POTION_CONTENTS1_21_2,
            StructuredDataKey.SUSPICIOUS_STEW_EFFECTS,
            StructuredDataKey.WRITABLE_BOOK_CONTENT,
            StructuredDataKey.WRITTEN_BOOK_CONTENT,
            StructuredDataKey.TRIM1_21_5,
            StructuredDataKey.DEBUG_STICK_STATE,
            StructuredDataKey.ENTITY_DATA1_21_9,
            StructuredDataKey.BUCKET_ENTITY_DATA,
            StructuredDataKey.BLOCK_ENTITY_DATA1_21_9,
            StructuredDataKey.INSTRUMENT1_21_5,
            StructuredDataKey.RECIPES,
            StructuredDataKey.LODESTONE_TRACKER,
            StructuredDataKey.FIREWORK_EXPLOSION,
            StructuredDataKey.FIREWORKS,
            StructuredDataKey.PROFILE1_21_9,
            StructuredDataKey.NOTE_BLOCK_SOUND,
            StructuredDataKey.BANNER_PATTERNS,
            StructuredDataKey.BASE_COLOR,
            StructuredDataKey.POT_DECORATIONS1_20_5,
            StructuredDataKey.BLOCK_STATE,
            StructuredDataKey.BEES1_21_9,
            StructuredDataKey.LOCK1_21_2,
            StructuredDataKey.CONTAINER_LOOT,
            StructuredDataKey.TOOL1_21_5,
            StructuredDataKey.ITEM_NAME,
            StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER,
            StructuredDataKey.FOOD1_21_2,
            StructuredDataKey.JUKEBOX_PLAYABLE1_21_5,
            StructuredDataKey.ATTRIBUTE_MODIFIERS1_21_6,
            StructuredDataKey.REPAIRABLE,
            StructuredDataKey.ENCHANTABLE,
            StructuredDataKey.CONSUMABLE1_21_5,
            StructuredDataKey.USE_COOLDOWN,
            StructuredDataKey.DAMAGE,
            StructuredDataKey.EQUIPPABLE1_21_6,
            StructuredDataKey.ITEM_MODEL,
            StructuredDataKey.GLIDER,
            StructuredDataKey.TOOLTIP_STYLE,
            StructuredDataKey.DEATH_PROTECTION1_21_2,
            StructuredDataKey.WEAPON,
            StructuredDataKey.POTION_DURATION_SCALE,
            StructuredDataKey.VILLAGER_VARIANT,
            StructuredDataKey.WOLF_VARIANT,
            StructuredDataKey.WOLF_COLLAR,
            StructuredDataKey.FOX_VARIANT,
            StructuredDataKey.SALMON_SIZE,
            StructuredDataKey.PARROT_VARIANT,
            StructuredDataKey.TROPICAL_FISH_PATTERN,
            StructuredDataKey.TROPICAL_FISH_BASE_COLOR,
            StructuredDataKey.TROPICAL_FISH_PATTERN_COLOR,
            StructuredDataKey.MOOSHROOM_VARIANT,
            StructuredDataKey.RABBIT_VARIANT,
            StructuredDataKey.PIG_VARIANT,
            StructuredDataKey.FROG_VARIANT,
            StructuredDataKey.HORSE_VARIANT,
            StructuredDataKey.PAINTING_VARIANT,
            StructuredDataKey.LLAMA_VARIANT,
            StructuredDataKey.AXOLOTL_VARIANT,
            StructuredDataKey.CAT_VARIANT,
            StructuredDataKey.CAT_COLLAR,
            StructuredDataKey.SHEEP_COLOR,
            StructuredDataKey.SHULKER_COLOR,
            StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5,
            StructuredDataKey.BREAK_SOUND,
            StructuredDataKey.COW_VARIANT,
            StructuredDataKey.CHICKEN_VARIANT1_21_5,
            StructuredDataKey.WOLF_SOUND_VARIANT
         );
      super.onMappingDataLoaded();
   }

   @Override
   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_9.PLAYER));
      this.addItemHasher(connection);
   }

   public ProtocolStorables1_21_9 createStorables() {
      return new ProtocolStorables1_21_9();
   }

   @Override
   public MappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_9 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_9 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPacket1_21_6> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public RecipeDisplayRewriter<ClientboundPacket1_21_6> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public RegistryDataRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_6> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_6> getTagRewriter() {
      return this.tagRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket1_21_6> getComponentRewriter() {
      return this.componentRewriter;
   }

   public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> types() {
      return VersionedTypes.V1_21_6;
   }

   public Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_9> mappedTypes() {
      return VersionedTypes.V1_21_9;
   }

   @Override
   protected PacketTypesProvider<ClientboundPacket1_21_6, ClientboundPacket1_21_9, ServerboundPacket1_21_6, ServerboundPacket1_21_9> createPacketTypesProvider() {
      return new SimplePacketTypesProvider<>(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, ClientboundPackets1_21_9.class, ClientboundConfigurationPackets1_21_9.class),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class)
      );
   }
}
