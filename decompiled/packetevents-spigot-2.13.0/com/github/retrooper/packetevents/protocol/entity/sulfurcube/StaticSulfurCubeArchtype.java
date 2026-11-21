package com.github.retrooper.packetevents.protocol.entity.sulfurcube;

import com.github.retrooper.packetevents.protocol.attribute.AttributeEntry;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Internal
final class StaticSulfurCubeArchtype extends AbstractMappedEntity implements SulfurCubeArchtype {
   private final MappedEntitySet<ItemType> items;
   private final List<AttributeEntry> attributeModifiers;
   private final boolean buoyant;
   @Nullable
   private final SulfurCubeExplosionData explosion;
   @Nullable
   private final SulfurCubeContactDamage contactDamage;
   private final SulfurCubeKnockbackModifiers knockbackModifiers;
   private final SulfurCubeSoundSettings soundSettings;

   @Internal
   public StaticSulfurCubeArchtype(
      @Nullable TypesBuilderData data,
      MappedEntitySet<ItemType> items,
      List<AttributeEntry> attributeModifiers,
      boolean buoyant,
      @Nullable SulfurCubeExplosionData explosion,
      @Nullable SulfurCubeContactDamage contactDamage,
      SulfurCubeKnockbackModifiers knockbackModifiers,
      SulfurCubeSoundSettings soundSettings
   ) {
      super(data);
      this.items = items;
      this.attributeModifiers = Collections.unmodifiableList(attributeModifiers);
      this.buoyant = buoyant;
      this.explosion = explosion;
      this.contactDamage = contactDamage;
      this.knockbackModifiers = knockbackModifiers;
      this.soundSettings = soundSettings;
   }

   public SulfurCubeArchtype copy(@Nullable TypesBuilderData newData) {
      return new StaticSulfurCubeArchtype(
         newData, this.items, this.attributeModifiers, this.buoyant, this.explosion, this.contactDamage, this.knockbackModifiers, this.soundSettings
      );
   }

   @Override
   public MappedEntitySet<ItemType> getItems() {
      return this.items;
   }

   @Override
   public List<AttributeEntry> getAttributeModifiers() {
      return this.attributeModifiers;
   }

   @Override
   public boolean isBuoyant() {
      return this.buoyant;
   }

   @Nullable
   @Override
   public SulfurCubeExplosionData getExplosion() {
      return this.explosion;
   }

   @Nullable
   @Override
   public SulfurCubeContactDamage getContactDamage() {
      return this.contactDamage;
   }

   @Override
   public SulfurCubeKnockbackModifiers getKnockbackModifiers() {
      return this.knockbackModifiers;
   }

   @Override
   public SulfurCubeSoundSettings getSoundSettings() {
      return this.soundSettings;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         StaticSulfurCubeArchtype that = (StaticSulfurCubeArchtype)obj;
         if (this.buoyant != that.buoyant) {
            return false;
         } else if (!this.items.equals(that.items)) {
            return false;
         } else if (!this.attributeModifiers.equals(that.attributeModifiers)) {
            return false;
         } else if (!Objects.equals(this.explosion, that.explosion)) {
            return false;
         } else if (!Objects.equals(this.contactDamage, that.contactDamage)) {
            return false;
         } else {
            return !this.knockbackModifiers.equals(that.knockbackModifiers) ? false : this.soundSettings.equals(that.soundSettings);
         }
      } else {
         return false;
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.items, this.attributeModifiers, this.buoyant, this.explosion, this.contactDamage, this.knockbackModifiers, this.soundSettings);
   }
}
