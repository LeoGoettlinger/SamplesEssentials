package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.chicken.ChickenSoundVariant;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.chicken.ChickenSoundVariants;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class ChickenSoundVariantComponent {
   private final ChickenSoundVariant soundVariant;

   public ChickenSoundVariantComponent(ChickenSoundVariant soundVariant) {
      this.soundVariant = soundVariant;
   }

   public static ChickenSoundVariantComponent read(PacketWrapper<?> wrapper) {
      ChickenSoundVariant type = wrapper.readMappedEntity(ChickenSoundVariants.getRegistry());
      return new ChickenSoundVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, ChickenSoundVariantComponent component) {
      wrapper.writeMappedEntity(component.soundVariant);
   }

   public ChickenSoundVariant getSoundVariant() {
      return this.soundVariant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ChickenSoundVariantComponent)) {
         return false;
      } else {
         ChickenSoundVariantComponent that = (ChickenSoundVariantComponent)obj;
         return this.soundVariant.equals(that.soundVariant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.soundVariant);
   }
}
