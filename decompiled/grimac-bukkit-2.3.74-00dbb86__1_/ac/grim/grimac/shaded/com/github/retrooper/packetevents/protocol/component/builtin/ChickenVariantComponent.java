package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariant;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.chicken.ChickenVariants;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MaybeMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;

public class ChickenVariantComponent {
   private MaybeMappedEntity<ChickenVariant> variant;

   public ChickenVariantComponent(ChickenVariant variant) {
      this(new MaybeMappedEntity<>(variant));
   }

   @ApiStatus.Obsolete
   public ChickenVariantComponent(MaybeMappedEntity<ChickenVariant> variant) {
      this.variant = variant;
   }

   public static ChickenVariantComponent read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         return new ChickenVariantComponent(ChickenVariant.read(wrapper));
      } else {
         MaybeMappedEntity<ChickenVariant> variant = MaybeMappedEntity.read(wrapper, ChickenVariants.getRegistry(), ChickenVariant::read);
         return new ChickenVariantComponent(variant);
      }
   }

   public static void write(PacketWrapper<?> wrapper, ChickenVariantComponent component) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         ChickenVariant.write(wrapper, component.variant.getValueOrThrow());
      } else {
         MaybeMappedEntity.write(wrapper, component.variant, ChickenVariant::write);
      }
   }

   public MaybeMappedEntity<ChickenVariant> getVariant() {
      return this.variant;
   }

   public void setVariant(MaybeMappedEntity<ChickenVariant> variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ChickenVariantComponent)) {
         return false;
      } else {
         ChickenVariantComponent that = (ChickenVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
