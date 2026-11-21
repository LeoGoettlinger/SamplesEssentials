package com.sk89q.worldedit.world.biome;

import com.fastasyncworldedit.core.registry.RegistryItem;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.function.pattern.BiomePattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.util.formatting.text.Component;

public class BiomeType implements RegistryItem, Keyed, BiomePattern {
   public static final NamespacedRegistry<BiomeType> REGISTRY = new NamespacedRegistry<>("biome type", "biome_type", "minecraft", true);
   private final String id;
   private int legacyId = -1;
   private int internalId;

   public BiomeType(String id) {
      this.id = id;
   }

   @Override
   public String id() {
      return this.id;
   }

   public int getLegacyId() {
      return this.legacyId;
   }

   public void setLegacyId(int legacyId) {
      this.legacyId = legacyId;
   }

   @Override
   public void setInternalId(int internalId) {
      this.internalId = internalId;
   }

   @Override
   public int getInternalId() {
      return this.internalId;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public String toString() {
      return this.id();
   }

   @Override
   public int hashCode() {
      return this.internalId;
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof BiomeType && this.id.equals(((BiomeType)obj).id);
   }

   @Override
   public BiomeType applyBiome(BlockVector3 position) {
      return this;
   }

   public Component getRichName() {
      return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry().getRichName(this);
   }
}
