package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.rabbit.RabbitVariant;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.rabbit.RabbitVariants;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class RabbitVariantComponent {
   private RabbitVariant variant;

   public RabbitVariantComponent(RabbitVariant variant) {
      this.variant = variant;
   }

   public static RabbitVariantComponent read(PacketWrapper<?> wrapper) {
      RabbitVariant type = wrapper.readMappedEntity(RabbitVariants.getRegistry());
      return new RabbitVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, RabbitVariantComponent component) {
      wrapper.writeMappedEntity(component.variant);
   }

   public RabbitVariant getVariant() {
      return this.variant;
   }

   public void setVariant(RabbitVariant variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof RabbitVariantComponent)) {
         return false;
      } else {
         RabbitVariantComponent that = (RabbitVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
