package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record FoodProperties1_20_5(
   int nutrition,
   float saturationModifier,
   boolean canAlwaysEat,
   float eatSeconds,
   @Nullable Item usingConvertsTo,
   FoodProperties1_20_5.FoodEffect[] possibleEffects
) implements Copyable {
   public static final Type<FoodProperties1_20_5> TYPE1_20_5 = new Type<FoodProperties1_20_5>(FoodProperties1_20_5.class) {
      public FoodProperties1_20_5 read(ByteBuf buffer) {
         int nutrition = Types.VAR_INT.readPrimitive(buffer);
         float saturationModifier = buffer.readFloat();
         boolean canAlwaysEat = buffer.readBoolean();
         float eatSeconds = buffer.readFloat();
         FoodProperties1_20_5.FoodEffect[] possibleEffects = FoodProperties1_20_5.FoodEffect.ARRAY_TYPE.read(buffer);
         return new FoodProperties1_20_5(nutrition, saturationModifier, canAlwaysEat, eatSeconds, null, possibleEffects);
      }

      public void write(ByteBuf buffer, FoodProperties1_20_5 value) {
         Types.VAR_INT.writePrimitive(buffer, value.nutrition);
         buffer.writeFloat(value.saturationModifier);
         buffer.writeBoolean(value.canAlwaysEat);
         buffer.writeFloat(value.eatSeconds);
         FoodProperties1_20_5.FoodEffect.ARRAY_TYPE.write(buffer, value.possibleEffects);
      }
   };
   public static final Type<FoodProperties1_20_5> TYPE1_21 = new Type<FoodProperties1_20_5>(FoodProperties1_20_5.class) {
      public FoodProperties1_20_5 read(ByteBuf buffer) {
         int nutrition = Types.VAR_INT.readPrimitive(buffer);
         float saturationModifier = buffer.readFloat();
         boolean canAlwaysEat = buffer.readBoolean();
         float eatSeconds = buffer.readFloat();
         Item usingConvertsTo = VersionedTypes.V1_21.optionalItem.read(buffer);
         FoodProperties1_20_5.FoodEffect[] possibleEffects = FoodProperties1_20_5.FoodEffect.ARRAY_TYPE.read(buffer);
         return new FoodProperties1_20_5(nutrition, saturationModifier, canAlwaysEat, eatSeconds, usingConvertsTo, possibleEffects);
      }

      public void write(ByteBuf buffer, FoodProperties1_20_5 value) {
         Types.VAR_INT.writePrimitive(buffer, value.nutrition);
         buffer.writeFloat(value.saturationModifier);
         buffer.writeBoolean(value.canAlwaysEat);
         buffer.writeFloat(value.eatSeconds);
         VersionedTypes.V1_21.optionalItem.write(buffer, value.usingConvertsTo);
         FoodProperties1_20_5.FoodEffect.ARRAY_TYPE.write(buffer, value.possibleEffects);
      }
   };

   public FoodProperties1_20_5 copy() {
      return new FoodProperties1_20_5(
         this.nutrition,
         this.saturationModifier,
         this.canAlwaysEat,
         this.eatSeconds,
         this.usingConvertsTo == null ? null : this.usingConvertsTo.copy(),
         Copyable.copy(this.possibleEffects)
      );
   }

   public record FoodEffect(PotionEffect effect, float probability) {
      public static final Type<FoodProperties1_20_5.FoodEffect> TYPE = new Type<FoodProperties1_20_5.FoodEffect>(FoodProperties1_20_5.FoodEffect.class) {
         public FoodProperties1_20_5.FoodEffect read(ByteBuf buffer) {
            PotionEffect effect = PotionEffect.TYPE.read(buffer);
            float probability = buffer.readFloat();
            return new FoodProperties1_20_5.FoodEffect(effect, probability);
         }

         public void write(ByteBuf buffer, FoodProperties1_20_5.FoodEffect value) {
            PotionEffect.TYPE.write(buffer, value.effect);
            buffer.writeFloat(value.probability);
         }
      };
      public static final Type<FoodProperties1_20_5.FoodEffect[]> ARRAY_TYPE = new ArrayType<>(TYPE);
   }
}
