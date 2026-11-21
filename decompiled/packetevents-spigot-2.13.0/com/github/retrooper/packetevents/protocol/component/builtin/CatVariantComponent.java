package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.cat.CatVariant;
import com.github.retrooper.packetevents.protocol.entity.cat.CatVariants;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class CatVariantComponent {
   private CatVariant variant;

   public CatVariantComponent(CatVariant variant) {
      this.variant = variant;
   }

   public static CatVariantComponent read(PacketWrapper<?> wrapper) {
      CatVariant type = wrapper.readMappedEntity(CatVariants.getRegistry());
      return new CatVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, CatVariantComponent component) {
      wrapper.writeMappedEntity(component.variant);
   }

   public CatVariant getVariant() {
      return this.variant;
   }

   public void setVariant(CatVariant variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CatVariantComponent)) {
         return false;
      } else {
         CatVariantComponent that = (CatVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
