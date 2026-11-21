package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.parrot.ParrotVariant;
import com.github.retrooper.packetevents.protocol.entity.parrot.ParrotVariants;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class ParrotVariantComponent {
   private ParrotVariant variant;

   public ParrotVariantComponent(ParrotVariant variant) {
      this.variant = variant;
   }

   public static ParrotVariantComponent read(PacketWrapper<?> wrapper) {
      ParrotVariant type = wrapper.readMappedEntity(ParrotVariants.getRegistry());
      return new ParrotVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, ParrotVariantComponent component) {
      wrapper.writeMappedEntity(component.variant);
   }

   public ParrotVariant getVariant() {
      return this.variant;
   }

   public void setVariant(ParrotVariant variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ParrotVariantComponent)) {
         return false;
      } else {
         ParrotVariantComponent that = (ParrotVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
