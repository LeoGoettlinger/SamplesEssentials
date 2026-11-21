package com.github.retrooper.packetevents.protocol.entity.wolfvariant;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.world.biome.Biome;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticWolfVariant extends AbstractMappedEntity implements WolfVariant {
   private final WolfAssetSet assets;
   private final WolfAssetSet babyAssets;
   private final MappedEntitySet<Biome> biomes;

   @Deprecated
   @Obsolete
   public StaticWolfVariant(ResourceLocation wildTexture, ResourceLocation tameTexture, ResourceLocation angryTexture, MappedEntitySet<Biome> biomes) {
      this(new WolfAssetSet(wildTexture, tameTexture, angryTexture), biomes);
   }

   @Deprecated
   @Obsolete
   public StaticWolfVariant(ResourceLocation angryTexture, ResourceLocation tameTexture, ResourceLocation wildTexture) {
      this(new WolfAssetSet(wildTexture, tameTexture, angryTexture));
   }

   @Obsolete
   public StaticWolfVariant(WolfAssetSet assets, MappedEntitySet<Biome> biomes) {
      this(null, assets, assets, biomes);
   }

   @Obsolete
   public StaticWolfVariant(WolfAssetSet assets) {
      this(null, assets, assets, MappedEntitySet.createEmpty());
   }

   public StaticWolfVariant(WolfAssetSet assets, WolfAssetSet babyAssets) {
      this(null, assets, babyAssets, MappedEntitySet.createEmpty());
   }

   @Internal
   public StaticWolfVariant(@Nullable TypesBuilderData data, WolfAssetSet assets, WolfAssetSet babyAssets, MappedEntitySet<Biome> biomes) {
      super(data);
      this.assets = assets;
      this.babyAssets = babyAssets;
      this.biomes = biomes;
   }

   public WolfVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticWolfVariant(newData, this.assets, this.babyAssets, this.biomes);
   }

   @Override
   public WolfAssetSet getAssets() {
      return this.assets;
   }

   @Override
   public WolfAssetSet getBabyAssets() {
      return this.babyAssets;
   }

   @Override
   public MappedEntitySet<Biome> getBiomes() {
      return this.biomes;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StaticWolfVariant)) {
         return false;
      } else {
         StaticWolfVariant that = (StaticWolfVariant)obj;
         if (!this.assets.equals(that.assets)) {
            return false;
         } else {
            return !this.babyAssets.equals(that.babyAssets) ? false : this.biomes.equals(that.biomes);
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.assets, this.babyAssets, this.biomes);
   }
}
