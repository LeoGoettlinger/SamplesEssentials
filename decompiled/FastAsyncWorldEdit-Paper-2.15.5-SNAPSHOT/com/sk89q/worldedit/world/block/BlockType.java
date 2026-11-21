package com.sk89q.worldedit.world.block;

import com.fastasyncworldedit.core.function.mask.SingleBlockTypeMask;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.registry.state.AbstractProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class BlockType implements Keyed, Pattern {
   public static final NamespacedRegistry<BlockType> REGISTRY = new NamespacedRegistry<>("block type", "block_type", "minecraft", true);
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final String id;
   private final LazyReference<FuzzyBlockState> emptyFuzzy = LazyReference.from(() -> new FuzzyBlockState(this));
   private final BlockTypesCache.Settings settings;
   @Deprecated
   private final LazyReference<String> name = LazyReference.from(
      () -> WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getName(this)
   );
   private Integer legacyCombinedId;
   private boolean initItemType;
   private ItemType itemType;

   protected BlockType(String id, int internalId, List<BlockState> states) {
      int i = id.indexOf("[");
      this.id = i == -1 ? id : id.substring(0, i);
      this.settings = new BlockTypesCache.Settings(this, id, internalId, states);
   }

   @Deprecated(
      since = "2.7.0"
   )
   public BlockType(String id) {
      this(id, null);
   }

   @Deprecated(
      since = "2.7.0"
   )
   public BlockType(String id, Function<BlockState, BlockState> values) {
      if (!id.contains(":")) {
         id = "minecraft:" + id;
      }

      this.id = id;
      this.settings = new BlockTypesCache.Settings(this, id, 0, null);
   }

   @Deprecated
   public int getMaxStateId() {
      return this.settings.permutations;
   }

   @Override
   public String id() {
      return this.id;
   }

   public Component getRichName() {
      return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBlockRegistry().getRichName(this);
   }

   public String getNamespace() {
      String id = this.id();
      int i = id.indexOf(58);
      return i == -1 ? "minecraft" : id.substring(0, i);
   }

   public String getResource() {
      String id = this.id();
      return id.substring(id.indexOf(58) + 1);
   }

   @Deprecated
   public String getName() {
      String name = this.name.getValue();
      return name != null && !name.isEmpty() ? name : this.id();
   }

   @Deprecated
   public BlockState withPropertyId(int propertyId) {
      if (this.settings.stateOrdinals == null) {
         return this.settings.defaultState;
      } else if (propertyId < this.settings.stateOrdinals.length && propertyId >= 0) {
         int ordinal = this.settings.stateOrdinals[propertyId];
         if (ordinal < BlockTypesCache.states.length && ordinal >= 0) {
            return BlockTypesCache.states[ordinal];
         } else {
            LOGGER.error(
               "Attempted to load blockstate with ordinal {} of type {} outside of states length. Using default state. Using default state.",
               ordinal,
               this.id()
            );
            return this.settings.defaultState;
         }
      } else {
         LOGGER.error("Attempted to load blockstate with id {} of type {} outside of state ordinals length. Using default state.", propertyId, this.id());
         return this.settings.defaultState;
      }
   }

   @Deprecated
   public BlockState withStateId(int internalStateId) {
      return this.withPropertyId(internalStateId >> BlockTypesCache.BIT_OFFSET);
   }

   public Map<String, ? extends Property<?>> getPropertyMap() {
      return this.settings.propertiesMap;
   }

   public List<? extends Property<?>> getProperties() {
      return this.settings.propertiesList;
   }

   @Deprecated
   public Set<? extends Property<?>> getPropertiesSet() {
      return this.settings.propertiesSet;
   }

   public <V> Property<V> getProperty(String name) {
      return (Property<V>)this.settings.propertiesMap.get(name);
   }

   public boolean hasProperty(PropertyKey key) {
      int ordinal = key.getId();
      return this.settings.propertiesMapArr.length > ordinal && this.settings.propertiesMapArr[ordinal] != null;
   }

   public boolean hasProperty(Property<?> property) {
      int ordinal = property.getKey().getId();
      return this.settings.propertiesMapArr.length > ordinal && property.equals(this.settings.propertiesMapArr[ordinal]);
   }

   public <V> Property<V> getProperty(PropertyKey key) {
      try {
         return (Property<V>)this.settings.propertiesMapArr[key.getId()];
      } catch (IndexOutOfBoundsException var3) {
         return null;
      }
   }

   public BlockState getDefaultState() {
      return this.settings.defaultState;
   }

   public FuzzyBlockState getFuzzyMatcher() {
      return this.emptyFuzzy.getValue();
   }

   public List<BlockState> getAllStates() {
      return this.settings.stateOrdinals == null
         ? Collections.singletonList(this.getDefaultState())
         : IntStream.of(this.settings.stateOrdinals).filter(i -> i != -1).mapToObj(i -> BlockTypesCache.states[i]).collect(Collectors.toList());
   }

   @Deprecated(
      forRemoval = true
   )
   public BlockState getState(Map<Property<?>, Object> key) {
      int id = this.getInternalId();

      for (Entry<Property<?>, Object> iter : key.entrySet()) {
         Property<?> prop = iter.getKey();
         Object value = iter.getValue();
         AbstractProperty btp = this.settings.propertiesMap.get(prop.getName());
         Preconditions.checkArgument(btp != null, "%s has no property named %s", this, prop.getName());
         id = btp.modify(id, btp.getValueFor((String)value));
      }

      return this.withStateId(id);
   }

   public boolean hasItemType() {
      return this.getItemType() != null;
   }

   @Nullable
   public ItemType getItemType() {
      if (!this.initItemType) {
         this.initItemType = true;
         this.itemType = ItemTypes.get(this.id);
      }

      return this.itemType;
   }

   public BlockMaterial getMaterial() {
      return this.settings.blockMaterial;
   }

   @Deprecated
   public int getLegacyCombinedId() {
      Integer combinedId = LegacyMapper.getInstance().getLegacyCombined(this);
      return combinedId == null ? 0 : combinedId;
   }

   @Deprecated
   public int getLegacyId() {
      return this.computeLegacy(0);
   }

   @Deprecated
   public int getLegacyData() {
      return this.computeLegacy(1);
   }

   private int computeLegacy(int index) {
      if (this.legacyCombinedId == null) {
         this.legacyCombinedId = LegacyMapper.getInstance().getLegacyCombined(this.getDefaultState());
      }

      return index == 0 ? this.legacyCombinedId >> 4 : this.legacyCombinedId & 15;
   }

   @Override
   public String toString() {
      return this.id();
   }

   public int getInternalId() {
      return this.settings.internalId;
   }

   @Override
   public int hashCode() {
      return this.settings.internalId;
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this;
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      return set.setBlock(extent, this.getDefaultState());
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      return this.getDefaultState().toBaseBlock();
   }

   public SingleBlockTypeMask toMask() {
      return this.toMask(new NullExtent());
   }

   public SingleBlockTypeMask toMask(Extent extent) {
      return new SingleBlockTypeMask(extent, this);
   }
}
