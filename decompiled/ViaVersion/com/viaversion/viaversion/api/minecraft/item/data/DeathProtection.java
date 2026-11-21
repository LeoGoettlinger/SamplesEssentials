package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ConsumeEffect;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Copyable;
import java.util.function.Function;

public record DeathProtection(ConsumeEffect<?>[] deathEffects) implements Copyable {
   public static final Type<DeathProtection> TYPE1_21_2 = new TransformingType<ConsumeEffect<?>[], DeathProtection>(
      ConsumeEffect.ARRAY_TYPE1_21_2, DeathProtection.class, DeathProtection::new, DeathProtection::deathEffects
   ) {
      public void write(Ops ops, DeathProtection value) {
         ops.writeMap(map -> map.writeOptional("death_effects", ConsumeEffect.ARRAY_TYPE1_21_2, value.deathEffects, new ConsumeEffect[0]));
      }
   };
   public static final Type<DeathProtection> TYPE26_3 = new TransformingType<ConsumeEffect<?>[], DeathProtection>(
      ConsumeEffect.ARRAY_TYPE26_3, DeathProtection.class, DeathProtection::new, DeathProtection::deathEffects
   ) {
      public void write(Ops ops, DeathProtection value) {
         ops.writeMap(map -> map.writeOptional("death_effects", ConsumeEffect.ARRAY_TYPE26_3, value.deathEffects, new ConsumeEffect[0]));
      }
   };

   public DeathProtection copy() {
      return new DeathProtection(Copyable.copy(this.deathEffects));
   }
}
