package com.viaversion.viaversion.api.data;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingDataBase implements MappingData {
   protected final String unmappedVersion;
   protected final String mappedVersion;
   protected FullMappings argumentTypeMappings;
   protected FullMappings entityMappings;
   protected FullMappings recipeSerializerMappings;
   protected FullMappings itemDataSerializerMappings;
   protected FullMappings slotDisplayMappings;
   protected FullMappings attributeMappings;
   protected FullMappings blockEntityMappings;
   protected ParticleMappings particleMappings;
   protected BiMappings itemMappings;
   protected BiMappings blockMappings;
   protected Mappings blockStateMappings;
   protected Mappings soundMappings;
   protected Mappings statisticsMappings;
   protected Mappings enchantmentMappings;
   protected Mappings paintingMappings;
   protected Mappings menuMappings;
   protected Map<RegistryType, List<TagData>> tags;
   protected IntSet changedBlocks;
   protected Set<String> changedEnvironmentAttributes;

   public MappingDataBase(String unmappedVersion, String mappedVersion) {
      this.unmappedVersion = unmappedVersion;
      this.mappedVersion = mappedVersion;
   }

   @Override
   public void load() {
      if (Via.getManager().isDebug()) {
         this.getLogger().info("Loading " + this.unmappedVersion + " -> " + this.mappedVersion + " mappings...");
      }

      CompoundTag data = this.readMappingsFile("mappings-" + this.unmappedVersion + "to" + this.mappedVersion + ".nbt");
      this.blockStateMappings = this.loadMappings(data, "blockstates");
      this.statisticsMappings = this.loadMappings(data, "statistics");
      this.menuMappings = this.loadMappings(data, "menus");
      this.enchantmentMappings = this.loadMappings(data, "enchantments");
      this.paintingMappings = this.loadMappings(data, "paintings");
      CompoundTag unmappedIdentifierData = this.readUnmappedIdentifiersFile("identifiers-" + this.unmappedVersion + ".nbt");
      CompoundTag mappedIdentifierData = this.readMappedIdentifiersFile("identifiers-" + this.mappedVersion + ".nbt");
      if (unmappedIdentifierData != null && mappedIdentifierData != null) {
         this.itemMappings = this.loadFullOrBiMappings(data, unmappedIdentifierData, mappedIdentifierData, "items");
         this.blockMappings = this.loadFullOrBiMappings(data, unmappedIdentifierData, mappedIdentifierData, "blocks");
         this.soundMappings = this.loadFullOrBiMappings(data, unmappedIdentifierData, mappedIdentifierData, "sounds");
         this.entityMappings = this.loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "entities");
         this.argumentTypeMappings = this.loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "argumenttypes");
         this.slotDisplayMappings = this.loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "slot_displays");
         this.recipeSerializerMappings = this.loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "recipe_serializers");
         this.itemDataSerializerMappings = this.loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "data_component_type");
         this.attributeMappings = this.loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "attributes");
         this.blockEntityMappings = this.loadFullMappings(data, unmappedIdentifierData, mappedIdentifierData, "blockentities");
         MappingDataLoader.IdentifiersPair particleIdentifiers = this.identifiersFromGlobalIds(unmappedIdentifierData, mappedIdentifierData, "particles");
         if (particleIdentifiers != null) {
            Mappings particleMappings = this.loadMappings(data, "particles");
            if (particleMappings == null) {
               particleMappings = new IdentityMappings(particleIdentifiers.unmapped().size(), particleIdentifiers.mapped().size());
            }

            this.particleMappings = new ParticleMappings(particleIdentifiers, particleMappings);
         }
      } else {
         this.itemMappings = this.loadBiMappings(data, "items");
         this.blockMappings = this.loadBiMappings(data, "blocks");
         this.soundMappings = this.loadMappings(data, "sounds");
      }

      CompoundTag tagsTag = data.getCompoundTag("tags");
      if (tagsTag != null) {
         this.tags = new EnumMap<>(RegistryType.class);
         this.loadTags(RegistryType.ITEM, tagsTag);
         this.loadTags(RegistryType.BLOCK, tagsTag);
         this.loadTags(RegistryType.ENTITY, tagsTag);
         this.loadTags(RegistryType.FLUID, tagsTag);
      }

      IntArrayTag changedBlocks = data.getIntArrayTag("changed_blocks");
      if (changedBlocks != null) {
         this.changedBlocks = new IntOpenHashSet(changedBlocks.getValue());
      }

      ListTag<StringTag> changedEnvironmentAttributes = data.getListTag("changed_environment_attributes", StringTag.class);
      if (changedEnvironmentAttributes != null) {
         this.changedEnvironmentAttributes = changedEnvironmentAttributes.stream().map(StringTag::getValue).collect(Collectors.toSet());
      }

      this.loadExtras(data);
   }

   @Nullable
   protected List<String> identifiersFromGlobalIds(CompoundTag mappingsTag, String key) {
      return MappingDataLoader.INSTANCE.identifiersFromGlobalIds(mappingsTag, key);
   }

   @Nullable
   protected MappingDataLoader.IdentifiersPair identifiersFromGlobalIds(CompoundTag unmappedTag, CompoundTag mappedTag, String key) {
      return MappingDataLoader.INSTANCE.identifiersFromGlobalIds(unmappedTag, mappedTag, key);
   }

   @Nullable
   protected CompoundTag readMappingsFile(String name) {
      return MappingDataLoader.INSTANCE.loadNBT(name);
   }

   @Nullable
   protected CompoundTag readUnmappedIdentifiersFile(String name) {
      return MappingDataLoader.INSTANCE.loadNBT(name, true);
   }

   @Nullable
   protected CompoundTag readMappedIdentifiersFile(String name) {
      return MappingDataLoader.INSTANCE.loadNBT(name, true);
   }

   @Nullable
   protected Mappings loadMappings(CompoundTag data, String key) {
      return MappingDataLoader.INSTANCE.loadMappings(data, key);
   }

   @Nullable
   protected BiMappings loadFullOrBiMappings(CompoundTag data, CompoundTag unmappedIdentifiersTag, CompoundTag mappedIdentifiersTag, String key) {
      FullMappings mappings = this.loadFullMappings(data, unmappedIdentifiersTag, mappedIdentifiersTag, key);
      return (BiMappings)(mappings != null ? mappings : this.loadBiMappings(data, key));
   }

   @Nullable
   protected FullMappings loadFullMappings(CompoundTag data, CompoundTag unmappedIdentifiersTag, CompoundTag mappedIdentifiersTag, String key) {
      if (unmappedIdentifiersTag.contains(key) && mappedIdentifiersTag.contains(key)) {
         MappingDataLoader.IdentifiersPair identifiersPair = this.identifiersFromGlobalIds(unmappedIdentifiersTag, mappedIdentifiersTag, key);
         Mappings mappings = this.loadBiMappings(data, key);
         if (mappings == null) {
            mappings = new IdentityMappings(identifiersPair.unmapped().size(), identifiersPair.mapped().size());
         }

         return this.createFullMappings(identifiersPair, mappings);
      } else {
         return null;
      }
   }

   protected FullMappings createFullMappings(MappingDataLoader.IdentifiersPair identifiersPair, Mappings mappings) {
      return FullMappingsBase.of(identifiersPair, mappings);
   }

   @Nullable
   protected BiMappings loadBiMappings(CompoundTag data, String key) {
      Mappings mappings = this.loadMappings(data, key);
      return mappings != null ? BiMappings.of(mappings) : null;
   }

   private void loadTags(RegistryType type, CompoundTag data) {
      CompoundTag tag = data.getCompoundTag(type.identifier());
      if (tag != null) {
         List<TagData> tagsList = new ArrayList<>(this.tags.size());

         for (Entry<String, Tag> entry : tag.entrySet()) {
            int[] entries;
            if (entry.getValue() instanceof ByteArrayTag rangesTag) {
               entries = IdRanges.decodeAsList(rangesTag).toIntArray();
            } else {
               entries = ((IntArrayTag)entry.getValue()).getValue();
            }

            tagsList.add(new TagData(entry.getKey(), entries));
         }

         this.tags.put(type, tagsList);
      }
   }

   @Override
   public int getNewBlockStateId(int id) {
      return this.checkValidity(id, this.blockStateMappings.getNewId(id), "blockstate");
   }

   @Override
   public int getNewBlockId(int id) {
      return this.checkValidity(id, this.blockMappings.getNewId(id), "block");
   }

   @Override
   public int getOldBlockId(int id) {
      return this.blockMappings.inverse().getNewIdOrDefault(id, 1);
   }

   @Override
   public int getNewItemId(int id) {
      return this.checkValidity(id, this.itemMappings.getNewId(id), "item");
   }

   @Override
   public int getOldItemId(int id) {
      return this.itemMappings.inverse().getNewIdOrDefault(id, 1);
   }

   @Override
   public int getNewParticleId(int id) {
      return this.checkValidity(id, this.particleMappings.getNewId(id), "particles");
   }

   @Override
   public int getNewAttributeId(int id) {
      return this.checkValidity(id, this.attributeMappings.getNewId(id), "attributes");
   }

   @Override
   public int getNewSoundId(int id) {
      return this.checkValidity(id, this.soundMappings.getNewId(id), "sound");
   }

   @Override
   public int getOldSoundId(int id) {
      return this.soundMappings.inverse().getNewIdOrDefault(id, 0);
   }

   @Nullable
   @Override
   public List<TagData> getTags(RegistryType type) {
      return this.tags != null ? this.tags.get(type) : null;
   }

   @Nullable
   @Override
   public BiMappings getItemMappings() {
      return this.itemMappings;
   }

   @Nullable
   @Override
   public FullMappings getFullItemMappings() {
      return this.itemMappings instanceof FullMappings fullItemMappings ? fullItemMappings : null;
   }

   @Nullable
   @Override
   public ParticleMappings getParticleMappings() {
      return this.particleMappings;
   }

   @Nullable
   @Override
   public Mappings getBlockMappings() {
      return this.blockMappings;
   }

   @Nullable
   @Override
   public FullMappings getFullBlockMappings() {
      return this.blockMappings instanceof FullMappings fullBlockMappings ? fullBlockMappings : null;
   }

   @Nullable
   @Override
   public FullMappings getBlockEntityMappings() {
      return this.blockEntityMappings;
   }

   @Nullable
   @Override
   public Mappings getBlockStateMappings() {
      return this.blockStateMappings;
   }

   @Nullable
   @Override
   public Mappings getSoundMappings() {
      return this.soundMappings;
   }

   @Nullable
   @Override
   public FullMappings getFullSoundMappings() {
      return this.soundMappings instanceof FullMappings fullSoundMappings ? fullSoundMappings : null;
   }

   @Nullable
   @Override
   public Mappings getStatisticsMappings() {
      return this.statisticsMappings;
   }

   @Nullable
   @Override
   public Mappings getMenuMappings() {
      return this.menuMappings;
   }

   @Nullable
   @Override
   public Mappings getEnchantmentMappings() {
      return this.enchantmentMappings;
   }

   @Nullable
   @Override
   public FullMappings getAttributeMappings() {
      return this.attributeMappings;
   }

   @Nullable
   @Override
   public FullMappings getEntityMappings() {
      return this.entityMappings;
   }

   @Nullable
   @Override
   public FullMappings getArgumentTypeMappings() {
      return this.argumentTypeMappings;
   }

   @Nullable
   @Override
   public FullMappings getDataComponentSerializerMappings() {
      return this.itemDataSerializerMappings;
   }

   @Nullable
   @Override
   public Mappings getPaintingMappings() {
      return this.paintingMappings;
   }

   @Nullable
   @Override
   public FullMappings getRecipeSerializerMappings() {
      return this.recipeSerializerMappings;
   }

   @Nullable
   @Override
   public FullMappings getSlotDisplayMappings() {
      return this.slotDisplayMappings;
   }

   @Nullable
   @Override
   public IntSet changedBlocks() {
      return this.changedBlocks;
   }

   @Nullable
   @Override
   public Set<String> changedEnvironmentAttributes() {
      return this.changedEnvironmentAttributes;
   }

   protected Logger getLogger() {
      return Via.getPlatform().getLogger();
   }

   protected int checkValidity(int id, int mappedId, String type) {
      if (mappedId == -1) {
         if (Via.getConfig().logOtherConversionWarnings()) {
            this.getLogger().warning(String.format("Missing %s %s for %s %s %d", this.mappedVersion, type, this.unmappedVersion, type, id));
         }

         return 0;
      } else {
         return mappedId;
      }
   }

   protected void loadExtras(CompoundTag data) {
   }
}
