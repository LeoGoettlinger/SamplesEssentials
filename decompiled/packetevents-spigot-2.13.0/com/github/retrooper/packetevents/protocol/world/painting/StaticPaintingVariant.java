package com.github.retrooper.packetevents.protocol.world.painting;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticPaintingVariant extends AbstractMappedEntity implements PaintingVariant {
   private final int width;
   private final int height;
   private final ResourceLocation assetId;
   @Nullable
   private final Component title;
   @Nullable
   private final Component author;

   public StaticPaintingVariant(int width, int height, ResourceLocation assetId) {
      this(null, width, height, assetId, null, null);
   }

   public StaticPaintingVariant(int width, int height, ResourceLocation assetId, @Nullable Component title, @Nullable Component author) {
      this(null, width, height, assetId, title, author);
   }

   @Internal
   public StaticPaintingVariant(
      @Nullable TypesBuilderData data, int width, int height, ResourceLocation assetId, @Nullable Component title, @Nullable Component author
   ) {
      super(data);
      this.width = width;
      this.height = height;
      this.assetId = assetId;
      this.title = title;
      this.author = author;
   }

   public PaintingVariant copy(@Nullable TypesBuilderData newData) {
      return new StaticPaintingVariant(newData, this.width, this.height, this.assetId, this.title, this.author);
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Nullable
   @Override
   public Component getTitle() {
      return this.title;
   }

   @Nullable
   @Override
   public Component getAuthor() {
      return this.author;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StaticPaintingVariant)) {
         return false;
      } else {
         StaticPaintingVariant that = (StaticPaintingVariant)obj;
         if (this.width != that.width) {
            return false;
         } else if (this.height != that.height) {
            return false;
         } else if (!this.assetId.equals(that.assetId)) {
            return false;
         } else {
            return !Objects.equals(this.title, that.title) ? false : Objects.equals(this.author, that.author);
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.width, this.height, this.assetId, this.title, this.author);
   }

   @Override
   public String toString() {
      return "StaticPaintingVariant{width="
         + this.width
         + ", height="
         + this.height
         + ", assetId="
         + this.assetId
         + ", title="
         + this.title
         + ", author="
         + this.author
         + '}';
   }
}
