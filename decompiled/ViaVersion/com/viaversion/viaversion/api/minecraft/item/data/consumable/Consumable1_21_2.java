package com.viaversion.viaversion.api.minecraft.item.data.consumable;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.EnumType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record Consumable1_21_2(float consumeSeconds, int animationType, Holder<SoundEvent> sound, boolean hasConsumeParticles, ConsumeEffect<?>[] consumeEffects)
   implements Copyable,
   Rewritable {
   public static final Type<Consumable1_21_2> TYPE1_21_2 = new Consumable1_21_2.ConsumableType(ConsumeEffect.ARRAY_TYPE1_21_2, EnumTypes.ITEM_USE_ANIMATION);
   public static final Type<Consumable1_21_2> TYPE1_21_5 = new Consumable1_21_2.ConsumableType(
      ConsumeEffect.ARRAY_TYPE1_21_2, EnumTypes.ITEM_USE_ANIMATION1_21_5
   );
   public static final Type<Consumable1_21_2> TYPE26_3 = new Consumable1_21_2.ConsumableType(ConsumeEffect.ARRAY_TYPE26_3, EnumTypes.ITEM_USE_ANIMATION);

   public Consumable1_21_2 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      Holder<SoundEvent> soundHolder = SoundEvent.rewriteHolder(this.sound, Rewritable.soundRewriteFunction(protocol, clientbound));
      return soundHolder == this.sound
         ? this
         : new Consumable1_21_2(this.consumeSeconds, this.animationType, soundHolder, this.hasConsumeParticles, this.consumeEffects);
   }

   public Consumable1_21_2 copy() {
      return new Consumable1_21_2(this.consumeSeconds, this.animationType, this.sound, this.hasConsumeParticles, Copyable.copy(this.consumeEffects));
   }

   private static final class ConsumableType extends Type<Consumable1_21_2> {
      private final Type<ConsumeEffect<?>[]> consumeEffectType;
      private final EnumType animationType;

      private ConsumableType(Type<ConsumeEffect<?>[]> consumeEffectType, EnumType animationType) {
         super(Consumable1_21_2.class);
         this.consumeEffectType = consumeEffectType;
         this.animationType = animationType;
      }

      public Consumable1_21_2 read(ByteBuf buffer) {
         float consumeSeconds = buffer.readFloat();
         int animationType = Types.VAR_INT.readPrimitive(buffer);
         Holder<SoundEvent> sound = Types.SOUND_EVENT.read(buffer);
         boolean hasConsumeParticles = buffer.readBoolean();
         ConsumeEffect<?>[] consumeEffects = this.consumeEffectType.read(buffer);
         return new Consumable1_21_2(consumeSeconds, animationType, sound, hasConsumeParticles, consumeEffects);
      }

      public void write(ByteBuf buffer, Consumable1_21_2 value) {
         buffer.writeFloat(value.consumeSeconds);
         Types.VAR_INT.writePrimitive(buffer, value.animationType);
         Types.SOUND_EVENT.write(buffer, value.sound);
         buffer.writeBoolean(value.hasConsumeParticles);
         this.consumeEffectType.write(buffer, value.consumeEffects);
      }

      public void write(Ops ops, Consumable1_21_2 value) {
         Holder<SoundEvent> defaultSound = Holder.of(ops.context().registryAccess().id(MappingData.MappingType.SOUND, "entity.generic.eat"));
         ops.writeMap(
            map -> map.writeOptional("consume_seconds", Types.FLOAT, value.consumeSeconds, 1.6F)
               .writeOptional("animation", this.animationType, value.animationType, 1)
               .writeOptional("sound", Types.SOUND_EVENT, value.sound, defaultSound)
               .writeOptional("has_consume_particles", Types.BOOLEAN, value.hasConsumeParticles, true)
               .writeOptional("on_consume_effects", this.consumeEffectType, value.consumeEffects, new ConsumeEffect[0])
         );
      }
   }
}
