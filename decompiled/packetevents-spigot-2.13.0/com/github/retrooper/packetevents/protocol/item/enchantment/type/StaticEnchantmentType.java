package com.github.retrooper.packetevents.protocol.item.enchantment.type;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.component.StaticComponentMap;
import com.github.retrooper.packetevents.protocol.item.enchantment.EnchantmentDefinition;
import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntityRefSet;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.mapper.ResolvableEntity;
import com.github.retrooper.packetevents.util.mappings.GlobalRegistryHolder;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticEnchantmentType extends AbstractMappedEntity implements EnchantmentType, ResolvableEntity {
   private final Component description;
   private final EnchantmentDefinition definition;
   private final MappedEntityRefSet<EnchantmentType> exclusiveSetRef;
   @Nullable
   private MappedEntitySet<EnchantmentType> exclusiveSet;
   private final StaticComponentMap effects;

   public StaticEnchantmentType(
      Component description, EnchantmentDefinition definition, MappedEntitySet<EnchantmentType> exclusiveSet, StaticComponentMap effects
   ) {
      this(null, description, definition, exclusiveSet, effects);
   }

   @Internal
   public StaticEnchantmentType(
      @Nullable TypesBuilderData data,
      Component description,
      EnchantmentDefinition definition,
      MappedEntityRefSet<EnchantmentType> exclusiveSet,
      StaticComponentMap effects
   ) {
      super(data);
      this.description = description;
      this.definition = definition;
      this.exclusiveSetRef = exclusiveSet;
      this.effects = effects;
   }

   @Override
   public void doResolve(PacketWrapper<?> wrapper) {
      this.exclusiveSet = this.exclusiveSetRef.resolve(wrapper, EnchantmentTypes.getRegistry());
   }

   public EnchantmentType copy(@Nullable TypesBuilderData newData) {
      StaticEnchantmentType type = new StaticEnchantmentType(newData, this.description, this.definition, this.exclusiveSetRef, this.effects);
      type.exclusiveSet = this.exclusiveSet;
      return type;
   }

   @Override
   public Component getDescription() {
      return this.description;
   }

   @Override
   public EnchantmentDefinition getDefinition() {
      return this.definition;
   }

   @Override
   public MappedEntitySet<EnchantmentType> getExclusiveSet() {
      if (this.exclusiveSet == null) {
         this.exclusiveSet = this.exclusiveSetRef
            .resolve(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), GlobalRegistryHolder.INSTANCE, EnchantmentTypes.getRegistry());
      }

      return this.exclusiveSet;
   }

   @Override
   public MappedEntityRefSet<EnchantmentType> getExclusiveRefSet() {
      return this.exclusiveSetRef;
   }

   @Override
   public StaticComponentMap getEffects() {
      return this.effects;
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StaticEnchantmentType)) {
         return false;
      } else {
         StaticEnchantmentType that = (StaticEnchantmentType)obj;
         if (!this.description.equals(that.description)) {
            return false;
         } else if (!this.definition.equals(that.definition)) {
            return false;
         } else {
            return !this.exclusiveSetRef.equals(that.exclusiveSetRef) ? false : this.effects.equals(that.effects);
         }
      }
   }

   @Override
   public int deepHashCode() {
      return Objects.hash(this.description, this.definition, this.exclusiveSetRef, this.effects);
   }

   @Override
   public String toString() {
      return "StaticEnchantmentType{description="
         + this.description
         + ", definition="
         + this.definition
         + ", exclusiveSetRef="
         + this.exclusiveSetRef
         + ", effects="
         + this.effects
         + "}";
   }
}
