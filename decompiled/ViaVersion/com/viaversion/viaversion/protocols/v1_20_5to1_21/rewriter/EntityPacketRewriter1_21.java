package com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.PaintingVariant;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.Paintings1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.ProtocolStorables1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.WolfVariantRegistryMarker;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;

public final class EntityPacketRewriter1_21 extends EntityRewriter<ClientboundPacket1_20_5, Protocol1_20_5To1_21> {
   public EntityPacketRewriter1_21(Protocol1_20_5To1_21 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      RegistryDataRewriter registryDataRewriter = new RegistryDataRewriter(this.protocol);
      CompoundTag campfireDamageType = new CompoundTag();
      campfireDamageType.putString("scaling", "when_caused_by_living_non_player");
      campfireDamageType.putString("message_id", "inFire");
      campfireDamageType.putFloat("exhaustion", 0.1F);
      registryDataRewriter.addEntries("damage_type", new RegistryEntry("minecraft:campfire", campfireDamageType));
      this.protocol.registerClientbound(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA, wrapper -> {
         String registryKey = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
         RegistryEntry[] entries = wrapper.read(Types.REGISTRY_ENTRY_ARRAY);
         entries = registryDataRewriter.handle(wrapper.user(), registryKey, entries);
         wrapper.write(Types.REGISTRY_ENTRY_ARRAY, entries);
         if (registryKey.equals("wolf_variant")) {
            wrapper.user().put(new WolfVariantRegistryMarker());
         }
      });
      this.protocol.appendClientbound(ClientboundConfigurationPackets1_20_5.FINISH_CONFIGURATION, wrapper -> {
         PacketWrapper paintingRegistryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
         paintingRegistryPacket.write(Types.STRING, "minecraft:painting_variant");
         RegistryEntry[] paintingsRegistry = new RegistryEntry[Paintings1_20_5.PAINTINGS.length];

         for (int i = 0; i < Paintings1_20_5.PAINTINGS.length; i++) {
            PaintingVariant painting = Paintings1_20_5.PAINTINGS[i];
            CompoundTag tag = new CompoundTag();
            tag.putInt("width", painting.width());
            tag.putInt("height", painting.height());
            tag.putString("asset_id", painting.assetId());
            paintingsRegistry[i] = new RegistryEntry(painting.assetId(), tag);
         }

         paintingRegistryPacket.write(Types.REGISTRY_ENTRY_ARRAY, paintingsRegistry);
         paintingRegistryPacket.send(Protocol1_20_5To1_21.class);
         PacketWrapper enchantmentRegistryPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
         enchantmentRegistryPacket.write(Types.STRING, "minecraft:enchantment");
         RegistryEntry[] enchantmentRegistry = new RegistryEntry[Enchantments1_20_5.ENCHANTMENTS.size()];

         for (int i = 0; i < Enchantments1_20_5.ENCHANTMENTS.size(); i++) {
            String key = Enchantments1_20_5.idToKey(i);
            CompoundTag tag = this.protocol.getMappingData().enchantment(i);
            enchantmentRegistry[i] = new RegistryEntry(key, tag);
         }

         enchantmentRegistryPacket.write(Types.REGISTRY_ENTRY_ARRAY, enchantmentRegistry);
         enchantmentRegistryPacket.send(Protocol1_20_5To1_21.class);
         PacketWrapper jukeboxSongsPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
         jukeboxSongsPacket.write(Types.STRING, "minecraft:jukebox_song");
         jukeboxSongsPacket.write(Types.REGISTRY_ENTRY_ARRAY, this.protocol.getMappingData().jukeboxSongs());
         jukeboxSongsPacket.send(Protocol1_20_5To1_21.class);
         if (!wrapper.user().has(WolfVariantRegistryMarker.class)) {
            createDefaultWolfVariantRegistryDataPacket(wrapper).send(Protocol1_20_5To1_21.class);
         }
      });
      this.protocol.appendClientbound(ClientboundPackets1_20_5.LOGIN, wrapper -> {
         ProtocolStorables1_21 storables = wrapper.user().storables(this.protocol);
         storables.efficiencyAttributes().onLoginSent(wrapper.get(Types.INT, 0), wrapper.user());
      });
      this.protocol.appendClientbound(ClientboundPackets1_20_5.RESPAWN, wrapper -> {
         ProtocolStorables1_21 storables = wrapper.user().storables(this.protocol);
         storables.efficiencyAttributes().onRespawn(wrapper.user());
         storables.playerPosition().setPosition(0.0, 0.0, 0.0);
         storables.playerPosition().setOnGround(false);
      });
      this.protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS, wrapper -> {
         if (Via.getConfig().fix1_21PlacementRotation()) {
            this.storePosition(wrapper);
            this.storeOnGround(wrapper);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_ROT, wrapper -> {
         if (Via.getConfig().fix1_21PlacementRotation()) {
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.FLOAT);
            this.storeOnGround(wrapper);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT, wrapper -> {
         if (Via.getConfig().fix1_21PlacementRotation()) {
            this.storePosition(wrapper);
            wrapper.passthrough(Types.FLOAT);
            wrapper.passthrough(Types.FLOAT);
            this.storeOnGround(wrapper);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_STATUS_ONLY, wrapper -> {
         if (Via.getConfig().fix1_21PlacementRotation()) {
            this.storeOnGround(wrapper);
         }
      });
   }

   private static PacketWrapper createDefaultWolfVariantRegistryDataPacket(PacketWrapper wrapper) {
      PacketWrapper wolfVariantPacket = wrapper.create(ClientboundConfigurationPackets1_20_5.REGISTRY_DATA);
      wolfVariantPacket.write(Types.STRING, "minecraft:wolf_variant");
      wolfVariantPacket.write(Types.REGISTRY_ENTRY_ARRAY, getDefaultWolfVariantRegistryEntries());
      return wolfVariantPacket;
   }

   private static RegistryEntry[] getDefaultWolfVariantRegistryEntries() {
      CompoundTag paleWolfVariant = new CompoundTag();
      paleWolfVariant.putString("wild_texture", "minecraft:entity/wolf/wolf");
      paleWolfVariant.putString("angry_texture", "minecraft:entity/wolf/wolf_angry");
      paleWolfVariant.put("biomes", new ListTag<>(StringTag.class));
      paleWolfVariant.putString("tame_texture", "minecraft:entity/wolf/wolf_tame");
      return new RegistryEntry[]{new RegistryEntry("minecraft:pale", paleWolfVariant)};
   }

   private void storePosition(PacketWrapper wrapper) {
      double x = wrapper.passthrough(Types.DOUBLE);
      double y = wrapper.passthrough(Types.DOUBLE);
      double z = wrapper.passthrough(Types.DOUBLE);
      ProtocolStorables1_21 storables = wrapper.user().storables(this.protocol);
      storables.playerPosition().setPosition(x, y, z);
   }

   private void storeOnGround(PacketWrapper wrapper) {
      boolean onGround = wrapper.passthrough(Types.BOOLEAN);
      ProtocolStorables1_21 storables = wrapper.user().storables(this.protocol);
      storables.playerPosition().setOnGround(onGround);
   }

   @Override
   protected void registerRewrites() {
      this.dataTypeMapper()
         .skip(VersionedTypes.V1_20_5.entityDataTypes.wolfVariantType)
         .skip(VersionedTypes.V1_20_5.entityDataTypes.paintingVariantType)
         .register();
      this.filter().dataType(VersionedTypes.V1_20_5.entityDataTypes.paintingVariantType).handler((event, data) -> {
         int variant = data.<Integer>value();
         data.setTypeAndValue(VersionedTypes.V1_21.entityDataTypes.paintingVariantType, Holder.of(variant));
      });
      this.filter().dataType(VersionedTypes.V1_20_5.entityDataTypes.wolfVariantType).handler((event, data) -> {
         int variant = data.<Integer>value();
         data.setTypeAndValue(VersionedTypes.V1_21.entityDataTypes.wolfVariantType, Holder.of(variant));
      });
      this.registerEntityDataTypeHandler(
         VersionedTypes.V1_21.entityDataTypes.itemType,
         VersionedTypes.V1_21.entityDataTypes.blockStateType,
         VersionedTypes.V1_21.entityDataTypes.optionalBlockStateType,
         VersionedTypes.V1_21.entityDataTypes.particleType,
         VersionedTypes.V1_21.entityDataTypes.particlesType,
         VersionedTypes.V1_21.entityDataTypes.componentType,
         VersionedTypes.V1_21.entityDataTypes.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_20_5.ABSTRACT_MINECART, 11);
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_20_5.getTypeFromId(type);
   }
}
