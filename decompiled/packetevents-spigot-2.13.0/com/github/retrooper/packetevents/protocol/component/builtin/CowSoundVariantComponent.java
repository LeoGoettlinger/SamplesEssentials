package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.cow.CowSoundVariant;
import com.github.retrooper.packetevents.protocol.entity.cow.CowSoundVariants;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class CowSoundVariantComponent {
   private final CowSoundVariant soundVariant;

   public CowSoundVariantComponent(CowSoundVariant soundVariant) {
      this.soundVariant = soundVariant;
   }

   public static CowSoundVariantComponent read(PacketWrapper<?> wrapper) {
      CowSoundVariant type = wrapper.readMappedEntity(CowSoundVariants.getRegistry());
      return new CowSoundVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, CowSoundVariantComponent component) {
      wrapper.writeMappedEntity(component.soundVariant);
   }

   public CowSoundVariant getSoundVariant() {
      return this.soundVariant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CowSoundVariantComponent)) {
         return false;
      } else {
         CowSoundVariantComponent that = (CowSoundVariantComponent)obj;
         return this.soundVariant.equals(that.soundVariant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.soundVariant);
   }
}
