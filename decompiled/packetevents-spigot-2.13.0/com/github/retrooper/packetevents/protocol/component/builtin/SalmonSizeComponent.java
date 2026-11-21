package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.salmon.SalmonVariant;
import com.github.retrooper.packetevents.protocol.entity.salmon.SalmonVariants;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class SalmonSizeComponent {
   private SalmonVariant variant;

   public SalmonSizeComponent(SalmonVariant variant) {
      this.variant = variant;
   }

   public static SalmonSizeComponent read(PacketWrapper<?> wrapper) {
      SalmonVariant type = wrapper.readMappedEntity(SalmonVariants.getRegistry());
      return new SalmonSizeComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, SalmonSizeComponent component) {
      wrapper.writeMappedEntity(component.variant);
   }

   public SalmonVariant getVariant() {
      return this.variant;
   }

   public void setVariant(SalmonVariant variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof SalmonSizeComponent)) {
         return false;
      } else {
         SalmonSizeComponent that = (SalmonSizeComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
