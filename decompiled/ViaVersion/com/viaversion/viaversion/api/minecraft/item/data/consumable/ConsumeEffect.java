package com.viaversion.viaversion.api.minecraft.item.data.consumable;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.data.EnumTypes;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;

public record ConsumeEffect<T>(int id, Type<T> type, T value) {
   private static final HolderSetType REMOVE_EFFECTS_TYPE = new HolderSetType() {
      @Override
      public void write(Ops ops, HolderSet value) {
         ops.writeMap(map -> map.write("effects", new HolderSetType(EnumTypes.MOB_EFFECT), value));
      }
   };
   public static final Type<Float> TELEPORT_RANDOMLY_TYPE1_21_2 = new Type<Float>(Float.class) {
      public void write(ByteBuf buffer, Float value) {
         Types.FLOAT.writePrimitive(buffer, value);
      }

      public Float read(ByteBuf buffer) {
         return Types.FLOAT.readPrimitive(buffer);
      }

      public void write(Ops ops, Float value) {
         ops.writeMap(map -> map.writeOptional("diameter", Types.FLOAT, value, 16.0F));
      }
   };
   public static final Type<TeleportRandomlyConsumeEffect> TELEPORT_RANDOMLY_TYPE26_3 = new Type<TeleportRandomlyConsumeEffect>(
      TeleportRandomlyConsumeEffect.class
   ) {
      public void write(ByteBuf buffer, TeleportRandomlyConsumeEffect value) {
         Types.FLOAT.writePrimitive(buffer, value.diameter());
         Types.BOOLEAN.write(buffer, value.directionalParticles());
      }

      public TeleportRandomlyConsumeEffect read(ByteBuf buffer) {
         float diameter = Types.FLOAT.readPrimitive(buffer);
         boolean directionalParticles = Types.BOOLEAN.read(buffer);
         return new TeleportRandomlyConsumeEffect(diameter, directionalParticles);
      }

      public void write(Ops ops, TeleportRandomlyConsumeEffect value) {
         ops.writeMap(
            map -> map.writeOptional("diameter", Types.FLOAT, value.diameter(), 16.0F)
               .writeOptional("directional_particles", Types.BOOLEAN, value.directionalParticles(), true)
         );
      }
   };
   private static final HolderType<SoundEvent> PLAY_SOUND_TYPE = new HolderType<SoundEvent>(MappingData.MappingType.SOUND) {
      public SoundEvent readDirect(ByteBuf buffer) {
         return Types.SOUND_EVENT.readDirect(buffer);
      }

      public void writeDirect(ByteBuf buffer, SoundEvent value) {
         Types.SOUND_EVENT.writeDirect(buffer, value);
      }

      @Override
      public void write(Ops ops, Holder<SoundEvent> value) {
         ops.writeMap(map -> map.write("sound", Types.SOUND_EVENT, value));
      }
   };
   public static final Type<?>[] EFFECT_TYPES1_21_2 = new Type[]{
      ApplyStatusEffects.TYPE, REMOVE_EFFECTS_TYPE, Types.EMPTY, TELEPORT_RANDOMLY_TYPE1_21_2, PLAY_SOUND_TYPE
   };
   public static final Type<?>[] EFFECT_TYPES26_3 = new Type[]{
      ApplyStatusEffects.TYPE, REMOVE_EFFECTS_TYPE, Types.EMPTY, TELEPORT_RANDOMLY_TYPE26_3, PLAY_SOUND_TYPE
   };
   public static final Type<ConsumeEffect<?>> TYPE1_21_2 = new ConsumeEffect.ConsumeEffectType(EFFECT_TYPES1_21_2);
   public static final Type<ConsumeEffect<?>[]> ARRAY_TYPE1_21_2 = new ArrayType<>(TYPE1_21_2);
   public static final Type<ConsumeEffect<?>> TYPE26_3 = new ConsumeEffect.ConsumeEffectType(EFFECT_TYPES26_3);
   public static final Type<ConsumeEffect<?>[]> ARRAY_TYPE26_3 = new ArrayType<>(TYPE26_3);

   static <T> ConsumeEffect<T> of(int id, Type<T> type, Object value) {
      return new ConsumeEffect<>(id, type, (T)value);
   }

   void writeValue(ByteBuf buf) {
      this.type.write(buf, this.value);
   }

   private static final class ConsumeEffectType extends Type<ConsumeEffect<?>> {
      private final Type<?>[] types;

      private ConsumeEffectType(Type<?>[] types) {
         super(ConsumeEffect.class);
         this.types = types;
      }

      public ConsumeEffect<?> read(ByteBuf buffer) {
         int effectType = Types.VAR_INT.readPrimitive(buffer);
         Type<?> type = this.types[effectType];
         Object value = type.read(buffer);
         return ConsumeEffect.of(effectType, type, value);
      }

      public void write(ByteBuf buffer, ConsumeEffect<?> value) {
         Types.VAR_INT.writePrimitive(buffer, value.id);
         value.writeValue(buffer);
      }

      public void write(Ops ops, ConsumeEffect<?> value) {
         this.writeGeneric(ops, value);
      }

      private <E> void writeGeneric(Ops ops, ConsumeEffect<E> value) {
         ops.writeMap(map -> map.write("type", EnumTypes.CONSUME_EFFECT, value.id).writeInlinedMap(value.type, value.value));
      }
   }
}
