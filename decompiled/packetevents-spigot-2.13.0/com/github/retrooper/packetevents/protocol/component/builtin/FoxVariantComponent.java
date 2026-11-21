package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.fox.FoxVariant;
import com.github.retrooper.packetevents.protocol.entity.fox.FoxVariants;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class FoxVariantComponent {
   private FoxVariant variant;

   public FoxVariantComponent(FoxVariant variant) {
      this.variant = variant;
   }

   public static FoxVariantComponent read(PacketWrapper<?> wrapper) {
      FoxVariant type = wrapper.readMappedEntity(FoxVariants.getRegistry());
      return new FoxVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, FoxVariantComponent component) {
      wrapper.writeMappedEntity(component.variant);
   }

   public FoxVariant getVariant() {
      return this.variant;
   }

   public void setVariant(FoxVariant variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof FoxVariantComponent)) {
         return false;
      } else {
         FoxVariantComponent that = (FoxVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
