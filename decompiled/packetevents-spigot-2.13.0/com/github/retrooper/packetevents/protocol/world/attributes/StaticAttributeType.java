package com.github.retrooper.packetevents.protocol.world.attributes;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.world.attributes.modifiers.AttributeModifier;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticAttributeType<T> extends AbstractMappedEntity implements AttributeType<T> {
   @Nullable
   private final NbtCodec<T> valueCodec;
   private final NbtCodec<AttributeModifier<T, ?>> modifierCodec;

   @Internal
   public StaticAttributeType(@Nullable TypesBuilderData data, @Nullable NbtCodec<T> valueCodec, NbtCodec<AttributeModifier<T, ?>> modifierCodec) {
      super(data);
      this.valueCodec = valueCodec;
      this.modifierCodec = modifierCodec;
   }

   @Override
   public boolean isSynced() {
      return this.valueCodec != null;
   }

   @Override
   public NbtCodec<T> getValueCodec() {
      if (this.valueCodec == null) {
         throw new UnsupportedOperationException();
      } else {
         return this.valueCodec;
      }
   }

   @Override
   public NbtCodec<AttributeModifier<T, ?>> getModifierCodec() {
      return this.modifierCodec;
   }
}
