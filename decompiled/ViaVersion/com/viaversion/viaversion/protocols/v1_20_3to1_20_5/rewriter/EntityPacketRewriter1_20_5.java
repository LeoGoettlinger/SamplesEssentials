package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.libs.fastutil.ints.IntArraySet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundConfigurationPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.AcknowledgedMessagesStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ProtocolStorables1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ScoreboardTeamStorage;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import com.viaversion.viaversion.util.TagUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class EntityPacketRewriter1_20_5 extends EntityRewriter<ClientboundPacket1_20_3, Protocol1_20_3To1_20_5> {
   private static final UUID CREATIVE_BLOCK_INTERACTION_RANGE = UUID.fromString("736565d2-e1a7-403d-a3f8-1aeb3e302542");
   private static final UUID CREATIVE_ENTITY_INTERACTION_RANGE = UUID.fromString("98491ef6-97b1-4584-ae82-71a8cc85cf73");

   public EntityPacketRewriter1_20_5(Protocol1_20_3To1_20_5 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.registerSetEntityData(ClientboundPackets1_20_3.SET_ENTITY_DATA, Types1_20_3.ENTITY_DATA_LIST, VersionedTypes.V1_20_5.entityDataList);
      this.protocol.replaceClientbound(ClientboundPackets1_20_3.SET_EQUIPMENT, wrapper -> {
         int entityId = wrapper.passthrough(Types.VAR_INT);
         EntityType type = this.<EntityTracker>tracker(wrapper.user()).entityType(entityId);

         byte slot;
         do {
            slot = wrapper.read(Types.BYTE);
            int rawSlot = slot & 127;
            if (type != null && type.isOrHasParent(EntityTypes1_20_5.ABSTRACT_HORSE) && rawSlot == 4) {
               boolean lastSlot = (slot & -128) == 0;
               slot = (byte)(lastSlot ? 6 : -122);
            }

            wrapper.write(Types.BYTE, slot);
            Item item = this.protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_20_2));
            wrapper.write(VersionedTypes.V1_20_5.item, item);
         } while ((slot & -128) != 0);
      });
      this.protocol.registerClientbound(ClientboundConfigurationPackets1_20_3.REGISTRY_DATA, wrapper -> {
         PacketWrapper knownPacksPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.SELECT_KNOWN_PACKS);
         knownPacksPacket.write(Types.VAR_INT, 0);
         knownPacksPacket.send(Protocol1_20_3To1_20_5.class);
         CompoundTag registryData = wrapper.read(Types.COMPOUND_TAG);
         this.cacheDimensionData(wrapper.user(), registryData);
         this.trackBiomeSize(wrapper.user(), registryData);

         for (CompoundTag dimensionType : TagUtil.getRegistryEntries(registryData, "dimension_type")) {
            CompoundTag elementTag = dimensionType.getCompoundTag("element");
            CompoundTag monsterSpawnLightLevel = elementTag.getCompoundTag("monster_spawn_light_level");
            if (monsterSpawnLightLevel != null) {
               CompoundTag value = monsterSpawnLightLevel.removeUnchecked("value");
               monsterSpawnLightLevel.putInt("min_inclusive", value.getInt("min_inclusive"));
               monsterSpawnLightLevel.putInt("max_inclusive", value.getInt("max_inclusive"));
            }
         }

         for (CompoundTag biome : TagUtil.getRegistryEntries(registryData, "worldgen/biome")) {
            CompoundTag effects = biome.getCompoundTag("element").getCompoundTag("effects");
            this.checkSoundTag(effects.getCompoundTag("mood_sound"), "sound");
            this.checkSoundTag(effects.getCompoundTag("additions_sound"), "sound");
            this.checkSoundTag(effects.getCompoundTag("music"), "sound");
            this.checkSoundTag(effects, "ambient_sound");
            CompoundTag particle = effects.getCompoundTag("particle");
            if (particle != null) {
               CompoundTag particleOptions = particle.getCompoundTag("options");
               String particleType = particleOptions.getString("type");
               this.updateParticleFormat(particleOptions, Key.stripMinecraftNamespace(particleType));
            }
         }

         for (Entry<String, Tag> entry : registryData.entrySet()) {
            CompoundTag entryTag = (CompoundTag)entry.getValue();
            String type = entryTag.getString("type");
            ListTag<CompoundTag> valueTag = entryTag.getListTag("value", CompoundTag.class);
            RegistryEntry[] registryEntries = new RegistryEntry[valueTag.stream().map(e -> e.getInt("id")).distinct().toArray().length];
            boolean requiresDummyValues = false;
            int highestId = -1;
            IntSet ids = new IntArraySet();

            for (CompoundTag tag : valueTag) {
               String name = tag.getString("name");
               int id = tag.getInt("id");
               if (ids.add(id)) {
                  highestId = Math.max(highestId, id);
                  if (id >= registryEntries.length) {
                     registryEntries = Arrays.copyOf(registryEntries, Math.max(registryEntries.length * 2, id + 1));
                     requiresDummyValues = true;
                  }
               }

               registryEntries[id] = new RegistryEntry(name, tag.get("element"));
            }

            String strippedKey = Key.stripMinecraftNamespace(type);
            if (strippedKey.equals("damage_type")) {
               if (Arrays.stream(registryEntries).noneMatch(e -> Key.namespaced(e.key()).equals("minecraft:spit"))) {
                  registryEntries = Arrays.copyOf(registryEntries, ++highestId + 1);
                  CompoundTag spitData = new CompoundTag();
                  spitData.putString("scaling", "when_caused_by_living_non_player");
                  spitData.putString("message_id", "mob");
                  spitData.putFloat("exhaustion", 0.1F);
                  registryEntries[highestId] = new RegistryEntry("minecraft:spit", spitData);
               }

               Set<String> registryEntryKeys = Arrays.stream(registryEntries).map(e -> Key.stripMinecraftNamespace(e.key())).collect(Collectors.toSet());

               for (String key : this.protocol.getMappingData().damageKeys()) {
                  if (!registryEntryKeys.contains(key)) {
                     registryEntries = Arrays.copyOf(registryEntries, ++highestId + 1);
                     registryEntries[highestId] = new RegistryEntry(Key.namespaced(key), this.protocol.getMappingData().damageType(key));
                  }
               }
            }

            if (requiresDummyValues) {
               int finalLength = highestId + 1;
               if (registryEntries.length != finalLength) {
                  registryEntries = Arrays.copyOf(registryEntries, finalLength);
               }

               this.replaceNullValues(registryEntries);
            }

            ProtocolStorables1_20_5 storables = wrapper.user().storables(this.protocol);
            if (strippedKey.equals("trim_pattern")) {
               storables.armorTrims().setTrimPatterns(this.toMappings(registryEntries));
            } else if (strippedKey.equals("trim_material")) {
               storables.armorTrims().setTrimMaterials(this.toMappings(registryEntries));
            }

            PacketWrapper registryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
            registryPacket.write(Types.STRING, type);
            registryPacket.write(Types.REGISTRY_ENTRY_ARRAY, registryEntries);
            registryPacket.send(Protocol1_20_3To1_20_5.class);
         }

         wrapper.cancel();
         PacketWrapper wolfVariantsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
         wolfVariantsPacket.write(Types.STRING, "minecraft:wolf_variant");
         CompoundTag paleWolf = new CompoundTag();
         paleWolf.putString("wild_texture", "entity/wolf/wolf");
         paleWolf.putString("tame_texture", "entity/wolf/wolf_tame");
         paleWolf.putString("angry_texture", "entity/wolf/wolf_angry");
         paleWolf.put("biomes", new ListTag<>(StringTag.class));
         wolfVariantsPacket.write(Types.REGISTRY_ENTRY_ARRAY, new RegistryEntry[]{new RegistryEntry("minecraft:pale", paleWolf)});
         wolfVariantsPacket.send(Protocol1_20_3To1_20_5.class);
         PacketWrapper bannerPatternsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
         bannerPatternsPacket.write(Types.STRING, "minecraft:banner_pattern");
         Key[] keys = BannerPatterns1_20_5.keys();
         RegistryEntry[] patternEntries = new RegistryEntry[keys.length];

         for (int i = 0; i < keys.length; i++) {
            CompoundTag pattern = new CompoundTag();
            Key keyx = keys[i];
            pattern.putString("asset_id", keyx.path());
            pattern.putString("translation_key", "block.minecraft.banner." + keyx.path());
            patternEntries[i] = new RegistryEntry(keyx.toString(), pattern);
         }

         bannerPatternsPacket.write(Types.REGISTRY_ENTRY_ARRAY, patternEntries);
         bannerPatternsPacket.send(Protocol1_20_3To1_20_5.class);
      });
      this.protocol.registerClientbound(ClientboundPackets1_20_3.LOGIN, new PacketHandlers() {
         @Override
         public void register() {
            this.map(Types.INT);
            this.map(Types.BOOLEAN);
            this.map(Types.STRING_ARRAY);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.VAR_INT);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.handler(wrapper -> {
               String dimensionKey = wrapper.read(Types.STRING);
               DimensionData data = EntityPacketRewriter1_20_5.this.<EntityTracker>tracker(wrapper.user()).dimensionData(dimensionKey);
               wrapper.write(Types.VAR_INT, data.id());
            });
            this.map(Types.STRING);
            this.map(Types.LONG);
            this.map(Types.BYTE);
            this.map(Types.BYTE);
            this.map(Types.BOOLEAN);
            this.map(Types.BOOLEAN);
            this.map(Types.OPTIONAL_GLOBAL_POSITION);
            this.map(Types.VAR_INT);
            this.handler(EntityPacketRewriter1_20_5.this.worldDataTrackerHandlerByKey1_20_5(3));
            this.handler(EntityPacketRewriter1_20_5.this.playerTrackerHandler());
            this.handler(wrapper -> {
               ProtocolStorables1_20_5 storables = wrapper.user().storables(EntityPacketRewriter1_20_5.this.protocol);
               AcknowledgedMessagesStorage storage = storables.acknowledgedMessages();
               if (storage.secureChatEnforced() != null) {
                  wrapper.write(Types.BOOLEAN, storage.isSecureChatEnforced());
               } else {
                  wrapper.write(Types.BOOLEAN, Via.getConfig().enforceSecureChat());
               }

               storage.clear();
               byte gamemode = wrapper.get(Types.BYTE, 0);
               boolean creativeMode = gamemode == GameMode.CREATIVE.id();
               EntityPacketRewriter1_20_5.this.sendRangeAttributes(wrapper.user(), creativeMode);
               EntityPacketRewriter1_20_5.this.<EntityTracker>tracker(wrapper.user()).setInstaBuild(creativeMode);
            });
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_20_3.RESPAWN, wrapper -> {
         String dimensionKey = wrapper.read(Types.STRING);
         DimensionData data = this.<EntityTracker>tracker(wrapper.user()).dimensionData(dimensionKey);
         wrapper.write(Types.VAR_INT, data.id());
         wrapper.passthrough(Types.STRING);
         this.worldDataTrackerHandlerByKey1_20_5(0).handle(wrapper);
         wrapper.passthrough(Types.LONG);
         byte gamemode = wrapper.passthrough(Types.BYTE);
         boolean creativeMode = gamemode == GameMode.CREATIVE.id();
         this.sendRangeAttributes(wrapper.user(), creativeMode);
         this.<EntityTracker>tracker(wrapper.user()).setInstaBuild(creativeMode);
         ProtocolStorables1_20_5 storables = wrapper.user().storables(this.protocol);
         storables.setScoreboardTeams(new ScoreboardTeamStorage());
      });
      this.protocol.registerClientbound(ClientboundPackets1_20_3.UPDATE_MOB_EFFECT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         byte amplifier = wrapper.read(Types.BYTE);
         wrapper.write(Types.VAR_INT, Integer.valueOf(amplifier));
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.BYTE);
         wrapper.read(Types.OPTIONAL_COMPOUND_TAG);
      });
      this.protocol.registerClientbound(ClientboundPackets1_20_3.UPDATE_ATTRIBUTES, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            String attributeIdentifier = wrapper.read(Types.STRING);
            int mappedId = Attributes1_20_5.keyToId(attributeIdentifier);
            wrapper.write(Types.VAR_INT, mappedId != -1 ? mappedId : 0);
            wrapper.passthrough(Types.DOUBLE);
            int modifierSize = wrapper.passthrough(Types.VAR_INT);

            for (int j = 0; j < modifierSize; j++) {
               wrapper.passthrough(Types.UUID);
               wrapper.passthrough(Types.DOUBLE);
               wrapper.passthrough(Types.BYTE);
            }
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_20_3.GAME_EVENT, wrapper -> {
         short event = wrapper.passthrough(Types.UNSIGNED_BYTE);
         if (event == 3) {
            int value = (int)Math.floor(wrapper.passthrough(Types.FLOAT) + 0.5F);
            boolean creativeMode = value == GameMode.CREATIVE.id();
            this.sendRangeAttributes(wrapper.user(), creativeMode);
            this.<EntityTracker>tracker(wrapper.user()).setInstaBuild(creativeMode);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets1_20_3.PLAYER_ABILITIES, wrapper -> {
         byte flags = wrapper.passthrough(Types.BYTE);
         this.<EntityTracker>tracker(wrapper.user()).setInstaBuild((flags & 8) != 0);
      });
   }

   private KeyMappings toMappings(RegistryEntry[] entries) {
      String[] keys = new String[entries.length];

      for (int i = 0; i < entries.length; i++) {
         keys[i] = Key.stripMinecraftNamespace(entries[i].key());
      }

      return new KeyMappings(keys);
   }

   private void updateParticleFormat(CompoundTag options, String particleType) {
      if ("block".equals(particleType) || "block_marker".equals(particleType) || "falling_dust".equals(particleType) || "dust_pillar".equals(particleType)) {
         this.moveTag(options, "value", "block_state");
      } else if ("item".equals(particleType)) {
         this.moveTag(options, "value", "item");
      } else if ("dust_color_transition".equals(particleType)) {
         this.moveTag(options, "fromColor", "from_color");
         this.moveTag(options, "toColor", "to_color");
      } else if ("entity_effect".equals(particleType)) {
         this.moveTag(options, "value", "color");
      }
   }

   private void moveTag(CompoundTag compoundTag, String from, String to) {
      Tag tag = compoundTag.remove(from);
      if (tag != null) {
         compoundTag.put(to, tag);
      }
   }

   private void checkSoundTag(@Nullable CompoundTag tag, String key) {
      if (tag != null) {
         String sound = tag.getString(key);
         if (sound != null && this.protocol.getMappingData().soundId(sound) == -1) {
            CompoundTag directSoundValue = new CompoundTag();
            directSoundValue.putString("sound_id", sound);
            tag.put(key, directSoundValue);
         }
      }
   }

   private void replaceNullValues(RegistryEntry[] entries) {
      RegistryEntry first = null;

      for (RegistryEntry registryEntry : entries) {
         if (registryEntry != null) {
            first = registryEntry;
            break;
         }
      }

      for (int i = 0; i < entries.length; i++) {
         if (entries[i] == null) {
            entries[i] = first.withKey(UUIDUtil.randomUUID().toString());
         }
      }
   }

   private void sendRangeAttributes(UserConnection connection, boolean creativeMode) {
      PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_20_5.UPDATE_ATTRIBUTES, connection);
      wrapper.write(Types.VAR_INT, this.<EntityTracker>tracker(connection).clientEntityId());
      if (connection.getProtocolInfo().serverProtocolVersion().olderThanOrEqualTo(ProtocolVersion.v1_7_6)) {
         wrapper.write(Types.VAR_INT, 3);
         this.writeAttribute(wrapper, "generic.step_height", 0.5, null, 0.0);
      } else {
         wrapper.write(Types.VAR_INT, 2);
      }

      this.writeAttribute(wrapper, "player.block_interaction_range", 4.5, creativeMode ? CREATIVE_BLOCK_INTERACTION_RANGE : null, 0.5);
      if (connection.getProtocolInfo().serverProtocolVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
         this.writeAttribute(wrapper, "player.entity_interaction_range", 3.0, creativeMode ? CREATIVE_ENTITY_INTERACTION_RANGE : null, 1.0);
      } else {
         this.writeAttribute(wrapper, "player.entity_interaction_range", 3.0, creativeMode ? CREATIVE_ENTITY_INTERACTION_RANGE : null, 2.0);
      }

      wrapper.scheduleSend(Protocol1_20_3To1_20_5.class);
   }

   private void writeAttribute(PacketWrapper wrapper, String attributeId, double base, @Nullable UUID modifierId, double amount) {
      wrapper.write(Types.VAR_INT, Attributes1_20_5.keyToId(attributeId));
      wrapper.write(Types.DOUBLE, base);
      if (modifierId != null) {
         wrapper.write(Types.VAR_INT, 1);
         wrapper.write(Types.UUID, modifierId);
         wrapper.write(Types.DOUBLE, amount);
         wrapper.write(Types.BYTE, (byte)0);
      } else {
         wrapper.write(Types.VAR_INT, 0);
      }
   }

   static int withAlpha(int rgb) {
      return 0xFF000000 | rgb & 16777215;
   }

   @Override
   protected void registerRewrites() {
      this.filter().mapDataType(typeId -> {
         int id = typeId;
         if (typeId >= VersionedTypes.V1_20_5.entityDataTypes.particlesType.typeId()) {
            id = typeId + 1;
         }

         if (id >= VersionedTypes.V1_20_5.entityDataTypes.wolfVariantType.typeId()) {
            id++;
         }

         if (id >= VersionedTypes.V1_20_5.entityDataTypes.armadilloState.typeId()) {
            id++;
         }

         return VersionedTypes.V1_20_5.entityDataTypes.byId(id);
      });
      this.registerEntityDataTypeHandler(
         VersionedTypes.V1_20_5.entityDataTypes.itemType,
         VersionedTypes.V1_20_5.entityDataTypes.blockStateType,
         VersionedTypes.V1_20_5.entityDataTypes.optionalBlockStateType,
         VersionedTypes.V1_20_5.entityDataTypes.particleType,
         null,
         VersionedTypes.V1_20_5.entityDataTypes.componentType,
         VersionedTypes.V1_20_5.entityDataTypes.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_20_5.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_20_5.LIVING_ENTITY).index(10).handler((event, data) -> {
         int effectColor = data.<Integer>value();
         if (effectColor == 0) {
            data.setTypeAndValue(VersionedTypes.V1_20_5.entityDataTypes.particlesType, new Particle[0]);
         } else {
            Particle particle = new Particle(this.protocol.getMappingData().getParticleMappings().mappedId("entity_effect"));
            particle.add(Types.INT, withAlpha(effectColor));
            data.setTypeAndValue(VersionedTypes.V1_20_5.entityDataTypes.particlesType, new Particle[]{particle});
         }
      });
      this.filter().type(EntityTypes1_20_5.LLAMA).handler((event, data) -> {
         int dataIndex = event.index();
         if (dataIndex == 20) {
            event.cancel();
            int color = data.<Integer>value();
            PacketWrapper setEquipment = PacketWrapper.create(ClientboundPackets1_20_5.SET_EQUIPMENT, event.user());
            setEquipment.write(Types.VAR_INT, event.entityId());
            setEquipment.write(Types.BYTE, (byte)6);
            setEquipment.write(VersionedTypes.V1_20_5.item, new StructuredItem(color + 446, 1, new StructuredDataContainer()));
            setEquipment.scheduleSend(Protocol1_20_3To1_20_5.class);
         } else if (dataIndex > 20) {
            event.setIndex(dataIndex - 1);
         }
      });
      this.filter().type(EntityTypes1_20_5.AREA_EFFECT_CLOUD).handler((event, data) -> {
         int dataIndex = event.index();
         if (dataIndex == 9) {
            EntityData particleData = event.dataAtIndex(11);
            int color = data.<Integer>value();
            if (particleData == null) {
               if (color != 0) {
                  Particle particle = new Particle(this.protocol.getMappingData().getParticleMappings().mappedId("entity_effect"));
                  particle.add(Types.INT, withAlpha(color));
                  event.createExtraData(new EntityData(10, VersionedTypes.V1_20_5.entityDataTypes.particleType, particle));
               }
            } else {
               this.addColor(particleData, color);
            }

            event.cancel();
         } else {
            if (dataIndex == 11) {
               EntityData colorData = event.dataAtIndex(9);
               if (colorData != null && colorData.dataType() == VersionedTypes.V1_20_5.entityDataTypes.varIntType) {
                  this.addColor(data, colorData.<Integer>value());
               }
            }

            if (dataIndex > 9) {
               event.setIndex(dataIndex - 1);
            }
         }
      });
      this.filter().type(EntityTypes1_20_5.ARROW).index(10).handler((event, data) -> {
         int color = data.<Integer>value();
         if (color != -1) {
            data.setValue(withAlpha(color));
         }
      });
      this.filter().type(EntityTypes1_20_5.ITEM_PROJECTILE).index(8).handler((event, data) -> {
         Item item = data.value();
         if (item == null || item.isEmpty()) {
            event.cancel();
         }
      });
   }

   private void addColor(@Nullable EntityData particleMeta, int color) {
      if (particleMeta != null) {
         Particle particle = particleMeta.value();
         if (particle.id() == this.protocol.getMappingData().getParticleMappings().mappedId("entity_effect")) {
            particle.<Integer>getArgument(0).setValue(withAlpha(color));
         }
      }
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_20_5.getTypeFromId(type);
   }
}
