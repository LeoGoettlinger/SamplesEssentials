package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cat.CatSoundVariant;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.cat.CatSoundVariants;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class CatSoundVariantComponent {
   private final CatSoundVariant soundVariant;

   public CatSoundVariantComponent(CatSoundVariant soundVariant) {
      this.soundVariant = soundVariant;
   }

   public static CatSoundVariantComponent read(PacketWrapper<?> wrapper) {
      CatSoundVariant type = wrapper.readMappedEntity(CatSoundVariants.getRegistry());
      return new CatSoundVariantComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, CatSoundVariantComponent component) {
      wrapper.writeMappedEntity(component.soundVariant);
   }

   public CatSoundVariant getSoundVariant() {
      return this.soundVariant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CatSoundVariantComponent)) {
         return false;
      } else {
         CatSoundVariantComponent that = (CatSoundVariantComponent)obj;
         return this.soundVariant.equals(that.soundVariant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.soundVariant);
   }
}
