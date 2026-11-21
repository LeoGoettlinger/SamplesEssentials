package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public final class SoundEventType extends HolderType<SoundEvent> {
   public SoundEventType() {
      super(MappingData.MappingType.SOUND);
   }

   public SoundEvent readDirect(ByteBuf buffer) {
      String identifier = Types.STRING.read(buffer);
      Float fixedRange = Types.OPTIONAL_FLOAT.read(buffer);
      return new SoundEvent(identifier, fixedRange);
   }

   public void writeDirect(ByteBuf buffer, SoundEvent value) {
      Types.STRING.write(buffer, value.identifier());
      Types.OPTIONAL_FLOAT.write(buffer, value.fixedRange());
   }

   public void writeDirect(Ops ops, SoundEvent object) {
      ops.writeMap(map -> map.write("sound_id", Types.IDENTIFIER, Key.of(object.identifier())).writeOptional("range", Types.FLOAT, object.fixedRange()));
   }

   public static final class OptionalSoundEventType extends HolderType.OptionalHolderType<SoundEvent> {
      public OptionalSoundEventType() {
         super(Types.SOUND_EVENT);
      }
   }
}
