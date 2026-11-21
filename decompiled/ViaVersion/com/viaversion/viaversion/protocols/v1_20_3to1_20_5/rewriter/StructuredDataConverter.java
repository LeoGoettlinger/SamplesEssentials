package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.rewriter;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AdventureModePredicate;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPattern;
import com.viaversion.viaversion.api.minecraft.item.data.BannerPatternLayer;
import com.viaversion.viaversion.api.minecraft.item.data.Bee;
import com.viaversion.viaversion.api.minecraft.item.data.BlockPredicate;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.FoodProperties1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.Instrument1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffect;
import com.viaversion.viaversion.api.minecraft.item.data.PotionEffectData;
import com.viaversion.viaversion.api.minecraft.item.data.StatePropertyMatcher;
import com.viaversion.viaversion.api.minecraft.item.data.SuspiciousStewEffect;
import com.viaversion.viaversion.api.minecraft.item.data.ToolRule;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimMaterial1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimPattern;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Reference2ObjectOpenHashMap;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Attributes1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.BannerPatterns1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Enchantments1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.EquipmentSlots1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Instruments1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.MapDecorations1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.PotionEffects1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data.Potions1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.ArmorTrimStorage;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage.BannerPatternStorage;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class StructuredDataConverter {
   static final int HIDE_ENCHANTMENTS = 1;
   static final int HIDE_ATTRIBUTES = 2;
   static final int HIDE_UNBREAKABLE = 4;
   static final int HIDE_CAN_DESTROY = 8;
   static final int HIDE_CAN_PLACE_ON = 16;
   static final int HIDE_ADDITIONAL = 32;
   static final int HIDE_DYE_COLOR = 64;
   static final int HIDE_ARMOR_TRIM = 128;
   private static final String BACKUP_TAG_KEY = "VV|DataComponents";
   private static final String ITEM_BACKUP_TAG_KEY = "VV|Id";
   private final Map<StructuredDataKey<?>, StructuredDataConverter.DataConverter<?>> rewriters = new Reference2ObjectOpenHashMap<>();
   private final boolean backupInconvertibleData;

   public StructuredDataConverter(boolean backupInconvertibleData) {
      this.backupInconvertibleData = backupInconvertibleData;
      this.register(StructuredDataKey.CUSTOM_DATA, (data, tag) -> {});
      this.register(StructuredDataKey.DAMAGE, (data, tag) -> tag.putInt("Damage", data));
      this.register(StructuredDataKey.UNBREAKABLE1_20_5, (data, tag) -> {
         tag.putBoolean("Unbreakable", true);
         if (!data.showInTooltip()) {
            this.putHideFlag(tag, 4);
         }
      });
      this.register(StructuredDataKey.CUSTOM_NAME, (data, tag) -> getDisplayTag(tag).putString("Name", ComponentUtil.tagToJsonString(data)));
      this.register(StructuredDataKey.ITEM_NAME, (data, tag) -> {
         CompoundTag displayTag = getDisplayTag(tag);
         if (!displayTag.contains("Name")) {
            CompoundTag name = new CompoundTag();
            name.putBoolean("italic", false);
            name.putString("text", "");
            name.put("extra", new ListTag<>(Collections.singletonList(data)));
            displayTag.putString("Name", ComponentUtil.tagToJsonString(name));
         }
      });
      this.register(StructuredDataKey.LORE, (data, tag) -> {
         ListTag<StringTag> lore = new ListTag<>(StringTag.class);

         for (Tag loreEntry : data) {
            lore.add(new StringTag(ComponentUtil.tagToJsonString(loreEntry)));
         }

         getDisplayTag(tag).put("Lore", lore);
      });
      this.register(StructuredDataKey.ENCHANTMENTS1_20_5, (data, tag) -> this.convertEnchantments(data, tag, false));
      this.register(StructuredDataKey.STORED_ENCHANTMENTS1_20_5, (data, tag) -> this.convertEnchantments(data, tag, true));
      this.register(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, (data, tag) -> {
         ListTag<CompoundTag> modifiers = new ListTag<>(CompoundTag.class);

         for (int i = 0; i < data.modifiers().length; i++) {
            AttributeModifiers1_20_5.AttributeModifier modifier = data.modifiers()[i];
            String identifier = Attributes1_20_5.idToKey(modifier.attribute());
            if (identifier != null) {
               CompoundTag modifierTag = new CompoundTag();
               modifierTag.put("UUID", new IntArrayTag(UUIDUtil.toIntArray(modifier.modifier().uuid())));
               modifierTag.putString("AttributeName", identifier.equals("generic.jump_strength") ? "horse.jump_strength" : identifier);
               modifierTag.putString("Name", modifier.modifier().name());
               modifierTag.putDouble("Amount", modifier.modifier().amount());
               if (modifier.slotType() != 0) {
                  modifierTag.putString("Slot", EquipmentSlots1_20_5.idToKey(modifier.slotType()));
               }

               modifierTag.putInt("Operation", modifier.modifier().operation());
               modifiers.add(modifierTag);
            }
         }

         tag.put("AttributeModifiers", modifiers);
         if (!data.showInTooltip()) {
            this.putHideFlag(tag, 2);
         }
      });
      this.register(StructuredDataKey.CUSTOM_MODEL_DATA1_20_5, (data, tag) -> tag.putInt("CustomModelData", data));
      this.register(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP, (data, tag) -> this.putHideFlag(tag, 32));
      this.register(StructuredDataKey.REPAIR_COST, (data, tag) -> tag.putInt("RepairCost", data));
      this.register(StructuredDataKey.DYED_COLOR1_20_5, (data, tag) -> {
         getDisplayTag(tag).putInt("color", data.rgb());
         if (!data.showInTooltip()) {
            this.putHideFlag(tag, 64);
         }
      });
      this.register(StructuredDataKey.MAP_COLOR, (data, tag) -> getDisplayTag(tag).putInt("MapColor", data));
      this.register(StructuredDataKey.MAP_ID, (data, tag) -> tag.putInt("map", data));
      this.register(StructuredDataKey.MAP_DECORATIONS, (data, tag) -> {
         ListTag<CompoundTag> decorations = new ListTag<>(CompoundTag.class);

         for (Entry<String, Tag> entry : data.entrySet()) {
            CompoundTag decorationTag = (CompoundTag)entry.getValue();
            int id = MapDecorations1_20_5.keyToId(decorationTag.getString("type"));
            if (id != -1) {
               CompoundTag convertedDecoration = new CompoundTag();
               convertedDecoration.putString("id", entry.getKey());
               convertedDecoration.putInt("type", id);
               convertedDecoration.putDouble("x", decorationTag.getDouble("x"));
               convertedDecoration.putDouble("z", decorationTag.getDouble("z"));
               convertedDecoration.putFloat("rot", decorationTag.getFloat("rotation"));
               decorations.add(convertedDecoration);
            }
         }

         tag.put("Decorations", decorations);
      });
      this.register(StructuredDataKey.WRITABLE_BOOK_CONTENT, (data, tag) -> {
         ListTag<StringTag> pages = new ListTag<>(StringTag.class);
         CompoundTag filteredPages = new CompoundTag();

         for (int i = 0; i < data.pages().length; i++) {
            FilterableString page = data.pages()[i];
            pages.add(new StringTag(page.raw()));
            if (page.filtered() != null) {
               filteredPages.putString(Integer.toString(i), page.filtered());
            }
         }

         tag.put("pages", pages);
         tag.put("filtered_pages", filteredPages);
      });
      this.register(StructuredDataKey.WRITTEN_BOOK_CONTENT, (data, tag) -> {
         ListTag<StringTag> pages = new ListTag<>(StringTag.class);
         CompoundTag filteredPages = new CompoundTag();

         for (int i = 0; i < data.pages().length; i++) {
            FilterableComponent page = data.pages()[i];
            pages.add(new StringTag(ComponentUtil.tagToJsonString(page.raw())));
            if (page.filtered() != null) {
               filteredPages.putString(Integer.toString(i), ComponentUtil.tagToJsonString(page.filtered()));
            }
         }

         tag.put("pages", pages);
         tag.put("filtered_pages", filteredPages);
         tag.putString("author", data.author());
         tag.putInt("generation", data.generation());
         tag.putBoolean("resolved", data.resolved());
         tag.putString("title", data.title().raw());
         if (data.title().filtered() != null) {
            tag.putString("filtered_title", data.title().filtered());
         }
      });
      this.register(StructuredDataKey.BASE_COLOR, (data, tag) -> getBlockEntityTag(tag).putInt("Base", data));
      this.register(StructuredDataKey.V1_20_5.chargedProjectiles, (connection, data, tag) -> {
         this.convertItemList(connection, data, tag, "ChargedProjectiles");
         if (data.length != 0) {
            tag.putBoolean("Charged", true);
         }
      });
      this.register(StructuredDataKey.V1_20_5.bundleContents, (connection, data, tag) -> this.convertItemList(connection, data, tag, "Items"));
      this.register(StructuredDataKey.LODESTONE_TRACKER, (data, tag) -> {
         tag.putBoolean("LodestoneTracked", data.tracked());
         if (data.position() != null) {
            CompoundTag positionTag = new CompoundTag();
            positionTag.putInt("X", data.position().x());
            positionTag.putInt("Y", data.position().y());
            positionTag.putInt("Z", data.position().z());
            tag.put("LodestonePos", positionTag);
            tag.putString("LodestoneDimension", data.position().dimension());
         } else {
            tag.putString("LodestoneDimension", "viaversion:viaversion");
         }
      });
      this.register(StructuredDataKey.FIREWORKS, (data, tag) -> {
         CompoundTag fireworksTag = new CompoundTag();
         fireworksTag.putByte("Flight", (byte)data.flightDuration());
         tag.put("Fireworks", fireworksTag);
         if (data.explosions().length > 0) {
            ListTag<CompoundTag> explosionsTag = new ListTag<>(CompoundTag.class);

            for (FireworkExplosion explosion : data.explosions()) {
               explosionsTag.add(this.convertExplosion(explosion));
            }

            fireworksTag.put("Explosions", explosionsTag);
         }
      });
      this.register(StructuredDataKey.FIREWORK_EXPLOSION, (data, tag) -> tag.put("Explosion", this.convertExplosion(data)));
      this.register(StructuredDataKey.PROFILE1_20_5, (data, tag) -> {
         if (data.name() != null && data.id() == null && data.properties().length == 0) {
            tag.putString("SkullOwner", data.name());
         } else {
            CompoundTag profileTag = new CompoundTag();
            tag.put("SkullOwner", profileTag);
            if (data.name() != null) {
               profileTag.putString("Name", data.name());
            }

            if (data.id() != null) {
               profileTag.put("Id", new IntArrayTag(UUIDUtil.toIntArray(data.id())));
            }

            CompoundTag propertiesTag = new CompoundTag();

            for (GameProfile.Property property : data.properties()) {
               ListTag<CompoundTag> values = propertiesTag.getListTag(property.name(), CompoundTag.class);
               if (values == null) {
                  values = new ListTag<>(CompoundTag.class);
               }

               CompoundTag propertyTag = new CompoundTag();
               propertyTag.putString("Value", property.value());
               if (property.signature() != null) {
                  propertyTag.putString("Signature", property.signature());
               }

               values.add(propertyTag);
               propertiesTag.put(property.name(), values);
            }

            profileTag.put("Properties", propertiesTag);
         }
      });
      this.register(StructuredDataKey.INSTRUMENT1_20_5, (data, tag) -> {
         if (!data.hasId()) {
            if (backupInconvertibleData) {
               CompoundTag backupTag = new CompoundTag();
               Instrument1_20_5 instrument = data.value();
               if (instrument.soundEvent().hasId()) {
                  backupTag.putInt("sound_event", instrument.soundEvent().id());
               } else {
                  CompoundTag soundEventTag = new CompoundTag();
                  SoundEvent soundEvent = instrument.soundEvent().value();
                  soundEventTag.putString("identifier", soundEvent.identifier());
                  if (soundEvent.fixedRange() != null) {
                     soundEventTag.putFloat("fixed_range", soundEvent.fixedRange());
                  }

                  backupTag.put("sound_event", soundEventTag);
               }

               backupTag.putInt("use_duration", instrument.useDuration());
               backupTag.putFloat("range", instrument.range());
               getBackupTag(tag).put("instrument", backupTag);
            }
         } else {
            String identifier = Instruments1_20_3.idToKey(data.id());
            if (identifier != null) {
               tag.putString("instrument", identifier);
            }
         }
      });
      this.register(StructuredDataKey.BEES1_20_5, (data, tag) -> {
         ListTag<CompoundTag> bees = new ListTag<>(CompoundTag.class);

         for (Bee bee : data) {
            CompoundTag beeTag = new CompoundTag();
            beeTag.put("EntityData", bee.entityData().tag());
            beeTag.putInt("TicksInHive", bee.ticksInHive());
            beeTag.putInt("MinOccupationTicks", bee.minTicksInHive());
            bees.add(beeTag);
         }

         this.getBlockEntityTag(tag, "beehive").put("Bees", bees);
      });
      this.register(StructuredDataKey.LOCK1_20_5, (data, tag) -> getBlockEntityTag(tag).put("Lock", data));
      this.register(
         StructuredDataKey.NOTE_BLOCK_SOUND, (data, tag) -> this.getBlockEntityTag(tag, "player_head").putString("note_block_sound", data.original())
      );
      this.register(StructuredDataKey.POT_DECORATIONS1_20_5, (data, tag) -> {
         IntArrayTag originalSherds = null;
         ListTag<StringTag> sherds = new ListTag<>(StringTag.class);

         for (int id : data.itemIds()) {
            String name = this.toMappedItemName(id);
            if (name.isEmpty()) {
               if (backupInconvertibleData && originalSherds == null) {
                  originalSherds = new IntArrayTag(data.itemIds());
               }
            } else {
               sherds.add(new StringTag(name));
            }
         }

         if (originalSherds != null) {
            getBackupTag(tag).put("pot_decorations", originalSherds);
         }

         this.getBlockEntityTag(tag, "decorated_pot").put("sherds", sherds);
      });
      this.register(StructuredDataKey.DEBUG_STICK_STATE, (data, tag) -> tag.put("DebugProperty", data.tag()));
      this.register(StructuredDataKey.RECIPES, (data, tag) -> tag.put("Recipes", data));
      this.register(StructuredDataKey.ENTITY_DATA1_20_5, (data, tag) -> tag.put("EntityTag", data));
      this.register(StructuredDataKey.BUCKET_ENTITY_DATA, (data, tag) -> {
         for (String mobTagName : BlockItemPacketRewriter1_20_5.MOB_TAGS) {
            if (data.contains(mobTagName)) {
               tag.put(mobTagName, data.get(mobTagName));
            }
         }
      });
      this.register(StructuredDataKey.BLOCK_ENTITY_DATA1_20_5, (data, tag) -> {
         CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
         if (blockEntityTag != null) {
            blockEntityTag.putAll(data);
         } else {
            tag.put("BlockEntityTag", data);
         }
      });
      this.register(StructuredDataKey.CONTAINER_LOOT, (data, tag) -> {
         Tag lootTable = data.get("loot_table");
         if (lootTable != null) {
            getBlockEntityTag(tag).put("LootTable", lootTable);
         }

         Tag lootTableSeed = data.get("loot_table_seed");
         if (lootTableSeed != null) {
            getBlockEntityTag(tag).put("LootTableSeed", lootTableSeed);
         }
      });
      this.register(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, (data, tag) -> {
         if (backupInconvertibleData) {
            getBackupTag(tag).putBoolean("enchantment_glint_override", data);
         }

         if (data) {
            ListTag<CompoundTag> enchantmentsTag = tag.getListTag("Enchantments", CompoundTag.class);
            if (enchantmentsTag == null) {
               enchantmentsTag = new ListTag<>(CompoundTag.class);
               tag.put("Enchantments", enchantmentsTag);
            } else if (!enchantmentsTag.isEmpty()) {
               return;
            }

            CompoundTag invalidEnchantment = new CompoundTag();
            invalidEnchantment.putString("id", "");
            enchantmentsTag.add(invalidEnchantment);
         }
      });
      this.register(StructuredDataKey.POTION_CONTENTS1_20_5, (data, tag) -> {
         if (data.potion() != null) {
            String potion = Potions1_20_5.idToKey(data.potion());
            if (potion != null) {
               tag.putString("Potion", potion);
            }
         }

         if (data.customColor() != null) {
            tag.putInt("CustomPotionColor", data.customColor());
         }

         ListTag<CompoundTag> customPotionEffectsTag = new ListTag<>(CompoundTag.class);

         for (PotionEffect effect : data.customEffects()) {
            CompoundTag effectTag = new CompoundTag();
            String id = PotionEffects1_20_5.idToKey(effect.effect());
            if (id != null) {
               effectTag.putString("id", id);
            }

            PotionEffectData details = effect.effectData();
            effectTag.putByte("amplifier", (byte)details.amplifier());
            effectTag.putInt("duration", details.duration());
            effectTag.putBoolean("ambient", details.ambient());
            effectTag.putBoolean("show_particles", details.showParticles());
            effectTag.putBoolean("show_icon", details.showIcon());
            customPotionEffectsTag.add(effectTag);
         }

         if (!customPotionEffectsTag.isEmpty()) {
            tag.put("custom_potion_effects", customPotionEffectsTag);
         }
      });
      this.register(StructuredDataKey.SUSPICIOUS_STEW_EFFECTS, (data, tag) -> {
         ListTag<CompoundTag> effectsTag = new ListTag<>(CompoundTag.class);

         for (SuspiciousStewEffect effect : data) {
            CompoundTag effectTag = new CompoundTag();
            String id = PotionEffects1_20_5.idToKey(effect.mobEffect());
            if (id != null) {
               effectTag.putString("id", id);
            }

            effectTag.putInt("duration", effect.duration());
            effectsTag.add(effectTag);
         }

         tag.put("effects", effectsTag);
      });
      this.register(StructuredDataKey.BANNER_PATTERNS, (connection, data, tag) -> {
         BannerPatternStorage patternStorage = connection.get(BannerPatternStorage.class);
         if (backupInconvertibleData && Arrays.stream(data).anyMatch(layerx -> {
            if (layerx.pattern().isDirect()) {
               return true;
            } else {
               int idx = layerx.pattern().id();
               String identifier = patternStorage != null ? patternStorage.pattern(idx) : BannerPatterns1_20_5.idToKey(idx);
               return identifier == null || identifier.equals("flow") || identifier.equals("guster");
            }
         })) {
            ListTag<CompoundTag> originalPatterns = new ListTag<>(CompoundTag.class);

            for (BannerPatternLayer layer : data) {
               CompoundTag layerTag = new CompoundTag();
               if (layer.pattern().isDirect()) {
                  CompoundTag patternTag = new CompoundTag();
                  BannerPattern pattern = layer.pattern().value();
                  patternTag.putString("asset_id", pattern.assetId());
                  patternTag.putString("translation_key", pattern.translationKey());
                  layerTag.put("pattern", patternTag);
               } else {
                  layerTag.putInt("pattern", layer.pattern().id());
               }

               layerTag.putInt("dye_color", layer.dyeColor());
               originalPatterns.add(layerTag);
            }

            getBackupTag(tag).put("banner_patterns", originalPatterns);
         }

         ListTag<CompoundTag> patternsTag = new ListTag<>(CompoundTag.class);

         for (BannerPatternLayer layer : data) {
            if (!layer.pattern().isDirect()) {
               int id = layer.pattern().id();
               String key = patternStorage != null ? patternStorage.pattern(id) : BannerPatterns1_20_5.idToKey(id);
               if (key != null) {
                  String compactKey = BannerPatterns1_20_5.fullIdToCompact(key);
                  if (compactKey != null) {
                     CompoundTag patternTag = new CompoundTag();
                     patternTag.putString("Pattern", compactKey);
                     patternTag.putInt("Color", layer.dyeColor());
                     patternsTag.add(patternTag);
                  }
               }
            }
         }

         this.getBlockEntityTag(tag, "banner").put("Patterns", patternsTag);
      });
      this.register(StructuredDataKey.V1_20_5.container, (connection, data, tag) -> this.convertItemList(connection, data, getBlockEntityTag(tag), "Items"));
      this.register(StructuredDataKey.CAN_PLACE_ON1_20_5, (data, tag) -> this.convertBlockPredicates(tag, data, "CanPlaceOn", 16));
      this.register(StructuredDataKey.CAN_BREAK1_20_5, (data, tag) -> this.convertBlockPredicates(tag, data, "CanDestroy", 8));
      this.register(StructuredDataKey.TRIM1_20_5, (connection, data, tag) -> {
         CompoundTag trimTag = new CompoundTag();
         ArmorTrimStorage trimStorage = connection.get(ArmorTrimStorage.class);
         if (data.material().isDirect()) {
            CompoundTag materialTag = new CompoundTag();
            ArmorTrimMaterial1_20_5 material = data.material().value();
            materialTag.putString("asset_name", material.assetName());
            String ingredientName = this.toMappedItemName(material.itemId());
            if (backupInconvertibleData && ingredientName.isEmpty()) {
               getBackupTag(materialTag).putInt("VV|Id", material.itemId());
            }

            materialTag.putString("ingredient", ingredientName);
            materialTag.put("item_model_index", new FloatTag(material.itemModelIndex()));
            CompoundTag overrideArmorMaterials = new CompoundTag();
            if (!material.overrideArmorMaterials().isEmpty()) {
               for (Entry<String, String> entry : material.overrideArmorMaterials().entrySet()) {
                  overrideArmorMaterials.put(entry.getKey(), new StringTag(entry.getValue()));
               }

               materialTag.put("override_armor_materials", overrideArmorMaterials);
            }

            materialTag.put("description", material.description());
            trimTag.put("material", materialTag);
         } else {
            String oldKey = trimStorage.trimMaterials().idToKey(data.material().id());
            if (oldKey != null) {
               trimTag.putString("material", oldKey);
            }
         }

         if (data.pattern().isDirect()) {
            CompoundTag patternTag = new CompoundTag();
            ArmorTrimPattern pattern = data.pattern().value();
            patternTag.putString("assetId", pattern.assetName());
            String itemName = this.toMappedItemName(pattern.itemId());
            if (backupInconvertibleData && itemName.isEmpty()) {
               getBackupTag(patternTag).putInt("VV|Id", pattern.itemId());
            }

            patternTag.putString("templateItem", itemName);
            patternTag.put("description", pattern.description());
            patternTag.putBoolean("decal", pattern.decal());
            trimTag.put("pattern", patternTag);
         } else {
            String oldKeyx = trimStorage.trimPatterns().idToKey(data.pattern().id());
            if (oldKeyx != null) {
               if (!"flow".equals(oldKeyx) && !"bolt".equals(oldKeyx)) {
                  trimTag.putString("pattern", oldKeyx);
               } else if (backupInconvertibleData) {
                  CompoundTag backupTrim = trimTag.copy();
                  if (!data.material().isDirect()) {
                     backupTrim.putInt("material", data.material().id());
                  }

                  backupTrim.putInt("pattern", data.pattern().id());
                  backupTrim.putBoolean("show_in_tooltip", data.showInTooltip());
                  getBackupTag(tag).put("trim", backupTrim);
               }
            }
         }

         if (trimTag.contains("material") && trimTag.contains("pattern")) {
            tag.put("Trim", trimTag);
         }

         if (!data.showInTooltip()) {
            this.putHideFlag(tag, 128);
         }
      });
      this.register(StructuredDataKey.BLOCK_STATE, (data, tag) -> {
         CompoundTag blockStateTag = new CompoundTag();
         tag.put("BlockStateTag", blockStateTag);

         for (Entry<String, String> entry : data.properties().entrySet()) {
            blockStateTag.putString(entry.getKey(), entry.getValue());
         }
      });
      this.register(StructuredDataKey.HIDE_TOOLTIP, (data, tag) -> {
         this.putHideFlag(tag, 255);
         if (backupInconvertibleData) {
            getBackupTag(tag).putBoolean("hide_tooltip", true);
         }
      });
      this.register(StructuredDataKey.INTANGIBLE_PROJECTILE, (data, tag) -> {
         if (backupInconvertibleData) {
            getBackupTag(tag).put("intangible_projectile", data);
         }
      });
      this.register(StructuredDataKey.MAX_STACK_SIZE, (data, tag) -> {
         if (backupInconvertibleData) {
            getBackupTag(tag).putInt("max_stack_size", data);
         }
      });
      this.register(StructuredDataKey.MAX_DAMAGE, (data, tag) -> {
         if (backupInconvertibleData) {
            getBackupTag(tag).putInt("max_damage", data);
         }
      });
      this.register(StructuredDataKey.RARITY, (data, tag) -> {
         if (backupInconvertibleData) {
            getBackupTag(tag).putInt("rarity", data);
         }
      });
      this.register(StructuredDataKey.FOOD1_20_5, (data, tag) -> {
         if (backupInconvertibleData) {
            CompoundTag backupTag = new CompoundTag();
            backupTag.putInt("nutrition", data.nutrition());
            backupTag.putFloat("saturation_modifier", data.saturationModifier());
            backupTag.putBoolean("can_always_eat", data.canAlwaysEat());
            backupTag.putFloat("eat_seconds", data.eatSeconds());
            ListTag<CompoundTag> possibleEffectsTag = new ListTag<>(CompoundTag.class);

            for (FoodProperties1_20_5.FoodEffect effect : data.possibleEffects()) {
               CompoundTag effectTag = new CompoundTag();
               PotionEffect potionEffect = effect.effect();
               CompoundTag potionEffectTag = new CompoundTag();
               potionEffectTag.putInt("effect", potionEffect.effect());
               potionEffectTag.put("effect_data", this.convertPotionEffectData(potionEffect.effectData()));
               effectTag.putFloat("probability", effect.probability());
               effectTag.put("effect", potionEffectTag);
               possibleEffectsTag.add(effectTag);
            }

            backupTag.put("possible_effects", possibleEffectsTag);
            getBackupTag(tag).put("food", backupTag);
         }
      });
      this.register(StructuredDataKey.FIRE_RESISTANT, (data, tag) -> {
         if (backupInconvertibleData) {
            getBackupTag(tag).putBoolean("fire_resistant", true);
         }
      });
      this.register(StructuredDataKey.TOOL1_20_5, (data, tag) -> {
         if (backupInconvertibleData) {
            CompoundTag backupTag = new CompoundTag();
            ListTag<CompoundTag> rulesTag = new ListTag<>(CompoundTag.class);

            for (ToolRule rule : data.rules()) {
               CompoundTag ruleTag = new CompoundTag();
               HolderSet set = rule.blocks();
               if (set.hasTagKey()) {
                  ruleTag.putString("blocks", set.tagKey());
               } else {
                  ruleTag.put("blocks", new IntArrayTag(set.ids()));
               }

               if (rule.speed() != null) {
                  ruleTag.putFloat("speed", rule.speed());
               }

               if (rule.correctForDrops() != null) {
                  ruleTag.putBoolean("correct_for_drops", rule.correctForDrops());
               }

               rulesTag.add(ruleTag);
            }

            backupTag.put("rules", rulesTag);
            backupTag.putFloat("default_mining_speed", data.defaultMiningSpeed());
            backupTag.putInt("damage_per_block", data.damagePerBlock());
            getBackupTag(tag).put("tool", backupTag);
         }
      });
      this.register(StructuredDataKey.OMINOUS_BOTTLE_AMPLIFIER, (data, tag) -> {
         if (backupInconvertibleData) {
            getBackupTag(tag).putInt("ominous_bottle_amplifier", data);
         }
      });
      this.register(StructuredDataKey.MAP_POST_PROCESSING, (data, tag) -> {});
      this.register(StructuredDataKey.CREATIVE_SLOT_LOCK, (data, tag) -> {});
   }

   private int unmappedItemId(int id) {
      return Protocol1_20_3To1_20_5.MAPPINGS.getOldItemId(id);
   }

   private String toMappedItemName(int id) {
      int mappedId = this.unmappedItemId(id);
      return mappedId != -1 ? Protocol1_20_3To1_20_5.MAPPINGS.getFullItemMappings().identifier(mappedId) : "";
   }

   private static CompoundTag getBlockEntityTag(CompoundTag tag) {
      return getOrCreate(tag, "BlockEntityTag");
   }

   private CompoundTag getBlockEntityTag(CompoundTag tag, String blockEntity) {
      CompoundTag blockEntityTag = getOrCreate(tag, "BlockEntityTag");
      if (!blockEntityTag.contains("id")) {
         blockEntityTag.putString("id", blockEntity);
      }

      return blockEntityTag;
   }

   private static CompoundTag getDisplayTag(CompoundTag tag) {
      return getOrCreate(tag, "display");
   }

   private static CompoundTag getBackupTag(CompoundTag tag) {
      return getOrCreate(tag, "VV|DataComponents");
   }

   private static CompoundTag getOrCreate(CompoundTag tag, String key) {
      CompoundTag subTag = tag.getCompoundTag(key);
      if (subTag == null) {
         subTag = new CompoundTag();
         tag.put(key, subTag);
      }

      return subTag;
   }

   @Nullable
   public static CompoundTag removeBackupTag(CompoundTag tag) {
      CompoundTag backupTag = tag.getCompoundTag("VV|DataComponents");
      if (backupTag != null) {
         tag.remove("VV|DataComponents");
      }

      return backupTag;
   }

   static int removeItemBackupTag(CompoundTag tag, int unmappedId) {
      IntTag itemBackupTag = tag.getIntTag("VV|Id");
      if (itemBackupTag != null) {
         tag.remove("VV|Id");
         return itemBackupTag.asInt();
      } else {
         return unmappedId;
      }
   }

   private void convertBlockPredicates(CompoundTag tag, AdventureModePredicate data, String key, int hideFlag) {
      ListTag<StringTag> predicatedListTag = new ListTag<>(StringTag.class);

      for (BlockPredicate predicate : data.predicates()) {
         HolderSet holders = predicate.holderSet();
         if (holders == null) {
            if (this.backupInconvertibleData) {
            }
         } else if (holders.hasTagKey()) {
            String tagKey = "#" + holders.tagKey();
            predicatedListTag.add(this.serializeBlockPredicate(predicate, tagKey));
         } else {
            for (int id : holders.ids()) {
               String name = Protocol1_20_3To1_20_5.MAPPINGS.blockName(id);
               if (name == null) {
                  if (this.backupInconvertibleData) {
                  }
               } else {
                  predicatedListTag.add(this.serializeBlockPredicate(predicate, name));
               }
            }
         }
      }

      tag.put(key, predicatedListTag);
      if (!data.showInTooltip()) {
         this.putHideFlag(tag, hideFlag);
      }
   }

   private StringTag serializeBlockPredicate(BlockPredicate predicate, String identifier) {
      StringBuilder builder = new StringBuilder(identifier);
      if (predicate.propertyMatchers() != null) {
         for (StatePropertyMatcher matcher : predicate.propertyMatchers()) {
            if (matcher.matcher().isLeft()) {
               builder.append(matcher.name()).append('=');
               builder.append(matcher.matcher().left());
            }
         }
      }

      if (predicate.tag() != null) {
         builder.append(predicate.tag());
      }

      return new StringTag(builder.toString());
   }

   private CompoundTag convertExplosion(FireworkExplosion explosion) {
      CompoundTag explosionTag = new CompoundTag();
      explosionTag.putInt("Type", explosion.shape());
      explosionTag.put("Colors", new IntArrayTag((int[])explosion.colors().clone()));
      explosionTag.put("FadeColors", new IntArrayTag((int[])explosion.fadeColors().clone()));
      explosionTag.putBoolean("Trail", explosion.hasTrail());
      explosionTag.putBoolean("Flicker", explosion.hasTwinkle());
      return explosionTag;
   }

   private CompoundTag convertPotionEffectData(PotionEffectData data) {
      CompoundTag effectDataTag = new CompoundTag();
      effectDataTag.putInt("amplifier", data.amplifier());
      effectDataTag.putInt("duration", data.duration());
      effectDataTag.putBoolean("ambient", data.ambient());
      effectDataTag.putBoolean("show_particles", data.showParticles());
      effectDataTag.putBoolean("show_icon", data.showIcon());
      if (data.hiddenEffect() != null) {
         effectDataTag.put("hidden_effect", this.convertPotionEffectData(data.hiddenEffect()));
      }

      return effectDataTag;
   }

   private void convertItemList(UserConnection connection, Item[] items, CompoundTag tag, String key) {
      ListTag<CompoundTag> itemsTag = new ListTag<>(CompoundTag.class);

      for (int i = 0; i < items.length; i++) {
         Item item = items[i];
         CompoundTag savedItem = this.itemToTag(connection, item);
         if (this.backupInconvertibleData) {
            savedItem.putByte("Slot", (byte)i);
         }

         itemsTag.add(savedItem);
      }

      tag.put(key, itemsTag);
   }

   private CompoundTag itemToTag(UserConnection connection, Item item) {
      CompoundTag savedItem = new CompoundTag();
      if (item != null) {
         String name = this.toMappedItemName(item.identifier());
         savedItem.putString("id", name);
         if (this.backupInconvertibleData && name.isEmpty()) {
            savedItem.putInt("VV|Id", item.identifier());
         }

         savedItem.putByte("Count", (byte)item.amount());
         CompoundTag itemTag = new CompoundTag();

         for (StructuredData<?> data : item.dataContainer().data().values()) {
            this.writeToTag(connection, data, itemTag);
         }

         savedItem.put("tag", itemTag);
      } else {
         savedItem.putString("id", "air");
      }

      return savedItem;
   }

   private void convertEnchantments(Enchantments data, CompoundTag tag, boolean storedEnchantments) {
      ListTag<CompoundTag> enchantments = new ListTag<>(CompoundTag.class);

      for (Int2IntMap.Entry entry : data.enchantments().int2IntEntrySet()) {
         int enchantmentId = entry.getIntKey();
         String identifier = Enchantments1_20_5.idToKey(enchantmentId);
         if (identifier != null && !identifier.equals("density") && !identifier.equals("breach") && !identifier.equals("wind_burst")) {
            if (identifier.equals("sweeping_edge")) {
               identifier = "sweeping";
            }

            CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", identifier);
            enchantment.putShort("lvl", (short)entry.getIntValue());
            enchantments.add(enchantment);
         }
      }

      tag.put(storedEnchantments ? "StoredEnchantments" : "Enchantments", enchantments);
      if (!data.showInTooltip()) {
         this.putHideFlag(tag, storedEnchantments ? 32 : 1);
      }
   }

   private void putHideFlag(CompoundTag tag, int value) {
      tag.putInt("HideFlags", tag.getInt("HideFlags") | value);
   }

   public <T> void writeToTag(UserConnection connection, StructuredData<T> data, CompoundTag tag) {
      if (!data.isEmpty()) {
         StructuredDataConverter.DataConverter<T> converter = (StructuredDataConverter.DataConverter<T>)this.rewriters.get(data.key());
         Preconditions.checkNotNull(converter, "No converter for %s found", new Object[]{data.key()});
         converter.convert(connection, data.value(), tag);
      }
   }

   private <T> void register(StructuredDataKey<T> key, StructuredDataConverter.DataConverter<T> converter) {
      this.rewriters.put(key, converter);
   }

   private <T> void register(StructuredDataKey<T> key, StructuredDataConverter.SimpleDataConverter<T> converter) {
      StructuredDataConverter.DataConverter<T> c = (connection, data, tag) -> converter.convert(data, tag);
      this.rewriters.put(key, c);
   }

   public boolean backupInconvertibleData() {
      return this.backupInconvertibleData;
   }

   @FunctionalInterface
   interface DataConverter<T> {
      void convert(UserConnection var1, T var2, CompoundTag var3);
   }

   @FunctionalInterface
   interface SimpleDataConverter<T> {
      void convert(T var1, CompoundTag var2);
   }
}
