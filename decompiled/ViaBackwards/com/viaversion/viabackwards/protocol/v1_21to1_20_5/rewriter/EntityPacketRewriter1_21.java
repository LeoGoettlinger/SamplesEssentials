package com.viaversion.viabackwards.protocol.v1_21to1_20_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.Protocol1_21To1_20_5;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.EnchantmentsPaintingsStorage;
import com.viaversion.viabackwards.protocol.v1_21to1_20_5.storage.PlayerRotationStorage;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.PaintingVariant;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.minecraft.WolfVariant;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.Paintings1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import java.util.HashMap;
import java.util.Map;

public final class EntityPacketRewriter1_21 extends EntityRewriter<ClientboundPacket1_21, Protocol1_21To1_20_5> {
   private final Map<String, EntityPacketRewriter1_21.PaintingData> oldPaintings = new HashMap<>();

   public EntityPacketRewriter1_21(Protocol1_21To1_20_5 protocol) {
      super(
         protocol,
         ((EntityDataTypes1_20_5)protocol.mappedTypes().entityDataTypes()).optionalComponentType,
         ((EntityDataTypes1_20_5)protocol.mappedTypes().entityDataTypes()).booleanType
      );

      for (int i = 0; i < Paintings1_20_5.PAINTINGS.length; i++) {
         PaintingVariant painting = Paintings1_20_5.PAINTINGS[i];
         this.oldPaintings.put(painting.assetId(), new EntityPacketRewriter1_21.PaintingData(painting, i));
      }
   }

   public void registerPackets() {
      ((Protocol1_21To1_20_5)this.protocol).replaceClientbound(ClientboundConfigurationPackets1_21.REGISTRY_DATA, wrapper -> {
         String key = Key.stripMinecraftNamespace((String)wrapper.passthrough(Types.STRING));
         RegistryEntry[] entries = (RegistryEntry[])wrapper.passthrough(Types.REGISTRY_ENTRY_ARRAY);
         boolean paintingVariant = key.equals("painting_variant");
         boolean enchantment = key.equals("enchantment");
         if (!paintingVariant && !enchantment && !key.equals("jukebox_song")) {
            ((Protocol1_21To1_20_5)this.protocol).getRegistryDataRewriter().trackDimensionAndBiomes(wrapper.user(), key, entries);
         } else {
            String[] keys = new String[entries.length];

            for (int i = 0; i < entries.length; i++) {
               keys[i] = Key.stripMinecraftNamespace(entries[i].key());
            }

            EnchantmentsPaintingsStorage storage = (EnchantmentsPaintingsStorage)wrapper.user().get(EnchantmentsPaintingsStorage.class);
            if (paintingVariant) {
               storage.setPaintings(new KeyMappings(keys), this.paintingMappingsForEntries(entries));
            } else if (enchantment) {
               Tag[] descriptions = new Tag[entries.length];
               int[] maxLevels = new int[entries.length];

               for (int i = 0; i < entries.length; i++) {
                  RegistryEntry entry = entries[i];
                  if (entry.tag() instanceof CompoundTag tag) {
                     descriptions[i] = tag.get("description");
                     maxLevels[i] = tag.getInt("max_level");
                  }
               }

               storage.setEnchantments(new KeyMappings(keys), descriptions, maxLevels);
            } else {
               int[] jukeboxSongMappings = new int[keys.length];

               for (int ix = 0; ix < keys.length; ix++) {
                  int itemId = ((Protocol1_21To1_20_5)this.protocol).getMappingData().getFullItemMappings().mappedId("music_disc_" + keys[ix]);
                  jukeboxSongMappings[ix] = itemId;
               }

               storage.setJubeboxSongsToItems(jukeboxSongMappings);
            }

            wrapper.cancel();
         }
      });
      ((Protocol1_21To1_20_5)this.protocol).registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         this.storePlayerRotation(wrapper);
      });
      ((Protocol1_21To1_20_5)this.protocol).registerServerbound(ServerboundPackets1_20_5.MOVE_PLAYER_ROT, this::storePlayerRotation);
   }

   private void storePlayerRotation(PacketWrapper wrapper) {
      float yaw = (Float)wrapper.passthrough(Types.FLOAT);
      float pitch = (Float)wrapper.passthrough(Types.FLOAT);
      ((PlayerRotationStorage)wrapper.user().get(PlayerRotationStorage.class)).setRotation(yaw, pitch);
   }

   private int[] paintingMappingsForEntries(RegistryEntry[] entries) {
      int[] mappings = new int[entries.length];

      for (int i = 0; i < entries.length; i++) {
         RegistryEntry entry = entries[i];
         EntityPacketRewriter1_21.PaintingData paintingData = this.oldPaintings.get(Key.stripMinecraftNamespace(entry.key()));
         if (paintingData != null) {
            mappings[i] = paintingData.id;
         } else if (entry.tag() != null) {
            CompoundTag tag = (CompoundTag)entry.tag();

            for (int j = 0; j < Paintings1_20_5.PAINTINGS.length; j++) {
               PaintingVariant painting = Paintings1_20_5.PAINTINGS[j];
               if (painting.width() == tag.getInt("width") && painting.height() == tag.getInt("height")) {
                  mappings[i] = j;
                  break;
               }
            }
         }
      }

      return mappings;
   }

   protected void registerRewrites() {
      EntityDataTypes1_20_5 mappedEntityDataTypes = (EntityDataTypes1_20_5)VersionedTypes.V1_20_5.entityDataTypes;
      this.dataTypeMapper()
         .skip(((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).wolfVariantType)
         .skip(((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).paintingVariantType)
         .register();
      this.filter().dataType(((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).wolfVariantType).handler((event, data) -> {
         Holder<WolfVariant> variant = (Holder<WolfVariant>)data.value();
         if (variant.hasId()) {
            data.setTypeAndValue(mappedEntityDataTypes.wolfVariantType, variant.id());
         } else {
            event.cancel();
         }
      });
      this.filter().dataType(((EntityDataTypes1_21)VersionedTypes.V1_21.entityDataTypes).paintingVariantType).handler((event, data) -> {
         Holder<PaintingVariant> variant = (Holder<PaintingVariant>)data.value();
         if (variant.hasId()) {
            EnchantmentsPaintingsStorage storage = (EnchantmentsPaintingsStorage)event.user().get(EnchantmentsPaintingsStorage.class);
            int mappedId = storage.mappedPainting(variant.id());
            data.setTypeAndValue(mappedEntityDataTypes.paintingVariantType, mappedId);
         } else {
            event.cancel();
         }
      });
      this.registerEntityDataTypeHandler1_20_3(
         mappedEntityDataTypes.itemType,
         mappedEntityDataTypes.blockStateType,
         mappedEntityDataTypes.optionalBlockStateType,
         mappedEntityDataTypes.particleType,
         mappedEntityDataTypes.particlesType,
         mappedEntityDataTypes.componentType,
         mappedEntityDataTypes.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_20_5.ABSTRACT_MINECART, 11);
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_20_5.getTypeFromId(type);
   }

   private record PaintingData(PaintingVariant painting, int id) {
   }
}
