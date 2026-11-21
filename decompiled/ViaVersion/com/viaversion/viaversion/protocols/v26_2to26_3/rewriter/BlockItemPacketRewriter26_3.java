package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItemTemplate;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations26_3;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ConsumeEffect;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.TeleportRandomlyConsumeEffect;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrim1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrim26_3;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimMaterial1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimMaterial26_3;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ClientboundPackets26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPacket26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPackets26_3;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter26_3 extends StructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket26_3, Protocol26_2To26_3> {
   private static final Set<String> NEW_MAP_DECORATION_TYPES = Set.of("abandoned_camp", "ancient_city", "desert_pyramid", "mineshaft", "ocean_ruin_warm");
   private static final int TELEPORT_RANDOMLY_EFFECT = 3;
   private static final String TRIM_PALETTE_PREFIX = "trim/";
   private static final int BREWING_STAND_MENU_TYPE = 11;
   private static final int BREWING_STAND_TOTAL_BREW_TIME_SLOT = 2;
   private static final int BREWING_STAND_TOTAL_FUEL_SLOT = 3;

   public BlockItemPacketRewriter26_3(Protocol26_2To26_3 protocol) {
      super(protocol);
   }

   @Override
   protected void registerPackets() {
      this.protocol.replaceClientbound(ClientboundPackets26_1.LEVEL_PARTICLES, wrapper -> {
         wrapper.write(this.protocol.getParticleRewriter().mappedParticleType(), new Particle(0));
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.BOOLEAN);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         float maxSpeed = wrapper.read(Types.FLOAT);
         wrapper.write(Types.FLOAT, maxSpeed);
         wrapper.write(Types.FLOAT, maxSpeed);
         wrapper.write(Types.FLOAT, maxSpeed);
         wrapper.passthroughAndMap(Types.INT, Types.VAR_INT);
         wrapper.write(Types.VAR_INT, 0);
         Particle particle = wrapper.read(this.protocol.getParticleRewriter().particleType());
         this.protocol.getParticleRewriter().rewriteParticle(wrapper.user(), particle);
         wrapper.set(this.protocol.getParticleRewriter().mappedParticleType(), 0, particle);
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.EXPLODE, wrapper -> wrapper.write(Types.BOOLEAN, true));
      this.protocol.registerClientbound(ClientboundPackets26_1.OPEN_SIGN_EDITOR, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         int signTextSlot = wrapper.read(Types.BOOLEAN) ? 1 : 0;
         wrapper.write(Types.VAR_INT, signTextSlot);
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.OPEN_SCREEN, wrapper -> {
         wrapper.resetReader();
         int containerId = wrapper.passthrough(Types.VAR_INT);
         int menuType = wrapper.passthrough(Types.VAR_INT);
         if (menuType == 11) {
            wrapper.send(Protocol26_2To26_3.class);
            wrapper.cancel();
            this.sendContainerData(wrapper, containerId, 2, 400);
            this.sendContainerData(wrapper, containerId, 3, 20);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets26_3.SIGN_UPDATE, wrapper -> {
         wrapper.passthrough(Types.BLOCK_POSITION1_14);
         wrapper.write(Types.BOOLEAN, true);

         for (int i = 0; i < 4; i++) {
            wrapper.passthrough(Types.STRING);
         }

         int signTextSlot = wrapper.read(Types.VAR_INT);
         if (signTextSlot == 0) {
            wrapper.set(Types.BOOLEAN, 0, false);
         }
      });
      this.protocol.registerClientbound(ClientboundPackets26_1.LIGHT_UPDATE, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         wrapper.passthrough(Types.VAR_INT);
         this.handleLightMasks(wrapper);
      });
      this.protocol.appendClientbound(ClientboundPackets26_1.LEVEL_CHUNK_WITH_LIGHT, this::handleLightMasks);
      this.protocol.replaceClientbound(ClientboundPackets26_1.UPDATE_ADVANCEMENTS, wrapper -> {
         float x = 0.0F;
         float y = 0.0F;
         wrapper.passthrough(Types.BOOLEAN);
         int size = wrapper.passthrough(Types.VAR_INT);

         for (int i = 0; i < size; i++) {
            wrapper.passthrough(Types.STRING);
            wrapper.passthrough(Types.OPTIONAL_STRING);
            if (wrapper.passthrough(Types.BOOLEAN)) {
               Tag title = wrapper.passthrough(Types.TRUSTED_TAG);
               Tag description = wrapper.passthrough(Types.TRUSTED_TAG);
               NBTComponentRewriter<ClientboundPacket26_1> componentRewriter = this.protocol.getComponentRewriter();
               componentRewriter.processTag(wrapper.user(), title);
               componentRewriter.processTag(wrapper.user(), description);
               this.passthroughClientboundItemTemplate(wrapper);
               wrapper.passthrough(Types.VAR_INT);
               int flags = wrapper.passthrough(Types.INT);
               if ((flags & 1) != 0) {
                  wrapper.passthrough(Types.STRING);
               }

               x = wrapper.read(Types.FLOAT);
               y = wrapper.read(Types.FLOAT);
            }

            int requirements = wrapper.passthrough(Types.VAR_INT);

            for (int array = 0; array < requirements; array++) {
               wrapper.passthrough(Types.STRING_ARRAY);
            }

            wrapper.passthrough(Types.BOOLEAN);
            wrapper.write(Types.FLOAT, x);
            wrapper.write(Types.FLOAT, y);
         }
      });
   }

   private void sendContainerData(PacketWrapper wrapper, int containerId, int slot, int value) {
      PacketWrapper containerData = wrapper.create(ClientboundPackets26_3.CONTAINER_SET_DATA);
      containerData.write(Types.VAR_INT, containerId);
      containerData.write(Types.SHORT, (short)slot);
      containerData.write(Types.SHORT, (short)value);
      containerData.send(Protocol26_2To26_3.class);
   }

   private void handleLightMasks(PacketWrapper wrapper) {
      for (int i = 0; i < 4; i++) {
         long[] mask = wrapper.read(Types.LONG_ARRAY_PRIMITIVE);
         wrapper.write(Types.BIT_SET, BitSet.valueOf(mask));
      }
   }

   @Override
   protected void handleItemDataComponentsToClient(UserConnection connection, Item item, StructuredDataContainer container) {
      upgradeData(container);
      this.appendItemDataFixComponents(connection, item, container);
      super.handleItemDataComponentsToClient(connection, item, container);
   }

   @Override
   protected void handleItemDataComponentsToServer(UserConnection connection, Item item, StructuredDataContainer container) {
      downgradeData(container);
      super.handleItemDataComponentsToServer(connection, item, container);
   }

   private void appendItemDataFixComponents(UserConnection connection, Item item, StructuredDataContainer container) {
      ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
      if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_12_2)
         && (
            item.identifier() == 1043
               || item.identifier() == 1033
               || item.identifier() == 1053
               || item.identifier() == 1058
               || item.identifier() == 1048
               || item.identifier() == 1038
               || item.identifier() == 1028
         )) {
         container.setEmpty(StructuredDataKey.BLOCK_TRANSFORMER);
      }

      if (serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_8)
         && (
            item.identifier() == 1036
               || item.identifier() == 1046
               || item.identifier() == 1041
               || item.identifier() == 1026
               || item.identifier() == 1056
               || item.identifier() == 1031
               || item.identifier() == 1051
         )) {
         container.setEmpty(StructuredDataKey.BLOCK_TRANSFORMER);
      }
   }

   public static void upgradeData(StructuredDataContainer container) {
      container.remove(StructuredDataKey.MAP_COLOR);
      container.replaceKey(StructuredDataKey.INSTRUMENT26_1, StructuredDataKey.INSTRUMENT26_3);
      if (container.hasEmpty(StructuredDataKey.SWING_ANIMATION)) {
         container.remove(StructuredDataKey.SWING_ANIMATION);
         container.setEmpty(StructuredDataKey.ATTACK_ANIMATION);
         container.setEmpty(StructuredDataKey.INTERACT_ANIMATION);
      }

      container.replace(StructuredDataKey.SWING_ANIMATION, animation -> {
         container.set(StructuredDataKey.ATTACK_ANIMATION, animation);
         container.set(StructuredDataKey.INTERACT_ANIMATION, animation);
         return null;
      });
      container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3, holder -> {
         if (holder.hasId()) {
            return Holder.of(holder.id());
         } else {
            ArmorTrimMaterial1_20_5 material = holder.value();
            return Holder.of(new ArmorTrimMaterial26_3(trimPaletteId(material.assetName()), material.description()));
         }
      });
      container.replace(StructuredDataKey.TRIM1_21_5, StructuredDataKey.TRIM26_3, trim -> {
         if (trim.material().hasId()) {
            return new ArmorTrim26_3(Holder.of(trim.material().id()), trim.pattern());
         } else {
            ArmorTrimMaterial1_20_5 material = trim.material().value();
            return new ArmorTrim26_3(Holder.of(new ArmorTrimMaterial26_3(trimPaletteId(material.assetName()), material.description())), trim.pattern());
         }
      });
      container.replace(
         StructuredDataKey.POT_DECORATIONS1_20_5,
         VersionedTypes.V26_3.structuredDataKeys().potDecorations,
         decorations -> new PotDecorations26_3(
            new StructuredItemTemplate(decorations.backItem(), 1),
            new StructuredItemTemplate(decorations.leftItem(), 1),
            new StructuredItemTemplate(decorations.rightItem(), 1),
            new StructuredItemTemplate(decorations.frontItem(), 1)
         )
      );
      container.replace(StructuredDataKey.CONSUMABLE1_21_2, StructuredDataKey.CONSUMABLE26_3, consumable -> {
         upgradeConsumeEffects(consumable.consumeEffects());
         return consumable;
      });
      container.replace(StructuredDataKey.DEATH_PROTECTION1_21_2, StructuredDataKey.DEATH_PROTECTION26_3, deathProtection -> {
         upgradeConsumeEffects(deathProtection.deathEffects());
         return deathProtection;
      });
   }

   public static void downgradeData(StructuredDataContainer container) {
      container.remove(StructuredDataKey.PROVIDES_POTTERY_PATTERN);
      container.remove(StructuredDataKey.BLOCK_TRANSFORMER);
      container.remove(StructuredDataKey.COMPOSTABLE);
      container.remove(StructuredDataKey.VILLAGER_FOOD);
      container.remove(StructuredDataKey.COOKING_FUEL);
      container.remove(StructuredDataKey.BREWING_FUEL);
      container.remove(StructuredDataKey.MOB_VISIBILITY);
      container.remove(StructuredDataKey.SIGN_TEXT_FRONT);
      container.remove(StructuredDataKey.SIGN_TEXT_BACK);
      container.remove(StructuredDataKey.WAXED);
      container.remove(StructuredDataKey.CUSHION_COLOR);
      CompoundTag mapDecorations = container.get(StructuredDataKey.MAP_DECORATIONS);
      if (mapDecorations != null) {
         for (Entry<String, Tag> entry : mapDecorations.entrySet()) {
            StringTag typeTag = ((CompoundTag)entry.getValue()).getStringTag("type");
            if (NEW_MAP_DECORATION_TYPES.contains(Key.stripMinecraftNamespace(typeTag.getValue()))) {
               typeTag.setValue("village_plains");
            }
         }
      }

      container.replace(StructuredDataKey.CONSUMABLE26_3, StructuredDataKey.CONSUMABLE1_21_2, consumable -> {
         downgradeConsumeEffects(consumable.consumeEffects());
         return consumable;
      });
      container.replace(StructuredDataKey.DEATH_PROTECTION26_3, StructuredDataKey.DEATH_PROTECTION1_21_2, deathProtection -> {
         downgradeConsumeEffects(deathProtection.deathEffects());
         return deathProtection;
      });
      container.replaceKey(StructuredDataKey.ATTACK_ANIMATION, StructuredDataKey.SWING_ANIMATION);
      container.replaceKey(StructuredDataKey.INTERACT_ANIMATION, StructuredDataKey.SWING_ANIMATION);
      container.replaceKey(StructuredDataKey.INSTRUMENT26_3, StructuredDataKey.INSTRUMENT26_1);
      container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, holder -> {
         if (holder.hasId()) {
            return Holder.of(holder.id());
         } else {
            ArmorTrimMaterial26_3 trim = holder.value();
            return Holder.of(new ArmorTrimMaterial1_20_5(trimAssetName(trim.paletteId()), new HashMap<>(), trim.description()));
         }
      });
      container.replace(
         StructuredDataKey.TRIM26_3,
         StructuredDataKey.TRIM1_21_5,
         trim -> {
            if (trim.material().hasId()) {
               return new ArmorTrim1_20_5(Holder.of(trim.material().id()), trim.pattern());
            } else {
               ArmorTrimMaterial26_3 material = trim.material().value();
               return new ArmorTrim1_20_5(
                  Holder.of(new ArmorTrimMaterial1_20_5(trimAssetName(material.paletteId()), new HashMap<>(), material.description())), trim.pattern()
               );
            }
         }
      );
      container.replace(
         VersionedTypes.V26_3.structuredDataKeys().potDecorations,
         StructuredDataKey.POT_DECORATIONS1_20_5,
         decorations -> new PotDecorations1_20_5(
            new int[]{
               potDecorationId(decorations.back()),
               potDecorationId(decorations.left()),
               potDecorationId(decorations.right()),
               potDecorationId(decorations.front())
            }
         )
      );
   }

   public static String trimPaletteId(String assetName) {
      return Key.namespaced("trim/" + assetName);
   }

   public static String trimAssetName(String paletteId) {
      String path = Key.stripNamespace(paletteId);
      return path.startsWith("trim/") ? path.substring("trim/".length()) : path;
   }

   private static void upgradeConsumeEffects(ConsumeEffect<?>[] effects) {
      for (int i = 0; i < effects.length; i++) {
         if (effects[i].value() instanceof Float diameter) {
            effects[i] = new ConsumeEffect<>(3, ConsumeEffect.TELEPORT_RANDOMLY_TYPE26_3, new TeleportRandomlyConsumeEffect(diameter, false));
         }
      }
   }

   private static void downgradeConsumeEffects(ConsumeEffect<?>[] effects) {
      for (int i = 0; i < effects.length; i++) {
         if (effects[i].value() instanceof TeleportRandomlyConsumeEffect effect) {
            effects[i] = new ConsumeEffect<>(3, ConsumeEffect.TELEPORT_RANDOMLY_TYPE1_21_2, effect.diameter());
         }
      }
   }

   private static int potDecorationId(@Nullable Item decoration) {
      return decoration != null ? decoration.identifier() : Protocol26_2To26_3.MAPPINGS.getFullItemMappings().id("brick");
   }

   @Override
   protected void backupInconvertibleData(UserConnection connection, Item item, StructuredDataContainer container, CompoundTag backupTag) {
      super.backupInconvertibleData(connection, item, container, backupTag);
      ArmorTrim1_20_5 trim = container.get(StructuredDataKey.TRIM1_21_5);
      if (trim != null) {
         this.backupTrimMaterial(backupTag, "trim", trim.material());
      }

      this.backupTrimMaterial(backupTag, "provides_trim_material", container.get(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1));
      Integer mapColor = container.get(StructuredDataKey.MAP_COLOR);
      if (mapColor != null) {
         backupTag.putInt("map_color", mapColor);
      }
   }

   private void backupTrimMaterial(CompoundTag tag, String key, @Nullable Holder<ArmorTrimMaterial1_20_5> materialHolder) {
      if (materialHolder != null && !materialHolder.hasId()) {
         CompoundTag trimTag = new CompoundTag();
         ArmorTrimMaterial1_20_5 material = materialHolder.value();
         CompoundTag overrides = new CompoundTag();

         for (Entry<String, String> entry : material.overrideArmorMaterials().entrySet()) {
            overrides.putString(entry.getKey(), entry.getValue());
         }

         trimTag.put("overrides", overrides);
         trimTag.putInt("item", material.itemId());
         tag.put(key, trimTag);
      }
   }

   @Override
   protected void restoreBackupData(Item item, StructuredDataContainer container, CompoundTag customData) {
      super.restoreBackupData(item, container, customData);
      if (customData.remove(this.nbtTagName("backup")) instanceof CompoundTag backupTag) {
         CompoundTag trimTag = backupTag.getCompoundTag("trim");
         if (trimTag != null) {
            container.replace(
               StructuredDataKey.TRIM26_3,
               StructuredDataKey.TRIM1_21_5,
               trim -> new ArmorTrim1_20_5(this.restoreTrimMaterial(trim.material(), trimTag), trim.pattern())
            );
         }

         CompoundTag providesTrimMaterialTag = backupTag.getCompoundTag("provides_trim_material");
         if (providesTrimMaterialTag != null) {
            container.replace(
               StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3,
               StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1,
               material -> this.restoreTrimMaterial(material, providesTrimMaterialTag)
            );
         }

         IntTag mapColorTag = backupTag.getIntTag("map_color");
         if (mapColorTag != null) {
            container.set(StructuredDataKey.MAP_COLOR, mapColorTag.asInt());
         }
      }
   }

   private Holder<ArmorTrimMaterial1_20_5> restoreTrimMaterial(Holder<ArmorTrimMaterial26_3> holder, CompoundTag tag) {
      if (holder.hasId()) {
         return Holder.of(holder.id());
      } else {
         ArmorTrimMaterial26_3 material = holder.value();
         Map<String, String> overrides = new HashMap<>();
         CompoundTag overridesTag = tag.getCompoundTag("overrides");
         if (overridesTag != null) {
            for (Entry<String, Tag> entry : overridesTag.entrySet()) {
               if (entry.getValue() instanceof StringTag value) {
                  overrides.put(entry.getKey(), value.getValue());
               }
            }
         }

         return Holder.of(new ArmorTrimMaterial1_20_5(trimAssetName(material.paletteId()), tag.getInt("item"), overrides, material.description()));
      }
   }
}
