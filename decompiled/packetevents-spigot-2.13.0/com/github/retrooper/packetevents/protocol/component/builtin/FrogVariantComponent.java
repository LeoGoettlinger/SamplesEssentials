package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariant;
import com.github.retrooper.packetevents.protocol.entity.frog.FrogVariants;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class FrogVariantComponent {
   private FrogVariant variant;

   public FrogVariantComponent(FrogVariant variant) {
      this.variant = variant;
   }

   public static FrogVariantComponent read(PacketWrapper<?> wrapper) {
      FrogVariant variant = wrapper.readMappedEntity(FrogVariants.getRegistry());
      return new FrogVariantComponent(variant);
   }

   public static void write(PacketWrapper<?> wrapper, FrogVariantComponent component) {
      wrapper.writeMappedEntity(component.variant);
   }

   public FrogVariant getVariant() {
      return this.variant;
   }

   public void setVariant(FrogVariant variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof FrogVariantComponent)) {
         return false;
      } else {
         FrogVariantComponent that = (FrogVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
