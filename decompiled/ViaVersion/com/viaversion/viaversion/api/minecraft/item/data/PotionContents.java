package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PotionContents(@Nullable Integer potion, @Nullable Integer customColor, PotionEffect[] customEffects, @Nullable String customName)
   implements Copyable {
   public static final Type<PotionContents> TYPE1_20_5 = new Type<PotionContents>(PotionContents.class) {
      public PotionContents read(ByteBuf buffer) {
         Integer potion = buffer.readBoolean() ? Types.VAR_INT.readPrimitive(buffer) : null;
         Integer customColor = buffer.readBoolean() ? buffer.readInt() : null;
         PotionEffect[] customEffects = PotionEffect.ARRAY_TYPE.read(buffer);
         return new PotionContents(potion, customColor, customEffects, null);
      }

      public void write(ByteBuf buffer, PotionContents value) {
         buffer.writeBoolean(value.potion != null);
         if (value.potion != null) {
            Types.VAR_INT.writePrimitive(buffer, value.potion);
         }

         buffer.writeBoolean(value.customColor != null);
         if (value.customColor != null) {
            buffer.writeInt(value.customColor);
         }

         PotionEffect.ARRAY_TYPE.write(buffer, value.customEffects);
      }
   };
   public static final Type<PotionContents> TYPE1_21_2 = new Type<PotionContents>(PotionContents.class) {
      public PotionContents read(ByteBuf buffer) {
         Integer potion = buffer.readBoolean() ? Types.VAR_INT.readPrimitive(buffer) : null;
         Integer customColor = buffer.readBoolean() ? buffer.readInt() : null;
         PotionEffect[] customEffects = PotionEffect.ARRAY_TYPE.read(buffer);
         String customName = Types.OPTIONAL_STRING.read(buffer);
         return new PotionContents(potion, customColor, customEffects, customName);
      }

      public void write(ByteBuf buffer, PotionContents value) {
         buffer.writeBoolean(value.potion != null);
         if (value.potion != null) {
            Types.VAR_INT.writePrimitive(buffer, value.potion);
         }

         buffer.writeBoolean(value.customColor != null);
         if (value.customColor != null) {
            buffer.writeInt(value.customColor);
         }

         PotionEffect.ARRAY_TYPE.write(buffer, value.customEffects);
         Types.OPTIONAL_STRING.write(buffer, value.customName);
      }

      public void write(Ops ops, PotionContents value) {
         ops.writeMap(
            map -> map.writeOptional("potion", EnumTypes.POTION, value.potion)
               .writeOptional("custom_color", Types.INT, value.customColor)
               .writeOptional("custom_effects", PotionEffect.ARRAY_TYPE, value.customEffects, new PotionEffect[0])
               .writeOptional("custom_name", Types.STRING, value.customName)
         );
      }
   };

   public PotionContents(@Nullable Integer potion, @Nullable Integer customColor, PotionEffect[] customEffects) {
      this(potion, customColor, customEffects, null);
   }

   public PotionContents copy() {
      return new PotionContents(this.potion, this.customColor, Copyable.copy(this.customEffects), this.customName);
   }
}
