package com.github.retrooper.packetevents.protocol.item.trimpattern;

import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticTrimPattern extends AbstractMappedEntity implements TrimPattern {
   private final ResourceLocation assetId;
   @Nullable
   private final ItemType templateItem;
   private final Component description;
   private final boolean decal;

   public StaticTrimPattern(ResourceLocation assetId, @Nullable ItemType templateItem, Component description, boolean decal) {
      this(null, assetId, templateItem, description, decal);
   }

   @Internal
   public StaticTrimPattern(@Nullable TypesBuilderData data, ResourceLocation assetId, @Nullable ItemType templateItem, Component description, boolean decal) {
      super(data);
      this.assetId = assetId;
      this.templateItem = templateItem;
      this.description = description;
      this.decal = decal;
   }

   public TrimPattern copy(@Nullable TypesBuilderData newData) {
      return new StaticTrimPattern(newData, this.assetId, this.templateItem, this.description, this.decal);
   }

   @Override
   public ResourceLocation getAssetId() {
      return this.assetId;
   }

   @Obsolete
   @Override
   public ItemType getTemplateItem() {
      return this.templateItem != null ? this.templateItem : ItemTypes.AIR;
   }

   @Override
   public Component getDescription() {
      return this.description;
   }

   @Override
   public boolean isDecal() {
      return this.decal;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StaticTrimPattern)) {
         return false;
      } else {
         StaticTrimPattern that = (StaticTrimPattern)obj;
         if (this.decal != that.decal) {
            return false;
         } else if (!this.assetId.equals(that.assetId)) {
            return false;
         } else {
            return !Objects.equals(this.templateItem, that.templateItem) ? false : this.description.equals(that.description);
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.assetId, this.templateItem, this.description, this.decal);
   }

   @Override
   public String toString() {
      return "StaticTrimPattern{assetId="
         + this.assetId
         + ", templateItem="
         + this.templateItem
         + ", description="
         + this.description
         + ", decal="
         + this.decal
         + '}';
   }
}
