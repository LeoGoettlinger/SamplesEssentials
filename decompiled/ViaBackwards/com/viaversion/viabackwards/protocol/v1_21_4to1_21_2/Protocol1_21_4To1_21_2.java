package com.viaversion.viabackwards.protocol.v1_21_4to1_21_2;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.rewriters.BackwardsRegistryRewriter;
import com.viaversion.viabackwards.api.rewriters.text.JsonNBTComponentRewriter;
import com.viaversion.viabackwards.protocol.v1_21_4to1_21_2.rewriter.BlockItemPacketRewriter1_21_4;
import com.viaversion.viabackwards.protocol.v1_21_4to1_21_2.rewriter.ComponentRewriter1_21_4;
import com.viaversion.viabackwards.protocol.v1_21_4to1_21_2.rewriter.EntityPacketRewriter1_21_4;
import com.viaversion.viabackwards.protocol.v1_21_4to1_21_2.rewriter.ParticleRewriter1_21_4;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_4;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPacket1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPacket1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ServerboundPackets1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.util.ArrayUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.ProtocolUtil;
import java.util.BitSet;

public final class Protocol1_21_4To1_21_2
   extends BackwardsProtocol<ClientboundPacket1_21_2, ClientboundPacket1_21_2, ServerboundPacket1_21_4, ServerboundPacket1_21_2> {
   public static final BackwardsMappingData MAPPINGS = new BackwardsMappingData("1.21.4", "1.21.2", Protocol1_21_2To1_21_4.class);
   private final EntityPacketRewriter1_21_4 entityRewriter = new EntityPacketRewriter1_21_4(this);
   private final BlockItemPacketRewriter1_21_4 itemRewriter = new BlockItemPacketRewriter1_21_4(this);
   private final ParticleRewriter<ClientboundPacket1_21_2> particleRewriter = new ParticleRewriter1_21_4(this);
   private final JsonNBTComponentRewriter<ClientboundPacket1_21_2> translatableRewriter = new ComponentRewriter1_21_4(this);
   private final TagRewriter<ClientboundPacket1_21_2> tagRewriter = new TagRewriter(this);
   private final RecipeDisplayRewriter<ClientboundPacket1_21_2> recipeRewriter = new RecipeDisplayRewriter(this);
   private final BlockRewriter<ClientboundPacket1_21_2> blockRewriter = BlockRewriter.for1_20_2(this, ChunkType1_20_2::new);
   private final BackwardsRegistryRewriter registryDataRewriter = new BackwardsRegistryRewriter(this) {
      @Override
      public RegistryEntry[] handle(UserConnection connection, String key, RegistryEntry[] entries) {
         String strippedKey = Key.stripMinecraftNamespace(key);
         if (strippedKey.equals("worldgen/biome")) {
            for (RegistryEntry entry : entries) {
               if (entry.tag() != null) {
                  CompoundTag effectsTag = ((CompoundTag)entry.tag()).getCompoundTag("effects");
                  ListTag<CompoundTag> weightedMusicTags = effectsTag.getListTag("music", CompoundTag.class);
                  if (weightedMusicTags != null) {
                     if (weightedMusicTags.isEmpty()) {
                        effectsTag.remove("music");
                     } else {
                        CompoundTag musicTag = (CompoundTag)weightedMusicTags.get(0);
                        effectsTag.put("music", musicTag.get("data"));
                     }
                  }
               }
            }
         } else if (strippedKey.equals("trim_material")) {
            for (RegistryEntry entryx : entries) {
               if (entryx.tag() != null) {
                  CompoundTag compoundTag = (CompoundTag)entryx.tag();
                  compoundTag.putFloat("item_model_index", Protocol1_21_4To1_21_2.this.itemModelIndex(entryx.key()));
                  if ("resin".equals(compoundTag.getString("asset_name"))) {
                     compoundTag.putString("asset_name", "gold");
                  }
               }
            }
         }

         return super.handle(connection, key, entries);
      }
   };

   public Protocol1_21_4To1_21_2() {
      super(ClientboundPacket1_21_2.class, ClientboundPacket1_21_2.class, ServerboundPacket1_21_4.class, ServerboundPacket1_21_2.class);
   }

   protected void registerPackets() {
      super.registerPackets();
      this.replaceClientbound(ClientboundPackets1_21_2.LEVEL_PARTICLES, wrapper -> {
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.read(Types.BOOLEAN);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.INT);
         Particle particle = (Particle)wrapper.passthroughAndMap(VersionedTypes.V1_21_4.particle(), VersionedTypes.V1_21_2.particle());
         this.particleRewriter.rewriteParticle(wrapper.user(), particle);
      });
      this.registerClientbound(ClientboundConfigurationPackets1_21.UPDATE_ENABLED_FEATURES, wrapper -> {
         String[] enabledFeatures = (String[])wrapper.read(Types.STRING_ARRAY);
         wrapper.write(Types.STRING_ARRAY, (String[])ArrayUtil.add(enabledFeatures, "winter_drop"));
      });
      this.replaceClientbound(ClientboundPackets1_21_2.PLAYER_INFO_UPDATE, wrapper -> {
         BitSet actions = (BitSet)wrapper.read(Types.PROFILE_ACTIONS_ENUM1_21_4);
         BitSet updatedActions = new BitSet(7);

         for (int i = 0; i < 7; i++) {
            if (actions.get(i)) {
               updatedActions.set(i);
            }
         }

         wrapper.write(Types.PROFILE_ACTIONS_ENUM1_21_2, updatedActions);
         int entries = (Integer)wrapper.passthrough(Types.VAR_INT);

         for (int ix = 0; ix < entries; ix++) {
            wrapper.passthrough(Types.UUID);
            if (actions.get(0)) {
               wrapper.passthrough(Types.STRING);
               wrapper.passthrough(Types.PROFILE_PROPERTY_ARRAY);
            }

            if (actions.get(1) && (Boolean)wrapper.passthrough(Types.BOOLEAN)) {
               wrapper.passthrough(Types.UUID);
               wrapper.passthrough(Types.PROFILE_KEY);
            }

            if (actions.get(2)) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if (actions.get(3)) {
               wrapper.passthrough(Types.BOOLEAN);
            }

            if (actions.get(4)) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if (actions.get(5)) {
               this.translatableRewriter.processTag(wrapper.user(), (Tag)wrapper.passthrough(Types.TRUSTED_OPTIONAL_TAG));
            }

            if (actions.get(6)) {
               wrapper.passthrough(Types.VAR_INT);
            }

            if (actions.get(7)) {
               wrapper.read(Types.BOOLEAN);
            }
         }
      });
   }

   protected void onMappingDataLoaded() {
      super.onMappingDataLoaded();
      this.tagRewriter.addEmptyTags(RegistryType.ITEM, new String[]{"minecraft:tall_flowers", "minecraft:flowers"});
      this.tagRewriter.addEmptyTag(RegistryType.BLOCK, "minecraft:tall_flowers");
   }

   public void init(UserConnection user) {
      this.addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_21_4.PLAYER));
   }

   @Override
   public BackwardsMappingData getMappingData() {
      return MAPPINGS;
   }

   public EntityPacketRewriter1_21_4 getEntityRewriter() {
      return this.entityRewriter;
   }

   public BlockItemPacketRewriter1_21_4 getItemRewriter() {
      return this.itemRewriter;
   }

   public BlockRewriter<ClientboundPacket1_21_2> getBlockRewriter() {
      return this.blockRewriter;
   }

   public ParticleRewriter<ClientboundPacket1_21_2> getParticleRewriter() {
      return this.particleRewriter;
   }

   public JsonNBTComponentRewriter<ClientboundPacket1_21_2> getComponentRewriter() {
      return this.translatableRewriter;
   }

   public TagRewriter<ClientboundPacket1_21_2> getTagRewriter() {
      return this.tagRewriter;
   }

   public RecipeDisplayRewriter<ClientboundPacket1_21_2> getRecipeRewriter() {
      return this.recipeRewriter;
   }

   @Override
   public BackwardsRegistryRewriter getRegistryDataRewriter() {
      return this.registryDataRewriter;
   }

   public VersionedTypesHolder types() {
      return VersionedTypes.V1_21_4;
   }

   public VersionedTypesHolder mappedTypes() {
      return VersionedTypes.V1_21_2;
   }

   protected PacketTypesProvider<ClientboundPacket1_21_2, ClientboundPacket1_21_2, ServerboundPacket1_21_4, ServerboundPacket1_21_2> createPacketTypesProvider() {
      return new SimplePacketTypesProvider(
         ProtocolUtil.packetTypeMap(this.unmappedClientboundPacketType, new Class[]{ClientboundPackets1_21_2.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedClientboundPacketType, new Class[]{ClientboundPackets1_21_2.class, ClientboundConfigurationPackets1_21.class}),
         ProtocolUtil.packetTypeMap(this.mappedServerboundPacketType, new Class[]{ServerboundPackets1_21_4.class, ServerboundConfigurationPackets1_20_5.class}),
         ProtocolUtil.packetTypeMap(
            this.unmappedServerboundPacketType, new Class[]{ServerboundPackets1_21_2.class, ServerboundConfigurationPackets1_20_5.class}
         )
      );
   }

   private float itemModelIndex(String trim) {
      String var2 = Key.stripNamespace(trim);

      return switch (var2) {
         case "amethyst" -> 1.0F;
         case "copper" -> 0.5F;
         case "diamond" -> 0.8F;
         case "emerald" -> 0.7F;
         case "gold", "resin" -> 0.6F;
         case "iron" -> 0.2F;
         case "lapis" -> 0.9F;
         case "netherite" -> 0.3F;
         case "quartz" -> 0.1F;
         case "redstone" -> 0.4F;
         default -> 1.0F;
      };
   }
}
