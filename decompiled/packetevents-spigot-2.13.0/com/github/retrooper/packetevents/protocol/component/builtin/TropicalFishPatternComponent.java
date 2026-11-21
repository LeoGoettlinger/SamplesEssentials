package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.entity.tropicalfish.TropicalFishPattern;
import com.github.retrooper.packetevents.protocol.entity.tropicalfish.TropicalFishPatterns;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class TropicalFishPatternComponent {
   private TropicalFishPattern variant;

   public TropicalFishPatternComponent(TropicalFishPattern variant) {
      this.variant = variant;
   }

   public static TropicalFishPatternComponent read(PacketWrapper<?> wrapper) {
      TropicalFishPattern type = wrapper.readMappedEntity(TropicalFishPatterns.getRegistry());
      return new TropicalFishPatternComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, TropicalFishPatternComponent component) {
      wrapper.writeMappedEntity(component.variant);
   }

   public TropicalFishPattern getVariant() {
      return this.variant;
   }

   public void setVariant(TropicalFishPattern variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof TropicalFishPatternComponent)) {
         return false;
      } else {
         TropicalFishPatternComponent that = (TropicalFishPatternComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
