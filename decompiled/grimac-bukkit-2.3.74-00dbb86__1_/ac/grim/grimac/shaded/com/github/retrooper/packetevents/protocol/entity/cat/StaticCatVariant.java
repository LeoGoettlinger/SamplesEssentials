package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cat;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticCatVariant extends AbstractMappedEntity implements CatVariant {
   private final ResourceLocation assetId;
   private final ResourceLocation babyAssetId;

   @ApiStatus.Obsolete
   public StaticCatVariant(ResourceLocation assetId) {
      this(null, assetId, assetId);
   }

   public StaticCatVariant(ResourceLocation assetId, ResourceLocation babyAssetId) {
      this(null, assetId, babyAssetId);
   }

   @ApiStatus.Internal
   public StaticCatVariant(@Nullable TypesBuilderData data, ResourceLocation assetId, ResourceLocation babyAssetId) {
      super(data);
      this.assetId = assetId;
      this.babyAssetId = babyAssetId;
   }

   public CatVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticCatVariant(newData, this.assetId, this.babyAssetId);
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Override
   public ResourceLocation getBabyAssetId() {
      return this.babyAssetId;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (!(obj instanceof StaticCatVariant)) {
         return false;
      } else {
         StaticCatVariant that = (StaticCatVariant)obj;
         return !this.assetId.equals(that.assetId) ? false : this.babyAssetId.equals(that.babyAssetId);
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.assetId, this.babyAssetId);
   }
}
