package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.sulfurcube;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attribute;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.AttributeEntry;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.AttributeModifier;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntityBuilder;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sounds;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.ItemTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.TagKey;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class SulfurCubeArchtypeBuilder implements MappedEntityBuilder<SulfurCubeArchtype> {
   private static final MappedEntitySet<ItemType> REGULAR_ITEMS = new MappedEntitySet<>(new ResourceLocation(ItemTags.SULFUR_CUBE_ARCHETYPE_REGULAR.getName()));
   private static final SulfurCubeKnockbackModifiers REGULAR_KNOCKBACK_MODIFIERS = new SulfurCubeKnockbackModifiers(0.4125F, 0.09F);
   private static final SulfurCubeSoundSettings REGULAR_SOUND_SETTINGS = new SulfurCubeSoundSettings(
      Sounds.ENTITY_SULFUR_CUBE_REGULAR_HIT, Sounds.ENTITY_SULFUR_CUBE_REGULAR_PUSH, 0.2F, 0.5F
   );
   private MappedEntitySet<ItemType> items = REGULAR_ITEMS;
   private List<AttributeEntry> attributeModifiers = new ArrayList<>();
   private boolean buoyant = false;
   @Nullable
   private SulfurCubeExplosionData explosion = null;
   @Nullable
   private SulfurCubeContactDamage contactDamage = null;
   private SulfurCubeKnockbackModifiers knockbackModifiers = REGULAR_KNOCKBACK_MODIFIERS;
   private SulfurCubeSoundSettings soundSettings = REGULAR_SOUND_SETTINGS;

   private SulfurCubeArchtypeBuilder() {
   }

   public static SulfurCubeArchtypeBuilder sulfurCubeArchtypeBuilder() {
      return new SulfurCubeArchtypeBuilder();
   }

   @ApiStatus.Internal
   public SulfurCubeArchtype build(@Nullable TypesBuilderData data) {
      return new StaticSulfurCubeArchtype(
         data, this.items, this.attributeModifiers, this.buoyant, this.explosion, this.contactDamage, this.knockbackModifiers, this.soundSettings
      );
   }

   public MappedEntitySet<ItemType> getItems() {
      return this.items;
   }

   public SulfurCubeArchtypeBuilder setItems(ItemTags itemsTag) {
      return this.setItems(new MappedEntitySet<>(new ResourceLocation(itemsTag.getName())));
   }

   public SulfurCubeArchtypeBuilder setItems(TagKey itemsTag) {
      return this.setItems(new MappedEntitySet<>(itemsTag.getId()));
   }

   public SulfurCubeArchtypeBuilder setItems(MappedEntitySet<ItemType> items) {
      this.items = items;
      return this;
   }

   public SulfurCubeArchtypeBuilder addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
      return this.addAttributeModifier(new AttributeEntry(attribute, modifier));
   }

   public SulfurCubeArchtypeBuilder addAttributeModifier(AttributeEntry entry) {
      this.attributeModifiers.add(entry);
      return this;
   }

   public List<AttributeEntry> getAttributeModifiers() {
      return this.attributeModifiers;
   }

   public SulfurCubeArchtypeBuilder setAttributeModifiers(List<AttributeEntry> attributeModifiers) {
      this.attributeModifiers = attributeModifiers;
      return this;
   }

   public boolean isBuoyant() {
      return this.buoyant;
   }

   public SulfurCubeArchtypeBuilder setBuoyant(boolean buoyant) {
      this.buoyant = buoyant;
      return this;
   }

   @Nullable
   public SulfurCubeExplosionData getExplosion() {
      return this.explosion;
   }

   public SulfurCubeArchtypeBuilder setExplosion(@Nullable SulfurCubeExplosionData explosion) {
      this.explosion = explosion;
      return this;
   }

   @Nullable
   public SulfurCubeContactDamage getContactDamage() {
      return this.contactDamage;
   }

   public SulfurCubeArchtypeBuilder setContactDamage(@Nullable SulfurCubeContactDamage contactDamage) {
      this.contactDamage = contactDamage;
      return this;
   }

   public SulfurCubeKnockbackModifiers getKnockbackModifiers() {
      return this.knockbackModifiers;
   }

   public SulfurCubeArchtypeBuilder setKnockbackModifiers(SulfurCubeKnockbackModifiers knockbackModifiers) {
      this.knockbackModifiers = knockbackModifiers;
      return this;
   }

   public SulfurCubeSoundSettings getSoundSettings() {
      return this.soundSettings;
   }

   public SulfurCubeArchtypeBuilder setSoundSettings(SulfurCubeSoundSettings soundSettings) {
      this.soundSettings = soundSettings;
      return this;
   }
}
