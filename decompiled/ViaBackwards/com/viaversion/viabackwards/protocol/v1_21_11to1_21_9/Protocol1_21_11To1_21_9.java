package com.viaversion.viabackwards.protocol.v1_21_11to1_21_9;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.rewriter.BlockItemPacketRewriter1_21_11;
import com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.rewriter.ComponentRewriter1_21_11;
import com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.rewriter.EntityPacketRewriter1_21_11;
import com.viaversion.viabackwards.protocol.v1_21_11to1_21_9.storage.ProtocolStorables1_21_11;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.ProtocolUtil;
import com.viaversion.viaversion.util.TagUtil;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Protocol1_21_11To1_21_9
   extends BackwardsProtocol<ClientboundPacket1_21_11, ClientboundPacket1_21_9, ServerboundPacket1_21_9, ServerboundPacket1_21_9> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21.11", "1.21.9", Protocol1_21_9To1_21_11.class);
   private static final int END_FOG_COLOR = 10518688;
   private static final int OVERWORLD_FOG_COLOR = -4138753;
   private final EntityPacketRewriter1_21_11 entityRewriter = new EntityPacketRewriter1_21_11(this);
   private final BlockItemPacketRewriter1_21_11 itemRewriter = new BlockItemPacketRewriter1_21_11(this);
   private final ParticleRewriter<ClientboundPacket1_21_11> particleRewriter = new ParticleRewriter(this);
   private final NBTComponentRewriter<ClientboundPacket1_21_11> translatableRewriter = new ComponentRewriter1_21_11(this);
   private final TagRewriter<ClientboundPacket1_21_11> tagRewriter = new TagRewriter(this);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_11> recipeRewriter = new RecipeDisplayRewriter1_21_5(this);
   private final BlockRewriter<ClientboundPacket1_21_11> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_21_5::new);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this) {
      protected void updateType(CompoundTag tag, String key, FullMappings mappings) {
         super.updateType(tag, key, mappings);
         if (key.equals("sound") && tag.get(key) instanceof ListTag<?> listTag) {
            Tag first;
            if (listTag.isEmpty()) {
               first = new StringTag(mappings.mappedIdentifier(0));
            } else {
               first = listTag.get(0);
            }

            tag.put(key, first);
         }
      }
   };

   public Protocol1_21_11To1_21_9() {
      super(ClientboundPacket1_21_11.class, ClientboundPacket1_21_9.class, ServerboundPacket1_21_9.class, ServerboundPacket1_21_9.class);
   }

   private void moveAttribute(
      CompoundTag baseTag, @Nullable CompoundTag attributes, String key, String mappedKey, Function<Tag, Tag> tagMapper, @Nullable Tag defaultTag
   ) {
      Tag attributeTag;
      if (attributes != null && (attributeTag = TagUtil.getNamespacedTag(attributes, key)) != null) {
         baseTag.put(mappedKey, tagMapper.apply(attributeTag));
      } else if (defaultTag != null) {
         baseTag.put(mappedKey, defaultTag);
      }
   }

   private int floatsToARGB(float r, float g, float b) {
      return 0xFF000000 | (int)(r * 255.0F) << 16 | (int)(g * 255.0F) << 8 | (int)(b * 255.0F);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.registryDataRewriter.removeEnchantmentEffects(new String[]{"apply_exhaustion", "apply_impulse"});
      this.registryDataRewriter
         .addHandler(
            "dimension_type",
            (key, tag) -> {
               if (Key.equals(key, "the_nether")) {
                  tag.putString("effects", "minecraft:the_nether");
                  tag.putBoolean("natural", false);
               } else if (Key.equals(key, "the_end")) {
                  tag.putString("effects", "minecraft:the_end");
                  tag.putBoolean("natural", false);
               } else {
                  tag.putString("effects", "minecraft:overworld");
                  tag.putBoolean("natural", true);
               }

               ByteTag trueTag = new ByteTag((byte)1);
               ByteTag falseTag = new ByteTag((byte)0);
               CompoundTag attributes = tag.getCompoundTag("attributes");
               this.moveAttribute(tag, attributes, "visual/cloud_height", "cloud_height", Function.identity(), null);
               this.moveAttribute(tag, attributes, "gameplay/can_start_raid", "has_raids", Function.identity(), trueTag);
               this.moveAttribute(
                  tag,
                  attributes,
                  "gameplay/piglins_zombify",
                  "piglin_safe",
                  attributeTag -> ((NumberTag)attributeTag).asBoolean() ? ByteTag.ZERO : trueTag,
                  ByteTag.ZERO
               );
               this.moveAttribute(tag, attributes, "gameplay/respawn_anchor_works", "respawn_anchor_works", Function.identity(), falseTag);
               this.moveAttribute(tag, attributes, "gameplay/bed_rule", "bed_works", attributeTag -> {
                  CompoundTag bedRule = (CompoundTag)attributeTag;
                  return !bedRule.getBoolean("can_sleep") && !bedRule.getBoolean("can_set_spawn") ? ByteTag.ZERO : trueTag;
               }, trueTag);
               this.moveAttribute(tag, attributes, "gameplay/fast_lava", "ultrawarm", Function.identity(), ByteTag.ZERO);
            }
         );
      this.registryDataRewriter.addHandler("worldgen/biome", (key, tag) -> {
         CompoundTag effects = tag.getCompoundTag("effects");
         this.moveAttribute(effects, effects, "water_color", "water_color", this::mapColor, new IntTag(4159204));
         this.moveAttribute(effects, effects, "foliage_color", "foliage_color", this::mapColor, null);
         this.moveAttribute(effects, effects, "dry_foliage_color", "dry_foliage_color", this::mapColor, null);
         this.moveAttribute(effects, effects, "grass_color", "grass_color", this::mapColor, null);
         CompoundTag attributes = (CompoundTag)tag.removeUnchecked("attributes");
         this.moveAttribute(effects, attributes, "visual/sky_color", "sky_color", this::mapColor, new IntTag(0));
         this.moveAttribute(effects, attributes, "visual/water_fog_color", "water_fog_color", this::mapColor, new IntTag(-16448205));
         this.moveAttribute(effects, attributes, "visual/fog_color", "fog_color", this::mapColor, new IntTag(Key.equals(key, "the_end") ? 10518688 : -4138753));
         if (attributes != null) {
            CompoundTag backgroundMusic = TagUtil.getNamespacedCompoundTag(attributes, "audio/background_music");
            if (backgroundMusic != null) {
               CompoundTag def = backgroundMusic.getCompoundTag("default");
               if (def != null) {
                  CompoundTag data = new CompoundTag();
                  Tag maxDelay = def.get("max_delay");
                  Tag minDelay = def.get("min_delay");
                  Tag sound = def.get("sound");
                  if (maxDelay != null) {
                     data.put("max_delay", maxDelay);
                  }

                  if (minDelay != null) {
                     data.put("min_delay", minDelay);
                  }

                  if (sound != null) {
                     data.put("sound", sound);
                  }

                  if (!data.contains("replace_current_music")) {
                     data.putBoolean("replace_current_music", false);
                  }

                  CompoundTag entry = new CompoundTag();
                  entry.put("data", data);
                  entry.putInt("weight", 1);
                  ListTag<CompoundTag> musicList = new ListTag(CompoundTag.class);
                  musicList.add(entry);
                  effects.put("music", musicList);
               }
            }

            CompoundTag ambientSounds = TagUtil.getNamespacedCompoundTag(attributes, "audio/ambient_sounds");
            if (ambientSounds != null) {
               Tag loop = ambientSounds.get("loop");
               if (loop != null) {
                  effects.put("ambient_sound", loop);
               }

               Tag mood = ambientSounds.get("mood");
               if (mood != null) {
                  effects.put("mood_sound", mood);
               }

               Tag additions = ambientSounds.get("additions");
               if (additions != null) {
                  effects.put("additions_sound", additions);
               }
            }

            ListTag<CompoundTag> ambientParticles = TagUtil.getNamespacedCompoundTagList(attributes, "visual/ambient_particles");
            if (ambientParticles != null && !ambientParticles.isEmpty()) {
               CompoundTag first = (CompoundTag)ambientParticles.get(0);
               Tag probability = first.get("probability");
               CompoundTag particle = first.getCompoundTag("particle");
               CompoundTag options = new CompoundTag();
               options.put("probability", probability);
               options.put("options", particle);
               effects.put("particle", options);
            }
         }
      });
      this.registryDataRewriter.addHandler("enchantment", (key, tag) -> {
         CompoundTag effects = tag.getCompoundTag("effects");
         if (effects != null) {
            TagUtil.removeNamespaced(effects, "post_piercing_attack");
         }
      });
      this.registryDataRewriter.remove("zombie_nautilus_variant");
      this.registryDataRewriter.remove("timeline");
      this.tagRewriter.removeTags("timeline");
   }

   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_11.PLAYER));
      connection.storables(this).setItemHasher(new ItemHasherBase(this, connection));
   }

   public ProtocolStorables1_21_11 createStorables() {
      return new ProtocolStorables1_21_11();
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_11 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_11 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21_11> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket1_21_11> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_11> getParticleRewriter() {
      return this.particleRewriter;
   }

   public NBTComponentRewriter<ClientboundPacket1_21_11> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_11> getTagRewriter() {
      return this.tagRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V1_21_11;
   }

   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V1_21_9;
   }

   protected PacketTypesProvider<ClientboundPacket1_21_11, ClientboundPacket1_21_9, ServerboundPacket1_21_9, ServerboundPacket1_21_9> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(
            this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21_11.class, ClientboundConfigurationPackets1_21_9.class}
         ),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21_9.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class}
         )
      );
   }

   private Tag mapColor(Tag attributeTag) {
      if (attributeTag instanceof ListTag<?> listTag) {
         NumberTag r = (NumberTag)listTag.get(0);
         NumberTag g = (NumberTag)listTag.get(1);
         NumberTag b = (NumberTag)listTag.get(2);
         return new IntTag(this.floatsToARGB(r.asFloat(), g.asFloat(), b.asFloat()));
      } else {
         return (Tag)(attributeTag instanceof StringTag stringTag ? new IntTag(Integer.parseInt(stringTag.getValue().substring(1), 16)) : attributeTag);
      }
   }
}
