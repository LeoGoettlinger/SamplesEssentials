package com.viaversion.viaversion.protocols.v1_21_5to1_21_6;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_6;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ClientboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPacket1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.packet.ServerboundPackets1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundConfigurationPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPacket1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.rewriter.BlockItemPacketRewriter1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.rewriter.EntityPacketRewriter1_21_6;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.storage.SneakStorage;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.ProtocolUtil;
import java.util.Locale;

public final class Protocol1_21_5To1_21_6
   extends AbstractProtocol<ClientboundPacket1_21_5, ClientboundPacket1_21_6, ServerboundPacket1_21_5, ServerboundPacket1_21_6> {
   public static final MappingData MAPPINGS = new MappingDataBase("1.21.5", "1.21.6");
   private final EntityPacketRewriter1_21_6 entityRewriter = new EntityPacketRewriter1_21_6(this);
   private final BlockItemPacketRewriter1_21_6 itemRewriter = new BlockItemPacketRewriter1_21_6(this);
   private final ParticleRewriter<ClientboundPacket1_21_5> particleRewriter = new ParticleRewriter<>(this);
   private final TagRewriter<ClientboundPacket1_21_5> tagRewriter = new TagRewriter<>(this);
   private final NBTComponentRewriter<ClientboundPacket1_21_5> componentRewriter = new NBTComponentRewriter<>(this);
   private final RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(this);
   private final BlockRewriter<ClientboundPacket1_21_5> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_21_5::new);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_5> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(this);

   public Protocol1_21_5To1_21_6() {
      super(ClientboundPacket1_21_5.class, ClientboundPacket1_21_6.class, ServerboundPacket1_21_5.class, ServerboundPacket1_21_6.class);
   }

   @Override
   protected void registerPackets() {
      super.registerPackets();
      this.registryDataRewriter.addHandler("dimension_type", (key, dimension) -> {
         String effects = dimension.getString("effects");
         if (effects != null) {
            effects = Key.stripMinecraftNamespace(effects);
            if ("the_nether".equals(effects) || "the_end".equals(effects)) {
               return;
            }
         }

         if (!dimension.contains("cloud_height")) {
            dimension.putInt("cloud_height", 192);
         }
      });
      this.registerClientbound(ClientboundPackets1_21_5.CHANGE_DIFFICULTY, wrapper -> {
         short difficulty = wrapper.read(Types.UNSIGNED_BYTE);
         wrapper.write(Types.VAR_INT, Integer.valueOf(difficulty));
      });
      this.registerServerbound(ServerboundPackets1_21_6.CHANGE_DIFFICULTY, wrapper -> {
         int difficulty = wrapper.read(Types.VAR_INT);
         wrapper.write(Types.UNSIGNED_BYTE, (short)difficulty);
      });
      this.registerServerbound(ServerboundPackets1_21_6.CHANGE_GAME_MODE, ServerboundPackets1_21_5.CHAT_COMMAND, wrapper -> {
         int gameMode = wrapper.read(Types.VAR_INT);
         GameMode mode = GameMode.getById(gameMode);
         wrapper.write(Types.STRING, "gamemode " + mode.name().toLowerCase(Locale.ROOT));
      });
      this.cancelServerbound(ServerboundPackets1_21_6.CUSTOM_CLICK_ACTION);
      this.cancelServerbound(ServerboundConfigurationPackets1_21_6.CUSTOM_CLICK_ACTION);
   }

   @Override
   protected void onMappingDataLoaded() {
      EntityTypes1_21_6.initialize(this);
      ParticleType.Fillers.fill1_21_5(this);
      VersionedTypes.V1_21_6
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
            StructuredDataKey.ENTITY_DATA1_20_5,
            StructuredDataKey.BUCKET_ENTITY_DATA,
            StructuredDataKey.BLOCK_ENTITY_DATA1_20_5,
            StructuredDataKey.INSTRUMENT1_21_5,
            StructuredDataKey.RECIPES,
            StructuredDataKey.LODESTONE_TRACKER,
            StructuredDataKey.FIREWORK_EXPLOSION,
            StructuredDataKey.FIREWORKS,
            StructuredDataKey.PROFILE1_20_5,
            StructuredDataKey.NOTE_BLOCK_SOUND,
            StructuredDataKey.BANNER_PATTERNS,
            StructuredDataKey.BASE_COLOR,
            StructuredDataKey.POT_DECORATIONS1_20_5,
            StructuredDataKey.BLOCK_STATE,
            StructuredDataKey.BEES1_20_5,
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
      this.tagRewriter.renameTag(RegistryType.BLOCK, "plays_ambient_desert_block_sounds", "triggers_ambient_desert_dry_vegetation_block_sounds");
      super.onMappingDataLoaded();
   }

   @Override
   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_6.PLAYER));
      this.addItemHasher(connection);
      connection.put(new SneakStorage());
   }

   @Override
   public MappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_6 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_6 getItemRewriter() {
      return this.itemRewriter;
   }

   @Override
   public BlockRewriter<ClientboundPacket1_21_5> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public RecipeDisplayRewriter<ClientboundPacket1_21_5> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public RegistryDataRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_5> getParticleRewriter() {
      return this.particleRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_5> getTagRewriter() {
      return this.tagRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket1_21_5> getComponentRewriter() {
      return this.componentRewriter;
   }

   @Override
   public VersionedTypesHolder types() {
      return VersionedTypes.V1_21_5;
   }

   @Override
   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V1_21_6;
   }

   @Override
   protected PacketTypesProvider<ClientboundPacket1_21_5, ClientboundPacket1_21_6, ServerboundPacket1_21_5, ServerboundPacket1_21_6> createPacketTypesProvider() {
      return new SimplePacketTypesProvider<>(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, ClientboundPackets1_21_5.class, ClientboundConfigurationPackets1_21.class),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, ClientboundPackets1_21_6.class, ClientboundConfigurationPackets1_21_6.class),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, ServerboundPackets1_21_5.class, ServerboundConfigurationPackets1_20_5.class),
         ProtocolUtil.packetTypeMap(this.unmappedServerboundPacketType, ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_6.class)
      );
   }
}
