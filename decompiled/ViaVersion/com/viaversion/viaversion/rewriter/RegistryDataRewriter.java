package com.viaversion.viaversion.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.entity.DimensionData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.RegistryEntry;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.DimensionDataImpl;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectArrayMap;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import com.viaversion.viaversion.util.TagUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RegistryDataRewriter implements com.viaversion.viaversion.api.rewriter.RegistryDataRewriter {
   private final Map<String, RegistryDataRewriter.TagHandler<?>> registryEntryHandlers = new Object2ObjectArrayMap<>();
   private final Map<String, Consumer<CompoundTag>> enchantmentEffectHandlers = new Object2ObjectArrayMap<>();
   private final Set<String> enchantmentEffectsToRemove = new HashSet<>();
   private final Map<String, List<RegistryEntry>> toAdd = new Object2ObjectArrayMap<>();
   private final Set<String> toRemove = new HashSet<>();
   protected final Protocol<?, ?, ?, ?> protocol;

   public RegistryDataRewriter(Protocol<?, ?, ?, ?> protocol) {
      this.protocol = protocol;
   }

   @Override
   public void handle(PacketWrapper wrapper) {
      String registryKey = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
      RegistryEntry[] entries = wrapper.read(Types.REGISTRY_ENTRY_ARRAY);
      entries = this.handle(wrapper.user(), registryKey, entries);
      wrapper.write(Types.REGISTRY_ENTRY_ARRAY, entries);
      if (this.toRemove.contains(registryKey)) {
         wrapper.cancel();
      }
   }

   public RegistryEntry[] handle(UserConnection connection, String key, RegistryEntry[] entries) {
      key = Key.stripMinecraftNamespace(key);
      String[] keys = new String[entries.length];

      for (int i = 0; i < entries.length; i++) {
         keys[i] = Key.stripMinecraftNamespace(entries[i].key());
      }

      this.protocol.getEntityRewriter().<EntityTracker>tracker(connection).addRegistryKeys(key, new KeyMappings(keys));
      switch (key) {
         case "enchantment":
            this.updateEnchantments(connection, entries);
            break;
         case "trim_material":
            this.updateTrimMaterials(connection, entries);
            break;
         case "jukebox_song":
            this.updateJukeboxSongs(connection, entries);
            break;
         case "trim_pattern":
         case "instrument":
            this.updateTextComponents(connection, entries, "description");
            break;
         case "painting_variant":
            this.updateTextComponents(connection, entries, "title", "author");
            break;
         case "worldgen/biome":
            this.updateBiomes(entries);
            break;
         case "worldgen/block_state_provider":
            this.updateBlockStateProviders(entries);
            break;
         case "dimension_type":
            this.updateDimensionTypes(entries);
            break;
         case "dialog":
            this.updateDialogs(connection, entries);
      }

      RegistryDataRewriter.TagHandler<?> registryEntryHandler = this.registryEntryHandlers.get(key);
      if (registryEntryHandler != null) {
         for (RegistryEntry entry : entries) {
            if (entry.tag() != null) {
               this.callTagHandler(registryEntryHandler, entry.key(), entry.tag());
            }
         }
      }

      List<RegistryEntry> toAdd = this.toAdd.get(key);
      if (toAdd != null) {
         Set<String> existingKeys = new HashSet<>();
         RegistryEntry[] updatedEntries = new RegistryEntry[entries.length + toAdd.size()];
         int index = 0;

         for (RegistryEntry entryx : entries) {
            updatedEntries[index++] = entryx;
            existingKeys.add(Key.stripMinecraftNamespace(entryx.key()));
         }

         for (RegistryEntry entryx : toAdd) {
            if (!existingKeys.contains(Key.stripMinecraftNamespace(entryx.key()))) {
               updatedEntries[index++] = entryx.copy();
            }
         }

         if (index < updatedEntries.length) {
            entries = Arrays.copyOf(updatedEntries, index);
         } else {
            entries = updatedEntries;
         }
      }

      this.trackDimensionAndBiomes(connection, key, entries);
      return entries;
   }

   public void addEntries(String registryKey, RegistryEntry... entries) {
      this.toAdd.computeIfAbsent(Key.stripMinecraftNamespace(registryKey), $ -> new ArrayList<>()).addAll(List.of(entries));
   }

   public void remove(String registryKey) {
      this.toRemove.add(Key.stripMinecraftNamespace(registryKey));
   }

   public void addHandler(String registryKey, RegistryDataRewriter.TagHandler<CompoundTag> handler) {
      this.registryEntryHandlers.put(Key.stripMinecraftNamespace(registryKey), handler);
   }

   public <T extends Tag> void addTagHandler(String registryKey, RegistryDataRewriter.TagHandler<T> handler) {
      this.registryEntryHandlers.put(Key.stripMinecraftNamespace(registryKey), handler);
   }

   private <T extends Tag> void callTagHandler(RegistryDataRewriter.TagHandler<T> handler, String key, Tag tag) {
      handler.accept(key, (T)tag);
   }

   @Override
   public void sendMissingRegistries(UserConnection connection) {
      EntityTracker entityTracker = this.protocol.getEntityRewriter().tracker(connection);

      for (Entry<String, List<RegistryEntry>> entry : this.toAdd.entrySet()) {
         if (entityTracker.registryKeys(entry.getKey()) == null) {
            List<RegistryEntry> toAdd = entry.getValue();
            RegistryEntry[] entries = new RegistryEntry[toAdd.size()];

            for (int i = 0; i < toAdd.size(); i++) {
               entries[i] = toAdd.get(i).copy();
            }

            ClientboundPacketType packetType = this.protocol.getPacketTypesProvider().mappedClientboundType(State.CONFIGURATION, "REGISTRY_DATA");
            PacketWrapper registryData = PacketWrapper.create(packetType, connection);
            registryData.write(Types.STRING, entry.getKey());
            registryData.write(Types.REGISTRY_ENTRY_ARRAY, entries);
            registryData.send((Class<? extends Protocol>)this.protocol.getClass());
         }
      }
   }

   public void addEnchantmentEffectRewriter(String key, Consumer<CompoundTag> rewriter) {
      this.enchantmentEffectHandlers.put(Key.stripMinecraftNamespace(key), rewriter);
   }

   public void removeEnchantmentEffects(String... types) {
      for (String type : types) {
         this.enchantmentEffectsToRemove.add(Key.stripMinecraftNamespace(type));
      }
   }

   public void trackDimensionAndBiomes(UserConnection connection, String registryKey, RegistryEntry[] entries) {
      if (registryKey.equals("worldgen/biome")) {
         this.protocol.getEntityRewriter().<EntityTracker>tracker(connection).setBiomesSent(entries.length);
      } else if (registryKey.equals("dimension_type")) {
         Map<String, DimensionData> dimensionDataMap = new HashMap<>(entries.length);

         for (int i = 0; i < entries.length; i++) {
            RegistryEntry entry = entries[i];
            String key = Key.stripMinecraftNamespace(entry.key());
            DimensionData dimensionData = (DimensionData)(entry.tag() != null
               ? new DimensionDataImpl(i, (CompoundTag)entry.tag())
               : DimensionDataImpl.withDefaultsFor(key, i));
            dimensionDataMap.put(key, dimensionData);
         }

         this.protocol.getEntityRewriter().<EntityTracker>tracker(connection).setDimensions(dimensionDataMap);
      }
   }

   public void updateDialogs(UserConnection connection, RegistryEntry[] entries) {
      if (this.protocol.getMappingData() == null || this.protocol.getMappingData().getFullItemMappings() != null) {
         for (RegistryEntry entry : entries) {
            if (entry.tag() != null) {
               this.updateDialog(connection, (CompoundTag)entry.tag());
            }
         }
      }
   }

   @Override
   public void updateDialog(UserConnection connection, CompoundTag tag) {
      ComponentRewriter componentRewriter = this.protocol.getComponentRewriter();
      if (componentRewriter != null) {
         componentRewriter.processTag(connection, tag.get("title"));
         componentRewriter.processTag(connection, tag.get("external_title"));
      }

      ListTag<CompoundTag> inputsTag = tag.getListTag("inputs", CompoundTag.class);
      if (inputsTag != null && componentRewriter != null) {
         for (CompoundTag input : inputsTag) {
            this.updateTextComponent(connection, input, "label");
            String type = input.getString("type");
            if (Key.equals(type, "single_option")) {
               ListTag<CompoundTag> optionsTag = input.getListTag("options", CompoundTag.class);
               if (optionsTag != null) {
                  optionsTag.forEach(option -> this.updateTextComponent(connection, option, "display"));
               }
            }
         }
      }

      String type = tag.getString("type");
      String var11 = Key.stripMinecraftNamespace(type);
      switch (var11) {
         case "confirmation":
            this.updateDialogAction(connection, tag.getCompoundTag("yes"));
            this.updateDialogAction(connection, tag.getCompoundTag("no"));
            break;
         case "dialog_list":
            this.updateDialogAction(connection, tag.getCompoundTag("exit_action"));
            ListTag<CompoundTag> dialogsTag = tag.getListTag("dialogs", CompoundTag.class);
            if (dialogsTag != null) {
               dialogsTag.forEach(dialog -> this.updateDialog(connection, dialog));
            } else {
               CompoundTag dialogTag = tag.getCompoundTag("dialogs");
               if (dialogTag != null) {
                  this.updateDialog(connection, dialogTag);
               }
            }
            break;
         case "multi_action":
            this.updateDialogAction(connection, tag.getCompoundTag("exit_action"));
            ListTag<CompoundTag> actionsTag = tag.getListTag("actions", CompoundTag.class);
            if (actionsTag != null) {
               actionsTag.forEach(action -> this.updateDialogAction(connection, action));
            }
            break;
         case "notice":
            this.updateDialogAction(connection, tag.getCompoundTag("action"));
            break;
         case "server_links":
            this.updateDialogAction(connection, tag.getCompoundTag("exit_action"));
      }

      ListTag<CompoundTag> bodiesTag = tag.getListTag("body", CompoundTag.class);
      if (bodiesTag != null) {
         bodiesTag.forEach(body -> this.updateDialogBody(connection, body));
      } else {
         CompoundTag bodyTag = tag.getCompoundTag("body");
         if (bodyTag != null) {
            this.updateDialogBody(connection, bodyTag);
         }
      }
   }

   public void updateDialogAction(UserConnection connection, CompoundTag tag) {
      this.updateTextComponent(connection, tag, "label");
      this.updateTextComponent(connection, tag, "tooltip");
   }

   protected void updateTextComponents(UserConnection connection, RegistryEntry[] entries, String... keys) {
      for (RegistryEntry entry : entries) {
         if (entry.tag() instanceof CompoundTag tag) {
            for (String key : keys) {
               this.updateTextComponent(connection, tag, key);
            }
         }
      }
   }

   protected void updateTextComponent(UserConnection connection, @Nullable CompoundTag tag, String key) {
      if (tag != null && this.protocol.getComponentRewriter() != null) {
         this.protocol.getComponentRewriter().processTag(connection, tag.get(key));
      }
   }

   public void updateDialogBody(UserConnection connection, CompoundTag tag) {
      String type = tag.getString("type");
      ComponentRewriter componentRewriter = this.protocol.getComponentRewriter();
      if (Key.equals(type, "plain_message")) {
         if (componentRewriter != null) {
            componentRewriter.processTag(connection, tag.get("contents"));
         }
      } else if (Key.equals(type, "item")) {
         if (componentRewriter != null) {
            Tag description = tag.get("description");
            componentRewriter.processTag(connection, description);
            if (description instanceof CompoundTag descriptionTag) {
               componentRewriter.processTag(connection, descriptionTag.get("contents"));
            }
         }

         StringTag itemIdTag = tag.getStringTag("item");
         if (itemIdTag != null) {
            String mappedId = this.protocol.getMappingData().getFullItemMappings().mappedIdentifier(itemIdTag.getValue());
            if (mappedId != null) {
               itemIdTag.setValue(mappedId);
            }
         } else if (componentRewriter != null) {
            componentRewriter.handleShowItem(connection, tag.getCompoundTag("item"));
         }
      }
   }

   public void updateEnchantments(UserConnection connection, RegistryEntry[] entries) {
      for (RegistryEntry entry : entries) {
         if (entry.tag() != null) {
            CompoundTag tag = (CompoundTag)entry.tag();
            this.updateTextComponent(connection, tag, "description");
            if (!Mappings.isFullIdentity(this.protocol.getMappingData().getFullItemMappings())) {
               this.updateItemList(tag.getListTag("supported_items", StringTag.class));
               this.updateItemList(tag.getListTag("primary_items", StringTag.class));
            }

            CompoundTag effects = tag.getCompoundTag("effects");
            if (effects != null) {
               for (Entry<String, Tag> effectEntry : effects.entrySet()) {
                  if (effectEntry.getValue() instanceof CompoundTag compoundTag) {
                     this.updateNestedEffect(compoundTag);
                  } else if (effectEntry.getValue() instanceof ListTag<?> listTag && listTag.getElementType() == CompoundTag.class) {
                     for (Tag effectTag : listTag) {
                        this.updateNestedEffect((CompoundTag)effectTag);
                     }
                  }
               }

               MappingData mappingData = this.protocol.getMappingData();
               if (mappingData != null && mappingData.getAttributeMappings() != null) {
                  this.updateAttributesFields(effects);
               }
            }
         }
      }
   }

   public void updateTrimMaterials(UserConnection connection, RegistryEntry[] entries) {
      this.updateTextComponents(connection, entries, "description");
      if (!Mappings.isFullIdentity(this.protocol.getMappingData().getFullItemMappings())) {
         for (RegistryEntry entry : entries) {
            if (entry.tag() != null) {
               StringTag ingredientTag = ((CompoundTag)entry.tag()).getStringTag("ingredient");
               if (ingredientTag == null) {
                  return;
               }

               this.updateItem(ingredientTag);
            }
         }
      }
   }

   public void updateJukeboxSongs(UserConnection connection, RegistryEntry[] entries) {
      this.updateTextComponents(connection, entries, "description");
   }

   public void updateDimensionTypes(RegistryEntry[] entries) {
      for (RegistryEntry entry : entries) {
         if (entry.tag() != null) {
            CompoundTag attributes = ((CompoundTag)entry.tag()).getCompoundTag("attributes");
            if (attributes != null) {
               this.updateEnvironmentAttributes(attributes);
            }
         }
      }
   }

   public void updateBiomes(RegistryEntry[] entries) {
      for (RegistryEntry entry : entries) {
         if (entry.tag() != null) {
            CompoundTag tag = (CompoundTag)entry.tag();
            CompoundTag attributes = tag.getCompoundTag("attributes");
            if (attributes != null) {
               this.updateEnvironmentAttributes(attributes);
            }

            CompoundTag effects = tag.getCompoundTag("effects");
            if (effects != null) {
               CompoundTag particle = effects.getCompoundTag("particle");
               if (particle != null) {
                  this.handleParticleData(particle.getCompoundTag("options"));
               }
            }
         }
      }
   }

   protected void updateEnvironmentAttributes(CompoundTag tag) {
      MappingData mappings = this.protocol.getMappingData();
      if (mappings != null) {
         if (mappings.changedEnvironmentAttributes() != null) {
            for (String attribute : mappings.changedEnvironmentAttributes()) {
               TagUtil.removeNamespaced(tag, attribute);
            }
         }

         Tag ambientParticles = TagUtil.getNamespacedTag(tag, "visual/ambient_particles");
         if (ambientParticles instanceof CompoundTag modifierTag) {
            ambientParticles = modifierTag.get("argument");
         }

         if (ambientParticles instanceof ListTag) {
            for (Tag ambientParticle : (ListTag)ambientParticles) {
               if (ambientParticle instanceof CompoundTag ambientParticleTag) {
                  CompoundTag particle = ambientParticleTag.getCompoundTag("particle");
                  if (particle != null) {
                     this.handleParticleData(particle);
                  }
               }
            }
         }

         Tag dripstoneParticle = TagUtil.getNamespacedTag(tag, "visual/default_dripstone_particle");
         if (dripstoneParticle instanceof CompoundTag modifierTag && modifierTag.contains("argument")) {
            dripstoneParticle = modifierTag.get("argument");
         }

         if (dripstoneParticle instanceof CompoundTag particle) {
            this.handleParticleData(particle);
         }
      }
   }

   private void updateNestedEffect(CompoundTag effectsTag) {
      CompoundTag effect = effectsTag.getCompoundTag("effect");
      if (effect != null) {
         this.runEffectRewriters(effect);
         ListTag<CompoundTag> innerEffects = effect.getListTag("effects", CompoundTag.class);
         if (innerEffects != null) {
            for (CompoundTag innerEffect : innerEffects) {
               this.runEffectRewriters(innerEffect);
            }
         }
      }

      CompoundTag requirements = effectsTag.getCompoundTag("requirements");
      if (requirements != null) {
         this.updateEnchantmentTerm(requirements);
      }
   }

   public void updateEnchantmentTerm(CompoundTag term) {
      String condition = term.getString("condition");
      if (condition == null) {
         condition = term.getString("type");
      }

      if (Key.equals(condition, "inverted")) {
         CompoundTag childTerm = term.getCompoundTag("term");
         if (childTerm != null) {
            this.updateEnchantmentTerm(childTerm);
         }
      } else if (Key.equals(condition, "entity_properties")) {
         CompoundTag predicate = term.getCompoundTag("predicate");
         if (predicate != null) {
            this.updateType(predicate, "type", this.protocol.getMappingData().getEntityMappings());
            this.updateType(predicate, "entity_type", this.protocol.getMappingData().getEntityMappings());
         }
      } else if (Key.equals(condition, "block_state_property")) {
         this.updateType(term, "block", this.protocol.getMappingData().getFullBlockMappings());
      } else if (Key.equals(condition, "match_block")) {
         this.replaceWithDummyCondition(term);
      } else {
         ListTag<CompoundTag> terms = term.getListTag("terms", CompoundTag.class);
         if (terms != null) {
            for (CompoundTag childTerm : terms) {
               this.updateEnchantmentTerm(childTerm);
            }
         }
      }
   }

   private void updateAttributesFields(CompoundTag effects) {
      ListTag<CompoundTag> attributesList = TagUtil.getNamespacedCompoundTagList(effects, "attributes");
      if (attributesList != null) {
         for (CompoundTag attributeData : attributesList) {
            this.updateType(attributeData, "attribute", this.protocol.getMappingData().getAttributeMappings());
         }
      }
   }

   protected void handleParticleData(CompoundTag particleData) {
      this.updateType(particleData, "type", this.protocol.getMappingData().getParticleMappings());
      Tag blockState = particleData.get("block_state");
      if (blockState != null) {
         this.updateBlockState(blockState);
      }
   }

   private void runEffectRewriters(CompoundTag effectTag) {
      String effect = effectTag.getString("type");
      if (effect != null) {
         effect = Key.stripMinecraftNamespace(effect);
         if (this.enchantmentEffectsToRemove.contains(effect)) {
            this.replaceWithDummyCondition(effectTag);
         } else {
            if (effect.equals("attribute")) {
               this.updateType(effectTag, "attribute", this.protocol.getMappingData().getAttributeMappings());
            } else if (effect.equals("spawn_particles")) {
               CompoundTag particleData = effectTag.getCompoundTag("particle");
               if (particleData != null) {
                  this.handleParticleData(particleData);
               }
            } else if (effect.equals("replace_disk") || effect.equals("replace_block")) {
               this.updateBlockStateProvider(effectTag.getCompoundTag("block_state"));
            }

            Consumer<CompoundTag> rewriter = this.enchantmentEffectHandlers.get(effect);
            if (rewriter != null) {
               rewriter.accept(effectTag);
            } else if (effect.equals("play_sound")) {
               this.updateType(effectTag, "sound", this.protocol.getMappingData().getFullSoundMappings());
            }
         }
      }
   }

   public void updateBlockStateProviders(RegistryEntry[] entries) {
      for (RegistryEntry entry : entries) {
         if (entry.tag() != null) {
            this.updateBlockStateProvider((CompoundTag)entry.tag());
         }
      }
   }

   @Override
   public boolean updateBlockStateProvider(CompoundTag tag) {
      boolean changed = false;
      String type = tag.getString("type");
      if (type == null) {
         return this.updateBlockState(tag);
      } else {
         type = Key.stripMinecraftNamespace(type);
         switch (type) {
            case "simple":
            case "rotated":
               changed |= this.updateBlockState(tag.get("state"));
               break;
            case "weighted":
               for (CompoundTag entry : tag.getListTag("entries", CompoundTag.class)) {
                  changed |= this.updateBlockState(entry.get("data"));
               }
               break;
            case "noise_threshold":
               changed |= this.updateBlockState(tag.get("default_state"));
               changed |= this.updateBlockStates(tag.getListTag("low_states"));
               changed |= this.updateBlockStates(tag.getListTag("high_states"));
               break;
            case "noise":
            case "dual_noise":
               changed |= this.updateBlockStates(tag.getListTag("states"));
               break;
            case "randomized_int_state":
               changed |= this.updateBlockStateProvider(tag.getCompoundTag("source"));
               break;
            case "rule_based_state":
               CompoundTag fallback = tag.getCompoundTag("fallback");
               if (fallback != null) {
                  changed |= this.updateBlockStateProvider(fallback);
               }

               tag.put("rules", new ListTag<>(CompoundTag.class));
               changed = true;
               break;
            case "copy_properties":
               changed |= this.updateBlockStateProvider(tag.getCompoundTag("source_block_state_provider"));
         }

         return changed;
      }
   }

   private boolean updateBlockStates(@Nullable ListTag<?> statesTag) {
      if (statesTag == null) {
         return false;
      } else {
         boolean changed = false;

         for (Tag stateTag : statesTag) {
            changed |= this.updateBlockState(stateTag);
         }

         return changed;
      }
   }

   protected boolean updateBlockState(Tag blockStateTag) {
      FullMappings blockMappings = this.protocol.getMappingData().getFullBlockMappings();
      if (blockMappings == null) {
         return false;
      } else {
         if (blockStateTag instanceof CompoundTag compoundTag) {
            String block = compoundTag.getString("id");
            if (block == null) {
               return false;
            }

            int blockId = blockMappings.id(block);
            if (blockId == -1 || this.protocol.getMappingData().hasBlockChanged(blockId)) {
               compoundTag.putString("id", "minecraft:dirt");
               compoundTag.remove("properties");
               return true;
            }
         } else if (blockStateTag instanceof StringTag stringTag) {
            int blockId = blockMappings.id(stringTag.getValue());
            if (blockId == -1 || this.protocol.getMappingData().hasBlockChanged(blockId)) {
               stringTag.setValue("minecraft:dirt");
               return true;
            }
         }

         return false;
      }
   }

   private void replaceWithDummyCondition(CompoundTag tag) {
      tag.clear();
      tag.putString("type", "minecraft:all_of");
      tag.put("effects", new ListTag<>(CompoundTag.class));
   }

   protected void updateType(CompoundTag tag, String key, FullMappings mappings) {
      Tag typeTag = tag.get(key);
      if (typeTag != null && !Mappings.isFullIdentity(mappings)) {
         if (typeTag instanceof StringTag stringTag) {
            this.setMappedOrDummyId(mappings, stringTag);
         } else if (typeTag instanceof ListTag<?> listTag && listTag.getElementType() == StringTag.class) {
            for (StringTag entry : listTag) {
               this.setMappedOrDummyId(mappings, entry);
            }
         }
      }
   }

   private void setMappedOrDummyId(FullMappings mappings, StringTag tag) {
      if (!Key.stripMinecraftNamespace(tag.getValue()).startsWith("#")) {
         String mappedType = mappings.mappedIdentifier(tag.getValue());
         if (mappedType == null) {
            mappedType = mappings.mappedIdentifier(0);
         }

         tag.setValue(mappedType);
      }
   }

   private void updateItemList(ListTag<StringTag> listTag) {
      if (listTag != null) {
         for (StringTag tag : listTag) {
            this.updateItem(tag);
         }
      }
   }

   private void updateItem(StringTag tag) {
      String mapped = this.protocol.getMappingData().getFullItemMappings().mappedIdentifier(tag.getValue());
      if (mapped != null) {
         tag.setValue(mapped);
      }
   }

   public RegistryEntry[] entriesFromTag(CompoundTag tag) {
      RegistryEntry[] entries = new RegistryEntry[tag.size()];
      int index = 0;

      for (Entry<String, Tag> entry : tag.entrySet()) {
         entries[index++] = new RegistryEntry(entry.getKey(), entry.getValue());
      }

      return entries;
   }

   @Override
   public boolean shouldRemoveRegistry(String registryKey) {
      return this.toRemove.contains(Key.stripMinecraftNamespace(registryKey));
   }

   @Override
   public boolean hasRegistriesToRemove() {
      return !this.toRemove.isEmpty();
   }

   @FunctionalInterface
   public interface TagHandler<T extends Tag> extends BiConsumer<String, T> {
   }
}
