package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.pig.PigSoundVariant;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.pig.PigSoundVariants;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class PigSoundVariantComponent {
   private final PigSoundVariant soundVariant;

   public PigSoundVariantComponent(PigSoundVariant soundVariant) {
      this.soundVariant = soundVariant;
   }

   public static PigSoundVariantComponent read(PacketWrapper<?> wrapper) {
      PigSoundVariant type = wrapper.readMappedEntity(PigSoundVariants.getRegistry());
      return new PigSoundVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, PigSoundVariantComponent component) {
      wrapper.writeMappedEntity(component.soundVariant);
   }

   public PigSoundVariant getSoundVariant() {
      return this.soundVariant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof PigSoundVariantComponent)) {
         return false;
      } else {
         PigSoundVariantComponent that = (PigSoundVariantComponent)obj;
         return this.soundVariant.equals(that.soundVariant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.soundVariant);
   }
}
