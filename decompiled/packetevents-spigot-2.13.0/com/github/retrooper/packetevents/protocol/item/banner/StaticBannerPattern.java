package com.github.retrooper.packetevents.protocol.item.banner;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticBannerPattern extends AbstractMappedEntity implements BannerPattern {
   private final ResourceLocation assetId;
   private final String translationKey;

   public StaticBannerPattern(ResourceLocation assetId, String translationKey) {
      this(null, assetId, translationKey);
   }

   @Internal
   public StaticBannerPattern(@Nullable TypesBuilderData data, ResourceLocation assetId, String translationKey) {
      super(data);
      this.assetId = assetId;
      this.translationKey = translationKey;
   }

   public BannerPattern copy(@Nullable TypesBuilderData newData) {
      return new StaticBannerPattern(newData, this.assetId, this.translationKey);
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Override
   public String getTranslationKey() {
      return this.translationKey;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StaticBannerPattern)) {
         return false;
      } else {
         StaticBannerPattern that = (StaticBannerPattern)obj;
         return !this.assetId.equals(that.assetId) ? false : this.translationKey.equals(that.translationKey);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.assetId, this.translationKey);
   }

   @Override
   public String toString() {
      return "StaticBannerPattern{assetId=" + this.assetId + ", translationKey='" + this.translationKey + '\'' + '}';
   }
}
