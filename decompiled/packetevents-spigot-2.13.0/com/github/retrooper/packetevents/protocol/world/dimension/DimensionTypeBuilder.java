package com.github.retrooper.packetevents.protocol.world.dimension;

import com.github.retrooper.packetevents.protocol.mapper.MappedEntityBuilder;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntityRef;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntityRefSet;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttribute;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttributeMap;
import com.github.retrooper.packetevents.protocol.world.attributes.modifiers.AttributeModifier;
import com.github.retrooper.packetevents.protocol.world.attributes.timelines.Timeline;
import com.github.retrooper.packetevents.protocol.world.clock.WorldClock;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.resources.TagKey;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class DimensionTypeBuilder implements MappedEntityBuilder<DimensionType> {
   private boolean hasFixedTime = false;
   private DimensionType.Skybox skybox = DimensionType.Skybox.OVERWORLD;
   private DimensionType.CardinalLight cardinalLight = DimensionType.CardinalLight.DEFAULT;
   private final EnvironmentAttributeMap attributes = EnvironmentAttributeMap.create();
   private MappedEntityRefSet<Timeline> timelines = MappedEntitySet.createEmpty();
   @Obsolete
   @Nullable
   private Long fixedTime = null;
   @Obsolete
   private boolean natural;
   @Obsolete
   private boolean bedWorks;
   @Obsolete
   private boolean respawnAnchorWorks;
   @Obsolete
   @Nullable
   private ResourceLocation effects;
   private double coordinateScale = 1.0;
   private int minY = 0;
   private int height = 256;
   @Experimental
   private NBT monsterSpawnLightLevel = new NBTInt(7);
   private int monsterSpawnBlockLightLimit = 7;
   private boolean hasSkylight = true;
   private boolean hasCeiling = false;
   private int logicalHeight = 256;
   private TagKey infiniburn = BlockTags.INFINIBURN_OVERWORLD.getKey();
   private float ambientLight = 0.0F;
   @Nullable
   private MappedEntityRef<WorldClock> defaultClock;
   private boolean hasEnderDragonFight;

   private DimensionTypeBuilder() {
   }

   public static DimensionTypeBuilder dimensionTypeBuilder() {
      return new DimensionTypeBuilder();
   }

   @Internal
   public DimensionType build(@Nullable TypesBuilderData data) {
      return new StaticDimensionType(
         data,
         this.hasFixedTime,
         this.skybox,
         this.cardinalLight,
         this.attributes.copyImmutable(),
         this.timelines,
         this.fixedTime,
         this.natural,
         this.bedWorks,
         this.respawnAnchorWorks,
         this.effects,
         this.coordinateScale,
         this.minY,
         this.height,
         this.monsterSpawnLightLevel,
         this.monsterSpawnBlockLightLimit,
         this.hasSkylight,
         this.hasCeiling,
         this.logicalHeight,
         this.infiniburn,
         this.ambientLight,
         this.defaultClock,
         this.hasEnderDragonFight
      );
   }

   public boolean isRespawnAnchorWorks() {
      return this.respawnAnchorWorks;
   }

   public DimensionTypeBuilder setRespawnAnchorWorks(boolean respawnAnchorWorks) {
      this.respawnAnchorWorks = respawnAnchorWorks;
      return this;
   }

   public boolean isHasFixedTime() {
      return this.hasFixedTime;
   }

   public DimensionTypeBuilder setHasFixedTime(boolean hasFixedTime) {
      this.hasFixedTime = hasFixedTime;
      return this;
   }

   public DimensionType.Skybox getSkybox() {
      return this.skybox;
   }

   public DimensionTypeBuilder setSkybox(DimensionType.Skybox skybox) {
      this.skybox = skybox;
      return this;
   }

   public DimensionType.CardinalLight getCardinalLight() {
      return this.cardinalLight;
   }

   public DimensionTypeBuilder setCardinalLight(DimensionType.CardinalLight cardinalLight) {
      this.cardinalLight = cardinalLight;
      return this;
   }

   public EnvironmentAttributeMap getAttributes() {
      return this.attributes;
   }

   public <T> DimensionTypeBuilder setAttribute(EnvironmentAttribute<T> attribute, T value) {
      return this.setAttribute(attribute, value, AttributeModifier.override());
   }

   public <T, A> DimensionTypeBuilder setAttribute(EnvironmentAttribute<T> attribute, A value, AttributeModifier<T, A> modifier) {
      this.attributes.set(attribute, value, modifier);
      return this;
   }

   public DimensionTypeBuilder setAttributes(EnvironmentAttributeMap attributes) {
      this.attributes.setAll(attributes);
      return this;
   }

   public MappedEntityRefSet<Timeline> getTimelines() {
      return this.timelines;
   }

   public DimensionTypeBuilder setTimelines(MappedEntityRefSet<Timeline> timelines) {
      this.timelines = timelines;
      return this;
   }

   @Nullable
   public Long getFixedTime() {
      return this.fixedTime;
   }

   public DimensionTypeBuilder setFixedTime(@Nullable Long fixedTime) {
      this.hasFixedTime = fixedTime != null;
      this.fixedTime = fixedTime;
      return this;
   }

   public boolean isNatural() {
      return this.natural;
   }

   public DimensionTypeBuilder setNatural(boolean natural) {
      this.natural = natural;
      return this;
   }

   public boolean isBedWorks() {
      return this.bedWorks;
   }

   public DimensionTypeBuilder setBedWorks(boolean bedWorks) {
      this.bedWorks = bedWorks;
      return this;
   }

   @Nullable
   public ResourceLocation getEffects() {
      return this.effects;
   }

   public DimensionTypeBuilder setEffects(ResourceLocation effects) {
      this.effects = effects;
      return this;
   }

   public double getCoordinateScale() {
      return this.coordinateScale;
   }

   public DimensionTypeBuilder setCoordinateScale(double coordinateScale) {
      this.coordinateScale = coordinateScale;
      return this;
   }

   public int getMinY() {
      return this.minY;
   }

   public DimensionTypeBuilder setMinY(int minY) {
      this.minY = minY;
      return this;
   }

   public int getHeight() {
      return this.height;
   }

   public DimensionTypeBuilder setHeight(int height) {
      this.height = height;
      return this;
   }

   @Experimental
   public NBT getMonsterSpawnLightLevel() {
      return this.monsterSpawnLightLevel;
   }

   @Experimental
   public DimensionTypeBuilder setMonsterSpawnLightLevel(NBT monsterSpawnLightLevel) {
      this.monsterSpawnLightLevel = monsterSpawnLightLevel;
      return this;
   }

   public int getMonsterSpawnBlockLightLimit() {
      return this.monsterSpawnBlockLightLimit;
   }

   public DimensionTypeBuilder setMonsterSpawnBlockLightLimit(int monsterSpawnBlockLightLimit) {
      this.monsterSpawnBlockLightLimit = monsterSpawnBlockLightLimit;
      return this;
   }

   public boolean isHasSkylight() {
      return this.hasSkylight;
   }

   public DimensionTypeBuilder setHasSkylight(boolean hasSkylight) {
      this.hasSkylight = hasSkylight;
      return this;
   }

   public boolean isHasCeiling() {
      return this.hasCeiling;
   }

   public DimensionTypeBuilder setHasCeiling(boolean hasCeiling) {
      this.hasCeiling = hasCeiling;
      return this;
   }

   public int getLogicalHeight() {
      return this.logicalHeight;
   }

   public DimensionTypeBuilder setLogicalHeight(int logicalHeight) {
      this.logicalHeight = logicalHeight;
      return this;
   }

   public TagKey getInfiniburn() {
      return this.infiniburn;
   }

   public DimensionTypeBuilder setInfiniburn(TagKey infiniburn) {
      this.infiniburn = infiniburn;
      return this;
   }

   public float getAmbientLight() {
      return this.ambientLight;
   }

   public DimensionTypeBuilder setAmbientLight(float ambientLight) {
      this.ambientLight = ambientLight;
      return this;
   }

   @Nullable
   public WorldClock getDefaultClock() {
      return this.defaultClock != null ? this.defaultClock.get() : null;
   }

   @Nullable
   public MappedEntityRef<WorldClock> getDefaultClockRef() {
      return this.defaultClock;
   }

   public DimensionTypeBuilder setDefaultClock(@Nullable WorldClock defaultClock) {
      return this.setDefaultClock(defaultClock != null ? new MappedEntityRef.Static<>(defaultClock) : null);
   }

   public DimensionTypeBuilder setDefaultClock(@Nullable MappedEntityRef<WorldClock> defaultClock) {
      this.defaultClock = defaultClock;
      return this;
   }

   public boolean isHasEnderDragonFight() {
      return this.hasEnderDragonFight;
   }

   public DimensionTypeBuilder setHasEnderDragonFight(boolean hasEnderDragonFight) {
      this.hasEnderDragonFight = hasEnderDragonFight;
      return this;
   }
}
