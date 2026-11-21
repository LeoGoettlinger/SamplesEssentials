package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.painting.PaintingVariant;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class PaintingVariantComponent {
   private PaintingVariant variant;

   public PaintingVariantComponent(PaintingVariant variant) {
      this.variant = variant;
   }

   public static PaintingVariantComponent read(PacketWrapper<?> wrapper) {
      PaintingVariant variant = PaintingVariant.read(wrapper);
      return new PaintingVariantComponent(variant);
   }

   public static void write(PacketWrapper<?> wrapper, PaintingVariantComponent component) {
      PaintingVariant.write(wrapper, component.variant);
   }

   public PaintingVariant getVariant() {
      return this.variant;
   }

   public void setVariant(PaintingVariant variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof PaintingVariantComponent)) {
         return false;
      } else {
         PaintingVariantComponent that = (PaintingVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
