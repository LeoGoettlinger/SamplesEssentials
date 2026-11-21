package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.nautilus.ZombieNautilusVariant;
import com.github.retrooper.packetevents.protocol.entity.nautilus.ZombieNautilusVariants;
import com.github.retrooper.packetevents.protocol.mapper.MaybeMappedEntity;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class ZombieNautilusVariantComponent {
   private MaybeMappedEntity<ZombieNautilusVariant> variant;

   public ZombieNautilusVariantComponent(ZombieNautilusVariant variant) {
      this(new MaybeMappedEntity<>(variant));
   }

   public ZombieNautilusVariantComponent(MaybeMappedEntity<ZombieNautilusVariant> variant) {
      this.variant = variant;
   }

   public static ZombieNautilusVariantComponent read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         return new ZombieNautilusVariantComponent(ZombieNautilusVariant.read(wrapper));
      } else {
         MaybeMappedEntity<ZombieNautilusVariant> variant = MaybeMappedEntity.read(wrapper, ZombieNautilusVariants.getRegistry(), ZombieNautilusVariant::read);
         return new ZombieNautilusVariantComponent(variant);
      }
   }

   public static void write(PacketWrapper<?> wrapper, ZombieNautilusVariantComponent component) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_26_1)) {
         ZombieNautilusVariant.write(wrapper, component.variant.getValueOrThrow());
      } else {
         MaybeMappedEntity.write(wrapper, component.variant, ZombieNautilusVariant::write);
      }
   }

   public MaybeMappedEntity<ZombieNautilusVariant> getVariant() {
      return this.variant;
   }

   public void setVariant(MaybeMappedEntity<ZombieNautilusVariant> variant) {
      this.variant = variant;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ZombieNautilusVariantComponent)) {
         return false;
      } else {
         ZombieNautilusVariantComponent that = (ZombieNautilusVariantComponent)obj;
         return this.variant.equals(that.variant);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.variant);
   }
}
