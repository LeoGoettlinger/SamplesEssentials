package com.github.retrooper.packetevents.protocol.world.attributes.easing;

import com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import com.github.retrooper.packetevents.util.FloatUnaryOperator;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticEasingType extends AbstractMappedEntity implements EasingType {
   private final FloatUnaryOperator operator;

   @Internal
   public StaticEasingType(@Nullable TypesBuilderData data, FloatUnaryOperator operator) {
      super(data);
      this.operator = operator;
   }

   @Override
   public float apply(float x) {
      return this.operator.apply(x);
   }
}
