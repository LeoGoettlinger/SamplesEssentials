package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PotionEffectData(int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon, @Nullable PotionEffectData hiddenEffect) {
   public static final Type<PotionEffectData> TYPE = new Type<PotionEffectData>(PotionEffectData.class) {
      public PotionEffectData read(ByteBuf buffer) {
         int amplifier = Types.VAR_INT.readPrimitive(buffer);
         int duration = Types.VAR_INT.readPrimitive(buffer);
         boolean ambient = buffer.readBoolean();
         boolean showParticles = buffer.readBoolean();
         boolean showIcon = buffer.readBoolean();
         PotionEffectData hiddenEffect = PotionEffectData.OPTIONAL_TYPE.read(buffer);
         return new PotionEffectData(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
      }

      public void write(ByteBuf buffer, PotionEffectData value) {
         Types.VAR_INT.writePrimitive(buffer, value.amplifier);
         Types.VAR_INT.writePrimitive(buffer, value.duration);
         buffer.writeBoolean(value.ambient);
         buffer.writeBoolean(value.showParticles);
         buffer.writeBoolean(value.showIcon);
         PotionEffectData.OPTIONAL_TYPE.write(buffer, value.hiddenEffect);
      }

      public void write(Ops ops, PotionEffectData value) {
         ops.writeMap(
            map -> map.writeOptional("amplifier", Types.UNSIGNED_BYTE, (short)value.amplifier, (short)0)
               .writeOptional("duration", Types.INT, value.duration, 0)
               .writeOptional("ambient", Types.BOOLEAN, value.ambient, false)
               .writeOptional("show_particles", Types.BOOLEAN, value.showParticles, true)
               .writeOptional("show_icon", Types.BOOLEAN, value.showIcon)
               .writeOptional("hidden_effect", PotionEffectData.TYPE, value.hiddenEffect)
         );
      }
   };
   public static final Type<PotionEffectData> OPTIONAL_TYPE = new OptionalType<PotionEffectData>(TYPE) {};
}
