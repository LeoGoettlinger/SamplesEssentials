package com.viaversion.viabackwards.protocol.v26_1to1_21_11;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.NBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v26_1to1_21_11.rewriter.BlockItemPacketRewriter26_1;
import com.viaversion.viabackwards.protocol.v26_1to1_21_11.rewriter.ComponentRewriter26_1;
import com.viaversion.viabackwards.protocol.v26_1to1_21_11.rewriter.EntityPacketRewriter26_1;
import com.viaversion.viabackwards.protocol.v26_1to1_21_11.storage.DayTimeStorage;
import com.viaversion.viabackwards.protocol.v26_1to1_21_11.storage.ProtocolStorables26_1;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType26_1;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.api.type.types.version.Types26_1;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundConfigurationPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.ProtocolUtil;
import com.viaversion.viaversion.util.TagUtil;

public final class Protocol26_1To1_21_11
   extends BackwardsProtocol<ClientboundPacket26_1, ClientboundPacket1_21_11, ServerboundPacket26_1, ServerboundPacket1_21_9> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("26.1", "1.21.11", Protocol1_21_11To26_1.class);
   private final EntityPacketRewriter26_1 entityRewriter = new EntityPacketRewriter26_1(this);
   private final BlockItemPacketRewriter26_1 itemRewriter = new BlockItemPacketRewriter26_1(this);
   private final ParticleRewriter<ClientboundPacket26_1> particleRewriter = new ParticleRewriter(this);
   private final NBTComponentRewriter<ClientboundPacket26_1> translatableRewriter = new ComponentRewriter26_1(this);
   private final TagRewriter<ClientboundPacket26_1> tagRewriter = new TagRewriter(this);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this);
   private final BlockRewriter<ClientboundPacket26_1> blockRewriter = new BlockRewriter1_21_5(this, ChunkType26_1::new, ChunkType1_21_5::new);
   private final RecipeDisplayRewriter<ClientboundPacket26_1> recipeRewriter = new RecipeDisplayRewriter1_21_5(this);

   public Protocol26_1To1_21_11() {
      super(ClientboundPacket26_1.class, ClientboundPacket1_21_11.class, ServerboundPacket26_1.class, ServerboundPacket1_21_9.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.registryDataRewriter.addHandler("dimension_type", (key, tag) -> this.removeEnvironmentAttributes(tag.getCompoundTag("attributes")));
      this.registryDataRewriter.addHandler("timeline", (key, tag) -> this.removeEnvironmentAttributes(tag.getCompoundTag("tracks")));
      this.registryDataRewriter.addHandler("wolf_sound_variant", (key, tag) -> {
         CompoundTag sounds = tag.getCompoundTag("adult_sounds");
         tag.remove("baby_sounds");
         tag.putAll(sounds);
      });
      this.registryDataRewriter.addHandler("frog_variant", (key, tag) -> this.swapEntityNameAffix("frog", tag));
      this.registryDataRewriter.addHandler("chicken_variant", (key, tag) -> this.swapEntityNameAffix("chicken", tag));
      this.registryDataRewriter.addHandler("cow_variant", (key, tag) -> this.swapEntityNameAffix("cow", tag));
      this.registryDataRewriter.addHandler("pig_variant", (key, tag) -> this.swapEntityNameAffix("pig", tag));
      this.registryDataRewriter.addHandler("cat_variant", (key, tag) -> this.removeEntityNamePrefix("cat", tag));
      this.registryDataRewriter.remove("world_clock");
      this.registryDataRewriter.remove("cat_sound_variant");
      this.registryDataRewriter.remove("cow_sound_variant");
      this.registryDataRewriter.remove("pig_sound_variant");
      this.registryDataRewriter.remove("chicken_sound_variant");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "supports_dry_vegetation", "dry_vegetation_may_place_on");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "supports_bamboo", "bamboo_plantable_on");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "supports_small_dripleaf", "small_dripleaf_placeable");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "supports_big_dripleaf", "big_dripleaf_placeable");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "overrides_mushroom_light_requirement", "mushroom_grow_block");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "support_override_snow_layer", "snow_layer_can_survive_on");
      this.tagRewriter.renameTag(RegistryType.BLOCK, "cannot_support_snow_layer", "snow_layer_cannot_survive_on");
      this.cancelClientbound(ClientboundPackets26_1.LOW_DISK_SPACE_WARNING);
      this.cancelClientbound(ClientboundPackets26_1.GAME_RULE_VALUES);
      this.registerClientbound(ClientboundPackets26_1.SET_TIME, wrapper -> {
         long gameTime = (Long)wrapper.passthrough(Types.LONG);
         Long dayTime = null;
         boolean advanceTime = true;
         int count = (Integer)wrapper.read(Types.VAR_INT);

         for (int i = 0; i < count; i++) {
            int clockType = (Integer)wrapper.read(Types.VAR_INT);
            long totalTicks = (Long)wrapper.read(Types.VAR_LONG);
            wrapper.read(Types.FLOAT);
            float tickRate = (Float)wrapper.read(Types.FLOAT);
            if (Key.equals(this.entityRewriter.tracker(wrapper.user()).registryKeys("world_clock").idToKey(clockType), "overworld")) {
               dayTime = totalTicks;
               advanceTime = tickRate != 0.0F;
            }
         }

         ProtocolStorables26_1 storables = (ProtocolStorables26_1)wrapper.user().storables(this);
         DayTimeStorage dayTimeStorage = storables.dayTimeStorage();
         if (dayTime == null) {
            dayTime = dayTimeStorage.setGameTimeAndUpdateDayTime(gameTime);
            advanceTime = dayTimeStorage.advanceTime();
         } else {
            dayTimeStorage.setGameTime(gameTime);
            dayTimeStorage.setDayTime(dayTime);
            dayTimeStorage.setAdvanceTime(advanceTime);
         }

         wrapper.write(Types.LONG, dayTime);
         wrapper.write(Types.BOOLEAN, advanceTime);
      });
   }

   private void removeEnvironmentAttributes(CompoundTag tag) {
      if (tag != null) {
         TagUtil.removeNamespaced(tag, "visual/block_light_tint");
         TagUtil.removeNamespaced(tag, "visual/night_vision_color");
         TagUtil.removeNamespaced(tag, "visual/ambient_light_color");
      }
   }

   private void removeEntityNamePrefix(String key, CompoundTag tag) {
      StringTag assetIdTag = tag.getStringTag("asset_id");
      String assetId = assetIdTag.getValue();
      assetIdTag.setValue(assetId.replace(key + "_", ""));
   }

   private void swapEntityNameAffix(String key, CompoundTag tag) {
      StringTag assetIdTag = tag.getStringTag("asset_id");
      String assetId = assetIdTag.getValue();
      if (assetId.contains(key + "_")) {
         assetIdTag.setValue(assetId.replace(key + "_", "") + "_" + key);
      }
   }

   public void init(UserConnection connection) {
      this.addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_21_11.PLAYER));
      connection.storables(this).setItemHasher(new ItemHasherBase(this, connection));
   }

   public ProtocolStorables26_1 createStorables() {
      return new ProtocolStorables26_1();
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter26_1 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter26_1 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket26_1> getBlockRewriter() {
      return this.blockRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket26_1> getRecipeRewriter() {
      return this.recipeRewriter;
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

   public Types26_1<StructuredDataKeys1_21_11, EntityDataTypes26_1> types() {
      return VersionedTypes.V26_1;
   }

   public Types1_20_5<StructuredDataKeys1_21_11, EntityDataTypes1_21_11> mappedTypes() {
      return VersionedTypes.V1_21_11;
   }

   protected PacketTypesProvider<ClientboundPacket26_1, ClientboundPacket1_21_11, ServerboundPacket26_1, ServerboundPacket1_21_9> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets26_1.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21_11.class, ClientboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets26_1.class, ServerboundConfigurationPackets1_21_9.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_6.class, ServerboundConfigurationPackets1_21_9.class}
         )
      );
   }
}
