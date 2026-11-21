package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record Instrument1_20_5(Holder<SoundEvent> soundEvent, int useDuration, float range) implements Rewritable {
   public static final HolderType<Instrument1_20_5> TYPE = new HolderType<Instrument1_20_5>() {
      public Instrument1_20_5 readDirect(ByteBuf buffer) {
         Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
         int useDuration = Types.VAR_INT.readPrimitive(buffer);
         float range = Types.FLOAT.readPrimitive(buffer);
         return new Instrument1_20_5(soundEvent, useDuration, range);
      }

      public void writeDirect(ByteBuf buffer, Instrument1_20_5 value) {
         Types.SOUND_EVENT.write(buffer, value.soundEvent());
         Types.VAR_INT.writePrimitive(buffer, value.useDuration());
         Types.FLOAT.writePrimitive(buffer, value.range());
      }
   };

   public Instrument1_20_5 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      Holder<SoundEvent> soundEvent = SoundEvent.rewriteHolder(this.soundEvent, Rewritable.soundRewriteFunction(protocol, clientbound));
      return soundEvent == this.soundEvent ? this : new Instrument1_20_5(soundEvent, this.useDuration, this.range);
   }
}
